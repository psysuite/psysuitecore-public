package org.albaspazio.psysuite.core.performance

/**
 * Represents a single CPU/memory sample collected at 100Hz intervals.
 *
 * This data class is used internally to store resource metrics during monitoring phases.
 * Samples are collected from Device.getAppCpuPercent(), Device.getAppRamMb(), etc.
 *
 * @param condition Test condition identifier (e.g., "BIS_AUDIO")
 * @param algorithm Trial management mode: "ADA" or "FIX"
 * @param phase_id Phase being monitored: "1" (ADOPY), "2" (Trial), "3" (Cool-down)
 * @param timestamp_ms Milliseconds since epoch when sample was collected
 * @param device_cpu_percent Device-wide CPU usage (%)
 * @param device_ram_mb Device-wide RAM usage (MB)
 * @param app_cpu_percent App process CPU usage (%)
 * @param app_ram_mb App process RAM usage (MB)
 * @param trial_num Trial number (0 for Phase 1, trial# for Phase 2-3)
 */
data class PerformanceSample(
    val condition: String,
    val algorithm: String,
    val phase_id: String,
    val timestamp_ms: Long,
    val device_cpu_percent: Float,
    val device_ram_mb: Float,
    val app_cpu_percent: Float,
    val app_ram_mb: Float,
    val trial_num: Int
) {
    /**
     * Serialize sample to TSV format (tab-separated values)
     * used by tests only
     * Format: condition\talgorithm\tphase_id\ttimestamp_ms\tdevice_cpu_percent\tdevice_ram_mb\tapp_cpu_percent\tapp_ram_mb\ttrial_num
     *
     * @return String representation suitable for writing to TSV file
     */
    fun toTsv(): String {
//        return "$condition\t$algorithm\t$phase_id\t$timestamp_ms\t${String.format("%.2f", device_cpu_percent)}\t${String.format("%.2f", device_ram_mb)}\t${String.format("%.2f", app_cpu_percent)}\t${String.format("%.2f", app_ram_mb)}\t$trial_num"
        return "$condition\t$algorithm\t$phase_id\t$timestamp_ms\t${String.format("%.2f", app_cpu_percent)}\t${String.format("%.2f", app_ram_mb)}\t$trial_num"
    }
}

/**
 * Represents a timing event (INIT, CYCLE, PHASE3_END) with baseline metrics.
 *
 * Events record when specific operations (engine initialization, trial cycle) complete,
 * along with their duration and resource baselines captured at phase start.
 *
 * @param EVENT_TYPE Event type: "INIT", "CYCLE", or "PHASE3_END"
 * @param duration_ms Operation duration in milliseconds (0 for PHASE3_END marker)
 * @param algorithm Trial management mode: "ADA" or "FIX"
 * @param trial_num Trial number (0 for INIT, trial# for CYCLE/PHASE3_END)
 * @param condition Test condition identifier (e.g., "BIS_AUDIO")
 * @param timestamp_ms Event timestamp (milliseconds since epoch)
 * @param baselineAppCpu_percent App CPU usage (%) at phase start
 * @param baselineAppRam_mb App RAM usage (MB) at phase start
 */
data class PerformanceEvent(
    val EVENT_TYPE: String,
    val duration_ms: Long,
    val algorithm: String,
    val trial_num: Int,
    val condition: String,
    val timestamp_ms: Long,
    val baselineAppCpu_percent: Float,
    val baselineAppRam_mb: Float
) {
    /**
     * Serialize event to TSV format (tab-separated values)
     * used by tests only
     *
     * Format: EVENT_TYPE\tduration_ms\talgorithm\ttrial_num\tcondition\ttimestamp_ms\tbaselineAppCpu_percent\tbaselineAppRam_mb
     *
     * @return String representation suitable for writing to TSV file
     */
    fun toTsv(): String {
        return "$EVENT_TYPE\t$duration_ms\t$algorithm\t$trial_num\t$condition\t$timestamp_ms\t${String.format("%.2f", baselineAppCpu_percent)}\t${String.format("%.2f", baselineAppRam_mb)}"
    }
}
