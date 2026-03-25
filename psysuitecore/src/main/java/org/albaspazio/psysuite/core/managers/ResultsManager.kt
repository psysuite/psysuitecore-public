package org.albaspazio.psysuite.core.managers

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.albaspazio.core.accessory.SingletonHolder
import org.albaspazio.core.ui.show1MethodDialog
import org.albaspazio.psysuite.core.R
import org.albaspazio.psysuite.core.utils.filesystem.FileSystemManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

/*
    RULES:
    user can send data by web upload or email, whether enabled. In both cases, these conditions are needed:
    1) the device must be registered
    2) internet connection must exist
    3) result file must exist
 */

// SINGLETON
class ResultsManager private constructor(private var activity: Activity?) {

    companion object : SingletonHolder<ResultsManager, Activity>(::ResultsManager)

    private var resources: Resources?       = activity?.resources
    private var prefs: SharedPreferences?   = activity?.getSharedPreferences("psysuite_web_config", Context.MODE_PRIVATE)

    private var maxRetryAttempts: Int       = prefs?.getInt("max_retry_attempts", 3) ?: 3
    private var retryDelayMs: Long          = prefs?.getLong("retry_delay_ms", 5000) ?: 5000


    private var fileSystemManager           = FileSystemManager.getInstance()
    private val HTTP_ERROR_SUBMISSION_NOT_ALLOWED = 423

    private var uploadAD: AlertDialog? = null
    private var uploadJob: Job? = null

    // Simple properties - no SecureStorage needed
    var webApiUrl: String = ""
    var webApiKey: String = ""

    // region flags
    val isWebUploadEnabled: Boolean
        get() = webApiUrl.isNotBlank() && webApiKey.isNotBlank()

    val isNetworkAvailable: Boolean
        get() {
            if (activity == null) return false
            val connectivityManager = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    val canUpload: Boolean
        get() = isNetworkAvailable && isWebUploadEnabled

    val existResultsToSend: Boolean
        get() {
            return try {
                val resultsDir = fileSystemManager.getResultsFolder()
                resultsDir.exists() && resultsDir.listFiles()?.isNotEmpty() == true
            } catch (e: Exception) {
                false
            }
        }

    // endregion


    fun updateContext(newActivity: Activity) {
        this.activity = newActivity
        this.resources = newActivity.resources
        this.prefs = newActivity.getSharedPreferences("psysuite_web_config", Context.MODE_PRIVATE)
    }

    fun moveResultFile(source: File, destination: File): Boolean {
        return try {
            if (!source.exists()) {
                Log.e("ResultsManager", "Source file does not exist: ${source.absolutePath}")
                return false
            }
            source.copyTo(destination, overwrite = true)
            source.delete()
            true
        } catch (e: Exception) {
            Log.e("ResultsManager", "Error moving file", e)
            false
        }
    }

    fun deleteResultFile(file: File): Boolean {
        return try {
            if (file.exists()) {
                file.delete()
            }
            true
        } catch (e: Exception) {
            Log.e("ResultsManager", "Error deleting file", e)
            false
        }
    }

    fun uploadSelectedResults(
        files: List<Any>,
        callback: (fileItem: Any, success: Boolean, errorMessage: String?) -> Unit
    ) {
        uploadJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                withContext(Dispatchers.Main) {
                    uploadAD = show1MethodDialog(activity!!, "Upload", "Preparing upload...", resources?.getString(R.string.abort) ?: "Abort") {
                        uploadJob?.cancel()
                        uploadAD?.dismiss()
                        uploadAD = null
                    }
                }

                var successCount = 0
                val totalCount = files.size

                for ((index, item) in files.withIndex()) {
                    // Update progress dialog
                    withContext(Dispatchers.Main) {
                        val displayName = if (item is org.albaspazio.psysuite.core.utils.filesystem.ResultFileItem) {
                            item.displayName
                        } else {
                            item.toString()
                        }
                        uploadAD?.setMessage("Uploading $displayName... (${index + 1}/$totalCount)")
                    }

                    try {
                        if (item is org.albaspazio.psysuite.core.utils.filesystem.ResultFileItem) {
                            // Check if already submitted
                            val fileSystemManager = org.albaspazio.psysuite.core.utils.filesystem.FileSystemManager.getInstance()
                            if (fileSystemManager.isAlreadySubmitted(item.exp_uid)) {
                                Log.i("ResultsManager", "Skipping already submitted file: ${item.displayName}")
                                withContext(Dispatchers.Main) {
                                    callback(item, true, "Already submitted")
                                }
                                successCount++
                                continue
                            }

                            // Parse and upload
                            val experimentData = parseExperimentFiles(item.jsonFile, item.txtFile)
                            if (experimentData != null) {
                                Log.i("ResultsManager", "Uploading experiment: ${experimentData.exp_uid}")
                                val success = doUploadExperiment(experimentData)
                                if (success) {
                                    // Move files to submitted folder
                                    val filesToMove = listOf(item.jsonFile, item.txtFile)
                                    val moved = fileSystemManager.moveFilesToSubmitted(filesToMove)
                                    if (moved) {
                                        successCount++
                                        Log.i("ResultsManager", "Successfully uploaded and moved: ${item.displayName}")
                                        withContext(Dispatchers.Main) {
                                            callback(item, true, null)
                                        }
                                    } else {
                                        Log.w("ResultsManager", "Upload succeeded but failed to move files: ${item.displayName}")
                                        withContext(Dispatchers.Main) {
                                            callback(item, false, "Upload succeeded but failed to move files")
                                        }
                                    }
                                } else {
                                    Log.w("ResultsManager", "Upload failed: ${item.displayName}")
                                    withContext(Dispatchers.Main) {
                                        callback(item, false, "Upload failed")
                                    }
                                }
                            } else {
                                Log.e("ResultsManager", "Failed to parse experiment data: ${item.displayName}")
                                withContext(Dispatchers.Main) {
                                    callback(item, false, "Failed to parse experiment data")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ResultsManager", "Error uploading ${if (item is org.albaspazio.psysuite.core.utils.filesystem.ResultFileItem) item.displayName else item}", e)
                        withContext(Dispatchers.Main) {
                            callback(item, false, "Error: ${e.message}")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    uploadAD?.dismiss()
                    Log.i("ResultsManager", "Batch upload completed: $successCount/$totalCount files uploaded successfully")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    uploadAD?.dismiss()
                }
                Log.e("ResultsManager", "Batch upload error", e)
                files.forEach { item ->
                    withContext(Dispatchers.Main) {
                        callback(item, false, "Batch upload error: ${e.message}")
                    }
                }
            }
        }
    }

    fun onTestFinished(result: Any) {
        // Stub implementation - to be completed with actual test result processing
        Log.d("ResultsManager", "onTestFinished called with result: $result")
    }

    private fun parseExperimentFiles(jsonFile: File, resultFile: File): ExperimentUploadData? {
        return try {
            val configJson = JSONObject(jsonFile.readText())
            val classesArray = configJson.getJSONArray("classes")
            val testClassName = classesArray.getString(0).substringAfterLast(".")
            val exp_uid = configJson.getString("exp_uid")
            val validJson = filterJson(configJson)
            val trials = parseTrialResults(resultFile)

            val deviceManager = DeviceIdentificationManager.getInstance(activity!!)
            val experimentData = ExperimentUploadData(
                exp_uid = exp_uid,
                testClassName = testClassName,
                configuration = validJson,
                trials = trials,
                deviceId = deviceManager.deviceId ?: ""
            )

            return experimentData

        } catch (e: Exception) {
            Log.e("ResultsManager", "Error parsing experiment files", e)
            null
        }
    }

    private fun filterJson(configJson: JSONObject): JSONObject {
        val validFields = listOf(
            "label", "age", "gender", "population", "session", "type", "project",
            "device", "vercode", "stimuliDelays", "whitenoise",
            "trman_type", "showResult", "canRepeat", "doTraining", "date"
        )

        val validJson = JSONObject()
        validFields.forEach { field ->
            if (configJson.has(field)) {
                var value = configJson.get(field)
                if (field == "date" && value is String) {
                    value = normalizeDateFormat(value)
                }
                validJson.put(field, value)
            }
        }
        return validJson
    }

    private fun normalizeDateFormat(dateString: String): String {
        return try {
            val formats = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "dd/MM/yyyy",
                "dd-MM-yyyy",
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "dd.MM.yyyy",
                "MM/dd/yyyy",
                "MM-dd-yyyy"
            )

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            for (format in formats) {
                try {
                    val parser = java.text.SimpleDateFormat(format, java.util.Locale.US)
                    val date = parser.parse(dateString)
                    val normalizedDate = sdf.format(date)
                    Log.d("ResultsManager", "Date normalized from '$dateString' (format: $format) to '$normalizedDate'")
                    return normalizedDate
                } catch (e: Exception) {
                    continue
                }
            }

            Log.w("ResultsManager", "Could not parse date format: $dateString, returning as-is")
            dateString
        } catch (e: Exception) {
            Log.e("ResultsManager", "Error normalizing date format", e)
            dateString
        }
    }

    private fun parseTrialResults(resultFile: File): List<TrialData> {
        val trials = mutableListOf<TrialData>()
        try {
            val lines = resultFile.readLines()
            if (lines.isEmpty()) {
                return trials
            }

            val headers = lines[0].split("\t")
            
            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.isBlank()) {
                    continue
                }
                
                val values = line.split("\t")
                
                if (values.size == headers.size) {
                    val trialData = mutableMapOf<String, Any>()
                    for (j in headers.indices) {
                        val header = headers[j].trim()
                        val value = values[j].trim()
                        trialData[header] = when {
                            value.toIntOrNull() != null -> value.toInt()
                            value.toDoubleOrNull() != null -> value.toDouble()
                            value.equals("true", ignoreCase = true) -> true
                            value.equals("false", ignoreCase = true) -> false
                            else -> value
                        }
                    }
                    trials.add(TrialData(i, trialData))
                }
            }
        } catch (e: Exception) {
            Log.e("ResultsManager", "Error parsing trial results", e)
        }
        return trials
    }

    private suspend fun doUploadExperiment(experimentData: ExperimentUploadData): Boolean = withContext(Dispatchers.IO) {
        var attempt = 0
        var delay = retryDelayMs

        while (attempt < maxRetryAttempts) {
            try {
                if (!isNetworkAvailable) {
                    Log.w("ResultsManager", "No network available for upload attempt ${attempt + 1}")
                    delay(delay)
                    delay = min(delay * 2, 60000)
                    attempt++
                    continue
                }

                Log.d("ResultsManager", "Attempting upload to: $webApiUrl/api/upload/experiment")

                val url = URL("$webApiUrl/api/upload/experiment")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $webApiKey")
                connection.doOutput = true

                val payload = JSONObject().apply {
                    put("exp_uid", experimentData.exp_uid)
                    put("test_class_name", experimentData.testClassName)
                    put("device_id", experimentData.deviceId)
                    put("configuration", experimentData.configuration)
                    put("trials", JSONArray().apply {
                        experimentData.trials.forEach { trial ->
                            put(JSONObject().apply {
                                put("trial_number", trial.trialNumber)
                                trial.data.forEach { (key, value) -> put(key, value) }
                            })
                        }
                    })
                }

                try {
                    connection.outputStream.use { os ->
                        os.write(payload.toString().toByteArray())
                        os.flush()
                    }
                    Log.d("ResultsManager", "Request payload sent successfully")
                } catch (e: Exception) {
                    Log.e("ResultsManager", "Failed to send request payload", e)
                    throw e
                }

                val responseCode = connection.responseCode
                Log.d("ResultsManager", "Received response code: $responseCode")

                when (responseCode) {
                    HttpURLConnection.HTTP_CREATED -> {
                        Log.i("ResultsManager", "Experiment uploaded successfully")
                        return@withContext true
                    }
                    HttpURLConnection.HTTP_CONFLICT -> {
                        Log.i("ResultsManager", "Experiment already exists on server")
                        return@withContext true
                    }
                    HTTP_ERROR_SUBMISSION_NOT_ALLOWED -> {
                        return@withContext false
                    }
                    else -> {
                        val errorMessage = try {
                            connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                        } catch (_: Exception) {
                            "HTTP $responseCode"
                        }
                        Log.w("ResultsManager", "Upload failed with code $responseCode: $errorMessage")
                    }
                }

            } catch (e: IOException) {
                Log.w("ResultsManager", "Upload attempt ${attempt + 1} failed", e)
            } catch (e: Exception) {
                Log.e("ResultsManager", "Unexpected error during upload", e)
                return@withContext false
            }

            attempt++
            if (attempt < maxRetryAttempts) {
                delay(delay)
                delay = min(delay * 2, 60000)
            }
        }

        Log.e("ResultsManager", "Upload failed after $maxRetryAttempts attempts")
        return@withContext false
    }

    // Data classes for upload
    data class ExperimentUploadData(
        val exp_uid: String,
        val testClassName: String,
        val configuration: JSONObject,
        val trials: List<TrialData>,
        var deviceId: String = ""
    )

    data class TrialData(
        val trialNumber: Int,
        val data: Map<String, Any>
    )
}
