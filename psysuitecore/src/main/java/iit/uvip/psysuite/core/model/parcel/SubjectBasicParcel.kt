package iit.uvip.psysuite.core.model.parcel

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.TestBasic.Companion.TEST_NEXTTRIAL_NOCHOOSE
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.getLabelLog

import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.accessory.getDateString
import org.albaspazio.core.accessory.getFullDateString
import org.albaspazio.core.filesystem.*

/*
base class for all Subjects information
This class manage simple subjects that participate in tests with only one condition.

 created one parcel for each test,
 initializing all the options presently
 particularly: classes, nextTrailModality, showFeedback, canRepeat, showResult, whitenoise
*/

abstract class SubjectBasicParcel(
    open var classes: List<String> = listOf(),
    open var label: String = "",
    open var age: Int = -1,
    open var gender: Int = -1,
    open var population: Int = Populations.POPULATION_TD,
    open var type: Int = -1,

    open var block: Int = -1,
    open var isDebug: Boolean = false,
    open var device: Device? = null,
    open var vercode: Int = -1,
    open var stimuliDelays: DelaysAligner = DelaysAligner(),

    open var nextTrailModality: Int = TEST_NEXTTRIAL_NOCHOOSE,
    open var whitenoise: Int    = TestBasic.TEST_SWITCH_CHOOSE_ON,
    open var trman_type: Int    = TestBasic.TEST_TRMAN_FIXED,
    open var showResult: Int    = TestBasic.TEST_SWITCH_DISABLED,
    open var canRepeat:Int      = TestBasic.TEST_SWITCH_CHOOSE_OFF
) : Parcelable {

    @IgnoredOnParcel var subjectFileName:String = ""

    companion object  {
        @JvmStatic val CURR_SUBJ_FILE:String = "curr_subject"

        @JvmStatic val ERROR_SUBJECT_INCOMPLETE:Int = 2

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

    open fun loadSubject(): SubjectBasicParcel {
        val subj = existFile(CURR_SUBJ_FILE + TestBasic.SUBJFILE_EXTENSION)
        if (subj.first) {
            val jsontext = readText(CURR_SUBJ_FILE + TestBasic.SUBJFILE_EXTENSION)
            return try {
                loadJsonText(jsontext)
            } catch (e: Exception) {
                this
            }
        }
        return this
    }

    private fun loadJsonText(jsontext: String): SubjectBasicParcel {

        val moshi           = Moshi.Builder().build()
        val jsonAdapter     = moshi.adapter(this.javaClass)
        return jsonAdapter.fromJson(jsontext)!!
    }

    // return   : -1 no file exist
    //          :  0 only one result file exist
    //          :  1-based last block file  (if it finds lab_type_2.txt => return 3)
    open fun existSubjectFile(ctx: Context):Int{
        val prefix = getFilesPrefix(ctx)
        return  if(existFileStartingWith(prefix, allowedext = listOf(".json")))
                        getLastValidBlock(prefix, listOf(".json"))
                else
                        -1
    }

    // returns 0 if no old blocks exist, otherwise last block+1
    private fun getLastValidBlock(prefix: String, allowedext: List<String> = listOf()):Int{
        val list = getFileList(allowedext = allowedext, contains = prefix)
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
    // used by the following three methods to compose subject files
    // RETURNS: "${label}_${age}_${gender}_${type_label}_$population_label"
    open fun getFilesPrefix(ctx: Context):String {

        val ci                  = getCompanionObjectMethod(classes[0], "getConditionsInfo")
        @Suppress("UNCHECKED_CAST")
        val type_label          = (ci.first?.call(ci.second, ctx) as List<ConditionData>).getLabelLog(type)
        val population_label    = Populations.all_populations.getLabelLog(population)

        val gender_str          =   if(gender == 0) "m"
                                    else            "f"
        return "${label}_${age}_${gender_str}_${type_label}_$population_label"
    }

    // RETURNS: label_type_population(_blk)_datetime.txt
    open fun composeResultFileName(ctx: Context, blk: Int = -1):String{

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getFullDateString()}${blkstr}${TestBasic.RES_EXTENSION}"
    }

    // RETURNS: label_type_population(_blk)_datetime.txt
    open fun composeSummaryFileName(ctx: Context, blk: Int = -1):String{

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getFullDateString()}_summary${blkstr}${TestBasic.RES_EXTENSION}"
    }

    // RETURNS: label_type_population(_blk)_date.json
    open fun composeSubjectFileName(ctx: Context, blk: Int = -1):String{
        if(label.isBlank() || type == -1)   return ""

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getDateString()}${blkstr}${TestBasic.SUBJFILE_EXTENSION}"
    }

    // RETURNS: filename or "" if file does not exist
    fun getAbsoluteSubjectFilePath():String = getAbsoluteFilePath(subjectFileName).second   // is "" if file was not present

    // =============================================================================================================
    // WRITE
    // =============================================================================================================
    open fun writeJson(context: Context):Int{

        val moshi       = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(this.javaClass)

        return try {
                    // want to create subject file always without block info, I want to add block info only renaming it after a block stop
                    subjectFileName = composeSubjectFileName(context)
                    if(subjectFileName.isEmpty())   ERROR_SUBJECT_INCOMPLETE
                    else {
                        saveText(context, subjectFileName, jsonAdapter.toJson(this), forceOld = true)        // var jsontext = context!!.resources.openRawResource(R.raw.script_001).bufferedReader().use { it.readText() }
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