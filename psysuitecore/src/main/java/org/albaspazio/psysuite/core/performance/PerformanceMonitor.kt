package org.albaspazio.psysuite.core.performance

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.SingletonHolder
import org.albaspazio.psysuite.tests.TrialBasic
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

/**
 * Performance Monitor Singleton
 *
 * Orchestrates CPU/memory resource monitoring across event-driven phases:
 * - Phase 0: ADOPY Initialization
 * - Phase 1: Pre-set (200ms before update)
 * - Phase 2: Update model
 * - Phase 3: Get trial stimulus
 * - Phase 4: Post get (200ms cool-down)
 *
 * @param context Application context for file I/O and system resource access
 */
class PerformanceMonitor(private val context: Context?) {

    init {
        if (context == null) {
            throw IllegalStateException("PerformanceMonitor: context is null. Must be initialized in MainApplication.onCreate() first.")
        }
    }

    companion object : SingletonHolder<PerformanceMonitor, Context?>(::PerformanceMonitor) {
        private const val TAG = "PerformanceMonitor"
        
        /**
         * Returns singleton instance if already initialized, null otherwise
         * Safe to call without context parameter
         */
        fun getInstanceOrNull(): PerformanceMonitor? = instance
    }

    // Dynamic condition and algorithm derived from current trial
    var currentCondition: String = ""
    var currentAlgorithm: String = ""

    // Flag indicating whether peritrial monitoring has been started (skips initial trial 0 fetch)
    private var monitoringStarted: Boolean = false

    // ============================================================================
    // Phase State & Baseline Metrics
    // ============================================================================

    /**
     * Track current trial number for per-trial phase events
     * Starts at -1 before trials begin, increments on GETSTIM_START
     */
    private var trialNum: Int = -1

    /**
     * Timestamp of last event (START event) for calculating durations in END events
     */
    private var lastEventTimestamp: Long = 0

    /**
     * Phase 4 timeout: set atomically when GETSTIM_END triggers
     * Sampling loop checks this value to determine when to stop Phase 4
     */
    private val phase4EndTime: AtomicLong = AtomicLong(0)

    /**
     * Flag indicating whether sampling loop is currently active
     */
    private var samplingActive: Boolean = false

    /**
     * Current phase ID being monitored: "0" (Init), "1" (Pre-set), "2" (Set), "3" (Get), "4" (Post-get)
     */
    private var currentPhaseId: String = ""

    // ============================================================================
    // Coroutine Management
    // ============================================================================

    /**
     * Scope for background coroutines (file I/O, sampling)
     * Uses Dispatchers.IO to keep main thread unblocked
     */
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    /**
     * Current sampling job (started when monitoring begins, cancelled when monitoring ends)
     */
    private var samplingJob: Job? = null

    // ============================================================================
    // Component Instances
    // ============================================================================

    /**
     * Event Recorder: synchronously records timing events (INIT, CYCLE, PHASE3_END)
     * Lazily initialized to avoid overhead when monitoring is disabled
     */
    private val eventRecorder: PerformanceEventRecorder by lazy {
        PerformanceEventRecorder()
    }

    /**
     * File Writer: asynchronously writes samples and events to TSV files with batching and retry
     * Initialized when initializeFileWriter() is called with a filesPrefix parameter
     */
    private var fileWriter: PerformanceFileWriter? = null

    /**
     * Initialize the FileWriter with a specific filesPrefix and algorithm mode.
     */
    fun initializeMonitor(filesPrefix: String, isAda:Boolean) {

        currentAlgorithm = if(isAda) "ADA" else "FIX"

        if (fileWriter == null) {
            fileWriter = PerformanceFileWriter(
                context = context!!,
                filesPrefix = filesPrefix
            )
            Log.d("PERF_DEBUG", "✅ FileWriter initialized with prefix: $filesPrefix (isAda: $isAda)")
        } else {
            Log.d("PERF_DEBUG", "⚠️ FileWriter already initialized")
        }
    }

    // ============================================================================
    // Main Event Interface: triggerEvent()
    // ============================================================================

    /**
     * Primary method for controlling performance monitoring via event-driven triggers.
     *
     * CRITICAL: This is the ONLY method called from TestXXX, TestFragment, and TestBasic.
     * All phase transitions, baseline captures, and sampling control flows through this method.
     *
     * This simplified API handles ALL timestamp and trial_num calculations internally:
     * - Timestamps are calculated using System.currentTimeMillis()
     * - Durations are calculated as (current_timestamp - lastEventTimestamp)
     *
     * Event Types:
     * - "ADOPY_INIT_START": engine initialization
     * - "ADOPY_INIT_END": engine ready, INIT event recorded
     * - "UPDATE_START": Phase 1 begins (update model, set command)
     * - "UPDATE_END": Phase 1 ends (update model, set command)
     *
     * @param eventType String identifying the event ("ADOPY_INIT_START", "ADOPY_INIT_END", etc.)
     * @param trial Optional TrialBasic instance to extract label/condition and algorithm
     */
    fun triggerEvent(eventType: String, trial: TrialBasic? = null) {
        try {
            if (trial != null) {
                currentCondition = trial.label
                currentAlgorithm = trial.algorithm
                trialNum         = trial.id
            }

            when (eventType) {
                "ADOPY_INIT_START"           -> handleAdopyInitStart()
                "ADOPY_INIT_END"             -> handleAdopyInitEnd()
                "PERITRIAL_MONITORING_START" -> handlePeriTrialMonitoringStart()
                "UPDATE_START"               -> handleUpdateStart()
                "UPDATE_END"                 -> handleUpdateEnd()
                "GETSTIM_START"              -> handleGetStimulusStart()
                "GETSTIM_END"                -> handleGetStimulusEnd()
                else -> {
                    Log.w(TAG, "Unknown event type: $eventType")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in triggerEvent($eventType): ${e.message}", e)
            // Continue without re-throwing; monitoring failures should not crash trials
        }
    }

    // ============================================================================
    // Phase 0: ADOPY Initialization
    // ============================================================================

    /**
     * Handle ADOPY_INIT_START event:
     * 1. Record current timestamp internally
     * 2. Measure baseline CPU/RAM INSIDE this method (before sampling starts)
     * 3. Store baseline for later inclusion in INIT event
     * 4. Start Phase 0 sampling at 100Hz
     *
     * Called by: TestBIS.initTest() immediately before ADOWrapper instantiation
     */
    private fun handleAdopyInitStart() {

        Log.d(TAG, "ADOPY_INIT_START: baseline capture + Phase 0 sampling begin")

        val timestamp_ms    = System.currentTimeMillis()
        val cpu             = Device.getAppCpuPercent()
        val ram             = Device.getAppRamMb()

        currentCondition = "INIT"

        eventRecorder.recordEvent(
            type = "ADOPY_INIT_START",
            duration_ms = 0,
            algorithm = currentAlgorithm,   // this first value it set by initializeMonitor according to whole task algorithm (in ADA, some trials could be also FIX)
            trial_count = 0,
            condition = currentCondition,
            timestamp_ms = timestamp_ms,
            baselineAppCpu_percent = cpu,
            baselineAppRam_mb = ram
        )
        fileWriter?.enqueueEvent(eventRecorder.getLastEvent())
        lastEventTimestamp = System.currentTimeMillis()

        Log.d(TAG, "  Baseline: CPU=${String.format("%.2f", cpu)}%, RAM=${String.format("%.2f", ram)}MB")

        currentPhaseId = "0"
        startSamplingLoop()
    }

    /**
     * Handle ADOPY_INIT_END event:
     * 1. Calculate current timestamp and duration internally
     * 2. Stop Phase 0 sampling
     * 3. Record INIT event with duration and baseline metrics
     *
     * Called by: TestBIS.initTest() immediately after ADOWrapper is fully constructed (after first get() is called)
     */
    private fun handleAdopyInitEnd() {
        Log.d(TAG, "ADOPY_INIT_END: Phase 0 sampling stop + INIT event recorded")

        val timestamp_ms    = System.currentTimeMillis()
        val duration_ms     = timestamp_ms - lastEventTimestamp

        val cpu             = Device.getAppCpuPercent()
        val ram             = Device.getAppRamMb()

        samplingActive = false

        eventRecorder.recordEvent(
            type = "ADOPY_INIT_END",
            duration_ms = duration_ms,
            algorithm = currentAlgorithm,
            trial_count = 0,
            condition = currentCondition,   // INIT, as set by handleAdopyInitStart()
            timestamp_ms = timestamp_ms,
            baselineAppCpu_percent = cpu,
            baselineAppRam_mb = ram
        )

        fileWriter?.enqueueEvent(eventRecorder.getLastEvent())   // Queue event for asynchronous file writing
        lastEventTimestamp = timestamp_ms

        Log.d(TAG, "  INIT event: duration=${duration_ms}ms, cpu=${String.format("%.2f", cpu)}%, ram=${String.format("%.2f", ram)}MB")
    }

    // ============================================================================
    // Phase 1: Pre-set (200ms before UPDATE)
    // ============================================================================

    /**
     * Handle PERITRIAL_MONITORING_START event:
     * 1. Begin monitoring 200ms before UPDATE phase
     * 2. Start Phase 1 sampling at 100Hz
     * 3. Reset timestamp baseline (will be updated in UPDATE_START)
     *
     * Called by: TestBasic.setResponse() 200ms before UPDATE_START
     */
    private fun handlePeriTrialMonitoringStart() {
        Log.d(TAG, "PERITRIAL_MONITORING_START: begin monitoring 200ms before UPDATE")
        monitoringStarted = true
        lastEventTimestamp = System.currentTimeMillis()
        currentPhaseId = "1"
        startSamplingLoop()
    }

    // ============================================================================
    // Phase 2: UPDATE
    // ============================================================================

    /**
     * Handle UPDATE_START event:
     * 1. Record current timestamp internally
     * 2. Measure baseline CPU/RAM INSIDE this method (before sampling continues)
     * 3. Store baseline for later inclusion in UPDATE event
     *
     * Called by: TestBasic.setResponse() after 200ms delay
     */
    private fun handleUpdateStart() {
        val timestamp_ms = System.currentTimeMillis()
        Log.d(TAG, "UPDATE_START: model update phase begins (trial $trialNum)")

        val cpu     = Device.getAppCpuPercent()
        val ram     = Device.getAppRamMb()

        eventRecorder.recordEvent(
            type = "UPDATE_START",
            duration_ms = 0,
            algorithm = currentAlgorithm,
            trial_count = trialNum,
            condition = currentCondition,
            timestamp_ms = timestamp_ms,
            baselineAppCpu_percent = cpu,
            baselineAppRam_mb = ram
        )
        fileWriter?.enqueueEvent(eventRecorder.getLastEvent())
        lastEventTimestamp = timestamp_ms
        currentPhaseId = "2"
    }

    /**
     * Handle UPDATE_END event:
     * 1. Calculate duration from UPDATE_START
     * 2. Record UPDATE event with baseline metrics
     * 3. Queue event for file writing
     *
     * Called by: TestBasic.setResponse() after mTrialsManager.setResponse() completes
     */
    private fun handleUpdateEnd() {
        val timestamp_ms = System.currentTimeMillis()
        val duration_ms = timestamp_ms - lastEventTimestamp
        
        Log.d(TAG, "UPDATE_END: duration=${duration_ms}ms")

        val cpu = Device.getAppCpuPercent()
        val ram  = Device.getAppRamMb()

        eventRecorder.recordEvent(
            type = "UPDATE_END",
            duration_ms = duration_ms,
            algorithm = currentAlgorithm,
            trial_count = trialNum,
            condition = currentCondition,
            timestamp_ms = timestamp_ms,
            baselineAppCpu_percent = cpu,
            baselineAppRam_mb = ram
        )
        
        fileWriter?.enqueueEvent(eventRecorder.getLastEvent())
        lastEventTimestamp = timestamp_ms
    }

    // ============================================================================
    // Phase 3 & 4: GETSTIM and Post-get Cool-down
    // ============================================================================

    private fun handleGetStimulusStart() {
        val timestamp_ms = System.currentTimeMillis()
        Log.d(TAG, "GETSTIM_START: fetch new stimulus phase begins (trial $trialNum)")

        val cpu = Device.getAppCpuPercent()
        val ram  = Device.getAppRamMb()

        eventRecorder.recordEvent(
            type = "GETSTIM_START",
            duration_ms = 0,
            algorithm = currentAlgorithm,
            trial_count = trialNum,
            condition = currentCondition,
            timestamp_ms = timestamp_ms,
            baselineAppCpu_percent = cpu,
            baselineAppRam_mb = ram
        )

        fileWriter?.enqueueEvent(eventRecorder.getLastEvent())
        lastEventTimestamp = timestamp_ms

        if (monitoringStarted) {
            currentPhaseId = "3"
            startSamplingLoop()
        }
    }

    /**
     * Handle GETSTIM_END event:
     * 1. Calculate duration from GETSTIM_START
     * 2. Record GETSTIM event
     * 3. Queue event for file writing
     * 4. Start 200ms cool-down (Phase 3)
     *
     * Called by: TestBasic.doNextTrial() after mTrialsManager.getNewTrial() completes
     */
    private fun handleGetStimulusEnd() {
        val timestamp_ms = System.currentTimeMillis()
        val duration_ms = timestamp_ms - lastEventTimestamp
        
        Log.d(TAG, "GETSTIM_END: duration=${duration_ms}ms, starting 200ms cool-down")

        val cpu = Device.getAppCpuPercent()
        val ram  = Device.getAppRamMb()

        eventRecorder.recordEvent(
            type = "GETSTIM_END",
            duration_ms = duration_ms,
            algorithm = currentAlgorithm,
            trial_count = trialNum,
            condition = currentCondition,
            timestamp_ms = timestamp_ms,
            baselineAppCpu_percent = cpu,
            baselineAppRam_mb = ram
        )
        
        fileWriter?.enqueueEvent(eventRecorder.getLastEvent())
        
        if (monitoringStarted) {
            phase4EndTime.set(timestamp_ms + 200)
            currentPhaseId = "4"
            Log.d("PERF_DEBUG", "✅ GETSTIM_END: phase4EndTime set to ${timestamp_ms + 200}")
        }
        lastEventTimestamp = timestamp_ms
    }

    // ============================================================================
    // Sampling Loop Control
    // ============================================================================

    /**
     * Start the background sampling loop on Dispatchers.IO.
     *
     * The sampling loop collects CPU/memory metrics at 100Hz (10ms intervals) and queues them
     * for asynchronous file writing. The loop is done in two distinct phases:
     * - 0: model init
     * - continues through multiple phases (1: pre set, 2: set, 3: get, 4: post get) until explicitly stopped by Phase 4 timeout.
     *
     * Key properties:
     * - First sample recorded immediately at T+0 (no artificial 10ms delay)
     * - Subsequent samples at 10ms intervals (100Hz frequency)
     * - Phase 3→4 transition: sampling never stops, only phase_id tagging changes internally
     * - Phase 4 timeout checked atomically via phase4EndTime value
     * - Continuous operation across multiple trials within a test session
     */
    private fun startSamplingLoop() {
        if (samplingActive) {
            Log.d(TAG, "Sampling already active; not restarting")
            return
        }

        samplingActive = true
        Log.d("PERF_DEBUG", "✅ Sampling loop started")

        samplingJob = ioScope.launch {
            var firstSample = true
            var lastRecordedTimestamp: Long = 0

            while (samplingActive) {
                try {
                    val now = System.currentTimeMillis()
                    val phase4TimeLeft = if (phase4EndTime.get() > 0) phase4EndTime.get() - now else -1
                    if (now % 1000 < 50) Log.d("PERF_DEBUG", "⏱️  Sampling: now=$now, phase4EndTime=${phase4EndTime.get()}, timeLeft=${phase4TimeLeft}ms")

                    // Timestamp monotonicity check and adjustment
                    val timestamp = if (now < lastRecordedTimestamp) {
                                        Log.w(TAG, "Clock skew detected: $now < $lastRecordedTimestamp; adjusting")
                                        lastRecordedTimestamp + 1
                                    } else
                                        now

                    lastRecordedTimestamp = timestamp

                    // Check Phase 4 timeout
                    if (phase4EndTime.get() > 0 && timestamp >= phase4EndTime.get()) {
                        Log.d(TAG, "Phase 4 timeout reached; stopping sampling")
                        fileWriter?.flushSync()
                        phase4EndTime.set(0)
                        samplingActive = false
                        monitoringStarted = false
                        break
                    }

                    val deviceCpu = Device.getDeviceCpuPercent()
                    val deviceRam = Device.getSystemRam(context!!) / (1024.0f * 1024.0f) // Convert bytes to MB
                    val appCpu = Device.getAppCpuPercent()
                    val appRam = Device.getAppRamMb()

                    val sample = PerformanceSample(
                        condition = currentCondition.ifEmpty { "INIT" },
                        algorithm = currentAlgorithm.ifEmpty { "ADA" },
                        phase_id = currentPhaseId,
                        timestamp_ms = timestamp,
                        device_cpu_percent = deviceCpu,
                        device_ram_mb = deviceRam,
                        app_cpu_percent = appCpu,
                        app_ram_mb = appRam,
                        trial_num = if (trialNum < 0) 0 else trialNum
                    )

                    if (fileWriter != null) {
                        fileWriter!!.enqueueTimeseriesSample(sample)
                    } else {
                        Log.w("PERF_DEBUG", "⚠️  fileWriter is NULL at sampling time!")
                    }

                    // Timing: first sample immediate (T+0), subsequent at 10ms intervals
                    if (!firstSample) {
                        // Subsequent samples: 10ms interval (100Hz = 10ms)
                        delay(10.milliseconds)
                    } else {
                        firstSample = false
                        // No delay; return immediately to record first sample at T+0
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error in sampling loop: ${e.message}", e)
                    // Continue sampling despite error; do not crash
                }
            }

            Log.d(TAG, "Sampling loop exited")
        }
    }

    // ============================================================================
    // Finalization & Cleanup
    // ============================================================================

    /**
     * Performance monitoring session finalization
     */
    fun finalize() {
        Log.d("PERF_DEBUG", "⏸️  Finalizing performance monitoring...")

        samplingActive = false
        samplingJob?.cancel()

        Log.d("PERF_DEBUG", "💾 Calling fileWriter?.flushSync()...")
        fileWriter?.flushSync()

        Log.d("PERF_DEBUG", "✅ Performance monitoring finalized")
    }
}
