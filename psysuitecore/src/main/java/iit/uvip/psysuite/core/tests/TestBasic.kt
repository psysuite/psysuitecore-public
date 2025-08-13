package iit.uvip.psysuite.core.tests

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.PublishRelay

import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.trials.TrialsManager

import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.logLastTwo
import org.albaspazio.core.filesystem.*
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showAlert


/*
must contain all the possible codes

TestFragment (instantiate correct TestBasic derived class and call its -> test.initTest
 */

abstract class TestBasic(protected val ctx: Context,
                         protected val activity: Activity,
                         protected val hostfragment: Fragment,
                         protected val subject: SubjectBasicParcel,
                         protected val vibrator: VibrationManager? = null,
                         protected val mImageView: ImageView? = null,
                         protected val speechManager: SpeechManager? = null,
                         protected val outResultsDir:String= Environment.DIRECTORY_DOWNLOADS
                         ) {
    open var LOG_TAG:String = TestBasic::class.java.simpleName

    companion object {

        @JvmStatic val TESTINFO_BUNDLE_LABEL            = "test"    // used as subject-test bundle element label
        @JvmStatic val SUBJFILE_EXTENSION: String       = ".json"
        @JvmStatic val RES_EXTENSION: String            = ".txt"
        @JvmStatic val TEST_BUNDLE_RESULT_LABEL: String = "result"

        @JvmStatic val audioResources:HashMap<Long, String> = hashMapOf(
            7L      to "t1000hz_7ms.wav",
            10L     to "t1000hz_10ms.wav",
            17L     to "t1000hz_17ms.wav",
            20L     to "t1000hz_20ms.wav",
            30L     to "t1000hz_30ms.wav",
            35L     to "t1000hz_35ms.wav",
            50L     to "t1000hz_50ms.wav",
            100L    to "t1000hz_100ms.wav",
            1000L   to "t1000hz_1000ms.wav",
        )

        //-----------------------------------------------------------------------------------------
        // region ---- TESTS UNIQUE CODES ---
        //-----------------------------------------------------------------------------------------

        @JvmStatic val TEST_RT_AUDIO                = 1
        @JvmStatic val TEST_RT_TACTILE              = 2
        @JvmStatic val TEST_RT_VISUAL               = 3

        @JvmStatic val TEST_BISECTION_AUDIO                 = 100
        @JvmStatic val TEST_BISECTION_TACTILE               = 101
        @JvmStatic val TEST_BISECTION_AUDIO_TACTILE         = 102
        @JvmStatic val TEST_BISECTION_AUDIO_VISUAL          = 103
        @JvmStatic val TEST_BISECTION_VISUAL                = 104
        @JvmStatic val TEST_BISECTION_VISUAL_TACTILE        = 105
        @JvmStatic val TEST_BISECTION_AUDIO_SUPRA           = 106
        @JvmStatic val TEST_BISECTION_TACTILE_SUPRA         = 107
        @JvmStatic val TEST_BISECTION_AUDIO_TACTILE_SUPRA   = 108
        @JvmStatic val TEST_BISECTION_AUDIO_VISUAL_SUPRA    = 109
        @JvmStatic val TEST_BISECTION_VISUAL_SUPRA          = 110
        @JvmStatic val TEST_BISECTION_VISUAL_TACTILE_SUPRA  = 111

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

        @JvmStatic val TEST_MOTPRE_VH               = 220
        @JvmStatic val TEST_MOTPRE_VV               = 221
        @JvmStatic val TEST_MOTPRE_VHV              = 222
        @JvmStatic val TEST_MOTPRE_VV_CUE_ARROW     = 223
        @JvmStatic val TEST_MOTPRE_VH_CUE_ARROW     = 224
        @JvmStatic val TEST_MOTPRE_VV_CUE_WEIGHT    = 225
        @JvmStatic val TEST_MOTPRE_VH_FIXSPEED      = 226
        @JvmStatic val TEST_MOTPRE_VH_VARSPEED_FIXVT= 227
        @JvmStatic val TEST_MOTPRE_VH_VARSPEED_FIXVPL= 228

        @JvmStatic val TEST_TSP_A_SUB               = 230
        @JvmStatic val TEST_TSP_V_SUB               = 231
        @JvmStatic val TEST_TSP_T_SUB               = 232
        @JvmStatic val TEST_TSP_A_SUPRA             = 233
        @JvmStatic val TEST_TSP_V_SUPRA             = 234
        @JvmStatic val TEST_TSP_T_SUPRA             = 235

        @JvmStatic val TEST_TIR_A_SUB               = 240
        @JvmStatic val TEST_TIR_V_SUB               = 241
        @JvmStatic val TEST_TIR_T_SUB               = 242
        @JvmStatic val TEST_TIR_A_SUPRA             = 243
        @JvmStatic val TEST_TIR_V_SUPRA             = 244
        @JvmStatic val TEST_TIR_T_SUPRA             = 245

        @JvmStatic val TEST_MUSICAL_METERS          = 250

        // endregion
        //-----------------------------------------------------------------------------------------

        // --------------------------------------------------------------------------------------------
        // region ---- USER CONTROLLED trial-by-trial BEHAVIOUR ----
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

        @JvmStatic val TEST_SWITCH_DISABLED             = 0         //  disabled, cannot enable it
        @JvmStatic val TEST_SWITCH_CHOOSE_OFF           = 1         //  can choose, disabled by default
        @JvmStatic val TEST_SWITCH_CHOOSE_ON            = 2         //  can choose, enabled by default
        @JvmStatic val TEST_SWITCH_ENABLED              = 3         //  enabled, cannot disable it

        @JvmStatic val TEST_TRMAN_FIXED                 = 0         //  trials are predetermined at test start
        @JvmStatic val TEST_TRMAN_CHOOSE_FIXED          = 1         //  can choose, predetermined by default
        @JvmStatic val TEST_TRMAN_CHOOSE_MIXED          = 2         //  can choose, mixed by default
        @JvmStatic val TEST_TRMAN_CHOOSE_ADAPTIVE       = 3         //  can choose, Quest by default
        @JvmStatic val TEST_TRMAN_MIXED                 = 4         //  some trials are calculated trial-by-trial according to a Quest algorithm, other are predetermined
        @JvmStatic val TEST_TRMAN_ADAPTIVE              = 5         //  trials are calculated trial-by-trial according to a Quest algorithm
        // endregion
        //-----------------------------------------------------------------------------------------

        //-----------------------------------------------------------------------------------------
        // region ---- peri-trials EVENTS ----
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
        @JvmStatic val EVENT_NAVIGATE_BACK              = 214   // event to leave TestFragment and go back to TestsMenu fragment
        @JvmStatic val EVENT_TRIAL_STARTED              = 215   // after a trial end, event signaling that the next trial is started
        // endregion
        //-----------------------------------------------------------------------------------------

        // --------------------------------------------------------------------------------------------
        // region ---- end of trial management ----
        //-----------------------------------------------------------------------------------------
        @JvmStatic val TEST_ABORTED_KEEP_RESULT         = 1000
        @JvmStatic val TEST_ABORTED_DEL_RESULT          = 1001
        @JvmStatic val TEST_COMPLETED                   = 1002  // terminate all test
        @JvmStatic val BLOCK_COMPLETED                  = 1003
        @JvmStatic val TEST_ABORTED_WITH_ERROR          = 1004

        // endregion
        //-----------------------------------------------------------------------------------------

        //-----------------------------------------------------------------------------------------
        // region ---- TEST COMMON CONSTANTS ---

        // Email configuration
        @JvmStatic val DEFAULT_EMAIL_RECIPIENTS = arrayOf("psysuite.uvip@gmail.com")

        // Common log header
        @JvmStatic val LOG_HEADER = "id\ttype\terror\tsuccess\telapsed\n"
        @JvmStatic val TEST_BASIC_LABEL = "to-be-overridden"

//        @JvmStatic val NUM_TRIALS = 18


        // Common stimulus types
        @JvmStatic val STIMULUS_TYPE_VISUAL             = "VISUAL"
        @JvmStatic val STIMULUS_TYPE_AUDIO              = "AUDIO"
        @JvmStatic val STIMULUS_TYPE_TACTILE            = "TACTILE"
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE      = "AUDIO_TACTILE"
        @JvmStatic val STIMULUS_TYPE_AUDIO_VISUAL       = "AUDIO_VIDEO"
        @JvmStatic val STIMULUS_TYPE_VISUAL_TACTILE     = "VIDEO_TACTILE"

        @JvmStatic val STIMULUS_TYPE_AUDIO_LOG          = "A"
        @JvmStatic val STIMULUS_TYPE_TACTILE_LOG        = "T"
        @JvmStatic val STIMULUS_TYPE_VISUAL_LOG         = "V"
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE_LOG  = "AT"
        @JvmStatic val STIMULUS_TYPE_TACTILE_AUDIO_LOG  = "TA"
        @JvmStatic val STIMULUS_TYPE_AUDIO_VISUAL_LOG   = "AV"
        @JvmStatic val STIMULUS_TYPE_VISUAL_AUDIO_LOG   = "VA"
        @JvmStatic val STIMULUS_TYPE_VISUAL_TACTILE_LOG = "VT"
        @JvmStatic val STIMULUS_TYPE_TACTILE_VISUAL_LOG = "TV"


        // Common condition types
        @JvmStatic val STIMULUS_ISI_SUB        = "SUB"
        @JvmStatic val STIMULUS_ISI_SUPRA      = "SUPRA"

        @JvmStatic val CONFLICT_TYPE_NONE   = "none"

        // Common durations
        @JvmStatic val STIMULUS_DURATION_VISUAL     = 50L
        @JvmStatic val STIMULUS_DURATION_TACTILE    = 50L
        @JvmStatic val STIMULUS_DURATION_AUDIO      = 50L

        @JvmStatic val DEFAULT_ISI          = 1000L
        @JvmStatic val QUESTION_DELAY       = 500L      // latency
        @JvmStatic val FIRST_STIMULUS_DELAY = 500L      // latency

        // Common trial configurations
        @JvmStatic val DEFAULT_WHITE_NOISE_DURATION             = 1000L
        @JvmStatic val DEFAULT_WHITE_NOISE_FIRST_STIM_INTERVAL  = 1000L
        // endregion
        //-----------------------------------------------------------------------------------------
    }

    // ===============================================================================================================
    // PUBLIC
    // ===============================================================================================================
    val nTrials:Int     get() = mTrialsManager.nTrials
    val currTrial:Int   get() = mTrialsManager.currTrial

    val testEvent:PublishRelay<Triple<Int,Any?,List<String>>> = PublishRelay.create()
    var showTrialsID:Int        = 0     // define when display trial id(0: never, 1: only @ trial end, 2: always)
    var abortMode:Int           = 0     // define abort modality (0:in answer dialog @ trial end, 1:button @ trial end, 2:always)


    var mTestLabel: String                      = ""
    var mQuestion:String                        = ""
    var validAnswers: MutableList<String>       = mutableListOf()

    // Stimulus type constants that can be overridden by subclasses
    protected open val STIM_A: Int      = StimuliManager.STIM_TYPE_A4
    protected open val STIM_V: Int      = StimuliManager.STIM_TYPE_V1
    protected open val STIM_T: Int      = StimuliManager.STIM_TYPE_T1
    protected open val STIM_ATV: Int    = STIM_A or STIM_T or STIM_V
    protected open val STIM_TV:Int      = STIM_T or STIM_V
    protected open val STIM_AV:Int      = STIM_A or STIM_V
    protected open val STIM_AT:Int      = STIM_A or STIM_T
    // ---------------------------------------------------------------
    abstract fun initTest()

    // ===============================================================================================================
    // PUBLIC
    // ===============================================================================================================
    fun start():Boolean{
        return  try {
            if(!mStimuliManager.isValid || !this::mTrialsManager.isInitialized){
                onCriticalError(ctx.resources.getString(R.string.test_failure), true)
                return false
            }

            if(subject.isDebug) testEvent.accept(Triple(EVENT_SHOW_DEBUGINFO, getDebugInfo(), listOf()))    // send debug info

            show(mTrialsManager.mTrial)
            true
        }
        catch(e:Exception){
            e.logLastTwo(LOG_TAG)
            onCriticalError(e.toString())
            false
        }
    }

    // writes summary and return absolute filepath or empty string
    fun closeSummary(filename:String = ""):String{
        return  if(filename.isEmpty())  mSummary?.close(mSummaryFile) ?: ""
        else                            mSummary?.close(filename) ?: ""
    }

    fun repeatTrial(){
        show(mTrial, true)
    }

    open fun onAnswerGiven(result: Int = -1, elapsed: Long = -1, extra_text: String = ""){
        if (result != -1 || extra_text.isNotEmpty()){
            mTrialsManager.setResponse(result, elapsed, extra_text)
            mSummary?.add(mTrial)
        }
    }
    // called by:   - TestFragment:: onAnswerGiven, showShortAbort, btNext ,btAbort (no abort response), btPause
    // prev trial has ended, save it
    // and check whether entire task/block has ended or call next trial
    open fun onNextTrial() {

        saveText(mTrial.Log())
        when {
            currTrial == (nTrials - 1) -> {
                terminateTest(TEST_COMPLETED)
                testEvent.accept(Triple(EVENT_TEST_END, null, listOf()))            // END !
            }
            mListBlocks.contains(currTrial) -> {
                testEvent.accept(Triple(EVENT_BLOCK_END, null, listOf()))
            }
            else -> {
                testEvent.accept(Triple(EVENT_TRIAL_STARTED, null, listOf()))
                doNextTrial()
            }
        }
    }

    fun terminateTest(code:Int){

        var filesToReturn = listOf( getAbsoluteResultFilePath(),
                                    subject.getAbsoluteSubjectFilePath(),
                                    closeSummary())
        unloadStimuli()
        when(code){

            TEST_COMPLETED -> {
                closeSummary()
                notifyFile(mResultFile, ctx, outResultsDir)
            }
            BLOCK_COMPLETED -> {
                val renamedfiles = stopTestAfterBlock()        // change output files names and notify them
                filesToReturn = listOf(renamedfiles.first, renamedfiles.second, renamedfiles.third)
                notifyFile(renamedfiles.first, ctx, outResultsDir)

            }
            TEST_ABORTED_KEEP_RESULT -> {
                closeSummary()
                notifyFile(mResultFile, ctx, outResultsDir)
            }
            TEST_ABORTED_DEL_RESULT -> {
                deleteFile(mResultFile)
                deleteFile(subject.subjectFileName)
                deleteFile(mSummaryFile)
                filesToReturn = listOf()
            }
        }
        testEvent.accept(Triple(EVENT_NAVIGATE_BACK, code, filesToReturn))
    }

    // called by TestFragment::onBlockEnded()
    fun startNewBlock(){
        mCurrBlock++
        doNextTrial()
    }

    //called by TestFragment::onStoppedAfterBlock()
    fun stopTestAfterBlock():Triple<String,String,String>{

        val newresname = subject.composeResultFileName(ctx, mCurrBlock)
        renameFile(mResultFile, newresname)

        val newsubjname = subject.composeSubjectFileName(ctx, mCurrBlock)
        renameFile(subject.subjectFileName, newsubjname)

        val newsummaryname = subject.composeSummaryFileName(ctx, mCurrBlock)

        return Triple(newresname, newsubjname, newsummaryname)
    }

    //called by TestFragment::onTestEnded()
    fun unloadStimuli(){
        mStimuliManager.unloadStimuli()
        mStimuliHandler.removeCallbacksAndMessages(null)
        mNoise?.stop()
    }

    //called by TestFragment::onTestSetupComplete()
    fun adjustBlocks(blk:Int){

        if((this@TestBasic.nBlocks == 1 && blk > 0) || (blk >= this@TestBasic.nBlocks)){
            // incongruent condition
            showAlert(activity, ctx.resources.getString(R.string.error), "")
            return
        }
        if(blk == -1){
            mTrialsManager.currTrial   = 0
            mCurrBlock  = 0
        }
        else {  // if it found lab_type_blk2.txt => blk=3)
            mCurrBlock = blk

            // following trial of the previous block
            mTrialsManager.currTrial = mListBlocks[mCurrBlock-1] + 1
        }
    }

    //called by TestFragment::onAbortTest()/onTestEnded()/onTestError()
    fun getAbsoluteResultFilePath(): String = getAbsoluteFilePath(mResultFile).second      // is "" if file was not present

    //called by TestFragment::showAnswerDialog
    open fun getTrialCorrectAnswer():Int{
        return  if(!this::mTrialsManager.isInitialized) 0
                else                                    mTrial.correct_answer
    }

    // ===============================================================================================================
    // PROTECTED/PRIVATE
    // ===============================================================================================================

    // proxy for methods to be implemented in each subclass
    protected abstract fun show(trial: TrialBasic, isRepeat:Boolean=false)
    protected abstract fun onTrialEnd()
    protected abstract fun initSummary()                // init summary content (mSummary),

    protected var nextTrailModality:Int         = 0     // define how trials are displayed. 0: automatically, 1: after a next button, 2: after answer
    protected val delaysAligner: DelaysAligner  = subject.stimuliDelays

    // they are just proxy for properties (implemented / edited / accessed) in each subclass
    protected lateinit var mTrialsManager: TrialsManager

    protected val mTrial: TrialBasic
        get() = mTrialsManager.mTrial

    // BLOCKS -------------------------------------------------------------------
    protected var mListBlocks:MutableList<Int>  = mutableListOf()
        set(value) {
            field   = value
            this@TestBasic.nBlocks = value.size + 1
        }

    protected var mNoise: MediaPlayer? = null
    protected open var mDrawablesResource:MutableList<Int>  = mutableListOf()   // list of drawables' resources id to be edited in subclasses

    private var nBlocks:Int     = 0
    private var mCurrBlock: Int = 0
    private var mResultFile: String                         = subject.composeResultFileName(ctx)
    private var mSummaryFile: String                        = subject.composeSummaryFileName(ctx)

    private var mResultUri: Uri?                            = null

    // SUMMARY -------------------------------------------------------------------
    protected var mSummary: Summary?                        = null
//    fun closeSummary(blk:Int = -1):String = mSummary?.close(subject.composeSummaryFileName(ctx, blk)) ?: "" // writes summary and return filename or empty string

    // STIMULI -------------------------------------------------------------------

    // this instance is defined and validated during sub-class init{}
    // in case of error an exception is thrown and test is aborted.
    // the only susceptible to error is AudioManager in case of the test involves different resources to be loaded
    protected lateinit var mStimuliManager: StimuliManager
    protected var mStimuliHandler: Handler = Handler(Looper.getMainLooper())         // IDE suggested: Handler(Looper.myLooper()!!)

    protected var currStimulusLabel:String      = ""
    protected var currStimulusDuration:Long     = 100L          // default value to be used when stimulus duration in not given
    protected var currAudioResourceName:String  = "t200hz_2s"   // default amplitude to be used when  not given

    protected var ITI:Long                      = 0             // default ITI

    // called by above nextTrial & by TestFragment after user decided to continue after block end
    // set/calculate new trial and shows it
    protected fun doNextTrial():Int{
        return  try {
                    mTrialsManager.getNewTrial() as TrialBasic

                    if(subject.isDebug) testEvent.accept(Triple(EVENT_SHOW_DEBUGINFO, getDebugInfo(), listOf()))    // send debug info

                    show(mTrial)
                    currTrial
                }
                catch(e:Exception){
                    e.logLastTwo(LOG_TAG)
                    onCriticalError(e.toString())
                    EVENT_TEST_ERROR
                }
    }

    // -> abortTest & send(event_test_error)
    private fun onCriticalError(msg:String, delete:Boolean=false){
        if(delete)  terminateTest(TEST_ABORTED_DEL_RESULT)
        else        terminateTest(TEST_ABORTED_KEEP_RESULT)
        testEvent.accept(Triple(EVENT_TEST_ERROR, msg, listOf(  getAbsoluteResultFilePath(),
                                                                subject.getAbsoluteSubjectFilePath(),
                                                                closeSummary())))
    }
    // ===============================================================================================================
    // ACCESSORY
    // ===============================================================================================================
    protected fun saveText(text: String, overwrite: Boolean = false, notifyDm: Boolean = false): Any {
        return  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        saveTextQ(ctx, mResultUri!!, text, overwrite = overwrite, notifyDm = notifyDm)
                else
                        saveText(ctx, mResultFile, text, overwrite = overwrite, notifyDm = notifyDm)
    }

    // is always created without block information, which is added when interrupting after a block
    protected fun createResultFile(header:String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mResultUri = saveTextQ(ctx, mResultFile, header)
        else
            saveText(ctx, mResultFile, header)
    }

    private fun getDebugInfo():String = mTrial.debugInfo()

}