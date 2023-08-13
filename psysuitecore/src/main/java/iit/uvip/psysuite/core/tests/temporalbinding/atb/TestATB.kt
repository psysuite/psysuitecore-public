package iit.uvip.psysuite.core.tests.temporalbinding.atb

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.adaptive.AdaptiveWrapper
import iit.uvip.psysuite.adaptive.TaskADAParams
import iit.uvip.psysuite.adaptive.ado.ADOParams
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.AudioManager
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.StimuliManager.Companion.STIM_TYPE_T1
import iit.uvip.psysuite.core.stimuli.TactileManager
import iit.uvip.psysuite.core.stimuli.VibratorNotDefinedException
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.ISI
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.ISI_INF
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION_INF
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION_TOD
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_AT
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.WN_FIRSTSTIM_INTERVAL
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.unbalSD
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsInfants
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.CorrectedStimuliDelay
import iit.uvip.psysuite.core.utility.StimulusATBInfants
import iit.uvip.psysuite.core.utility.StimulusDelay

import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast

import kotlin.math.roundToInt


class TestATB(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              speechManager: SpeechManager?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, speechManager=speechManager)
{
    override var LOG_TAG:String = TestATB::class.java.simpleName

    companion object {

        @JvmStatic val TEST_BASIC_LABEL         = "ATB"
        @JvmStatic val NUM_REPETITIONS_INFANTS  = 3
        @JvmStatic val NUM_REPETITIONS          = 5

        @JvmStatic val recipients:Array<String> = arrayOf("psysuite.uvip@gmail.com")

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_single)}" , TEST_ATB_TIME_SINGLESTIM          ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_single_tag)}", Populations.hearing_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_double)}" , TEST_ATB_TIME_DOUBLESTIM          ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_double_tag)}", Populations.hearing_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_single_tod)}" , TEST_ATB_TIME_SINGLESTIM_TOD  ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_single_tod_tag)}", Populations.hearing_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_double_tod)}" , TEST_ATB_TIME_DOUBLESTIM_TOD  ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_double_tod_tag)}", Populations.hearing_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_infants)}", TEST_ATB_TIME_INF                 ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_infants_tag)}", Populations.hearing_populations))

        fun getNextTrialModes(ctx:Context):List<List<Int>> = listOf(
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_AUTO, TEST_NEXTTRIAL_BUTTON))

        fun getEmailRecipients():Array<String> = recipients
    }

    private var curISI: Long = 0L

    // stimuli combinations
    private val STIM_TYPE_TIME_A800_T   = 100
    private val STIM_TYPE_TIME_A_T800   = 101

    private val UNIMODAL_AUDIO_CODE     = StimuliManager.STIM_TYPE_A4
    private val BIMODAL_CODE            = StimuliManager.STIM_TYPE_A4T1

    private var allQuestions:MutableList<String> = mutableListOf()

    // 5   different trials
    private val lStimuli: List<StimulusATBInfants> = listOf(
        StimulusATBInfants(BIMODAL_CODE,0),
        StimulusATBInfants(UNIMODAL_AUDIO_CODE, 1),
        StimulusATBInfants(STIM_TYPE_T1, 2),
        StimulusATBInfants(STIM_TYPE_TIME_A_T800,  3),
        StimulusATBInfants(STIM_TYPE_TIME_A800_T,  4)
    )

    // 26 different elements
    private val lStimuliUnBalanced: List<StimulusDelay> = listOf(

        StimulusDelay( TYPE_A_T, unbalSD[0].first),
        StimulusDelay( TYPE_T_A, unbalSD[0].first),
        StimulusDelay( TYPE_A_T, unbalSD[0].first),
        StimulusDelay( TYPE_T_A, unbalSD[0].first),

        StimulusDelay( TYPE_A_T, unbalSD[1].first),
        StimulusDelay( TYPE_T_A, unbalSD[1].first),
        StimulusDelay( TYPE_A_T, unbalSD[1].first),
        StimulusDelay( TYPE_T_A, unbalSD[1].first),

        StimulusDelay( TYPE_A_T, unbalSD[2].first),
        StimulusDelay( TYPE_T_A, unbalSD[2].first),
        StimulusDelay( TYPE_A_T, unbalSD[2].first),
        StimulusDelay( TYPE_T_A, unbalSD[2].first),

        StimulusDelay( TYPE_A_T, unbalSD[3].first),
        StimulusDelay( TYPE_T_A, unbalSD[3].first),
        StimulusDelay( TYPE_A_T, unbalSD[3].first),
        StimulusDelay( TYPE_T_A, unbalSD[3].first),

        StimulusDelay( TYPE_A_T, unbalSD[4].first),
        StimulusDelay( TYPE_T_A, unbalSD[4].first),
        StimulusDelay( TYPE_A_T, unbalSD[4].first),
        StimulusDelay( TYPE_T_A, unbalSD[4].first),

        StimulusDelay( TYPE_A_T, unbalSD[5].first),
        StimulusDelay( TYPE_T_A, unbalSD[5].first),
        StimulusDelay( TYPE_A_T, unbalSD[5].first),
        StimulusDelay( TYPE_T_A, unbalSD[5].first),

        StimulusDelay( TYPE_A_T, unbalSD[6].first),
        StimulusDelay( TYPE_T_A, unbalSD[6].first)
    )
    private val EVENT_SECOND_TRAIN      = 1201

    private val amplitude = 100

    private val nQuestTrials                = 30
    private val adoParams                   = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)
    private val taskADAParams               = TaskADAParams(1200.0F, nQuestTrials+10)
    private val adoWrapper:AdaptiveWrapper  = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

    private var vibration_trains_timings: MutableList<LongArray>    = mutableListOf()
    private var vibration_trains_amplitudes: MutableList<IntArray>  = mutableListOf()

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest() {

        if(vibrator == null)    throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")

        nextTrailModality   = subject.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        allQuestions        = mutableListOf(ctx.resources.getString(R.string.atvb_question_synchro), ctx.resources.getString(R.string.atvb_question_equal))
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        // set stim duration (presently the same in the two subtasks
        when (subject.type) {
            TEST_ATB_TIME_SINGLESTIM ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI           // 1000L
                currStimulusDuration    = STIM_DURATION // 50L
            }
            TEST_ATB_TIME_DOUBLESTIM ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI           // 1000L
                currStimulusDuration    = STIM_DURATION // 50L
            }
            TEST_ATB_TIME_SINGLESTIM_TOD ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI               // 1000L
                currStimulusDuration    = STIM_DURATION_TOD // 200L
            }
            TEST_ATB_TIME_DOUBLESTIM_TOD ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI               // 1000L
                currStimulusDuration    = STIM_DURATION_TOD // 200L
            }
            TEST_ATB_TIME_INF   -> {
                curISI                  = ISI_INF           // 2000L
                currStimulusDuration    = STIM_DURATION_INF // 1000L
            }
        }
        mTrialsManager =
            if(subject.trman_type == TEST_TRMAN_FIXED){
                val trials = if(!subject.isDebug) {
                    // create trials/summary
                    when (subject.type) {
                        TEST_ATB_TIME_DOUBLESTIM_TOD,
                        TEST_ATB_TIME_DOUBLESTIM ->{
                            createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                            initSummary()
                            createTrialsTimeDouble()
                        }
                        TEST_ATB_TIME_SINGLESTIM_TOD,
                        TEST_ATB_TIME_SINGLESTIM       -> {
                            createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                            initSummary()
                            createTrialsTimeSingle()
                        }
                        TEST_ATB_TIME_INF   -> {
                            initTimeArrays()
                            createResultFile(TrialBindingsInfants.LOG_HEADER)
                            createTrialsTimeInfants()
                        }
                        else -> throw Exception("ERROR in TEST ATB")
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
                    TEST_ATB_TIME_DOUBLESTIM_TOD,
                    TEST_ATB_TIME_DOUBLESTIM    -> createTrialsAdaptiveDouble()

                    TEST_ATB_TIME_SINGLESTIM_TOD,
                    TEST_ATB_TIME_SINGLESTIM    -> createTrialsAdaptiveSingle()
                    else                        -> throw Exception("ERROR in TEST ATB")
                }
                val trman = AdaptiveTrialsManager(trials as MutableList<TrialBasic>, adoWrapper)
                trman.getStimulus()
                trman
            }
        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        if (subject.whitenoise > TEST_SWITCH_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        mStimuliManager = StimuliManager(
                AudioManager(UNIMODAL_AUDIO_CODE, audioResources[currStimulusDuration] ?: "t1000hz_50ms.wav", duration = currStimulusDuration, ctx = ctx, handler = mStimuliHandler),
                TactileManager(vibrator, duration = currStimulusDuration, handler = mStimuliHandler),
                null,
                delaysAligner, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }
    //              _   _   _   _   _
    // 9 segments  | |_| |_| |_| |_| |
    private fun initTimeArrays() {
        // init here for readability. will manage amplitudes changes
        vibration_trains_amplitudes = mutableListOf(
            intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
            intArrayOf(amplitude, 0, amplitude, 0, amplitude),
            intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
            intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
            intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude, 0, amplitude)
        )
        vibration_trains_timings = mutableListOf(
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration),
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration),
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration),
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration),
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration + 800L, currStimulusDuration, currStimulusDuration + 800L))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrialsTimeInfants():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS_INFANTS) {
            trials.add(TrialBindingsInfants(++cnt, lStimuli[0].type, lStimuli[0].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[1].type, lStimuli[1].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[4].type, lStimuli[4].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[2].type, lStimuli[2].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[1].type, lStimuli[1].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[0].type, lStimuli[0].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[3].type, lStimuli[3].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[2].type, lStimuli[2].tactile_pattern))
        }
        return trials
    }

    //           at0  a   t  at1200  6latenze
    // 5 x {2 x [ 2 + 2 + 2 + (2 + 4 x 6lat)]} = 5 x 2 x 32 = 320
    // 20 at0 + 20a + 20t + 20at1200 + 40lat6
    private fun createTrialsTimeDouble():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {
            val rtrials: MutableList<TrialBasic> = mutableListOf()
            for (j in 0 until 2) {

                // 6
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0.0F))

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
    //           at0  at1200  6latenze
    // 5 x {2 x [ 2  + (2  +  4 x 6lat)]} = 5 x 2 x 28 = 280
    //        20 at0 + 20at1200 + 40lat6
    private fun createTrialsTimeSingle():List<TrialBasic>{
        var cnt = -1
        val trials: MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {
            val rtrials: MutableList<TrialBasic> = mutableListOf()
            for (j in 0 until 2) {

                // 2
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))

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

    // 22 fixed + 28 adaptive
    private fun createTrialsAdaptiveDouble():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()

        // static part
        // 10
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))

        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0.0F))

        // 12
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 800.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 800.0F))

        // 28
        for (j in 0 until 28) {
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 0.0F, isADA = true))
        }
        trials.shuffle()
        return trials
    }

    // 18 fixed + 32 adaptive
    private fun createTrialsAdaptiveSingle():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()

        // static part
        // 6
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))

        // 12
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 800.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 800.0F))

        // 32
        for (j in 0 until 32) {
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 0.0F, isADA = true))
        }
        trials.shuffle()
        return trials
    }

    private fun createTrialsDebug():List<TrialBasic>{
        var cnt = -1
        val trials: MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until 100000) {
            for (j in 0 until 2) {
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_AT, 0.0F))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_A_T, 50.0F))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_A, 50.0F))
            }
        }
        return trials
    }
    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onEndTrial(prev_result: Int, elapsed: Int, extra_text:String){
        testEvent.accept(Triple(EVENT_UPDATE_TRIAL_ID, 0L, listOf()))
        return super.onEndTrial(prev_result, elapsed, extra_text)
    }

    // called by secondTrain
    override fun onTrialEnd(){

        mNoise?.stop()
        mNoise?.prepare()

        when (nextTrailModality) {
            TEST_NEXTTRIAL_BUTTON       ->  testEvent.accept(Triple(EVENT_SHOW_NEXT_BUTTON, null, listOf()))
            TEST_NEXTTRIAL_AUTO         ->  {
                // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_SHOW_ABORT, 1000L, listOf()))
                }, currStimulusDuration)
            }
            TEST_NEXTTRIAL_VOICE_ANSWER         ->  testEvent.accept(Triple(EVENT_GIVE_VOCAL_ANSWER, null, listOf()))
            TEST_NEXTTRIAL_ANSWER               ->  testEvent.accept(Triple(EVENT_GIVE_ANSWER, null, listOf()))
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER  -> {
                                                    testEvent.accept(Triple(EVENT_GIVE_VOCAL_ANSWER, null, listOf()))
                                                    testEvent.accept(Triple(EVENT_GIVE_ANSWER, null, listOf()))
                                                   }
        }
    }

    override fun initSummary(){

        mSummary = when (subject.type) {
            TEST_ATB_TIME_DOUBLESTIM,
            TEST_ATB_TIME_SINGLESTIM,
            TEST_ATB_TIME_DOUBLESTIM_TOD,
            TEST_ATB_TIME_SINGLESTIM_TOD    ->  ATBUnBalancedSummary(ctx)

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

            TEST_ATB_TIME_INF -> {
                mStimuliHandler.postDelayed({
                    firstTrain((trial as TrialBindingsInfants).tactile_pattern)     // schedule first 3 stimuli
                    secondTrain(trial.type)    // schedule second 2 stimuli
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_ATB_TIME_SINGLESTIM,
            TEST_ATB_TIME_SINGLESTIM_TOD -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced)
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_ATB_TIME_DOUBLESTIM,
            TEST_ATB_TIME_DOUBLESTIM_TOD -> {

                // since I have to apply the possible shift, I calculate here the correction and thus call deliverShiftedStimulus for the 1st stim.
                // for the second I call instead deliverUnBalancedStimuli
                // to preserve the desired ISI between 1st and 2nd stimuli,
                // I also add the shift that could be eventually imposed to the fastest modality
                val corr_delays = delaysAligner.arrangeDelays(BIMODAL_CODE, 0,0,-1)
                val shift       = WN_FIRSTSTIM_INTERVAL - corr_delays.shift

                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    mStimuliManager.deliverShiftedStimulus(BIMODAL_CODE, corr_delays.a, corr_delays.t, corr_delays.v) // simult
                }, shift)
                mStimuliHandler.postDelayed({
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced)
                }, shift + curISI)
            }
        }
    }

    // tactile are programmed once, audio are programmed with postDelayed
    private fun firstTrain(tactile_pattern: Int) {

        // assuming audio is faster than vibro, I delay the former
        var A_delay     = delaysAligner.getStimuliDelay(BIMODAL_CODE).t - delaysAligner.getStimuliDelay(BIMODAL_CODE).a
        val timings     = vibration_trains_timings[tactile_pattern]

        if(A_delay < 0L){  // audio delayed wrt vibro: delay vibro timings and preserve audio onsets
            vibration_trains_timings[tactile_pattern].mapIndexed { index, it -> timings[index] = it - A_delay }
            A_delay = 0
        }

        vibrator?.vibratePattern(timings, vibration_trains_amplitudes[tactile_pattern])

        if(A_delay > 0L){
            mStimuliHandler.postDelayed({
                mStimuliManager.deliverUnimodalStimulus(UNIMODAL_AUDIO_CODE)
                testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
            }, A_delay)
        }
        else {
            mStimuliManager.deliverUnimodalStimulus(UNIMODAL_AUDIO_CODE)
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
        }

        mStimuliHandler.postDelayed({   mStimuliManager.deliverUnimodalStimulus(UNIMODAL_AUDIO_CODE)    }, curISI + A_delay)

        mStimuliHandler.postDelayed({   mStimuliManager.deliverUnimodalStimulus(UNIMODAL_AUDIO_CODE)    }, 2*curISI + A_delay)
    }

    // only for infants subtest
    // tactile have been already programmed at the beginning of the trial => just playback audio and take care of events
    private fun secondTrain(type:Int){

        // assuming audio is faster than vibro, I delay the former
        var A_delay    = delaysAligner.getStimuliDelay(BIMODAL_CODE).t - delaysAligner.getStimuliDelay(BIMODAL_CODE).a
        if(A_delay < 0L)   A_delay = 0L    // audio delayed wrt vibro: I previoulsy delayed vibro timings and now I preserve audio

        when(type){
            BIMODAL_CODE,
            UNIMODAL_AUDIO_CODE,
            STIM_TYPE_TIME_A_T800   -> {
                mStimuliHandler.postDelayed({
                    mStimuliManager.deliverUnimodalStimulus(UNIMODAL_AUDIO_CODE)
                    testEvent.accept(Triple(EVENT_SECOND_TRAIN, null, listOf()))
                }, 3 * curISI + A_delay)
                mStimuliHandler.postDelayed({
                    mStimuliManager.deliverUnimodalStimulus(UNIMODAL_AUDIO_CODE)
                }, 4 * curISI + A_delay)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI + A_delay)
            }

            STIM_TYPE_T1 -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_SECOND_TRAIN, null, listOf()))
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI)
            }

            STIM_TYPE_TIME_A800_T -> {
                mStimuliHandler.postDelayed({
                    mStimuliManager.deliverUnimodalStimulus(UNIMODAL_AUDIO_CODE)
                    testEvent.accept(Triple(EVENT_SECOND_TRAIN, null, listOf()))
                }, (3 * curISI + 800L + A_delay))
                mStimuliHandler.postDelayed({
                    mStimuliManager.deliverUnimodalStimulus(UNIMODAL_AUDIO_CODE)
                    testEvent.accept(Triple(EVENT_SECOND_TRAIN, null, listOf()))
                }, (4 * curISI + 800 + A_delay))
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (5 * curISI + 800L + A_delay))
            }
        }
    }

    private fun deliverUnBalancedStimuli(trial:TrialBindingsUnBalanced){

        var type = 0
        val corr_delays: CorrectedStimuliDelay = when(trial.type) {
            TYPE_AT     -> {
                type = mStimuliManager.typeAT
                delaysAligner.arrangeDelays(type, 0,0, -1)
            }
            TYPE_A      -> {
                type = mStimuliManager.typeA
                CorrectedStimuliDelay(0, -1, -1)
            }
            TYPE_T      -> {
                type = mStimuliManager.typeT
                CorrectedStimuliDelay(-1, 0, -1)
            }
            TYPE_A_T    -> {
                type = mStimuliManager.typeAT
                delaysAligner.arrangeDelays(type, 0,trial.stim_value, -1)
            }
            TYPE_T_A    -> {
                type = mStimuliManager.typeAT
                delaysAligner.arrangeDelays(type, trial.stim_value,0, -1)
            }
            else        -> {
                type = mStimuliManager.typeAT
                CorrectedStimuliDelay(0, 0, -1)
            }
        }
        mStimuliManager.deliverShiftedStimulus(type, corr_delays.a, corr_delays.t, corr_delays.v){ onTrialEnd()}
    }
    // =============================================================================================================================
}

/*
This App perform an Audio-Tactile Binding (ATB) test:

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
