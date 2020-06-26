package iit.uvip.psysuite.core.common.subjects_parcel

import android.content.Context
import android.os.Parcelable
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iit.uvip.psysuite.core.common.TestBasic
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
    open var testClass:String = "",
    open var device:Device? = null


) : Parcelable {

    @IgnoredOnParcel
    private var subjectFileName:String = ""

    companion object  {
        @JvmStatic val CURR_SUBJ_FILE:String = "curr_subject"

        @JvmStatic val SUBJECTFILE_EXIST:Int = 1
        @JvmStatic val ERROR_SUBJECT_INCOMPLETE:Int = 2
        @JvmStatic val ERROR_GENERIC:Int = 3

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

    override fun equals(other: Any?): Boolean {
        if (other is SubjectBasicParcel) {
            return label.equals(other.label, ignoreCase = true)
        }
        return false
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

    fun existSubjectFile():Boolean{
        return existFileStartingWith(label, allowedext = listOf(".json"))
    }

    fun composeResultFileName():String{
        return "${label}_${type}_${getFullDateString()}${TestBasic.RES_EXTENSION}"
    }

    private fun composeSubjectFileName():String{
        if(label.isBlank() || type == -1)   return ""

        return "${label}_${type}_${getDateString()}${TestBasic.FILE_EXTENSION}"
    }

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
                    subjectFileName = composeSubjectFileName()
                    if(subjectFileName.isEmpty()){
                        ERROR_SUBJECT_INCOMPLETE
                    }
                    else {
                        if (existSubjectFile()) SUBJECTFILE_EXIST
                        else {
                            saveText(context, subjectFileName, jsonAdapter.toJson(this))        // var jsontext = context!!.resources.openRawResource(R.raw.script_001).bufferedReader().use { it.readText() }
                            0
                        }
                    }
        }
        catch (e: Exception){
            e.printStackTrace()
            throw(e)
        }
    }
    // =============================================================================================================
}