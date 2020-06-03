package iit.uvip.psysuite.core.common

import android.content.Context
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import org.albaspazio.core.accessory.deleteFile
import org.albaspazio.core.accessory.saveText
import java.util.*


/*
must contain all the possible codes

 */

abstract class TestBasic(protected val ctx: Context, protected open val data: SubjectBasicParcel) {


    companion object {

        @JvmStatic val TEST_BASIC_LABEL                 = "test"    // used by tests that have only one type
        @JvmStatic val FILE_EXTENSION: String = ".json"
        @JvmStatic val RES_EXTENSION: String = ".txt"
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

        //-----------------------------------------------------------------------------------------
        //
        //-----------------------------------------------------------------------------------------
        @JvmStatic val EVENT_STIMULI_START              = 200
        @JvmStatic val EVENT_STIMULI_END                = 201
        @JvmStatic val EVENT_GIVE_ANSWER                = 202
        @JvmStatic val EVENT_GIVE_VOCAL_ANSWER          = 203
        @JvmStatic val EVENT_GIVE_VOCAL_NORMAL_ANSWER   = 204
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
        //-----------------------------------------------------------------------------------------

        @JvmStatic val TEST_PRE                         = 230
        @JvmStatic val TEST_POST                        = 231
        @JvmStatic val TEST_TRAINING                    = 232

    }
    // they are just proxy for properties (implemented / edited) in each subclass

    val testEvent:PublishRelay<Int> = PublishRelay.create()
    var mQuestion:String            = ""

    var showTrialsID:Int        = 0     // define when display trial id(0: never, 1: only @ trial end, 2: always)
    var abortMode:Int           = 0     // define abort modality (0:in answer dialog @ trial end, 1:button @ trial end, 2:always)
    var nextTrailModality:Int   = 0     // define how trials are displayed. 0: automatically, 1: after a next button, 2: after answer

    protected var mTrials:MutableList<TrialBasic>    = mutableListOf()
    var nTrials:Int                             = 0
    var currTrial:Int                           = 0
    protected lateinit var mTrial: TrialBasic

    protected var mResultFile: String       = ""
    protected var mStimuliHandler: Handler  = Handler()

    var validAnswers: MutableList<String> = mutableListOf()


    protected abstract fun initTest()

    abstract fun onTrialEnd()
    abstract fun show(trialid:Int, isRepeat:Boolean=false)

    // ===============================================================================================================
    protected fun createResultFile(subj_label:String, header:String){

        val c = Calendar.getInstance()
        mResultFile = subj_label + "_" +
                c.get(Calendar.YEAR).toString() +
                c.get(Calendar.MONTH).toString() +
                c.get(Calendar.DAY_OF_MONTH).toString() +
                c.get(Calendar.HOUR).toString() +
                c.get(Calendar.MINUTE).toString() +
                c.get(Calendar.SECOND).toString()
        mResultFile += ".txt"

        saveText(ctx, mResultFile, header)
    }

    open fun nextTrial(prev_result: String = "", elapsed: Int = -1): Int {

        if(currTrial == (nTrials - 1))
        {
            // END !
            if (prev_result != "")
                setResponse(prev_result, elapsed, true, true)
            return EVENT_TEST_END
        }
        else
        {
            if (prev_result != "")
                setResponse(prev_result, elapsed, true, false)
            currTrial++
            show(currTrial)
        }
        return currTrial
    }

    // calculate test result (== 0 first button || == 1 second button)
    open fun setResponse(result: String, elapsed: Int, writeit: Boolean, notifyDM: Boolean) {
        mTrial.setResponse(result, elapsed)

        if(writeit)
            saveText(ctx, mResultFile, mTrial.Log(), notifyDm = notifyDM)
    }

    open fun abortTest(){
        mStimuliHandler.removeCallbacksAndMessages(null)
        deleteFile(mResultFile)
    }
}

data class TaskCode(val label: String, val id: Int) : Parcelable {

    private constructor(parcel: Parcel) : this(
        label = parcel.readString()!!,
        id = parcel.readInt()
    )

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<TaskCode> {
            override fun createFromParcel(parcel: Parcel) = TaskCode(parcel)
            override fun newArray(size: Int) = arrayOfNulls<TaskCode>(size)
        }
    }

    override fun toString(): String {
        return label
    }

    override fun describeContents(): Int {
        // TODO Auto-generated method stub
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(label)
        dest.writeInt(id)
    }
}