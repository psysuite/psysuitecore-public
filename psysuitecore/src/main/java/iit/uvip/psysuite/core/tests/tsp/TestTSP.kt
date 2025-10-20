package iit.uvip.psysuite.core.tests.tsp

import android.app.Activity
import android.content.Context
import android.os.SystemClock.uptimeMillis
import android.view.Gravity
import android.view.View
import android.view.View.VISIBLE
import android.view.View.generateViewId
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentTestBinding
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.AudioManager
import iit.uvip.psysuite.core.stimuli.ImageViewDefinedException
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.TactileManager
import iit.uvip.psysuite.core.stimuli.VibratorNotDefinedException
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.ui.fragments.TestFragment
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.StimuliSetTSP
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast

/**
 * Manages the Test of Temporal Sensitivity Perception (TSP).
 *
 * This test assesses the subject's ability to perceive the temporal order of stimuli.
 * It supports visual, auditory, and tactile modalities, with options for sub-threshold
 * and supra-threshold Inter-Stimulus Intervals (ISIs). The test presents a sequence
 * of cue stimuli followed by a target stimulus, and the subject is expected to respond
 * upon perceiving the target.
 *
 * The test configuration, including stimulus modality, ISI type (sub/supra),
 * and debug mode, is determined by the [subject] parcel.
 *
 * @property LOG_TAG The log tag for this class, typically the simple name of the class.
 * @property mDrawablesResource A list of drawable resources, initialized with a black circle for visual stimuli.
 * @param ctx The Android [Context] for accessing resources and system services.
 * @param activity The current [Activity] hosting the test.
 * @param hostfragment The [Fragment] that hosts this test, typically a [TestFragment].
 * @param subject The [SubjectBasicParcel] containing subject-specific configuration for the test.
 * @param vibrator An optional [VibrationManager] for delivering tactile stimuli.
 * @param mImageView An optional [ImageView] for displaying visual stimuli.
 * @param speechManager An optional [SpeechManager] for voice-related functionalities (not directly used in core logic).
 * @param mainView The main [View] of the test fragment.
 * @throws ImageViewDefinedException If a visual TSP test is configured but `mImageView` is null.
 * @throws VibratorNotDefinedException If a tactile TSP test is configured but `vibrator` is null.
 */
class TestTSP(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?,
              mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView)  {

    override var LOG_TAG: String = TestTSP::class.java.simpleName

    companion object {
        // Overrides
        @JvmStatic val TEST_BASIC_LABEL = "TSP"

        // Test-specific durations
        @JvmStatic val ISI_SUB   = 750L
        @JvmStatic val ISI_SUPRA = 1500L
        @JvmStatic val ISI_RND_MULT = 0.1F  // percentage of isi to randomize
        @JvmStatic val FIRST_STIMULUS_DELAY = 1000L  //
        @JvmStatic val N_BLOCKS = 10  //
        @JvmStatic val STIMULUS_DURATION_VISUAL = 100L  //

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("${STIMULUS_TYPE_VISUAL_LOG}_${STIMULUS_ISI_SUB}",     TEST_TSP_V_SUB,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_LOG}_${STIMULUS_ISI_SUB}" ,   Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_AUDIO_LOG}_${STIMULUS_ISI_SUB}",     TEST_TSP_A_SUB,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_LOG}_${STIMULUS_ISI_SUB}" ,   Populations.hearing_populations),
            ConditionData("${STIMULUS_TYPE_TACTILE_LOG}_${STIMULUS_ISI_SUB}",     TEST_TSP_T_SUB,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE_LOG}_${STIMULUS_ISI_SUB}" ,   Populations.all_populations),
            ConditionData("${STIMULUS_TYPE_VISUAL_LOG}_${STIMULUS_ISI_SUPRA}",   TEST_TSP_V_SUPRA,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_LOG}_${STIMULUS_ISI_SUPRA}" , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_AUDIO_LOG}_${STIMULUS_ISI_SUPRA}",   TEST_TSP_A_SUPRA,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_LOG}_${STIMULUS_ISI_SUPRA}" , Populations.hearing_populations),
            ConditionData("${STIMULUS_TYPE_TACTILE_LOG}_${STIMULUS_ISI_SUPRA}",   TEST_TSP_T_SUPRA,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE_LOG}_${STIMULUS_ISI_SUPRA}" , Populations.all_populations),
        )

        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO)
        )
    }

    // region: DEFINE TRIALS SCHEMA: stimulus type & delay
    private var trialsUnimodalSubSchema:List<StimuliSetTSP> = listOf(
        StimuliSetTSP(2, 200F, true),
        StimuliSetTSP(2, 150F, true),
        StimuliSetTSP(2, 100F, true),
        StimuliSetTSP(2, 50F,  true),
        StimuliSetTSP(2, 50F,  false),
        StimuliSetTSP(2, 100F, false),
        StimuliSetTSP(2, 150F, false),
        StimuliSetTSP(2, 200F, false)
    )

    private var trialsUnimodalSupraSchema:List<StimuliSetTSP> = listOf(
        StimuliSetTSP(2, 400F, true),
        StimuliSetTSP(2, 300F, true),
        StimuliSetTSP(2, 200F, true),
        StimuliSetTSP(2, 100F, true),
        StimuliSetTSP(2, 100F, false),
        StimuliSetTSP(2, 200F, false),
        StimuliSetTSP(2, 300F, false),
        StimuliSetTSP(2, 400F, false)
    )
    // endregion

    private val binding: FragmentTestBinding =  (hostfragment as TestFragment).binding

    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.black_circle)
    private var currImageRes:Int        = 0

    // region RESPONSE BUTTON
    private lateinit var mRespButton:Button
    private var parent_layout_width:Int         = 0
    private var parent_layout_height:Int        = 0
    private lateinit var mLayout:ConstraintLayout
    private val isLandscape: Boolean = false
    // endregion

//    private var curr_trial_stimvalue:Long             = 0L     // isi within the cue sequence and base target
    private var main_isi:Long                   = 0L     // isi within the cue sequence and base target
    private var nCueStimuli:Int                 = 3      // num of cue stimuli

    private val isSupra: Boolean = (subject.type == TEST_TSP_V_SUPRA || subject.type == TEST_TSP_A_SUPRA || subject.type == TEST_TSP_T_SUPRA)

    private var trialAbortTime:Long = 0L

    private var trialStartMs:Long               = 0L                    // trial onset
    private var trialEndMs:Long                 = trialAbortTime        // user press latency

    /**
     * Initializes the TSP test environment.
     *
     * This method performs the following setup tasks:
     * 1.  Assigns the root view from the binding to `mLayout`.
     * 2.  Validates that `mImageView` is provided for visual tests and `vibrator` for tactile tests,
     *     throwing [ImageViewDefinedException] or [VibratorNotDefinedException] respectively if not.
     * 3.  Sets up basic test parameters: `validAnswers` (empty), `mQuestion` (empty),
     * 4.  Determines `currStimulusDuration` and `currStimulusLabel` based on the [subject]'s test type
     *     (visual, auditory, or tactile).
     * 5.  Sets `main_isi` (Inter-Stimulus Interval) based on whether the test is sub-threshold or supra-threshold.
     * 6.  Initializes `currImageRes` with the default drawable.
     * 7.  Determines and sets `mTestLabel` from the condition information. Shows a toast if the test code is unrecognized.
     * 8.  Creates trials using [createFixedTrials] or [createTrialsDebug] based on `subject.isDebug`
     *     and initializes `mTrialsManager` with a [FixedTrialsManager].
     * 9.  Creates the result file using [createResultFile] with the [LOG_HEADER].
     * 10. Initializes `mStimuliManager` with the appropriate managers (Visual, Audio, or Tactile)
     *     based on the subject's test type.
     * 11. Emits an [EVENT_TEST_SETUP_COMPLETED] event.
     *
     * @throws ImageViewDefinedException If a visual TSP test is selected but `mImageView` is null.
     * @throws VibratorNotDefinedException If a tactile TSP test is selected but `vibrator` is null.
     */
    override fun initTest(){

        mLayout = binding.root

        when {
            mImageView == null && (subject.type == TEST_TSP_V_SUB || subject.type == TEST_TSP_V_SUPRA)  -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null && (subject.type == TEST_TSP_T_SUB || subject.type == TEST_TSP_T_SUPRA)    -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
            mainView == null                                                                            -> throw ImageViewDefinedException("MAIN_VIEW_NOT_DEFINED")
        }
        // set question & create mTrials list
        validAnswers    = mutableListOf()
        mQuestion       = ""

        when(subject.type){
            TEST_TSP_V_SUB, TEST_TSP_V_SUPRA    -> {
                currStimulusDuration    = STIMULUS_DURATION_VISUAL
                currStimulusLabel       = "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_LOG}"
            }
            TEST_TSP_A_SUB, TEST_TSP_A_SUPRA    -> {
                currStimulusDuration    = STIMULUS_DURATION_AUDIO
                currStimulusLabel       = "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_LOG}"
            }
            else                                -> {
                currStimulusDuration    = STIMULUS_DURATION_TACTILE
                currStimulusLabel       = "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE_LOG}"
            }
        }

        main_isi = if(isSupra) ISI_SUPRA else ISI_SUB
        currImageRes            = mDrawablesResource[0]

        mTestLabel              = ""
        getConditionsInfo(ctx).map { if (it.id == subject.type) mTestLabel = it.label }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        mTrialsManager =
            when (subject.isDebug) {
                true -> {
                    val trials = createTrialsDebug()
                    FixedTrialsManager(trials as MutableList<TrialBasic>)
                }
                else -> FixedTrialsManager(createFixedTrials() as MutableList<TrialBasic>)
            }
        createResultFile(TrialTSP.LOG_HEADER)

        mStimuliManager = when(subject.type){

            TEST_TSP_V_SUB, TEST_TSP_V_SUPRA -> {
                StimuliManager(null, null,
                    VisualManager(STIM_T, mImageView!!, currImageRes, duration = currStimulusDuration, handler = mStimuliHandler),
                    subject.stimuliDelays, ctx, mStimuliHandler)
            }
            TEST_TSP_A_SUB, TEST_TSP_A_SUPRA -> {
                StimuliManager(AudioManager(STIM_A, audioResources[STIMULUS_DURATION_AUDIO] ?: "t1000hz_50ms.wav",  duration = STIMULUS_DURATION_AUDIO, handler = mStimuliHandler, ctx = ctx), null,null,
                    subject.stimuliDelays, ctx, mStimuliHandler)
            }
            else -> StimuliManager(null, TactileManager(vibrator!!, duration = STIMULUS_DURATION_TACTILE, handler = mStimuliHandler), null,
                subject.stimuliDelays, ctx, mStimuliHandler)
        }

        mRespButton             = createResponseButton("press", mLayout, ::onPress)
        mRespButton.visibility  = View.INVISIBLE

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // ===================================================================================
    // region CREATE TRIALS
    // ===================================================================================
    private fun createFixedTrials():List<TrialBasic> {
        
        val trials:MutableList<TrialBasic> = mutableListOf()

        val schema =    if(isSupra)     trialsUnimodalSupraSchema
        else                            trialsUnimodalSubSchema

        var temp_trials:MutableList<TrialBasic> = mutableListOf()

        for(i in 0 until N_BLOCKS){
            temp_trials = mutableListOf()
            for(section in schema)
                for(i in 0 until section.ntrials)
                    temp_trials.add(TrialTSP(-1, subject.type, currStimulusLabel, section.magnitude, main_isi, section.isBefore, nCueStimuli, currStimulusDuration))
            temp_trials.shuffle()
            trials.addAll(temp_trials)
        }
        return trials
    }

    private fun createTrialsDebug():List<TrialBasic> {
        val trials: MutableList<TrialBasic> = mutableListOf()

        return trials
    }

    // endregion

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        trialAbortTime = (nCueStimuli+2)*trial.stim_value    // allowed number of ms to wait for user response. after this interval the trial ends

        if(subject.whitenoise == TEST_SWITCH_ENABLED) mNoise?.start()

        if(isRepeat)    mTrial.repetitions++

        mStimuliHandler.postDelayed({
            mRespButton.visibility = VISIBLE
        }, FIRST_STIMULUS_DELAY)

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialTSP)
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
        }, FIRST_STIMULUS_DELAY + 250L)

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialTSP)
        }, (FIRST_STIMULUS_DELAY + trial.stim_value + 250L))

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialTSP)
            trialStartMs = uptimeMillis()
        }, (FIRST_STIMULUS_DELAY + 2*trial.stim_value + 250L))

        mStimuliHandler.postDelayed({
            onStimuliEnd()
        }, (FIRST_STIMULUS_DELAY + trialAbortTime + 250L))
    }

    private fun createResponseButton(txt:String, parent_layout:ConstraintLayout, onPress:() -> Unit): Button {

        parent_layout_width     = parent_layout.width
        parent_layout_height    = parent_layout.height

        mRespButton = AppCompatButton(ctx).apply {
            id              = generateViewId()
            text            = txt
            textAlignment   = TextView.TEXT_ALIGNMENT_CENTER
            gravity         = Gravity.CENTER
            visibility      = VISIBLE

            parent_layout.addView(this)

            if(isLandscape) {
                x = (parent_layout_width*0.8).toFloat()
                y = (parent_layout_height*0.1).toFloat()

                layoutParams.width = (parent_layout_width*0.15).toInt()
                layoutParams.height = (parent_layout_height*0.8).toInt()
            }
            else{
                x = (parent_layout_width*0.1).toFloat()
                y = (parent_layout_height*0.7).toFloat()

                layoutParams.width = (parent_layout_width*0.8).toInt()
                layoutParams.height = (parent_layout_height*0.25).toInt()
            }

            setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
//            setTextAppearance(TextAppearance_AppCompat_Widget_Button_Colored)
            setLinkTextColor(context.resources.getColor(R.color.colorPrimary))
        }
        mRespButton.setOnClickListener {  onPress()  }
        return mRespButton
    }

    private fun onPress(){
        trialEndMs = uptimeMillis() - trialStartMs      // behavioral result
        onStimuliEnd()
    }

    private fun deliverStimulus(trial: TrialTSP){
        when(trial.type) {
            TEST_TSP_A_SUB, TEST_TSP_A_SUPRA ->  mStimuliManager.deliverAStimulus(trial.duration)
            TEST_TSP_V_SUB, TEST_TSP_V_SUPRA ->  mStimuliManager.deliverVStimulus(trial.duration)
            TEST_TSP_T_SUB, TEST_TSP_T_SUPRA ->  mStimuliManager.deliverTStimulus(trial.duration)
        }
    }

    // called by button press or timeout
    override fun onStimuliEnd(){

        mStimuliHandler.removeCallbacksAndMessages(null)
        mRespButton.visibility = View.INVISIBLE
        setAnswer(trialEndMs.toInt())

        super.onStimuliEnd()
    }

    override fun initSummary() {}
}