package org.albaspazio.psysuite.core.performance

import java.util.Locale

/**
 * Serialization utilities for converting PerformanceSample and PerformanceEvent to TSV format.
 * 
 * Samples and Events are defined in PerformanceModels.kt
 */
object PerformanceSerializer {
    
    /**
     * Serialize a PerformanceSample to a tab-separated string for output to timeseries file.
     * 
     * Output format (9 tab-separated columns):
     * condition\talgorithm\tphase_id\ttimestamp_ms\tdevice_cpu_percent\tdevice_ram_mb\tapp_cpu_percent\tapp_ram_mb\ttrial_num
     * 
     * Example:
     * BIS_AUDIO    0	ADA	1	45.2	1024.5	12.3	256.8   1705327822123
     */
    fun sampleToTsv(sample: PerformanceSample): String {
        return buildString {
            append(sample.condition).append("\t")
            append(sample.trial_num).append("\t")
            append(sample.algorithm).append("\t")
            append(sample.phase_id).append("\t")
//            append(String.format(Locale.US, "%.2f", sample.device_cpu_percent)).append("\t")
//            append(String.format(Locale.US, "%.2f", sample.device_ram_mb)).append("\t")
            append(String.format(Locale.US, "%.2f", sample.app_cpu_percent)).append("\t")
            append(String.format(Locale.US, "%.2f", sample.app_ram_mb)).append("\t")
            append(sample.timestamp_ms)
        }
    }
    
    /**
     * Serialize a PerformanceEvent to a tab-separated string for output to events file.
     * 
     * Output format (8 tab-separated columns):
     * EVENT_TYPE\tduration_ms\talgorithm\ttrial_num\tcondition\ttimestamp_ms\tbaselineAppCpu_percent\tbaselineAppRam_mb
     * 
     * Examples:
     * INIT	0   2345	ADA	BIS_AUDIO	35.5	256.8   1705327822000
     * CYCLE    1	95	ADA	BIS_AUDIO	42.1	258.5   1705327825100
     * PHASE3_END   1	0	ADA	BIS_AUDIO	0.0	0.0 1705327825300
     */
    fun eventToTsv(event: PerformanceEvent): String {
        return buildString {
            append(event.EVENT_TYPE).append("\t")
            append(event.trial_num).append("\t")
            append(event.duration_ms).append("\t")
            append(event.algorithm).append("\t")
            append(event.condition).append("\t")
            append(String.format(Locale.US, "%.2f", event.baselineAppCpu_percent)).append("\t")
            append(String.format(Locale.US, "%.2f", event.baselineAppRam_mb)).append("\t")
            append(event.timestamp_ms)
        }
    }
}
