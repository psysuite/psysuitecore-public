package iit.uvip.psysuite.core.tests.temporalbinding.avb

import android.app.Activity
import android.content.Context
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
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.ISI
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.ISI_INF
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION_INF
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION_TOD
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_AV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.WN_FIRSTSTIM_INTERVAL
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.unbalSD
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsInfants
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.CorrectedStimuliDelay
import iit.uvip.psysuite.core.utility.StimulusDelay
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import kotlin.math.roundToInt


class TestAVB(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              mImageView: ImageView?,
              speechManager: SpeechManager?
) : TestBasic(ctx, activity, hostfragment, subject, mImageView = mImageView, speechManager=speechManager)
{
    override var LOG_TAG:String = TestAVB::class.java.simpleName

    companion object {
        // Overrides
        @JvmStatic val TEST_BASIC_LABEL         = "AVB"

        // Test-specific repetitions
        @JvmStatic val NUM_REPETITIONS_INFANTS  = 3
        @JvmStatic val NUM_REPETITIONS          = 5

        // Email configuration
        @JvmStatic val recipients:Array<String> = arrayOf("psysuite.uvip@gmail.com")

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_single)}" , TEST_AVB_TIME_SINGLESTIM          ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_single_tag)}", Populations.sighted_hearing_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_double)}" , TEST_AVB_TIME_DOUBLESTIM          ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_double_tag)}", Populations.sighted_hearing_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_single_tod)}" , TEST_AVB_TIME_SINGLESTIM_TOD  ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_single_tod_tag)}", Populations.sighted_hearing_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_double_tod)}" , TEST_AVB_TIME_DOUBLESTIM_TOD  ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_double_tod_tag)}", Populations.sighted_hearing_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_infants)}", TEST_AVB_TIME_INF                 ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_infants_tag)}", Populations.sighted_hearing_populations))

        fun getNextTrialModes(ctx:Context):List<List<Int>> = listOf(
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_AUTO, TEST_NEXTTRIAL_BUTTON))

        fun getEmailRecipients():Array<String> = recipients
    }

    private var curISI: Long = 0L

    private var allQuestions:MutableList<String> = mutableListOf()
    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle)

    // 26 different elements
    private val lStimuliUnBalanced: List<StimulusDelay> = listOf(

        StimulusDelay( TYPE_A_V, unbalSD[0].first),
        StimulusDelay( TYPE_V_A, unbalSD[0].first),
        StimulusDelay( TYPE_A_V, unbalSD[0].first),
        StimulusDelay( TYPE_V_A, unbalSD[0].first),

        StimulusDelay( TYPE_A_V, unbalSD[1].first),
        StimulusDelay( TYPE_V_A, unbalSD[1].first),
        StimulusDelay( TYPE_A_V, unbalSD[1].first),
        StimulusDelay( TYPE_V_A, unbalSD[1].first),

        StimulusDelay( TYPE_A_V, unbalSD[2].first),
        StimulusDelay( TYPE_V_A, unbalSD[2].first),
        StimulusDelay( TYPE_A_V, unbalSD[2].first),
        StimulusDelay( TYPE_V_A, unbalSD[2].first),

        StimulusDelay( TYPE_A_V, unbalSD[3].first),
        StimulusDelay( TYPE_V_A, unbalSD[3].first),
        StimulusDelay( TYPE_A_V, unbalSD[3].first),
        StimulusDelay( TYPE_V_A, unbalSD[3].first),

        StimulusDelay( TYPE_A_V, unbalSD[4].first),
        StimulusDelay( TYPE_V_A, unbalSD[4].first),
        StimulusDelay( TYPE_A_V, unbalSD[4].first),
        StimulusDelay( TYPE_V_A, unbalSD[4].first),

        StimulusDelay( TYPE_A_V, unbalSD[5].first),
        StimulusDelay( TYPE_V_A, unbalSD[5].first),
        StimulusDelay( TYPE_A_V, unbalSD[5].first),
        StimulusDelay( TYPE_V_A, unbalSD[5].first),

        StimulusDelay( TYPE_A_V, unbalSD[6].first),
        StimulusDelay( TYPE_V_A, unbalSD[6].first)
    )

    private val EVENT_SECOND_TRAIN          = 1201

    private val nAdaptiveTrials             = 88
    private val adoParams                   = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)
    private val taskADAParams               = TaskADAParams(1200.0F, nAdaptiveTrials)
    private val adoWrapper:AdaptiveWrapper  = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest() {

        if (mImageView == null) throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")

        allQuestions        = mutableListOf(ctx.resources.getString(R.string.atvb_question_synchro), ctx.resources.getString(R.string.atvb_question_equal))
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        // set stim duration (presently the same in the two subtasks
        when (subject.type) {
            TEST_AVB_TIME_SINGLESTIM ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI           // 1000L
                currStimulusDuration    = STIM_DURATION // 50L
            }
            TEST_AVB_TIME_DOUBLESTIM ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI           // 1000L
                currStimulusDuration    = STIM_DURATION // 50L
            }
            TEST_AVB_TIME_SINGLESTIM_TOD ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI               // 1000L
                currStimulusDuration    = STIM_DURATION_TOD // 200L
            }
            TEST_AVB_TIME_DOUBLESTIM_TOD ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI               // 1000L
                currStimulusDuration    = STIM_DURATION_TOD // 200L
            }
            TEST_AVB_TIME_INF   -> {
                curISI                  = ISI_INF           // 2000L
                currStimulusDuration    = STIM_DURATION_INF // 1000L
            }
        }
        mTrialsManager =
            if(subject.trman_type == TEST_TRMAN_FIXED){
                val trials = if(!subject.isDebug) {
                    // create trials/summary
                    when (subject.type) {
                        TEST_AVB_TIME_DOUBLESTIM_TOD,
                        TEST_AVB_TIME_DOUBLESTIM ->{
                            createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                            initSummary()
                            createTrialsTimeDouble()
                        }
                        TEST_AVB_TIME_SINGLESTIM_TOD,
                        TEST_AVB_TIME_SINGLESTIM       -> {
                            createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                            initSummary()
                            createTrialsTimeSingle()
                        }
                        TEST_AVB_TIME_INF   -> {
                            createResultFile(TrialBindingsInfants.LOG_HEADER)
                            createTrialsTimeInfants()
                        }
                        else -> throw Exception("ERROR in TEST AVB")
                    }
                }
                else{
                    createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                    createTrialsDebug()
                }
                val ntr = trials.size
                mListBlocks = mutableListOf((ntr *0.2F).roundToInt(), (ntr * 0.4F).roundToInt(), (ntr * 0.6F).roundToInt(), (ntr * 0.8F).roundToInt())    // define 5 blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)
                FixedTrialsManager(trials as MutableList<TrialBasic>)
            }
            else{
                createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                initSummary()

                val trials = when (subject.type) {
                    TEST_AVB_TIME_DOUBLESTIM_TOD,
                    TEST_AVB_TIME_DOUBLESTIM    -> createTrialsAdaptiveDouble()

                    TEST_AVB_TIME_SINGLESTIM_TOD,
                    TEST_AVB_TIME_SINGLESTIM    -> createTrialsAdaptiveSingle()
                    else                        -> throw Exception("ERROR in TEST AVB")
                }
                AdaptiveTrialsManager(trials as MutableList<TrialBasic>, adoWrapper)
            }

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx) // TODO: manage this case

        if (subject.whitenoise > TEST_SWITCH_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        mStimuliManager = StimuliManager(
            AudioManager(STIM_A, audioResources[currStimulusDuration] ?: "t1000hz_50ms.wav", duration = currStimulusDuration, ctx = ctx, handler = mStimuliHandler),
            null,
            VisualManager(STIM_V, mImageView, mDrawablesResource[1], duration = currStimulusDuration, handler = mStimuliHandler),
            subject.stimuliDelays, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrialsTimeInfants():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS_INFANTS) {

            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 800.0F))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 800.0F))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))
        }
        return trials
    }

    // [(4x2) x 6lat + 4 + 4 + 4 + 4] = 64
    private fun createTrialsTimeDouble():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {
            val rtrials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
            for (j in 0 until 2) {

                // 6
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_A,0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_V,0.0F))

                // 26
                lStimuliUnBalanced.map {
                    rtrials.add(TrialBindingsUnBalanced(++cnt, it.type, it.magnitude))
                }
            }
            rtrials.shuffle()
            trials.addAll(rtrials)
        }
        return trials
    }

    // only-A & only-T were removed in single stimulus sub-task. 7/8/2020
    // trials reduced from 280 -> 140 for psysuite 2 paper. 17/7/2025
    private fun createTrialsTimeSingle():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {
            val rtrials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
//            for (j in 0 until 2) {

            // 2
            rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
            rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))

            // 26
            lStimuliUnBalanced.map {
                rtrials.add(TrialBindingsUnBalanced(++cnt, it.type, it.magnitude))
            }
//            }
            rtrials.shuffle()
            trials.addAll(rtrials)
        }
        return trials
    }

    // 22 fixed + 28 adaptive
    private fun createTrialsAdaptiveDouble():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()

        // static part
        // 10
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))

        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))

        // 12
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 800.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 800.0F))

        // 28
        for (j in 0 until 28) {
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 0.0F, isADA = true))
        }
        trials.shuffle()
        return trials
    }

    // 18 fixed + 64 adaptive = 82 trials
    private fun createTrialsAdaptiveSingle():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()

        // static part
        // 6
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))

        // 12
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 800.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 800.0F))

        // 64
        for (j in 0 until 32) {
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 0.0F, isADA = true))
        }
        trials.shuffle()
        return trials
    }

    private fun createTrialsDebug():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until 100000) {

            val rtrials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
            for (j in 0 until 2) {
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AV, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_V, 50.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_A, 50.0F))
            }
            trials.addAll(rtrials)
        }
        return trials
    }


    override fun initSummary(){

        mSummary = when (subject.type) {
            TEST_AVB_TIME_DOUBLESTIM,
            TEST_AVB_TIME_SINGLESTIM,
            TEST_AVB_TIME_DOUBLESTIM_TOD,
            TEST_AVB_TIME_SINGLESTIM_TOD    ->  AVBUnBalancedSummary(ctx)

            else                            ->  null
        }
    }
    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        if(isRepeat)    trial.repetitions++

        mNoise?.start()

        when(subject.type) {

            TEST_AVB_TIME_INF ->    deliverInfants(trial as TrialBindingsUnBalanced)

            TEST_AVB_TIME_SINGLESTIM,
            TEST_AVB_TIME_SINGLESTIM_TOD -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced){ onStimuliEnd() }
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_AVB_TIME_DOUBLESTIM,
            TEST_AVB_TIME_DOUBLESTIM_TOD -> {

                // since I have to apply the possible shift, I calculate here the correction and thus call deliverShiftedStimulus for the 1st stim.
                // for the second I call instead deliverUnBalancedStimuli
                val corr_delays = subject.stimuliDelays.arrangeDelays(STIM_AV, 0,-1,0) //arrangeDelays(0,0,-1, subject.stimuliDelay)
                val shift       = WN_FIRSTSTIM_INTERVAL - corr_delays.shift

                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    mStimuliManager.deliverShiftedStimulus(
                        STIM_AV,
                        corr_delays.a,
                        corr_delays.t,
                        corr_delays.v
                    ) // simult
                }, shift)
                mStimuliHandler.postDelayed({
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced){ onStimuliEnd() }
                }, shift + curISI)     // to preserve the desired ISI between 1st and 2nd stimuli,
                                                                                                    // I also add the shift that could be eventually imposed to the fastest modality
            }
        }
    }

    private fun deliverInfants(trial:TrialBindingsUnBalanced) {

        // since I have to apply the possible shift, I calculate here the correction and thus call deliverShiftedStimulus for the 1st stim.
        // to preserve the desired ISI between stimuli, I also subtract the positive shift that could be eventually imposed to the fastest modality

        val corr_delays = subject.stimuliDelays.arrangeDelays(STIM_AV, 0,-1,0) //arrangeDelays(0,0,-1, subject.stimuliDelay)
        val shift       = WN_FIRSTSTIM_INTERVAL - corr_delays.shift
        // first train
        mStimuliHandler.postDelayed({
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
            mStimuliManager.deliverShiftedStimulus(
                STIM_AV,
                corr_delays.a,
                corr_delays.t,
                corr_delays.v
            ) // simult
        }, shift)

        mStimuliHandler.postDelayed({
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
            mStimuliManager.deliverShiftedStimulus(
                STIM_AV,
                corr_delays.a,
                corr_delays.t,
                corr_delays.v
            ) // simult
        }, shift + curISI)

        mStimuliHandler.postDelayed({
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
            mStimuliManager.deliverShiftedStimulus(
                STIM_AV,
                corr_delays.a,
                corr_delays.t,
                corr_delays.v
            ) // simult
        }, shift + 2*curISI)

        // second train
        mStimuliHandler.postDelayed({
            deliverUnBalancedStimuli(trial)
        }, shift + 3*curISI)

        mStimuliHandler.postDelayed({
            deliverUnBalancedStimuli(trial)
        }, shift + 4*curISI)

        mStimuliHandler.postDelayed({
            onStimuliEnd()
        }, shift + 5*curISI)
    }

    private fun deliverUnBalancedStimuli(trial:TrialBindingsUnBalanced, onEnd:() -> Unit = {}){

        var type: Int
        val corr_delays: CorrectedStimuliDelay = when(trial.type) {
            TYPE_AV     -> {
                type = mStimuliManager.typeAV
                subject.stimuliDelays.arrangeDelays(type, 0,-1, 0)
            }
            TYPE_A      -> {
                type = mStimuliManager.typeA
                CorrectedStimuliDelay(0, -1, -1)
            }
            TYPE_V      -> {
                type = mStimuliManager.typeV
                CorrectedStimuliDelay(-1, -1, 0)
            }
            TYPE_A_V    -> {
                type = mStimuliManager.typeAV
                subject.stimuliDelays.arrangeDelays(type, 0,-1, trial.stim_value)
            }
            TYPE_V_A    -> {
                type = mStimuliManager.typeAV
                subject.stimuliDelays.arrangeDelays(type, trial.stim_value,-1, 0)
            }
            else        -> {
                type = mStimuliManager.typeAV
                CorrectedStimuliDelay(0, -1, 0)
            }
        }
        mStimuliManager.deliverShiftedStimulus(type, corr_delays.a, corr_delays.t, corr_delays.v){ onEnd()}
    }
    // =============================================================================================================================
}

/*
This App perform an Audio-Visual Binding (AVB) test:

It has two versions: infant and children/adults


1) INFANT:

It has one single experimental condition composed by 24 trials (with fixed scheme!).
Each trial consists in a pair of stimulation modalities (audio and tactle) each composed by two consecutive trains of respectively 3 and 2 either audio and/or tactile stimuli (stim duration 1sec, isi=1sec). ITI=2sec.

single trial:
       1st train    2nd train
        ___   __    __  |  __    __
A    __|  |__|  |__|  |_|_|  |__|  |__
                        |
        __    __    __  |  __    __
T    __|  |__|  |__|  |_|_|  |__|  |__
                        |
                        |

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

2) CHILDREN / ADULTS

single trial:

        __  | __
A    __|  |_|_|  |__
            |
        __  |  __
T    __|  |_|_|  |__
            |

CODE    #REP    TYPE
0       10       A,T
3       10       A
6       10       T
7       5       A+100,T
8       5       A,T+100
7       5       A+200,T
8       5       A,T+200
7       5       A+300,T
8       5       A,T+300
7       5       A+400,T
8       5       A,T+400
7       5       A+800,T
8       5       A,T+800

Tot trials = 80

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

7       3       A+1200,T
8       3       A,T+1200


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
