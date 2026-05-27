package org.albaspazio.psysuite.core.utils.filesystem

import android.os.Environment
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages file system operations for PsySuite results
 * Handles folder creation, file scanning, and validation
 * Singleton pattern for consistent access across the app
 */
class FileSystemManager private constructor() {

    companion object {
        private const val TAG = "FileSystemManager"
        const val RESULTS_FOLDER_NAME = "psysuite_results"
        private const val SUBMITTED_FOLDER_NAME = "submitted"
        
        @Volatile
        private var INSTANCE: FileSystemManager? = null
        
        /**
         * Gets the singleton instance of FileSystemManager
         * Thread-safe lazy initialization
         */
        fun getInstance(): FileSystemManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FileSystemManager().also { 
                    INSTANCE = it
                    Log.i(TAG, "FileSystemManager singleton instance created")
                }
            }
        }
    }

    init {
        // Don't initialize folders here - wait for permissions to be granted
        // initializeFolders()
    }
    // Cached folder references - initialized once and reused
    @Volatile
    private var resultsDir: File? = null
    @Volatile
    private var submittedDir: File? = null
    @Volatile
    private var foldersInitialized = false

    /**
     * Initializes and caches the folder references
     * Thread-safe lazy initialization with error handling
     * @throws SecurityException if storage permission is not granted
     * @throws IOException if folder creation fails
     */
    private fun initializeFolders() {
        if (foldersInitialized) return
        
        synchronized(this) {
            if (foldersInitialized) return
            
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                
                // Check if external storage is available
                val state = Environment.getExternalStorageState()
                if (state != Environment.MEDIA_MOUNTED) {
                    throw IOException("External storage is not available. State: $state")
                }
                
                val tempResultsDir = File(downloadsDir, RESULTS_FOLDER_NAME)
                
                if (!tempResultsDir.exists()) {
                    val created = tempResultsDir.mkdirs()
                    if (!created) {
                        throw IOException("Failed to create results folder at ${tempResultsDir.absolutePath}")
                    }
                    Log.i(TAG, "Created results folder at ${tempResultsDir.absolutePath}")
                } else if (!tempResultsDir.canWrite()) {
                    throw SecurityException("No write permission for results folder at ${tempResultsDir.absolutePath}")
                }

                val tempSubmittedDir = File(tempResultsDir, SUBMITTED_FOLDER_NAME)

                if (!tempSubmittedDir.exists()) {
                    val created = tempSubmittedDir.mkdirs()
                    if (!created) {
                        Log.w(TAG, "Failed to create submitted folder at ${tempSubmittedDir.absolutePath}")
                    } else {
                        Log.i(TAG, "Created submitted folder at ${tempSubmittedDir.absolutePath}")
                    }
                }
                
                // Cache the folder references
                this.resultsDir = tempResultsDir
                this.submittedDir = tempSubmittedDir
                this.foldersInitialized = true
                
                Log.i(TAG, "FileSystemManager folders initialized successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize FileSystemManager folders", e)
                throw e
            }
        }
    }
    
    /**
     * Gets the results folder, initializing if necessary
     * @throws SecurityException if storage permission is not granted
     * @throws IOException if folder creation fails
     */
    fun getResultsFolder(): File {
        if (!foldersInitialized) {
            initializeFolders()
        }
        return resultsDir ?: throw IOException("Results folder not initialized")
    }
    
    /**
     * Gets the submitted folder, initializing if necessary
     * @throws SecurityException if storage permission is not granted
     * @throws IOException if folder creation fails
     */
    fun getSubmittedFolder(): File {
        if (!foldersInitialized) {
            initializeFolders()
        }
        return submittedDir ?: throw IOException("Submitted folder not initialized")
    }
    
    /**
     * Legacy method for backward compatibility
     * @deprecated Use getResultsFolder() and getSubmittedFolder() instead
     */
    @Deprecated("Use getResultsFolder() and getSubmittedFolder() instead")
    fun ensureResultsFoldersExists(): List<File> {
        return listOf(getResultsFolder(), getSubmittedFolder())
    }
    
    /**
     * Convenience method to get both folders at once
     * @return Pair of (resultsFolder, submittedFolder)
     */
    fun getFolders(): Pair<File, File> {
        return Pair(getResultsFolder(), getSubmittedFolder())
    }
    
    /**
     * Checks if the FileSystemManager has been properly initialized
     * @return true if folders are initialized and accessible
     */
    fun isInitialized(): Boolean {
        return try {
            foldersInitialized && resultsDir?.exists() == true && submittedDir?.exists() == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Scans for valid result file pairs (JSON + TXT) in the given folder
     * Excludes files in the submitted subfolder
     * @param folder The folder to scan
     * @throws SecurityException if storage access is denied
     * @throws IOException if folder access fails
     */
    fun scanForValidResultPairs(folder:File): List<ResultFileItem> {
        val resultItems = mutableListOf<ResultFileItem>()
        
        try {
            if (!folder.canRead()) {
                throw SecurityException("No read permission for results folder")
            }
            
            val jsonFiles = folder.listFiles { file ->
                file.isFile && file.name.endsWith(".json") && !file.absolutePath.contains(SUBMITTED_FOLDER_NAME)
            }
            
            if (jsonFiles == null) {
                throw IOException("Cannot list files in results folder - folder may not be accessible")
            }
            
            Log.d(TAG, "Found ${jsonFiles.size} JSON files in results folder")
            
            for (jsonFile in jsonFiles) {
                try {
                    val baseName = jsonFile.nameWithoutExtension
                    val matchingTxtFile = findMatchingTxtFile(folder, baseName)
                    
                    if (matchingTxtFile != null && validateFilePair(jsonFile, matchingTxtFile)) {
                        val resultItem = createResultFileItem(jsonFile, matchingTxtFile)
                        resultItems.add(resultItem)
                        Log.d(TAG, "Added valid file pair: ${jsonFile.name} -> ${matchingTxtFile.name}")
                    } else if (matchingTxtFile == null) {
                        Log.w(TAG, "No matching TXT file found for ${jsonFile.name}")
                    } else {
                        Log.w(TAG, "Invalid file pair: ${jsonFile.name}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to process file pair for ${jsonFile.name}", e)
                    // Continue processing other files instead of failing completely
                }
            }
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Storage access denied", e)
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Critical error scanning for result files", e)
            throw e // Re-throw critical errors
        } catch (e:Exception) {
            Log.e(TAG, "Unexpected error scanning for result files", e)
            throw IOException("Unexpected error while scanning files: ${e.message}", e)
        }
        
        Log.i(TAG, "Found ${resultItems.size} valid result file pairs")
        
        // Log summary for debugging
        if (resultItems.isNotEmpty()) {
            Log.d(TAG, "Valid file pairs found:")
            resultItems.forEach { item ->
                Log.d(TAG, "  JSON: ${item.jsonFile.name} -> TXT: ${item.txtFile.name}")
            }
        }
        
        return resultItems
    }

    /**
     * Finds the matching TXT file for a given JSON base name
     * TXT files may have additional suffix like "_XXYYZZ" that should be ignored when matching
     */
    private fun findMatchingTxtFile(resultsDir: File, jsonBaseName: String): File? {
        try {
            // First try exact match (for backward compatibility)
            val exactMatch = File(resultsDir, "$jsonBaseName.txt")
            if (exactMatch.exists()) {
                Log.d(TAG, "Found exact match TXT file: ${exactMatch.name}")
                return exactMatch
            }
            
            // Look for TXT files that start with the JSON base name
            val txtFiles = resultsDir.listFiles { file ->
                file.isFile && 
                file.name.endsWith(".txt") && 
                !file.absolutePath.contains(SUBMITTED_FOLDER_NAME) &&
                file.nameWithoutExtension.startsWith(jsonBaseName)
            }
            
            if (txtFiles == null || txtFiles.isEmpty()) {
                Log.d(TAG, "No TXT files found starting with: $jsonBaseName")
                return null
            }
            
            // Find the best match - prefer files that match the pattern: jsonBaseName_SUFFIX
            for (txtFile in txtFiles) {
                val txtBaseName = txtFile.nameWithoutExtension
                
                // Check if it matches the pattern: jsonBaseName_SUFFIX
                if (txtBaseName == jsonBaseName) {
                    // Exact match (already handled above, but just in case)
                    Log.d(TAG, "Found exact match TXT file: ${txtFile.name}")
                    return txtFile
                } else if (txtBaseName.startsWith("${jsonBaseName}_")) {
                    // Matches pattern with suffix
                    val suffix = txtBaseName.substring(jsonBaseName.length + 1)
                    Log.d(TAG, "Found matching TXT file with suffix '$suffix': ${txtFile.name}")
                    return txtFile
                }
            }
            
            // If no perfect match found, take the first file that starts with the base name
            val firstMatch = txtFiles.firstOrNull()
            if (firstMatch != null) {
                Log.d(TAG, "Using first matching TXT file: ${firstMatch.name}")
                return firstMatch
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "Error finding matching TXT file for $jsonBaseName", e)
        }
        
        return null
    }

    /**
     * Creates a ResultFileItem from JSON and TXT file pair
     */
    private fun createResultFileItem(jsonFile: File, txtFile: File): ResultFileItem {
        val displayName = jsonFile.nameWithoutExtension
        val creationDate = Date(jsonFile.lastModified())
        val trialCount = parseTrialCount(txtFile)
        val totalSize = jsonFile.length() + txtFile.length()
        
        return ResultFileItem(
            jsonFile = jsonFile,
            txtFile = txtFile,
            displayName = displayName,
            creationDate = creationDate,
            trialCount = trialCount,
            fileSize = totalSize
        )
    }

    /**
     * get the trial count from the number of lines (-1, the header) of results file
     */
    fun parseTrialCount(resFile: File): Int {
        return try {
            resFile.readLines().size - 1
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse trial count from ${resFile.name}", e)
            -1 // Return -1 for unknown
        }
    }

    /**
     * Moves files to the submitted folder after successful upload
     */
    fun moveFilesToSubmitted(files: List<File>): Boolean {
        return try {
            val submittedFolder = getSubmittedFolder()
            var allMoved = true
            
            for (file in files) {
                if (!file.exists()) {
                    Log.w(TAG, "File does not exist, skipping: ${file.name}")
                    continue
                }
                
                val destFile = File(submittedFolder, file.name)
                val moved = file.renameTo(destFile)
                if (moved) {
                    Log.i(TAG, "Moved ${file.name} to submitted folder")
                } else {
                    Log.e(TAG, "Failed to move ${file.name} to submitted folder")
                    allMoved = false
                }
            }
            allMoved
        } catch (e: Exception) {
            Log.e(TAG, "Error moving files to submitted folder", e)
            false
        }
    }

    /**
     * Checks if a result with the given unique ID has already been submitted
     */
    fun isAlreadySubmitted(exp_uid: String): Boolean {
        return try {
            val submittedFolder = getSubmittedFolder()
            val submittedFiles = submittedFolder.listFiles { file -> 
                file.isFile && file.name.endsWith(".json")
            } ?: return false
            
            for (jsonFile in submittedFiles) {
                try {
                    val jsonContent = jsonFile.readText()
                    val jsonObject = JSONObject(jsonContent)
                    val fileexp_uid = jsonObject.optString("exp_uid", "")
                    
                    if (fileexp_uid == exp_uid) {
                        Log.d(TAG, "Found already submitted result with ID: $exp_uid")
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse submitted file ${jsonFile.name}", e)
                }
            }
            
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for already submitted results", e)
            false
        }
    }

    /**
     * Validates that a file pair is complete and readable
     */
    fun validateFilePair(jsonFile: File, txtFile: File): Boolean {
        return try {
            // Check files exist and are readable
            if (!jsonFile.exists() || !jsonFile.canRead()) {
                Log.w(TAG, "JSON file not accessible: ${jsonFile.name}")
                return false
            }
            
            if (!txtFile.exists() || !txtFile.canRead()) {
                Log.w(TAG, "TXT file not accessible: ${txtFile.name}")
                return false
            }
            
            // Try to parse JSON to ensure it's valid
            val jsonContent = jsonFile.readText()
            JSONObject(jsonContent)
            
            // Check that TXT file has content
            if (txtFile.length() == 0L) {
                Log.w(TAG, "TXT file is empty: ${txtFile.name}")
                return false
            }
            
            true
        } catch (e: Exception) {
            Log.w(TAG, "File pair validation failed for ${jsonFile.name}", e)
            false
        }
    }

    /**
     * Debug method to list all files in the results folder
     * Useful for troubleshooting file matching issues
     */
    fun debugListAllFiles(): String {
        return try {
            val resultsFolder = getResultsFolder()
            val allFiles = resultsFolder.listFiles() ?: return "No files found"
            
            val jsonFiles = allFiles.filter { it.name.endsWith(".json") }
            val txtFiles = allFiles.filter { it.name.endsWith(".txt") }
            
            val debug = StringBuilder()
            debug.append("Results folder: ${resultsFolder.absolutePath}\n")
            debug.append("Total files: ${allFiles.size}\n")
            debug.append("JSON files (${jsonFiles.size}):\n")
            jsonFiles.forEach { debug.append("  ${it.name}\n") }
            debug.append("TXT files (${txtFiles.size}):\n")
            txtFiles.forEach { debug.append("  ${it.name}\n") }
            
            debug.toString()
        } catch (e: Exception) {
            "Error listing files: ${e.message}"
        }
    }
}

/**
 * Data class representing a result file item with metadata
 */
data class ResultFileItem(
    val jsonFile: File,
    val txtFile: File,
    val displayName: String,
    val creationDate: Date,
    val trialCount: Int,
    val fileSize: Long,
    var isSelected: Boolean = false
) {
    /**
     * Gets the unique ID from the JSON file
     */
    val exp_uid: String
        get() = try {
            val jsonContent = jsonFile.readText()
            val jsonObject = JSONObject(jsonContent)
            jsonObject.optString("exp_uid", "")
        } catch (e: Exception) {
            ""
        }

    /**
     * Formats the file size for display
     */
    val formattedSize: String
        get() = formatFileSize(fileSize)

    /**
     * Formats the creation date for display
     */
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(creationDate)

    /**
     * Formats the trial count for display
     */
    val formattedTrialCount: String
        get() = if (trialCount >= 0) trialCount.toString() else "Unknown"

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}