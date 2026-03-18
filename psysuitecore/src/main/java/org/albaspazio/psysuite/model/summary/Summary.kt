package org.albaspazio.psysuite.model.summary

import android.content.Context
import android.net.Uri
import android.os.Environment
import org.albaspazio.psysuite.tests.TrialBasic
import org.albaspazio.core.filesystem.getAbsoluteFilePath
import org.albaspazio.core.filesystem.saveText

/**
 * Abstract base class for creating and managing summaries of test results.
 * It handles the collection of trial data, organizing it by conditions, and writing the final summary to a file.
 *
 * Subclasses must define the specific [conditions] and potentially override [cond_labels]
 * if different from the default condition labels.
 *
 * @param ctx The Android [Context] used for file operations.
 */
abstract class Summary(private val ctx: Context){

    /**
     * List of [SummaryCondition] objects, each representing a distinct condition within the test
     * for which results are summarized.
     */
    abstract var conditions: List<SummaryCondition>

    /** The generated summary string. Populated when [close] is called. */
    protected var summary:String = ""
    /** Optional list of labels for the conditions, used if conditions are handled in a specific order or manner. */
    protected open val cond_labels:List<String> = listOf()

    /**
     * Adds a completed trial to the summary.
     * By default, it adds the trial to the first condition in the [conditions] list.
     * This method might be overridden in subclasses to distribute trials to different conditions based on trial properties.
     * @param trial The [TrialBasic] object containing the data from the completed trial.
     */
    open fun add(trial: TrialBasic){
        // Default implementation adds to the first condition. 
        // Subclasses might need to override this if there are multiple conditions 
        // and trials need to be routed based on their properties.
        if (conditions.isNotEmpty()) {
            conditions[0].add(trial)
        } else {
            // Handle the case where conditions list is empty, perhaps log an error or throw an exception.
        }
    }

    /**
     * Finalizes the summary, compiles the summary string from all conditions, and writes it to a specified file.
     * If there's only one condition, its summary is used directly.
     * If there are multiple conditions, their summaries are concatenated, each prefixed with "Condition [label]".
     *
     * @param filename The desired name for the summary file.
     * @param dir The directory where the file should be saved (e.g., [Environment.DIRECTORY_DOWNLOADS]). Defaults to [Environment.DIRECTORY_DOWNLOADS].
     * @return The absolute file path of the saved summary file, or an empty string if saving failed.
     */
    open fun close(filename:String, dir:String = Environment.DIRECTORY_DOWNLOADS):String{

        if(conditions.size == 1) {
            conditions[0].close() // Finalize the single condition
            summary = conditions[0].toString()
        }
        else {
            summary = "" // Reset summary string for multiple conditions
            conditions.forEachIndexed { index, condition ->
                condition.close() // Finalize each condition
                summary += "Condition ${condition.label.ifEmpty { cond_labels.getOrElse(index) { (index + 1).toString() } }}\n"
                summary += condition.toString()
                if (index < conditions.size - 1) summary += "\n\n" // Add separation between conditions
            }
        }
        return writeFile(summary, filename, dir)
    }

    /**
     * Writes the provided summary string to a file.
     * Notifies the MediaScanner about the new file so it appears in downloads/galleries.
     *
     * @param summary The summary string to write.
     * @param filename The name of the file.
     * @param dir The directory to save the file in. Defaults to [Environment.DIRECTORY_DOWNLOADS].
     * @return The absolute path of the written file, or an empty string if the write operation failed.
     */
    private fun writeFile(summary:String, filename:String, dir:String = Environment.DIRECTORY_DOWNLOADS):String{

        val res:Any? = saveText(ctx, filename, summary, dir, true, notifyDm=true)

        return when(res){
            null, false -> "" // Save operation failed or returned null/false
            is Uri -> getAbsoluteFilePath(filename, dir).second // If Uri is returned, get path
            true -> getAbsoluteFilePath(filename, dir).second // If true is returned, get path (some saveText versions might return Boolean)
            else   -> "" // Any other unexpected result
        }
    }

}