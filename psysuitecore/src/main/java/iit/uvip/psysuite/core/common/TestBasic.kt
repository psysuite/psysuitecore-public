package iit.uvip.psysuite.core.common

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Environment
import android.os.Handler
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.stimuli.*
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import org.albaspazio.core.accessory.*
import org.albaspazio.core.ui.showAlert
import java.util.Collections.max


/*
must contain all the possible codes

 */

abstract class TestBasic(protected val ctx: Context,
                         protected val activity: Activity,
                         protected val hostfragment: Fragment,
                         protected val subjectparcel: SubjectBasicParcel,
                         protected val vibrator: VibrationManager? = null,
                         protected val mImageView: ImageView? = null
) {
    open var LOG_TAG:String = TestBasic::class.java.simpleName

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

        @JvmStatic val TEST_ABORT_ANSWER                = 0         //  never show abort button (it is displayed in the answer dialog)
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

        @JvmStatic val TEST_ATB_TIME_SINGLESTIM     = 130
        @JvmStatic val TEST_ATB_TIME_DOUBLESTIM     = 131
        @JvmStatic val TEST_ATB_TIME_INF            = 132
        @JvmStatic val TEST_ATB_TIME_SINGLESTIM_TOD = 133
        @JvmStatic val TEST_ATB_TIME_DOUBLESTIM_TOD = 134

        @JvmStatic val TEST_ATVB_TIME_S_UNBAL       = 140
        @JvmStatic val TEST_ATVB_TIME_D_UNBAL       = 141
        @JvmStatic val TEST_ATVB_TIME_D_BAL         = 142
        @JvmStatic val TEST_ATVB_TIME_S_BAL         = 143

        @JvmStatic val TEST_SAMPLE_ALIGNED          = 150
        @JvmStatic val TEST_SAMPLE_SHIFTED          = 151
        @JvmStatic val TEST_SAMPLE_PAIR             = 152

        @JvmStatic val TEST_TFI                     = 160
        @JvmStatic val TEST_TFI_TODDLERS            = 161

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

        //-----------------------------------------------------------------------------------------
        // STIMULUS TYPES UNIQUE CODES
        //-----------------------------------------------------------------------------------------
        // A1: tone, A2:resource, V1: view made visible/invisible, V2: imageview with different color frame (one as background), T1: single, T2:sequence
        @JvmStatic val STIM_TYPE_A1                 = 1     //  000 000 001
        @JvmStatic val STIM_TYPE_A2                 = 2     //  000 000 010
        @JvmStatic val STIM_TYPE_A3                 = 4     //  000 000 100
        @JvmStatic val STIM_TYPE_T1                 = 8     //  000 001 000
        @JvmStatic val STIM_TYPE_T2                 = 16    //  000 010 000
        @JvmStatic val STIM_TYPE_V1                 = 64    //  001 000 000
        @JvmStatic val STIM_TYPE_V2                 = 128   //  010 000 000

        @JvmStatic val STIM_TYPE_A1T1               = 9     //  000 001 001
        @JvmStatic val STIM_TYPE_A1T2               = 17    //  000 010 001
        @JvmStatic val STIM_TYPE_A1V1               = 65    //  001 000 001
        @JvmStatic val STIM_TYPE_A1V2               = 129   //  010 000 001
        @JvmStatic val STIM_TYPE_A2T1               = 10    //  000 001 010
        @JvmStatic val STIM_TYPE_A2T2               = 18    //  000 010 010
        @JvmStatic val STIM_TYPE_A2V1               = 66    //  001 000 010
        @JvmStatic val STIM_TYPE_A2V2               = 131   //  010 000 011
        @JvmStatic val STIM_TYPE_A3T1               = 12    //  000 001 100
        @JvmStatic val STIM_TYPE_A3T2               = 20    //  000 010 100
        @JvmStatic val STIM_TYPE_A3V1               = 68    //  001 000 100
        @JvmStatic val STIM_TYPE_A3V2               = 131   //  010 000 100
        @JvmStatic val STIM_TYPE_T1V1               = 72    //  001 001 000
        @JvmStatic val STIM_TYPE_T2V1               = 80    //  001 010 000
        @JvmStatic val STIM_TYPE_T1V2               = 136   //  010 001 000
        @JvmStatic val STIM_TYPE_T2V2               = 144   //  010 010 000

        @JvmStatic val STIM_TYPE_A1T1V1             = 73    //  001 001 001
        @JvmStatic val STIM_TYPE_A1T2V1             = 81    //  001 010 001
        @JvmStatic val STIM_TYPE_A1T1V2             = 137   //  010 001 001
        @JvmStatic val STIM_TYPE_A1T2V2             = 145   //  010 010 001
        @JvmStatic val STIM_TYPE_A2T1V1             = 74    //  001 001 010
        @JvmStatic val STIM_TYPE_A2T2V1             = 82    //  001 010 010
        @JvmStatic val STIM_TYPE_A2T1V2             = 138   //  010 001 010
        @JvmStatic val STIM_TYPE_A2T2V2             = 146   //  010 010 010
        @JvmStatic val STIM_TYPE_A3T1V1             = 76    //  001 001 100
        @JvmStatic val STIM_TYPE_A3T2V1             = 84    //  001 010 100
        @JvmStatic val STIM_TYPE_A3T1V2             = 140   //  010 001 100
        @JvmStatic val STIM_TYPE_A3T2V2             = 148   //  010 010 100
        //-----------------------------------------------------------------------------------------

        @JvmStatic val TEST_ABORT                       = 230
        @JvmStatic val TEST_COMPLETED                   = 231
        @JvmStatic val BLOCK_COMPLETED                  = 232
        @JvmStatic val TEST_ABORTED_WITH_ERROR          = 233

        //-----------------------------------------------------------------------------------------
        // POPULATIONS
        //-----------------------------------------------------------------------------------------
        @JvmStatic val POPULATION_TD                    = 0

        @JvmStatic val POPULATION_CB                    = 10
        @JvmStatic val POPULATION_LB                    = 11
        @JvmStatic val POPULATION_CLV                   = 12
        @JvmStatic val POPULATION_LLV                   = 13

        @JvmStatic val POPULATION_CD                    = 20
        @JvmStatic val POPULATION_LD                    = 21
        @JvmStatic val POPULATION_CAI                   = 22
        @JvmStatic val POPULATION_LAI                   = 23

        @JvmStatic val POPULATION_ADHD                  = 30

        @JvmStatic val populations:List<SpinnerData> = listOf(
            SpinnerData("TD",   POPULATION_TD),
            SpinnerData("CB",   POPULATION_CB),
            SpinnerData("LB",   POPULATION_LB),
            SpinnerData("CLV",  POPULATION_CLV),
            SpinnerData("LLV",  POPULATION_LLV),
            SpinnerData("CD",   POPULATION_CD),
            SpinnerData("LD",   POPULATION_LD),
            SpinnerData("CAI",  POPULATION_CAI),
            SpinnerData("LAI",  POPULATION_LAI),
            SpinnerData("ADHD", POPULATION_ADHD))

        //-----------------------------------------------------------------------------------------

        fun maintype2unimodaltypes(source:Int):List<Int>{

            val a = when {
                source and STIM_TYPE_A1 > 0 -> STIM_TYPE_A1
                source and STIM_TYPE_A2 > 0 -> STIM_TYPE_A2
                source and STIM_TYPE_A3 > 0 -> STIM_TYPE_A3
                else -> -1
            }

            val t = when {
                source and STIM_TYPE_T1 > 0 -> STIM_TYPE_T1
                source and STIM_TYPE_T2 > 0 -> STIM_TYPE_T2
                else -> -1
            }

            val v = when {
                source and STIM_TYPE_V1 > 0 -> STIM_TYPE_V1
                source and STIM_TYPE_V2 > 0 -> STIM_TYPE_V2
                else -> -1
            }
            return listOf(a,t,v)
        }
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
    val showResult:Boolean                      = this.subjectparcel.showResult
    val delaysAligner:DelaysAligner             = this.subjectparcel.stimuliDelays


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
    // the only susceptible to error is MediaPlayer in case of the test involves different resources to be loaded
    protected lateinit var mStimuliManager: StimuliManager

    protected var mNoise: MediaPlayer? = null

    protected open var mDrawablesResource:MutableList<Int>  = mutableListOf()   // list of drawables' resources id to be edited in subclasses
    protected var mStimuliHandler: Handler                  = Handler()

    protected var mSummary:Summary?                         = null
    private var mResultFile: String                         = ""
    private var mCurrBlock: Int                             = 0

    protected var currStimulusDuration:Long     = 100L          // default value to be used when stimulus duration in not given
    protected var currVibrationAmplitude:Int    = -1            // default amplitude to be used when  not given
    protected var currVolume:Float              = 1F            // default audio volume to be used when  not given
    protected var currAudioResourceName:String  = "t200hz_2s"   // default amplitude to be used when  not given
    protected var ITI:Long                      = 0             // default ITI

    // proxy for methods to be implemented in each subclass
    abstract fun initTest()
    abstract fun onTrialEnd()
    abstract fun show(trial:TrialBasic, isRepeat:Boolean=false)

    abstract fun initSummary()              // init summary content (mSummary),
    fun closeSummary():String = mSummary?.close(subjectparcel.composeSummaryFileName(ctx)) ?: ""

    // ===============================================================================================================
    fun start():Boolean{

        return  try {
                    adjustBlocks(subjectparcel.block)     // set currTrial, mCurrBlock, mTrial

                    if(!mStimuliManager.isValid || !this::mTrial.isInitialized){
                        onCriticalError(ctx.resources.getString(R.string.test_failure), true)
                        return false
                    }

                    if(subjectparcel.isDebug) testEvent.accept(Pair(EVENT_SHOW_DEBUGINFO, getDebugInfo()))    // send debug info

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
                saveText(ctx, mResultFile, mTrial.Log(), overwrite = false, notifyDm = true)
                EVENT_TEST_END            // END !
            }
            mListBlocks.contains(currTrial) -> {
                saveText(ctx, mResultFile, mTrial.Log(), overwrite = false, notifyDm = false)
                EVENT_BLOCK_END
            }
            else -> {
                saveText(ctx, mResultFile, mTrial.Log(), overwrite = false, notifyDm = false)
                doNextTrial()
            }
        }
    }

    // called by above nextTrial & by TestFragment after user decided to continue after block end
    private fun doNextTrial():Int{
        return  try {
                    mTrial = getNewTrial()  // it also updates currTrial

                    if(subjectparcel.isDebug) testEvent.accept(Pair(EVENT_SHOW_DEBUGINFO, getDebugInfo()))    // send debug info

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
    open fun getNewTrial():TrialBasic{
        currTrial++
        return mTrials[currTrial]
    }

    fun abortTest(deleteOrShow:Boolean, dir:String= Environment.DIRECTORY_DOWNLOADS){
        mStimuliHandler.removeCallbacksAndMessages(null)
        mNoise?.stop()

        if(deleteOrShow){
                deleteFile(mResultFile)
                deleteFile(subjectparcel.subjectFileName)
        }
        else    notifyFile(mResultFile, ctx, dir)
    }

    fun stopTestAfterBlock(dir:String= Environment.DIRECTORY_DOWNLOADS):String{

        mStimuliHandler.removeCallbacksAndMessages(null)

        val newresname = subjectparcel.composeResultFileName(ctx, mCurrBlock)
        renameFile(mResultFile, newresname)

        val newsubjname = subjectparcel.composeSubjectFileName(ctx, mCurrBlock)
        renameFile(subjectparcel.subjectFileName, newsubjname)

        notifyFile(newresname, ctx, dir)

        return newresname
    }

    fun getTrialCorrectAnswer():String = mTrial.getCorrectAnswer()

    private fun onCriticalError(msg:String, delete:Boolean=false){
        abortTest(delete)
        testEvent.accept(Pair(EVENT_TEST_ERROR, msg))
    }
    // ===============================================================================================================
    // ACCESSORY
    // ===============================================================================================================

    private fun adjustBlocks(blk:Int){      // blk is 0-based

        if((nBlocks == 1 && blk > 0) || (blk >= nBlocks)){
            // incongruent condition
            showAlert(activity, ctx.resources.getString(R.string.error), "")
            return
        }

        if(nBlocks == 1 || blk == -1){
            currTrial   = 0
            mCurrBlock  = 0
        }
        else  {
            // nBlocks > 1 :blocks subdivision is available
            mCurrBlock = blk

            // following trial of the previous block
            currTrial = mListBlocks[mCurrBlock-1] + 1
        }
        mTrial      = mTrials[currTrial]
    }

    protected fun getTestTitle():String = "${ctx.resources.getString(R.string.app_name)} - ${ctx.resources.getString(R.string.lab_test_res)}: $mTestLabel"

    protected fun createResultFile(subj:SubjectBasicParcel, header:String){
        mResultFile = subj.composeResultFileName(ctx, subj.block)
        saveText(ctx, mResultFile, header)
    }

    fun getResultFile(): String =   if(existFile(mResultFile).first)    mResultFile
                                    else                                ""

    fun getAbsoluteResultFilePath(): String = getAbsoluteFilePath(mResultFile).second      // is "" if file was not present

    // set trial id according to its order in the list
    protected fun setTrialsID(){  mTrials.mapIndexed { index, trialBasic -> trialBasic.id = index } }

    private fun getDebugInfo():String = mTrial.debugInfo()

    // =============================================================================================================================
    // STIMULUS DELIVERY
    // =============================================================================================================================
    // ---------------------------------------------------------------------------------------------
    // PAIRS  (deliverAlignedStimuliPair -> deliverShiftedStimuliPair -> 2 deliverShiftedStimulus)
    // ---------------------------------------------------------------------------------------------
    // deliver a pair of identical aligned stimuli, separated by "isi" ms.
    // calculate delay correction and call the following function
    protected fun deliverAlignedStimuliPair(isi:Long, type:Int, stimuliManager: StimuliManager? = null, onEnd:()-> Unit = {}) {
        val corr_delays = delaysAligner.arrangeDelays(type, 0, 0,0) // here type filters (can set to -1) corr_delays values
        deliverShiftedStimuliPair(isi, type, corr_delays.a, corr_delays.t, corr_delays.v, stimuliManager){ onEnd()}
    }

    private fun deliverShiftedStimuliPair(isi:Long, type:Int,
                                          a:Long, t:Long, v:Long,
                                          stimuliManager: StimuliManager? = null,
                                          onEnd:() -> Unit = {}){

        deliverShiftedStimulus(type, a, t, v, stimuliManager)
        mStimuliHandler.postDelayed({
            deliverShiftedStimulus(type, a, t, v, stimuliManager){ onEnd() }
        }, isi)
    }
    // ---------------------------------------------------------------------------------------------
    // SHIFTED STIMULUS (call 1-to-3 deliverUnimodalStimulus at different latencies, receive already corrected shifting)
    // the only method that call
    // ---------------------------------------------------------------------------------------------
    protected fun deliverShiftedStimulus(type:Int,
                                         a:Long, t:Long, v:Long,
                                         stimuliManager: StimuliManager? = null,
                                         onEnd:() -> Unit = {}) {

        val unimodal_types  = maintype2unimodaltypes(type)
        val atype           = unimodal_types[0]
        val ttype           = unimodal_types[1]
        val vtype           = unimodal_types[2]

        val durlist         = mutableListOf<Long>()

//        Log.d("TestBasic", "---------A=$a, T=$t, V=$v")
//        val onsetDate           = Date()

        try{
            val am: AudioManager?
            if(a > -1 && atype > -1) {
                am = mStimuliManager.getValidAudioManager(stimuliManager?.mAudioManager) ?: throw Exception("error in deliverShiftedStimulus: a valid audio manager was not found")
                if(am.type != atype)    throw Exception(ctx.resources.getString(R.string.error_audiomanager))

                durlist.add(am.duration + a)
                mStimuliHandler.postDelayed({
//                    val elapsedms = getTimeDifference(onsetDate)
//                    Log.d("TestBasic", "audio: type=${am.type}, onset=$a, elapsed=$elapsedms")
                    deliverUnimodalStimulus(am)
                }, a)
            }

            val tm: TactileManager?
            if(t > -1 && ttype > -1) {
                tm = mStimuliManager.getValidTactileManager(stimuliManager?.mTactileManager) ?: throw Exception("error in deliverShiftedStimulus: a valid tactile manager was not found")
                if(tm.type != ttype)    throw Exception(ctx.resources.getString(R.string.error_tactilemanager))

                durlist.add(tm.duration + t)
                mStimuliHandler.postDelayed({
//                    val elapsedms = getTimeDifference(onsetDate)
//                    Log.d("TestBasic", "tactile: type=${tm.type}, onset=$t, elapsed=$elapsedms")
                    deliverUnimodalStimulus(tm)
                }, t)
            }

            val vm: VisualManager?
            if(v > -1 && vtype > -1) {
                vm = mStimuliManager.getValidVisualManager(stimuliManager?.mVisualManager) ?: throw Exception("error in deliverShiftedStimulus: a valid visual manager was not found")
                if(vm.type != vtype)    throw Exception(ctx.resources.getString(R.string.error_visualmanager))

                durlist.add(vm.duration + v)
                mStimuliHandler.postDelayed({
//                    val elapsedms = getTimeDifference(onsetDate)
//                    Log.d("TestBasic", "visual: type=${vm.type}, onset=$v, elapsed=$elapsedms")
                    deliverUnimodalStimulus(vm)
                }, v)
            }

            val end:Long = max(durlist)
            mStimuliHandler.postDelayed({   onEnd() }, end)
        }
        catch (e:Exception){
            val msg = e.message ?: ctx.resources.getString(R.string.error_tactilemanager)
            onCriticalError(msg)
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ALIGNED STIMULUS (correct delays and call deliverShiftedStimulus)
    // ---------------------------------------------------------------------------------------------
    protected fun deliverAlignedStimulus(type:Int,
                                         stimuliManager: StimuliManager? = null,
                                         onEnd:()-> Unit = {}){

        val corr_delays = delaysAligner.arrangeDelays(type, 0,0,0)
        deliverShiftedStimulus(type, corr_delays.a, corr_delays.t, corr_delays.v, stimuliManager){onEnd()}
    }

    // --------------------------------------------------------------------------------------------------------------
    // UNIMODAL STIMULUS
    // here I give the final deliver command, latencies and delays corrections have been already defined
    // --------------------------------------------------------------------------------------------------------------
    protected fun deliverUnimodalStimulus(type:Int, manager: StimulusManager? = null, onEnd:() -> Unit = {}){
        when(type){
            STIM_TYPE_A1                -> deliverA1Stimulus(manager as AudioManager?   , onEnd)
            STIM_TYPE_A2                -> deliverA2Stimulus(manager as AudioManager?   , onEnd)
            STIM_TYPE_A3                -> deliverA3Stimulus(manager as AudioManager?   , onEnd)
            STIM_TYPE_T1,STIM_TYPE_T2   -> deliverTStimulus(manager as TactileManager? , onEnd)
            STIM_TYPE_V1,STIM_TYPE_V2   -> deliverVStimulus(manager as VisualManager?  , onEnd)
        }
    }

    private fun deliverUnimodalStimulus(manager: StimulusManager, onEnd:() -> Unit = {}){
        deliverUnimodalStimulus(manager.type, manager, onEnd)
    }

    protected fun deliverA1Stimulus(managerA: AudioManager? = null, onEnd:() -> Unit = {}){

        try {
            val am = mStimuliManager.getValidAudioManager(managerA) ?:  throw Exception("deliverA1Stimulus: mAudioManager is null")
            if (!am.isValid)                                          throw Exception("deliverA1Stimulus: mAudioManager is not valid")

            am.deliver()
            mStimuliHandler.postDelayed({ onEnd() }, am.duration)
        }
        catch (e:Exception){
            val msg = e.message ?: ctx.resources.getString(R.string.error_audiomanager)
            onCriticalError(msg)
        }
    }

     private fun deliverA2Stimulus(managerA: AudioManager? = null, onEnd:() -> Unit = {}){

        try {
            if(mStimuliManager.getValidAudioManager(managerA) == null) throw Exception("deliverA1Stimulus: mAudioManager and given audio manager are both null")
            // one of the two is not null

            var duration:Long = mStimuliManager.audioDuration
            val audio = if(managerA != null) {
                            duration = managerA.duration
                            when {
                                managerA.isLoaded(managerA.resource as String) -> managerA
                                (managerA.resource as String).isNotEmpty()       -> {
                                    managerA.loadResource(managerA.resource as String)
                                    managerA
                                }
                                else        -> throw Exception("deliverA2Stimulus: mediaplayer audio resource is empty")
                            }
                        } else mStimuliManager.mAudioManager

            audio!!.deliver()
            mStimuliHandler.postDelayed({ onEnd() }, duration)
        }
        catch (e:Exception){
            val msg = e.message ?: ctx.resources.getString(R.string.error_audiomanager)
            onCriticalError(msg)
        }
    }

     private fun deliverA3Stimulus(managerA: AudioManager? = null, onEnd:() -> Unit = {}){

         try {
             val am = mStimuliManager.getValidAudioManager(managerA) ?:  throw Exception("deliverA3Stimulus: mAudioManager is null")
             if (!am.isValid)                                          throw Exception("deliverA3Stimulus: mAudioManager is not valid")

             am.deliver()
             mStimuliHandler.postDelayed({ onEnd() }, am.duration)
         }
         catch (e:Exception){
             val msg = e.message ?: ctx.resources.getString(R.string.error_audiomanager)
             onCriticalError(msg)
         }

//        try {
//            if(mStimuliManager.getValidAudioManager(managerA) == null) throw Exception("deliverA1Stimulus: mAudioManager and given audio manager are both null")
//            // one of the two is not null
//
//            var duration:Long = mStimuliManager.audioDuration
//            val audio = if(managerA != null) {
//                            duration = managerA.duration
//                            when {
//                                managerA.isLoaded(managerA.resource as String) -> managerA
//                                (managerA.resource as String).isNotEmpty()       -> {
//                                    managerA.loadResource(managerA.resource as String)
//                                    managerA
//                                }
//                                else        -> throw Exception("deliverA2Stimulus: mediaplayer audio resource is empty")
//                            }
//                        } else mStimuliManager.mAudioManager
//
//            audio!!.deliver()
//            mStimuliHandler.postDelayed({ onEnd() }, duration)
//        }
//        catch (e:Exception){
//            val msg = e.message ?: ctx.resources.getString(R.string.error_audiomanager)
//            onCriticalError(msg)
//        }
    }

    protected fun deliverTStimulus(managerT: TactileManager? = null, onEnd:() -> Unit = {}){
        try {
            val tm = mStimuliManager.getValidTactileManager(managerT) ?:    throw Exception("deliverT1Stimulus: mTactileManager is null")
            if(!tm.isValid)                                               throw Exception("deliverT1Stimulus: mTactileManager is not valid")

            tm.deliver()
            mStimuliHandler.postDelayed({ onEnd() }, tm.duration)
        }
        catch (e:Exception){
            val msg = e.message ?: ctx.resources.getString(R.string.error_tactilemanager)
            onCriticalError(msg)
        }
    }

    private fun deliverVStimulus(managerV: VisualManager? = null, onEnd:() -> Unit = {}){

        try {
            val vm = mStimuliManager.getValidVisualManager(managerV) ?:     throw Exception("deliverVStimulus: mVisualManager is null")
            if(!vm.isValid)                                               throw Exception("deliverVStimulus: mVisualManager is not valid")

            vm.deliver()
            mStimuliHandler.postDelayed({ onEnd() }, vm.duration)
        }
        catch (e:Exception){
            val msg = e.message ?: ctx.resources.getString(R.string.error_visualmanager)
            onCriticalError(msg)
        }
    }
}