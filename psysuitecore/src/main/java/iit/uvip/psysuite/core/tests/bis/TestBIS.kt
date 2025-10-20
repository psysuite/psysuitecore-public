// GEMINI_RETEST_BIS_ACTIVE
package iit.uvip.psysuite.core.tests.bis

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
import iit.uvip.psysuite.core.tests.bis.TestBIS.Companion.TRIAL_STAGE_1
import iit.uvip.psysuite.core.tests.bis.TestBIS.Companion.TRIAL_STAGE_2
import iit.uvip.psysuite.core.tests.bis.TestBIS.Companion.TRIAL_STAGE_3
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.trials.TrialsManager
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.StimuliSetBIS
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast

/**
 * Implements the Bisection (BIS) psychophysical test.
 *
 * This test typically involves presenting a subject with three stimuli in sequence:
 * a reference stimulus, a variable stimulus, and another reference stimulus.
 * The subject's task is usually to judge whether the second stimulus occurred closer
 * in time or perceived magnitude to the first or the third stimulus.
 *
 * This class manages the specific logic for various BIS test modalities, including:
 * - Unimodal tests (audio, tactile, visual)
 * - Bimodal tests (audio-tactile, audio-visual, visual-tactile)
 * - Both sub-threshold and supra-threshold stimulus intensity levels.
 *
 * It handles trial generation, stimulus delivery through [StimuliManager],
 * and interaction with adaptive testing procedures via [AdaptiveWrapper] if configured.
 *
 * @param ctx The application context.
 * @param activity The host activity.
 * @param hostfragment The host fragment, typically for UI interactions or lifecycle management.
 * @param subject Parcelable object containing subject-specific data and test configuration.
 * @param vibrator An optional [VibrationManager] for tactile stimuli. Required for tactile test variants.
 * @param mImageView An optional [ImageView] for visual stimuli. Required for visual test variants.
 * @param speechManager An optional [SpeechManager] for voice-based instructions or feedback.
 */
class TestBIS(
    ctx: Context,
    activity: Activity,
    hostfragment: Fragment,
    subject: SubjectBasicParcel,
    vibrator: VibrationManager?,
    mImageView: ImageView?,
    speechManager:SpeechManager?,
    mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView){

    // Log tag used for logging messages from this [TestBIS] class. Defaults to the simple name of the class.
    override var LOG_TAG:String = TestBIS::class.java.simpleName

    companion object {
        // Overrides
        @JvmStatic val TEST_BASIC_LABEL = "BIS"

        // Test-specific durations
        @JvmStatic val LAST_STIMULUS_DELAY_SUB          = 1000L     // ms of the third stimulus wrt first
        @JvmStatic val LAST_STIMULUS_DELAY_SUPRA        = 2500L     // ms of the third stimulus wrt first
        @JvmStatic val AV_STIMULUS_DELTA                = 200       // ms between the AV stimuli

        // Test-specific trial stages
        @JvmStatic val TRIAL_STAGE_1                    = 1
        @JvmStatic val TRIAL_STAGE_2                    = 2
        @JvmStatic val TRIAL_STAGE_3                    = 3

        /**
         * Retrieves a list of available BIS test conditions and their metadata.
         *
         * The conditions returned depend on the presence of a system vibrator,
         * as tactile conditions are only available if a vibrator is detected.
         * Each [ConditionData] object includes the test ID, label, logging key,
         * and applicable population groups.
         *
         * @param ctx The application context, used to check for vibrator availability.
         * @return A list of [ConditionData] objects representing the configurable BIS test conditions.
         */
        fun getConditionsInfo(ctx: Context): List<ConditionData>{
            return if(VibrationManager.sysHasVibrator(ctx))
                mutableListOf(
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}"           , TEST_BISECTION_AUDIO           , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_LOG}"           , Populations.hearing_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}"         , TEST_BISECTION_TACTILE         , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_TACTILE_LOG}"         , Populations.all_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}"          , TEST_BISECTION_VISUAL          , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL_LOG}"          , Populations.sighted_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_TACTILE}"   , TEST_BISECTION_AUDIO_TACTILE   , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_TACTILE_LOG}"   , Populations.hearing_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_VISUAL}"    , TEST_BISECTION_AUDIO_VISUAL    , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_VISUAL_LOG}"    , Populations.sighted_hearing_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_TACTILE}"  , TEST_BISECTION_VISUAL_TACTILE  , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL_TACTILE_LOG}"  , Populations.sighted_populations),

                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_${STIMULUS_ISI_SUPRA}"           , TEST_BISECTION_AUDIO_SUPRA           , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_LOG}${STIMULUS_ISI_SUPRA}"           , Populations.hearing_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}_${STIMULUS_ISI_SUPRA}"         , TEST_BISECTION_TACTILE_SUPRA         , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_TACTILE_LOG}${STIMULUS_ISI_SUPRA}"         , Populations.all_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}_${STIMULUS_ISI_SUPRA}"          , TEST_BISECTION_VISUAL_SUPRA          , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL_LOG}${STIMULUS_ISI_SUPRA}"          , Populations.sighted_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_TACTILE}_${STIMULUS_ISI_SUPRA}"   , TEST_BISECTION_AUDIO_TACTILE_SUPRA   , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_TACTILE_LOG}${STIMULUS_ISI_SUPRA}"   , Populations.hearing_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_VISUAL}_${STIMULUS_ISI_SUPRA}"    , TEST_BISECTION_AUDIO_VISUAL_SUPRA    , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_VISUAL_LOG}${STIMULUS_ISI_SUPRA}"    , Populations.sighted_hearing_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_TACTILE}_${STIMULUS_ISI_SUPRA}"  , TEST_BISECTION_VISUAL_TACTILE_SUPRA  , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL_TACTILE_LOG}${STIMULUS_ISI_SUPRA}"  , Populations.sighted_populations))

            else
                mutableListOf(
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO                              , TEST_BISECTION_AUDIO                  , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_LOG}"                            , Populations.hearing_populations),
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_VISUAL                             , TEST_BISECTION_VISUAL                 , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL_LOG}"                           , Populations.sighted_populations),
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO_VISUAL                       , TEST_BISECTION_AUDIO_VISUAL           , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_VISUAL_LOG}"                     , Populations.sighted_hearing_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_${STIMULUS_ISI_SUPRA}"        , TEST_BISECTION_AUDIO_SUPRA            , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_LOG}${STIMULUS_ISI_SUPRA}"       , Populations.hearing_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}_${STIMULUS_ISI_SUPRA}"       , TEST_BISECTION_VISUAL_SUPRA           , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL_LOG}${STIMULUS_ISI_SUPRA}"      , Populations.sighted_populations),
                    ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_VISUAL}_${STIMULUS_ISI_SUPRA}" , TEST_BISECTION_AUDIO_VISUAL_SUPRA     , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO_VISUAL_LOG}${STIMULUS_ISI_SUPRA}", Populations.sighted_hearing_populations))
        }

        /**
         * Defines the allowed modes or actions for proceeding to the next trial.
         *
         * This method returns a list of possible next trial modes for each available test condition.
         * The availability of modes can also depend on system capabilities like the presence of a vibrator.
         * In this specific implementation for BIS, it appears to consistently define `TEST_NEXTTRIAL_ANSWER`
         * as the mode for all conditions.
         *
         * @param ctx The application context, used to check for vibrator availability.
         * @return A list of lists, where each inner list contains integer codes for next trial modes
         *         corresponding to the test conditions.
         */
        fun getNextTrialModes(ctx: Context):List<List<Int>>{
            return if(VibrationManager.sysHasVibrator(ctx))
                listOf( listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),
                        listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER))
            else
                listOf( listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),
                        listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER),listOf(TEST_NEXTTRIAL_ANSWER))
        }
    }

    private val isSupra: Boolean = (subject.type > TEST_BISECTION_VISUAL_TACTILE)

    // region: DEFINE TRIALS SCHEMA: stimulus type & delay
    private var trialsUnimodalSubSchema:List<StimuliSetBIS> = listOf(
        StimuliSetBIS(2, 225F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 175F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 125F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 75F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 25F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 25F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 75F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 125F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 175F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 225F, false, CONFLICT_TYPE_NONE)
    )

    private var trialsUnimodalSupraSchema:List<StimuliSetBIS> = listOf(
        StimuliSetBIS(2, 450F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 350F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 250F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 150F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 50F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 50F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 150F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 250F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 350F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 450F, false, CONFLICT_TYPE_NONE)
    )

    private val bimodalStimuliDelta = listOf(0F,100F,200F)      // ms between the AV stimuli
                                                                // first stim is delivered at the given latency. the second  AV_STIMULUS_DELTA after
                                                                // ntrials latency conflict-type
    // 64 trials
    private var trialsBimodalSubSchema:List<StimuliSetBIS> = listOf(
        StimuliSetBIS(2, 300F, true),
        StimuliSetBIS(4, 200F, true),
        StimuliSetBIS(4, 100F, true),
        StimuliSetBIS(4, 50F,  true),
        StimuliSetBIS(2, 15F,  true),
        StimuliSetBIS(2, 15F,  false),
        StimuliSetBIS(4, 50F,  false),
        StimuliSetBIS(4, 100F, false),
        StimuliSetBIS(4, 200F, false),
        StimuliSetBIS(2, 300F, false),

        StimuliSetBIS(2, 300F, true),
        StimuliSetBIS(4, 200F, true),
        StimuliSetBIS(4, 100F, true),
        StimuliSetBIS(4, 50F,  true),
        StimuliSetBIS(2, 15F,  true),
        StimuliSetBIS(2, 15F,  false),
        StimuliSetBIS(4, 50F,  false),
        StimuliSetBIS(4, 100F, false),
        StimuliSetBIS(4, 200F, false),
        StimuliSetBIS(2, 300F, false)
    )

    private var trialsBimodalSupraSchema:List<StimuliSetBIS> = listOf(
        StimuliSetBIS(2, 300F, true),
        StimuliSetBIS(4, 200F, true),
        StimuliSetBIS(4, 100F, true),
        StimuliSetBIS(4, 50F,  true),
        StimuliSetBIS(2, 15F,  true),
        StimuliSetBIS(2, 15F,  false),
        StimuliSetBIS(4, 50F,  false),
        StimuliSetBIS(4, 100F, false),
        StimuliSetBIS(4, 200F, false),
        StimuliSetBIS(2, 300F, false),

        StimuliSetBIS(2, 300F, true),
        StimuliSetBIS(4, 200F, true),
        StimuliSetBIS(4, 100F, true),
        StimuliSetBIS(4, 50F,  true),
        StimuliSetBIS(2, 15F,  true),
        StimuliSetBIS(2, 15F,  false),
        StimuliSetBIS(4, 50F,  false),
        StimuliSetBIS(4, 100F, false),
        StimuliSetBIS(4, 200F, false),
        StimuliSetBIS(2, 300F, false)
    )

    // endregion

    private var conflictType:String = ""
    private var lastStimulusDelay:Long = 0L
    private var midLatency:Long = 0L
    private var questionDelay:Long = 0L

    private var currStimulus:String = ""
    private var currStimulusDuration2:Long     = 0L          // default value to be used when second stimulus duration is not given

    /**
     * Defines the drawable resources used for visual stimuli in the BIS test.
     * This list typically includes resources for different states or types of visual cues,
     * such as different colors for circles presented during the trials.
     * For example:
     * - `R.drawable.white_circle`
     * - `R.drawable.red_circle`
     * - `R.drawable.grey_circle`
     * - `R.drawable.blue_circle`
     * These drawables are utilized by the [VisualManager] during the `show` phase of a trial.
     */
    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.red_circle, R.drawable.grey_circle, R.drawable.blue_circle)

    private val nBlocks                     = 10     // I present N_BLOCKS * 2 * 10 conditions = 200 trials

    // region adaptive setup
    private val nAdaptiveTrials             = 40
    private val nTotalAdaptiveTrials        = 200
    private val nFixedInAdaptiveTrials      = 20
    private val nAT_range_sub               = 400F
    private val nAT_range_supra             = 600F
    private val adoParams                   = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)

    private lateinit var taskADAParams:TaskADAParams
    private lateinit var adoWrapper:AdaptiveWrapper
    // endregion

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    /**
     * Initializes the Bisection (BIS) test environment based on the subject's configuration.
     * This method orchestrates the setup of various components required for the test,
     * including stimulus parameters, trial generation, and hardware resources.
     *
     * Key responsibilities include:
     * - Validating required hardware (ImageView, Vibrator for tactile tests).
     * - Setting up adaptive testing parameters if applicable.
     * - Determining stimulus timings (e.g., `lastStimulusDelay`, `midLatency`).
     * - Initializing specific bisection parameters via [initBisection] based on the test type.
     * - Creating and configuring the [TrialsManager] (either [FixedTrialsManager] or [AdaptiveTrialsManager])
     *   with the appropriate set of trials ([createUnimodalTrials], [createBimodalTrials], [createTrialsAdaptive], or [createTrialsDebug]).
     * - Setting the test label based on condition information.
     * - Preparing the result file for logging.
     * - Initializing audio noise and the [StimuliManager] for delivering stimuli.
     * - Emitting an `EVENT_TEST_SETUP_COMPLETED` event upon successful setup.
     *
     * @throws ImageViewDefinedException if the required ImageView (`mImageView`) is not provided.
     * @throws VibratorNotDefinedException if the vibrator is required for the current test type but not provided.
     * @throws IllegalArgumentException if an unsupported test type or condition is encountered during trial setup.
     */
    override fun initTest() {

        // sanity checks
        when {
            mImageView == null -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null && (subject.type == TEST_BISECTION_TACTILE || subject.type == TEST_BISECTION_AUDIO_TACTILE) -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }
        validAnswers    = mutableListOf(ctx.resources.getString(R.string.bisection_rb1_text), ctx.resources.getString(R.string.bisection_rb3_text))

        // manage SUB/SUPRA
        lastStimulusDelay = if(subject.type > TEST_BISECTION_VISUAL_TACTILE) {
                                taskADAParams   = TaskADAParams(nAT_range_supra, nTotalAdaptiveTrials)
                                LAST_STIMULUS_DELAY_SUPRA
                            }
                            else {
                                taskADAParams = TaskADAParams(nAT_range_sub, nTotalAdaptiveTrials)
                                LAST_STIMULUS_DELAY_SUB
                            }
        midLatency      = lastStimulusDelay / 2
        questionDelay   = lastStimulusDelay + QUESTION_DELAY
        adoWrapper      = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

        initBisection(subject.type) // set mQuestion/ currStimulusDuration/ currStimulusDuration2/ currStimulusLabel

//        testEvent.accept(Triple(EVENT_SHOW_INFO, "Setting up trials", listOf()))
        // manage trials
        mTrialsManager =
            if (subject.isDebug)
                FixedTrialsManager(createTrialsDebug() as MutableList<TrialBasic>)
            else {
                var training_trials = if(subject.doTraining == TEST_SWITCH_ENABLED)
                    when (subject.type) {
                        TEST_BISECTION_AUDIO, TEST_BISECTION_TACTILE, TEST_BISECTION_VISUAL,
                        TEST_BISECTION_AUDIO_SUPRA, TEST_BISECTION_TACTILE_SUPRA, TEST_BISECTION_VISUAL_SUPRA ->
                                createUnimodalTrainingTrials() as MutableList<TrialBasic>
                        else -> mutableListOf()
                    }
                else    mutableListOf()

                if (subject.trman_type == TEST_TRMAN_FIXED) {
                    val trials = when (subject.type) {
                        TEST_BISECTION_AUDIO, TEST_BISECTION_TACTILE, TEST_BISECTION_VISUAL, TEST_BISECTION_AUDIO_SUPRA, TEST_BISECTION_TACTILE_SUPRA, TEST_BISECTION_VISUAL_SUPRA
                             -> createUnimodalTrials() as MutableList<TrialBasic>
                        else -> createBimodalTrials(subject.type) as MutableList<TrialBasic>
                    }
                    FixedTrialsManager(trials, training_trials)
                } else {
                    when (subject.type) {
                        TEST_BISECTION_AUDIO, TEST_BISECTION_TACTILE, TEST_BISECTION_VISUAL,
                        TEST_BISECTION_AUDIO_SUPRA, TEST_BISECTION_TACTILE_SUPRA, TEST_BISECTION_VISUAL_SUPRA -> {
                            val trials = createTrialsAdaptive2()
                            AdaptiveTrialsManager(trials as MutableList<TrialBasic>, adoWrapper, training_trials)
                        }
                        else -> throw IllegalArgumentException(ctx.getString(R.string.condition_not_allowed))
                    }
                }
            }

        mTestLabel              = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)
        createResultFile(TrialBIS.LOG_HEADER)

        mNoise = AudioManager.getAudioResource(ctx,"wnoise_20s", 0.01f)

        mStimuliManager =   if(vibrator != null)
                                StimuliManager(
                                    AudioManager(STIM_A, audioResources[STIMULUS_DURATION_AUDIO] ?: "t1000hz_50ms.wav",  duration = STIMULUS_DURATION_AUDIO, handler = mStimuliHandler, ctx = ctx),
                                    TactileManager(vibrator, duration = STIMULUS_DURATION_TACTILE, handler = mStimuliHandler),
                                    VisualManager(STIM_V,mImageView!!, mDrawablesResource[1], mDrawablesResource[0], duration = STIMULUS_DURATION_VISUAL, handler = mStimuliHandler),
                                    subject.stimuliDelays, ctx, mStimuliHandler)
                            else
                                StimuliManager(
                                    AudioManager(STIM_A, audioResources[STIMULUS_DURATION_AUDIO] ?: "t1000hz_50ms.wav",  duration = STIMULUS_DURATION_AUDIO, handler = mStimuliHandler, ctx = ctx),
                                    null,
                                    VisualManager(STIM_V, mImageView!!, mDrawablesResource[1], mDrawablesResource[0], duration = STIMULUS_DURATION_VISUAL, handler = mStimuliHandler),
                                    subject.stimuliDelays, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // INIT TRIALS
    // =============================================================================================================================
    private fun initBisection(type:Int){

        when (subject.type) {
            TEST_BISECTION_AUDIO, TEST_BISECTION_AUDIO_SUPRA                    ->{
                mQuestion               = ctx.resources.getString(R.string.bisection_question_text_audio)
                currStimulusDuration    = STIMULUS_DURATION_AUDIO
                currStimulusLabel       = STIMULUS_TYPE_AUDIO
                conflictType            = CONFLICT_TYPE_NONE                
            }
            TEST_BISECTION_TACTILE, TEST_BISECTION_TACTILE_SUPRA                -> {
                mQuestion               = ctx.resources.getString(R.string.bisection_question_text_tactile)
                currStimulusDuration    = STIMULUS_DURATION_TACTILE
                currStimulusLabel       = STIMULUS_TYPE_TACTILE
                conflictType            = CONFLICT_TYPE_NONE
            }
            TEST_BISECTION_VISUAL, TEST_BISECTION_VISUAL_SUPRA                  -> {
                mQuestion               = ctx.resources.getString(R.string.bisection_question_text_visual)
                currStimulusDuration    = STIMULUS_DURATION_VISUAL
                currStimulusLabel       = STIMULUS_TYPE_VISUAL
                conflictType            = CONFLICT_TYPE_NONE
            }
            TEST_BISECTION_AUDIO_TACTILE, TEST_BISECTION_AUDIO_TACTILE_SUPRA    -> {
                mQuestion               = ctx.resources.getString(R.string.bisection_question_text_mixed)
                currStimulusDuration    = STIMULUS_DURATION_AUDIO
                currStimulusDuration2   = STIMULUS_DURATION_TACTILE
                currStimulusLabel       = STIMULUS_TYPE_AUDIO_TACTILE
                conflictType            = STIMULUS_TYPE_AUDIO_TACTILE_LOG
            }
            TEST_BISECTION_AUDIO_VISUAL, TEST_BISECTION_AUDIO_VISUAL_SUPRA      -> {
                mQuestion               = ctx.resources.getString(R.string.bisection_question_text_mixed)
                currStimulusDuration    = STIMULUS_DURATION_AUDIO
                currStimulusDuration2   = STIMULUS_DURATION_VISUAL
                currStimulusLabel       = STIMULUS_TYPE_AUDIO_VISUAL
                conflictType            = STIMULUS_TYPE_AUDIO_VISUAL_LOG
            }
            TEST_BISECTION_VISUAL_TACTILE, TEST_BISECTION_VISUAL_TACTILE_SUPRA      -> {
                mQuestion               = ctx.resources.getString(R.string.bisection_question_text_mixed)
                currStimulusDuration    = STIMULUS_DURATION_VISUAL
                currStimulusDuration2   = STIMULUS_DURATION_TACTILE
                currStimulusLabel       = STIMULUS_TYPE_VISUAL_TACTILE
                conflictType            = STIMULUS_TYPE_VISUAL_TACTILE_LOG
            }
            else -> throw IllegalArgumentException(ctx.getString(R.string.condition_not_allowed))
        }
    }

    // =============================================================================================================================
    // region CREATE TRIALS
    // =============================================================================================================================
    // 88 trials
    private fun createUnimodalTrials():List<TrialBasic>{

        val schema =    if(isSupra)     trialsUnimodalSupraSchema
                        else            trialsUnimodalSubSchema

        var trials:MutableList<TrialBasic> = mutableListOf()
        var temp_trials:MutableList<TrialBasic> = mutableListOf()

        for(i in 0 until nBlocks){
            temp_trials = mutableListOf()
            for(section in schema)
                for(i in 0 until section.ntrials)
                    temp_trials.add(TrialBIS(-1, subject.type, currStimulusLabel, section.magnitude, section.isBefore, section.conflict, currStimulusDuration, mid_latency = midLatency))
            temp_trials.shuffle()
            trials.addAll(temp_trials)
        }

        return trials
    }

    private fun createTrialsAdaptive():List<TrialBasic>{
        var cnt = -1
        val trials: MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until (nAdaptiveTrials-10)/2){
            trials.add(TrialBIS(++cnt, subject.type, STIMULUS_TYPE_AUDIO, TrialsManager.ADAPTIVE_VALUE, true , CONFLICT_TYPE_NONE,STIMULUS_DURATION_AUDIO, isADA=true))
            trials.add(TrialBIS(++cnt, subject.type, STIMULUS_TYPE_AUDIO, TrialsManager.ADAPTIVE_VALUE, false, CONFLICT_TYPE_NONE,STIMULUS_DURATION_AUDIO, isADA=true))
        }

        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 300F, true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 200F, true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 100F, true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 50F , true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 15F , true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 15F , false, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 50F , false, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 100F, false, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 200F, false, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 300F, false, conflictType, currStimulusDuration, currStimulusDuration2))

        trials.shuffle()
        return trials
    }

    private fun createTrialsAdaptive2():List<TrialBasic>{

        val schema =    if(isSupra)     trialsUnimodalSupraSchema
                        else            trialsUnimodalSubSchema

        var trials:MutableList<TrialBasic> = mutableListOf()

        // 20 trials are fixed
        for(section in schema)
            for(i in 0 until section.ntrials)
                trials.add(TrialBIS(-1, subject.type, currStimulusLabel, section.magnitude, section.isBefore, section.conflict, currStimulusDuration, mid_latency = midLatency))

        // 180 are adaptive
        for (i in 0 until (nTotalAdaptiveTrials - nFixedInAdaptiveTrials)/2){
            trials.add(TrialBIS(-1, subject.type, currStimulusLabel, TrialsManager.ADAPTIVE_VALUE, true , CONFLICT_TYPE_NONE,STIMULUS_DURATION_AUDIO, mid_latency = midLatency, isADA=true))
            trials.add(TrialBIS(-1, subject.type, currStimulusLabel, TrialsManager.ADAPTIVE_VALUE, false, CONFLICT_TYPE_NONE,STIMULUS_DURATION_AUDIO, mid_latency = midLatency, isADA=true))
        }

        trials.shuffle()
        return trials
    }

    private fun createUnimodalTrainingTrials():List<TrialBasic>{

        val trials:MutableList<TrialBasic> = mutableListOf()

        val schema =    if(isSupra)     trialsUnimodalSupraSchema
        else            trialsUnimodalSubSchema

        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[0].magnitude, true,  CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[0].magnitude, false, CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[1].magnitude, true,  CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[1].magnitude, false, CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[2].magnitude, true,  CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[2].magnitude, false, CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[3].magnitude, true,  CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[3].magnitude, false, CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[4].magnitude, true,  CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, schema[4].magnitude, false, CONFLICT_TYPE_NONE, currStimulusDuration, mid_latency = midLatency, isTraining = true))

        return trials
    }

    private fun createBimodalTrials(type:Int):List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()

        val schema =    if(type > TEST_BISECTION_VISUAL_TACTILE)    trialsBimodalSupraSchema
                        else                                        trialsUnimodalSubSchema

        val conflict_type = when (type) {
            TEST_BISECTION_AUDIO_TACTILE    ->  STIMULUS_TYPE_AUDIO_TACTILE_LOG
            TEST_BISECTION_AUDIO_VISUAL     ->  STIMULUS_TYPE_AUDIO_VISUAL_LOG
            TEST_BISECTION_VISUAL_TACTILE   ->  STIMULUS_TYPE_VISUAL_TACTILE_LOG
            else                            ->  throw IllegalArgumentException(ctx.getString(R.string.condition_not_allowed))
        }
        schema.map { it.conflict = conflict_type }


        bimodalStimuliDelta.map{
        for(section in schema)
            for(i in 0 until section.ntrials){
                when(section.conflict == conflictType){
                    //                                 id   type        label,                   corr_answ, stim_value          conflict_type   duration       duration2
                    true    -> trials.add(TrialBIS(-1, subject.type, currStimulusLabel, section.magnitude, section.isBefore, section.conflict, currStimulusDuration, currStimulusDuration2, mid_latency = midLatency, conflict_magn=it))
                    false   -> trials.add(TrialBIS(-1, subject.type, currStimulusLabel, section.magnitude, section.isBefore, section.conflict, currStimulusDuration2, currStimulusDuration, mid_latency = midLatency, conflict_magn=it))
                }
            }
        }
        trials.shuffle()
        return trials
    }

    private fun createTrialsDebug():List<TrialBasic>{
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_mixed)

        val trials:MutableList<TrialBasic> = mutableListOf()
        for(i in 0 until 10000){
            //                     id   type                        label,                        corr_answ, stim_value          conflict_type   duration       duration2
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_TACTILE, STIMULUS_TYPE_AUDIO_TACTILE, 400F, true, CONFLICT_TYPE_NONE, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_TACTILE, mid_latency = midLatency))
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_TACTILE, STIMULUS_TYPE_AUDIO_TACTILE, 400F, false, CONFLICT_TYPE_NONE, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_TACTILE, mid_latency = midLatency))

            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VISUAL, STIMULUS_TYPE_AUDIO_VISUAL, 400F, true, STIMULUS_TYPE_VISUAL_AUDIO_LOG, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_VISUAL, mid_latency = midLatency))
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VISUAL, STIMULUS_TYPE_AUDIO_VISUAL, 400F, false, STIMULUS_TYPE_VISUAL_AUDIO_LOG, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_VISUAL, mid_latency = midLatency))
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VISUAL, STIMULUS_TYPE_AUDIO_VISUAL, 400F, true, STIMULUS_TYPE_AUDIO_VISUAL_LOG, STIMULUS_DURATION_VISUAL, STIMULUS_DURATION_AUDIO, mid_latency = midLatency))
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VISUAL, STIMULUS_TYPE_AUDIO_VISUAL, 400F, false, STIMULUS_TYPE_AUDIO_VISUAL_LOG, STIMULUS_DURATION_VISUAL, STIMULUS_DURATION_AUDIO, mid_latency = midLatency))
        }
        return trials
    }
    //endregion

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================

    /**
     * Intended for initializing any summary data or views at the beginning of the test.
     * In the current implementation for [TestBIS], this method is empty, suggesting
     * that no specific summary initialization is required at this stage or it's handled elsewhere.
     */
    override fun initSummary(){}

    // =============================================================================================================================
    // region DELIVER STIMULI
    // =============================================================================================================================

    // a trial has this temporal line:
    // +  FIRST_STIMULUS_DELAY                          => 1st stim
    // + (FIRST_STIMULUS_DELAY + mTrial.stim_value)     => 2nd stim
    // + (FIRST_STIMULUS_DELAY + LAST_STIMULUS_DELAY)   => 3rd stim
    // + (QUESTION_DELAY + FIRST_STIMULUS_DELAY)        => event : show question
    /**
     * Manages the presentation of stimuli for a given trial in the BIS test.
     *
     * This method orchestrates the temporal sequence of three stimuli, characteristic of
     * a bisection task. It accounts for:
     * - Starting background white noise if enabled for the subject.
     * - Incrementing trial repetition count if the trial is being repeated.
     * - Calculating time shifts for aligning bimodal stimuli using [delaysAligner].
     * - Scheduling the delivery of the three stimuli ([TRIAL_STAGE_1], [TRIAL_STAGE_2], [TRIAL_STAGE_3])
     *   at precise timings using [mStimuliHandler].
     *   - The first stimulus is delivered after [FIRST_STIMULUS_DELAY].
     *   - The second (variable) stimulus is delivered relative to the first, based on `trial.stim_value`.
     *   - The third stimulus is delivered after `lastStimulusDelay` relative to the first.
     * - Scheduling the `onStimuliEnd()` call after a `questionDelay` to prompt for an answer.
     *
     * The actual stimulus delivery for each stage is delegated to [deliverStimulus].
     *
     * @param trial The [TrialBasic] object (cast to [TrialBIS]) containing details for the current trial.
     * @param isRepeat Boolean flag indicating if this trial is a repetition.
     */
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        if(subject.whitenoise == TEST_SWITCH_ENABLED) mNoise?.start()

        if(isRepeat)    mTrial.repetitions++

        // to align bimodal stimuli, I have to delay the fastest modality by time_shift ms.
        // Thus I anticipate all main onsets by the same ms.
        // Since this code act for every kind of stimulus combination, I assume a trimodal stim
        val time_shift = when(trial.type){
            TEST_BISECTION_AUDIO_TACTILE    -> subject.stimuliDelays.getShift(STIM_AT, 0,0,-1)
            TEST_BISECTION_AUDIO_VISUAL     -> subject.stimuliDelays.getShift(STIM_AV, 0,-1,0)
            else                            -> 0
        }

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialBIS, TRIAL_STAGE_1)
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
        }, 500L) // FIRST_STIMULUS_DELAY - time_shift)

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialBIS, TRIAL_STAGE_2)
        }, (FIRST_STIMULUS_DELAY - time_shift + (trial as TrialBIS).stim_value))

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialBIS, TRIAL_STAGE_3)
        }, (FIRST_STIMULUS_DELAY - time_shift + lastStimulusDelay))

        mStimuliHandler.postDelayed({
            onStimuliEnd()
        }, (FIRST_STIMULUS_DELAY - time_shift + questionDelay))
    }

    private fun deliverStimulus(trial: TrialBIS, stage:Int=0){

        when(trial.type) {
            TEST_BISECTION_AUDIO,           TEST_BISECTION_AUDIO_SUPRA           ->  mStimuliManager.deliverAStimulus()
            TEST_BISECTION_TACTILE,         TEST_BISECTION_TACTILE_SUPRA         ->  mStimuliManager.deliverTStimulus()
            TEST_BISECTION_VISUAL,          TEST_BISECTION_VISUAL_SUPRA          ->  mStimuliManager.deliverVStimulus()
            TEST_BISECTION_AUDIO_TACTILE,   TEST_BISECTION_AUDIO_TACTILE_SUPRA   ->  deliverATStimuli(trial, stage)
            TEST_BISECTION_AUDIO_VISUAL,    TEST_BISECTION_AUDIO_VISUAL_SUPRA    ->  deliverAVStimuli(trial, stage)
            TEST_BISECTION_VISUAL_TACTILE,  TEST_BISECTION_VISUAL_TACTILE_SUPRA  ->  deliverTVStimuli(trial, stage)
        }
    }

    private fun deliverATStimuli(trial:TrialBIS, stage:Int=0){

        if(stage == TRIAL_STAGE_2){
            // mid (second) stimulus: audio and video are dissociated
            val corr_delays =   if(trial.conflict_type == STIMULUS_TYPE_AUDIO_TACTILE_LOG)
                                    subject.stimuliDelays.arrangeDelays(STIM_AT, 0, trial.conflict_magn.toLong(),-1)
                                else
                                     subject.stimuliDelays.arrangeDelays(STIM_AT, trial.conflict_magn.toLong(),0, -1)

            mStimuliManager.deliverShiftedStimulus(STIM_AT, corr_delays.a, -1, corr_delays.v)
        }
        // normal stimulus (1st or 3rd): audio and video simultaneously
        else    mStimuliManager.deliverAlignedStimulus(STIM_AT)
    }

    private fun deliverAVStimuli(trial:TrialBIS, stage:Int=0){

        mStimuliManager.mVisualManager!!.drawResOn = mDrawablesResource[stage]
        if(stage == TRIAL_STAGE_2){
            // mid (second) stimulus: audio and video are dissociated
            val corr_delays =   if(trial.conflict_type == STIMULUS_TYPE_VISUAL_AUDIO_LOG)
                                    subject.stimuliDelays.arrangeDelays(STIM_AV, AV_STIMULUS_DELTA.toLong(),-1,0)
                                else
                                    subject.stimuliDelays.arrangeDelays(STIM_AV,0, -1, AV_STIMULUS_DELTA.toLong())
            mStimuliManager.deliverShiftedStimulus(STIM_AV, corr_delays.a, -1, corr_delays.v)
        }
        // normal stimulus (1st or 3rd): audio and video simultaneously
        else    mStimuliManager.deliverAlignedStimulus(STIM_AV)
    }

    private fun deliverTVStimuli(trial:TrialBIS, stage:Int=0){

        mStimuliManager.mVisualManager!!.drawResOn = mDrawablesResource[stage]
        if(stage == TRIAL_STAGE_2){
            // mid (second) stimulus: audio and video are dissociated
            val corr_delays =   if(trial.conflict_type == STIMULUS_TYPE_VISUAL_TACTILE_LOG)
                                    subject.stimuliDelays.arrangeDelays(STIM_TV, -1, trial.conflict_magn.toLong(),0)
                                else
                                    subject.stimuliDelays.arrangeDelays(STIM_TV,-1, 0, trial.conflict_magn.toLong())
            mStimuliManager.deliverShiftedStimulus(STIM_TV, -1, corr_delays.v, corr_delays.v)
        }
        // normal stimulus (1st or 3rd): audio and video simultaneously
        else    mStimuliManager.deliverAlignedStimulus(STIM_TV)
    }
    // endregion

    // =====================================================================================
    // region DEBUG
    // =====================================================================================
    // Trial(val type:Int, val label:String, val conflict_type:String, val stim_value:Int, val duration:Int)
    // just one trial for each latency
    private var trialsDefaultSchema_debug: List<StimuliSetBIS> = listOf(StimuliSetBIS(2, 200F, true, CONFLICT_TYPE_NONE))

    private fun createDefaultTrials_debug(stim_type_label:String, duration:Long, duration2:Long=0L):List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(section in trialsUnimodalSubSchema)
            for(i in 0 until 1)
                trials.add(TrialBIS(-1, subject.type, stim_type_label, section.magnitude, section.isBefore, section.conflict, duration, duration2))

        trials.shuffle()
        return trials
    }
    // endregion

    // =============================================================================================================================
}
