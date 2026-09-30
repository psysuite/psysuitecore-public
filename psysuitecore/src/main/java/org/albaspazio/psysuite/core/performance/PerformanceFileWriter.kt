package org.albaspazio.psysuite.core.performance

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.albaspazio.psysuite.core.utils.filesystem.FileSystemManager
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

class PerformanceFileWriter(
    private val context: Context,
    private val filesPrefix: String
) {
    companion object {
        private const val TAG = "PerformanceFileWriter"
        private const val BATCH_SIZE = 10
        private val RETRY_BACKOFF_MS = listOf(100L, 200L, 400L)
        private const val TIMESERIES_HEADER = "cond\ttrial_num\talgo\tphase_id\tcpu_perc\tram_mb\ttime_ms"
//        private const val TIMESERIES_HEADER = "condition\talgorithm\tphase_id\ttimestamp_ms\tdevice_cpu_percent\tdevice_ram_mb\tapp_cpu_percent\tapp_ram_mb\ttrial_num"
        private const val EVENTS_HEADER = "event\ttrial_num\tdur_ms\talgo\tcond\tbCpu_perc\tbRam_mb\ttime_ms"
    }

    private val sampleBuffer = Collections.synchronizedList(mutableListOf<PerformanceSample>())
    private val eventBuffer = Collections.synchronizedList(mutableListOf<PerformanceEvent>())
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val outputDir: File by lazy {
        FileSystemManager.getInstance().getResultsFolder()
    }
    
    private var timeseriesHeaderWritten = false
    private var eventsHeaderWritten = false

    fun enqueueTimeseriesSample(sample: PerformanceSample) {
        sampleBuffer.add(sample)
        if (sampleBuffer.size >= BATCH_SIZE) {
            Log.d("PERF_DEBUG", "⚠️ Buffer reached BATCH_SIZE (${sampleBuffer.size}), auto-flushing")
            flushTimeseries()
        } else if (sampleBuffer.size % 5 == 0) {
            Log.d("PERF_DEBUG", "⏳ Sample enqueued, buffer size: ${sampleBuffer.size}/${BATCH_SIZE}")
        }
    }

    fun enqueueEvent(event: PerformanceEvent?) {
        if (event == null) {
            Log.w(TAG, "Attempted to enqueue null event")
            return
        }
        eventBuffer.add(event)
        if (eventBuffer.size >= BATCH_SIZE) {
            flushEvents()
        }
    }

    private fun flushTimeseriesInternal() {
        if (sampleBuffer.isEmpty()) return
        try {
            val samplesToWrite = sampleBuffer.toList()
            sampleBuffer.clear()
            val filename = generateFilename("timeseries")
            val file = File(outputDir, filename)
            
            val linesToWrite = mutableListOf<String>()
            if (!timeseriesHeaderWritten && !file.exists()) {
                linesToWrite.add(TIMESERIES_HEADER)
                timeseriesHeaderWritten = true
            } else if (!timeseriesHeaderWritten) {
                timeseriesHeaderWritten = true
            }
            
            linesToWrite.addAll(samplesToWrite.map { PerformanceSerializer.sampleToTsv(it) })
            appendToFile(file, linesToWrite)
            Log.d("PERF_DEBUG", "✅ Flushed ${samplesToWrite.size} timeseries samples to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to flush timeseries samples: ${e.message}", e)
        }
    }

    private fun flushEventsInternal() {
        if (eventBuffer.isEmpty()) return
        try {
            val eventsToWrite = eventBuffer.toList()
            eventBuffer.clear()
            val filename = generateFilename("events")
            val file = File(outputDir, filename)
            
            val linesToWrite = mutableListOf<String>()
            if (!eventsHeaderWritten && !file.exists()) {
                linesToWrite.add(EVENTS_HEADER)
                eventsHeaderWritten = true
            } else if (!eventsHeaderWritten) {
                eventsHeaderWritten = true
            }
            
            linesToWrite.addAll(eventsToWrite.map { PerformanceSerializer.eventToTsv(it) })
            appendToFile(file, linesToWrite)
            Log.d("PERF_DEBUG", "✅ Flushed ${eventsToWrite.size} events to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to flush events: ${e.message}", e)
        }
    }

    fun flushTimeseries() {
        ioScope.launch {
            flushTimeseriesInternal()
        }
    }

    fun flushEvents() {
        ioScope.launch {
            flushEventsInternal()
        }
    }

//    fun flush() {
//        flushTimeseries()
//        flushEvents()
//        Log.d(TAG, "All buffers flushed")
//    }

    fun flushSync() {
        flushTimeseriesInternal()
        flushEventsInternal()
        Log.d(TAG, "All buffers flushed synchronously")
    }

//    private suspend fun writeToFileWithRetry(file: File, lines: List<String>) {
//        var lastException: Exception? = null
//        try {
//            appendToFile(file, lines)
//            Log.d("PERF_DEBUG", "✅ Successfully wrote ${lines.size} lines to ${file.absolutePath}")
//            return
//        } catch (e: Exception) {
//            lastException = e
//            Log.w(TAG, "Initial write failed for ${file.absolutePath}: ${e.message}")
//        }
//        for ((index, backoffMs) in RETRY_BACKOFF_MS.withIndex()) {
//            try {
//                Log.d(TAG, "Retry ${index + 1} for ${file.name} after ${backoffMs}ms")
//                delay(backoffMs)
//                appendToFile(file, lines)
//                Log.d("PERF_DEBUG", "✅ Retry ${index + 1} succeeded for ${file.absolutePath}")
//                return
//            } catch (e: Exception) {
//                lastException = e
//                Log.w(TAG, "Retry ${index + 1} failed for ${file.name}: ${e.message}")
//            }
//        }
//        Log.e(TAG, "All retry attempts failed for ${file.name}", lastException)
//        try {
//            val fallbackDir = context.getExternalFilesDir(null) ?: context.cacheDir
//            val fallbackFile = File(fallbackDir, file.name)
//            Log.w(TAG, "Attempting fallback write to directory: ${fallbackFile.absolutePath}")
//            appendToFile(fallbackFile, lines)
//            Log.d("PERF_DEBUG", "✅ Fallback write succeeded to ${fallbackFile.absolutePath}")
//        } catch (fallbackException: Exception) {
//            Log.e(TAG, "Fallback write also failed: ${fallbackException.message}", fallbackException)
//            throw fallbackException
//        }
//    }

    private fun appendToFile(file: File, lines: List<String>) {
        if (lines.isEmpty()) return
        FileWriter(file, true).use { fw ->
            BufferedWriter(fw).use { bw ->
                for (line in lines) {
                    bw.write(line)
                    bw.newLine()
                }
                bw.flush()
            }
        }
    }

    private fun generateFilename(filetype: String): String {
        return "${filesPrefix}_performance_${filetype}.txt"
    }
}
