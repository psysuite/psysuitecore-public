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
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
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


// show -> onTrialEnd -> EVENT_GIVE_ANSWER

class TestRIVGRP(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?,
              private val mainView: View
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView) {

    override var LOG_TAG: String = TestRIVGRP::class.java.simpleName

    private var STIM_V  = StimuliManager.STIM_TYPE_V1
//    private var STIM_A  = StimuliManager.STIM_TYPE_A2
//    private var STIM_AV = STIM_A or STIM_V

    private var isRivalryFirst: Boolean = true

    override var mDrawablesResource: MutableList<Int> = mutableListOf(
        R.drawable.rivalry_chouse_rface,
        R.drawable.rivalry_chouse_rface,
        R.drawable.grouping_house_face_1,
        R.drawable.grouping_house_face_2,
        R.drawable.rivalry_chouse_rcar,
        R.drawable.rivalry_chouse_rcar,
        R.drawable.grouping_house_car_1,
        R.drawable.grouping_house_car_2
    )

    private var imagesNames: MutableList<String> = mutableListOf(
        "rivalry_chouse_rface",
        "rivalry_chouse_rface",
        "grouping_house_face_1",
        "grouping_house_face_2",
        "rivalry_chouse_rcar",
        "rivalry_chouse_rcar",
        "grouping_house_car_1",
        "grouping_house_car_2"
    )

    private var buttonsLabels: MutableList<Pair<String, String>> = mutableListOf(
        Pair(ctx.resources.getString(R.string.label_house), ctx.resources.getString(R.string.label_face)),
        Pair(ctx.resources.getString(R.string.label_house), ctx.resources.getString(R.string.label_car))
    )

    private val buttonOffSet:Int = 10   // offset of response buttons from midline

    // continuous response
    private var txt_left:TextView? = null
    private var txt_right:TextView? = null
    private var view_buttons:View? = null

    // discrete response
    private var right_button:Button? = null
    private var left_button: Button? = null
    private var isLeftPressed:Boolean = false
    private var isRightPressed:Boolean = false

    private var currImageName:String        = ""
    private var currMP: MediaPlayer?        = null
    private var currVisual: VisualManager?  = null

    private var isContinuosResponse:Boolean     = false   // false: two buttons, true: finger sliding
    private var pressPosition:Int               = 0       // 0: screen center, -100 left border, 100 right border
    private var trialStartMs:Long               = 0L
    private var disposableTimer: Disposable?    = null
    private var parent_layout_width:Int         = 0
    private var parent_layout_height:Int        = 0

    private val responseSamplingInterval:Long = 100L

    companion object {
        @JvmStatic val TEST_BASIC_LABEL = "RIVGRP"


        @JvmStatic val STIMULUS_TYPE_1_LOG  = "HF"
        @JvmStatic val STIMULUS_TYPE_2_LOG  = "HC"

        @JvmStatic val EFFECT_TYPE_1_LOG  = "RIV"
        @JvmStatic val EFFECT_TYPE_2_LOG  = "GRP"

        @JvmStatic val STIMULUS_TYPE_1  = "HOUSE_FACE"
        @JvmStatic val STIMULUS_TYPE_2  = "HOUSE_CAR"

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_1_LOG}"                      , TEST_RIVGRP_RIV_HF, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_1_LOG}"                         , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}"                      , TEST_RIVGRP_GRP_HF,   "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}"                       , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" , TEST_RIVGRP_RIVGRP_HF, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}_${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_1_LOG}"                      , TEST_RIVGRP_RIV_HC, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_1_LOG}"                         , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_2_LOG}"                      , TEST_RIVGRP_GRP_HC,   "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_2_LOG}"                       , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" , TEST_RIVGRP_RIVGRP_HC, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}_${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}" , Populations.sighted_populations),
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

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){
        // set question & create mTrials list
        validAnswers    = mutableListOf()
        mQuestion       = ""
        abortMode       = TEST_ABORT_TRIALEND   // show abort button after each 8 trial

        when(subject.type){
            TEST_RIVGRP_RIV_HF,TEST_RIVGRP_GRP_HF,TEST_RIVGRP_RIVGRP_HF -> setUI(buttonsLabels[0])
            else                                                        -> setUI(buttonsLabels[1])
        }
        val subj = (subject as SubjectRIVGRPParcel)

        // subj dialog init
        isRivalryFirst          = subj.rivFirst
        currStimulusDuration    = subj.blockDuration

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

        createResultFile(subject, TrialRIVGRP.LOG_HEADER)
        currVisual = VisualManager(STIM_V, mImageView!!, (mTrialsManager.mTrials[0] as TrialRIVGRP).img_res, duration = currStimulusDuration, handler = mStimuliHandler)
        mStimuliManager = StimuliManager(
            null, //AudioManager(StimuliManager.STIM_TYPE_A2, "",  duration = currStimulusDuration, ctx = ctx, handler = mStimuliHandler),
            null,
            currVisual,
            delaysAligner, ctx, mStimuliHandler)

        testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))
    }

    override fun initSummary() {
        mSummary = when(subject.type)
        {
            TEST_TFI, TEST_TFI_TODDLERS ->  TFISummary(ctx)
            else                        ->  TFIBISummary(ctx)
        }
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrials(resp_type:String):List<TrialBasic>{

        val trials:MutableList<TrialBasic> = mutableListOf()
        when(subject.type){
            TEST_RIVGRP_RIV_HF  -> {
                currImageName = "${STIMULUS_TYPE_1_LOG}${EFFECT_TYPE_1_LOG}"
                trials.add(TrialRIVGRP(1,1, currImageName, mDrawablesResource[0], imagesNames[0], resp_type))
                trials.add(TrialRIVGRP(2,1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                trials.add(TrialRIVGRP(3,1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                trials.add(TrialRIVGRP(4,1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
            }
            TEST_RIVGRP_GRP_HF     -> {
                currImageName = "${STIMULUS_TYPE_1_LOG}${EFFECT_TYPE_2_LOG}"
                trials.add(TrialRIVGRP(1,2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                trials.add(TrialRIVGRP(2,2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                trials.add(TrialRIVGRP(3,2, currImageName, mDrawablesResource[2],imagesNames[1], resp_type))
                trials.add(TrialRIVGRP(4,2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
            }
            TEST_RIVGRP_RIVGRP_HF  -> {
                currImageName = "${STIMULUS_TYPE_1_LOG}${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}"
                if(isRivalryFirst) {
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                }else{
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                    trials.add(TrialRIVGRP(2, 2, currImageName, mDrawablesResource[3],imagesNames[3], resp_type))
                    trials.add(TrialRIVGRP(1, 1, currImageName, mDrawablesResource[0],imagesNames[0], resp_type))
                    trials.add(TrialRIVGRP(4, 2, currImageName, mDrawablesResource[2],imagesNames[2], resp_type))
                    trials.add(TrialRIVGRP(3, 1, currImageName, mDrawablesResource[1],imagesNames[1], resp_type))
                }
            }
            TEST_RIVGRP_RIV_HC     -> {
                currImageName = "${STIMULUS_TYPE_2_LOG}${EFFECT_TYPE_1_LOG}"
                trials.add(TrialRIVGRP(1,1, currImageName, mDrawablesResource[4],imagesNames[4], resp_type))
                trials.add(TrialRIVGRP(2,1, currImageName, mDrawablesResource[5],imagesNames[5], resp_type))
                trials.add(TrialRIVGRP(3,1, currImageName, mDrawablesResource[4],imagesNames[4], resp_type))
                trials.add(TrialRIVGRP(4,1, currImageName, mDrawablesResource[5],imagesNames[5], resp_type))
            }
            TEST_RIVGRP_GRP_HC  -> {
                currImageName = "${STIMULUS_TYPE_2_LOG}${EFFECT_TYPE_2_LOG}"
                trials.add(TrialRIVGRP(1,2, currImageName, mDrawablesResource[6],imagesNames[6], resp_type))
                trials.add(TrialRIVGRP(2,2, currImageName, mDrawablesResource[7],imagesNames[7], resp_type))
                trials.add(TrialRIVGRP(3,2, currImageName, mDrawablesResource[6],imagesNames[6], resp_type))
                trials.add(TrialRIVGRP(4,2, currImageName, mDrawablesResource[7],imagesNames[7], resp_type))
            }
            else                -> {
                currImageName = "${STIMULUS_TYPE_2_LOG}${EFFECT_TYPE_1_LOG}_${EFFECT_TYPE_2_LOG}"
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

    private fun createTrialsDebug(resp_type:String):List<TrialBasic>{
        return createTrials(resp_type)
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onEndTrial(prev_result: Int, elapsed: Int, extra_text:String): Int {

        // if !last trial && !block end => doNextTrial
        return when {
            currTrial == (nTrials - 1) -> {
                saveText("", notifyDm = true)
                EVENT_TEST_END            // END !
            }
            mListBlocks.contains(currTrial) -> {
                EVENT_BLOCK_END
            }
            else -> doNextTrial()
        }
    }

    override fun onTrialEnd(){

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
        currMP?.stop()
        currVisual?.stop()

        mStimuliHandler.postDelayed({ testEvent.accept(Pair(EVENT_SHOW_NEXT_BUTTON, null)) }, 2000L)
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        try {
            val res = saveText(mTrial.Log(), notifyDm = false)

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
            mStimuliHandler.postDelayed({ onTrialEnd() }, currStimulusDuration)
            mStimuliManager.deliverAlignedStimulus(STIM_V)

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
    private fun setUI(labels:Pair<String, String>) {

        val mainlayout = mainView.findViewById(R.id.fragment_test_layout) as ConstraintLayout
        parent_layout_width     = mainlayout.width
        parent_layout_height    = mainlayout.height

        val img_scale           = 0.75F
        val border_scale:Float  = (1 - img_scale)/2

        mImageView!!.layoutParams = ConstraintLayout.LayoutParams((parent_layout_width*img_scale).toInt(), (parent_layout_height*img_scale).toInt())
        mImageView.id           = View.generateViewId()
//        ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT)

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)
        constraintSet.connect(mImageView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, (border_scale*parent_layout_width).toInt())
        constraintSet.connect(mImageView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (border_scale*parent_layout_height).toInt())
        constraintSet.applyTo(mainlayout)

        if(isContinuosResponse) createContinuousResponse(labels, mainlayout)
        else                    createDiscreteResponse(labels, mainlayout)

    }

    private fun createDiscreteResponse(labels:Pair<String, String>, mainlayout:ConstraintLayout){

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)

        // I create one single button (parentwidth, 100) and I draw two texts on it
        left_button = Button(ctx).apply {
            id              = View.generateViewId()
            text            = labels.first
            textAlignment   = TextView.TEXT_ALIGNMENT_CENTER
            gravity         = Gravity.CENTER

            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            setLinkTextColor(ctx.resources.getColor(R.color.colorPrimary))
            setOnTouchListener{ _, event ->
                val action = event.action

                if (action == MotionEvent.ACTION_DOWN) {
                    isLeftPressed = true
                    performClick()
                }
                else if (action == MotionEvent.ACTION_UP)
                    isLeftPressed = false

                true
            }
        }
        mainlayout.addView(left_button)
        constraintSet.connect(left_button!!.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(left_button!!.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0)
        constraintSet.constrainHeight(left_button!!.id, 100.toDp(ctx))
        constraintSet.constrainWidth(left_button!!.id, (parent_layout_width/2) - 4*buttonOffSet)
        constraintSet.applyTo(mainlayout)

        // RIGHT BUTTON
        right_button = Button(ctx).apply {
            id              = View.generateViewId()
            text            = labels.second
            textAlignment   = TextView.TEXT_ALIGNMENT_CENTER
            gravity         = Gravity.CENTER

            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            setLinkTextColor(ctx.resources.getColor(R.color.colorPrimary))
            setOnTouchListener{ _, event ->
                val action = event.action

                if (action == MotionEvent.ACTION_DOWN) {
                    isRightPressed = true
                    performClick()
                }
                else if (action == MotionEvent.ACTION_UP)
                    isRightPressed = false

                true
            }
        }
        mainlayout.addView(right_button)
        constraintSet.connect(right_button!!.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(right_button!!.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
        constraintSet.constrainHeight(right_button!!.id, 100.toDp(ctx))
        constraintSet.constrainWidth(right_button!!.id, (parent_layout_width/2) - 4*buttonOffSet)
        constraintSet.applyTo(mainlayout)

    }

    private fun createContinuousResponse(labels:Pair<String, String>, mainlayout:ConstraintLayout){

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)

        // I create one single button (parentwidth, 100) and I draw two texts on it
        view_buttons = View(ctx).apply {
            id = View.generateViewId()
            setOnTouchListener{ v, event ->
                val action = event.action
                pressPosition = when(action){
                    MotionEvent.ACTION_DOWN -> {
                        performClick()
                        getPressedButton(event)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        getPressedButton(event)
                    }
                    MotionEvent.ACTION_UP -> {
                        -1000
                    }
                    else -> pressPosition
                }
                true
            }
        }
        mainlayout.addView(view_buttons)
        constraintSet.connect(view_buttons!!.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.constrainHeight(view_buttons!!.id, 100.toDp(ctx))
        constraintSet.constrainWidth(view_buttons!!.id, ConstraintSet.MATCH_CONSTRAINT)
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
        constraintSet.connect(txt_left!!.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(txt_left!!.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0)
        constraintSet.constrainHeight(txt_left!!.id, 100.toDp(ctx))
        constraintSet.constrainWidth(txt_left!!.id, (parent_layout_width/2) - buttonOffSet)
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
        constraintSet.connect(txt_right!!.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(txt_right!!.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
        constraintSet.constrainHeight(txt_right!!.id, 100.toDp(ctx))
        constraintSet.constrainWidth(txt_right!!.id, (parent_layout_width/2) - buttonOffSet)
        constraintSet.applyTo(mainlayout)
    }

    //============================================================================================
    // MANAGE POLLING
    //============================================================================================

    // output values [-100....0....100]
    private fun getPressedButton(me:MotionEvent):Int{
        return (((me.rawX-parent_layout_width/2)/(parent_layout_width/2))*100).toInt()
    }

    private fun setPressedButton(){

        val elapsed = uptimeMillis() - trialStartMs

        val response:Int =   if(isContinuosResponse)   pressPosition
                            else{
                                if (isLeftPressed && isRightPressed)        3
                                else if(isLeftPressed && !isRightPressed)   1
                                else if(!isLeftPressed && isRightPressed)   2
                                else                                        0
                            }
//        testEvent.accept(Pair(EVENT_SHOW_DEBUGINFO, "pressed button: $response"))

        saveText("$elapsed\t$response\n", notifyDm = false)
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