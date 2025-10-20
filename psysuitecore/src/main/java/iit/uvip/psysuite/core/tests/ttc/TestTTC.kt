package iit.uvip.psysuite.core.tests.ttc

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.SystemClock.uptimeMillis
import android.view.Surface
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.albaspazio.psysuite.adaptive.AdaptiveWrapper
import org.albaspazio.psysuite.adaptive.TaskADAParams
import org.albaspazio.psysuite.adaptive.ado.ADOParams
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentTestBinding
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.ImageViewDefinedException
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.trials.TrialsManager
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


// show -> onStimuliEnd -> EVENT_GIVE_ANSWER

/**
 * Manages the Time-To-Contact (TTC) test.
 * This test typically assesses an observer's ability to judge when a moving object,
 * which may become occluded or change speed, would have collided with a target or boundary.
 * It involves presenting visual stimuli (e.g., a moving shape) and requiring the user
 * to respond at the perceived moment of collision.
 *
 * This implementation supports various motion types (horizontal, vertical), factor types
 * (fixed speed, variable speed with fixed visible time, variable speed with fixed visible path length),
 * and cue types (direction, weight). It can run in fixed or adaptive trial modes.
 *
 * @param ctx The application context.
 * @param activity The hosting activity.
 * @param hostfragment The hosting fragment, expected to be a [TestFragment] to access its binding and scenario view.
 * @param subject The subject details parcel.
 * @param vibrator An optional [VibrationManager] (not directly used in this specific test logic but available from [TestBasic]).
 * @param mImageView An optional [ImageView] for displaying visual stimuli (used for the moving target).
 * @param speechManager An optional [SpeechManager] (not directly used in this specific test logic but available from [TestBasic]).
 * @param mainView The main view of the test, used for UI manipulation and potentially drawing the scenario.
 */
class TestTTC(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?,
              mainView: View?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager, mainView) {

    override var LOG_TAG: String = TestTTC::class.java.simpleName

    private val isLandscape:Boolean get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (ctx.display?.rotation ?: 0) {
                Surface.ROTATION_0,
                Surface.ROTATION_180    -> false
                else                    -> true
            }
        } else {
//            TODO("VERSION.SDK_INT < R")
            true
        }
    }

    private val TRIALS_BLOCK_NREP = 8

    companion object {
        // Overrides
        @JvmStatic val TEST_BASIC_LABEL = "TTC"

        // Test-specific motion types
        @JvmStatic val MOTION_TYPE_H_LOG    = "HO"
        @JvmStatic val MOTION_TYPE_V_LOG    = "VE"

        // Test-specific factor types
        @JvmStatic val FACTOR_TYPE_FSP      = "FSPD"
        @JvmStatic val FACTOR_TYPE_VSP_VT   = "VSP_VT"
        @JvmStatic val FACTOR_TYPE_VSP_VPL  = "VSP_VPL"

        // Test-specific cue types
        @JvmStatic val CUE_TYPE_DIR_LOG     = "DIR"
        @JvmStatic val CUE_TYPE_WEIGHT_LOG  = "WGTH"

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(
            ConditionData("${STIMULUS_TYPE_VISUAL_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_FSP}",     TEST_MOTPRE_VH_FIXSPEED,        "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_FSP}" , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_VISUAL_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_VSP_VT}",  TEST_MOTPRE_VH_VARSPEED_FIXVT,  "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_VSP_VT}" , Populations.sighted_populations),
            ConditionData("${STIMULUS_TYPE_VISUAL_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_VSP_VPL}", TEST_MOTPRE_VH_VARSPEED_FIXVPL, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL_LOG}_${MOTION_TYPE_H_LOG}_${FACTOR_TYPE_VSP_VPL}" , Populations.sighted_populations)
        )

        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
            listOf(TEST_NEXTTRIAL_AUTO),
        )
    }

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
    private var fixedDuration:Long              = 2250L     // default motion time
    private val mPercVisibility:List<Float>     = listOf(0.0F, 0.2F, 0.35F, 0.6F)     // percentage of visible trajectory (at costant speed, we have constant distance)
    private val mMinVisibility:Float            = 0.4F
    private val mRangeVisibilityFSP:Float       = 0.6F


    // TASK VARYING SPEED
    private val mSpeeds:List<Float>             = listOf(0F, 0.04F, 0.09F, 0.15F)
    private val mMinSpeed:Float                 = 0.36F
    private val mRangeSpeed:Float               = 0.15F

    // ========> fixed VT & IPL
    // reference movement : 900 px in 2250 ms (0.4 px/ms)
    // first 550 px in 1375 ms, last 350 px in 625 ms
    // I want to vary the speed, preserving: VT (1350ms) and IPL (350px
    //                  REF
    //    SP	0.36	0.4	    0.45	0.51
    //    TPL	845	    900	    969	    1051
    //    TT	2347	2250	2153	2061
    //
    //    IT	972	875	778	686
    //    VPL	495	550	619	701

    private val fixedVT:Long                    = 1375L    // default visible (then it disappears) time in varying speed task
    private val fixedIPL:Int                    = 350
    private val mDistancesVT_IPL:List<Int>            = mSpeeds.map {
        (it*fixedVT + fixedIPL).toInt()
    }
    private val mDurationsVT_IPL:List<Long>           = mSpeeds.mapIndexed{ idx, speed ->
        (mDistancesVT_IPL[idx]/speed).toLong()
    }

    // ========> fixed VPL & IT
    // I want to vary the speed, preserving: VPL (550px) and IT (875ms)
    //                  REF
    //    SP	0.36	0.4	    0.45	0.51
    //    TPL	865	    900	    944	    996
    //    TT	2403	2250	2097	1953
    //
    //    IPL	315	    350	    394	    446
    //    VT	1528	1375	1222	1078
    private val fixedIT:Long                    = 875L    // default visible (then it disappears) time in varying speed task
    private val fixedVPL:Int                    = 550
    private val mDistancesVPL_IT:List<Int>      = mSpeeds.map {
        (it*fixedIT + fixedVPL).toInt()
    }
    private val mDurationsVPL_IT:List<Long>     = mSpeeds.mapIndexed{ idx, speed ->
        (mDistancesVPL_IT[idx]/speed).toLong()
    }
    private val waitInterval:Long get()         = 1000L    // interval between scenario creation and target movement start

    private val trialAbortTime:Long get()       = 2*fixedDuration    // allowed number of ms to wait for user response. after this interval the trial ends
    private val movementSamplingInterval:Long   = 10L                   // position update pace

    private var trialStartMs:Long               = 0L                    // trial onset
    private var trialEndMs:Long                 = trialAbortTime        // user press latency
    private var disposableTimer: Disposable?    = null

    private val nAdaptiveTrials                 = 30
    private val nMinMagnitude:Float             = 0.01F
    private val adoParams                       = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)
    private lateinit var taskADAParams: TaskADAParams
    private lateinit var adoWrapper:AdaptiveWrapper


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

                            val range:Float = when(subject.type){
                                TEST_MOTPRE_VH_VARSPEED_FIXVT,
                                TEST_MOTPRE_VH_VARSPEED_FIXVPL  -> mRangeSpeed
                                TEST_MOTPRE_VH_FIXSPEED         -> mRangeVisibilityFSP    //
                                else -> throw Exception("TestTTC:initTest. unrecognized subject.type")
                            }
                            
                            taskADAParams = TaskADAParams(range, nAdaptiveTrials, nMinMagnitude)
                            adoWrapper    = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

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
                                        subject.stimuliDelays, ctx, mStimuliHandler)

        mScenario = ScenarioView(ctx, null).apply {
            binding.root.addView(this)
        }

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // region CREATE TRIALS
    // =============================================================================================================================
    private fun createFixedTrials():List<TrialBasic> {
        return when (subject.type) {
/*           TEST_MOTPRE_VH                  -> createFT_VH()
//            TEST_MOTPRE_VHV                 -> createFT_VHV()
//            TEST_MOTPRE_VV                  -> createFT_VV()
//            TEST_MOTPRE_VV_CUE_ARROW        -> createFT_VV_DIR()
//            TEST_MOTPRE_VH_CUE_ARROW        -> createFT_VH_DIR()
//            TEST_MOTPRE_VV_CUE_WEIGHT       -> createFT_VV_WEIGHT()
*/
            TEST_MOTPRE_VH_FIXSPEED         -> createFT_VH_FIXSPEED()
            TEST_MOTPRE_VH_VARSPEED_FIXVT  -> createFT_VH_VARSPEED_VT_IPL(fixedVT, fixedIPL) //mDurationsVT_IPL, mDistancesVT_IPL)
            TEST_MOTPRE_VH_VARSPEED_FIXVPL  -> createFT_VH_VARSPEED_VPL_IT(fixedIT, fixedVPL) //mDurationsVPL_IT, mDistancesVPL_IT)
            else                            -> throw IllegalArgumentException("Should not happen. given test code was not recognized")
        }
    }

    private fun createAdaptiveTrials():List<TrialBasic> {
        return when (subject.type) {
            TEST_MOTPRE_VH_FIXSPEED         -> createFT_adaptive_VH_FIXSPEED()
            TEST_MOTPRE_VH_VARSPEED_FIXVT   -> createFT_adaptive_VH_VARSPEED_VT_IPL(fixedVT, fixedIPL) //mDurationsVT_IPL, mDistancesVT_IPL)
            TEST_MOTPRE_VH_VARSPEED_FIXVPL  -> createFT_adaptive_VH_VARSPEED_VPL_IT(fixedIT, fixedVPL) //mDurationsVPL_IT, mDistancesVPL_IT)
            else                            -> throw IllegalArgumentException("Should not happen. given test code was not recognized")
        }
    }

    // VISUAL - HORIZONTAL -> RIGHT - CHANGE INVISIBILITY ONSET
    // 2 x 1direction x 4lat x 8rep + 8 test@zero + 3 test = 75  -> 17 trials for one direction, for each (4) latencies + 7 at 0-latency
    private fun createFT_VH_FIXSPEED():List<TrialBasic> {

        val alltrials:MutableList<TrialBasic> = mutableListOf()

        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))

        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercVisibility[2], fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercVisibility[1], fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercVisibility[0], fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))

        val trials: MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            trials.clear()
            for(l in mPercVisibility){
                trials.add(TrialTTC(-1, subject.type, mTestLabel, l, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, l, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
            }
            trials.shuffle()
            alltrials.addAll(trials)
        }
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))

        return alltrials
    }

    // VISUAL - HORIZONTAL -> RIGHT - CHANGE INVISIBILITY ONSET
    // 4(start) + 4(end) catch + 3 fixed + 40 adaptive
    private fun createFT_adaptive_VH_FIXSPEED():List<TrialBasic> {

        val alltrials:MutableList<TrialBasic> = mutableListOf()

        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))

        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercVisibility[2], fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercVisibility[1], fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercVisibility[2], fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercVisibility[1], fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mPercVisibility[0], fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))

        val trials: MutableList<TrialBasic> = mutableListOf()

        trials.clear()
        for (i in 0 until nAdaptiveTrials)
            trials.add(TrialTTC(-1, subject.type, mTestLabel, TrialsManager.ADAPTIVE_VALUE, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true, isADA=true))

        trials.shuffle()
        alltrials.addAll(trials)

        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        alltrials.add(TrialTTC(-1, subject.type, mTestLabel, mRangeVisibilityFSP, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))

        return alltrials
    }

    // VISUAL - HORIZONTAL -> RIGHT - CHANGE TARGET SPEED, FIXED VT and IPL
    // 4 catch + 8*(8+1catch) + 4catch = 16 catch + 16 per each of 4 speeds = 80
    // catches have IPL=0 e VT == TT = mDurationsVT_IPL[i-th]
    private fun createFT_VH_VARSPEED_VT_IPL(vt:Long, ipl:Int):List<TrialBasic> { //durations:List<Long>, distances:List<Int>):List<TrialBasic> {

        // these 8 trials are inserted within valid trials
        val catchtrials:MutableList<TrialBasic> = mutableListOf()
        mSpeeds.mapIndexed{ i, speed ->
            catchtrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, mDurationsVT_IPL[i], 0, mMinSpeed, mDrawablesResource[0], true, true))
            catchtrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, mDurationsVT_IPL[i], 0, mMinSpeed, mDrawablesResource[0], true, true))
        }
        catchtrials.shuffle()

        // first 4 trials are catch trials
        val alltrials:MutableList<TrialBasic> = mutableListOf()
        mSpeeds.mapIndexed{ i, speed -> alltrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, mDurationsVT_IPL[i], 0, mMinSpeed, mDrawablesResource[0], true, true))}
        alltrials.shuffle()

        val trials: MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            trials.clear()

            mSpeeds.map{
                trials.add(TrialTTC(-1, subject.type, mTestLabel, it + mMinSpeed, vt, ipl, mMinSpeed, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, it + mMinSpeed, vt, ipl, mMinSpeed, mDrawablesResource[0], true, true))
            }
            trials.add(catchtrials.removeFirst())
            trials.shuffle()

            alltrials.addAll(trials)
        }

        // last 4 trials are catch trials
        trials.clear()
        mSpeeds.mapIndexed{ i, speed -> alltrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, mDurationsVT_IPL[i], 0, mMinSpeed, mDrawablesResource[0], true, true))}
        trials.shuffle()

        alltrials.addAll(trials)
        return alltrials
    }

    // 4 catch + 8*(5+1catch) + 4catch = 16 catch + 40 adaptive = 56
    private fun createFT_adaptive_VH_VARSPEED_VT_IPL(vt:Long, ipl:Int):List<TrialBasic> { //durations:List<Long>, distances:List<Int>):List<TrialBasic> {

        // these trials are inserted within valid trials
        val catchtrials:MutableList<TrialBasic> = mutableListOf()
        mSpeeds.mapIndexed{ i, speed ->
            catchtrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, mDurationsVT_IPL[i], 0, mMinSpeed, mDrawablesResource[0], true, true))
            catchtrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, mDurationsVT_IPL[i], 0, mMinSpeed, mDrawablesResource[0], true, true))
        }
        catchtrials.shuffle()

        // first 4 trials are catch trials
        val alltrials:MutableList<TrialBasic> = mutableListOf()
        mSpeeds.mapIndexed{ i, speed -> alltrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, mDurationsVT_IPL[i], 0, mMinSpeed, mDrawablesResource[0], true, true))}
        alltrials.shuffle()

        val trials: MutableList<TrialBasic> = mutableListOf()

        // 8 blocks of 5+1 trials each
        for (i in 0 until nAdaptiveTrials/5) {
            trials.clear()
            trials.add(TrialTTC(-1, subject.type, mTestLabel, TrialsManager.ADAPTIVE_VALUE, vt, ipl, mMinSpeed, mDrawablesResource[0], true, true, isADA=true))
            trials.add(TrialTTC(-1, subject.type, mTestLabel, TrialsManager.ADAPTIVE_VALUE, vt, ipl, mMinSpeed, mDrawablesResource[0], true, true, isADA=true))
            trials.add(TrialTTC(-1, subject.type, mTestLabel, TrialsManager.ADAPTIVE_VALUE, vt, ipl, mMinSpeed, mDrawablesResource[0], true, true, isADA=true))
            trials.add(TrialTTC(-1, subject.type, mTestLabel, TrialsManager.ADAPTIVE_VALUE, vt, ipl, mMinSpeed, mDrawablesResource[0], true, true, isADA=true))
            trials.add(TrialTTC(-1, subject.type, mTestLabel, TrialsManager.ADAPTIVE_VALUE, vt, ipl, mMinSpeed, mDrawablesResource[0], true, true, isADA=true))
            trials.add(catchtrials.removeFirst())
            trials.shuffle()

            alltrials.addAll(trials)
        }

        // last 4 trials are catch trials
        trials.clear()
        mSpeeds.mapIndexed{ i, speed -> alltrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, mDurationsVT_IPL[i], 0, mMinSpeed, mDrawablesResource[0], true, true))}
        trials.shuffle()

        alltrials.addAll(trials)
        return alltrials
    }

    // catches have IT=0 e VPL == TPL = mDistancesVPL_IT[i-th]
    private fun createFT_VH_VARSPEED_VPL_IT(_it:Long, vpl:Int):List<TrialBasic> { //durations:List<Long>, distances:List<Int>):List<TrialBasic> {

        // these trials are inserted within valid trials
        val catchtrials:MutableList<TrialBasic> = mutableListOf()
        mSpeeds.mapIndexed{ i, speed -> 
            catchtrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, 0, mDistancesVPL_IT[i], mMinSpeed, mDrawablesResource[0], true, true))
            catchtrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, 0, mDistancesVPL_IT[i], mMinSpeed, mDrawablesResource[0], true, true))
        }
        catchtrials.shuffle()

        // first 4 trials are catch trials
        val alltrials:MutableList<TrialBasic> = mutableListOf()
        mSpeeds.mapIndexed{ i, speed -> alltrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, 0, mDistancesVPL_IT[i], mMinSpeed, mDrawablesResource[0], true, true))}
        alltrials.shuffle()

        val trials: MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            trials.clear()

            mSpeeds.map{
                trials.add(TrialTTC(-1, subject.type, mTestLabel, it + mMinSpeed, _it, vpl, mMinSpeed, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, it + mMinSpeed, _it, vpl, mMinSpeed, mDrawablesResource[0], true, true))
            }
            trials.add(catchtrials.removeFirst())
            trials.shuffle()

            alltrials.addAll(trials)
        }

        // last 4 trials are catch trials
        trials.clear()
        mSpeeds.mapIndexed{ i, speed -> alltrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, 0, mDistancesVPL_IT[i], mMinSpeed, mDrawablesResource[0], true, true))}
        trials.shuffle()

        alltrials.addAll(trials)
        return alltrials
    }

    // catches have IT=0 e VPL == TPL = mDistancesVPL_IT[i-th]
    private fun createFT_adaptive_VH_VARSPEED_VPL_IT(_it:Long, vpl:Int):List<TrialBasic> { //durations:List<Long>, distances:List<Int>):List<TrialBasic> {

        // these trials are inserted within valid trials
        val catchtrials:MutableList<TrialBasic> = mutableListOf()
        mSpeeds.mapIndexed{ i, speed ->
            catchtrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, 0, mDistancesVPL_IT[i], mMinSpeed, mDrawablesResource[0], true, true))
            catchtrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, 0, mDistancesVPL_IT[i], mMinSpeed, mDrawablesResource[0], true, true))
        }
        catchtrials.shuffle()

        // first 4 trials are catch trials
        val alltrials:MutableList<TrialBasic> = mutableListOf()
        mSpeeds.mapIndexed{ i, speed -> alltrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, 0, mDistancesVPL_IT[i], mMinSpeed, mDrawablesResource[0], true, true))}
        alltrials.shuffle()

        val trials: MutableList<TrialBasic> = mutableListOf()
            // 8 blocks of 5+1 trials each
        for (i in 0 until nAdaptiveTrials/5) {
            trials.clear()
            trials.add(TrialTTC(-1, subject.type, mTestLabel, TrialsManager.ADAPTIVE_VALUE, _it, vpl, mMinSpeed, mDrawablesResource[0], true, true, isADA = true))

            trials.add(catchtrials.removeFirst())
            trials.shuffle()

            alltrials.addAll(trials)
        }

        // last 4 trials are catch trials
        trials.clear()
        mSpeeds.mapIndexed{ i, speed -> alltrials.add(TrialTTC(-1, subject.type, mTestLabel, speed + mMinSpeed, 0, mDistancesVPL_IT[i], mMinSpeed, mDrawablesResource[0], true, true))}
        trials.shuffle()

        alltrials.addAll(trials)
        return alltrials
    }

    private fun createTrialsDebug():List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()

        // no cue
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[7], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[7], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[7], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[7], false, false))


        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], false, false))

        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], true, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], false, true))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], true, false))
        trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, fixedDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], false, false))

        return trials
    }

    // 2 x 2direction x 3lat x 4rep= 48  -> 8 trials for each direction(2), for each latency(3)
    /*
    private fun createFT_VV():List<TrialBasic> {
        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for(l in mPercInvisibility){
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))
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
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, false))
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
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[4], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[6], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[4], false, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))
        }
        catch_trials.shuffle()

        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for(l in mPercInvisibility){
                trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], true, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))

                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))

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
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[7], true, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[7], true, false))
        }
        catch_trials.shuffle()

        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for (j in 0 until 3)
                trials.add(catch_trials[i*3 + j])

            for(l in mPercInvisibility){

                // congruent
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, false))

                // incongruent
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, false))

                // neutral
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[7], true, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mDrawablesResource[7], true, false))
            }


            trials.shuffle()
            alltrials.addAll(trials)
        }
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, true))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, false))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[3], true, false))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[5], true, true))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[7], true, false))
        alltrials.add(0, TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mDrawablesResource[7], true, true))

        return alltrials
    }

    // 2 x 3interference(congr/incongr/neutr) x 3lat x 4rep +
    // 2 x 3interference(congr/incongr/neutr) x 0lat x 2rep = 72 + 12 -> 8 trial for each interferences level(3), for each latency(3) + 4 catch trials for each interferences level(3)
    private fun createFT_VV_DIR():List<TrialBasic> {

        val catch_trials = mutableListOf<TrialBasic>()
        for (i in 0 until 2) {
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], false, false))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
            catch_trials.add(TrialTTC(-1, subject.type, mTestLabel, 0F, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))
        }
        catch_trials.shuffle()
        
        val alltrials:MutableList<TrialBasic> = mutableListOf()

        for (i in 0 until TRIALS_BLOCK_NREP) {
            val trials: MutableList<TrialBasic> = mutableListOf()

            for (j in 0 until 3)
                trials.add(catch_trials[i*TRIALS_BLOCK_NREP + j])

            for(l in mPercInvisibility){

                // congruent
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, false))

                // incongruent
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[6], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[4], false, false))

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
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[1], false, false))

                // heavy
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[2], false, false))

                // neutral
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, true))
                trials.add(TrialTTC(-1, subject.type, mTestLabel, movementDuration*l, movementDuration, fixedDistance, mMinVisibility, mDrawablesResource[0], false, false))
            }
            trials.shuffle()
            alltrials.addAll(trials)
        }
        return alltrials
    }
    */
    // endregion

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onStimuliEnd(){

        stopPolling()
        setAnswer(trialEndMs.toInt())
        mStimuliHandler.removeCallbacksAndMessages(null)
        mScenario.clearScenario()

        super.onStimuliEnd()
    }

    override fun initSummary(){}

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        try {
            fixedDuration = (trial as TrialTTC).TT

            mScenario.createScenario(binding.root, mTrial as TrialTTC, isLandscape, movementSamplingInterval, ::onPress)

            mStimuliHandler.postDelayed({
                trialEndMs   = trialAbortTime
                trialStartMs = uptimeMillis()
                startPolling(movementSamplingInterval)
            }, waitInterval)   // after waitInterval, start moving

            mStimuliHandler.postDelayed({
                if(trial.isCatch)   {
                        mScenario.setToEnd()
                        stopPolling()
                        mStimuliHandler.postDelayed({ mScenario.hidePoint() },200L)
                }else   mScenario.hidePoint()
            },  trial.stim_value + waitInterval)   // hide target

            mStimuliHandler.postDelayed({ stopPolling() },          fixedDuration + waitInterval)   // when movement duration is reached => stop moving
            mStimuliHandler.postDelayed({ onStimuliEnd() },           trialAbortTime + waitInterval)     // when max time is reached => force trial end
        }
        catch(e:Exception){
            e.printStackTrace()
        }
    }

    private fun onPress(){
        trialEndMs = uptimeMillis() - trialStartMs      // behavioral result
        onStimuliEnd()
    }

    private fun startPolling(iti: Long){

        disposableTimer = Observable.interval(iti, iti, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                run {
                    val elapsed = uptimeMillis() - trialStartMs
                    mScenario.movePoint(elapsed)
                }
            }
    }

    private fun stopPolling() {
        disposableTimer?.dispose()
        disposableTimer = null
    }
    // =============================================================================================================================
}