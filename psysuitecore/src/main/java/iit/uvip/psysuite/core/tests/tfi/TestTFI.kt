package iit.uvip.psysuite.core.tests.tfi

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.*
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
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
              speechManager: SpeechManager?,
              mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView)
{
    override var LOG_TAG:String = TestTFI::class.java.simpleName

    private val N_RIP_X_COND_X_BLOCK:Int        = 4
    private val N_RIP_X_COND_X_BLOCK_TOD:Int    = 2
    private val N_RIP_X_COND_X_BLOCK_BI:Int     = 5

    private val NUM_BLOCKS:Int                  = 4
    private val NUM_BLOCKS_BI:Int               = 2

    private val WN_PRESTIM_INTERVAL     = 1000L
    private val WN_POSTTSTIM_INTERVAL   = 500L
    private val STIM_DURATION_SHORT     = 7L
    private val STIM_DURATION           = 20L
    private val STIM_DURATION_TACTILE   = 35L

    private var rip_x_cond_block        = N_RIP_X_COND_X_BLOCK
    private var nblocks                 = NUM_BLOCKS

    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle, R.drawable.ape)

    companion object {

        @JvmStatic val TEST_BASIC_LABEL                 = "TFI"

        @JvmStatic val soa_0:Long = 30L
        @JvmStatic val soa_1:Long = 40L
        @JvmStatic val soa_2:Long = 85L

        @JvmStatic val TEST_BASIC_TODDLERS_LABEL        = "TFI toddlers"
        @JvmStatic val TEST_BASIC_BI_LABEL              = "TFI BI"
        @JvmStatic val TEST_BASIC_AV_LABEL              = "DFI AV"
        @JvmStatic val recipients:Array<String>         = arrayOf(  "uvip.apptester@gmail.com",
                                                                    "psysuite.uvip@gmail.com")
        fun getConditionsInfo(ctx: Context): List<ConditionData>{

            return if(VibrationManager.sysHasVibrator(ctx)){
                        mutableListOf(  ConditionData(TEST_BASIC_LABEL          , TEST_TFI          , TEST_BASIC_LABEL, Populations.sighted_hearing_populations),
                                        ConditionData(TEST_BASIC_TODDLERS_LABEL , TEST_TFI_TODDLERS , "${TEST_BASIC_LABEL}TOD", Populations.sighted_hearing_populations),
                                        ConditionData(TEST_BASIC_BI_LABEL       , TEST_TFI_BIMODAL  , "${TEST_BASIC_LABEL}BI",  Populations.sighted_hearing_populations),
                                        ConditionData(TEST_BASIC_AV_LABEL       , TEST_TFI_AV       , "${TEST_BASIC_LABEL}AV",  Populations.sighted_hearing_populations))
                    }else{
                        mutableListOf(  ConditionData(TEST_BASIC_AV_LABEL       , TEST_TFI_AV       , "${TEST_BASIC_LABEL}AV", Populations.sighted_hearing_populations))
                    }
        }

        fun getNextTrialModes(ctx: Context):List<List<Int>>{
            return if(VibrationManager.sysHasVibrator(ctx))
                listOf(listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER))
            else
                listOf(listOf(TEST_NEXTTRIAL_ANSWER))
        }
        fun getEmailRecipients():Array<String> = recipients
    }

    val STIM_A_ = STIM_A
    val STIM_T_ = STIM_T
    val STIM_V_ = STIM_V

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest() {

        if(mImageView == null) throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")

        if(vibrator == null && (subject.type == TEST_TFI || subject.type == TEST_TFI_TODDLERS))
            throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")

        createResultFile(TrialTFI.LOG_HEADER)
        initSummary()

        mQuestion           = ctx.resources.getString(R.string.tfi_question)
        validAnswers        = mutableListOf("0,0,1", "0,0,2", "1,0,0", "2,0,0", "0,1,0", "0,2,0", "2,0,1", "1,0,2", "1,2,0",
                                            "2,1,0", "0,1,2", "0,2,1", "1,0,1", "2,0,2", "1,1,0", "2,2,0", "0,2,2", "0,1,1",
                                            "1,2,1", "2,1,2", "2,1,1", "1,2,2", "1,1,2", "2,2,1", "1,1,1", "2,2,2")

        if (subject.whitenoise > TEST_SWITCH_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        when(subject.type){
            TEST_TFI            -> {
                rip_x_cond_block        = N_RIP_X_COND_X_BLOCK          // 4
                nblocks                 = NUM_BLOCKS                    // 4
                currStimulusDuration    = STIM_DURATION // 20L
            }
            TEST_TFI_TODDLERS   -> {
                rip_x_cond_block        = N_RIP_X_COND_X_BLOCK_TOD      // 2
                nblocks                 = NUM_BLOCKS                    // 4
                currStimulusDuration    = STIM_DURATION // 20L
             }
            TEST_TFI_BIMODAL,
            TEST_TFI_AV         -> {
                rip_x_cond_block        = N_RIP_X_COND_X_BLOCK_BI       // 5
                nblocks                 = NUM_BLOCKS_BI                 // 2
                currStimulusDuration    = STIM_DURATION_SHORT // 10L
            }
        }

//        subject.isDebug = true
        val trials =    if(subject.isDebug)  createTrialsDebug()
                        else
                            when(subject.type){
                                TEST_TFI, TEST_TFI_TODDLERS -> createTrials()
                                TEST_TFI_BIMODAL            -> createTrialsBimodal()
                                TEST_TFI_AV                 -> createTrialsAV()
                                else                        -> throw Exception("ERROR in TestTFI")
                            }
        mTrialsManager = FixedTrialsManager(trials as MutableList<TrialBasic>)


        // mTrials list
        var onImage         = 1
        when(subject.type){
            TEST_TFI            ->  mListBlocks = mutableListOf((nTrials * 0.25F).roundToInt(), (nTrials * 0.5F).roundToInt(), (nTrials * 0.75F).roundToInt())    // define two blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)
            TEST_TFI_TODDLERS   -> {
                                    mListBlocks = mutableListOf((nTrials * 0.25F).roundToInt(), (nTrials * 0.5F).roundToInt(), (nTrials * 0.75F).roundToInt())    // define two blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)
                                    onImage     = 2
            }
            TEST_TFI_BIMODAL,
            TEST_TFI_AV         ->  mListBlocks = mutableListOf(((nTrials * 0.5F).roundToInt()))    // define two blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)
        }

        mTestLabel      = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        mStimuliManager =   if(vibrator != null)
                                StimuliManager(
                                    AudioManager(STIM_A, audioResources[currStimulusDuration] ?: "t1000hz_7ms.wav",  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
                                    TactileManager(vibrator, duration = STIM_DURATION_TACTILE, handler = mStimuliHandler),
                                    VisualManager(STIM_V, mImageView, mDrawablesResource[onImage], duration = currStimulusDuration, handler = mStimuliHandler),
                                    subject.stimuliDelays, ctx, mStimuliHandler)
                            else
                                StimuliManager(
                                    AudioManager(STIM_A, audioResources[currStimulusDuration] ?: "t1000hz_7ms.wav",  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
                                    null,
                                    VisualManager(STIM_V, mImageView, mDrawablesResource[onImage], duration = currStimulusDuration, handler = mStimuliHandler),
                                    subject.stimuliDelays, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    //region CREATE TRIALS
    // =============================================================================================================================
    // set question and create trials list
    // [26 cond x 2 soa x 4/2 ] x 4 blocks = 208/104 x 4 blocks
    private fun createTrials():List<TrialBasic>{

        var cond_type = 0
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(b in 0 until NUM_BLOCKS){
            val block_trials:MutableList<TrialTFI> = mutableListOf()
            for(rb in 0 until rip_x_cond_block){

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 0, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 1, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 2, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 3, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 4, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 5, soa_1, validAnswers))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 6, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 7, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 8, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 9, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 10, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 11, soa_1, validAnswers))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 12, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 13, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 14, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 15, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 16, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 17, soa_1, validAnswers))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 18, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 19, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 20, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 21, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 22, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 23, soa_1, validAnswers))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 24, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 25, soa_1, validAnswers))

                cond_type = 0

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 0, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 1, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 2, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 3, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 4, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 5, soa_2, validAnswers))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 6, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 7, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 8, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 9, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 10, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 11, soa_2, validAnswers))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 12, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 13, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 14, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 15, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 16, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 17, soa_2, validAnswers))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 18, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 19, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 20, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 21, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 22, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 23, soa_2, validAnswers))

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 24, soa_2, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 25, soa_2, validAnswers))
            }
            block_trials.shuffle()
            trials.addAll(block_trials)
        }
        return trials
    }

    // [4x2 cond + 2x1 cond] x 1 soa x 5 x 2 blocks = 50 x 2 blocks
    private fun createTrialsBimodal():List<TrialBasic>{

        var cond_type = 0
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(b in 0 until nblocks){
            val block_trials:MutableList<TrialTFI> = mutableListOf()
            for(rb in 0 until rip_x_cond_block){

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 6, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 6, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 11,soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 11,soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 0, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 1, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 11,soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 11,soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 5, soa_1, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 3, soa_1, validAnswers))
            }
            block_trials.shuffle()
            trials.addAll(block_trials)
        }
        return trials
    }

    // [6x2 cond] x 1 soa x 5 x 2 blocks = 60 x 2 blocks
    private fun createTrialsAV():List<TrialBasic>{

        var cond_type = 0
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(b in 0 until nblocks){
            val block_trials:MutableList<TrialTFI> = mutableListOf()
            for(rb in 0 until rip_x_cond_block){

                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 6, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 7, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 0, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 1, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 2, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 3, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 13, soa_0, validAnswers))
            }
            block_trials.shuffle()
            trials.addAll(block_trials)
        }
        return trials
    }

    private fun createTrialsDebug():List<TrialBasic>{

        var cond_type = 0
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(b in 0 until 1000){

            val block_trials:MutableList<TrialTFI> = mutableListOf()

            for(rb in 0 until rip_x_cond_block){
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 6, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 7, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 13, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 3, soa_0, validAnswers))
                block_trials.add(TrialTFI(-1, cond_type++, "tfi", 1, soa_0, validAnswers))

//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,1", soa_1, validAnswers))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,1", soa_1, validAnswers))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,2", soa_1, validAnswers))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,0", soa_1, validAnswers))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "0,2,2", soa_1, validAnswers))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,2,0", soa_1, validAnswers))
//                block_trials.add(TrialTFI(-1, cond_type++, "tfi", "2,0,2", soa_1, validAnswers))
            }
            trials.addAll(block_trials)
        }
        return trials
    }
    //endregion
    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================

    override fun initSummary() {
        mSummary = when(subject.type)
        {
            TEST_TFI, TEST_TFI_TODDLERS ->  TFISummary(ctx)
            else                        ->  TFIBISummary(ctx)
        }
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat: Boolean) {

        mNoise?.start()

        val onset0      =  WN_PRESTIM_INTERVAL
        val onset1      =  WN_PRESTIM_INTERVAL + (trial as TrialTFI).soa
        val onset2      =  WN_PRESTIM_INTERVAL + 2*trial.soa
        val onsetEnd    =  WN_PRESTIM_INTERVAL + 2*trial.soa + currStimulusDuration + WN_POSTTSTIM_INTERVAL

        val corr_delays = subject.stimuliDelays.arrangeDelays(STIM_ATV, 0,0, 0)

        Log.d("TFI show1", "---------------------Trial type ${trial.correct_answer}, @ $onset0 | $onset1 | $onset2 | $onsetEnd")
//        Log.d("TFI show2", "delays ${corr_delays.a} | ${corr_delays.t} | ${corr_delays.v}")

            if(trial.stims[0] > 0)
                mStimuliHandler.postDelayed({   mStimuliManager.deliverShiftedStimulus(trial.stims[0], corr_delays.a, corr_delays.t, corr_delays.v, 1) }, onset0)

            if(trial.stims[1] > 0)
                mStimuliHandler.postDelayed({   mStimuliManager.deliverShiftedStimulus(trial.stims[1], corr_delays.a, corr_delays.t, corr_delays.v, 2) }, onset1)

            if(trial.stims[2] > 0)
                mStimuliHandler.postDelayed({   mStimuliManager.deliverShiftedStimulus(trial.stims[2], corr_delays.a, corr_delays.t, corr_delays.v, 3) }, onset2)

            mStimuliHandler.postDelayed({   onStimuliEnd()    }, onsetEnd)
    }
}
