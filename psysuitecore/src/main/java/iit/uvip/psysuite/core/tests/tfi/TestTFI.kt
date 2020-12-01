package iit.uvip.psysuite.core.tests.tfi

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.*
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.TrialBasic
import iit.uvip.psysuite.core.utility.ConditionData
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import kotlin.math.roundToInt

/*
    tot_trials = 26cond * 10 rep * 2 soa (= 520 trials) , divided in two blocks.
*/
class TestTFI(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView)
{
    override var LOG_TAG:String = TestTFI::class.java.simpleName

    private val N_RIP_X_COND_X_BLOCK:Int        = 4
    private val N_RIP_X_COND_X_BLOCK_TOD:Int    = 2

    private val NUM_BLOCKS:Int                  = 4

    private val WN_PRESTIM_INTERVAL     = 1000L
    private val WN_POSTTSTIM_INTERVAL   = 500L
    private val STIM_DURATION           = 35L

    private var rip_x_cond_block        = N_RIP_X_COND_X_BLOCK
    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle, R.drawable.ape)

    companion object {

        @JvmStatic val soa_1:Long = 55L
        @JvmStatic val soa_2:Long = 85L

        @JvmStatic val STIM_A     = StimuliManager.STIM_TYPE_A1
        @JvmStatic val STIM_V     = StimuliManager.STIM_TYPE_V2
        @JvmStatic val STIM_T     = StimuliManager.STIM_TYPE_T1
        @JvmStatic val STIM_ATV   = STIM_A or STIM_T or STIM_V

        @JvmStatic val TEST_BASIC_LABEL                 = "TFI"
        @JvmStatic val TEST_BASIC_TODDLERS_LABEL        = "TFI toddlers"
        @JvmStatic val recipients:Array<String>         = arrayOf(  "uvip.apptester@gmail.com",
                                                                    "psysuite.uvip@gmail.com",
                                                                    "alessia.tonelli@iit.it")

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData(TEST_BASIC_LABEL, TEST_TFI, TEST_BASIC_LABEL, Populations.sighted_hearing_populations),
            ConditionData(TEST_BASIC_TODDLERS_LABEL, TEST_TFI_TODDLERS, "TFITOD", Populations.sighted_hearing_populations)
        )

        fun getNextTrialModes():List<List<Int>> = listOf(listOf(TEST_NEXTTRIAL_ANSWER))

        fun getEmailRecipients():Array<String> = recipients
    }

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest() {

        when {
            mImageView == null -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }

        nextTrailModality   = subject.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        createResultFile(subject, TrialTFI.LOG_HEADER)
        initSummary()

        mQuestion           = ctx.resources.getString(R.string.tfi_question)
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        currStimulusDuration    = STIM_DURATION // 35L

        if (subject.whitenoise > TEST_WNOISE_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        if(!subject.isDebug)  createTrials()
        else                  createTrialsDebug()
        // mTrials list
        nTrials             = mTrials.size
        currTrial           = 0

        rip_x_cond_block    = N_RIP_X_COND_X_BLOCK
        var onImage         = 1
        if(subject.type == TEST_TFI_TODDLERS) {
            rip_x_cond_block = N_RIP_X_COND_X_BLOCK_TOD
            onImage          = 2
        }

        mListBlocks     = mutableListOf((nTrials * 0.25F).roundToInt(), (nTrials * 0.5F).roundToInt(), (nTrials * 0.75F).roundToInt())    // define two blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)
        mTestLabel      = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        mStimuliManager = StimuliManager(
            AudioManager(STIM_A, -1,  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
//                                            AudioManager(UNIMODAL_AUDIO_CODE, listOf("t1000hz_30ms.wav"), 100, duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
//                                            AudioManager(UNIMODAL_AUDIO_CODE, "t1000hz_30ms",  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
            TactileManager(vibrator!!, duration = currStimulusDuration, handler = mStimuliHandler),
            VisualManager(STIM_V, mImageView!!, mDrawablesResource[onImage], mDrawablesResource[0], duration = currStimulusDuration, handler = mStimuliHandler),
            delaysAligner, ctx)

        testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    // set question and create trials list
    // [26 cond x 2 soa x 4/2 ] x 4 blocks = 208/104 x 4 blocks
    private fun createTrials(){

        var cond_type = 0
        for(b in 0 until NUM_BLOCKS){
            val block_trials:MutableList<TrialTFI> = mutableListOf()
            for(rb in 0 until rip_x_cond_block){

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,0,1", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,0,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,0,0", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,0", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,1,0", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,0", soa_1))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,1", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,0,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,2,0", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,1,0", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,1,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,1", soa_1))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,0,1", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,1,0", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,0", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,1,1", soa_1))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,2,1", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,1,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,1,1", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,2,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,1,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,1", soa_1))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,1,1", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,2", soa_1))

                cond_type = 0

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,0,1", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,0,2", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,0,0", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,0", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,1,0", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,0", soa_2))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,1", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,0,2", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,2,0", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,1,0", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,1,2", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,1", soa_2))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,0,1", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,2", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,1,0", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,0", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,2", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,1,1", soa_2))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,2,1", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,1,2", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,1,1", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,2,2", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,1,2", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,1", soa_2))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "1,1,1", soa_2))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,2", soa_2))
            }
            block_trials.shuffle()
            mTrials.addAll(block_trials)
        }

        // set trial id according to its order in the list
        mTrials.mapIndexed { index, trial -> trial.id = (index + 1) }
    }
    private fun createTrialsDebug(){

        var cond_type = 0
        for(b in 0 until 1000){

            val block_trials:MutableList<TrialTFI> = mutableListOf()

            for(rb in 0 until rip_x_cond_block){
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,2", soa_1))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,0", soa_1))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,2", soa_1))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,0", soa_1))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,2", soa_1))
            }
            mTrials.addAll(block_trials)
        }

        // set trial id according to its order in the list
        mTrials.mapIndexed { index, trial -> trial.id = (index + 1) }
    }
    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onTrialEnd() {

        mNoise?.stop()
        mNoise?.prepare()

        when (nextTrailModality) {

            TEST_NEXTTRIAL_VOICE_ANSWER         ->  testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
            TEST_NEXTTRIAL_ANSWER               ->  testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER  -> {
                testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
                testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            }
        }
    }

    override fun nextTrial(prev_result: String, elapsed: Int): Int {
        testEvent.accept(Pair(EVENT_UPDATE_TRIAL_ID, 0L))
        return super.nextTrial(prev_result, elapsed)
    }

    override fun initSummary() {
        mSummary = TFISummary(ctx)
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat: Boolean) {

        mNoise?.start()

//        val onset0      = WN_PRESTIM_INTERVAL
        val onset1      =  (trial as TrialTFI).soa
        val onset2      =  2*trial.soa
        val onsetEnd    =  2*trial.soa + currStimulusDuration + WN_POSTTSTIM_INTERVAL

//        mStimuliHandler.postDelayed({
//            if(trial.stims[0] > 0)
//                deliverAlignedStimuliPair(2*trial.soa, trial.stims[0])
//
//            mStimuliHandler.postDelayed({   onTrialEnd()    }, onsetEnd)
//
//        }, WN_PRESTIM_INTERVAL)

//        if(trial.stims[0] > 0)
////           deliverAlignedStimuliPair(2*trial.soa, trial.stims[0])
//            mStimuliHandler.postDelayed({   deliverAlignedStimuliPair(2*trial.soa, trial.stims[0]) }, WN_PRESTIM_INTERVAL)
//
//        if(trial.stims[1] > 0)
//            mStimuliHandler.postDelayed({   deliverAlignedStimulus(trial.stims[1]) }, onset1)
//
//        mStimuliHandler.postDelayed({   onTrialEnd()    }, onsetEnd)

        var corr_delays = delaysAligner.arrangeDelays(STIM_ATV, -25,0, 0)

//        Log.d("TFI show1", "Trial type ${trial.correct_answer}")
//        Log.d("TFI show2", "delays ${corr_delays.a} | , ${corr_delays.t} | ${corr_delays.v}")
//        Log.d("TFI show3", "$onset0 | $onset1 | $onset2 | $onsetEnd ")

        mStimuliHandler.postDelayed({

            if(trial.stims[0] > 0)
                mStimuliManager.deliverShiftedStimulus(trial.stims[0], corr_delays.a, corr_delays.t, corr_delays.v)
//                deliverShiftedStimulus(trial.stims[0], corr_delays.a, corr_delays.t, corr_delays.v)
//                mStimuliHandler.postDelayed({   deliverShiftedStimulus(trial.stims[0], corr_delays.a, corr_delays.t, corr_delays.v) }, onset0)

            if(trial.stims[1] > 0)
                mStimuliHandler.postDelayed({   mStimuliManager.deliverShiftedStimulus(trial.stims[1], corr_delays.a, corr_delays.t, corr_delays.v) }, onset1)

            if(trial.stims[2] > 0){
                corr_delays = delaysAligner.arrangeDelays(STIM_ATV)
                mStimuliHandler.postDelayed({   mStimuliManager.deliverShiftedStimulus(trial.stims[2], corr_delays.a, corr_delays.t, corr_delays.v) }, onset2)
            }

            mStimuliHandler.postDelayed({   onTrialEnd()    }, onsetEnd)

        }, WN_PRESTIM_INTERVAL)
    }
}
