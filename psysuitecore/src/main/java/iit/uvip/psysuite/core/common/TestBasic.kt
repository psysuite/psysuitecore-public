package iit.uvip.psysuite.core.common

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.*
import org.albaspazio.core.ui.showAlert
import java.util.*
import java.util.Collections.max
import java.util.Collections.min


/*
must contain all the possible codes

 */

abstract class TestBasic(protected val ctx: Context,
                         protected val activity: Activity,
                         protected val hostfragment: Fragment,
                         protected open val subjectparcel: SubjectBasicParcel,
                         protected val vibrator: VibrationManager? = null,
                         protected val mImageView: ImageView? = null,
                         protected val isDebug:Boolean = false
) {

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

        //-----------------------------------------------------------------------------------------
        //
        //-----------------------------------------------------------------------------------------
        @JvmStatic val EVENT_STIMULI_START              = 200   // unused
        @JvmStatic val EVENT_STIMULI_END                = 201   // unused

        @JvmStatic val EVENT_GIVE_ANSWER                = 202
        @JvmStatic val EVENT_GIVE_VOCAL_ANSWER          = 203
        @JvmStatic val EVENT_ANSWER_GIVEN               = 205
        @JvmStatic val EVENT_TRIAL_REPEAT               = 206
        @JvmStatic val EVENT_TRIAL_ABORT                = 207
        @JvmStatic val EVENT_TEST_END                   = -100
        @JvmStatic val EVENT_BLOCK_END                  = -101
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

        @JvmStatic val TEST_ATVB_TIME_S_UNBAL    = 140
        @JvmStatic val TEST_ATVB_TIME_D_UNBAL    = 141
        @JvmStatic val TEST_ATVB_TIME_D_BAL   = 142
        @JvmStatic val TEST_ATVB_TIME_S_BAL   = 143

        @JvmStatic val TEST_SAMPLE_ALIGNED          = 150
        @JvmStatic val TEST_SAMPLE_SHIFTED          = 151
        @JvmStatic val TEST_SAMPLE_PAIR             = 152

        //-----------------------------------------------------------------------------------------
        // STIMULUS TYPES UNIQUE CODES
        //-----------------------------------------------------------------------------------------
        // A1: tone, A2:resource, V1: view made visible/invisible, V2: imageview with different color frame (one as background), T1: single, T2:sequence
        @JvmStatic val STIM_TYPE_A1                 = 1     //  000 000 001
        @JvmStatic val STIM_TYPE_A2                 = 2     //  000 000 010
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
        @JvmStatic val STIM_TYPE_A2V2               = 130   //  010 000 010
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
        //-----------------------------------------------------------------------------------------

        @JvmStatic val TEST_ABORT                       = 230
        @JvmStatic val TEST_COMPLETED                   = 231
        @JvmStatic val BLOCK_COMPLETED                  = 232

        @JvmStatic val TEST_PRE                         = 233
        @JvmStatic val TEST_POST                        = 234
        @JvmStatic val TEST_TRAINING                    = 235

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

    // they are just proxy for properties (implemented / edited / accessed) in each subclass
    protected lateinit var mTrial: TrialBasic
    protected var mTrials:MutableList<TrialBasic>   = mutableListOf()

    protected var nBlocks:Int                       = 0

    protected var mListBlocks:MutableList<Int>      = mutableListOf()
    set(value) {
        field = value
        nBlocks = value.size + 1
    }


    // this instances are defined and validated during sub-class init{}
    // in case of error an exception is thrown and test is aborted.
    // the only susceptible to error is MediaPlayer in case of the test involves different resources to be loaded
    protected var mMediaPlayerManager:MediaPlayerManager? = null
    protected var mToneManager:ToneManager?         = null
    protected var mTactileManager:TactileManager?   = null
    protected var mVisualManager:VisualManager?     = null

    protected open var mDrawablesResource:MutableList<Int>  = mutableListOf()   // list of drawables' resources id to be edited in subclasses
    protected var mStimuliHandler: Handler                  = Handler()

    private var mResultFile: String                         = ""
    private var mCurrBlock: Int                             = 0

    protected var currStimulusDuration:Long     = 100L          // default value to be used when stimulus duration in not given
    protected var currVibrationAmplitude:Int    = -1            // default amplitude to be used when  not given
    protected var currVolume:Float              = 1F            // default audio volume to be used when  not given
    protected var currAudioResourceName:String  = "t200hz_2s"   // default amplitude to be used when  not given
    protected var ITI:Long                      = 0             // default ITI

    // proxy for methods to be implemented in each subclass
    protected abstract fun initTest()
    abstract fun onTrialEnd()
    abstract fun show(trial:TrialBasic, isRepeat:Boolean=false)

    // ===============================================================================================================

    fun start(){

        adjustBlocks(subjectparcel.block)     // set currTrial, mCurrBlock, mTrial

        if(!this::mTrial.isInitialized) return

        if(isDebug)
            testEvent.accept(Pair(EVENT_SHOW_DEBUGINFO, getDebugInfo()))    // send debug info

        show(mTrial)
    }

    fun repeatTrial(){
        show(mTrial, true)
    }
    // ===============================================================================================================
    // TRIAL MANAGEMENT
    // ===============================================================================================================
    open fun nextTrial(prev_result: String = "", elapsed: Int = -1): Int {

        if (prev_result != "")  mTrial.setResponse(prev_result, elapsed)

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
        mTrial = getNewTrial()  // it also updates currTrial

        if(isDebug)
            testEvent.accept(Pair(EVENT_SHOW_DEBUGINFO, getDebugInfo()))    // send debug info

        show(mTrial)
        return currTrial
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

        if(deleteOrShow)    deleteFile(mResultFile)
        else                notifyFile(mResultFile, ctx, dir)
    }

    fun stopTestAfterBlock(dir:String= Environment.DIRECTORY_DOWNLOADS):String{

        mStimuliHandler.removeCallbacksAndMessages(null)

        val newresname = subjectparcel.composeResultFileName(mCurrBlock)
        renameFile(mResultFile, newresname)

        val newsubjname = subjectparcel.composeSubjectFileName(mCurrBlock)
        renameFile(subjectparcel.subjectFileName, newsubjname)

        notifyFile(newresname, ctx, dir)

        return newresname
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

    protected fun getTestTitle(_type:Int):String{
        return "${ctx.resources.getString(R.string.app_name)} - ${ctx.resources.getString(R.string.lab_test_res)}: $mTestLabel"
    }

    protected fun createResultFile(subj:SubjectBasicParcel, header:String){
        mResultFile = subj.composeResultFileName(subj.block)
        saveText(ctx, mResultFile, header)
    }

    fun getResultFile(blk:Int=0): String{
        return  if(existFile(mResultFile).first)    mResultFile
        else                                ""
    }

    fun getAbsoluteResultFilePath(): String{
        return getAbsoluteFilePath(mResultFile).second      // is "" if file was not present
    }

    // set trial id according to its order in the list
    protected fun setTrialsID(){
        mTrials.mapIndexed { index, trialBasic ->
            trialBasic.id = index
        }
    }

    private fun getDebugInfo():String{
        return mTrial.debugInfo()
    }

    // manage durations when they are defined in the single call or retrieve the global value
    // returns MAX, MIN, MEAN
    private fun getDuration(managerA:StimulusManager? = null, managerT:StimulusManager? = null, managerV:StimulusManager? = null):Triple<Long,Long,Long>{

        val durs:MutableList<Long> = mutableListOf()
        if(managerA != null)     if(managerA.duration > 0)    durs.add(managerA.duration)
        if(managerT != null)     if(managerT.duration > 0)    durs.add(managerT.duration)
        if(managerV != null)     if(managerV.duration > 0)    durs.add(managerV.duration)

        if(durs.isEmpty())  return Triple(currStimulusDuration, currStimulusDuration, currStimulusDuration)

        var mean:Long = 0
        var cnt = 0
        durs.map {
            mean += it
            cnt++
        }
        mean /= cnt
        return Triple(max(durs) as Long, min(durs) as Long, mean)
    }

    private fun stimtypesFromSource(source:Int):List<Int>{

        val a = when {
            source and STIM_TYPE_A1 > 0 -> STIM_TYPE_A1
            source and STIM_TYPE_A2 > 0 -> STIM_TYPE_A2
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

    // =============================================================================================================================
    // STIMULUS DELIVERY
    // =============================================================================================================================

    // deliver a pair of identical aligned stimuli, separated by "isi" ms.
    protected fun deliverAlignedStimuliPair(isi:Long, type:Int, managerA:StimulusManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val meanduration = getDuration(managerA, managerT, managerV).third

        deliverAlignedStimulus(type, managerA, managerT, managerV)
        mStimuliHandler.postDelayed({
            deliverAlignedStimulus(type, managerA, managerT, managerV){   onEnd() }
        }, (meanduration + isi))
    }
    protected fun deliverShiftedStimuliPair(isi:Long, a:Long, t:Long, v:Long, managerA:StimulusManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null,
                                            audiotype:Int = STIM_TYPE_A1, visualtype:Int = STIM_TYPE_V1, tactiletype:Int = STIM_TYPE_T1, onEnd:() -> Unit = {}){

        val meanduration = getDuration(managerA, managerT, managerV).third

        deliverShiftedStimulus(a, t, v, managerA, managerT, managerV, audiotype, visualtype, tactiletype)
        mStimuliHandler.postDelayed({
            deliverShiftedStimulus(a, t, v, managerA, managerT, managerV, audiotype, visualtype, tactiletype){   onEnd() }
        }, (meanduration + isi))
    }

    // deliver unimodal stimuli at different latencies
    protected fun deliverShiftedStimulus(type:Int, a:Long, t:Long, v:Long, managerA:StimulusManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}) {
        val list = stimtypesFromSource(type)

        deliverShiftedStimulus(a, t, v, managerA, managerT, managerV, list[0], list[1], list[2], onEnd)
    }
    protected fun deliverShiftedStimulus(a:Long, t:Long, v:Long, managerA:StimulusManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null,
                                         audiotype:Int = STIM_TYPE_A1, visualtype:Int = STIM_TYPE_V1, tactiletype:Int = STIM_TYPE_T1, onEnd:()-> Unit = {}) {

        val offset = 500L
        val durlist = mutableListOf<Long>()

        val am:StimulusManager?
        if(a > -1) {
            am = when (audiotype == STIM_TYPE_A1) {
                true -> managerA ?: if (mToneManager == null) {
                                        Log.e("TestBasic", "error in deliverShiftedStimulus: a valid audio manager was not found")
                                        return
                                    }
                                    else mToneManager

                false -> managerA ?:    if (mMediaPlayerManager == null) {
                                            Log.e("TestBasic", "error in deliverShiftedStimulus: a valid audio manager was not found")
                                            return
                                        }
                                        else mMediaPlayerManager
            }
            durlist.add(am!!.duration + a)
            mStimuliHandler.postDelayed({
                Log.d("TestBasic", "audio: type=$audiotype")
                deliverAlignedStimulus(audiotype    , managerA = am)
            }, a + offset)
        }

        val tm:TactileManager?
        if(t > -1) {
            tm = managerT   ?:  if(mTactileManager == null) {
                                    Log.e("TestBasic", "error in deliverShiftedStimulus: a valid tactile manager was not found")
                                    return
                                }
                                else                        mTactileManager
            durlist.add(tm!!.duration + t)
            mStimuliHandler.postDelayed({
                Log.d("TestBasic", "tactile: type=$tactiletype")
                deliverAlignedStimulus(tactiletype  , managerT = tm)
            }, t + offset)
        }

        val vm:VisualManager?
        if(v > -1) {
            vm = managerV   ?:  if(mVisualManager == null)  {
                                    Log.e("TestBasic", "error in deliverShiftedStimulus: a valid visual manager was not found")
                                    return
                                }
                                else                        mVisualManager
            durlist.add(vm!!.duration + v)
            mStimuliHandler.postDelayed({
                Log.d("TestBasic", "visual: type=$visualtype")
                deliverAlignedStimulus(visualtype   , managerV = vm)
            }, v + offset)
        }

        val end:Long = max(durlist)
        mStimuliHandler.postDelayed({   onEnd() }, end + offset)
    }

    // simultaneous multimodal stimuli...redirect call to specific calls
    protected fun deliverAlignedStimulus(type:Int, managerA:StimulusManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){
        when(type){
            STIM_TYPE_A1        -> deliverA1Stimulus(managerA as ToneManager?, onEnd)             // ToneParams()        tone generator
            STIM_TYPE_A2        -> deliverA2Stimulus(managerA as MediaPlayerManager?, onEnd)             // AudioParams()       mediaplayer from resource
            STIM_TYPE_T1        -> deliverT1Stimulus(managerT, onEnd)             // TactileParams()
            STIM_TYPE_T2        -> deliverT2Stimulus(managerT, onEnd)             // TactileParams()
            STIM_TYPE_V1        -> deliverV1Stimulus(managerV, onEnd)             // VisualParams()      made visible/invisible
            STIM_TYPE_V2        -> deliverV2Stimulus(managerV, onEnd)             // VisualParams()      imageview with different color frame (one as background)
            STIM_TYPE_A1T1      -> deliverA1T1Stimulus(managerA as ToneManager?, managerT, onEnd)
            STIM_TYPE_A2T1      -> deliverA2T1Stimulus(managerA as MediaPlayerManager?, managerT, onEnd)
            STIM_TYPE_A1V1      -> deliverA1V1Stimulus(managerA as ToneManager?, managerV, onEnd)
            STIM_TYPE_A2V1      -> deliverA2V1Stimulus(managerA as MediaPlayerManager?, managerV, onEnd)
            STIM_TYPE_A1V2      -> deliverA1V2Stimulus(managerA as ToneManager?, managerV, onEnd)
            STIM_TYPE_A2V2      -> deliverA2V2Stimulus(managerA as MediaPlayerManager?, managerV, onEnd)
            STIM_TYPE_T1V1      -> deliverT1V1Stimulus(managerT, managerV, onEnd)
            STIM_TYPE_T1V2      -> deliverT1V2Stimulus(managerT, managerV, onEnd)
            STIM_TYPE_A1T1V1    -> deliverA1T1V1Stimulus(managerA as ToneManager?, managerT, managerV, onEnd)
            STIM_TYPE_A2T1V1    -> deliverA2T1V1Stimulus(managerA as MediaPlayerManager?, managerT, managerV, onEnd)
            STIM_TYPE_A1T1V2    -> deliverA1T1V2Stimulus(managerA as ToneManager?, managerT, managerV, onEnd)
            STIM_TYPE_A2T1V2    -> deliverA2T1V2Stimulus(managerA as MediaPlayerManager?, managerT, managerV, onEnd)
        }
    }

    protected fun deliverA1Stimulus(managerA:ToneManager? = null, onEnd:() -> Unit = {}){

        val am          = managerA ?: mToneManager!!
        if(!am.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1Stimulus: $am")
            return
        }
        val duration    = am.duration
        am.deliver()

        // ---------------------------------------------------------------------------------------
        mStimuliHandler.postDelayed({   onEnd() }, duration)
    }
    protected fun deliverA2Stimulus(managerA:MediaPlayerManager? = null, onEnd:() -> Unit = {}){

        try {
            var duration:Long = mMediaPlayerManager!!.duration
            val audio = if(managerA != null) {
                            duration = managerA.duration
                            when {
                                managerA.isLoaded(managerA.resource) -> managerA
                                managerA.resource.isNotEmpty()       -> {
                                                                        managerA.loadResource(managerA.resource)
                                                                        managerA
                                }
                                else                                 -> throw Exception("deliverA2Stimulus: mediaplayer audio resource is empty")
                            }
                        } else mMediaPlayerManager!!

            audio.deliver()
            // ---------------------------------------------------------------------------------------
            mStimuliHandler.postDelayed({
                onEnd()
            }, duration)
        }
        catch (e:Exception){

        }
    }

    protected fun deliverT1Stimulus(managerT:TactileManager? = null, onEnd:() -> Unit = {}){

        val tm = managerT ?: mTactileManager!!
        if(!tm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverT1Stimulus: $tm")
            return
        }
        val duration = tm.duration
        tm.deliver()

        // ---------------------------------------------------------------------------------------
        mStimuliHandler.postDelayed({   onEnd() }, duration)
    }
    protected fun deliverT2Stimulus(managerT:TactileManager? = null, onEnd:() -> Unit = {}){

        val tm = managerT ?: mTactileManager!!
        if(!tm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverT1Stimulus: $tm")
            return
        }
        val duration = tm.duration
        tm.deliver()

        // ---------------------------------------------------------------------------------------
        mStimuliHandler.postDelayed({   onEnd() }, duration)
    }

    protected fun deliverV1Stimulus(managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val vm = managerV ?: mVisualManager!!
        if(!vm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverV1Stimulus: $vm")
            return
        }
        val duration = vm.duration
        vm.deliver()

        // ---------------------------------------------------------------------------------------
        mStimuliHandler.postDelayed({
            onEnd()
        }, duration)
    }

    protected fun deliverV2Stimulus(managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val vm = managerV ?: mVisualManager!!
        if(!vm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverV2Stimulus: $vm")
            return
        }
        val duration = vm.duration
        vm.deliver()

        // ---------------------------------------------------------------------------------------
        mStimuliHandler.postDelayed({
            onEnd()
        }, duration)
    }

    protected fun deliverA1T1Stimulus(managerA:ToneManager? = null, managerT:TactileManager? = null, onEnd:() -> Unit = {}){

        val am = managerA ?: mToneManager!!
        if(!am.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1T1Stimulus: $am")
            return
        }
        am.deliver()

        // ---------------------------------------------------------------------------------------
        val tm = managerT ?: mTactileManager!!
        if(!tm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1T1Stimulus: $tm")
            return
        }
        tm.deliver()

        // ---------------------------------------------------------------------------------------
        val maxduration = getDuration(am, tm).first
        mStimuliHandler.postDelayed({   onEnd() }, maxduration)
    }
    protected fun deliverA2T1Stimulus(managerA:MediaPlayerManager? = null, managerT:TactileManager? = null, onEnd:() -> Unit = {}){

        try {
            val am = if(managerA != null) {
                when {
                    managerA.isLoaded(managerA.resource) -> managerA
                    managerA.resource.isNotEmpty() -> {
                        managerA.loadResource(managerA.resource)
                        managerA
                    }
                    else                                 -> throw Exception("deliverA2T1Stimulus: mediaplayer audio resource is empty")
                }
            } else mMediaPlayerManager!!
            am.deliver()

            // ---------------------------------------------------------------------------------------
            val tm = managerT ?: mTactileManager!!
            if(!tm.isValid()){
                // TODO : ALERT
                Log.e("TestBasic", "error in deliverA2T1Stimulus: $tm")
                return
            }
            tm.deliver()

            // ---------------------------------------------------------------------------------------
            val maxduration = getDuration(am, tm).first
            mStimuliHandler.postDelayed({
                onEnd()
            }, maxduration)
        }
        catch (e:Exception){

        }
    }
    protected fun deliverA1V1Stimulus(managerA:ToneManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val am          = managerA ?: mToneManager!!
        if(!am.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1V1Stimulus: $am")
            return
        }
        am.deliver()

        // ---------------------------------------------------------------------------------------
        val vm = managerV ?: mVisualManager!!
        if(!vm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1V1Stimulus: $vm")
            return
        }
        vm.deliver()

        // ---------------------------------------------------------------------------------------
        val maxduration = getDuration(am, vm).first
        mStimuliHandler.postDelayed({
            onEnd()
        }, maxduration)
    }
    protected fun deliverA2V1Stimulus(managerA:MediaPlayerManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        try {
            val am = if(managerA != null) {
                when {
                    managerA.isLoaded(managerA.resource) -> managerA
                    managerA.resource.isNotEmpty() -> {
                        managerA.loadResource(managerA.resource)
                        managerA
                    }
                    else                                 -> throw Exception("deliverA2Stimulus: mediaplayer audio resource is empty")
                }
            } else mMediaPlayerManager!!
            am.deliver()

            // ---------------------------------------------------------------------------------------
            val vm = managerV ?: mVisualManager!!
            if(!vm.isValid()){
                // TODO : ALERT
                Log.e("TestBasic", "error in deliverA2V1Stimulus: $vm")
                return
            }
            vm.deliver()

            // ---------------------------------------------------------------------------------------
            val maxduration = getDuration(am, vm).first
            mStimuliHandler.postDelayed({
                onEnd()
            }, maxduration)
        }
        catch (e:Exception){

        }
    }
    protected fun deliverA1V2Stimulus(managerA:ToneManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val am          = managerA ?: mToneManager!!
        if(!am.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1V2Stimulus: $am")
            return
        }
        am.deliver()

        // ---------------------------------------------------------------------------------------
        val vm = managerV ?: mVisualManager!!
        if(!vm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error deliverA1V2Stimulus: $vm")
            return
        }
        vm.deliver()

        // ---------------------------------------------------------------------------------------
        val maxduration = getDuration(am, vm).first
        mStimuliHandler.postDelayed({
            onEnd()
        }, maxduration)
    }
    protected fun deliverA2V2Stimulus(managerA:MediaPlayerManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        try {
            val am = if(managerA != null) {
                when {
                    managerA.isLoaded(managerA.resource) -> managerA
                    managerA.resource.isNotEmpty() -> {
                        managerA.loadResource(managerA.resource)
                        managerA
                    }
                    else                                 -> throw Exception("deliverA2V2Stimulus: mediaplayer audio resource is empty")
                }
            } else mMediaPlayerManager!!
            am.deliver()

            // ---------------------------------------------------------------------------------------
            val vm = managerV ?: mVisualManager!!
            if(!vm.isValid()){
                // TODO : ALERT
                Log.e("TestBasic", "error in deliverA2V2Stimulus: $vm")
                return
            }
            vm.deliver()

            // ---------------------------------------------------------------------------------------
            val maxduration = getDuration(am, vm).first
            mStimuliHandler.postDelayed({
                onEnd()
            }, maxduration)
        }
        catch (e:Exception){

        }
    }
    protected fun deliverT1V1Stimulus(managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val tm = managerT ?: mTactileManager!!
        if(!tm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverT1V1Stimulus: $tm")
            return
        }
        tm.deliver()

        // ---------------------------------------------------------------------------------------
        val vm = managerV ?: mVisualManager!!
        if(!vm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverT1V1Stimulus: $vm")
            return
        }
        vm.deliver()

        // ---------------------------------------------------------------------------------------
        val maxduration = getDuration(tm, vm).first
        mStimuliHandler.postDelayed({   onEnd() }, maxduration)
    }
    protected fun deliverT1V2Stimulus(managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val tm = managerT ?: mTactileManager!!
        if(!tm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverT1V2Stimulus: $tm")
            return
        }
        tm.deliver()

        // ---------------------------------------------------------------------------------------
        val vm = managerV ?: mVisualManager!!
        if(!vm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverT1V2Stimulus: $vm")
            return
        }
        vm.deliver()

        // ---------------------------------------------------------------------------------------
        val maxduration = getDuration(tm, vm).first
        mStimuliHandler.postDelayed({   onEnd() }, maxduration)
    }

    protected fun deliverA1T1V1Stimulus(managerA:ToneManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val am = managerA ?: mToneManager!!
        if(!am.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1T1V1Stimulus: $am")
            return
        }
        am.deliver()

        // ---------------------------------------------------------------------------------------
        val tm = managerT ?: mTactileManager!!
        if(!tm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1T1V1Stimulus: $tm")
            return
        }
        tm.deliver()

        // ---------------------------------------------------------------------------------------
        val vm = managerV ?: mVisualManager!!
        if(!vm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1T1V1Stimulus: $vm")
            return
        }
        vm.deliver()

        // ---------------------------------------------------------------------------------------
        val maxduration = getDuration(am, tm, vm).first
        mStimuliHandler.postDelayed({   onEnd() }, maxduration)
    }
    protected fun deliverA2T1V1Stimulus(managerA:MediaPlayerManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        try {
            val am = if(managerA != null) {
                when {
                    managerA.isLoaded(managerA.resource) -> managerA
                    managerA.resource.isNotEmpty() -> {
                        managerA.loadResource(managerA.resource)
                        managerA
                    }
                    else                                 -> throw Exception("deliverA2T1V1Stimulus: mediaplayer audio resource is empty")
                }
            } else mMediaPlayerManager!!
            am.deliver()

            // ---------------------------------------------------------------------------------------
            val tm = managerT ?: mTactileManager!!
            if(!tm.isValid()){
                // TODO : ALERT
                Log.e("TestBasic", "error in deliverA2T1V1Stimulus: $tm")
                return
            }
            tm.deliver()

            // ---------------------------------------------------------------------------------------
            val vm = managerV ?: mVisualManager!!
            if(!vm.isValid()){
                // TODO : ALERT
                Log.e("TestBasic", "error in deliverA2T1V1Stimulus: $vm")
                return
            }
            vm.deliver()

            // ---------------------------------------------------------------------------------------
            val maxduration = getDuration(am, tm, vm).first
            mStimuliHandler.postDelayed({
                onEnd()
            }, maxduration)
        }
        catch (e:Exception){

        }
    }
    protected fun deliverA1T1V2Stimulus(managerA:ToneManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        val am = managerA ?: mToneManager!!
        if(!am.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1T1Stimulus: $am")
            return
        }
        am.deliver()

        // ---------------------------------------------------------------------------------------
        val tm = managerT ?: mTactileManager!!
        if(!tm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA1T1Stimulus: $tm")
            return
        }
        tm.deliver()

        // ---------------------------------------------------------------------------------------
        val vm = managerV ?: mVisualManager!!
        if(!vm.isValid()){
            // TODO : ALERT
            Log.e("TestBasic", "error in deliverA2T1V1Stimulus: $vm")
            return
        }
        vm.deliver()

        // ---------------------------------------------------------------------------------------
        val maxduration = getDuration(am, tm, vm).first
        mStimuliHandler.postDelayed({   onEnd() }, maxduration)
    }
    protected fun deliverA2T1V2Stimulus(managerA:MediaPlayerManager? = null, managerT:TactileManager? = null, managerV:VisualManager? = null, onEnd:() -> Unit = {}){

        try {
            val am = if(managerA != null) {
                when {
                    managerA.isLoaded(managerA.resource) -> managerA
                    managerA.resource.isNotEmpty() -> {
                        managerA.loadResource(managerA.resource)
                        managerA
                    }
                    else                                 -> throw Exception("deliverA2T1V2Stimulus: mediaplayer audio resource is empty")
                }
            } else mMediaPlayerManager!!
            am.deliver()

            // ---------------------------------------------------------------------------------------
            val tm = managerT ?: mTactileManager!!
            if(!tm.isValid()){
                // TODO : ALERT
                Log.e("TestBasic", "error in deliverA2T1V2Stimulus: $tm")
                return
            }
            tm.deliver()

            // ---------------------------------------------------------------------------------------
            val vm = managerV ?: mVisualManager!!
            if(!vm.isValid()){
                // TODO : ALERT
                Log.e("TestBasic", "error in deliverA2T1V2Stimulus: $vm")
                return
            }
            vm.deliver()

            // ---------------------------------------------------------------------------------------
            val maxduration = getDuration(am, tm, vm).first
            mStimuliHandler.postDelayed({
                onEnd()
            }, maxduration)
        }
        catch (e:Exception){

        }
    }
    // =============================================================================================================================
}

@Parcelize
data class TaskCode(val label: String, val id: Int) : Parcelable{
    override fun toString(): String {
        return label
    }
}

@Parcelize
data class TestResult(var code:Int=-1, var mailsubject:String, var mailbody:String, var res_files:ArrayList<String> = arrayListOf(), val testClass:String) : Parcelable

data class StimulusATBInfants(val type: Int, val tactile_pattern:Int)
data class Stimulus3delay(val type: Int, val a:Long, val t:Long, val v:Long)
data class StimulusBindingsUnbalanced(val type: Int, val delay:Long)
data class StimulusBIS(val ntrials:Int, val position:Int, val conflict:String)

fun VibrationManager.vibrateSingle(paramsT:TactileManager) {
    this.vibrateSingle(paramsT.duration, paramsT.amplitude)
}
