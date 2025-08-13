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
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.tests.tfi.TestTFI
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.ui.fragments.TestFragment
import iit.uvip.psysuite.core.utility.ConditionData

import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.toDp
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import java.lang.Double.POSITIVE_INFINITY


// show -> onTrialEnd -> EVENT_GIVE_ANSWER

class TestBeads(ctx: Context,
                activity: Activity,
                hostfragment: Fragment,
                subject: SubjectBasicParcel,
                vibrator: VibrationManager?,
                mImageView: ImageView?,
                speechManager: SpeechManager?,
                private val mainView: View
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView) {

    override var LOG_TAG: String = TestBeads::class.java.simpleName

    private val binding: FragmentTestBinding =  (hostfragment as TestFragment).binding


    override var mDrawablesResource: MutableList<Int> = mutableListOf(
        R.drawable.beads_15_85,
        R.drawable.beads_40_60
    )

    private var beads_orders:List<List<Boolean>> = listOf(
        listOf(true, true, true, true,  false, true,  true,  true, true, true,  true,  true, true, true,  true, false, true, true,  false, true),
        listOf(true, true, true, false, true,  true,  true,  true, true, false, false, true, true, true,  true, true,  true, true,  true,  false),
        listOf(true, true, true, true,  true,  true,  false, true, true, false, true,  true, true, false, true, true,  true, false, true,  false),
        listOf(true, true, false, true, true,  false, true, true, true,  true,  true, true, true,  true,  true, true, true,  true,  true,  false)
    )

    private val nbeadsXtrial:Int = beads_orders[0].size
    private var currBead:Int = 0

    private var beads_images:MutableList<ImageView> = mutableListOf()

    private var buttonsLabels: List<String> = mutableListOf(
        ctx.resources.getString(R.string.label_jar1),
        ctx.resources.getString(R.string.label_jar2),
        ctx.resources.getString(R.string.label_new_bead),
        ctx.resources.getString(R.string.label_choose_jar),
    )

    // discrete response
    private lateinit var button_left:Button
    private lateinit var button_right: Button

    private var currImageRes:Int        = 0
    private var currCondLabel:String    = ""

    private var currVisual: VisualManager?  = null

    private var trialStartMs:Long               = 0L
    private var parent_layout_width:Int         = binding.root.width


    companion object {
        // Overrides
        @JvmStatic val TEST_BASIC_LABEL = "BEADS"

        // Test-specific stimulus types
        @JvmStatic val STIMULUS_TYPE_1_LOG  = "LU"
        @JvmStatic val STIMULUS_TYPE_2_LOG  = "MU"

        // Test-specific bead types
        @JvmStatic val BEAD_TYPE_TRUE       = "G"
        @JvmStatic val BEAD_TYPE_FALSE      = "Y"

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData(STIMULUS_TYPE_1_LOG, TEST_BEADS_LOWUNCERT, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_1_LOG}" , Populations.sighted_populations),
            ConditionData(STIMULUS_TYPE_2_LOG, TEST_BEADS_MIDUNCERT, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_2_LOG}" , Populations.sighted_populations)
        )

        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(
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

        if(subject.type == TEST_BEADS_LOWUNCERT){
            currImageRes    = mDrawablesResource[0]
            currCondLabel   = STIMULUS_TYPE_1_LOG
        }
        else{
            currImageRes    = mDrawablesResource[1]
            currCondLabel   = STIMULUS_TYPE_2_LOG
        }
        setUI()

        currStimulusDuration    = POSITIVE_INFINITY.toLong()

        val trials = if(!subject.isDebug)  createTrials()
                     else                  createTrialsDebug()

        mTrialsManager = FixedTrialsManager(trials as MutableList<TrialBasic>)

        mTestLabel              = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(TrialBeads.LOG_HEADER)
        currVisual      = VisualManager(STIM_V, mImageView!!, (mTrialsManager.mTrials[0] as TrialBeads).img_res, duration = POSITIVE_INFINITY.toLong(), handler = mStimuliHandler)
        mStimuliManager = StimuliManager(null, null, currVisual, delaysAligner, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrials():List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        trials.add(TrialBeads(1, subject.type, currCondLabel, currImageRes, beads_orders[0]))
        trials.add(TrialBeads(2, subject.type, currCondLabel, currImageRes, beads_orders[1]))
        trials.add(TrialBeads(3, subject.type, currCondLabel, currImageRes, beads_orders[2]))
        trials.add(TrialBeads(4, subject.type, currCondLabel, currImageRes, beads_orders[3]))

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
            currTrial == (nTrials - 1) -> {
                saveText("", notifyDm = true)
                testEvent.accept(Triple(EVENT_TEST_END, null, listOf()))           // END !
            }
            mListBlocks.contains(currTrial) -> {
                EVENT_BLOCK_END
            }
            else -> doNextTrial()
        }
    }

    override fun onTrialEnd(){

        button_left.visibility  = View.INVISIBLE
        button_right.visibility = View.INVISIBLE

        for(b in 0 until nbeadsXtrial)  beads_images[b].visibility = View.INVISIBLE
        currVisual?.stop()

        mStimuliHandler.postDelayed({ testEvent.accept(Triple(EVENT_SHOW_NEXT_BUTTON, null, listOf())) }, 2000L)
    }

    override fun initSummary(){}
    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        try {
            val res = saveText(mTrial.Log(), notifyDm = false)

            mStimuliManager.mVisualManager!!.load((trial as TrialBeads).img_res)
            mStimuliManager.deliverAlignedStimulus(STIM_V)

            for(b in 0 until nbeadsXtrial){
                val bead_res =  if((trial as TrialBeads).beads_types[b])    R.drawable.green_circle
                                else                                        R.drawable.yellow_circle
                beads_images[b].setImageResource(bead_res)
                beads_images[b].visibility = View.INVISIBLE
            }
            trialStartMs = uptimeMillis()

            currBead = -1
            mStimuliHandler.postDelayed({ showBead() }, 2000L)

        }
        catch(e:Exception){
            e.printStackTrace()
        }
    }

    private fun showBead(){

        currBead++

        // write log
        val bead_type   =   if((mTrial as TrialBeads).beads_types[currBead])    BEAD_TYPE_TRUE
                            else                                                BEAD_TYPE_FALSE
        val elapsed = uptimeMillis() - trialStartMs
        saveText("BEAD\t" + (currBead+1).toString() + "\t" + bead_type + "\t" +elapsed.toString()+"\n", notifyDm = false)

        // set UI
        button_left.text        = buttonsLabels[2]
        button_right.text       = buttonsLabels[3]

        button_left.visibility  = View.VISIBLE
        button_right.visibility = View.VISIBLE

        button_left.setOnClickListener{ showBead() }
        button_right.setOnClickListener{ chooseJar() }

        beads_images[currBead].visibility = View.VISIBLE

        if(currBead == nbeadsXtrial-1)
            button_left.visibility  = View.INVISIBLE

    }

    private fun chooseJar(){

        button_left.visibility  = View.INVISIBLE
        button_right.visibility = View.INVISIBLE

        button_left.text        = buttonsLabels[0]
        button_right.text       = buttonsLabels[1]

        button_left.setOnClickListener{  jarChosen("a") }
        button_right.setOnClickListener{ jarChosen("b") }


        mStimuliHandler.postDelayed({
            button_left.visibility  = View.VISIBLE
            button_right.visibility = View.VISIBLE
        }, 1000L)

    }

    private fun jarChosen(jar:String){
        val elapsed = uptimeMillis() - trialStartMs
        val res     = saveText("CHOOSE\t$jar\t$elapsed\n", notifyDm = false)

        onTrialEnd()
    }
    // =============================================================================================================================
    // ARRANGE UI
    // =============================================================================================================================
    private fun setUI() {

        val mainlayout = binding.root
//        binding             = FragmentTestBinding.inflate(activity.layoutInflater)
//        val mainlayout      = binding.root
//        parent_layout_width = mainlayout.width

        val constraintSet = ConstraintSet()
        constraintSet.clone(mainlayout)

        button_left = createBottomButton(buttonsLabels[2], mainlayout, constraintSet, ConstraintSet.LEFT, 250, 100)
        button_right = createBottomButton(buttonsLabels[3], mainlayout, constraintSet, ConstraintSet.RIGHT, 250, 100)

        createBeadsRow(binding.hlayout, nbeadsXtrial)
    }

    private fun createBottomButton(txt:String, parent_layout:ConstraintLayout, constr_set:ConstraintSet, hconstr:Int, hconstr_margin:Int, vconstr_margin:Int):Button{
        val bt = AppCompatButton(ctx).apply {
            id              = View.generateViewId()
            text            = txt
            textAlignment   = TextView.TEXT_ALIGNMENT_CENTER
            gravity         = Gravity.CENTER
            visibility      = View.INVISIBLE

            setBackgroundColor(ctx.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            setLinkTextColor(ctx.resources.getColor(R.color.colorPrimary))
        }
        parent_layout.addView(bt)
        constr_set.connect(bt.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, vconstr_margin)
        constr_set.connect(bt.id, hconstr, ConstraintSet.PARENT_ID, hconstr, hconstr_margin)
        constr_set.constrainHeight(bt.id, 100.toDp(ctx))
        constr_set.constrainWidth(bt.id, 400)
        constr_set.applyTo(parent_layout)

        return bt
    }

    private fun createBeadsRow(layout:LinearLayout, nbut:Int, res:Int=-1){

        for (a in 0 until nbut) {
            val iv = ImageView(ctx).apply {
                id          = View.generateViewId()
                visibility  = View.INVISIBLE
                layoutParams = LinearLayout.LayoutParams(50,50)
                (layoutParams as LinearLayout.LayoutParams).setMargins(5, 5, 5, 5)

                if(res>0){
                    setImageResource(res)
                }
            }
            layout.addView(iv)
            beads_images.add(iv)
        }
    }
    // =============================================================================================================================
}