package iit.uvip.psysuite.core.tests.temporalbinding.tvb

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
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.ISI
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.ISI_INF
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION_INF
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_DURATION_TOD
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_TYPE_TIME_T800_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.STIM_TYPE_TIME_T_V800
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_TV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.WN_FIRSTSTIM_INTERVAL
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.unbalSD
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsInfants
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.CorrectedStimuliDelay
import iit.uvip.psysuite.core.utility.StimulusATBInfants
import iit.uvip.psysuite.core.utility.StimulusDelay
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import kotlin.math.roundToInt

/**
 * Manages the Tactile-Visual Binding (TVB) test.
 * This test assesses the temporal binding of tactile and visual stimuli by presenting them
 * at varying offsets and asking the subject to judge their synchrony or temporal order.
 * It supports various sub-tasks including single stimulus timing, double stimulus timing,
 * and an infant-specific version.
 * The test can be run in fixed or adaptive mode for trial management.
 *
 * @param ctx The application context.
 * @param activity The hosting activity.
 * @param hostfragment The hosting fragment.
 * @param subject The subject details.
 * @param vibrator The vibration manager for tactile stimuli.
 * @param mImageView The ImageView for displaying visual stimuli.
 * @param speechManager The speech manager for auditory feedback or instructions.
 */
class TestTVB(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?,
              mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView)
{
    /**
     * Tag for logging purposes, specific to this class.
     */
    override var LOG_TAG:String = TestTVB::class.java.simpleName

    /**
     * Companion object for TestTVB, holding constants and static methods.
     */
    companion object {
        /**
         * Label for this specific test, "TVB".
         */
        @JvmStatic val TEST_BASIC_LABEL         = "TVB"

        /**
         * Number of repetitions for the infant version of the test.
         */
        @JvmStatic val NUM_REPETITIONS_INFANTS  = 3
        /**
         * Standard number of repetitions for the test.
         */
        @JvmStatic val NUM_REPETITIONS          = 5

        /**
         * Default email recipients for sending test results.
         */
        @JvmStatic val recipients:Array<String> = arrayOf("psysuite.uvip@gmail.com")

        /**
         * Provides a list of available conditions for the TVB test.
         * Each condition has a label, an ID, a tag, and target populations.
         *
         * @param ctx The application context for accessing resources.
         * @return A list of [ConditionData] objects representing the test conditions.
         */
        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_single)}" , TEST_TVB_TIME_SINGLESTIM          ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_single_tag)}", Populations.sighted_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_double)}" , TEST_TVB_TIME_DOUBLESTIM          ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_double_tag)}", Populations.sighted_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_single_tod)}" , TEST_TVB_TIME_SINGLESTIM_TOD  ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_single_tod_tag)}", Populations.sighted_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_double_tod)}" , TEST_TVB_TIME_DOUBLESTIM_TOD  ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_double_tod_tag)}", Populations.sighted_populations),
            ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_infants)}", TEST_ATB_TIME_INF                 ,"${TEST_BASIC_LABEL}${ctx.resources.getString(R.string.atb_subtask_time_infants_tag)}", Populations.sighted_populations))

        /**
         * Defines the available modes for advancing to the next trial for each condition.
         *
         * @param ctx The application context (currently unused but good practice to include).
         * @return A list of lists, where each inner list contains allowed next trial modes for a corresponding condition.
         */
        fun getNextTrialModes(ctx:Context):List<List<Int>> = listOf(
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
            listOf(TEST_NEXTTRIAL_AUTO, TEST_NEXTTRIAL_BUTTON))

        /**
         * Retrieves the default email recipients for the test results.
         *
         * @return An array of email addresses.
         */
        fun getEmailRecipients():Array<String> = recipients
    }

    /**
     * Current Inter-Stimulus Interval (ISI) in milliseconds.
     */
    private var curISI: Long = 0L

    /**
     * List of questions to be asked during the test.
     */
    private var allQuestions:MutableList<String>        = mutableListOf()
    /**
     * List of drawable resources used for visual stimuli.
     * Typically includes a baseline (e.g., white circle) and an active stimulus (e.g., blue circle).
     */
    override var mDrawablesResource: MutableList<Int>   = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle)

    /**
     * List of stimuli configurations for the infant version of the TVB test.
     * Each [StimulusATBInfants] defines the type of stimulus and its associated index.
     */
    // 5   different trials
    private val lStimuli: List<StimulusATBInfants> = listOf(
        StimulusATBInfants(STIM_TV, 0),
        StimulusATBInfants(STIM_T, 1),
        StimulusATBInfants(STIM_V, 2),
        StimulusATBInfants(STIM_TYPE_TIME_T_V800, 3),
        StimulusATBInfants(STIM_TYPE_TIME_T800_V, 4)
    )

    /**
     * List of stimuli configurations for the unbalanced version of the TVB test.
     * Each [StimulusDelay] defines the type of stimuli pair (e.g., Tactile then Visual) and the delay between them.
     */
    // 26 different elements
    private val lStimuliUnBalanced: List<StimulusDelay> = listOf(

        StimulusDelay( TYPE_T_V, unbalSD[0].first),
        StimulusDelay( TYPE_V_T, unbalSD[0].first),
        StimulusDelay( TYPE_T_V, unbalSD[0].first),
        StimulusDelay( TYPE_V_T, unbalSD[0].first),

        StimulusDelay( TYPE_T_V, unbalSD[1].first),
        StimulusDelay( TYPE_V_T, unbalSD[1].first),
        StimulusDelay( TYPE_T_V, unbalSD[1].first),
        StimulusDelay( TYPE_V_T, unbalSD[1].first),

        StimulusDelay( TYPE_T_V, unbalSD[2].first),
        StimulusDelay( TYPE_V_T, unbalSD[2].first),
        StimulusDelay( TYPE_T_V, unbalSD[2].first),
        StimulusDelay( TYPE_V_T, unbalSD[2].first),

        StimulusDelay( TYPE_T_V, unbalSD[3].first),
        StimulusDelay( TYPE_V_T, unbalSD[3].first),
        StimulusDelay( TYPE_T_V, unbalSD[3].first),
        StimulusDelay( TYPE_V_T, unbalSD[3].first),

        StimulusDelay( TYPE_T_V, unbalSD[4].first),
        StimulusDelay( TYPE_V_T, unbalSD[4].first),
        StimulusDelay( TYPE_T_V, unbalSD[4].first),
        StimulusDelay( TYPE_V_T, unbalSD[4].first),

        StimulusDelay( TYPE_T_V, unbalSD[5].first),
        StimulusDelay( TYPE_V_T, unbalSD[5].first),
        StimulusDelay( TYPE_T_V, unbalSD[5].first),
        StimulusDelay( TYPE_V_T, unbalSD[5].first),

        StimulusDelay( TYPE_T_V, unbalSD[6].first),
        StimulusDelay( TYPE_V_T, unbalSD[6].first)
    )

    /**
     * Event code for the second train of stimuli (used in some TVB sub-tasks).
     */
    private val EVENT_SECOND_TRAIN          = 1201

    /**
     * Amplitude for tactile vibration stimuli (0-255).
     */
    private val amplitude                   = 100

    /**
     * Number of trials for adaptive test procedures.
     */
    private val nAdaptiveTrials             = 40
    /**
     * Parameters for the ADO (Adaptive Design Optimization) algorithm.
     * Includes guess rate, lapse rate, and noise percentage.
     */
    private val adoParams                   = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)
    /**
     * Task-specific parameters for adaptive procedures, including the maximum value and number of trials.
     */
    private val taskADAParams               = TaskADAParams(1200.0F, nAdaptiveTrials)
    /**
     * Wrapper for the adaptive algorithm (AdopyWrapper).
     */
    private val adoWrapper:AdaptiveWrapper  = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

    /**
     * Stores timings for vibration trains, used in the infant version.
     * Each LongArray represents the on/off durations for a vibration sequence.
     */
    private var vibration_trains_timings: MutableList<LongArray>    = mutableListOf()
    /**
     * Stores amplitudes for vibration trains, used in the infant version.
     * Each IntArray corresponds to the amplitudes for the timings in [vibration_trains_timings].
     */
    private var vibration_trains_amplitudes: MutableList<IntArray>  = mutableListOf()

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    /**
     * Initializes the TVB test based on the subject's parameters and selected sub-task.
     * Sets up trial managers (fixed or adaptive), loads stimuli, initializes questions,
     * and configures the [StimuliManager].
     *
     * @throws ImageViewDefinedException if the ImageView for visual stimuli is not provided.
     * @throws VibratorNotDefinedException if the VibrationManager for tactile stimuli is not provided.
     * @throws Exception if an unknown test type is specified.
     */
    override fun initTest() {

        when {
            mImageView == null -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }

        allQuestions        = mutableListOf(ctx.resources.getString(R.string.atvb_question_synchro), ctx.resources.getString(R.string.atvb_question_equal))
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        // set stim duration (presently the same in the two subtasks
        when (subject.type) {
            TEST_TVB_TIME_SINGLESTIM ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI           // 1000L
                currStimulusDuration    = STIM_DURATION // 50L
            }
            TEST_TVB_TIME_DOUBLESTIM ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI           // 1000L
                currStimulusDuration    = STIM_DURATION // 50L
            }
            TEST_TVB_TIME_SINGLESTIM_TOD ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI               // 1000L
                currStimulusDuration    = STIM_DURATION_TOD // 200L
            }
            TEST_TVB_TIME_DOUBLESTIM_TOD ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI               // 1000L
                currStimulusDuration    = STIM_DURATION_TOD // 200L
            }
            TEST_TVB_TIME_INF   -> {
                curISI                  = ISI_INF           // 2000L
                currStimulusDuration    = STIM_DURATION_INF // 1000L
            }
        }

        mTrialsManager =
            if(subject.trman_type == TEST_TRMAN_FIXED){
                val trials = if(!subject.isDebug) {
                    // create trials/summary
                    when (subject.type) {
                        TEST_TVB_TIME_DOUBLESTIM_TOD,
                        TEST_TVB_TIME_DOUBLESTIM ->{
                            createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                            initSummary()
                            createTrialsTimeDouble()
                        }
                        TEST_TVB_TIME_SINGLESTIM_TOD,
                        TEST_TVB_TIME_SINGLESTIM       -> {
                            createResultFile(TrialBindingsUnBalanced.LOG_HEADER)
                            initSummary()
                            createTrialsTimeSingle()
                        }
                        TEST_TVB_TIME_INF   -> {
                            initTimeArrays()
                            createResultFile(TrialBindingsInfants.LOG_HEADER)
                            createTrialsTimeInfants()
                        }
                        else -> throw Exception("ERROR in TEST TVB")
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
                    TEST_TVB_TIME_DOUBLESTIM_TOD,
                    TEST_TVB_TIME_DOUBLESTIM    -> createTrialsAdaptiveDouble()

                    TEST_TVB_TIME_SINGLESTIM_TOD,
                    TEST_TVB_TIME_SINGLESTIM    -> createTrialsAdaptiveSingle()
                    else                        -> throw Exception("ERROR in TEST AVB")
                }
                AdaptiveTrialsManager(trials as MutableList<TrialBasic>, adoWrapper)
            }


        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        if (subject.whitenoise > TEST_SWITCH_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        mStimuliManager = StimuliManager(null,
            TactileManager(vibrator!!, duration = currStimulusDuration, handler = mStimuliHandler),
            VisualManager(STIM_V, mImageView!!, mDrawablesResource[1], duration = currStimulusDuration, handler = mStimuliHandler),
            subject.stimuliDelays, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }
    /**
     * Initializes the timing arrays for vibration trains used in the infant version of the TVB test.
     * Defines patterns of vibrations (on/off durations) with a constant amplitude.
     * These arrays ([vibration_trains_timings] and [vibration_trains_amplitudes])
     * are used by the [TactileManager] to create complex vibration sequences.
     */
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
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration + 800L, currStimulusDuration, currStimulusDuration + 800L))    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    /**
     * Creates a list of trials for the infant version of the TVB test.
     * Trials are generated based on predefined stimuli configurations ([lStimuli]) and a fixed number of repetitions.
     *
     * @return A list of [TrialBasic] objects specifically configured for the infant TVB sub-task.
     */
    private fun createTrialsTimeInfants():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS_INFANTS) {

            val rtrials: MutableList<TrialBindingsInfants> = mutableListOf()

            rtrials.add(TrialBindingsInfants(++cnt, lStimuli[0].type, lStimuli[0].tactile_pattern))
            rtrials.add(TrialBindingsInfants(++cnt, lStimuli[1].type, lStimuli[1].tactile_pattern))
            rtrials.add(TrialBindingsInfants(++cnt, lStimuli[4].type, lStimuli[4].tactile_pattern))
            rtrials.add(TrialBindingsInfants(++cnt, lStimuli[2].type, lStimuli[2].tactile_pattern))
            rtrials.add(TrialBindingsInfants(++cnt, lStimuli[1].type, lStimuli[1].tactile_pattern))
            rtrials.add(TrialBindingsInfants(++cnt, lStimuli[0].type, lStimuli[0].tactile_pattern))
            rtrials.add(TrialBindingsInfants(++cnt, lStimuli[3].type, lStimuli[3].tactile_pattern))
            rtrials.add(TrialBindingsInfants(++cnt, lStimuli[2].type, lStimuli[2].tactile_pattern))

            trials.addAll(rtrials)
        }
        return trials
    }

    /**
     * Creates a list of trials for the "time double" TVB sub-task (fixed trial management).
     * This sub-task presents pairs of stimuli (TV, T-only, V-only) and various asynchronous T-V/V-T pairs.
     * Total trials: [( (TVx2, Tx2, Vx2) + 26 asynchronous) x 2 blocks ] x NUM_REPETITIONS.
     *
     * @return A list of [TrialBasic] objects for the "time double" TVB sub-task.
     */
    // [(4x2) x 6lat + 4 + 4 + 4 + 4] = 64 -> This comment seems to describe a different calculation.
    // The code generates (6 fixed types + 26 from lStimuliUnBalanced) * 2 inner loops * NUM_REPETITIONS
    private fun createTrialsTimeDouble():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {
            val rtrials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
            for (j in 0 until 2) {

                // 6
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))

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

    /**
     * Creates a list of trials for the "time single" TVB sub-task (fixed trial management).
     * This sub-task presents synchronous TV stimuli and various asynchronous T-V/V-T pairs.
     * Note: A comment indicates "only-A & only-T were removed". This method uses TYPE_TV and lStimuliUnBalanced.
     *
     * @return A list of [TrialBasic] objects for the "time single" TVB sub-task.
     */
    // only-A & only-T were removed in single stimulus sub-task. 7/8/2020
    private fun createTrialsTimeSingle():List<TrialBasic> {
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {
            val rtrials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
            for (j in 0 until 2) {

                // 2
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
                rtrials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))

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

    /**
     * Creates a list of trials for the "adaptive double" TVB sub-task.
     * This includes a fixed set of initial trials (synchronous TV, T-only, V-only, and fixed asynchronies)
     * followed by a set of adaptive trials for T-V and V-T conditions.
     * Total: 22 fixed trials + (28 T-V adaptive + 28 V-T adaptive) = 78 trials before shuffling.
     *
     * @return A list of [TrialBasic] objects for the "adaptive double" TVB sub-task.
     */
    // 22 fixed + 28 adaptive (Note: code generates 22 fixed + 28 pairs of T_V/V_T adaptive trials, so 22 + 56 = 78 total)
    private fun createTrialsAdaptiveDouble():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()

        // static part
        // 10
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))

        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0.0F))

        // 12
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 800.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 800.0F))

        // 28
        for (j in 0 until 28) {
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 0.0F, isADA = true))
        }
        trials.shuffle()
        return trials
    }

    /**
     * Creates a list of trials for the "adaptive single" TVB sub-task.
     * This includes a fixed set of initial trials (synchronous TV and fixed asynchronies)
     * followed by a set of adaptive trials for T-V and V-T conditions.
     * Total: 18 fixed trials + (32 T-V adaptive + 32 V-T adaptive) = 82 trials before shuffling.
     *
     * @return A list of [TrialBasic] objects for the "adaptive single" TVB sub-task.
     */
    // 18 fixed + 32 adaptive (Note: code generates 18 fixed + 32 pairs of T_V/V_T adaptive trials, so 18 + 64 = 82 total)
    private fun createTrialsAdaptiveSingle():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()

        // static part
        // 6
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))

        // 12
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 50.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 100.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 200.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 300.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 400.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 800.0F))
        trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 800.0F))

        // 32
        for (j in 0 until 32) {
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 0.0F, isADA = true))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 0.0F, isADA = true))
        }
        trials.shuffle()
        return trials
    }

    /**
     * Creates a large list of trials for debugging purposes.
     * Consists of repeating blocks of TV, T_V (50ms), and V_T (50ms) trials.
     *
     * @return A list of [TrialBasic] objects for debugging.
     */
    private fun createTrialsDebug():List<TrialBasic>{
        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until 100000) {
            for (j in 0 until 2) {
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0.0F))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 50.0F))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 50.0F))
            }
        }
        return trials
    }
    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    /**
     * Initializes the summary object based on the current test sub-task.
     * For TVB "time double" and "time single" tasks (including TOD versions), a [TVBUnBalancedSummary] is used.
     * Other task types might not have a specific summary object initialized here.
     */
    override fun initSummary(){

        mSummary = when (subject.type) {
            TEST_TVB_TIME_DOUBLESTIM,
            TEST_TVB_TIME_SINGLESTIM,
            TEST_TVB_TIME_DOUBLESTIM_TOD,
            TEST_TVB_TIME_SINGLESTIM_TOD    ->  TVBUnBalancedSummary(ctx)

            else                            ->  null
        }
    }
    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    /**
     * Presents the stimuli for the given trial.
     * Handles white noise, increments repetition count if it's a repeated trial,
     * and schedules stimuli delivery based on the subject's test type (sub-task).
     *
     * @param trial The [TrialBasic] object containing details for the current trial.
     * @param isRepeat True if this trial is a repetition, false otherwise.
     */
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        if(isRepeat)    trial.repetitions++

        mNoise?.start()

        when(subject.type) {

            TEST_TVB_TIME_INF -> {
                mStimuliHandler.postDelayed({
                    firstTrain((trial as TrialBindingsInfants).tactile_pattern)     // schedule first 3 stimuli
                    secondTrain(trial.type)    // schedule second 2 stimuli
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_TVB_TIME_SINGLESTIM,
            TEST_TVB_TIME_SINGLESTIM_TOD -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced)
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_TVB_TIME_DOUBLESTIM,
            TEST_TVB_TIME_DOUBLESTIM_TOD -> {

                // since I have to apply the possible shift, I calculate here the correction and thus call deliverShiftedStimulus for the 1st stim.
                // for the second I call instead deliverUnBalancedStimuli
                val corr_delays = subject.stimuliDelays.arrangeDelays(STIM_TV, -1,0,0)
                val shift       = WN_FIRSTSTIM_INTERVAL - corr_delays.shift

                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
                    mStimuliManager.deliverShiftedStimulus(
                        STIM_TV,
                        corr_delays.a,
                        corr_delays.t,
                        corr_delays.v
                    ) // simult
                }, shift)
                mStimuliHandler.postDelayed({
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced)
                }, shift + curISI)     // to preserve the desired ISI between 1st and 2nd stimuli,
            }
        }
    }

    /**
     * Delivers the first train of stimuli, primarily for the infant TVB sub-task.
     * This involves a sequence of tactile vibrations based on [tactile_pattern] and corresponding visual stimuli.
     * It accounts for potential delays between visual and tactile modalities to ensure perceived synchrony.
     *
     * @param tactile_pattern Index into [vibration_trains_timings] and [vibration_trains_amplitudes] to select the vibration pattern.
     */
    // tactile are programmed once, visual are programmed with postDelayed
    private fun firstTrain(tactile_pattern: Int) {

        // assuming vibro is faster than visual, I delay the former
        var V_delay     = subject.stimuliDelays.getStimuliDelay(STIM_TV).v - subject.stimuliDelays.getStimuliDelay(STIM_TV).t
        val timings = vibration_trains_timings[tactile_pattern]

        if(V_delay > 0) {
            vibration_trains_timings[tactile_pattern].mapIndexed { index, it ->
                timings[index] = it + V_delay
            }
        }
        else        // vibro delayed wrt visual: delay visual timings and preserve vibro onsets
            V_delay = 0


        vibrator?.vibratePattern(timings, vibration_trains_amplitudes[tactile_pattern])

        if(V_delay > 0L){
            mStimuliHandler.postDelayed({
                mStimuliManager.deliverUnimodalStimulus(STIM_V)
                testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
            }, V_delay)
        }
        else {
            mStimuliManager.deliverUnimodalStimulus(STIM_V)
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
        }

        mStimuliHandler.postDelayed({   mStimuliManager.deliverUnimodalStimulus(STIM_V)    }, curISI + V_delay)

        mStimuliHandler.postDelayed({   mStimuliManager.deliverUnimodalStimulus(STIM_V)    }, 2*curISI + V_delay)
    }

    /**
     * Delivers the second train of stimuli, specifically for the infant TVB sub-task.
     * Tactile stimuli are assumed to have been programmed by [firstTrain]. This method handles
     * visual stimuli and trial progression events. It accounts for potential delays between
     * audio (implicitly, though not directly used here) and tactile modalities.
     *
     * @param type The type of stimulus for the second train (e.g., STIM_TV, STIM_V, STIM_TYPE_TIME_T_V800).
     */
    // only for infants subtest
    // tactile have been already programmed at the beginning of the trial => just playback audio and take care of events
    private fun secondTrain(type:Int){

        // assuming audio is faster than vibro, I delay the former
        var A_delay    = subject.stimuliDelays.getStimuliDelay(STIM_TV).t - subject.stimuliDelays.getStimuliDelay(STIM_TV).a
        if(A_delay < 0L)   A_delay = 0L    // audio delayed wrt vibro: I previoulsy delayed vibro timings and now I preserve audio

        when(type){
            STIM_TV,
            STIM_V,
            STIM_TYPE_TIME_T_V800   -> {
                mStimuliHandler.postDelayed({
                    mStimuliManager.deliverUnimodalStimulus(STIM_V)
                    testEvent.accept(Triple(EVENT_SECOND_TRAIN, null, listOf()))
                }, 3 * curISI + A_delay)
                mStimuliHandler.postDelayed({
                    mStimuliManager.deliverUnimodalStimulus(STIM_V)
                }, 4 * curISI + A_delay)
                mStimuliHandler.postDelayed({
                    onStimuliEnd()
                }, 5 * curISI + A_delay)
            }

            STIM_T -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_SECOND_TRAIN, null, listOf()))
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    onStimuliEnd()
                }, 5 * curISI)
            }

            STIM_TYPE_TIME_T800_V -> {
                mStimuliHandler.postDelayed({
                    mStimuliManager.deliverUnimodalStimulus(STIM_V)
                    testEvent.accept(Triple(EVENT_SECOND_TRAIN, null, listOf()))
                }, (3 * curISI + 800L + A_delay))
                mStimuliHandler.postDelayed({
                    mStimuliManager.deliverUnimodalStimulus(STIM_V)
                    testEvent.accept(Triple(EVENT_SECOND_TRAIN, null, listOf()))
                }, (4 * curISI + 800 + A_delay))
                mStimuliHandler.postDelayed({
                    onStimuliEnd()
                }, (5 * curISI + 800L + A_delay))
            }
        }
    }
    /**
     * Delivers stimuli for "unbalanced" trials (typically asynchronous T-V or V-T pairs).
     * It determines the correct stimulus type and calculates necessary delays using [subject.stimuliDelays]
     * based on the trial's type and stimulus value (SOA).
     * Then, it uses [StimuliManager.deliverShiftedStimulus] to present the stimuli and schedules [onStimuliEnd].
     *
     * @param trial The [TrialBindingsUnBalanced] object containing details for the current trial.
     */
    private fun deliverUnBalancedStimuli(trial:TrialBindingsUnBalanced){

        var type = 0
        val corr_delays: CorrectedStimuliDelay = when(trial.type) {
            TYPE_TV     -> {
                type = mStimuliManager.typeTV
                subject.stimuliDelays.arrangeDelays(type, -1,0, 0)
            }
            TYPE_T      -> {
                type = mStimuliManager.typeT
                CorrectedStimuliDelay(-1, 0, -1)
            }
            TYPE_V      -> {
                type = mStimuliManager.typeV
                CorrectedStimuliDelay(-1, -1, 0)
            }
            TYPE_T_V    -> {
                type = mStimuliManager.typeTV
                subject.stimuliDelays.arrangeDelays(type, -1, 0, trial.stim_value)
            }
            TYPE_V_T    -> {
                type = mStimuliManager.typeTV
                subject.stimuliDelays.arrangeDelays(type, -1, trial.stim_value,0)
            }
            else        -> {
                type = mStimuliManager.typeTV
                CorrectedStimuliDelay(-1, 0, 0)
            }
        }
        mStimuliManager.deliverShiftedStimulus(type, corr_delays.a, corr_delays.t, corr_delays.v){ onStimuliEnd()}
    }
    // =============================================================================================================================
}

