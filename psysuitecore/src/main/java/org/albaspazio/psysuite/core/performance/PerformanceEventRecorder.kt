package org.albaspazio.psysuite.core.performance

import android.util.Log

/**
 * Performance Event Recorder
 *
 * Synchronously records timing events (INIT, CYCLE, PHASE3_END) with baseline metrics.
 * Events represent major milestones in test execution:
 * - INIT: Engine initialization completion (Phase 1 end)
 * - CYCLE: Trial cycle completion (Phase 2 end)
 * - PHASE3_END: Post-trial cool-down completion (Phase 3 end, 200ms after trial)
 *
 * Each event includes duration and baseline CPU/memory metrics captured at phase start
 * (inside triggerEvent() before sampling began).
 *
 * Events are stored in memory and later queued to PerformanceFileWriter for asynchronous TSV output.
 */
class PerformanceEventRecorder {

    private companion object {
        private const val TAG = "PerformanceEventRecorder"
    }

    /**
     * In-memory list of recorded events, queued for file writing
     */
    private val recordedEvents: MutableList<PerformanceEvent> = mutableListOf()

    /**
     * Last recorded event (for easy retrieval after recording)
     */
    private var lastEvent: PerformanceEvent? = null

    // ========================================================================
    // Event Recording Methods
    // ========================================================================

    /**
     * Record an event.
     **
     * @param type event type
     * @param duration_ms Time from *_START to *_END
     * @param algorithm Trial management mode: "ADA" or "FIX"
     * @param trial_count Always 0 for INIT events (not trial-specific)
     * @param condition Test condition identifier (e.g., "BIS_AUDIO")
     * @param timestamp_ms ADOPY_INIT_START timestamp (event start time)
     * @param baselineAppCpu_percent App CPU usage (%) measured inside ADOPY_INIT_START
     * @param baselineAppRam_mb App RAM usage (MB) measured inside ADOPY_INIT_START
     */
    fun recordEvent(
        type:String,
        duration_ms: Long,
        algorithm: String,
        trial_count: Int,
        condition: String,
        timestamp_ms: Long,
        baselineAppCpu_percent: Float,
        baselineAppRam_mb: Float
    ) {
        val event = PerformanceEvent(
            EVENT_TYPE = type,
            duration_ms = duration_ms,
            algorithm = algorithm,
            trial_num = trial_count,
            condition = condition,
            timestamp_ms = timestamp_ms,
            baselineAppCpu_percent = baselineAppCpu_percent,
            baselineAppRam_mb = baselineAppRam_mb
        )

        recordedEvents.add(event)
        lastEvent = event

        Log.d(TAG, "Recorded $type event: duration=${duration_ms}ms, baseline CPU=${String.format("%.2f", baselineAppCpu_percent)}%, RAM=${String.format("%.2f", baselineAppRam_mb)}MB")
    }

    // ========================================================================
    // Event Retrieval Methods
    // ========================================================================

    /**
     * Get the last recorded event (for immediate use after recording).
     *
     * @return Last recorded PerformanceEvent, or null if no events recorded yet
     */
    fun getLastEvent(): PerformanceEvent? {
        return lastEvent
    }

    /**
     * Get all recorded events (for bulk export to file).
     *
     * @return List of all recorded PerformanceEvent objects
     */
    fun getAllEvents(): List<PerformanceEvent> {
        return recordedEvents.toList()
    }

    /**
     * Get count of recorded events.
     *
     * @return Number of events recorded so far
     */
    fun getEventCount(): Int {
        return recordedEvents.size
    }

    /**
     * Clear all recorded events (useful for test reset or session cleanup).
     */
    fun clearEvents() {
        recordedEvents.clear()
        lastEvent = null
    }
}

//
//    /**
//     * Record an UPDATE event (Model update phase completion).
//     *
//     * Called when model update phase completes (UPDATE_END event).
//     *
//     * @param duration_ms Time from UPDATE_START to UPDATE_END (update duration)
//     * @param algorithm Trial management mode: "ADA" or "FIX"
//     * @param trial_num Trial number
//     * @param condition Test condition identifier (e.g., "BIS_AUDIO")
//     * @param timestamp_ms UPDATE_END timestamp
//     * @param baselineAppCpu_percent App CPU usage (%) measured at UPDATE_START
//     * @param baselineAppRam_mb App RAM usage (MB) measured at UPDATE_START
//     */
//    fun recordUpdateEvent(
//        duration_ms: Long,
//        algorithm: String,
//        trial_num: Int,
//        condition: String,
//        timestamp_ms: Long,
//        baselineAppCpu_percent: Float,
//        baselineAppRam_mb: Float
//    ) {
//        val event = PerformanceEvent(
//            EVENT_TYPE = "UPDATE",
//            duration_ms = duration_ms,
//            algorithm = algorithm,
//            trial_num = trial_num,
//            condition = condition,
//            timestamp_ms = timestamp_ms,
//            baselineAppCpu_percent = baselineAppCpu_percent,
//            baselineAppRam_mb = baselineAppRam_mb
//        )
//
//        recordedEvents.add(event)
//        lastEvent = event
//
//        Log.d(TAG, "Recorded UPDATE event: duration=${duration_ms}ms, baseline CPU=${String.format("%.2f", baselineAppCpu_percent)}%, RAM=${String.format("%.2f", baselineAppRam_mb)}MB")
//    }
//
//    /**
//     * Record a GETSTIM event (Fetch stimulus phase completion).
//     *
//     * Called when fetch stimulus phase completes (GETSTIM_END event).
//     *
//     * @param duration_ms Time from GETSTIM_START to GETSTIM_END (fetch duration)
//     * @param algorithm Trial management mode: "ADA" or "FIX"
//     * @param trial_num Trial number
//     * @param condition Test condition identifier (e.g., "BIS_AUDIO")
//     * @param timestamp_ms GETSTIM_END timestamp
//     */
//    fun recordGetStimulusEvent(
//        duration_ms: Long,
//        algorithm: String,
//        trial_num: Int,
//        condition: String,
//        timestamp_ms: Long
//    ) {
//        val event = PerformanceEvent(
//            EVENT_TYPE = "GETSTIM",
//            duration_ms = duration_ms,
//            algorithm = algorithm,
//            trial_num = trial_num,
//            condition = condition,
//            timestamp_ms = timestamp_ms,
//            baselineAppCpu_percent = 0f,
//            baselineAppRam_mb = 0f
//        )
//
//        recordedEvents.add(event)
//        lastEvent = event
//
//        Log.d(TAG, "Recorded GETSTIM event: duration=${duration_ms}ms")
//    }


//    /**
//     * Record a CYCLE event (Phase 2: Per-Trial Monitoring completion).
//     *
//     * Called when trial cycle completes (PERI_TRIAL_END event).
//     *
//     * @param duration_ms Time from PERI_TRIAL_START to PERI_TRIAL_END (trial cycle duration)
//     * @param algorithm Trial management mode: "ADA" or "FIX"
//     * @param trial_num Trial number (1-indexed for user facing, 0-indexed may be used internally)
//     * @param condition Test condition identifier (e.g., "BIS_AUDIO")
//     * @param timestamp_ms PERI_TRIAL_START timestamp (event start time)
//     * @param baselineAppCpu_percent App CPU usage (%) measured inside PERI_TRIAL_START
//     * @param baselineAppRam_mb App RAM usage (MB) measured inside PERI_TRIAL_START
//     */
//    fun recordCycleEvent(
//        duration_ms: Long,
//        algorithm: String,
//        trial_num: Int,
//        condition: String,
//        timestamp_ms: Long,
//        baselineAppCpu_percent: Float,
//        baselineAppRam_mb: Float
//    ) {
//        val event = PerformanceEvent(
//            EVENT_TYPE = "CYCLE",
//            duration_ms = duration_ms,
//            algorithm = algorithm,
//            trial_num = trial_num,
//            condition = condition,
//            timestamp_ms = timestamp_ms,
//            baselineAppCpu_percent = baselineAppCpu_percent,
//            baselineAppRam_mb = baselineAppRam_mb
//        )
//
//        recordedEvents.add(event)
//        lastEvent = event
//
//        Log.d(TAG, "Recorded CYCLE event (trial $trial_num): duration=${duration_ms}ms, baseline CPU=${String.format("%.2f", baselineAppCpu_percent)}%, RAM=${String.format("%.2f", baselineAppRam_mb)}MB")
//    }

//    /**
//     * Record a PHASE3_END marker (Phase 3: Post-Trial Cool-Down completion).
//     *
//     * Called when Phase 3 timeout (200ms) is reached.
//     *
//     * @param algorithm Trial management mode: "ADA" or "FIX"
//     * @param trial_num Trial number associated with this cool-down phase
//     * @param condition Test condition identifier (e.g., "BIS_AUDIO")
//     * @param timestamp_ms Exact moment Phase 3 completes (200ms after PERI_TRIAL_END)
//     */
//    fun recordPhase3EndMarker(
//        algorithm: String,
//        trial_num: Int,
//        condition: String,
//        timestamp_ms: Long
//    ) {
//        val event = PerformanceEvent(
//            EVENT_TYPE = "PHASE3_END",
//            duration_ms = 0,  // Marker events have no duration
//            algorithm = algorithm,
//            trial_num = trial_num,
//            condition = condition,
//            timestamp_ms = timestamp_ms,
//            baselineAppCpu_percent = 0f,  // Not applicable for marker events
//            baselineAppRam_mb = 0f        // Not applicable for marker events
//        )
//
//        recordedEvents.add(event)
//        lastEvent = event
//
//        Log.d(TAG, "Recorded PHASE3_END marker (trial $trial_num) at timestamp=$timestamp_ms")
//    }
