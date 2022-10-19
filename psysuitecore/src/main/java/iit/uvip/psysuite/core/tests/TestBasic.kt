package iit.uvip.psysuite.core.tests

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.stimuli.StimuliManager
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.logLastTwo
import org.albaspazio.core.filesystem.*
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showAlert



/*
must contain all the possible codes

 */

abstract class TestBasic(protected val ctx: Context,
                         protected val activity: Activity,
                         protected val hostfragment: Fragment,
                         protected val subject: SubjectBasicParcel,
                         protected val vibrator: VibrationManager? = null,
                         protected val mImageView: ImageView? = null,
                         protected val speechManager: SpeechManager? = null
) {
    open var LOG_TAG:String = TestBasic::class.java.simpleName

    companion object {

        @JvmStatic val TESTINFO_BUNDLE_LABEL            = "test"    // used as subject-test bundle element label
        @JvmStatic val SUBJFILE_EXTENSION: String       = ".json"
        @JvmStatic val RES_EXTENSION: String            = ".txt"
        @JvmStatic val TEST_BUNDLE_RESULT_LABEL: String = "result"

        @JvmStatic val audioResources:HashMap<Long, String> = hashMapOf(
            7L to "t1000hz_7ms.wav",
            10L to "t1000hz_10ms.wav",
            17L to "t1000hz_17ms.wav",
            20L to "t1000hz_20ms.wav",
            30L to "t1000hz_30ms.wav",
            35L to "t1000hz_35ms.wav",
            50L to "t1000hz_50ms.wav",
            100L to "t1000hz_100ms.wav",
            1000L to "t1000hz_1000ms.wav",
        )
        // --------------------------------------------------------------------------------------------
        // trial-by-trial management
        //-----------------------------------------------------------------------------------------
//        @JvmStatic val TEST_SHOWTRIALS_NEVER            = 0         //  SHOWTRIALS_NEVER
        @JvmStatic val TEST_SHOWTRIALS_TRIALEND         = 1         //  SHOWTRIALS_TRIALEND
        @JvmStatic val TEST_SHOWTRIALS_ALWAYS           = 2         //  SHOWTRIALS_ALWAYS

//        @JvmStatic val TEST_ABORT_ANSWER                = 0         //  never show abort button (it is displayed in the answer dialog)
        @JvmStatic val TEST_ABORT_TRIALEND              = 1         //  show abort button at each trial end (when answer dialog does not appear)
        @JvmStatic val TEST_ABORT_ALWAYS                = 2         //  keep abort button always active

        @JvmStatic val TEST_NEXTTRIAL_NOCHOOSE          = -1        //  goes directly to next trial, does not allow user to modify it
        @JvmStatic val TEST_NEXTTRIAL_AUTO              = 0         //  user can select to go directly to next trial
        @JvmStatic val TEST_NEXTTRIAL_BUTTON            = 1         //  user can select to wait and then press a NEXT button
        @JvmStatic val TEST_NEXTTRIAL_ANSWER            = 2         //  wait for ANSWER dialog
        @JvmStatic val TEST_NEXTTRIAL_VOICE_ANSWER      = 3         //  wait for VOICE ANSWER dialog through speech recognition
        @JvmStatic val TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER = 4       //  wait for either ANSWER dialog or VOICE ANSWER through speech recognition

        @JvmStatic val TEST_WNOISE_DISABLED             = 0         //  disabled, cannot enable it
        @JvmStatic val TEST_WNOISE_CHOOSE_OFF           = 1         //  can choose, disabled by default
        @JvmStatic val TEST_WNOISE_CHOOSE_ON            = 2         //  can choose, enabled by default
        @JvmStatic val TEST_WNOISE_ENABLED              = 4         //  enabled, cannot disable it

        //-----------------------------------------------------------------------------------------
        //
        //-----------------------------------------------------------------------------------------
        @JvmStatic val EVENT_TEST_SETUP_COMPLETED       = 200

        @JvmStatic val EVENT_STIMULI_START              = 201   // unused
        @JvmStatic val EVENT_STIMULI_END                = 202   // unused
        @JvmStatic val EVENT_GIVE_ANSWER                = 203
        @JvmStatic val EVENT_GIVE_VOCAL_ANSWER          = 204
        @JvmStatic val EVENT_ANSWER_GIVEN               = 205
        @JvmStatic val EVENT_TRIAL_REPEAT               = 206
        @JvmStatic val EVENT_TRIAL_ABORT                = 207
        @JvmStatic val EVENT_TEST_END                   = -100
        @JvmStatic val EVENT_BLOCK_END                  = -101
        @JvmStatic val EVENT_TEST_ERROR                 = -102
        @JvmStatic val EVENT_SHOW_NEXT_BUTTON           = 209
        @JvmStatic val EVENT_UPDATE_TRIAL_ID            = 210   // update trial id and possibly remove it after X msec
        @JvmStatic val EVENT_SHOW_ABORT                 = 212   // show abort button for any ms sec
        @JvmStatic val EVENT_SHOW_DEBUGINFO             = 213   // show debug info text

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
        @JvmStatic val TEST_TID_SHORT_VISUAL        = 124
        @JvmStatic val TEST_TID_LONG_VISUAL         = 125
        @JvmStatic val TEST_TID_SHORT_AUDIO_TRAIN   = 126
        @JvmStatic val TEST_TID_SHORT_TACTILE_TRAIN = 127
        @JvmStatic val TEST_TID_SHORT_VISUAL_TRAIN  = 128

        @JvmStatic val TEST_ATB_TIME_SINGLESTIM     = 130
        @JvmStatic val TEST_ATB_TIME_DOUBLESTIM     = 131
        @JvmStatic val TEST_ATB_TIME_INF            = 132
        @JvmStatic val TEST_ATB_TIME_SINGLESTIM_TOD = 133
        @JvmStatic val TEST_ATB_TIME_DOUBLESTIM_TOD = 134

        @JvmStatic val TEST_ATVB_TIME_S_UNBAL       = 140
        @JvmStatic val TEST_ATVB_TIME_D_UNBAL       = 141
        @JvmStatic val TEST_ATVB_TIME_D_BAL         = 142
        @JvmStatic val TEST_ATVB_TIME_S_BAL         = 143
        @JvmStatic val TEST_ATVB_TIME_S_BAL2        = 144

        @JvmStatic val TEST_SAMPLE_ALIGNED          = 150
        @JvmStatic val TEST_SAMPLE_SHIFTED          = 151
        @JvmStatic val TEST_SAMPLE_PAIR             = 152

        @JvmStatic val TEST_TFI                     = 160
        @JvmStatic val TEST_TFI_TODDLERS            = 161
        @JvmStatic val TEST_TFI_BIMODAL             = 162
        @JvmStatic val TEST_TFI_AV                  = 163

        @JvmStatic val TEST_TVB_TIME_SINGLESTIM     = 170
        @JvmStatic val TEST_TVB_TIME_DOUBLESTIM     = 171
        @JvmStatic val TEST_TVB_TIME_INF            = 172
        @JvmStatic val TEST_TVB_TIME_SINGLESTIM_TOD = 173
        @JvmStatic val TEST_TVB_TIME_DOUBLESTIM_TOD = 174

        @JvmStatic val TEST_AVB_TIME_SINGLESTIM     = 180
        @JvmStatic val TEST_AVB_TIME_DOUBLESTIM     = 181
        @JvmStatic val TEST_AVB_TIME_INF            = 182
        @JvmStatic val TEST_AVB_TIME_SINGLESTIM_TOD = 183
        @JvmStatic val TEST_AVB_TIME_DOUBLESTIM_TOD = 184

        @JvmStatic val TEST_FGI_1_UNSCRAMBLED       = 190
        @JvmStatic val TEST_FGI_1_SCRAMBLED         = 191
        @JvmStatic val TEST_FGI_2_UNSCRAMBLED       = 192
        @JvmStatic val TEST_FGI_2_SCRAMBLED         = 193
        @JvmStatic val TEST_FGI_3_UNSCRAMBLED       = 194
        @JvmStatic val TEST_FGI_3_SCRAMBLED         = 195

        @JvmStatic val TEST_RIVGRP_RIV_HF           = 200
        @JvmStatic val TEST_RIVGRP_GRP_HF           = 201
        @JvmStatic val TEST_RIVGRP_RIVGRP_HF        = 202
        @JvmStatic val TEST_RIVGRP_RIV_HC           = 203
        @JvmStatic val TEST_RIVGRP_GRP_HC           = 204
        @JvmStatic val TEST_RIVGRP_RIVGRP_HC        = 205

        @JvmStatic val TEST_BEADS_LOWUNCERT         = 210
        @JvmStatic val TEST_BEADS_MIDUNCERT         = 211


        //-----------------------------------------------------------------------------------------
        @JvmStatic val TEST_ABORTED                     = 230
        @JvmStatic val TEST_COMPLETED                   = 231
        @JvmStatic val BLOCK_COMPLETED                  = 232
        @JvmStatic val TEST_ABORTED_WITH_ERROR          = 233


//        fun unimodaltypes2maintype(a:Int, t:Int, v:Int):Int = a or t or v
    }

    val testEvent:PublishRelay<Pair<Int,Any?>> = PublishRelay.create()

    var showTrialsID:Int        = 0     // define when display trial id(0: never, 1: only @ trial end, 2: always)
    var abortMode:Int           = 0     // define abort modality (0:in answer dialog @ trial end, 1:button @ trial end, 2:always)
    var nextTrailModality:Int   = 0     // define how trials are displayed. 0: automatically, 1: after a next button, 2: after answer

    var nTrials:Int                             = 0
    var currTrial:Int                           = 0
    var mTestLabel: String                      = ""
    var mQuestion:String                        = ""
    var validAnswers: MutableList<String>       = mutableListOf()
    val showResult:Boolean                      = this.subject.showResult
    val delaysAligner: DelaysAligner = this.subject.stimuliDelays


    // they are just proxy for properties (implemented / edited / accessed) in each subclass
    protected lateinit var mTrial: TrialBasic
    protected var mTrials:MutableList<TrialBasic>   = mutableListOf()

    protected var nBlocks:Int                       = 0

    protected var mListBlocks:MutableList<Int>      = mutableListOf()
        set(value) {
            field = value
            nBlocks = value.size + 1
        }

    // this instance is defined and validated during sub-class init{}
    // in case of error an exception is thrown and test is aborted.
    // the only susceptible to error is AudioManager in case of the test involves different resources to be loaded
    protected lateinit var mStimuliManager: StimuliManager

    protected var mNoise: MediaPlayer? = null

    protected open var mDrawablesResource:MutableList<Int>  = mutableListOf()   // list of drawables' resources id to be edited in subclasses
    protected var mStimuliHandler: Handler                  = Handler()

    protected var mSummary: Summary?                        = null
    private var mResultFile: String                         = ""
    private var mResultUri: Uri?                            = null

    private var mCurrBlock: Int                             = 0

    protected var currStimulusDuration:Long     = 100L          // default value to be used when stimulus duration in not given
//    protected var currVibrationAmplitude:Int    = -1            // default amplitude to be used when  not given
//    protected var currVolume:Float              = 1F            // default audio volume to be used when  not given
    protected var currAudioResourceName:String  = "t200hz_2s"   // default amplitude to be used when  not given
    protected var ITI:Long                      = 0             // default ITI

    // proxy for methods to be implemented in each subclass
    abstract fun initTest()
    abstract fun onTrialEnd()
    abstract fun show(trial: TrialBasic, isRepeat:Boolean=false)

    abstract fun initSummary()              // init summary content (mSummary),
//    fun closeSummary(blk:Int = -1):String = mSummary?.close(subject.composeSummaryFileName(ctx, blk)) ?: "" // writes summary and return filename or empty string

    // writes summary and return absolute filepath or empty string
    fun closeSummary(filename:String = ""):String{
        return  if(filename.isEmpty())  mSummary?.close(subject.composeSummaryFileName(ctx)) ?: ""
                else                    mSummary?.close(filename) ?: ""
    }

    // ===============================================================================================================
    fun start():Boolean{
        return  try {
//                    if(!mStimuliManager.isValid || !this::mTrial.isInitialized){
//                        onCriticalError(ctx.resources.getString(R.string.test_failure), true)
//                        return false
//                    }

                    if(subject.isDebug) testEvent.accept(Pair(EVENT_SHOW_DEBUGINFO, getDebugInfo()))    // send debug info

                    show(mTrial)
                    true
                }
                catch(e:Exception){
                    e.logLastTwo(LOG_TAG)
                    onCriticalError(e.toString())
                    false
                }
    }

    fun repeatTrial(){
        show(mTrial, true)
    }
    // ===============================================================================================================
    // TRIAL MANAGEMENT
    // ===============================================================================================================
    open fun nextTrial(prev_result: String = "", elapsed: Int = -1): Int {

        if (prev_result != ""){
            mTrial.setResponse(prev_result, elapsed)
            mSummary?.add(mTrial)
        }

        // if !last trial && !block end => doNextTrial
        return when {
            currTrial == (nTrials - 1) -> {
                saveText(mTrial.Log(), notifyDm = true)
                EVENT_TEST_END            // END !
            }
            mListBlocks.contains(currTrial) -> {
                saveText(mTrial.Log(), notifyDm = false)
                EVENT_BLOCK_END
            }
            else -> {
                saveText(mTrial.Log(), notifyDm = false)
                doNextTrial()
            }
        }
    }

    // called by above nextTrial & by TestFragment after user decided to continue after block end
    protected fun doNextTrial():Int{
        return  try {
                    mTrial = getNewTrial()  // it also updates currTrial

                    if(subject.isDebug) testEvent.accept(Pair(EVENT_SHOW_DEBUGINFO, getDebugInfo()))    // send debug info

                    show(mTrial)
                    currTrial
                }
                catch(e:Exception){
                    e.logLastTwo(LOG_TAG)
                    onCriticalError(e.toString())
                    EVENT_TEST_ERROR
                }
    }

    // called by TestFragment::onBlockEnded()
    fun startNewBlock(){
        mCurrBlock++
        doNextTrial()
    }

    // in the present basic form it does not do anything special.
    // can be overridden to implement custom online trials' values manipulation (e.g. in quest-based tasks)
    open fun getNewTrial(): TrialBasic {
        currTrial++
        return mTrials[currTrial]
    }

    fun abortTest(deleteOrShow:Boolean, dir:String= Environment.DIRECTORY_DOWNLOADS){

        unloadStimuli()

        if(deleteOrShow){
                deleteFile(mResultFile)
                deleteFile(subject.subjectFileName)
        }
        else    notifyFile(mResultFile, ctx, dir)
    }

    fun stopTestAfterBlock(dir:String= Environment.DIRECTORY_DOWNLOADS):Triple<String,String,String>{

        unloadStimuli()

        val newresname = subject.composeResultFileName(ctx, mCurrBlock)
        renameFile(mResultFile, newresname)

        val newsubjname = subject.composeSubjectFileName(ctx, mCurrBlock)
        renameFile(subject.subjectFileName, newsubjname)

        val newsummaryname = subject.composeSummaryFileName(ctx, mCurrBlock)

        notifyFile(newresname, ctx, dir)

        return Triple(newresname, newsubjname, newsummaryname)
    }

    // -> abortTest & send(event_test_error)
    private fun onCriticalError(msg:String, delete:Boolean=false){
        abortTest(delete)
        testEvent.accept(Pair(EVENT_TEST_ERROR, msg))
    }

    // ===============================================================================================================
    // ACCESSORY
    // ===============================================================================================================

    fun unloadStimuli(){
        mStimuliManager.unloadStimuli()
        mStimuliHandler.removeCallbacksAndMessages(null)
        mNoise?.stop()
    }

    protected fun saveText(text: String, overwrite: Boolean = false, notifyDm: Boolean = false):Any?{
        return  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        saveTextQ(ctx, mResultUri!!, text, overwrite = overwrite, notifyDm = notifyDm)
                else
                        saveText(ctx, mResultFile, text, overwrite = overwrite, notifyDm = notifyDm)
    }

    fun adjustBlocks(blk:Int){

        if((nBlocks == 1 && blk > 0) || (blk >= nBlocks)){
            // incongruent condition
            showAlert(activity, ctx.resources.getString(R.string.error), "")
            return
        }
        if(blk == -1){
            currTrial   = 0
            mCurrBlock  = 0
        }
        else {  // if it found lab_type_blk2.txt => blk=3)
            mCurrBlock = blk

            // following trial of the previous block
            currTrial = mListBlocks[mCurrBlock-1] + 1
        }
        mTrial      = mTrials[currTrial]
    }

    // is always created without block information, which is added when interrupting after a block
    protected fun createResultFile(subj:SubjectBasicParcel, header:String){
        mResultFile = subj.composeResultFileName(ctx)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mResultUri = saveTextQ(ctx, mResultFile, header)
        else
            saveText(ctx, mResultFile, header)
    }

    fun getAbsoluteResultFilePath(): String = getAbsoluteFilePath(mResultFile).second      // is "" if file was not present

    // set trial id according to its order in the list
    protected fun setTrialsID(){  mTrials.mapIndexed { index, trialBasic -> trialBasic.id = index } }

    protected fun getDebugInfo():String = mTrial.debugInfo()

    fun getTrialCorrectAnswer():String{
        return  if(!this::mTrial.isInitialized)     validAnswers[0]
        else                                mTrial.getCorrectAnswer()
    }

    protected fun getTestTitle():String = "${ctx.resources.getString(R.string.app_name)} - ${ctx.resources.getString(R.string.lab_test_res)}: $mTestLabel"
}