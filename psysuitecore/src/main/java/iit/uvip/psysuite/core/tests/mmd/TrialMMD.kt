package iit.uvip.psysuite.core.tests.mmd

import iit.uvip.psysuite.core.trials.TrialBasic

/**
 * Represents a single trial in the Motion-Defined Motion (MMD) test.
 * Each trial involves presenting an auditory stimulus (or pair) and recording the subject's response.
 *
 * @property id The unique identifier for this trial. Inherited from [TrialBasic]. Defaults to -1.
 * @property type An integer code representing the type of trial. For MMD, this typically distinguishes
 *                between "same" (e.g., 0) and "different" (e.g., 1) stimulus conditions.
 * @property label A descriptive label for the trial (e.g., "same", "different"). Inherited from [TrialBasic].
 * @property correct_answer The correct answer for this trial (e.g., 0 for "same", 1 for "different", or vice-versa).
 *                          This is used to determine if the user's response was correct.
 * @property audio_id An identifier for the specific audio stimulus or stimulus pair used in this trial.
 *                    This helps in selecting the correct audio resource for playback.
 */
class TrialMMD(id: Int = -1, type: Int, label: String, override var correct_answer:Int, var audio_id: Int) :
    TrialBasic(id, type, label) {

    /**
     * Companion object for [TrialMMD] holding constants.
     */
    companion object {
        /**
         * Header string for logging MMD trial data to a result file.
         * Defines the columns: id, label, result (success), correct answer, user answer, elapsed time, repetitions, audio_id.
         */
        @JvmStatic val LOG_HEADER           = "id\tlabel\tres\tcor_ans\tuser_ans\telapsed\trep\taudio_id\n"
    }

    /**
     * Provides a string representation of the trial, typically for simpler display or debugging.
     * Includes id, type, label, success status, and audio_id.
     *
     * @return A tab-separated string with key trial information.
     */
    override fun toString():String{
        return "$id\t$type\t$label\t$success\t$audio_id\n"
    }

    /**
     * Formats the trial data for logging to a result file.
     * Uses the format defined by [LOG_HEADER].
     *
     * @return A tab-separated string containing all data fields for logging.
     */
    override fun Log():String{
        return "$id\t$label\t$success\t$correct_answer\t$user_answer\t$elapsed\t$repetitions\t$audio_id\n"
    }

    /**
     * Provides additional debug information for this trial, appending the audio_id to the base debug info.
     *
     * @return A string with detailed debug information for the trial.
     */
    override fun debugInfo():String{
        return "${super.debugInfo()}, id_audio=$audio_id"
    }
}
