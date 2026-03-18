package org.albaspazio.psysuite.model.summary

import org.albaspazio.psysuite.tests.TrialBasic
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Represents a single row in a test summary table, typically corresponding to a specific stimulus type or sub-condition.
 * It accumulates data from trials and calculates statistics such as the total number of trials, mean reaction time (RT),
 * and percentage of successful trials.
 *
 * @param type An integer identifier for the type of data this row represents (e.g., a specific stimulus ID or category).
 * @param label A descriptive label for this row (e.g., "Visual Stimulus A", "Auditory Low Frequency").
 * @param sub_label An optional sub-label providing more specific detail for this row, used in the string representation.
 *                  Defaults to an empty string.
 */
open class SummaryRow(val type:Int, val label:String, val sub_label:String = ""){

    /** The total number of trials included in this summary row. */
    protected var ntrial:Int              = 0
    /** The cumulative reaction time for all trials in this row. Used to calculate mean RT in [close]. */
    protected var rt:Long                 = 0 // Stores sum of RTs initially, then mean RT after close()
    /** The number of successful trials. Used to calculate success percentage in [close]. */
    protected var perc_succ:Int           = 0 // Stores count of successes initially, then percentage after close()

    /**
     * Finalizes the calculations for this summary row.
     * If trials were added ([ntrial] > 0), it calculates the mean reaction time (RT)
     * and the percentage of successful trials.
     * Mean RT is stored back in [rt], and success percentage in [perc_succ].
     */
    open fun close(){
        if(ntrial > 0){
            rt          = ((rt.toFloat()) / ntrial).roundToLong() // Calculate mean RT
            perc_succ   = (((perc_succ.toFloat()) / ntrial) * 100F).roundToInt() // Calculate success percentage
        }
    }

    /**
     * Adds data from a completed trial to this summary row.
     * It increments the trial count ([ntrial]), adds the trial's elapsed time to the cumulative [rt],
     * and increments [perc_succ] if the trial was successful.
     *
     * @param trial The [TrialBasic] object containing data from the completed trial.
     */
    open fun add(trial: TrialBasic){
        ntrial++
        rt += trial.elapsed
        if(trial.success)   perc_succ++
    }

    /**
     * Generates a string representation of this summary row, typically for display or file output.
     * The format is tab-separated and includes the label, sub-label (if present), number of trials,
     * percentage of success, and mean reaction time.
     *
     * Format without sub-label: "[label]\t[ntrial]\t[perc_succ]\t[rt]\n"
     * Format with sub-label:    "[label]\t[sub_label]\t[ntrial]\t[perc_succ]\t[rt]\n"
     *
     * @return A tab-separated string representing the data in this summary row.
     */
    override fun toString():String{
        return if(sub_label.isEmpty()) {
            "$label\t$ntrial\t$perc_succ\t$rt\n"
        } else {
            "$label\t$sub_label\t$ntrial\t$perc_succ\t$rt\n"
        }
    }
}