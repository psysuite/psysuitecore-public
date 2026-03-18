package org.albaspazio.psysuite.tests

import android.content.Context
import android.os.Environment
import android.os.Parcelable
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.parcelize.IgnoredOnParcel
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.accessory.getDateString
import org.albaspazio.core.accessory.getFullDateString
import org.albaspazio.core.filesystem.existFile
import org.albaspazio.core.filesystem.existFileStartingWith
import org.albaspazio.core.filesystem.getAbsoluteFilePath
import org.albaspazio.core.filesystem.getFileList
import org.albaspazio.core.filesystem.readText
import org.albaspazio.core.filesystem.saveText
import org.albaspazio.psysuite.core.R
import org.albaspazio.psysuite.model.Populations
import org.albaspazio.psysuite.stimuli.DelaysAligner
import org.albaspazio.psysuite.utility.ConditionData
import org.albaspazio.psysuite.utility.filesystem.FileSystemManager
import org.albaspazio.psysuite.utility.getIds
import org.albaspazio.psysuite.utility.getLabelLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Abstract base class for subject configurations, implementing [android.os.Parcelable] for easy transport.
 * This class manages common subject information and test parameters.
 * It provides functionality for:
 * - Storing basic subject demographics (label, age, gender, population).
 * - Holding test-specific parameters (type, block, debug status, device info, stimuli delays, trial progression modalities).
 * - Supporting longitudinal tests with session/spinner selection (when session_spsel != TEST_NO_LONGITUDINAL).
 * - Generating standardized file names for subject data, results, and summaries.
 * - Saving and loading subject configurations to/from JSON files.
 * - Validating subject information.
 *
 * All tests are intrinsically longitudinal. For non-longitudinal tests, set session_spsel = TEST_NO_LONGITUDINAL
 * to hide the longitudinal UI elements and ignore session functionality.
 *
 * @param classes List of class names, typically used for reflection or identification. Used to find companion object methods.
 * @param label A descriptive label or identifier for the subject (e.g., name or code). Defaults to an empty string.
 * @param age The age of the subject. Defaults to -1 (unknown).
 * @param gender The gender of the subject (e.g., 0 for male, 1 for female). Defaults to -1 (unknown).
 * @param population The population group the subject belongs to (e.g., [org.albaspazio.psysuite.model.Populations.Companion.POPULATION_TD]). Defaults to [org.albaspazio.psysuite.model.Populations.Companion.POPULATION_TD].
 * @param type An integer code representing the specific type of test or configuration. Defaults to -1.
 * @param project The name of the project this subject belongs to. Defaults to an empty string.
 * @param block The current block number in a series of tests. Defaults to -1.
 * @param isDebug Flag indicating if the test is running in debug mode. Defaults to `false`.
 * @param device Information about the device running the test. Defaults to `null`.
 * @param vercode Version code of the application or test suite. Defaults to -1.
 * @param stimuliDelays Configuration for aligning stimuli delays. Defaults to a new [org.albaspazio.psysuite.stimuli.DelaysAligner] instance.
 * @param nextTrailModality How the test proceeds to the next trial (e.g., [TestBasic.Companion.TEST_NEXTTRIAL_AUTO]). Defaults to [TEST_NEXTTRIAL_AUTO].
 * @param whitenoise Configuration for white noise during the test (e.g., [TestBasic.Companion.TEST_SWITCH_CHOOSE_ON]). Defaults to [TestBasic.Companion.TEST_SWITCH_CHOOSE_ON].
 * @param trman_type Trial management type (e.g., [TestBasic.Companion.TEST_TRMAN_FIXED]). Defaults to [TestBasic.Companion.TEST_TRMAN_FIXED].
 * @param showResult Configuration for showing results after a trial/test. Defaults to [TestBasic.Companion.TEST_SWITCH_DISABLED].
 * @param canRepeat Configuration for allowing trial repetition. Defaults to [TestBasic.Companion.TEST_SWITCH_CHOOSE_OFF].
 * @param doTraining Configuration for enabling a training phase. Defaults to [TestBasic.Companion.TEST_SWITCH_DISABLED].
 * @param showTrialID Configuration for showing trial IDs. Defaults to [TestBasic.Companion.TEST_SHOWTRIALS_NEVER].
 * @param abortMode Configuration for aborting the test. Defaults to [TestBasic.Companion.TEST_ABORT_TRIALEND].
 * @param session_spsel The currently selected item's index in the spinner. Set to TEST_NO_LONGITUDINAL for non-longitudinal tests. Defaults to TEST_NO_LONGITUDINAL.
 * @param spinner_label The label associated with the current spinner selection. Defaults to "session".
 * @param session_spdatares The resource ID for the data populating the spinner (e.g., a string array). Defaults to -1.
 * @param date The creation/modification date in ISO 8601 format (yyyy-MM-dd HH:mm:ss). Set automatically in writeJson(). Defaults to empty string.
 * @param exp_uid A unique identifier for the experiment instance, generated automatically in writeJson(). Defaults to empty string.
 */
abstract class SubjectBasicParcel(
    open var classes: List<String> = listOf(),
    open var label: String = "",
    open var age: Int = -1,
    open var gender: Int = -1,
    open var population: Int = Populations.Companion.POPULATION_TD,
    open var type: Int = -1,
    open var project: String = "",

    open var block: Int = -1,
    open var isDebug: Boolean = false,
    open var device: Device? = null,
    open var vercode: Int = -1,
    open var stimuliDelays: DelaysAligner = DelaysAligner(),

    open var nextTrailModality: Int = TestBasic.Companion.TEST_NEXTTRIAL_AUTO,
    open var whitenoise: Int    = TestBasic.Companion.TEST_SWITCH_CHOOSE_ON,
    open var trman_type: Int    = TestBasic.Companion.TEST_TRMAN_FIXED,
    open var showResult: Int    = TestBasic.Companion.TEST_SWITCH_DISABLED,
    open var canRepeat:Int      = TestBasic.Companion.TEST_SWITCH_CHOOSE_OFF,
    open var doTraining: Int    = TestBasic.Companion.TEST_SWITCH_DISABLED,

    open var showTrialID: Int = TestBasic.Companion.TEST_SHOWTRIALS_NEVER,
    open var abortMode: Int = TestBasic.Companion.TEST_ABORT_TRIALEND,

    open var session_spsel: Int = TestBasic.Companion.TEST_NO_LONGITUDINAL,
    open var session_spdatares: Int = R.array.sessions_array,
    open var date: String = "",
    open var exp_uid: String = ""
) : Parcelable {

    var session: String = ""

    /** The name of the file where this subject's data is stored. Not included in Parcelization. */
    @IgnoredOnParcel
    var subjectFileName:String = ""

    @IgnoredOnParcel
    private val outFolder = "${Environment.DIRECTORY_DOWNLOADS}/${FileSystemManager.Companion.RESULTS_FOLDER_NAME}"


    companion object  {
        /** Default filename for the current subject's data before specific naming is applied. */
        @JvmStatic val CURR_SUBJ_FILE:String = "curr_subject"

        /** Error code indicating that the subject information is incomplete for saving. */
        @JvmStatic val ERROR_SUBJECT_INCOMPLETE:Int = 2

        /**
         * Validates the subject's label and age.
         * @param lab The subject's label (name/code).
         * @param ag The subject's age as a string.
         * @return A string containing error messages if validation fails, or an empty string if valid.
         */
        fun validate(lab: String, ag: String):String{
            var res = ""
            if (lab.isBlank()) res = res + "\n" + "il nome è vuoto"
            try {
                ag.toInt()
            }
            catch (e: NumberFormatException){
                res = res + "\n" + "l'eta inserita non è valida"
            }
            return res
        }
    }

    /**
     * Indicates whether this test configuration uses longitudinal functionality.
     * Returns true if session_spsel != TEST_NO_LONGITUDINAL, false otherwise.
     */
    val isLongitudinal: Boolean
        get() = session_spsel != TestBasic.Companion.TEST_NO_LONGITUDINAL

    /**
     * Loads subject data from the default [CURR_SUBJ_FILE] if it exists.
     * If the file exists and is valid JSON for this subject type, it updates the current instance with loaded data.
     * @return The loaded [SubjectBasicParcel] instance, or the current instance if loading fails or file doesn't exist.
     */
    open fun loadSubject(): SubjectBasicParcel {
        val subj = existFile(CURR_SUBJ_FILE + TestBasic.Companion.SUBJFILE_EXTENSION)
        if (subj.first) {
            val jsontext =
                readText(CURR_SUBJ_FILE + TestBasic.Companion.SUBJFILE_EXTENSION, outFolder)
            return try {
                loadJsonText(jsontext)
            } catch (e: Exception) {
                this
            }
        }
        return this
    }

    /** Indicates if the subject belongs to a visually impaired population. */
    val isBlindUser:Boolean = Populations.Companion.vi_populations.getIds().contains(population)
    /** Indicates if the subject belongs to an auditory impaired population. */
    val isDeafUser:Boolean  = Populations.Companion.ai_populations.getIds().contains(population)

    /**
     * Parses JSON text and converts it to a [SubjectBasicParcel] instance of the current object's type.
     * @param jsontext The JSON string to parse.
     * @return A [SubjectBasicParcel] instance populated from the JSON text.
     * @throws com.squareup.moshi.JsonDataException if the JSON is malformed or doesn't match the expected structure.
     */
    private fun loadJsonText(jsontext: String): SubjectBasicParcel {

        val moshi           = Moshi.Builder().build()
        val jsonAdapter     = moshi.adapter(this.javaClass)
        return jsonAdapter.fromJson(jsontext)!!
    }

    /**
     * Checks if a subject file matching the current subject's prefix exists and determines the last block number.
     * @param ctx Android [android.content.Context] for accessing application-specific information if needed by underlying file operations.
     * @return -1 if no subject file exists, 0 if a base subject file (without block number) exists,
     *         or N (1-based) representing the next block number if block files (e.g., prefix_blkN-1.json) exist.
     */
    open fun existSubjectFile(ctx: Context):Int{
        val prefix = getFilesPrefix(ctx)
        return  if(existFileStartingWith(prefix, allowedext = listOf(".json")))
                        getLastValidBlock(prefix, listOf(".json"))
                else
                        -1
    }

    /**
     * Finds the last valid block number for a given file prefix by inspecting filenames.
     * For example, if files `prefix_blk0.json` and `prefix_blk1.json` exist, it returns 2 (for the next block).
     * If only `prefix.json` or no block files exist, it returns 0.
     * @param prefix The file prefix to search for.
     * @param allowedext List of allowed file extensions. Defaults to empty list (any extension).
     * @return The next block number (0-based if no blocks, N if last block was N-1).
     */
    private fun getLastValidBlock(prefix: String, allowedext: List<String> = listOf()):Int{
        val list = getFileList(outFolder, allowedext = allowedext, contains = prefix)
        var blk = -1
        list.map{
            val words = it.name.split(".")[0].split("_blk")
            if(words.size  > 1) blk = blk.coerceAtLeast(words[1].toInt())
        }
        return (blk+1)
    }

    // =============================================================================================================
    // RETURNS FILES NAME
    // =============================================================================================================

    /**
     * Generates a standardized prefix for filenames associated with this subject and test configuration.
     * The prefix typically includes subject label, age, gender, test type label, trial management type, and population label.
     * Example: "JohnDoe_25_m_VisualTest_FX_TD"
     * @param ctx Android [Context], used to retrieve condition information via reflection based on `classes[0]`.
     * @return A string representing the file prefix.
     */
    open fun getFilesPrefix(ctx: Context):String {

        val ci                  = getCompanionObjectMethod(classes[0], "getConditionsInfo")
        @Suppress("UNCHECKED_CAST")
        val type_label          = (ci.first?.call(ci.second, ctx) as List<ConditionData>).getLabelLog(type)
        val population_label    = Populations.Companion.all_populations.getLabelLog(population)

        val gender_str          =   if(gender == 0) "m"
                                    else            "f"

        val trmantype_str       = when (trman_type) {
            TestBasic.Companion.TEST_TRMAN_ADAPTIVE -> "AD"
            TestBasic.Companion.TEST_TRMAN_FIXED -> "FX"
            else -> "MX" // Mixed
        }

        return "${label}_${age}_${gender_str}_${type_label}_${trmantype_str}_${population_label}_sess$session"
    }

    /**
     * Composes a filename for storing test results.
     * The filename includes the prefix from [getFilesPrefix], the current full date and time, an optional block indicator, and a standard results extension.
     * Example: "JohnDoe_25_m_VisualTest_FX_TD_YYYYMMDDHHMMSS_blk1.txt"
     * @param ctx Android [Context] passed to [getFilesPrefix].
     * @param blk Optional block number. If > -1, "_blkX" is appended. Defaults to -1 (no block indicator).
     * @return The composed results filename.
     */
    open fun composeResultFileName(ctx: Context, blk: Int = -1):String{

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getFullDateString()}${blkstr}${TestBasic.Companion.RES_EXTENSION}"
    }

    /**
     * Composes a filename for storing test summaries.
     * Similar to [composeResultFileName] but includes "_summary" in the name.
     * Example: "JohnDoe_25_m_VisualTest_FX_TD_YYYYMMDDHHMMSS_summary_blk1.txt"
     * @param ctx Android [Context] passed to [getFilesPrefix].
     * @param blk Optional block number. If > -1, "_blkX" is appended. Defaults to -1 (no block indicator).
     * @return The composed summary filename.
     */
    open fun composeSummaryFileName(ctx: Context, blk: Int = -1):String{

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getFullDateString()}_summary${blkstr}${TestBasic.Companion.RES_EXTENSION}"
    }

    /**
     * Composes a filename for storing the subject's configuration data (JSON).
     * The filename includes the prefix from [getFilesPrefix], the current date, an optional block indicator, and a standard subject file extension.
     * Returns an empty string if the subject label or type is not set.
     * Example: "JohnDoe_25_m_VisualTest_FX_TD_YYYYMMDD_blk1.json"
     * @param ctx Android [Context] passed to [getFilesPrefix].
     * @param blk Optional block number. If > -1, "_blkX" is appended. Defaults to -1 (no block indicator).
     * @return The composed subject data filename, or an empty string if essential info is missing.
     */
    open fun composeSubjectFileName(ctx: Context, blk: Int = -1):String{
        if(label.isBlank() || type == -1)   return ""

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getDateString()}${blkstr}${TestBasic.Companion.SUBJFILE_EXTENSION}"
    }

    /**
     * Gets the absolute file path for the currently set [subjectFileName].
     * @return The absolute path as a string, or an empty string if [subjectFileName] is not set or the file doesn't exist.
     */
    val absoluteSubjectFilePath:String
            get() = getAbsoluteFilePath(subjectFileName, outFolder).second   // is "" if file was not present

    // =============================================================================================================
    // WRITE
    // =============================================================================================================
    /**
     * Writes the current subject configuration to a JSON file.
     * The filename is generated by [composeSubjectFileName] (without block info initially).
     * The [subjectFileName] property is updated with the name of the created file.
     * @param context Android [Context] used for file operations and by [composeSubjectFileName].
     * @return 0 on successful save, or [ERROR_SUBJECT_INCOMPLETE] if essential information (label, type) is missing.
     * @throws Exception if JSON serialization or file writing fails.
     */
    open fun writeJson(context: Context):Int{

        val moshi       = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(this.javaClass)

        return try {
                    // Set current date in ISO 8601 format
                    date = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())

                    // Set unique experiment ID
                    exp_uid = "${classes[0].substringAfterLast(".")}${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"

                    // want to create subject file always without block info, I want to add block info only renaming it after a block stop
                    subjectFileName = composeSubjectFileName(context)
                    if(subjectFileName.isEmpty())   ERROR_SUBJECT_INCOMPLETE
                    else {
                        saveText(
                            context,
                            subjectFileName,
                            jsonAdapter.toJson(this),
                            outFolder,
                            forceOld = true
                        )
                        0
                    }
                }
                catch (e: Exception){
                    e.printStackTrace()
                    throw(e)
                }
    }
    // =============================================================================================================
}