package iit.uvip.psysuite.core.common

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Environment
import android.os.Handler
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.*
import org.albaspazio.core.ui.showAlert
import java.io.File
import java.util.*


/*
must contain all the possible codes

 */

abstract class TestBasic(protected val ctx: Context,
                         protected val activity: Activity,
                         protected val hostfragment: Fragment,
                         protected open val data: SubjectBasicParcel,
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

        @JvmStatic val TEST_ATB_TIME                = 130
        @JvmStatic val TEST_ATB_FREQUENCY           = 131
        @JvmStatic val TEST_ATB_TIME_INF            = 132
        @JvmStatic val TEST_ATB_TIME_INF_15s        = 134
        @JvmStatic val TEST_ATB_FREQUENCY_INF       = 133

        @JvmStatic val TEST_ATVB_TIME_SINGLESTIM    = 140
        @JvmStatic val TEST_ATVB_TIME_DOUBLESTIM    = 141
        @JvmStatic val TEST_ATVB_TIME_DOUBLESTIM2   = 142
        @JvmStatic val TEST_ATVB_TIME_SINGLESTIM2   = 143

        @JvmStatic val TEST_SAMPLE                  = 150

        //-----------------------------------------------------------------------------------------
        // STIMULUS TYPES UNIQUE CODES
        //-----------------------------------------------------------------------------------------
        @JvmStatic val STIM_TYPE_A1                 = 1     // tone
        @JvmStatic val STIM_TYPE_A2                 = 2     // resource
        @JvmStatic val STIM_TYPE_T                  = 3     //
        @JvmStatic val STIM_TYPE_V1                 = 4     // view made visible/invisible
        @JvmStatic val STIM_TYPE_V2                 = 5     // imageview with different color frame (one as background)

        @JvmStatic val STIM_TYPE_A1T                = 6     //
        @JvmStatic val STIM_TYPE_A2T                = 7     //
        @JvmStatic val STIM_TYPE_A1V1               = 8     //
        @JvmStatic val STIM_TYPE_A2V1               = 9     //
        @JvmStatic val STIM_TYPE_A1V2               = 10    //
        @JvmStatic val STIM_TYPE_A2V2               = 11    //
        @JvmStatic val STIM_TYPE_TV1                = 12    //
        @JvmStatic val STIM_TYPE_TV2                = 13    //

        @JvmStatic val STIM_TYPE_A1TV1              = 14    //
        @JvmStatic val STIM_TYPE_A2TV1              = 15    //
        @JvmStatic val STIM_TYPE_A1TV2              = 16    //
        @JvmStatic val STIM_TYPE_A2TV2              = 17    //

        //-----------------------------------------------------------------------------------------

        @JvmStatic val TEST_ABORT                       = 230
        @JvmStatic val TEST_COMPLETED                   = 231
        @JvmStatic val BLOCK_COMPLETED                  = 232

        @JvmStatic val TEST_PRE                         = 233
        @JvmStatic val TEST_POST                        = 234
        @JvmStatic val TEST_TRAINING                    = 235

    }

    val testEvent:PublishRelay<Pair<Int,Any?>> = PublishRelay.create()
    var mQuestion:String            = ""

    var showTrialsID:Int        = 0     // define when display trial id(0: never, 1: only @ trial end, 2: always)
    var abortMode:Int           = 0     // define abort modality (0:in answer dialog @ trial end, 1:button @ trial end, 2:always)
    var nextTrailModality:Int   = 0     // define how trials are displayed. 0: automatically, 1: after a next button, 2: after answer

    var nTrials:Int                             = 0
    var currTrial:Int                           = 0
    var mTestLabel: String                      = ""
    var validAnswers: MutableList<String>       = mutableListOf()

    // they are just proxy for properties (implemented / edited / accessed) in each subclass
    protected lateinit var mTrial: TrialBasic
    protected var mTrials:MutableList<TrialBasic>   = mutableListOf()
    protected var mListBlocks:MutableList<Int>      = mutableListOf()

    protected var mToneGen    = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
    protected var mTone       = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE
    protected lateinit var currTone:MediaPlayer
    protected var mStimuliHandler: Handler      = Handler()

    private var mResultFile: String             = ""

    // proxy for methods to be implemented in each subclass
    protected abstract fun initTest()
    abstract fun onTrialEnd()
    abstract fun show(trial:TrialBasic, isRepeat:Boolean=false)

    // ===============================================================================================================

    fun start(){
        currTrial   = 0
        mTrial      = mTrials[currTrial]

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

        return  if(currTrial == (nTrials - 1)) {
                    saveText(ctx, mResultFile, mTrial.Log(), overwrite = false, notifyDm = true)
                    EVENT_TEST_END            // END !
                }
                else if(mListBlocks.contains(currTrial)){
                    saveText(ctx, mResultFile, mTrial.Log(), overwrite = false, notifyDm = false)
                    EVENT_BLOCK_END
                }
                else
                {
                    saveText(ctx, mResultFile, mTrial.Log(), overwrite = false, notifyDm = false)
                    doNextTrial()
                }
    }

    // called by above nextTrial & by TestFragment after user decided to continue after block end
    fun doNextTrial():Int{
        mTrial = getNewTrial()  // it also updates currTrial

        if(isDebug)
            testEvent.accept(Pair(EVENT_SHOW_DEBUGINFO, getDebugInfo()))    // send debug info

        show(mTrial)
        return currTrial
    }

    // in the present basic form it does not do anything special.
    // can be overridden to implement custom online trials' values manipulation (e.g. in quest-based tasks)
    open fun getNewTrial():TrialBasic{
        currTrial++
        return mTrials[currTrial]
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

    // ===============================================================================================================
    // ACCESSORY
    // ===============================================================================================================
    protected fun getTestTitle(_type:Int):String{
        return "${ctx.resources.getString(R.string.app_name)} - ${ctx.resources.getString(R.string.lab_test_res)}: $mTestLabel"
    }

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

    // set trial id according to its order in the list
    protected fun setTrialsID(){
        mTrials.mapIndexed { index, trialBasic ->
            trialBasic.id = index
        }
    }

    private fun getDebugInfo():String{
        return mTrial.debugInfo()
    }

    // =============================================================================================================================
    // STIMULUS DELIVERY
    // =============================================================================================================================

    protected fun playbackAllAudioResource(resname:String, onEnd:()-> Unit = {}){

        val mediaPlayer = MediaPlayer.create(ctx, ctx.resources.getIdentifier(resname, "raw", ctx.packageName))
        mediaPlayer.setOnCompletionListener{
            onEnd()
            it.release()
        }
        mediaPlayer.start()
    }

    protected fun deliverStimulus(type:Int, duration:Long, extra:Any? = null, onEnd:() -> Unit = {}){
        when(type){
            STIM_TYPE_A1        -> deliverA1Stimulus(duration   , extra, onEnd)    // tone
            STIM_TYPE_A2        -> deliverA2Stimulus(duration   , extra, onEnd)    // mediaplayer from resource
            STIM_TYPE_T         -> deliverTStimulus(duration    , extra, onEnd)
            STIM_TYPE_V1        -> deliverV1Stimulus(duration   , extra, onEnd)    // imageview made visible/invisible
            STIM_TYPE_V2        -> deliverV2Stimulus(duration   , extra, onEnd)    // imageview with different color frame (one as background)
            STIM_TYPE_A1T       -> deliverA1TStimulus(duration  , extra, onEnd)
            STIM_TYPE_A2T       -> deliverA2TStimulus(duration  , extra, onEnd)
            STIM_TYPE_A1V1      -> deliverA1V1Stimulus(duration , extra, onEnd)
            STIM_TYPE_A2V1      -> deliverA2V1Stimulus(duration , extra, onEnd)
            STIM_TYPE_A1V2      -> deliverA1V2Stimulus(duration , extra, onEnd)
            STIM_TYPE_A2V2      -> deliverA2V2Stimulus(duration , extra, onEnd)
            STIM_TYPE_TV1       -> deliverTV1Stimulus(duration  , extra, onEnd)
            STIM_TYPE_TV2       -> deliverTV2Stimulus(duration  , extra, onEnd)
            STIM_TYPE_A1TV1     -> deliverA1TV1Stimulus(duration, extra, onEnd)
            STIM_TYPE_A2TV1     -> deliverA2TV1Stimulus(duration, extra, onEnd)
            STIM_TYPE_A1TV2     -> deliverA1TV2Stimulus(duration, extra, onEnd)
            STIM_TYPE_A2TV2     -> deliverA2TV2Stimulus(duration, extra, onEnd)
        }
    }

    protected fun deliverA1Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        mToneGen.startTone(mTone, duration.toInt())
        mStimuliHandler.postDelayed({
            onEnd()
        }, duration)
    }

    protected fun deliverA2Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        currTone.start()                              // MediaPlayer from resource/file

        mStimuliHandler.postDelayed({
            currTone.stop()
            currTone.prepare()
            onEnd()
        }, duration)
    }

    protected fun deliverTStimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        vibrator?.vibrateSingle(duration)
        mStimuliHandler.postDelayed({
            onEnd()
        }, duration)
    }

    protected fun deliverV1Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        mImageView?.visibility = View.VISIBLE

        mStimuliHandler.postDelayed({
            mImageView?.visibility = View.INVISIBLE
            onEnd()
        }, duration)
    }

    protected fun deliverV2Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){

        if(extra is Pair<*, *>){
            if(extra.first is Int && extra.second is Int) {
                val drawables = extra as Pair<Int, Int>
                mImageView?.setImageResource(drawables.first)

                mStimuliHandler.postDelayed({
                    mImageView?.setImageResource(drawables.second)
                    onEnd()
                }, duration)
            }
        }
        else showAlert(activity, ctx.resources.getString(R.string.error), ctx.resources.getString(R.string.internal_error, "deliverV2Stimulus: extra is not an Pair of drawables"))
    }

    protected fun deliverA1TStimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        mToneGen.startTone(mTone, duration.toInt())
        vibrator?.vibrateSingle(duration)
        mStimuliHandler.postDelayed({
            onEnd()
        }, duration)
    }

    protected fun deliverA2TStimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        vibrator?.vibrateSingle(duration)
        currTone.start()                              // MediaPlayer from resource/file

        mStimuliHandler.postDelayed({
            currTone.stop()
            currTone.prepare()
            onEnd()
        }, duration)
    }

    protected fun deliverA1V1Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        mToneGen.startTone(mTone, duration.toInt())
        mImageView?.visibility = View.VISIBLE

        mStimuliHandler.postDelayed({
            mImageView?.visibility = View.INVISIBLE
            onEnd()
        }, duration)
    }

    protected fun deliverA2V1Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        currTone.start()                              // MediaPlayer from resource/file
        mImageView?.visibility = View.VISIBLE

        mStimuliHandler.postDelayed({
            mImageView?.visibility = View.INVISIBLE
            currTone.stop()
            currTone.prepare()
            onEnd()
        }, duration)
    }

    protected fun deliverA1V2Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){

        if(extra is Pair<*, *>){
            if(extra.first is Int && extra.second is Int) {
                val drawables = extra as Pair<Int, Int>

                mToneGen.startTone(mTone, duration.toInt())
                mImageView?.setImageResource(drawables.first)

                mStimuliHandler.postDelayed({
                    mImageView?.setImageResource(drawables.second)
                    onEnd()
                }, duration)
            }
        }
    }

    protected fun deliverA2V2Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){

        if(extra is Pair<*, *>) {
            if (extra.first is Int && extra.second is Int) {
                val drawables = extra as Pair<Int, Int>

                currTone.start()                              // MediaPlayer from resource/file
                mImageView?.setImageResource(drawables.first)

                mStimuliHandler.postDelayed({
                    mImageView?.setImageResource(drawables.second)
                    currTone.stop()
                    currTone.prepare()
                    onEnd()
                }, duration)
            }
        }
    }

    protected fun deliverTV1Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        vibrator?.vibrateSingle(duration)
        mImageView?.visibility = View.VISIBLE

        mStimuliHandler.postDelayed({
            mImageView?.visibility = View.INVISIBLE
            onEnd()
        }, duration)
    }

    protected fun deliverTV2Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){

        if(extra is Pair<*, *>) {
            if (extra.first is Int && extra.second is Int) {
                val drawables = extra as Pair<Int, Int>

                vibrator?.vibrateSingle(duration)
                mImageView?.setImageResource(drawables.first)

                mStimuliHandler.postDelayed({
                    mImageView?.setImageResource(drawables.second)
                    onEnd()
                }, duration)
            }
        }
    }

    protected fun deliverA1TV1Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        vibrator?.vibrateSingle(duration)
        mToneGen.startTone(mTone, duration.toInt())
        mImageView?.visibility = View.VISIBLE

        mStimuliHandler.postDelayed({
            mImageView?.visibility = View.INVISIBLE
            onEnd()
        }, duration)
    }

    protected fun deliverA2TV1Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){
        vibrator?.vibrateSingle(duration)
        currTone.start()                              // MediaPlayer from resource/file
        mImageView?.visibility = View.VISIBLE

        mStimuliHandler.postDelayed({
            mImageView?.visibility = View.INVISIBLE
            currTone.stop()
            currTone.prepare()
            onEnd()
        }, duration)
    }

    protected fun deliverA1TV2Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){

        if(extra is Pair<*, *>) {
            if (extra.first is Int && extra.second is Int) {
                val drawables = extra as Pair<Int, Int>

                vibrator?.vibrateSingle(duration)
                mToneGen.startTone(mTone, duration.toInt())
                mImageView?.setImageResource(drawables.first)

                mStimuliHandler.postDelayed({
                    mImageView?.setImageResource(drawables.second)
                    onEnd()
                }, duration)
            }
        }
    }

    protected fun deliverA2TV2Stimulus(duration: Long, extra:Any? = null, onEnd:() -> Unit = {}){

        if(extra is Pair<*, *>) {
            if (extra.first is Int && extra.second is Int) {
                val drawables = extra as Pair<Int, Int>

                vibrator?.vibrateSingle(duration)
                currTone.start()                              // MediaPlayer from resource/file
                mImageView?.setImageResource(drawables.first)

                mStimuliHandler.postDelayed({
                    mImageView?.setImageResource(drawables.second)
                    currTone.stop()
                    currTone.prepare()
                    onEnd()
                }, duration)
            }
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

data class StimulusTypeDelay(val type: Int, val delay: Long)
data class StimulusType2Delay(val type: Int, val delay1: Long, val delay2: Long)
data class Stimulus3delay(val a:Long, val t:Long, val v:Long)

