package org.albaspazio.psysuite.model.summary

import org.albaspazio.psysuite.tests.TrialBasic

/**
 * Abstract class representing the summary data for a single condition within a test.
 * A condition can have a main [label] and an optional [sub_label], and it contains a list of [SummaryRow]s,
 * where each row typically summarizes data for a specific stimulus type or sub-condition.
 *
 * Subclasses must implement how trials are added ([add]) and define the specific [rows] of summary data.
 *
 * @param label The main label for this condition (e.g., "Visual", "Auditory"). Defaults to an empty string.
 * @param sub_label An optional sub-label providing more detail for the header in the string representation (e.g., "Intensity", "Frequency"). Defaults to an empty string.
 */
abstract class SummaryCondition(val label:String="", val sub_label:String = ""){

    /**
     * List of [SummaryRow] objects, each representing a row in the summary table for this condition.
     */
    abstract var rows:List<SummaryRow>

    /**
     * Adds data from a completed trial to the appropriate [SummaryRow](s) within this condition.
     * Subclasses must implement this to correctly process the trial and update row statistics.
     * @param trial The [TrialBasic] object containing data from the completed trial.
     */
    abstract fun add(trial: TrialBasic)

    /**
     * Finalizes the calculations for all [SummaryRow]s within this condition.
     * This method should be called before generating the string representation of the summary.
     * It iterates through each row and calls its [SummaryRow.close] method.
     */
    fun close(){
        rows.forEach { // Changed map to forEach as map here implies a transformation we don't use
            it.close()
        }
    }

    /**
     * Generates a string representation of the summary for this condition.
     * This includes a header row followed by the string representation of each [SummaryRow].
     * The header format depends on whether a [sub_label] is provided.
     * Header without sub-label: "type\tntr\t%succ\trt\n"
     * Header with sub-label:    "type\t[sub_label]\tntr\t%succ\trt\n"
     *
     * @return A tab-separated string representing the summary data for this condition.
     */
    override fun toString():String{
        // Define the header based on the presence of sub_label
        val header = if(sub_label.isEmpty()) {
            "type\tntr\t%succ\trt\n"
        } else {
            "type\t$sub_label\tntr\t%succ\trt\n"
        }

        // Use a StringBuilder for efficient string concatenation
        val sb = StringBuilder()
        sb.append(header)

        rows.forEach { // Changed map to forEach for appending to StringBuilder
            sb.append(it.toString()) // Assumes SummaryRow.toString() already includes a newline if necessary for each row.
                                     // If not, sb.append(it.toString()).append("\n") might be needed depending on SummaryRow's implementation.
        }
        return sb.toString()
    }
}
