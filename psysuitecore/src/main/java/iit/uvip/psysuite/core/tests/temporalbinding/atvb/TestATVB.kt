package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.albaspazio.psysuite.adaptive.AdaptiveWrapper
import org.albaspazio.psysuite.adaptive.TaskADAParams
import org.albaspazio.psysuite.adaptive.ado.ADOParams
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.AudioManager
import iit.uvip.psysuite.core.stimuli.ImageViewDefinedException
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.TactileManager
import iit.uvip.psysuite.core.stimuli.VibratorNotDefinedException
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.ISI
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_ATV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_AT_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_AV_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_TV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_T_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_V_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_TV_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_AV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_A_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_V_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_AT
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_A_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_T_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.WN_FIRSTSTIM_INTERVAL
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.balshSD
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.unbalSD
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindings3latencies
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.CorrectedStimuliDelay
import iit.uvip.psysuite.core.utility.Stimulus3delay
import iit.uvip.psysuite.core.utility.StimulusDelay
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import kotlin.math.roundToInt

class TestATVB(
    ctx: Context,
    activity: Activity,
    hostfragment: Fragment,
    subject: SubjectBasicParcel,
    vibrator: VibrationManager?,
    mImageView: ImageView?,
    speechManager: SpeechManager?,
    mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView) {

    override var LOG_TAG: String = TestATVB::class.java.simpleName

    companion object {
        // Overrides
        @JvmStatic val TEST_BASIC_LABEL     = "ATVB"

        // Test-specific repetitions
        @JvmStatic val NUM_REPETITIONS      = 4
        @JvmStatic val NUM_REPETITIONS_B    = 8

        // Email configuration
        @JvmStatic val recipients:Array<String> = arrayOf("psysuite.uvip@gmail.com") // "psysuite.uvip@gmail.com",

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_single),  TEST_ATVB_TIME_S_UNBAL, "${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atvb_subtask_time_single_tag)}", Populations.sighted_hearing_populations),
            ConditionData(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_double), TEST_ATVB_TIME_D_UNBAL, "${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atvb_subtask_time_double_tag)}", Populations.sighted_hearing_populations),
            ConditionData(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_single_b), TEST_ATVB_TIME_S_BAL, "${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atvb_subtask_time_single_b_tag)}", Populations.sighted_hearing_populations))
//            ConditionData(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_single_b2), TEST_ATVB_TIME_S_BAL2, "${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atvb_subtask_time_single_b2_tag)}", Populations.sighted_hearing_populations))

        // unbalanced stimuli temporarily disabled
        fun getNextTrialModes(ctx:Context):List<List<Int>> = listOf(
            listOf(TEST_NEXTTRIAL_ANSWER),
            listOf(TEST_NEXTTRIAL_ANSWER),
            listOf(TEST_NEXTTRIAL_ANSWER))
//                            listOf(TEST_NEXTTRIAL_ANSWER))
//                            listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))

        fun getEmailRecipients():Array<String> = recipients
    }

    private var curISI: Long = 0L

    // 36 = (3x2) x 6     different elements
    private val lStimuliUnbalanced: List<StimulusDelay> = listOf(

        StimulusDelay( TYPE_A_TV, unbalSD[0].first),
        StimulusDelay( TYPE_TV_A, unbalSD[0].first),
        StimulusDelay( TYPE_V_AT, unbalSD[0].first),
        StimulusDelay( TYPE_AT_V, unbalSD[0].first),
        StimulusDelay( TYPE_T_AV, unbalSD[0].first),
        StimulusDelay( TYPE_AV_T, unbalSD[0].first),

        StimulusDelay( TYPE_A_TV, unbalSD[1].first),
        StimulusDelay( TYPE_TV_A, unbalSD[1].first),
        StimulusDelay( TYPE_V_AT, unbalSD[1].first),
        StimulusDelay( TYPE_AT_V, unbalSD[1].first),
        StimulusDelay( TYPE_T_AV, unbalSD[1].first),
        StimulusDelay( TYPE_AV_T, unbalSD[1].first),

        StimulusDelay( TYPE_A_TV, unbalSD[2].first),
        StimulusDelay( TYPE_TV_A, unbalSD[2].first),
        StimulusDelay( TYPE_V_AT, unbalSD[2].first),
        StimulusDelay( TYPE_AT_V, unbalSD[2].first),
        StimulusDelay( TYPE_T_AV, unbalSD[2].first),
        StimulusDelay( TYPE_AV_T, unbalSD[2].first),

        StimulusDelay( TYPE_A_TV, unbalSD[3].first),
        StimulusDelay( TYPE_TV_A, unbalSD[3].first),
        StimulusDelay( TYPE_V_AT, unbalSD[3].first),
        StimulusDelay( TYPE_AT_V, unbalSD[3].first),
        StimulusDelay( TYPE_T_AV, unbalSD[3].first),
        StimulusDelay( TYPE_AV_T, unbalSD[3].first),

        StimulusDelay( TYPE_A_TV, unbalSD[4].first),
        StimulusDelay( TYPE_TV_A, unbalSD[4].first),
        StimulusDelay( TYPE_V_AT, unbalSD[4].first),
        StimulusDelay( TYPE_AT_V, unbalSD[4].first),
        StimulusDelay( TYPE_T_AV, unbalSD[4].first),
        StimulusDelay( TYPE_AV_T, unbalSD[4].first),

        StimulusDelay( TYPE_A_TV, unbalSD[5].first),
        StimulusDelay( TYPE_TV_A, unbalSD[5].first),
        StimulusDelay( TYPE_V_AT, unbalSD[5].first),
        StimulusDelay( TYPE_AT_V, unbalSD[5].first),
        StimulusDelay( TYPE_T_AV, unbalSD[5].first),
        StimulusDelay( TYPE_AV_T, unbalSD[5].first),
    )

    // 18 different elements: 6 x 3 delays (200, 250, 300)
    // a-t-v, a-v-t, t-a-v, t-v-a, v-a-t, v-t-a
    private val lStimuliBalancedShort: List<StimulusDelay> = listOf(

        StimulusDelay( TYPE_V_A_T,balshSD[0].first),
        StimulusDelay( TYPE_T_A_V,balshSD[0].first),
        StimulusDelay( TYPE_A_T_V,balshSD[0].first),
        StimulusDelay( TYPE_V_T_A,balshSD[0].first),
        StimulusDelay( TYPE_A_V_T,balshSD[0].first),
        StimulusDelay( TYPE_T_V_A,balshSD[0].first),

        StimulusDelay( TYPE_V_A_T,balshSD[1].first),
        StimulusDelay( TYPE_T_A_V,balshSD[1].first),
        StimulusDelay( TYPE_A_T_V,balshSD[1].first),
        StimulusDelay( TYPE_V_T_A,balshSD[1].first),
        StimulusDelay( TYPE_A_V_T,balshSD[1].first),
        StimulusDelay( TYPE_T_V_A,balshSD[1].first),

        StimulusDelay( TYPE_V_A_T,balshSD[2].first),
        StimulusDelay( TYPE_T_A_V,balshSD[2].first),
        StimulusDelay( TYPE_A_T_V,balshSD[2].first),
        StimulusDelay( TYPE_V_T_A,balshSD[2].first),
        StimulusDelay( TYPE_A_V_T,balshSD[2].first),
        StimulusDelay( TYPE_T_V_A,balshSD[2].first)
    )

    // 72 different elements. UNUSED !
    private val lStimuliBalanced: List<Stimulus3delay> = listOf(

        Stimulus3delay( 0,50.0F, 100.0F, 0.0F),
        Stimulus3delay( 0,50.0F, 0.0F, 100.0F),
        Stimulus3delay( 0,100.0F, 50.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 50.0F, 100.0F),
        Stimulus3delay( 0,0.0F, 100.0F, 50.0F),
        Stimulus3delay( 0,100.0F, 0.0F, 50.0F),
        Stimulus3delay( 0,100.0F, 50.0F, 0.0F),
        Stimulus3delay( 0,100.0F, 0.0F, 50.0F),
        Stimulus3delay( 0,0.0F, 100.0F, 50.0F),
        Stimulus3delay( 0,50.0F, 100.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 50.0F, 100.0F),
        Stimulus3delay( 0,50.0F, 0.0F, 100.0F),

        Stimulus3delay( 0,100.0F, 200.0F, 0.0F),
        Stimulus3delay( 0,100.0F, 0.0F, 200.0F),
        Stimulus3delay( 0,200.0F, 100.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 100.0F, 200.0F),
        Stimulus3delay( 0,0.0F, 200.0F, 100.0F),
        Stimulus3delay( 0,200.0F, 0.0F, 100.0F),
        Stimulus3delay( 0,200.0F, 100.0F, 0.0F),
        Stimulus3delay( 0,200.0F, 0.0F, 100.0F),
        Stimulus3delay( 0,0.0F, 200.0F, 100.0F),
        Stimulus3delay( 0,100.0F, 200.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 100.0F, 200.0F),
        Stimulus3delay( 0,100.0F, 0.0F, 200.0F),

        Stimulus3delay( 0,200.0F, 400.0F, 0.0F),
        Stimulus3delay( 0,200.0F, 0.0F, 400.0F),
        Stimulus3delay( 0,400.0F, 200.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 200.0F, 400.0F),
        Stimulus3delay( 0,0.0F, 400.0F, 200.0F),
        Stimulus3delay( 0,400.0F, 0.0F, 200.0F),
        Stimulus3delay( 0,400.0F, 200.0F, 0.0F),
        Stimulus3delay( 0,400.0F, 0.0F, 200.0F),
        Stimulus3delay( 0,0.0F, 400.0F, 200.0F),
        Stimulus3delay( 0,200.0F, 400.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 200.0F, 400.0F),
        Stimulus3delay( 0,200.0F, 0.0F, 400.0F),

        Stimulus3delay( 0,300.0F, 600.0F, 0.0F),
        Stimulus3delay( 0,300.0F, 0.0F, 600.0F),
        Stimulus3delay( 0,600.0F, 300.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 300.0F, 600.0F),
        Stimulus3delay( 0,0.0F, 600.0F, 300.0F),
        Stimulus3delay( 0,600.0F, 0.0F, 300.0F),
        Stimulus3delay( 0,600.0F, 300.0F, 0.0F),
        Stimulus3delay( 0,600.0F, 0.0F, 300.0F),
        Stimulus3delay( 0,0.0F, 600.0F, 300.0F),
        Stimulus3delay( 0,300.0F, 600.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 300.0F, 600.0F),
        Stimulus3delay( 0,300.0F, 0.0F, 600.0F),

        Stimulus3delay( 0,400.0F, 800.0F, 0.0F),
        Stimulus3delay( 0,400.0F, 0.0F, 800.0F),
        Stimulus3delay( 0,800.0F, 400.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 400.0F, 800.0F),
        Stimulus3delay( 0,0.0F, 800.0F, 400.0F),
        Stimulus3delay( 0,800.0F, 0.0F, 400.0F),
        Stimulus3delay( 0,800.0F, 400.0F, 0.0F),
        Stimulus3delay( 0,800.0F, 0.0F, 400.0F),
        Stimulus3delay( 0,0.0F, 800.0F, 400.0F),
        Stimulus3delay( 0,400.0F, 800.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 400.0F, 800.0F),
        Stimulus3delay( 0,400.0F, 0.0F, 800.0F),

        Stimulus3delay( 0,800.0F, 1600.0F, 0.0F),
        Stimulus3delay( 0,800.0F, 0.0F, 1600.0F),
        Stimulus3delay( 0,1600.0F, 800.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 800.0F, 1600.0F),
        Stimulus3delay( 0,0.0F, 1600.0F, 800.0F),
        Stimulus3delay( 0,1600.0F, 0.0F, 800.0F),
        Stimulus3delay( 0,1600.0F, 800.0F, 0.0F),
        Stimulus3delay( 0,1600.0F, 0.0F, 800.0F),
        Stimulus3delay( 0,0.0F, 1600.0F, 800.0F),
        Stimulus3delay( 0,800.0F, 1600.0F, 0.0F),
        Stimulus3delay( 0,0.0F, 800.0F, 1600.0F),
        Stimulus3delay( 0,800.0F, 0.0F, 1600.0F)
    )

    private var allQuestions:MutableList<String> = mutableListOf()
    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle)

    private val nAdaptiveTrials             = 40
    private val adoParams                   = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)
    private val taskADAParams               = TaskADAParams(1200.0F, nAdaptiveTrials)
    private val adoWrapper:AdaptiveWrapper  = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest() {

        when {
            mImageView == null -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }

        allQuestions        = mutableListOf(ctx.resources.getString(R.string.atvb_question_synchro), ctx.resources.getString(R.string.atvb_question_equal), ctx.resources.getString(R.string.atvb_question_whichsecond))
        curISI                  = ISI           // 1000L
        currStimulusDuration    = STIM_DURATION // 50L

        validAnswers        =   if(subject.type == TEST_ATVB_TIME_S_BAL2 || subject.type == TEST_ATVB_TIME_S_BAL)
                                    mutableListOf(ctx.resources.getString(R.string.audio), ctx.resources.getString(R.string.tactile), ctx.resources.getString(R.string.visual))
                                else
                                    mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        mTrialsManager =
            if(subject.trman_type == TEST_TRMAN_FIXED){
                val trials = if(!subject.isDebug) {
                    // create trials/summary
                    when (subject.type) {
                        TEST_ATVB_TIME_S_UNBAL,
                        TEST_ATVB_TIME_D_UNBAL -> {
                            createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                            createTrialsTimeUnbalanced()
                        }

                        TEST_ATVB_TIME_S_BAL,
                        TEST_ATVB_TIME_D_BAL -> {
                            createResultFile(TrialBindingsBalanced.LOG_HEADER)
                            createTrialsTimeBalanced()
                        }
                        else -> throw Exception("ERROR in TEST ATVB")
                    }
                }
                else{
                    createResultFile(TrialBindingsBalanced.LOG_HEADER)
                    createTrialsDebug()
                }
                val ntr = trials.size
                mListBlocks = mutableListOf((ntr *0.25F).roundToInt(), (ntr*0.5F).roundToInt(), (ntr*0.75F).roundToInt())    // define 5 blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)
                FixedTrialsManager(trials as MutableList<TrialBasic>)
            }
            else{
                createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                initSummary()

                val trials = when (subject.type) {
                    TEST_ATVB_TIME_S_UNBAL,
                    TEST_ATVB_TIME_D_UNBAL      -> createTrialsAdaptiveUnbalanced()

                    TEST_ATVB_TIME_S_BAL,
                    TEST_ATVB_TIME_D_BAL        -> createTrialsAdaptiveBalanced()
                    else                        -> throw Exception("ERROR in TEST ATVB")
                }
                AdaptiveTrialsManager(trials as MutableList<TrialBasic>, adoWrapper)
            }
        initSummary()

        mQuestion  = when (subject.type) {
            TEST_ATVB_TIME_S_BAL,
            TEST_ATVB_TIME_S_BAL2   -> allQuestions[2]
            TEST_ATVB_TIME_D_UNBAL,
            TEST_ATVB_TIME_D_BAL    -> allQuestions[1]
            else                    -> allQuestions[0]
        }
        if (subject.whitenoise > TEST_SWITCH_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        mTestLabel      = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        mStimuliManager = StimuliManager(
            AudioManager(STIM_A, audioResources[currStimulusDuration] ?: "t1000hz_50ms.wav", duration = currStimulusDuration, ctx = ctx, handler = mStimuliHandler),
            TactileManager(vibrator!!, duration = currStimulusDuration, handler = mStimuliHandler),
            VisualManager(STIM_V, mImageView!!, mDrawablesResource[1], duration = currStimulusDuration, handler = mStimuliHandler),
            subject.stimuliDelays, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    // [(36 + 3)*2 + 6] * NUM_REPETITIONS(4) = 336
    private fun createTrialsTimeUnbalanced():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {

            val rtrials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
            for (j in 0 until 2) {
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_ATV, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_ATV, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_ATV, 0.0F))

                // 36
                lStimuliUnbalanced.map {
                    rtrials.add(TrialBindingsUnBalanced(++cnt, it.type, it.magnitude))
                }
            }
            rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT_V, unbalSD[6].first))
            rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_AT, unbalSD[6].first))
            rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV_T, unbalSD[6].first))
            rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_AV, unbalSD[6].first))
            rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV_A, unbalSD[6].first))
            rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_TV, unbalSD[6].first))

            rtrials.shuffle()
            trials.addAll(rtrials)
        }
        return trials
    }

    // [18 + 3] * 2 * NUM_REPETITIONS2(8) = 336
    private fun createTrialsTimeBalanced():List<TrialBasic> {
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS_B) {   // NUM_REPETITIONS2
            for (j in 0 until 2) {
                val rtrials: MutableList<TrialBindingsBalanced> = mutableListOf()

                rtrials.add(TrialBindingsBalanced(++cnt, TYPE_ATV, 0.0F,  validAnswers))
                rtrials.add(TrialBindingsBalanced(++cnt, TYPE_ATV, 0.0F,  validAnswers))
                rtrials.add(TrialBindingsBalanced(++cnt, TYPE_ATV, 0.0F,  validAnswers))

                // 18
                lStimuliBalancedShort.map {
                    rtrials.add(TrialBindingsBalanced(++cnt, it.type, it.magnitude, validAnswers))
                }
                rtrials.shuffle()
                trials.addAll(rtrials)
            }
        }
        return trials
    }

    // 44 fixed + 48 adaptive
    private fun createTrialsAdaptiveUnbalanced():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()

        // static part
        // 8
        for (i in 0 until 8) trials.add(TrialBindingsUnBalanced(++cnt, TYPE_ATV, 0.0F))

        // 36
        lStimuliUnbalanced.map {
            trials.add(TrialBindingsUnBalanced(++cnt, it.type, it.magnitude))
        }

        // 48
        for (j in 0 until 8) {
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT_V, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_AT, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV_T, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_AV, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV_A, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_TV, 0.0F, isADA = true))
        }
        trials.shuffle()
        return trials
    }

    // 24 fixed + 32 adaptive
    private fun createTrialsAdaptiveBalanced():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()

        // static part
        // 8
        for (i in 0 until 8) trials.add(TrialBindingsBalanced(++cnt, TYPE_ATV, 0.0F, validAnswers))

        // 16
        lStimuliBalancedShort.map {
            trials.add(TrialBindingsBalanced(++cnt, it.type, it.magnitude, validAnswers))
        }
        // 32
        for (j in 0 until 8) {
            trials.add(TrialBindingsBalanced(++cnt, TYPE_V_A_T, 0.0F, validAnswers, isADA = true))
            trials.add(TrialBindingsBalanced(++cnt, TYPE_T_A_V, 0.0F, validAnswers, isADA = true))
            trials.add(TrialBindingsBalanced(++cnt, TYPE_A_T_V, 0.0F, validAnswers, isADA = true))
            trials.add(TrialBindingsBalanced(++cnt, TYPE_V_T_A, 0.0F, validAnswers, isADA = true))
            trials.add(TrialBindingsBalanced(++cnt, TYPE_A_V_T, 0.0F, validAnswers, isADA = true))
            trials.add(TrialBindingsBalanced(++cnt, TYPE_T_V_A, 0.0F, validAnswers, isADA = true))
        }
        trials.shuffle()
        return trials
    }

    private fun createTrialsDebug():List<TrialBasic>{

//        createResultFile(TrialBindingsBalanced.LOG_HEADER)
//        subject.type = TEST_ATVB_TIME_S_BAL

        createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
        subject.type = TEST_ATVB_TIME_S_UNBAL

        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
//        for (i in 0 until 100000) {
//
//            val trials: MutableList<TrialBindings3latencies> = mutableListOf()
//            for (j in 0 until 2) {
//                trials.add(TrialBindingsBalanced(++cnt, TYPE_A_T_V, 150L, validAnswers))
//                trials.add(TrialBindingsBalanced(++cnt, TYPE_V_A_T, 150L, validAnswers))
//                trials.add(TrialBindingsBalanced(++cnt, TYPE_T_V_A, 150L, validAnswers))
//
//            }
//            mTrials.addAll(trials)
//        }
        for (i in 0 until 100000) {
            for (j in 0 until 2) {
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_ATV, 0.0F))
//                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_TV, 100, validAnswers[0]))
//                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV_A, 100, validAnswers[0]))
//                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_TV, 50, validAnswers[0]))
//                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV_A, 50, validAnswers[0]))
            }
        }
        return trials
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun initSummary(){

        mSummary = when (subject.type) {
            TEST_ATVB_TIME_S_UNBAL,
            TEST_ATVB_TIME_D_UNBAL  ->  ATVBUnBalancedSummary(ctx)
//            TEST_ATVB_TIME_S_BAL,
            TEST_ATVB_TIME_S_BAL   ->  ATVBBalancedSummary(ctx)
            else                    ->  null
        }
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    // get new trial info. start noise. schedule stimulations
    override fun show(trial: TrialBasic, isRepeat: Boolean) {

        if (isRepeat) trial.repetitions++

        mNoise?.start()

        when(subject.type) {

            TEST_ATVB_TIME_S_UNBAL -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    deliverUnBalancedStimuli((trial as TrialBindingsUnBalanced))
                }, WN_FIRSTSTIM_INTERVAL)
            }

            TEST_ATVB_TIME_D_UNBAL -> {
                // to align trimodal stimuli, I have to delay the fastest modality by time_shift ms.
                // Thus I anticipate all main onsets by the same ms
                val corr_delays = subject.stimuliDelays.arrangeDelays(STIM_ATV)
                val shift       = WN_FIRSTSTIM_INTERVAL - corr_delays.shift

                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    mStimuliManager.deliverShiftedStimulus(STIM_ATV, corr_delays.a, corr_delays.t, corr_delays.v) // simult
                }, shift)

                // this second stimuli onset could be improved. I should calculate here the final corrected delay (sum of trial specs & system delay)
                // and adjust  corr_delays.shift accordingly. but here few ms between the two stimuli does not change the task
                mStimuliHandler.postDelayed({
                    deliverUnBalancedStimuli((trial as TrialBindingsUnBalanced))
                }, shift + curISI)
            }

            TEST_ATVB_TIME_S_BAL,TEST_ATVB_TIME_S_BAL2 -> {

                val corr_delays = subject.stimuliDelays.arrangeDelays(STIM_ATV, (trial as TrialBindings3latencies).a, trial.t, trial.v)

                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    mStimuliManager.deliverShiftedStimulus(STIM_ATV, corr_delays.a, corr_delays.t, corr_delays.v){ onStimuliEnd()}
                }, WN_FIRSTSTIM_INTERVAL)
            }

//            TEST_ATVB_TIME_D_BAL -> {
//                val corr_delays = arrangeDelays(0,0,0, subject.stimuliDelay)
//                mStimuliHandler.postDelayed({
//                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
//                    deliverShiftedStimulus(TRIMODAL_AUDIO_CODE, corr_delays.a, corr_delays.t, corr_delays.v) // simult
//                }, WN_FIRSTSTIM_INTERVAL)
//                mStimuliHandler.postDelayed({
//                    deliverShiftedStimulus(TRIMODAL_AUDIO_CODE, (trial as TrialBindings3latencies).a, trial.t, trial.v){ onStimuliEnd()}
//                }, (WN_FIRSTSTIM_INTERVAL + currStimulusDuration + curISI - corr_delays.shift))
//            }
        }
    }

    private fun deliverUnBalancedStimuli(trial:TrialBindingsUnBalanced){

        val corr_delays: CorrectedStimuliDelay = when(trial.type){
            TYPE_ATV    ->  subject.stimuliDelays.arrangeDelays(STIM_ATV)
            TYPE_A_TV   ->  subject.stimuliDelays.arrangeDelays(STIM_ATV, 0, trial.stim_value, trial.stim_value)
            TYPE_TV_A   ->  subject.stimuliDelays.arrangeDelays(STIM_ATV, trial.stim_value,0,0)
            TYPE_T_AV   ->  subject.stimuliDelays.arrangeDelays(STIM_ATV, trial.stim_value,0, trial.stim_value)
            TYPE_AV_T   ->  subject.stimuliDelays.arrangeDelays(STIM_ATV, 0, trial.stim_value,0)
            TYPE_V_AT   ->  subject.stimuliDelays.arrangeDelays(STIM_ATV, trial.stim_value, trial.stim_value,0)
            TYPE_AT_V   ->  subject.stimuliDelays.arrangeDelays(STIM_ATV, 0,0, trial.stim_value)
            else        ->  subject.stimuliDelays.arrangeDelays(STIM_ATV)
        }
        mStimuliManager.deliverShiftedStimulus(STIM_ATV, corr_delays.a, corr_delays.t, corr_delays.v){ onStimuliEnd()}
    }
    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================
}

/*
This App perform an Audio-Tactile-Visual Binding (ATV-B) test:

It has two sub-tests: SINGLE and DOUBLE stimulations.
Each composed by 390 trials.
After every trial, subjects are asked to report whether the three stimulations were simultaneous.

SINGLE:
It is a trimodal version of the Stanley et al 2019 experiment.
Each trial consists in a triple of stimulation modalities (audio, tactile and visual). that can be in synch or reciprocally shifted
each stim last 1 sec, isi=2+   (1 sec stim+delay, one sec rest)

DOUBLE:
Each trial consists in TWO triples of stimulation modalities (audio, tactile and visual).
In the first triple, all three modalities are simultaneous. In the second triple, they can be in synch or reciprocally shifted.
each stim last 1 sec, isi=4+   (1 sec stim, one sec rest, 1 sec stim+delay, one sec rest)
each composed by two consecutive trains of respectively 2 and 2 either audio and/or tactile stimuli (stim duration 2sec, isi=2sec). ITI=2sec.

single trial:
        __
A    __|  |__
         __
T     __|  |__
         __
V     __|  |__

          __
A      __|  |__
         __
T     __|  |__
         __
V     __|  |__


in the second train, one of the two modalities can be in synch with other, delayed/anticipated by 800 ms or absent
in total, there are 5 types of stimuli

CODE    #REP    TYPE
0       6       A,T
3       6       A
6       6       T
7       3       A+800,T
8       3       A,T+800

The presentation order is fixed, 3 repetitions of the following 12 trials:

codes order: 0,3,7,6,3,0,8,6

A,T
A
A+800,T
T
A
A,T
A,T+800
T

Exported Data: trial_id, type



OLD TASK

single trial:
                1st train               2nd train
        __    __    __    __    __  |  __    __    __
A    __|  |__|  |__|  |__|  |__|  |_|_|  |__|  |__|  |____
                                    |
        __    __    __    __    __  |  __    __    __
T    __|  |__|  |__|  |__|  |__|  |_|_|  |__|  |__|  |____
                                    |
                                    |

OLD SCHEMA
CODE    #REP    TYPE
0       6       A,T
1       3       A+200,T
2       3       A,T+200
3       6       A
4       3       A+500,T
5       3       A,T+500
6       6       T
7       3       A+800,T
8       3       A,T+800


A,T
A,T+200
A
A+800,T
T
A,T+500
A
A+200,T
A,T
A,T+800
T
A+500,T
 */


/*

    // 18 different elements: 6 x 3 delays (50, 100, 200)
    // a-t-v, a-v-t, t-a-v, t-v-a, v-a-t, v-t-a
//    private val lStimuliBalancedShort2: List<StimulusDelay> = listOf(
//
//        StimulusDelay( TYPE_V_A_T,50),
//        StimulusDelay( TYPE_T_A_V,50),
//        StimulusDelay( TYPE_V_A_T,100),
//        StimulusDelay( TYPE_T_A_V,100),
//        StimulusDelay( TYPE_V_A_T,200),
//        StimulusDelay( TYPE_T_A_V,200),
//
//        StimulusDelay( TYPE_A_T_V,50),
//        StimulusDelay( TYPE_V_T_A,50),
//        StimulusDelay( TYPE_A_T_V,100),
//        StimulusDelay( TYPE_V_T_A,100),
//        StimulusDelay( TYPE_A_T_V,200),
//        StimulusDelay( TYPE_V_T_A,200),
//
//        StimulusDelay( TYPE_A_V_T,50),
//        StimulusDelay( TYPE_T_V_A,50),
//        StimulusDelay( TYPE_A_V_T,100),
//        StimulusDelay( TYPE_T_V_A,100),
//        StimulusDelay( TYPE_A_V_T,200),
//        StimulusDelay( TYPE_T_V_A,200)
//    )


    // [18 + 3] * 2 * NUM_REPETITIONS2(8) = 336
//    private fun createTrialsTimeBalanced2() {
//        var cnt = -1
//        mTrials = mutableListOf()
//        for (i in 0 until NUM_REPETITIONS_B) {   // NUM_REPETITIONS2
//
//            for (j in 0 until 2) {
//                val trials: MutableList<TrialBindings3latencies> = mutableListOf()
//
//                trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, ""))
//                trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, ""))
//                trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, ""))
//
//                // 18
//                lStimuliBalancedShort2.map {
//                    trials.add(TrialBindingsBalanced(++cnt, it.type, it.delay, validAnswers))
//                }
//                trials.shuffle()
//                mTrials.addAll(trials)
//             }
//        }
//    }

 */