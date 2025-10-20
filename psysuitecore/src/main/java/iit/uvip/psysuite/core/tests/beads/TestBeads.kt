package iit.uvip.psysuite.core.tests.beads

import android.app.Activity
import android.content.Context
import android.os.SystemClock.uptimeMillis
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentTestBinding
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.ui.fragments.TestFragment
import iit.uvip.psysuite.core.utility.ConditionData

import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.toDp
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import java.lang.Double.POSITIVE_INFINITY


// Behavior flow: show() -> [user interaction/delay] -> onStimuliEnd() -> EVENT_SHOW_NEXT_BUTTON or similar.

/**
 * Manages the "Beads Task" experiment.
 * This test typically involves presenting sequences of beads drawn from one of two jars
 * with different proportions of bead colors. The participant's task is usually to
 * decide from which jar the beads are being drawn, often with the option to draw more beads
 * before making a decision. This test assesses probabilistic reasoning and decision-making under uncertainty.
 *
 * This implementation uses visual stimuli (jars and beads) and button-based responses.
 *
 * @param ctx The application context.
 * @param activity The hosting activity.
 * @param hostfragment The hosting fragment, expected to be a [TestFragment] to access its binding.
 * @param subject The subject details parcel.
 * @param vibrator An optional [VibrationManager] (not directly used in this specific test logic but available from [TestBasic]).
 * @param mImageView An optional [ImageView] for displaying visual stimuli (used here for the jar image).
 * @param speechManager An optional [SpeechManager] (not directly used in this specific test logic but available from [TestBasic]).
 * @param mainView The main view of the test, used for UI manipulation.
 */
class TestBeads(ctx: Context,
                activity: Activity,
                hostfragment: Fragment,
                subject: SubjectBasicParcel,
                vibrator: VibrationManager?,
                mImageView: ImageView?,
                speechManager: SpeechManager?,
                mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView) {

    /**
     * Tag for logging purposes, specific to this class.
     */
    override var LOG_TAG: String = TestBeads::class.java.simpleName

    /**
     * View binding for the fragment, obtained from the hosting [TestFragment].
     */
    private val binding: FragmentTestBinding =  (hostfragment as TestFragment).binding

    /**
     * List of drawable resource IDs for the jar images representing different uncertainty levels.
     * `beads_15_85` for low uncertainty, `beads_40_60` for medium uncertainty.
     */
    override var mDrawablesResource: MutableList<Int> = mutableListOf(
        R.drawable.beads_15_85,
        R.drawable.beads_40_60
    )

    /**
     * Predefined sequences of beads for the trials. Each inner list represents a trial,
     * and each boolean represents a bead type (e.g., true for green, false for yellow).
     */
    private var beads_orders:List<List<Boolean>> = listOf(
        listOf(true, true, true, true,  false, true,  true,  true, true, true,  true,  true, true, true,  true, false, true, true,  false, true),
        listOf(true, true, true, false, true,  true,  true,  true, true, false, false, true, true, true,  true, true,  true, true,  true,  false),
        listOf(true, true, true, true,  true,  true,  false, true, true, false, true,  true, true, false, true, true,  true, false, true,  false),
        listOf(true, true, false, true, true,  false, true, true, true,  true,  true, true, true,  true,  true, true, true,  true,  true,  false)
    )

    /**
     * The number of beads to be drawn in each trial. Derived from the size of the first bead order sequence.
     */
    private val nbeadsXtrial:Int = beads_orders[0].size
    /**
     * Index of the currently displayed bead in the current trial sequence.
     */
    private var currBead:Int = 0

    /**
     * List of [ImageView] objects used to display the individual beads drawn during a trial.
     */
    private var beads_images:MutableList<ImageView> = mutableListOf()

    /**
     * Labels for the buttons used in the UI.
     * Includes labels for choosing Jar 1, Jar 2, drawing a new bead, and deciding to choose a jar.
     */
    private var buttonsLabels: List<String> = mutableListOf(
        ctx.resources.getString(R.string.label_jar1),
        ctx.resources.getString(R.string.label_jar2),
        ctx.resources.getString(R.string.label_new_bead),
        ctx.resources.getString(R.string.label_choose_jar),
    )

    /**
     * Button for the left-side choice (e.g., "New Bead" or "Jar 1").
     */
    private lateinit var button_left:Button
    /**
     * Button for the right-side choice (e.g., "Choose Jar" or "Jar 2").
     */
    private lateinit var button_right: Button

    /**
     * Resource ID of the current jar image being displayed.
     */
    private var currImageRes:Int        = 0
    /**
     * Label for the current condition (e.g., "LU" for Low Uncertainty).
     */
    private var currCondLabel:String    = ""

    /**
     * Visual manager for the main jar image.
     */
    private var currVisual: VisualManager?  = null

    /**
     * Timestamp of when the current trial started, used for logging reaction times.
     */
    private var trialStartMs:Long               = 0L
    // private var parent_layout_width:Int         = binding.root.width // This might be better initialized in setUI or where binding.root.width is guaranteed to be non-zero.

    /**
     * Companion object for TestBeads, holding constants and static methods.
     */
    companion object {
        /**
         * Label for this specific test: "BEADS".
         */
        @JvmStatic val TEST_BASIC_LABEL = "BEADS"

        /**
         * Log identifier for the Low Uncertainty stimulus type.
         */
        @JvmStatic val STIMULUS_TYPE_1_LOG  = "LU"
        /**
         * Log identifier for the Medium Uncertainty stimulus type.
         */
        @JvmStatic val STIMULUS_TYPE_2_LOG  = "MU"

        /**
         * Log identifier for a 'true' bead type (e.g., Green).
         */
        @JvmStatic val BEAD_TYPE_TRUE       = "G"
        /**
         * Log identifier for a 'false' bead type (e.g., Yellow).
         */
        @JvmStatic val BEAD_TYPE_FALSE      = "Y"

        /**
         * Provides a list of available conditions for the Beads test.
         * Each condition has an ID, a label derived from the test and stimulus type, and target populations.
         *
         * @param ctx The application context for accessing resources.
         * @return A list of [ConditionData] objects representing the test conditions.
         */
        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData(STIMULUS_TYPE_1_LOG, TEST_BEADS_LOWUNCERT, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}" , Populations.sighted_populations),
            ConditionData(STIMULUS_TYPE_2_LOG, TEST_BEADS_MIDUNCERT, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}" , Populations.sighted_populations)
        )

        /**
         * Defines the available modes for advancing to the next trial for each condition.
         * For the Beads test, it's set to [TEST_NEXTTRIAL_AUTO], implying progression is handled internally
         * or via specific UI elements rather than a generic "next trial" button after each bead/choice.
         *
         * @param ctx The application context.
         * @return A list of lists, where each inner list contains allowed next trial modes.
         */
        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO)
        )
    }

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    /**
     * Initializes the Beads test.
     * Sets up UI elements, determines the current condition (low or medium uncertainty) based on subject data,
     * prepares trials (either standard or debug), initializes the [StimuliManager] for the jar visual,
     * and creates the result file.
     */
    override fun initTest(){
        validAnswers    = mutableListOf() // Not used for discrete choices via buttons in this test.
        mQuestion       = "" // No single overarching question displayed; interaction is via buttons.

        if(subject.type == TEST_BEADS_LOWUNCERT){
            currImageRes    = mDrawablesResource[0]
            currCondLabel   = STIMULUS_TYPE_1_LOG
        }
        else{
            currImageRes    = mDrawablesResource[1]
            currCondLabel   = STIMULUS_TYPE_2_LOG
        }
        setUI()

        currStimulusDuration    = POSITIVE_INFINITY.toLong() // Jar image remains visible indefinitely.

        val trials = if(!subject.isDebug)  createTrials()
                     else                  createTrialsDebug()

        mTrialsManager = FixedTrialsManager(trials as MutableList<TrialBasic>)

        mTestLabel              = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast(ctx.getString(R.string.error_test_code_not_recognized), ctx)


        createResultFile(TrialBeads.LOG_HEADER) // Assuming TrialBeads class defines LOG_HEADER.
        currVisual      = VisualManager(STIM_V, mImageView!!, (trials[0] as TrialBeads).img_res, duration = POSITIVE_INFINITY.toLong(), handler = mStimuliHandler)
        mStimuliManager = StimuliManager(null, null, currVisual, subject.stimuliDelays, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    /**
     * Creates the standard list of trials for the Beads test.
     * Each trial uses one of the predefined [beads_orders] sequences.
     *
     * @return A list of [TrialBasic] objects, specifically [TrialBeads] instances.
     */
    private fun createTrials():List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        // Assuming TrialBeads constructor: TrialBeads(id, subjectType, conditionLabel, imageResource, beadSequence)
        trials.add(TrialBeads(1, subject.type, currCondLabel, currImageRes, beads_orders[0]))
        trials.add(TrialBeads(2, subject.type, currCondLabel, currImageRes, beads_orders[1]))
        trials.add(TrialBeads(3, subject.type, currCondLabel, currImageRes, beads_orders[2]))
        trials.add(TrialBeads(4, subject.type, currCondLabel, currImageRes, beads_orders[3]))

        return trials
    }

    /**
     * Creates a list of trials for debugging purposes.
     * Currently, this is identical to [createTrials].
     *
     * @return A list of [TrialBasic] objects for debugging.
     */
    private fun createTrialsDebug():List<TrialBasic>{
        return createTrials()
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    /**
     * Handles progression to the next trial or end of the test.
     * If it's the last trial, it saves data and ends the test.
     * If it's the end of a block, it triggers a block end event (currently just `EVENT_BLOCK_END`).
     * Otherwise, it proceeds to the next trial using [doNextTrial].
     */
    override fun onNextTrial(){
        when {
            currTrialID == (nTrials - 1) -> {
                saveText("", notifyDm = true) // Save any final data.
                testEvent.accept(Triple(EVENT_TEST_END, null, listOf())) // Signal test completion.
            }
            mListBlocks.contains(currTrialID) -> { // Handle block endings if defined.
                // Consider implementing a pause or message for block ends.
                // For now, it just calls EVENT_BLOCK_END, which TestBasic might handle.
                testEvent.accept(Triple(EVENT_BLOCK_END, null, listOf()))
                doNextTrial() // Assuming progression even after block end for now.
            }
            else -> doNextTrial() // Proceed to the next trial.
        }
    }

    /**
     * Called when a single trial (sequence of bead draws and decision) ends.
     * Hides response buttons and bead images, stops the main visual (jar),
     * and schedules the "next trial" button to appear after a delay.
     */
    override fun onStimuliEnd(){

        button_left.visibility  = View.INVISIBLE
        button_right.visibility = View.INVISIBLE

        for(b in 0 until nbeadsXtrial)  beads_images[b].visibility = View.INVISIBLE
        currVisual?.stop() // Stop displaying the jar image.
        super.onStimuliEnd()
    }

    /**
     * Initializes the summary data. Currently empty for this test.
     * Could be implemented to calculate statistics based on choices, beads drawn, etc.
     */
    override fun initSummary(){}

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    /**
     * Starts a new trial presentation.
     * Saves the trial log, loads and delivers the main jar visual,
     * prepares bead images (initially invisible), records the trial start time,
     * and schedules the first bead to be shown after a delay.
     *
     * @param trial The [TrialBasic] object for the current trial (expected to be [TrialBeads]).
     * @param isRepeat True if this trial is a repetition, false otherwise (not explicitly used here).
     */
    override fun show(trial: TrialBasic, isRepeat:Boolean){
        try {
            saveText(mTrial.Log(), notifyDm = false) // Log trial start.

            mStimuliManager.mVisualManager!!.load((trial as TrialBeads).img_res)
            mStimuliManager.deliverAlignedStimulus(STIM_V) // Show the jar image.

            for(b in 0 until nbeadsXtrial){
                val bead_res =  if((trial as TrialBeads).beads_types[b])    R.drawable.green_circle
                                else                                        R.drawable.yellow_circle
                beads_images[b].setImageResource(bead_res)
                beads_images[b].visibility = View.INVISIBLE
            }
            trialStartMs = uptimeMillis() // Record start time for RT calculations.

            currBead = -1 // Reset bead counter for the new trial.
            mStimuliHandler.postDelayed({ showBead() }, 2000L) // Show the first bead after a delay.

        }
        catch(e:Exception){
            e.printStackTrace() // Log any exceptions during stimulus presentation.
        }
    }

    /**
     * Shows the next bead in the sequence for the current trial.
     * Logs the bead event, updates button labels and visibility,
     * sets button click listeners for "show next bead" or "choose jar",
     * and makes the current bead image visible.
     * If it's the last bead, the "show next bead" button is hidden.
     */
    private fun showBead(){
        currBead++

        val bead_type   =   if((mTrial as TrialBeads).beads_types[currBead])    BEAD_TYPE_TRUE
                            else                                                BEAD_TYPE_FALSE
        val elapsed = uptimeMillis() - trialStartMs
        saveText("BEAD\t${(currBead + 1)}\t$bead_type\t$elapsed\n", notifyDm = false)

        button_left.text        = buttonsLabels[2] // "New Bead"
        button_right.text       = buttonsLabels[3] // "Choose Jar"

        button_left.visibility  = View.VISIBLE
        button_right.visibility = View.VISIBLE

        button_left.setOnClickListener{ if(currBead < nbeadsXtrial-1) showBead() } // Guard against index out of bounds
        button_right.setOnClickListener{ chooseJar() }

        if (currBead < beads_images.size) { // Ensure currBead is a valid index
            beads_images[currBead].visibility = View.VISIBLE
        }


        if(currBead == nbeadsXtrial-1) // If it's the last possible bead for the trial
            button_left.visibility  = View.INVISIBLE // Hide "New Bead" button
    }

    /**
     * Handles the participant's decision to choose a jar.
     * Hides the "New Bead" / "Choose Jar" buttons,
     * changes button labels to "Jar 1" and "Jar 2",
     * sets click listeners for jar selection, and makes these buttons visible after a delay.
     */
    private fun chooseJar(){
        button_left.visibility  = View.INVISIBLE
        button_right.visibility = View.INVISIBLE

        button_left.text        = buttonsLabels[0] // "Jar 1"
        button_right.text       = buttonsLabels[1] // "Jar 2"

        button_left.setOnClickListener{  jarChosen("a") } // Assuming "a" maps to Jar 1
        button_right.setOnClickListener{ jarChosen("b") } // Assuming "b" maps to Jar 2

        mStimuliHandler.postDelayed({
            button_left.visibility  = View.VISIBLE
            button_right.visibility = View.VISIBLE
        }, 1000L)
    }

    /**
     * Handles the participant's final jar choice.
     * Logs the choice and the elapsed time, then calls [onStimuliEnd] to finish the current trial.
     *
     * @param jar A string representing the chosen jar (e.g., "a" for Jar 1, "b" for Jar 2).
     */
    private fun jarChosen(jar:String){
        val elapsed = uptimeMillis() - trialStartMs
        saveText("CHOOSE\t$jar\t$elapsed\n", notifyDm = false)
        onStimuliEnd()
    }

    // =============================================================================================================================
    // ARRANGE UI
    // =============================================================================================================================
    /**
     * Sets up the User Interface elements for the Beads test.
     * Initializes the main layout, creates the bottom response buttons, and prepares the row for bead images.
     * It uses [FragmentTestBinding] to access UI elements defined in the layout XML.
     */
    private fun setUI() {
        val mainlayout = binding.root // The ConstraintLayout from FragmentTestBinding.
        // parent_layout_width = mainlayout.width // Best to get width when layout is complete.

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)

        // Create buttons for "New Bead"/"Jar 1" and "Choose Jar"/"Jar 2"
        button_left = createBottomButton(buttonsLabels[2], mainlayout, constraintSet, ConstraintSet.START, 24, 16) // "New Bead" initially
        button_right = createBottomButton(buttonsLabels[3], mainlayout, constraintSet, ConstraintSet.END, 24, 16)   // "Choose Jar" initially

        // Apply constraints after both buttons are created and added to the parent.
        constraintSet.applyTo(mainlayout)

        createBeadsRow(binding.hlayout, nbeadsXtrial) // Assuming hlayoutBeads is a LinearLayout in fragment_test.xml for beads.
    }

    /**
     * Creates a single button, typically positioned at the bottom of the screen.
     *
     * @param txt The text label for the button.
     * @param parent_layout The parent [ConstraintLayout] to which this button will be added.
     * @param constr_set The [ConstraintSet] used to define constraints for this button.
     * @param hconstr The horizontal constraint anchor (e.g., [ConstraintSet.START], [ConstraintSet.END]).
     * @param hconstr_marginDp Horizontal margin in Dp.
     * @param vconstr_marginDp Vertical margin from the bottom in Dp.
     * @return The created [Button].
     */
    private fun createBottomButton(txt:String, parent_layout:ConstraintLayout, constr_set:ConstraintSet, hconstr:Int, hconstr_marginDp:Int, vconstr_marginDp:Int):Button{
        val bt = AppCompatButton(ctx).apply {
            id              = View.generateViewId()
            text            = txt
            textAlignment   = TextView.TEXT_ALIGNMENT_CENTER
            gravity         = Gravity.CENTER
            visibility      = View.INVISIBLE // Initially invisible, made visible by showBead/chooseJar.

            // Consider using style attributes for theming instead of direct color setting.
            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary, null)) // Use theme-aware getColor.
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            // setLinkTextColor(ctx.resources.getColor(R.color.colorPrimary, null)) // Not typically needed for Button.
        }
        parent_layout.addView(bt) // Add to parent before constraining.

        // Define constraints for the button.
        constr_set.connect(bt.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, vconstr_marginDp.toDp(ctx))
        constr_set.connect(bt.id, hconstr, ConstraintSet.PARENT_ID, hconstr, hconstr_marginDp.toDp(ctx))
        constr_set.constrainHeight(bt.id, ConstraintSet.WRAP_CONTENT) // Use wrap_content or a fixed Dp height.
        // Example: constr_set.constrainHeight(bt.id, 48.toDp(ctx))
        constr_set.constrainWidth(bt.id, 0) // For spread behavior if constrained to both sides, or set fixed width.
        // Example for a more fixed width, if not stretching between two points:
        // constr_set.constrainWidth(bt.id, 150.toDp(ctx))
        // If you want buttons to be distributed, you might need to constrain them to each other or use guidelines.
        // For two buttons side-by-side, you might constrain START of right button to END of left button.

        // Note: applyTo(parent_layout) should ideally be called once after all views and their constraints are defined for this set.
        // Calling it here applies only this button's constraints. If called in setUI after all buttons, it's more efficient.
        return bt
    }

    /**
     * Creates a row of [ImageView] objects to display the beads.
     *
     * @param layout The [LinearLayout] that will contain the bead ImageViews.
     * @param nBeads The number of bead ImageViews to create.
     * @param res Optional drawable resource ID to pre-set for the ImageViews (not typically used here as beads change).
     */
    private fun createBeadsRow(layout:LinearLayout, nBeads:Int, res:Int = -1){
        beads_images.clear() // Clear any previous bead images.
        for (a in 0 until nBeads) {
            val iv = ImageView(ctx).apply {
                id          = View.generateViewId()
                visibility  = View.INVISIBLE // Beads become visible one by one.
                layoutParams = LinearLayout.LayoutParams(40.toDp(ctx), 40.toDp(ctx)) // Set size in Dp.
                (layoutParams as LinearLayout.LayoutParams).setMargins(2.toDp(ctx), 2.toDp(ctx), 2.toDp(ctx), 2.toDp(ctx))

                if(res > 0){
                    setImageResource(res)
                } else {
                    // Set a placeholder or default background if needed.
                    // setImageResource(R.drawable.empty_bead_placeholder) // Example
                }
            }
            layout.addView(iv)
            beads_images.add(iv)
        }
    }
    // =============================================================================================================================
}
