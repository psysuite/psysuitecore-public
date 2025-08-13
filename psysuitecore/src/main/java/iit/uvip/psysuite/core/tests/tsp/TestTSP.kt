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
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
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


class TestTSP(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?,
              private val mainView: View
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView)  {

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

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("${STIMULUS_TYPE_VISUAL_LOG}_${STIMULUS_ISI_SUB}",     TEST_TSP_V_SUB,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_LOG}_${STIMULUS_ISI_SUB}" ,   Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_AUDIO_LOG}_${STIMULUS_ISI_SUB}",     TEST_TSP_A_SUB,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_LOG}_${STIMULUS_ISI_SUB}" ,   Populations.hearing_populations),
            ConditionData("${STIMULUS_TYPE_TACTILE_LOG}_${STIMULUS_ISI_SUB}",     TEST_TSP_T_SUB,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE_LOG}_${STIMULUS_ISI_SUB}" ,   Populations.all_populations),
            ConditionData("${STIMULUS_TYPE_VISUAL_LOG}_${STIMULUS_ISI_SUPRA}",   TEST_TSP_V_SUPRA,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_LOG}_${STIMULUS_ISI_SUPRA}" , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_AUDIO_LOG}_${STIMULUS_ISI_SUPRA}",   TEST_TSP_A_SUPRA,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO_LOG}_${STIMULUS_ISI_SUPRA}" , Populations.hearing_populations),
            ConditionData("${STIMULUS_TYPE_TACTILE_LOG}_${STIMULUS_ISI_SUPRA}",   TEST_TSP_T_SUPRA,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE_LOG}_${STIMULUS_ISI_SUPRA}" , Populations.all_populations),
        )

        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(
            listOf(TEST_NEXTTRIAL_NOCHOOSE),
            listOf(TEST_NEXTTRIAL_NOCHOOSE),
            listOf(TEST_NEXTTRIAL_NOCHOOSE),
            listOf(TEST_NEXTTRIAL_NOCHOOSE),
            listOf(TEST_NEXTTRIAL_NOCHOOSE),
            listOf(TEST_NEXTTRIAL_NOCHOOSE)
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

    private var curr_trial_stimvalue:Long             = 0L     // isi within the cue sequence and base target
    private var main_isi:Long                   = 0L     // isi within the cue sequence and base target
    private var nCueStimuli:Int                 = 3      // num of cue stimuli

    private val isSupra: Boolean = (subject.type == TEST_TSP_V_SUPRA || subject.type == TEST_TSP_A_SUPRA || subject.type == TEST_TSP_T_SUPRA)

    private val trialAbortTime:Long
        get()       = (nCueStimuli+2)*curr_trial_stimvalue    // allowed number of ms to wait for user response. after this interval the trial ends

    private var trialStartMs:Long               = 0L                    // trial onset
    private var trialEndMs:Long                 = trialAbortTime        // user press latency

    override fun initTest(){

        mLayout = binding.root

        when {
            mImageView == null && (subject.type == TEST_TSP_V_SUB || subject.type == TEST_TSP_V_SUPRA)  -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null && (subject.type == TEST_TSP_T_SUB || subject.type == TEST_TSP_T_SUPRA)    -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }
        // set question & create mTrials list
        validAnswers    = mutableListOf()
        mQuestion       = ""
        abortMode       = TEST_ABORT_TRIALEND   // show abort button after each trial

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
        createResultFile(LOG_HEADER)

        mStimuliManager = when(subject.type){

            TEST_TSP_V_SUB, TEST_TSP_V_SUPRA -> {
                StimuliManager(null, null,
                    VisualManager(STIM_T, mImageView!!, currImageRes, duration = currStimulusDuration, handler = mStimuliHandler),
                    delaysAligner, ctx, mStimuliHandler)
            }
            TEST_TSP_A_SUB, TEST_TSP_A_SUPRA -> {
                StimuliManager(AudioManager(STIM_A, audioResources[STIMULUS_DURATION_AUDIO] ?: "t1000hz_50ms.wav",  duration = STIMULUS_DURATION_AUDIO, handler = mStimuliHandler, ctx = ctx), null,null,
                    delaysAligner, ctx, mStimuliHandler)
            }
            else -> StimuliManager(null, TactileManager(vibrator!!, duration = STIMULUS_DURATION_TACTILE, handler = mStimuliHandler), null,
                delaysAligner, ctx, mStimuliHandler)
        }
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

        mRespButton = createResponseButton("press", mLayout, ::onPress)
        if(subject.whitenoise == TEST_SWITCH_ENABLED) mNoise?.start()

        if(isRepeat)    mTrial.repetitions++

        curr_trial_stimvalue = trial.stim_value

        mStimuliHandler.postDelayed({
            trialStartMs = uptimeMillis()
            deliverStimulus(trial as TrialTSP)
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
        }, FIRST_STIMULUS_DELAY)

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialTSP)
        }, (FIRST_STIMULUS_DELAY + curr_trial_stimvalue))

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialTSP)
        }, (FIRST_STIMULUS_DELAY + 2*curr_trial_stimvalue))

        mStimuliHandler.postDelayed({
            onTrialEnd()
        }, (FIRST_STIMULUS_DELAY + trialAbortTime))
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
        mRespButton.setOnClickListener {
            onPress()
        }
        return mRespButton
    }

    private fun deliverStimulus(trial: TrialTSP){
        when(trial.type) {
            TEST_TSP_A_SUB, TEST_TSP_A_SUPRA ->  mStimuliManager.deliverAStimulus()
            TEST_TSP_V_SUB, TEST_TSP_V_SUPRA ->  mStimuliManager.deliverVStimulus()
            TEST_TSP_T_SUB, TEST_TSP_T_SUPRA ->  mStimuliManager.deliverTStimulus()
        }
    }

    private fun onPress(){
        trialEndMs = uptimeMillis() - trialStartMs      // behavioral result
        onTrialEnd()
    }

    // called by button press or timeout
    override fun onTrialEnd(){
        mStimuliHandler.removeCallbacksAndMessages(null)
        mLayout.removeView(mRespButton)

        onAnswerGiven(trialEndMs.toInt(), trialEndMs)
        mStimuliHandler.postDelayed({ testEvent.accept(Triple(EVENT_SHOW_ABORT, null, listOf())) }, 500L)
    }

    override fun initSummary() {}
}