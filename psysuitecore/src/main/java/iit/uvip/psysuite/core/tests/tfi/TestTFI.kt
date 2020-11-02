package iit.uvip.psysuite.core.tests.tfi

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.SpinnerData
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import iit.uvip.psysuite.core.common.stimuli.*
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.ui.showToast
import kotlin.math.roundToInt

/*
    tot_trials = 26cond * 10 rep * 2 soa (= 520 trials) , divided in two blocks.
*/
class TestTFI(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subjectparcel: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?
) : TestBasic(ctx, activity, hostfragment, subjectparcel, vibrator, mImageView)
{
    override var LOG_TAG:String = TestTFI::class.java.simpleName


    private val soa_1:Long = 55L
    private val soa_2:Long = 85L

    private val N_RIP_X_COND_X_BLOCK:Int = 6
    private val NUM_BLOCKS:Int    = 4

    private val WN_PRESTIM_INTERVAL     = 1000L
    private val WN_POSTTSTIM_INTERVAL   = 500L
    private val STIM_DURATION           = 35L



    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle, R.drawable.ape)

    companion object {

        @JvmStatic val UNIMODAL_AUDIO_CODE     = STIM_TYPE_A1
        @JvmStatic val TRIMODAL_AUDIO_CODE     = (STIM_TYPE_T1V2 or UNIMODAL_AUDIO_CODE)

        @JvmStatic val TEST_BASIC_LABEL                 = "TFI"
        @JvmStatic val TEST_BASIC_TODDLERS_LABEL        = "TFI toddlers"
        @JvmStatic val recipients:Array<String>         = arrayOf(  "uvip.apptester@gmail.com",
                                                                    "psysuite.uvip@gmail.com",
                                                                    "alessia.tonelli@iit.it")

        fun getConditionsInfo(ctx: Context): List<SpinnerData> = mutableListOf( SpinnerData(TEST_BASIC_LABEL, TEST_TFI, TEST_BASIC_LABEL),
                                                                                SpinnerData(TEST_BASIC_TODDLERS_LABEL, TEST_TFI_TODDLERS, "TFITOD"))

        fun getNextTrialModes():List<List<Int>>{
            return listOf(  listOf(TEST_NEXTTRIAL_ANSWER))
        }
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

        nextTrailModality   = subjectparcel.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        createResultFile(subjectparcel, TrialTFI.LOG_HEADER)
//        initSummary()

        mQuestion           = ctx.resources.getString(R.string.tfi_question)
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        currStimulusDuration    = STIM_DURATION // 35L

        if (subjectparcel.whitenoise > TEST_WNOISE_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        if(!subjectparcel.isDebug)  createTrials()
        else                        createTrialsDebug()
        // mTrials list
        nTrials         = mTrials.size
        currTrial       = 0

        mListBlocks     = mutableListOf((nTrials / 0.25F).roundToInt(), (nTrials / 0.5F).roundToInt(), (nTrials / 0.75F).roundToInt())    // define two blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)

        mTestLabel      = ""
        getConditionsInfo(ctx).map {
            if (it.id == subjectparcel.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        val onImage =   if(subjectparcel.type == TEST_TFI)    1
                        else                                  2

        mStimuliManager = StimuliManager(
            AudioManager(UNIMODAL_AUDIO_CODE, -1,  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
//                                            AudioManager(UNIMODAL_AUDIO_CODE, listOf("t1000hz_30ms.wav"), 100, duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
//                                            AudioManager(UNIMODAL_AUDIO_CODE, "t1000hz_30ms",  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
            TactileManager(vibrator!!, duration = currStimulusDuration, handler = mStimuliHandler),
            VisualManager(STIM_TYPE_V2, mImageView!!, mDrawablesResource[onImage], mDrawablesResource[0], duration = currStimulusDuration, handler = mStimuliHandler))

        testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================    // set question and create trials list
    private fun createTrials(){

        var cond_type = 0
        for(b in 0 until NUM_BLOCKS){

            val block_trials:MutableList<TrialTFI> = mutableListOf()

            for(rb in 0 until N_RIP_X_COND_X_BLOCK){

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

            for(rb in 0 until N_RIP_X_COND_X_BLOCK){
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
        TODO("Not yet implemented")
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

        val corr_delays = delaysAligner.arrangeDelays(TRIMODAL_AUDIO_CODE, -25,0, 0)

//        Log.d("TFI show1", "Trial type ${trial.correct_answer}")
//        Log.d("TFI show2", "delays ${corr_delays.a} | , ${corr_delays.t} | ${corr_delays.v}")
//        Log.d("TFI show3", "$onset0 | $onset1 | $onset2 | $onsetEnd ")

        mStimuliHandler.postDelayed({

//            if(trial.stims[0] > 0)
                deliverShiftedStimulus(trial.stims[0], corr_delays.a, corr_delays.t, corr_delays.v)
//                mStimuliHandler.postDelayed({   deliverShiftedStimulus(trial.stims[0], corr_delays.a, corr_delays.t, corr_delays.v) }, onset0)

            if(trial.stims[1] > 0)
                mStimuliHandler.postDelayed({   deliverShiftedStimulus(trial.stims[1], corr_delays.a, corr_delays.t, corr_delays.v) }, onset1)

            if(trial.stims[2] > 0)
                mStimuliHandler.postDelayed({   deliverShiftedStimulus(trial.stims[2], corr_delays.a+25, corr_delays.t, corr_delays.v) }, onset2)

            mStimuliHandler.postDelayed({   onTrialEnd()    }, onsetEnd)

        }, WN_PRESTIM_INTERVAL)
    }
}
