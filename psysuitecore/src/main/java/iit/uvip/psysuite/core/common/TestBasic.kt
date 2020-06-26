package iit.uvip.psysuite.core.common

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Parcelable
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.deleteFile
import org.albaspazio.core.accessory.existFile
import org.albaspazio.core.accessory.getAbsoluteFilePath
import org.albaspazio.core.accessory.saveText
import java.io.File
import java.util.*


/*
must contain all the possible codes

 */

abstract class TestBasic(protected val ctx: Context, protected open val data: SubjectBasicParcel) {

    companion object {

        @JvmStatic val TESTINFO_BUNDLE_LABEL           = "test"    // used as subject-test bundle element label
        @JvmStatic val FILE_EXTENSION: String           = ".json"
        @JvmStatic val RES_EXTENSION: String            = ".txt"
        @JvmStatic val TEST_BUNDLE_RES_FILE             = "result_file"    // used as subject-test bundle element label
        @JvmStatic val TEST_BUNDLE_RESULT_LABEL: String = "result"
        // --------------------------------------------------------------------------------------------
        // trial-by-trial management
        //-----------------------------------------------------------------------------------------
        @JvmStatic val TEST_SHOWTRIALS_NEVER            = 0         //  SHOWTRIALS_NEVER
        @JvmStatic val TEST_SHOWTRIALS_TRIALEND         = 1         //  SHOWTRIALS_TRIALEND
        @JvmStatic val TEST_SHOWTRIALS_ALWAYS           = 2         //  SHOWTRIALS_ALWAYS

        @JvmStatic val TEST_ABORT_ANSWER                = 0         //  SHOWTRIALS_NEVER
        @JvmStatic val TEST_ABORT_TRIALEND              = 1         //  SHOWTRIALS_TRIALEND
        @JvmStatic val TEST_ABORT_ALWAYS                = 2         //  SHOWTRIALS_ALWAYS

        @JvmStatic val TEST_NEXTTRIAL_NOCHOOSE          = -1        //  goes directly to next trial, does not allow user to modify it
        @JvmStatic val TEST_NEXTTRIAL_AUTO              = 0         //  user can select to go directly to next trial
        @JvmStatic val TEST_NEXTTRIAL_BUTTON            = 1         //  user can select to wait and then press a NEXT button
        @JvmStatic val TEST_NEXTTRIAL_ANSWER            = 2         //  wait for ANSWER dialog
        @JvmStatic val TEST_NEXTTRIAL_VOICE_ANSWER      = 3         //  wait for VOICE ANSWER dialog through speech recognition
        @JvmStatic val TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER = 4       //  wait for either ANSWER dialog or VOICE ANSWER through speech recognition

        //-----------------------------------------------------------------------------------------
        //
        //-----------------------------------------------------------------------------------------
        @JvmStatic val EVENT_STIMULI_START              = 200
        @JvmStatic val EVENT_STIMULI_END                = 201
        @JvmStatic val EVENT_GIVE_ANSWER                = 202
        @JvmStatic val EVENT_GIVE_VOCAL_ANSWER          = 203
        @JvmStatic val EVENT_ANSWER_GIVEN               = 205
        @JvmStatic val EVENT_TRIAL_REPEAT               = 206
        @JvmStatic val EVENT_TRIAL_ABORT                = 207
        @JvmStatic val EVENT_TEST_END                   = 208
        @JvmStatic val EVENT_SHOW_NEXT_BUTTON           = 209
        @JvmStatic val EVENT_UPDATE_TRIAL_ID            = 210
        @JvmStatic val EVENT_UPDATE_TRIAL_ID_REMOVE     = 211   // update trial id and remove it after 1 sec
        @JvmStatic val EVENT_SHOW_1SECABORT             = 212   // show abort button for 1 sec

        //-----------------------------------------------------------------------------------------
        // TESTS UNIQUE CODES
        //-----------------------------------------------------------------------------------------
        @JvmStatic val TEST_BISECTION_AUDIO         = 100
        @JvmStatic val TEST_BISECTION_TACTILE       = 101
        @JvmStatic val TEST_BISECTION_AUDIO_TACTILE = 102
        @JvmStatic val TEST_BISECTION_AUDIO_VIDEO   = 103

        @JvmStatic val TEST_MUSICAL_METERS          = 110

        @JvmStatic val TEST_TID_SHORT_AUDIO         = 120
        @JvmStatic val TEST_TID_SHORT_TACTILE       = 121
        @JvmStatic val TEST_TID_LONG_AUDIO          = 122
        @JvmStatic val TEST_TID_LONG_TACTILE        = 123

        @JvmStatic val TEST_ATB_TIME                = 130
        @JvmStatic val TEST_ATB_FREQUENCY           = 131
        @JvmStatic val TEST_ATB_TIME_INF            = 132
        @JvmStatic val TEST_ATB_TIME_INF_15s        = 134
        @JvmStatic val TEST_ATB_FREQUENCY_INF       = 133

        @JvmStatic val TEST_ATVB_TIME_SINGLESTIM    = 140
        @JvmStatic val TEST_ATVB_TIME_DOUBLESTIM    = 141
        @JvmStatic val TEST_ATVB_TIME_DOUBLESTIM2    = 142
        //-----------------------------------------------------------------------------------------

        @JvmStatic val TEST_ABORT                       = 230
        @JvmStatic val TEST_COMPLETED                   = 231

        @JvmStatic val TEST_PRE                         = 232
        @JvmStatic val TEST_POST                        = 233
        @JvmStatic val TEST_TRAINING                    = 234

    }

    val testEvent:PublishRelay<Int> = PublishRelay.create()
    var mQuestion:String            = ""

    var showTrialsID:Int        = 0     // define when display trial id(0: never, 1: only @ trial end, 2: always)
    var abortMode:Int           = 0     // define abort modality (0:in answer dialog @ trial end, 1:button @ trial end, 2:always)
    var nextTrailModality:Int   = 0     // define how trials are displayed. 0: automatically, 1: after a next button, 2: after answer

    var nTrials:Int                             = 0
    var currTrial:Int                           = 0
    private var mResultFile: String                 = ""

    var validAnswers: MutableList<String> = mutableListOf()

    // they are just proxy for properties (implemented / edited) in each subclass
    protected lateinit var mTrial: TrialBasic
    protected var mTrials:MutableList<TrialBasic>    = mutableListOf()
    protected var mStimuliHandler: Handler  = Handler()

    protected abstract fun initTest()
    abstract fun onTrialEnd()
    abstract fun show(trialid:Int, isRepeat:Boolean=false)

    // ===============================================================================================================
    protected fun createResultFile(subj:SubjectBasicParcel, header:String){
        mResultFile = subj.composeResultFileName()
        saveText(ctx, mResultFile, header)
    }

    fun getResultFile(): String{
        return  if(existFile(mResultFile).first)    mResultFile
                else                                ""
    }

    fun getAbsoluteResultFilePath(): String{
        return getAbsoluteFilePath(mResultFile).second
    }

    protected fun setTrialsID(){
        mTrials.mapIndexed { index, trialBasic ->
            trialBasic.id = index }           // set trial id according to its order in the list
    }

    open fun nextTrial(prev_result: String = "", elapsed: Int = -1): Int {

        if (prev_result != "")  mTrial.setResponse(prev_result, elapsed)

        if(currTrial == (nTrials - 1)) {
            saveText(ctx, mResultFile, mTrial.Log(), overwrite = false, notifyDm = true)
            return EVENT_TEST_END            // END !
        }
        else
        {
            saveText(ctx, mResultFile, mTrial.Log(), overwrite = false, notifyDm = false)
            currTrial++
            show(currTrial)
        }
        return currTrial
    }

    fun abortTest(deletelog:Boolean=false, dir:String= Environment.DIRECTORY_DOWNLOADS){
        mStimuliHandler.removeCallbacksAndMessages(null)
        if(deletelog)   deleteFile(mResultFile)
        else{
            val path    = Environment.getExternalStoragePublicDirectory(dir)
            val file    = File(path, mResultFile)
            val down    = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            down.addCompletedDownload(file.name, "User file", false, "text/plain", file.path, file.length(), true)
        }
    }
}

@Parcelize
data class TaskCode(val label: String, val id: Int) : Parcelable{

    override fun toString(): String {
        return label
    }
}

@Parcelize
data class TestResult(var code:Int=-1, var res_files:ArrayList<String> = arrayListOf()) : Parcelable

data class Stimulus(val type: Int, val delay: Long)
data class Stimulus2(val a:Long, val t:Long, val v:Long)

