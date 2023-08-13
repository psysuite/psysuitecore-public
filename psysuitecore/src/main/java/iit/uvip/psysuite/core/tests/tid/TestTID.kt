package iit.uvip.psysuite.core.tests.tid

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.adaptive.AdaptiveWrapper
import iit.uvip.psysuite.adaptive.ado.ADOParams
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.stimuli.*
import iit.uvip.psysuite.core.stimuli.StimuliManager.Companion.STIM_TYPE_A4
import iit.uvip.psysuite.core.stimuli.StimuliManager.Companion.STIM_TYPE_T1
import iit.uvip.psysuite.core.stimuli.StimuliManager.Companion.STIM_TYPE_V1
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.adaptive.TaskADAParams
import iit.uvip.psysuite.core.trials.TrialsManager
import iit.uvip.psysuite.core.utility.ConditionData
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast

// type     : audio/vibro
// duration : ref:100 & test:[50-200] /  ref:2000 & test:[1000-4000]

// TRIAL:
//    FIRST_STIMULUS_DELAY=1500--------s1------delta1------s2-----ISI=1000ms-----s3------delta2-------s4-----QUESTION_DELAY=1500ms------domanda

// show -> onTrialEnd -> EVENT_GIVE_ANSWER

class TestTID(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectTIDParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?,
              speechManager: SpeechManager?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView)
{
    override var LOG_TAG:String = TestTID::class.java.simpleName

    private var currISI:Long            = 0L
    private var currREP_X_BLOCK:Int     = 0
    private var currNTRIALS_X_BLOCK:Int = 0
    private var NLATENCIES:Int          = 0

    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle)

    private var refDelta:Float          = 0.0F

    companion object {

        @JvmStatic val TEST_BASIC_LABEL                 = "TID"
        @JvmStatic val TRAIN_LABEL                      = "TRAIN"

        @JvmStatic var NUM_BLOCKS                       = 2
        @JvmStatic var NUM_TRAIN_TRIALS                 = 100

        @JvmStatic val REF_STIM_DUR_SHORT:Float         = 200.0F
        @JvmStatic val ADO_RANGE_DUR_SHORT:Float        = 100.0F
        @JvmStatic val ISI_SHORT:Long                   = 1000L  // interval between pair#1 and pair#2
        @JvmStatic val NUM_FIXED_LATENCIES_SHORT        = 8     // four magnitudes presented before and after
        @JvmStatic val NUM_REP_X_LATENCY_X_BLOCK_SHORT  = 4     // MUST BE ODD !!!
        @JvmStatic var NUM_TRIALS_X_BLOCK_SHORT         = NUM_FIXED_LATENCIES_SHORT * NUM_REP_X_LATENCY_X_BLOCK_SHORT

        @JvmStatic val REF_STIM_DUR_LONG:Float          = 2000.0F
        @JvmStatic val ADO_RANGE_DUR_LONG:Float         = 1000.0F
        @JvmStatic val ISI_LONG:Long                    = 1000L  // interval between pair#1 and pair#2
        @JvmStatic val NUM_FIXED_LATENCIES_LONG         = 8
        @JvmStatic val NUM_REP_X_LATENCY_X_BLOCK_LONG   = 4     // MUST BE ODD !!!
        @JvmStatic var NUM_TRIALS_X_BLOCK_LONG          = NUM_FIXED_LATENCIES_LONG * NUM_REP_X_LATENCY_X_BLOCK_LONG

        @JvmStatic val recipients:Array<String>         = arrayOf(  "uvip.apptester@gmail.com","psysuite.uvip@gmail.com",
                                                                    "nicola.domenici@iit.it")

//        @JvmStatic val TEST_STIMULUS_DURATION_1_MIN = 100
//        @JvmStatic val TEST_STIMULUS_DURATION_1_MAX = 300
//
//        @JvmStatic val TEST_STIMULUS_DURATION_2_MIN = 1000
//        @JvmStatic val TEST_STIMULUS_DURATION_2_MAX = 3000

        @JvmStatic val STIMULUS_DURATION_AUDIO:Long     = 50L
        @JvmStatic val STIMULUS_DURATION_TACTILE:Long   = 50L
        @JvmStatic val STIMULUS_DURATION_VISUAL:Long    = 50L
        @JvmStatic val QUESTION_DELAY:Long              = 50L   // interval between end of last stimulus and dialog onset
        @JvmStatic val FIRST_STIMULUS_DELAY:Long        = 1500L // ms to wait before sending the first trial

        @JvmStatic val STIMULUS_TYPE_AUDIO          = "A"
        @JvmStatic val STIMULUS_TYPE_TACTILE        = "T"
        @JvmStatic val STIMULUS_TYPE_VISUAL         = "V"

        @JvmStatic val AUDIO_TYPE                   = STIM_TYPE_A4

        fun getConditionsInfo(ctx: Context): List<ConditionData> {

            val sts     = ctx.resources.getString(R.string.tid_rb_short_text)
            val stl     = ctx.resources.getString(R.string.tid_rb_long_text)

            val sts_sh  = ctx.resources.getString(R.string.tid_rb_short_text_short)
            val stl_sh  = ctx.resources.getString(R.string.tid_rb_long_text_short)

            return if(VibrationManager.sysHasVibrator(ctx))
                        mutableListOf(
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_$sts"                 , TEST_TID_SHORT_AUDIO          , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO}$sts_sh", Populations.hearing_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}_$sts"               , TEST_TID_SHORT_TACTILE        , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_TACTILE}$sts_sh", Populations.all_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}_$sts"                , TEST_TID_SHORT_VISUAL         , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL}$sts_sh", Populations.sighted_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_${TRAIN_LABEL}_$sts"  , TEST_TID_SHORT_AUDIO_TRAIN    , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO}_${TRAIN_LABEL}$sts_sh", Populations.hearing_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}_${TRAIN_LABEL}_$sts", TEST_TID_SHORT_TACTILE_TRAIN  , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_TACTILE}_${TRAIN_LABEL}$sts_sh", Populations.all_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}_${TRAIN_LABEL}_$sts" , TEST_TID_SHORT_VISUAL_TRAIN   , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL}_${TRAIN_LABEL}$sts_sh", Populations.sighted_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_$stl"                 , TEST_TID_LONG_AUDIO           , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO}$stl_sh", Populations.hearing_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}_$stl"               , TEST_TID_LONG_TACTILE         , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_TACTILE}$stl_sh", Populations.all_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}_$stl"                , TEST_TID_LONG_VISUAL          , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL}$stl_sh", Populations.sighted_populations))
                    else
                mutableListOf(
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_$sts"                 , TEST_TID_SHORT_AUDIO          , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO}$sts_sh", Populations.hearing_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}_$sts"                , TEST_TID_SHORT_VISUAL         , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL}$sts_sh", Populations.sighted_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_${TRAIN_LABEL}_$sts"  , TEST_TID_SHORT_AUDIO_TRAIN    , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO}_${TRAIN_LABEL}$sts_sh", Populations.hearing_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}_${TRAIN_LABEL}_$sts" , TEST_TID_SHORT_VISUAL_TRAIN   , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL}_${TRAIN_LABEL}$sts_sh", Populations.sighted_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_$stl"                 , TEST_TID_LONG_AUDIO           , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_AUDIO}$stl_sh", Populations.hearing_populations),
                            ConditionData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_VISUAL}_$stl"                , TEST_TID_LONG_VISUAL          , "${TEST_BASIC_LABEL}${STIMULUS_TYPE_VISUAL}$stl_sh", Populations.sighted_populations))
        }

        fun getNextTrialModes(ctx:Context):List<List<Int>>{
            return  if(VibrationManager.sysHasVibrator(ctx))
                        listOf( listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER))
                    else
                        listOf( listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER),
                                listOf(TEST_NEXTTRIAL_ANSWER))
        }

        fun getEmailRecipients():Array<String> = recipients
    }

    private val shortLatencies:List<Float>       = listOf(100.0F, 72.0F, 43.0F, 15.0F)
    private val shortTrainLatencies:List<Float>  = listOf(150.0F)
    private val longLatencies:List<Float>        = listOf(1000.0F, 720.0F, 430.0F, 150.0F)
    private var currLatencies:List<Float>        = listOf()

    private val nQuestTrials                = 30
    private val adoParams                   = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)
    private lateinit var taskADAParams: TaskADAParams
    private lateinit var adoWrapper:AdaptiveWrapper

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){

        when {
            mImageView == null  -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null    -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }
        nextTrailModality   = subject.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        mQuestion           = ctx.resources.getString(R.string.tid_question_text)
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.tid_rb1_text), ctx.resources.getString(R.string.tid_rb2_text))

        currStimulusDuration = when(subject.type){
            TEST_TID_SHORT_AUDIO, TEST_TID_LONG_AUDIO, TEST_TID_SHORT_AUDIO_TRAIN       -> STIMULUS_DURATION_AUDIO
            TEST_TID_SHORT_TACTILE, TEST_TID_LONG_TACTILE, TEST_TID_SHORT_TACTILE_TRAIN -> STIMULUS_DURATION_TACTILE
            else                                                                        -> STIMULUS_DURATION_VISUAL
        }

        // set values according to chosen latency


        when(subject.type){
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE, TEST_TID_LONG_VISUAL  -> {
                currISI             = ISI_LONG
                currREP_X_BLOCK     = NUM_REP_X_LATENCY_X_BLOCK_LONG
                currNTRIALS_X_BLOCK = NUM_TRIALS_X_BLOCK_LONG
                NLATENCIES   = longLatencies.size
                taskADAParams       = TaskADAParams(ADO_RANGE_DUR_LONG, nQuestTrials+10)
                currLatencies       = longLatencies
            }
            else -> {
                currISI             = ISI_SHORT
                currREP_X_BLOCK     = NUM_REP_X_LATENCY_X_BLOCK_SHORT
                currNTRIALS_X_BLOCK = NUM_TRIALS_X_BLOCK_SHORT
                NLATENCIES   = shortLatencies.size
                taskADAParams       = TaskADAParams(ADO_RANGE_DUR_SHORT, nQuestTrials+10)
                currLatencies       = shortLatencies
            }
        }
        adoWrapper  = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

        refDelta = when(subject.type) {
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE, TEST_TID_LONG_VISUAL -> REF_STIM_DUR_LONG
            else                                                             -> REF_STIM_DUR_SHORT
        }

        mTrialsManager =
            when (subject.trman_type) {
                TEST_TRMAN_FIXED -> {
                    val trials =    if (!subject.isDebug)   createConstantTrials(currStimulusDuration)
                                    else                    createTrialsDebug()
                    FixedTrialsManager(trials as MutableList<TrialBasic>)
                }
                else -> {
                    val trials = createQuestTrials(currStimulusDuration)
                    val trman = AdaptiveTrialsManager(trials as MutableList<TrialBasic>, adoWrapper)
                    trman.getStimulus()
                    trman
                }
            }

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(TrialTID.LOG_HEADER)

        mNoise = AudioManager.getAudioResource(ctx,"wnoise_20s", 0.01f)

        mStimuliManager =   if(vibrator != null)
                                StimuliManager(
                                    AudioManager(AUDIO_TYPE, audioResources[currStimulusDuration] ?: "t1000hz_50ms.wav",  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
                                    TactileManager(vibrator, duration = currStimulusDuration, handler = mStimuliHandler),
                                    VisualManager(STIM_TYPE_V1, mImageView!!, mDrawablesResource[1], duration = currStimulusDuration, handler = mStimuliHandler),
                                    delaysAligner, ctx, mStimuliHandler)
                            else
                                StimuliManager(
                                    AudioManager(AUDIO_TYPE, audioResources[currStimulusDuration] ?: "t1000hz_50ms.wav",  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
                                    null,
                                    VisualManager(STIM_TYPE_V1, mImageView!!, mDrawablesResource[1], duration = currStimulusDuration, handler = mStimuliHandler),
                                    delaysAligner, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    override fun initSummary(){}

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================    // set question and create trials list
    private fun createConstantTrials(duration:Long):List<TrialBasic>{

        val trials:MutableList<TrialBasic> = mutableListOf()
        val block_trials:MutableList<TrialBasic> = mutableListOf()
        when (subject.type) {
            TEST_TID_SHORT_AUDIO_TRAIN, TEST_TID_SHORT_TACTILE_TRAIN, TEST_TID_SHORT_VISUAL_TRAIN    -> {
                for(b in 0 until NUM_BLOCKS){
                    for(t in 0 until NUM_TRAIN_TRIALS/(4* NUM_BLOCKS)){
                        block_trials.add(TrialTID(-1, subject.type, t, (subject as SubjectTIDParcel).group, subject.session, refDelta, shortTrainLatencies[0],true, true,  duration, validAnswers))
                        block_trials.add(TrialTID(-1, subject.type, t, subject.group                      , subject.session, refDelta, shortTrainLatencies[0],true, false, duration, validAnswers))
                        block_trials.add(TrialTID(-1, subject.type, t, subject.group                      , subject.session, refDelta, shortTrainLatencies[0],false,true,  duration, validAnswers))
                        block_trials.add(TrialTID(-1, subject.type, t, subject.group                      , subject.session, refDelta, shortTrainLatencies[0],false,false, duration, validAnswers))
                    }
                    block_trials.shuffle()
                    trials.addAll(block_trials)
                }
            }
            else -> {
                for(b in 0 until NUM_BLOCKS){
                    for(t in 0 until currREP_X_BLOCK/2){
                        for(l in 0 until NLATENCIES){
                            block_trials.add(TrialTID(-1, subject.type, b, (subject as SubjectTIDParcel).group, subject.session, refDelta, currLatencies[l], true, true,  duration, validAnswers))
                            block_trials.add(TrialTID(-1, subject.type, b, subject.group                      , subject.session, refDelta, currLatencies[l], true, false, duration, validAnswers))
                            block_trials.add(TrialTID(-1, subject.type, b, subject.group                      , subject.session, refDelta, currLatencies[l],false, true,   duration, validAnswers))
                            block_trials.add(TrialTID(-1, subject.type, b, subject.group                      , subject.session, refDelta, currLatencies[l],false, false,  duration, validAnswers))
                        }
                    }
                    block_trials.shuffle()
                    trials.addAll(block_trials)
                }
            }
        }
        return trials
    }

    private fun createQuestTrials(duration:Long):List<TrialBasic>{

        val block_trials:MutableList<TrialTID> = mutableListOf()

        // 4*4 = 16
        for(l in 0 until NLATENCIES){
            block_trials.add(TrialTID(-1, subject.type, l, (subject as SubjectTIDParcel).group, subject.session, refDelta, currLatencies[l], true, true,  duration, validAnswers))
            block_trials.add(TrialTID(-1, subject.type, l, subject.group                      , subject.session, refDelta, currLatencies[l], true, false, duration, validAnswers))
            block_trials.add(TrialTID(-1, subject.type, l, subject.group                      , subject.session, refDelta, currLatencies[l],false, true,   duration, validAnswers))
            block_trials.add(TrialTID(-1, subject.type, l, subject.group                      , subject.session, refDelta, currLatencies[l],false, false,  duration, validAnswers))
        }

        // 8*4 = 32
        for(t in 0 until 8){
            //                    id:Int=-1, type:Int, val block:Int, val group:Int,            val session:Int,  var delta1:Int, var delta2:Int, val ref_first:Boolean, val duration:Int, answers:List<String>
            block_trials.add(TrialTID(-1, subject.type, t, (subject as SubjectTIDParcel).group, subject.session, refDelta, TrialsManager.ADAPTIVE_VALUE, true,  true, duration, validAnswers, isADA = true))
            block_trials.add(TrialTID(-1, subject.type, t, subject.group,                       subject.session, refDelta, TrialsManager.ADAPTIVE_VALUE, true, false, duration, validAnswers, isADA = true))
            block_trials.add(TrialTID(-1, subject.type, t, subject.group,                       subject.session, refDelta, TrialsManager.ADAPTIVE_VALUE, false, true, duration, validAnswers, isADA = true))
            block_trials.add(TrialTID(-1, subject.type, t, subject.group,                       subject.session, refDelta, TrialsManager.ADAPTIVE_VALUE, false, false, duration, validAnswers, isADA = true))
        }
        block_trials.shuffle()

        return block_trials
    }

    private fun createTrialsDebug():List<TrialBasic>{
        val duration = currStimulusDuration

        val trials:MutableList<TrialBasic> = mutableListOf()
        for(b in 0 until 10000){
            trials.add(TrialTID(-1, TEST_TID_SHORT_AUDIO, b,  (subject as SubjectTIDParcel).group, subject.session,  REF_STIM_DUR_SHORT, 100.0F, true, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_AUDIO, b,  subject.group                      , subject.session,  REF_STIM_DUR_SHORT, 100.0F, true, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_AUDIO, b,  subject.group                      , subject.session,  REF_STIM_DUR_SHORT, 100.0F, false, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_AUDIO, b,  subject.group                      , subject.session,  REF_STIM_DUR_SHORT, 100.0F, false, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_TACTILE, b, subject.group                     , subject.session,  REF_STIM_DUR_SHORT, 100.0F, true, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_TACTILE, b, subject.group                     , subject.session,  REF_STIM_DUR_SHORT, 100.0F, true, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_TACTILE, b, subject.group                     , subject.session,  REF_STIM_DUR_SHORT, 100.0F, false, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_TACTILE, b, subject.group                     , subject.session,  REF_STIM_DUR_SHORT, 100.0F, false, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_VISUAL, b, subject.group                      , subject.session,  REF_STIM_DUR_SHORT, 100.0F, true, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_VISUAL, b, subject.group                      , subject.session,  REF_STIM_DUR_SHORT, 100.0F, true, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_VISUAL, b, subject.group                      , subject.session,  REF_STIM_DUR_SHORT, 100.0F, false, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_SHORT_VISUAL, b, subject.group                      , subject.session,  REF_STIM_DUR_SHORT, 100.0F, false, false, duration, validAnswers))

            trials.add(TrialTID(-1, TEST_TID_LONG_AUDIO, b,  subject.group                       , subject.session,  REF_STIM_DUR_LONG, 1000.0F, true, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_AUDIO, b,  subject.group                       , subject.session,  REF_STIM_DUR_LONG, 1000.0F, true, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_AUDIO, b,  subject.group                       , subject.session,  REF_STIM_DUR_LONG, 1000.0F, false, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_AUDIO, b,  subject.group                       , subject.session,  REF_STIM_DUR_LONG, 1000.0F, false, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_TACTILE, b, subject.group                      , subject.session,  REF_STIM_DUR_LONG, 1000.0F, true, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_TACTILE, b, subject.group                      , subject.session,  REF_STIM_DUR_LONG, 1000.0F, true, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_TACTILE, b, subject.group                      , subject.session,  REF_STIM_DUR_LONG, 1000.0F, false, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_TACTILE, b, subject.group                      , subject.session,  REF_STIM_DUR_LONG, 1000.0F, false, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_VISUAL, b, subject.group                       , subject.session,  REF_STIM_DUR_LONG, 1000.0F, true, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_VISUAL, b, subject.group                       , subject.session,  REF_STIM_DUR_LONG, 1000.0F, true, false, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_VISUAL, b, subject.group                       , subject.session,  REF_STIM_DUR_LONG, 1000.0F, false, true, duration, validAnswers))
            trials.add(TrialTID(-1, TEST_TID_LONG_VISUAL, b, subject.group                       , subject.session,  REF_STIM_DUR_LONG, 1000.0F, false, false, duration, validAnswers))
        }
        return trials
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================    // a trial has this temporal line:
    //    FIRST_STIMULUS_DELAY=--1500--s1--delta1-s2-----ISI=1000ms-----s3------delta2-------s4-----QUESTION_DELAY=1500ms------domanda
    //                                  |           |                    |                    |
    // PAIR1:          FIRST_STIMULUS_DELAY
    // PAIR2:          FIRST_STIMULUS_DELAY + duration + mTrial.delta1 + duration + ISI
    // QUESTION:    FIRST_STIMULUS_DELAY + duration + mTrial.delta1 + duration + ISI + duration + mTrial.delta2 + duration + QUESTION_DELAY
    override fun show(trial: TrialBasic, isRepeat:Boolean){
        mNoise?.start()
        // PAIR 1
        mStimuliHandler.postDelayed({
            deliverPair((trial as TrialTID).type, trial.delta1)
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
        }, FIRST_STIMULUS_DELAY)

        // PAIR 2
        mStimuliHandler.postDelayed({
            deliverPair((trial as TrialTID).type, trial.delta2)
        }, FIRST_STIMULUS_DELAY + currStimulusDuration + (trial as TrialTID).delta1 + currStimulusDuration + currISI)

        // send stimuli-end event
        mStimuliHandler.postDelayed({
            onTrialEnd()
        }, FIRST_STIMULUS_DELAY + currStimulusDuration + trial.delta1 + currStimulusDuration + currISI + currStimulusDuration + trial.delta2 + currStimulusDuration + QUESTION_DELAY)
    }

    private fun deliverPair(type:Int, delta:Long){

        when(type) {
            TEST_TID_SHORT_AUDIO, TEST_TID_LONG_AUDIO, TEST_TID_SHORT_AUDIO_TRAIN       -> mStimuliManager.deliverAlignedStimuliPair(delta, AUDIO_TYPE)
            TEST_TID_SHORT_TACTILE, TEST_TID_LONG_TACTILE, TEST_TID_SHORT_TACTILE_TRAIN -> mStimuliManager.deliverAlignedStimuliPair(delta, STIM_TYPE_T1)
            TEST_TID_SHORT_VISUAL, TEST_TID_LONG_VISUAL, TEST_TID_SHORT_VISUAL_TRAIN    -> mStimuliManager.deliverAlignedStimuliPair(delta, STIM_TYPE_V1)
        }
    }

    // =============================================================================================================================
    // MANAGE TRIALS END
    // =============================================================================================================================
    // I have to transform result in string to results in 0 or 1
    override fun onEndTrial(prev_result: Int, elapsed: Int, extra_text:String){
        testEvent.accept(Triple(EVENT_UPDATE_TRIAL_ID, 0L, listOf()))
        super.onEndTrial(prev_result, elapsed, extra_text)
    }

    override fun onTrialEnd() {

        mNoise?.stop()
        mNoise?.prepare()

        when (nextTrailModality) {
            TEST_NEXTTRIAL_VOICE_ANSWER         ->  testEvent.accept(Triple(EVENT_GIVE_VOCAL_ANSWER, null, listOf()))
            TEST_NEXTTRIAL_ANSWER               ->  testEvent.accept(Triple(EVENT_GIVE_ANSWER, null, listOf()))
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER  -> {
                testEvent.accept(Triple(EVENT_GIVE_VOCAL_ANSWER, null, listOf()))
                testEvent.accept(Triple(EVENT_GIVE_ANSWER, null, listOf()))
            }

            TEST_NEXTTRIAL_AUTO         -> {
                // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
                mStimuliHandler.postDelayed({
                    testEvent.accept(Triple(EVENT_SHOW_ABORT, 1000L, listOf()))
                }, 1000L)
            }
        }
    }

    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================

    // =============================================================================================================================
}