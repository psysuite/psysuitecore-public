package iit.uvip.psysuite.core.tests.ttc

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.SystemClock.uptimeMillis
import android.view.Surface
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.adaptive.AdaptiveWrapper
import iit.uvip.psysuite.adaptive.TaskADAParams
import iit.uvip.psysuite.adaptive.ado.ADOParams
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentTestBinding
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.ui.fragments.TestFragment
import iit.uvip.psysuite.core.utility.ConditionData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import java.lang.Double.POSITIVE_INFINITY
import java.util.concurrent.TimeUnit
import kotlin.math.round


// show -> onTrialEnd -> EVENT_GIVE_ANSWER

class TestTTC(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?,
              private val mainView: View
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView) {

    override var LOG_TAG: String = TestTTC::class.java.simpleName

    private val isLandscape:Boolean get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (ctx.display?.rotation ?: 0) {
                Surface.ROTATION_0,
                Surface.ROTATION_180    -> false
                else                    -> true
            }
        } else {
            TODO("VERSION.SDK_INT < R")
        }
    }

    private val TRIALS_BLOCK_NREP = 8

    companion object {
        @JvmStatic val TEST_BASIC_LABEL     = "TTC"

        @JvmStatic val STIMULUS_TYPE_V_LOG  = "VI"

        @JvmStatic val MOTION_TYPE_H_LOG    = "HO"
        @JvmStatic val MOTION_TYPE_V_LOG    = "VE"

        @JvmStatic val FACTOR_TYPE_SPACE    = "SPA"
        @JvmStatic val FACTOR_TYPE_SPEED    = "VEL"

        @JvmStatic val CUE_TYPE_DIR_LOG     = "DIR"
        @JvmStatic val CUE_TYPE_WEIGHT_LOG  = "WGTH"

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
//            ConditionData("${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_V_LOG}",                        TEST_MOTPRE_VV,                "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_V_LOG}" , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_SPACE}",    TEST_MOTPRE_VH_FACTOR_SPACE,      "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_SPACE}" , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_SPEED}",    TEST_MOTPRE_VH_FACTOR_SPEED,      "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_SPEED}" , Populations.sighted_populations),
//            ConditionData("${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}_${CUE_TYPE_DIR_LOG}",    TEST_MOTPRE_VH_CUE_ARROW,      "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}_${CUE_TYPE_DIR_LOG}" , Populations.sighted_populations),
//            ConditionData("${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}", TEST_MOTPRE_VV_CUE_ARROW,      "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_V_LOG}_${CUE_TYPE_DIR_LOG}" , Populations.sighted_populations),
//            ConditionData("${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_V_LOG}", TEST_MOTPRE_VV_CUE_WEIGHT,     "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_V_LOG}_${CUE_TYPE_WEIGHT_LOG}" , Populations.sighted_populations),
//            ConditionData("${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}", TEST_MOTPRE_VH,                "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}" , Populations.sighted_populations),
//            ConditionData("${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}_${MOTION_TYPE_V_LOG}", TEST_MOTPRE_VHV,"${TEST_BASIC_LABEL}_${STIMULUS_TYPE_V_LOG}_${MOTION_TYPE_H_LOG}" , Populations.sighted_populations)
        )

        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(
            listOf(TEST_NEXTTRIAL_NOCHOOSE),
            listOf(TEST_NEXTTRIAL_NOCHOOSE),
//            listOf(TEST_NEXTTRIAL_NOCHOOSE),
//            listOf(TEST_NEXTTRIAL_NOCHOOSE),
//            listOf(TEST_NEXTTRIAL_NOCHOOSE),
//            listOf(TEST_NEXTTRIAL_NOCHOOSE)
        )
    }

    private var STIM_V  = StimuliManager.STIM_TYPE_V1
    private val binding: FragmentTestBinding =  (hostfragment as TestFragment).binding

    override var mDrawablesResource: MutableList<Int> = mutableListOf(
        R.drawable.black_circle,
        R.drawable.palloncino,
        R.drawable.incudine,
        R.drawable.arrow_right,
        R.drawable.arrow_down,
        R.drawable.arrow_left,
        R.drawable.arrow_top,
        R.drawable.arrow_4dir
    )

    private var currImageRes:Int        = 0

    private lateinit var mScenario:ScenarioView

    private val parent_layout_width:Int         = binding.root.width
    private val parent_layout_height:Int        = binding.root.height

    // in portrait:  only vertical is allowed
    // in landscape: both vertical and horizontal are allowed....but path must be the same => calculate it based on lower dimension (= screen height)

    // TASK CONSTANT SPEED
    private var fixedDistance:Int               = round(parent_layout_height*0.75).toInt()
    private var movementDuration:Long           = 2250L     // default motion time
    private val mPercInvisibility:List<Float>   = listOf(0.6F, 0.4F, 0.25F, 0F)     // percentage of invisible trajectory (at costant speed, we have constant distance)

    // TASK VARYING SPEED
    // reference movement : 900 px in 2250 ms (0.4 px/ms)
    // first 550 px in 1375 ms, last 350 px in 875 ms
    // I want to vary the speed, preserving:
    //          - the 1375 ms of trajectory observation time in all trials.
    //          - the distance of the invisible trajectory

    // increasing the speed (e.g. 0.45), the distance of the first part increase to 618.75 px
    //    speed	    0.36	0.4	    0.45	0.51			vis time = 1375, invis distance = 350
    //    distance	845	    900	    968.75	1051.25
    //    duration	2347	2250	2152	2061

    private val fixedVisibleTime:Long           = 1375L    // default visible (then it disappears) time in varying speed task
    private val fixedInvisibleDistance:Int      = 350
    private val mSpeeds:List<Float>             = listOf(0.36F, 0.4F, 0.45F, 0.51F)
    private val mDistances:List<Int>            = mSpeeds.map {
        (it*fixedVisibleTime + fixedInvisibleDistance).toInt()
    }
    private val mDurations:List<Long>            = mSpeeds.mapIndexed{idx, speed ->
        (mDistances[idx]/speed).toLong()
    }

    private val waitInterval:Long get()         = 1000L    // interval between scenario creation and target movement start

    private val trialAbortTime:Long get()       = 2*movementDuration    // allowed number of ms to wait for user response. after this interval the trial ends
    private val movementSamplingInterval:Long   = 10L                   // position update pace

    private var trialStartMs:Long               = 0L                    // trial onset
    private var trialEndMs:Long                 = trialAbortTime        // user press latency
    private var disposableTimer: Disposable?    = null

    private val nQuestTrials                = 30
    private val adoParams                   = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)
    private val taskADAParams               = TaskADAParams(400.0F, nQuestTrials+10)
    private val adoWrapper: AdaptiveWrapper = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){

        // set question & create mTrials list
        validAnswers    = mutableListOf()
        mQuestion       = ""
        abortMode       = TEST_ABORT_TRIALEND   // show abort button after each trial

        currStimulusDuration    = POSITIVE_INFINITY.toLong()
        currImageRes            = mDrawablesResource[0]

        mTestLabel              = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        mTrialsManager =
            when (subject.isDebug) {
                true -> {
                    val trials = createTrialsDebug()
                    FixedTrialsManager(trials as MutableList<TrialBasic>)
                }
                else -> {
                    when (subject.trman_type) {
                        TEST_TRMAN_FIXED -> FixedTrialsManager(createFixedTrials() as MutableList<TrialBasic>)
                        else -> {
                            val trman = AdaptiveTrialsManager(createAdaptiveTrials() as MutableList<TrialBasic>, adoWrapper)
                            trman.getStimulus()
                            trman
                        }
                    }
                }
            }

        createResultFile(TrialTTC.LOG_HEADER)

        mStimuliManager = StimuliManager(null, null,
                                        VisualManager(STIM_V, mImageView!!, currImageRes, duration = currStimulusDuration, handler = mStimuliHandler),
                                        delaysAligner, ctx, mStimuliHandler)

        mScenario = ScenarioView(ctx, null).apply {
            binding.root.addView(this)
        }

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))

    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createFixedTrials():List<TrialBasic> {
        return when (subject.type) {
            TEST_MOTPRE_VH              -> createFT_VH()
            TEST_MOTPRE_VV              -> createFT_VV()
            TEST_MOTPRE_VV_CUE_ARROW    -> createFT_VV_DIR()
            TEST_MOTPRE_VH_CUE_ARROW    -> createFT_VH_DIR()
            TEST_MOTPRE_VH_FACTOR_SPACE -> createFT_VH_SPACE()
            TEST_MOTPRE_VH_FACTOR_SPEED -> createFT_VH_SPEED()
            TEST_MOTPRE_VV_CUE_WEIGHT   -> createFT_VV_WEIGHT()
            else                        -> createFT_VHV()
        }
    }

    // VISUAL - HORIZONTAL -> RIGHT - CHANGE INVISIBILITY ONSET
    // 2 x 1direction x 4lat x 8rep + 4 test@zero + 3 test = 71  -> 17 trials for one direction, for each (4) latencies + 3 at 0-latency
    private fun createFT_VH_SPACE():List<TrialBasic> {

        val alltrials:MutableList<TrialBasic> = mutableListOf()

        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[0], true, true))

        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercInvisibility[2], movementDuration, fixedDistance, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercInvisibility[1], movementDuration, fixedDistance, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercInvisibility[0], movementDuration, fixedDistance, mDrawablesResource[0], true, true))

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for(l in mPercInvisibility){
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], true, true))
            }

            trials.shuffle()
            alltrials.addAll(trials)
        }
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[0], true, true))

        return alltrials
    }

    // VISUAL - HORIZONTAL -> RIGHT - CHANGE TARGET SPEED
    // 2 x 1direction x 4lat x 8rep + 8 test@zero with different speed = 72  -> 16 trials for one direction, for each (4) speed + 8 at 0-latency
    private fun createFT_VH_SPEED():List<TrialBasic> {
        val alltrials:MutableList<TrialBasic> = mutableListOf()

        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, mDurations[0], fixedDistance, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, mDurations[2], fixedDistance, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, mDurations[1], fixedDistance, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, mDurations[3], fixedDistance, mDrawablesResource[0], true, true))
        alltrials.shuffle()

        val trials: MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            trials.clear()

            mDurations.mapIndexed{id, duration ->
                trials.add(TrialTTC(-1, subject.type, mTestLabel, fixedVisibleTime.toFloat(), duration, mDistances[id], mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, fixedVisibleTime.toFloat(), duration, mDistances[id], mDrawablesResource[0], true, true))
            }

            trials.shuffle()
            alltrials.addAll(trials)
        }

        trials.clear()
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, mDurations[0], fixedDistance, mDrawablesResource[0], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, mDurations[2], fixedDistance, mDrawablesResource[0], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, mDurations[1], fixedDistance, mDrawablesResource[0], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, mDurations[3], fixedDistance, mDrawablesResource[0], true, true))
        alltrials.shuffle()

        alltrials.addAll(trials)
        return alltrials
    }

    // 2 x 2direction x 3lat x 4rep= 48  -> 8 trials for each direction(2), for each latency(3)
    private fun createFT_VV():List<TrialBasic> {
        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for(l in mPercInvisibility){
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, false))
            }
            trials.shuffle()
            alltrials.addAll(trials)
        }
        return alltrials
    }

    // 2 x 2direction x 3lat x 4rep= 48  -> 8 trials for each direction(2), for each latency(3)
    private fun createFT_VH():List<TrialBasic> {
        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for(l in mPercInvisibility){
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], true, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], true, false))
            }

            trials.shuffle()
            alltrials.addAll(trials)
        }
        return alltrials
    }

    // 2 x 2orientation x 2direction x 3lat x 4rep= 96  -> 8 trials for each orientation(2) , for each direction(2), for each latency(3)
    private fun createFT_VHV():List<TrialBasic> {

        val catch_trials = mutableListOf<TrialBasic>()
        for (i in 0 until 2) {
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], false, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], false, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], false, false))
        }
        catch_trials.shuffle()

        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for(l in mPercInvisibility){
                trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], true, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], true, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, false))

            }

            trials.shuffle()
            alltrials.addAll(trials)
        }
        return alltrials
    }

    // 2 x 3interference(congr/incongr/neutr) x 3lat x 4rep +
    // 2 x 3interference(congr/incongr/neutr) x 0lat x 2rep = 72 + 12 -> 8 trial for each interferences level(3), for each latency(3) + 4 catch trials for each interferences level(3)
    private fun createFT_VH_DIR():List<TrialBasic> {

        val catch_trials = mutableListOf<TrialBasic>()
        for (i in 0 until 2) {
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], true, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], true, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], true, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], true, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[7], true, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[7], true, false))
        }
        catch_trials.shuffle()

        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for (j in 0 until 3)
                trials.add(catch_trials[i*3 + j])

            for(l in mPercInvisibility){

                // congruent
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[3], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[5], true, false))

                // incongruent
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[5], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[3], true, false))

                // neutral
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[7], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[7], true, false))
            }


            trials.shuffle()
            alltrials.addAll(trials)
        }
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], true, true))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], true, false))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], true, false))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], true, true))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[7], true, false))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[7], true, true))

        return alltrials
    }

    // 2 x 3interference(congr/incongr/neutr) x 3lat x 4rep +
    // 2 x 3interference(congr/incongr/neutr) x 0lat x 2rep = 72 + 12 -> 8 trial for each interferences level(3), for each latency(3) + 4 catch trials for each interferences level(3)
    private fun createFT_VV_DIR():List<TrialBasic> {

        val catch_trials = mutableListOf<TrialBasic>()
        for (i in 0 until 2) {
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], false, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], false, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], false, false))
        }
        catch_trials.shuffle()
        
        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for (j in 0 until 3)
                trials.add(catch_trials[i*TRIALS_BLOCK_NREP + j])

            for(l in mPercInvisibility){

                // congruent
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[4], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[6], false, false))

                // incongruent
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[6], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[4], false, false))

                // neutral
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[7], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[7], false, false))
            }
            trials.shuffle()
            alltrials.addAll(trials)
        }
        return alltrials
    }

    // 2 x 3cue(heavy/light/neutr) x 3lat x 4rep = 72 -> 8 trial for each cue level(3), for each latency(3)
    private fun createFT_VV_WEIGHT():List<TrialBasic> {
        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()
            for(l in mPercInvisibility){
                // light
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[1], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[1], false, false))

                // heavy
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[2], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[2], false, false))

                // neutral
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[0], false, false))
            }
            trials.shuffle()
            alltrials.addAll(trials)
        }
        return alltrials
    }

    private fun createAdaptiveTrials():List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        return trials
    }

    private fun createTrialsDebug():List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()

        // no cue
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[1], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[1], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[1], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[1], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[2], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[2], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[2], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[2], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[7], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[7], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[7], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[7], false, false))





        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[0], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[1], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[1], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[1], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[1], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[2], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[2], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[2], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[2], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[4], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[5], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[6], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0.toFloat(), movementDuration, fixedDistance, mDrawablesResource[3], false, false))

        return trials
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onNextTrial(){
        testEvent.accept(Triple(EVENT_UPDATE_TRIAL_ID, 0L, listOf()))
        return super.onNextTrial()
    }

    override fun onTrialEnd(){

        stopPolling()
        onAnswerGiven(trialEndMs.toInt(), trialEndMs.toInt())

        mStimuliHandler.removeCallbacksAndMessages(null)
        mScenario.clearScenario()

        mStimuliHandler.postDelayed({ testEvent.accept(Triple(EVENT_SHOW_ABORT, null, listOf())) }, 500L)
    }

    override fun initSummary(){}

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        try {
            movementDuration = (trial as TrialTTC).time

            mScenario.createScenario(binding.root, mTrial as TrialTTC, isLandscape, movementSamplingInterval, ::onPress)

            mStimuliHandler.postDelayed({
                trialEndMs   = trialAbortTime
                trialStartMs = uptimeMillis()
                startPolling(movementSamplingInterval)
            }, waitInterval)   // after waitInterval, start moving

            mStimuliHandler.postDelayed({
                if(trial.magnitude == 0F)   {
                        mScenario.setToEnd()
                        stopPolling()
                        mStimuliHandler.postDelayed({ mScenario.hidePoint() },200L)
                }else   mScenario.hidePoint()
            },  trial.stim_value + waitInterval)   // hide target

            mStimuliHandler.postDelayed({ stopPolling() },          movementDuration + waitInterval)   // when movement duration is reached => stop moving
            mStimuliHandler.postDelayed({ onTrialEnd() },           trialAbortTime + waitInterval)     // when max time is reached => force trial end
        }
        catch(e:Exception){
            e.printStackTrace()
        }
    }

    private fun onPress(){
        trialEndMs = uptimeMillis() - trialStartMs      // behavioral result
        onTrialEnd()
    }

    private fun startPolling(iti: Long){

        disposableTimer = Observable.interval(iti, iti, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    run {
                        val elapsed = uptimeMillis() - trialStartMs
                        mScenario.movePoint(elapsed)
                    }
                }
//                , { _: Throwable  -> {} }
            )
    }

    private fun stopPolling() {
        disposableTimer?.dispose()
        disposableTimer = null
    }
    // =============================================================================================================================
}