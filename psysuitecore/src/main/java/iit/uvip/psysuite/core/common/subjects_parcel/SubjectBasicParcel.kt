package iit.uvip.psysuite.core.common.subjects_parcel

import android.content.Context
import android.os.Parcelable
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iit.uvip.psysuite.core.common.DelaysAligner
import iit.uvip.psysuite.core.common.SpinnerData
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.getLabelLog
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.*

/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
*/

// base class for all tests
// nextTrailModality = -1 => do not show switch button in the gui

@Parcelize
open class SubjectBasicParcel(
    open var type: Int = -1,
    open var label: String = "",
    open var age: Int = -1,
    open var gender: Int = -1,
    open var nextTrailModality: Int = -1,
    open var canRecordAudio:Boolean = false,
    open var classes:List<String> = listOf(),
    open var device:Device? = null,
    open var block:Int = -1,
    open var stimuliDelays:DelaysAligner = DelaysAligner(),
    open var whitenoise: Int = TestBasic.TEST_WNOISE_CHOOSE_ON,
    open var vercode: Int = -1,
    open var showResult: Boolean = false,
    open var population: Int = TestBasic.POPULATION_TD,
    open var isDebug:Boolean = false
) : Parcelable {

    @IgnoredOnParcel
    var subjectFileName:String = ""

    companion object  {
        @JvmStatic val CURR_SUBJ_FILE:String = "curr_subject"

        @JvmStatic val ERROR_SUBJECT_INCOMPLETE:Int = 2

        fun validate(lab:String, ag:String):String{
            var res = ""
            if (lab.isBlank()) res = res + "\n" + "il nome è vuoto"
            try {
                ag.toInt()
            }
            catch(e:NumberFormatException){
                res = res + "\n" + "l'eta inserita non è valida"
            }
            return res
        }
    }

    open fun loadSubject(): SubjectBasicParcel {
        val subj = existFile(CURR_SUBJ_FILE + TestBasic.FILE_EXTENSION)
        if (subj.first) {
            val jsontext = readText(CURR_SUBJ_FILE + TestBasic.FILE_EXTENSION)
            return try {
                loadJsonText(jsontext)
            } catch (e: Exception) {
                this
            }
        }
        return this
    }

    private fun loadJsonText(jsontext:String): SubjectBasicParcel {

        val moshi           = Moshi.Builder().build()
        val jsonAdapter     = moshi.adapter(this.javaClass)
        return jsonAdapter.fromJson(jsontext)!!
    }

    // return   : -1 no file exist
    //          :  0 only one result file exist
    //          :  1-based last block file  (if it finds lab_type_2.txt => return 3)
    open fun existSubjectFile(ctx:Context):Int{
        val prefix = getFilesPrefix(ctx)
        return  if(existFileStartingWith(prefix, allowedext = listOf(".json")))
                        getLastValidBlock(prefix, listOf(".json"))
                else
                        -1
    }

    private fun getLastValidBlock(prefix:String, allowedext:List<String> = listOf()):Int{
        val list = getFileList(allowedext = allowedext, contains = prefix)
        var blk = -1
        list.map{
            val words = it.name.split(".")[0].split("_blk")
            if(words.size  > 1) blk = blk.coerceAtLeast(words[1].toInt())
        }
        return (blk+1)
    }

    open fun getFilesPrefix(ctx:Context):String {

        val ci                  = getCompanionObjectMethod(classes[0], "getConditionsInfo")
        val type_label          = (ci.first?.call(ci.second, ctx) as List<SpinnerData>).getLabelLog(type)
        val population_label    = TestBasic.populations.getLabelLog(population)

        return "${label}_${type_label}_$population_label"
    }

    // label_type_population(_blk)_datetime.txt
    open fun composeResultFileName(ctx:Context, blk:Int = -1):String{

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getFullDateString()}${blkstr}${TestBasic.RES_EXTENSION}"
    }

    // label_type_population(_blk)_datetime.txt
    open fun composeSummaryFileName(ctx:Context, blk:Int = -1):String{

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getFullDateString()}_summary${blkstr}${TestBasic.RES_EXTENSION}"
    }

    // label_type_population(_blk)_date.json
    open fun composeSubjectFileName(ctx:Context, blk:Int = -1):String{
        if(label.isBlank() || type == -1)   return ""

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""
        return "${getFilesPrefix(ctx)}_${getDateString()}${blkstr}${TestBasic.FILE_EXTENSION}"
    }

    // return filename or "" if file does not exist
    fun getAbsoluteSubjectFilePath(): String{
        return getAbsoluteFilePath(subjectFileName).second
    }

    // =============================================================================================================
    // WRITE
    // =============================================================================================================
    open fun writeJson(context:Context):Int{

        val moshi       = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(this.javaClass)

        return try {
                    subjectFileName = composeSubjectFileName(context, block)
                    if(subjectFileName.isEmpty()){
                        ERROR_SUBJECT_INCOMPLETE
                    }
                    else {
                        saveText(context, subjectFileName, jsonAdapter.toJson(this))        // var jsontext = context!!.resources.openRawResource(R.raw.script_001).bufferedReader().use { it.readText() }
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