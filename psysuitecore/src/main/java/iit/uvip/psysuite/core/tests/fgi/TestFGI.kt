// GEMINI_FGI_ACTIVE_SECOND_TRY
package iit.uvip.psysuite.core.tests.fgi

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock.uptimeMillis
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.AudioManager
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.utility.ConditionData
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast

import androidx.constraintlayout.widget.ConstraintLayout
import iit.uvip.psysuite.core.R
import androidx.constraintlayout.widget.ConstraintSet
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

import org.albaspazio.core.accessory.toDp
import java.util.concurrent.TimeUnit
import android.widget.TextView
import iit.uvip.psysuite.core.stimuli.ImageViewDefinedException
import iit.uvip.psysuite.core.trials.FixedTrialsManager


// show -> onStimuliEnd -> EVENT_GIVE_ANSWER

/**
* hi
*/
class TestFGI(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?,
              mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView) {

    override var LOG_TAG: String = TestFGI::class.java.simpleName

    override var mDrawablesResource: MutableList<Int> = mutableListOf(
        R.drawable.fig_fv,
        R.drawable.fig_fv_scr,
        R.drawable.fig_yow,
        R.drawable.fig_yow_scr,
        R.drawable.fig_wm,
        R.drawable.fig_wm_scr
    )

    private var buttonsLabels: MutableList<Pair<String, String>> = mutableListOf(
        Pair(ctx.resources.getString(R.string.label_faces), ctx.resources.getString(R.string.label_jar)),
        Pair(ctx.resources.getString(R.string.label_faces), ctx.resources.getString(R.string.label_jar)),
        Pair(ctx.resources.getString(R.string.label_youngwoman), ctx.resources.getString(R.string.label_elderlywoman)),
        Pair(ctx.resources.getString(R.string.label_youngwoman), ctx.resources.getString(R.string.label_elderlywoman)),
        Pair(ctx.resources.getString(R.string.label_musician), ctx.resources.getString(R.string.label_woman)),
        Pair(ctx.resources.getString(R.string.label_musician), ctx.resources.getString(R.string.label_woman))
    )

    val mAudioResourcesNames:List<String> = listOf(
        "audio_dog.mp3",
        "audio_music.mp3",
        "audio_oldwoman_speaking.mp3",
        "audio_youngwoman_speaking.mp3",
        "audio_man_woman_speaking.mp3",
        "audio_woman_man_speaking.mp3"
    )

    lateinit var txt_left:TextView
    lateinit var txt_right:TextView
    lateinit var view_buttons:View

    var currImageName:String        = ""
    var currMP: MediaPlayer?        = null
    var currVisual: VisualManager?  = null

    private var currPressedButton:String        = "0"       // 0: none pressed, 1:left, 2:right
    private var trialStartMs:Long               = 0L
    private var disposableTimer: Disposable?    = null
    private var parent_layout_width:Int         = 0

    private val responseSamplingInterval:Long = 100L

    companion object {
        // Overrides
        @JvmStatic val TEST_BASIC_LABEL = "FGI"
        @JvmStatic val TRIAL_DURATION  = 60000L

        // Test-specific stimulus types
        @JvmStatic val STIMULUS_TYPE_SCRAMBLED      = "_SCR"
        @JvmStatic val STIMULUS_TYPE_UNSCRAMBLED    = ""
        @JvmStatic val STIMULUS_TYPE_1  = "FACCE/VASO"
        @JvmStatic val STIMULUS_TYPE_2  = "D. GIOVANE/ANZIANA"
        @JvmStatic val STIMULUS_TYPE_3  = "MUSICISTA/DONNA"

        // Test-specific stimulus logs
        @JvmStatic val STIMULUS_TYPE_1_LOG  = "FV"
        @JvmStatic val STIMULUS_TYPE_2_LOG  = "YOW"
        @JvmStatic val STIMULUS_TYPE_3_LOG  = "MW"

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("${STIMULUS_TYPE_1}${STIMULUS_TYPE_UNSCRAMBLED}" , TEST_FGI_1_UNSCRAMBLED, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}${STIMULUS_TYPE_UNSCRAMBLED}" , Populations.sighted_hearing_populations),
            ConditionData("${STIMULUS_TYPE_1}${STIMULUS_TYPE_SCRAMBLED}"   , TEST_FGI_1_SCRAMBLED,   "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}${STIMULUS_TYPE_SCRAMBLED}"   , Populations.sighted_hearing_populations),
            ConditionData("${STIMULUS_TYPE_2}${STIMULUS_TYPE_UNSCRAMBLED}" , TEST_FGI_2_UNSCRAMBLED, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}${STIMULUS_TYPE_UNSCRAMBLED}" , Populations.sighted_hearing_populations),
            ConditionData("${STIMULUS_TYPE_2}${STIMULUS_TYPE_SCRAMBLED}"   , TEST_FGI_2_SCRAMBLED,   "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}${STIMULUS_TYPE_SCRAMBLED}"   , Populations.sighted_hearing_populations),
            ConditionData("${STIMULUS_TYPE_3}${STIMULUS_TYPE_UNSCRAMBLED}" , TEST_FGI_3_UNSCRAMBLED, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_3_LOG}${STIMULUS_TYPE_UNSCRAMBLED}" , Populations.sighted_hearing_populations),
            ConditionData("${STIMULUS_TYPE_3}${STIMULUS_TYPE_SCRAMBLED}"   , TEST_FGI_3_SCRAMBLED,   "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_3_LOG}${STIMULUS_TYPE_SCRAMBLED}"   , Populations.sighted_hearing_populations)
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

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){

        // sanity checks
        when {
            mainView == null -> throw ImageViewDefinedException("MAIN_VIEW_NOT_DEFINED")
        }

        // set question & create mTrials list
        validAnswers    = mutableListOf()
        mQuestion       = ""

        val onImageRes:Int = when(subject.type){
            TEST_FGI_1_UNSCRAMBLED  -> {
                currImageName = "${STIMULUS_TYPE_1_LOG}${STIMULUS_TYPE_UNSCRAMBLED}"
                setUI(buttonsLabels[0])
                mDrawablesResource[0]
            }
            TEST_FGI_1_SCRAMBLED    -> {
                currImageName = "${STIMULUS_TYPE_1_LOG}${STIMULUS_TYPE_SCRAMBLED}"
                setUI(buttonsLabels[1])
                mDrawablesResource[1]
            }
            TEST_FGI_2_UNSCRAMBLED  -> {
                currImageName = "${STIMULUS_TYPE_2_LOG}${STIMULUS_TYPE_UNSCRAMBLED}"
                setUI(buttonsLabels[2])
                mDrawablesResource[2]
            }
            TEST_FGI_2_SCRAMBLED    -> {
                currImageName = "${STIMULUS_TYPE_2_LOG}${STIMULUS_TYPE_SCRAMBLED}"
                setUI(buttonsLabels[3])
                mDrawablesResource[3]
            }
            TEST_FGI_3_UNSCRAMBLED  -> {
                currImageName = "${STIMULUS_TYPE_3_LOG}${STIMULUS_TYPE_UNSCRAMBLED}"
                setUI(buttonsLabels[4])
                mDrawablesResource[4]
            }
            else                    -> {
                currImageName = "${STIMULUS_TYPE_3_LOG}${STIMULUS_TYPE_SCRAMBLED}"
                setUI(buttonsLabels[5])
                mDrawablesResource[5]
            }
        }
        val trials =    if(!subject.isDebug)  createTrials()
                        else                  createTrialsDebug()
        mTrialsManager = FixedTrialsManager(trials as MutableList<TrialBasic>)

        currStimulusDuration    = TRIAL_DURATION
        mTestLabel              = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(TrialFGI.LOG_HEADER)
        currVisual = VisualManager(STIM_V, mImageView!!, onImageRes, duration = currStimulusDuration, handler = mStimuliHandler)
        mStimuliManager = StimuliManager(
            AudioManager(StimuliManager.STIM_TYPE_A2, "",  duration = currStimulusDuration, ctx = ctx, handler = mStimuliHandler),
            null,
            currVisual,
            subject.stimuliDelays, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrials():List<TrialBasic>{

        val audios:List<String> = when(subject.type){
            TEST_FGI_1_UNSCRAMBLED, TEST_FGI_1_SCRAMBLED  -> listOf("", mAudioResourcesNames[0], mAudioResourcesNames[1], mAudioResourcesNames[4], mAudioResourcesNames[0], "", mAudioResourcesNames[4], mAudioResourcesNames[1])
            TEST_FGI_2_UNSCRAMBLED, TEST_FGI_2_SCRAMBLED  -> listOf("", mAudioResourcesNames[3], mAudioResourcesNames[1], mAudioResourcesNames[2], mAudioResourcesNames[3], "", mAudioResourcesNames[2], mAudioResourcesNames[1])
            else                                          -> listOf("", mAudioResourcesNames[0], mAudioResourcesNames[1], mAudioResourcesNames[3], mAudioResourcesNames[0], "", mAudioResourcesNames[3], mAudioResourcesNames[1])
        }
        val trials:MutableList<TrialBasic> = mutableListOf()
        trials.add(TrialFGI(1,1, currImageName, audios[0]))
        trials.add(TrialFGI(2,3, currImageName, audios[1]))
        trials.add(TrialFGI(3,4, currImageName, audios[2]))
        trials.add(TrialFGI(4,2, currImageName, audios[3]))
        trials.add(TrialFGI(5,3, currImageName, audios[4]))
        trials.add(TrialFGI(6,1, currImageName, audios[5]))
        trials.add(TrialFGI(7,2, currImageName, audios[6]))
        trials.add(TrialFGI(8,4, currImageName, audios[7]))

        return trials
    }

    private fun createTrialsDebug():List<TrialBasic>{
        return createTrials()
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onNextTrial(){

        // if !last trial && !block end => doNextTrial
        when {
            currTrialID == (nTrials - 1) -> {
                saveText("", notifyDm = true)
                testEvent.accept(Triple(EVENT_TEST_END, null, listOf()))            // END !
            }
            mListBlocks.contains(currTrialID) -> {
                EVENT_BLOCK_END
            }
            else -> doNextTrial()
        }
    }

    override fun onStimuliEnd(){

        txt_left.visibility     = View.INVISIBLE
        txt_right.visibility    = View.INVISIBLE
        view_buttons.visibility = View.INVISIBLE

        stopPolling()
        currMP?.stop()
        currVisual?.stop()

        super.onStimuliEnd()
    }

    override fun initSummary(){}
    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        try {
            mStimuliHandler.postDelayed({ onStimuliEnd() }, currStimulusDuration)

            saveText(mTrial.Log(), notifyDm = false)

            val audio_res = (trial as TrialFGI).audio_name
            if(audio_res.isNotEmpty()) {
                currMP =(mStimuliManager.getValidAudioManager() as AudioManager).loadMPResource(audio_res, loop = true)
                mStimuliManager.deliverAlignedStimulus(STIM_AV)
            }
            else    mStimuliManager.deliverAlignedStimulus(STIM_V)

            txt_left.visibility     = View.VISIBLE
            txt_right.visibility    = View.VISIBLE
            view_buttons.visibility = View.VISIBLE

            startPolling(responseSamplingInterval)
            trialStartMs = uptimeMillis()
        }
        catch(e:Exception){
            e.printStackTrace()
        }
    }

    // =============================================================================================================================
    // ARRANGE UI
    // =============================================================================================================================
    private fun setUI(labels:Pair<String, String>){

        val mainlayout: ConstraintLayout = mainView!!.findViewById(R.id.fragment_test_layout)
        parent_layout_width     = mainlayout.width

        val params              = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        mImageView!!.layoutParams = params

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)

        view_buttons = View(ctx).apply {
            id = View.generateViewId()
            setOnTouchListener{ v, event ->
                val action = event.action
                when(action){
                    MotionEvent.ACTION_DOWN -> {
                        getPressedButton(event)
                        performClick()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        getPressedButton(event)
                    }
                    MotionEvent.ACTION_UP -> {
                        currPressedButton = "0"
                    }
                }
                true
            }
        }
        mainlayout.addView(view_buttons)
        constraintSet.connect(view_buttons.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.constrainHeight(view_buttons.id, 100.toDp(ctx))
        constraintSet.constrainWidth(view_buttons.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.applyTo(mainlayout)

        txt_left = TextView(ctx).apply {
            id = View.generateViewId()
            text = labels.first
            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            setLinkTextColor(ctx.resources.getColor(R.color.colorPrimary))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
        }
        mainlayout.addView(txt_left)
        constraintSet.connect(txt_left.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(txt_left.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0)
        constraintSet.constrainHeight(txt_left.id, 100.toDp(ctx))
        constraintSet.constrainWidth(txt_left.id, 180.toDp(ctx))
        constraintSet.applyTo(mainlayout)

        txt_right = TextView(ctx).apply {
            text = labels.second
            id = View.generateViewId()
            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            setLinkTextColor(ctx.resources.getColor(R.color.colorPrimary))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
        }
        mainlayout.addView(txt_right)
        constraintSet.connect(txt_right.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(txt_right.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
        constraintSet.constrainHeight(txt_right.id, 100.toDp(ctx))
        constraintSet.constrainWidth(txt_right.id, 180.toDp(ctx))
        constraintSet.applyTo(mainlayout)
    }

    //============================================================================================
    // MANAGE POLLING
    //============================================================================================

    private fun getPressedButton(me:MotionEvent):String{
        currPressedButton = if(me.rawX > parent_layout_width/2)     "2"
                            else                                    "1"

        return currPressedButton
    }


    private fun setPressedButton(){

//        testEvent.accept(Triple(EVENT_SHOW_DEBUGINFO, "pressed button: $currPressedButton"))
        val elapsed = uptimeMillis() - trialStartMs
        saveText("$elapsed\t$currPressedButton\n", notifyDm = false)
    }


    private fun startPolling(iti: Long){

        disposableTimer = Observable.interval(iti, iti, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( { aLong: Long           -> setPressedButton() },
                        { throwable: Throwable  -> {} })
    }

    private fun stopPolling() {
        disposableTimer?.dispose()
        disposableTimer = null
    }
    // =============================================================================================================================
}