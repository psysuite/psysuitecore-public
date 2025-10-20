package iit.uvip.psysuite.core.tests.rivgrp

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock.uptimeMillis
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.tests.tfi.TFIBISummary
import iit.uvip.psysuite.core.tests.tfi.TFISummary
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.utility.ConditionData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.toDp
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import java.util.concurrent.TimeUnit


// show -> onStimuliEnd -> EVENT_GIVE_ANSWER

/**
 * Manages the Rivalry and Grouping (RIVGRP) test.
 * This test investigates perceptual phenomena of binocular rivalry and visual grouping,
 * using stimuli that can be interpreted in multiple ways (e.g., house/face, house/car).
 * It supports both discrete (button press) and continuous (finger sliding) response modes.
 *
 * @param ctx The application context.
 * @param activity The host activity.
 * @param hostfragment The fragment hosting this test.
 * @param subject Basic information about the subject undergoing the test.
 * @param vibrator Optional manager for haptic feedback.
 * @param mImageView The ImageView used to display visual stimuli.
 * @param speechManager Optional manager for speech synthesis (not directly used here but part of base class).
 * @param mainView The main view of the test fragment, used for UI manipulation.
 */
class TestRIVGRP(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?,
              mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView) {

    /**
     * Tag used for logging purposes.
     */
    override var LOG_TAG: String = TestRIVGRP::class.java.simpleName

    /**
     * Default auditory stimulus type (though RIVGRP primarily focuses on visual stimuli).
     */
    override var STIM_A  = StimuliManager.STIM_TYPE_A2

    /**
     * Flag to determine if the rivalry condition is presented before the grouping condition in mixed blocks.
     * True if rivalry is first, false otherwise.
     */
    private var isRivalryFirst: Boolean = true

    /**
     * List of drawable resources used for the visual stimuli.
     * Includes images for rivalry (e.g., house/face) and grouping conditions.
     */
    override var mDrawablesResource: MutableList<Int> = mutableListOf(
        R.drawable.rivalry_chouse_rface,
        R.drawable.rivalry_rhouse_cface,
        R.drawable.grouping_house_face_1,
        R.drawable.grouping_house_face_2,
        R.drawable.rivalry_chouse_rcar,
        R.drawable.rivalry_rhouse_ccar,
        R.drawable.grouping_house_car_1,
        R.drawable.grouping_house_car_2
    )

    /**
     * Corresponding names for the drawable resources, used for logging and trial identification.
     */
    private var imagesNames: MutableList<String> = mutableListOf(
        "rivalry_chouse_rface",
        "rivalry_rhouse_cface",
        "grouping_house_face_1",
        "grouping_house_face_2",
        "rivalry_chouse_rcar",
        "rivalry_rhouse_ccar",
        "grouping_house_car_1",
        "grouping_house_car_2"
    )

    /**
     * Labels for the response buttons, adapted based on the stimulus type (House/Face or House/Car).
     * Each pair contains the label for the left and right response options.
     */
    private var buttonsLabels: MutableList<Pair<String, String>> = mutableListOf(
        Pair(ctx.resources.getString(R.string.label_house), ctx.resources.getString(R.string.label_face)),
        Pair(ctx.resources.getString(R.string.label_house), ctx.resources.getString(R.string.label_car))
    )

    /**
     * Offset in dp for positioning response buttons from the midline in discrete response mode.
     */
    private val buttonOffSet:Int = 10   // offset of response buttons from midline

    // continuous response UI elements
    /** TextView for the left response option in continuous mode. */
    private var txt_left:TextView? = null
    /** TextView for the right response option in continuous mode. */
    private var txt_right:TextView? = null
    /** Custom view handling touch input for continuous response. */
    private var view_buttons:View? = null

    // discrete response UI elements
    /** Button for the right response option in discrete mode. */
    private var right_button:Button? = null
    /** Button for the left response option in discrete mode. */
    private var left_button: Button? = null
    /** Tracks if the left button is currently pressed. */
    private var isLeftPressed:Boolean = false
    /** Tracks if the right button is currently pressed. */
    private var isRightPressed:Boolean = false

    /**
     * Name of the current image being displayed or processed.
     */
    private var currImageName:String        = ""
    /**
     * MediaPlayer instance, potentially for auditory components (not actively used in RIVGRP).
     */
    private var currMP: MediaPlayer?        = null
    /**
     * Manages the display and timing of visual stimuli.
     */
    private var currVisual: VisualManager?  = null

    /**
     * Determines the response mode. False for discrete (two buttons), true for continuous (finger sliding).
     */
    private var isContinuosResponse:Boolean     = false
    /**
     * Current horizontal press position in continuous response mode.
     * Ranges from -100 (left edge) to 100 (right edge), with 0 at the center.
     */
    private var pressPosition:Int               = 0
    /**
     * Timestamp in milliseconds (uptimeMillis) when the current trial started.
     */
    private var trialStartMs:Long               = 0L
    /**
     * Disposable for managing the RxJava timer used for response polling.
     */
    private var disposableTimer: Disposable?    = null
    /**
     * Width of the parent layout, used for UI calculations.
     */
    private var parent_layout_width:Int         = 0
    /**
     * Height of the parent layout, used for UI calculations.
     */
    private var parent_layout_height:Int        = 0

    /**
     * Interval in milliseconds for sampling responses in continuous mode.
     */
    private val responseSamplingInterval:Long = 100L

    companion object {
        /**
         * Basic label identifying this test type.
         */
        @JvmStatic val TEST_BASIC_LABEL = "RIVGRP"

        // Test-specific stimulus types (Log versions are typically shorter for data files)
        /** Log representation for House/Face stimulus type. */
        @JvmStatic val STIMULUS_TYPE_1_LOG  = "HF"
        /** Log representation for House/Car stimulus type. */
        @JvmStatic val STIMULUS_TYPE_2_LOG  = "HC"
        /** Full representation for House/Face stimulus type. */
        @JvmStatic val STIMULUS_TYPE_1  = "HOUSE_FACE"
        /** Full representation for House/Car stimulus type. */
        @JvmStatic val STIMULUS_TYPE_2  = "HOUSE_CAR"

        // Test-specific effect types (Log versions)
        /** Log representation for Rivalry effect. */
        @JvmStatic val EFFECT_TYPE_1_LOG  = "RIV"
        /** Log representation for Grouping effect. */
        @JvmStatic val EFFECT_TYPE_2_LOG  = "GRP"

        /**
         * Provides a list of condition data for the RIVGRP test.
         * Each [ConditionData] entry defines a specific sub-test configuration,
         * including its ID, label, and target population.
         *
         * @param ctx The application context.
         * @return A list of [ConditionData] for all RIVGRP variants.
         */
        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_1_LOG}"                      , TEST_RIVGRP_RIV_HF, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_1_LOG}"                         , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}"                      , TEST_RIVGRP_GRP_HF,   "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}"                       , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" , TEST_RIVGRP_RIVGRP_HF, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_1_LOG}"                      , TEST_RIVGRP_RIV_HC, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_1_LOG}"                         , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_2_LOG}"                      , TEST_RIVGRP_GRP_HC,   "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_2_LOG}"                       , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" , TEST_RIVGRP_RIVGRP_HC, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" , Populations.sighted_populations),
        )

        /**
         * Defines the next trial modes for each condition.
         * In RIVGRP, typically there's no choice involved in proceeding to the next trial.
         *
         * @param ctx The application context.
         * @return A list of lists, where each inner list specifies trial progression options.
         */
        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO)
        )
    }

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    /**
     * Initializes the RIVGRP test.
     * Sets up UI elements based on the subject's test type (House/Face or House/Car).
     * Configures stimulus parameters (rivalry first, block duration) from the subject parcel.
     * Creates trials based on the selected test type and response mode (analog/discrete).
     * Initializes the results file, visual stimulus manager, and main stimuli manager.
     * Emits [EVENT_TEST_SETUP_COMPLETED] upon completion.
     */
    override fun initTest(){
        // set question & create mTrials list
        validAnswers    = mutableListOf()
        mQuestion       = ""

        when(subject.type){
            TEST_RIVGRP_RIV_HF,TEST_RIVGRP_GRP_HF,TEST_RIVGRP_RIVGRP_HF -> setUI(buttonsLabels[0]) // House/Face
            else                                                        -> setUI(buttonsLabels[1]) // House/Car
        }
        val subj = (subject as SubjectRIVGRPParcel)

        // subj dialog init
        isRivalryFirst          = subj.rivFirst
        currStimulusDuration    = subj.blockDuration
        // isContinuosResponse can be set via subject parcel if needed, defaulting to false.

        val resp_type:String    =   if(isContinuosResponse)     "analog"
                                    else                        "discrete"
        val trials = if(!subject.isDebug)  createTrials(resp_type)
                     else                  createTrialsDebug(resp_type)
        mTrialsManager = FixedTrialsManager(trials as MutableList<TrialBasic>)

        mTestLabel              = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(TrialRIVGRP.LOG_HEADER)
        currVisual = VisualManager(STIM_V, mImageView!!, (trials[0] as TrialRIVGRP).img_res, duration = currStimulusDuration, handler = mStimuliHandler)
        mStimuliManager = StimuliManager(
            null, //AudioManager(StimuliManager.STIM_TYPE_A2, "",  duration = currStimulusDuration, ctx = ctx, handler = mStimuliHandler),
            null,
            currVisual,
            subject.stimuliDelays, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    /**
     * Initializes the summary view for the test.
     * For RIVGRP, it defaults to [TFIBISummary], though the constant names suggest TFI.
     * This might need review if specific RIVGRP summary is required.
     */
    override fun initSummary() {
        mSummary = when(subject.type)
        {
            // These constants seem to be from TestTFI. RIVGRP might need its own summary or use a generic one.
            TEST_TFI, TEST_TFI_TODDLERS ->  TFISummary(ctx)
            else                        ->  TFIBISummary(ctx) // Defaulting to TFIBISummary for RIVGRP
        }
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    /**
     * Creates a list of trials for the RIVGRP test based on the subject's specific test type
     * (e.g., Rivalry House/Face, Grouping House/Car, or combined).
     *
     * @param resp_type A string indicating the response type ("analog" or "discrete").
     * @return A list of [TrialBasic] objects configured for the current test.
     */
    private fun createTrials(resp_type:String):List<TrialBasic>{

        val trials:MutableList<TrialBasic> = mutableListOf()
        when(subject.type){
            TEST_RIVGRP_RIV_HF  -> {
                currImageName = "${STIMULUS_TYPE_1_LOG}${EFFECT_TYPE_1_LOG}" // HF_RIV
                trials.add(TrialRIVGRP(1,1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                trials.add(TrialRIVGRP(2,1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                trials.add(TrialRIVGRP(3,1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                trials.add(TrialRIVGRP(4,1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
            }
            TEST_RIVGRP_GRP_HF     -> {
                currImageName = "${STIMULUS_TYPE_1_LOG}${EFFECT_TYPE_2_LOG}" // HF_GRP
                trials.add(TrialRIVGRP(1,2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                trials.add(TrialRIVGRP(2,2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                trials.add(TrialRIVGRP(3,2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                trials.add(TrialRIVGRP(4,2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
            }
            TEST_RIVGRP_RIVGRP_HF  -> {
                currImageName = "${STIMULUS_TYPE_1_LOG}${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" // HF_RIV_GRP
                if(isRivalryFirst) {
                    // Rivalry -> Grouping -> Rivalry -> Grouping ...
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type)) // Repeated block
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                }else{
                    // Grouping -> Rivalry -> Grouping -> Rivalry ...
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type)) // Repeated block
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                }
            }
            TEST_RIVGRP_RIV_HC     -> {
                currImageName = "${STIMULUS_TYPE_2_LOG}${EFFECT_TYPE_1_LOG}" // HC_RIV
                trials.add(TrialRIVGRP(1,1, currImageName, mDrawablesResource[4],imagesNames[4], resp_type))
                trials.add(TrialRIVGRP(2,1, currImageName, mDrawablesResource[5],imagesNames[5], resp_type))
                trials.add(TrialRIVGRP(3,1, currImageName, mDrawablesResource[4],imagesNames[4], resp_type))
                trials.add(TrialRIVGRP(4,1, currImageName, mDrawablesResource[5],imagesNames[5], resp_type))
            }
            TEST_RIVGRP_GRP_HC  -> {
                currImageName = "${STIMULUS_TYPE_2_LOG}${EFFECT_TYPE_2_LOG}" // HC_GRP
                trials.add(TrialRIVGRP(1,2, currImageName, mDrawablesResource[6],imagesNames[6], resp_type))
                trials.add(TrialRIVGRP(2,2, currImageName, mDrawablesResource[7],imagesNames[7], resp_type))
                trials.add(TrialRIVGRP(3,2, currImageName, mDrawablesResource[6],imagesNames[6], resp_type))
                trials.add(TrialRIVGRP(4,2, currImageName, mDrawablesResource[7],imagesNames[7], resp_type))
            }
            else                -> { // TEST_RIVGRP_RIVGRP_HC assumed
                currImageName = "${STIMULUS_TYPE_2_LOG}${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" // HC_RIV_GRP
                if(isRivalryFirst) {
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[4],imagesNames[4], resp_type))
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[7],imagesNames[7], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[5],imagesNames[5], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[6],imagesNames[6], resp_type))
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[4],imagesNames[4], resp_type))
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[7],imagesNames[7], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[5],imagesNames[5], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[6],imagesNames[6], resp_type))
                }else{
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[7],imagesNames[7], resp_type))
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[4],imagesNames[4], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[6],imagesNames[6], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[5],imagesNames[5], resp_type))
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[7],imagesNames[7], resp_type))
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[4],imagesNames[4], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[6],imagesNames[6], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[5],imagesNames[5], resp_type))
                }
            }
        }
        return trials
    }

    /**
     * Creates a list of trials for debugging purposes.
     * Currently, this method defaults to calling [createTrials], so debug trials are the same as standard trials.
     *
     * @param resp_type A string indicating the response type ("analog" or "discrete").
     * @return A list of [TrialBasic] objects.
     */
    private fun createTrialsDebug(resp_type:String):List<TrialBasic>{
        // For RIVGRP, debug trials are currently the same as regular trials.
        // This can be expanded to create a shorter or specific set of trials for debugging.
        return createTrials(resp_type)
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    /**
     * Handles the progression to the next trial.
     * If it's the last trial, it saves the final data and emits [EVENT_TEST_END].
     * If it's the end of a block (not explicitly handled here beyond a placeholder event), it would manage block transitions.
     * Otherwise, it calls [doNextTrial] from the base class to proceed.
     */
    override fun onNextTrial(){

        // if !last trial && !block end => doNextTrial
        when {
            currTrialID == (nTrials - 1) -> { // Last trial
                saveText("", notifyDm = true) // Save any remaining data and notify data manager
                testEvent.accept(Triple(EVENT_TEST_END, null, listOf()))            // END !
            }
            mListBlocks.contains(currTrialID) -> { // End of a block
                // Handle end of block logic, e.g., show a break message
                // Currently, this just sends an event that might not be actively handled.
                // Consider adding specific block end UI or logic here if needed.
                testEvent.accept(Triple(EVENT_BLOCK_END, null, listOf()))
                // Typically, after a block end, you might want to call doNextTrial() or show a specific UI.
                // For now, let's assume it proceeds to the next trial if not handled by EVENT_BLOCK_END observers.
                 doNextTrial() // Or show a break screen and then call doNextTrial
            }
            else -> doNextTrial() // Proceed to the next trial
        }
    }

    /**
     * Called when a trial ends (e.g., stimulus duration is over).
     * Hides response UI elements (buttons or continuous response views).
     * Stops response polling and any media playback.
     * Schedules [EVENT_SHOW_NEXT_BUTTON] to be emitted after a short delay.
     */
    override fun onStimuliEnd(){

        if(isContinuosResponse) {
            txt_left?.visibility        = View.INVISIBLE
            txt_right?.visibility       = View.INVISIBLE
            view_buttons?.visibility    = View.INVISIBLE
        }
        else {
            left_button?.visibility     = View.INVISIBLE
            right_button?.visibility    = View.INVISIBLE
        }

        stopPolling()
        currMP?.stop() // Stop media player if used
        currVisual?.stop() // Stop visual stimulus presentation

        super.onStimuliEnd()
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    /**
     * Shows the stimulus for the current trial.
     * Saves the current trial's log data.
     * Makes response UI elements visible.
     * Loads and displays the visual stimulus.
     * Schedules [onStimuliEnd] to be called after [currStimulusDuration].
     * Starts response polling.
     * Records the trial start time.
     *
     * @param trial The [TrialBasic] (specifically [TrialRIVGRP]) for the current trial.
     * @param isRepeat True if this trial is a repeat, false otherwise (not directly used here).
     */
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        try {
            saveText(mTrial.Log(), notifyDm = false) // Save trial log without immediate DM notification

            if(isContinuosResponse) {
                txt_left?.visibility        = View.VISIBLE
                txt_right?.visibility       = View.VISIBLE
                view_buttons?.visibility    = View.VISIBLE
            }
            else {
                left_button?.visibility     = View.VISIBLE
                right_button?.visibility    = View.VISIBLE
            }

            mStimuliManager.mVisualManager!!.load((trial as TrialRIVGRP).img_res)
            mStimuliHandler.postDelayed({ onStimuliEnd() }, currStimulusDuration) // Schedule end of trial
            mStimuliManager.deliverAlignedStimulus(STIM_V) // Present visual stimulus

            startPolling(responseSamplingInterval) // Start polling for responses
            trialStartMs = uptimeMillis() // Record trial start time
        }
        catch(e:Exception){
            e.printStackTrace() // Log any exceptions during stimulus presentation
        }
    }

    // =============================================================================================================================
    // ARRANGE UI
    // =============================================================================================================================
    /**
     * Sets up the main UI elements for the test, including the stimulus ImageView
     * and response mechanisms (discrete buttons or continuous response area).
     * This method is called during [initTest].
     *
     * @param labels A pair of strings for the left and right response options.
     */
    private fun setUI(labels:Pair<String, String>) {

        val mainlayout: ConstraintLayout = mainView!!.findViewById(R.id.fragment_test_layout)
        // Ensure parent_layout_width and parent_layout_height are captured after layout inflation.
        // If they are 0, it means the view hasn't been measured yet. Consider using View.post { ... }
        if (mainlayout.width == 0 || mainlayout.height == 0) {
            mainlayout.post {
                parent_layout_width = mainlayout.width
                parent_layout_height = mainlayout.height
                configureImageViewLayout(mainlayout)
                if (isContinuosResponse) createContinuousResponse(labels, mainlayout)
                else createDiscreteResponse(labels, mainlayout)
            }
        } else {
            parent_layout_width = mainlayout.width
            parent_layout_height = mainlayout.height
            configureImageViewLayout(mainlayout)
            if (isContinuosResponse) createContinuousResponse(labels, mainlayout)
            else createDiscreteResponse(labels, mainlayout)
        }
    }

    /**
     * Configures the layout parameters for the main ImageView that displays stimuli.
     * Scales and positions the ImageView within the provided ConstraintLayout.
     * @param mainlayout The ConstraintLayout containing the ImageView.
     */
    private fun configureImageViewLayout(mainlayout: ConstraintLayout) {
        val img_scale           = 0.75F
        val border_scale:Float  = (1 - img_scale)/2

        mImageView!!.layoutParams = ConstraintLayout.LayoutParams((parent_layout_width*img_scale).toInt(), (parent_layout_height*img_scale).toInt())
        mImageView.id           = View.generateViewId() // Ensure ImageView has an ID for constraints

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)
        // Center the ImageView or position it as required
        constraintSet.connect(mImageView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (border_scale*parent_layout_height).toInt())
        constraintSet.connect(mImageView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (border_scale*parent_layout_height).toInt())
        constraintSet.connect(mImageView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (border_scale*parent_layout_width).toInt())
        constraintSet.connect(mImageView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (border_scale*parent_layout_width).toInt())
        // Adjust constraints to ensure proper scaling and centering.
        // The original connect to LEFT and BOTTOM might not center it as intended with scaling.
        // Using TOP, BOTTOM, START, END with margins or bias might be more robust for centering.
        // For simplicity, sticking to original logic but noting potential improvement.
        // Original:
        // constraintSet.connect(mImageView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, (border_scale*parent_layout_width).toInt())
        // constraintSet.connect(mImageView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (border_scale*parent_layout_height).toInt())
        constraintSet.applyTo(mainlayout)
    }


    /**
     * Creates and configures UI elements for discrete responses (two separate buttons).
     *
     * @param labels A pair of strings for the left and right button labels.
     * @param mainlayout The parent ConstraintLayout to which buttons are added.
     */
    private fun createDiscreteResponse(labels:Pair<String, String>, mainlayout:ConstraintLayout){

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)

        // LEFT BUTTON
        left_button = Button(ctx).apply {
            id              = View.generateViewId()
            text            = labels.first
            textAlignment   = TextView.TEXT_ALIGNMENT_CENTER // Modern text alignment
            gravity         = Gravity.CENTER

            // Consider using style attributes for theming
            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary)) // Use theme attributes if possible
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            // setLinkTextColor is unusual for a button; ensure this is intended.
            // setTextColor might be more appropriate.
            // setLinkTextColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextColor(ctx.resources.getColor(android.R.color.white)) // Example: White text on primary color

            setOnTouchListener{ _, event ->
                val action = event.action
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        isLeftPressed = true
                        performClick() // For accessibility and standard button behavior
                    }
                    MotionEvent.ACTION_UP -> {
                        isLeftPressed = false
                    }
                }
                true // Consume the touch event
            }
        }
        mainlayout.addView(left_button)
        constraintSet.connect(left_button!!.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(left_button!!.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0)
        constraintSet.constrainHeight(left_button!!.id, 100.toDp(ctx))
        constraintSet.constrainWidth(left_button!!.id, (parent_layout_width/2) - 4*buttonOffSet)
        //constraintSet.applyTo(mainlayout) // Apply after all views in this set are configured

        // RIGHT BUTTON
        right_button = Button(ctx).apply {
            id              = View.generateViewId()
            text            = labels.second
            textAlignment   = TextView.TEXT_ALIGNMENT_CENTER
            gravity         = Gravity.CENTER

            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            // setLinkTextColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextColor(ctx.resources.getColor(android.R.color.white))

            setOnTouchListener{ _, event ->
                val action = event.action
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        isRightPressed = true
                        performClick()
                    }
                    MotionEvent.ACTION_UP -> {
                        isRightPressed = false
                    }
                }
                true
            }
        }
        mainlayout.addView(right_button)
        constraintSet.connect(right_button!!.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(right_button!!.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
        constraintSet.constrainHeight(right_button!!.id, 100.toDp(ctx))
        constraintSet.constrainWidth(right_button!!.id, (parent_layout_width/2) - 4*buttonOffSet)

        constraintSet.applyTo(mainlayout) // Apply all constraints at once
    }

    /**
     * Creates and configures UI elements for continuous responses (touch-sensitive area with labels).
     *
     * @param labels A pair of strings for the left and right area labels.
     * @param mainlayout The parent ConstraintLayout to which elements are added.
     */
    private fun createContinuousResponse(labels:Pair<String, String>, mainlayout:ConstraintLayout){

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)

        // Touch-sensitive view spanning the response area
        view_buttons = View(ctx).apply {
            id = View.generateViewId()
            setBackgroundColor(ctx.resources.getColor(android.R.color.transparent)) // Make it transparent or styled
            setOnTouchListener{ _, event -> // v is the view itself
                val action = event.action
                pressPosition = when(action){
                    MotionEvent.ACTION_DOWN -> {
                        performClick() // For accessibility
                        getPressedButton(event)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        getPressedButton(event)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { // Handle cancel as well
                        TEST_NO_LONGITUDINAL // Sentinel value indicating no press or end of press
                    }
                    else -> pressPosition // Keep current position for other actions
                }
                true // Consume touch event
            }
        }
        mainlayout.addView(view_buttons)
        constraintSet.connect(view_buttons!!.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(view_buttons!!.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0)
        constraintSet.connect(view_buttons!!.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
        constraintSet.constrainHeight(view_buttons!!.id, 100.toDp(ctx))
        constraintSet.constrainWidth(view_buttons!!.id, ConstraintSet.MATCH_CONSTRAINT) // Span width
        // constraintSet.applyTo(mainlayout) // Apply after all views

        // TextView for the left label
        txt_left = TextView(ctx).apply {
            id = View.generateViewId()
            text = labels.first
            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            setTextColor(ctx.resources.getColor(android.R.color.white)) // Ensure text is visible
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
            isClickable = false // Not directly interactive
            isFocusable = false
        }
        mainlayout.addView(txt_left)
        constraintSet.connect(txt_left!!.id, ConstraintSet.BOTTOM, view_buttons!!.id, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(txt_left!!.id, ConstraintSet.TOP, view_buttons!!.id, ConstraintSet.TOP, 0)
        constraintSet.connect(txt_left!!.id, ConstraintSet.LEFT, view_buttons!!.id, ConstraintSet.LEFT, 0)
        constraintSet.constrainHeight(txt_left!!.id, ConstraintSet.MATCH_CONSTRAINT) // Match height of view_buttons
        constraintSet.constrainWidth(txt_left!!.id, (parent_layout_width/2) - buttonOffSet) // Half width minus offset
        // constraintSet.applyTo(mainlayout)

        // TextView for the right label
        txt_right = TextView(ctx).apply {
            id = View.generateViewId()
            text = labels.second
            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            setTextColor(ctx.resources.getColor(android.R.color.white))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
            isClickable = false
            isFocusable = false
        }
        mainlayout.addView(txt_right)
        constraintSet.connect(txt_right!!.id, ConstraintSet.BOTTOM, view_buttons!!.id, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(txt_right!!.id, ConstraintSet.TOP, view_buttons!!.id, ConstraintSet.TOP, 0)
        constraintSet.connect(txt_right!!.id, ConstraintSet.RIGHT, view_buttons!!.id, ConstraintSet.RIGHT, 0)
        constraintSet.constrainHeight(txt_right!!.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainWidth(txt_right!!.id, (parent_layout_width/2) - buttonOffSet)

        constraintSet.applyTo(mainlayout) // Apply all constraints
    }

    //============================================================================================
    // MANAGE POLLING
    //============================================================================================

    /**
     * Calculates the normalized horizontal position of a touch event relative to the screen width.
     * Output ranges from -100 (far left) to 100 (far right), with 0 being the center.
     *
     * @param me The MotionEvent to analyze.
     * @return An integer representing the normalized horizontal position.
     */
    private fun getPressedButton(me:MotionEvent):Int{
        if (parent_layout_width == 0) return 0 // Avoid division by zero if layout not measured
        return (((me.rawX - (parent_layout_width.toFloat()/2)) / (parent_layout_width.toFloat()/2))*100).toInt()
    }

    /**
     * Records the current response state (either continuous position or discrete button presses)
     * along with the elapsed time since the trial started. This data is saved to the results file.
     */
    private fun setPressedButton(){

        val elapsed = uptimeMillis() - trialStartMs

        val response:Int =   if(isContinuosResponse)   pressPosition
                            else{ // Discrete response encoding
                                if (isLeftPressed && isRightPressed)        3 // Both pressed (should ideally not happen or be handled)
                                else if(isLeftPressed && !isRightPressed)   1 // Left pressed
                                else if(!isLeftPressed && isRightPressed)   2 // Right pressed
                                else                                        0 // No button pressed
                            }
//        testEvent.accept(Triple(EVENT_SHOW_DEBUGINFO, "pressed button: $response"))

        saveText("$elapsed\t$response\n", notifyDm = false) // Save time and response
    }


    /**
     * Starts polling for responses at a regular interval.
     * Uses an RxJava Observable to call [setPressedButton] repeatedly.
     *
     * @param iti The inter-trial interval (here used as polling interval) in milliseconds.
     */
    private fun startPolling(iti: Long){ // iti parameter name might be confusing, it's a polling interval here
        stopPolling() // Ensure any existing timer is stopped before starting a new one.
        disposableTimer = Observable.interval(iti, iti, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( { _: Long           -> setPressedButton() }, // aLong is the emission count, not used
                        { throwable: Throwable  ->
                            // Handle errors in the stream, e.g., log them
                            // Log.e(LOG_TAG, "Error in response polling", throwable)
                        })
    }

    /**
     * Stops the response polling timer by disposing of the RxJava Disposable.
     */
    private fun stopPolling() {
        disposableTimer?.dispose()
        disposableTimer = null
    }
    // =============================================================================================================================
}
