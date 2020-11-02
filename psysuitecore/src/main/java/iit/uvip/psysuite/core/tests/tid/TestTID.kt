package iit.uvip.psysuite.core.tests.tid

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.SpinnerData
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import iit.uvip.psysuite.core.common.stimuli.AudioManager
import iit.uvip.psysuite.core.common.stimuli.StimuliManager
import iit.uvip.psysuite.core.common.stimuli.TactileManager
import iit.uvip.psysuite.core.common.stimuli.VibratorNotDefinedException
import iit.uvip.psysuite.core.utility.QuestObject
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.ui.showToast

// type     : audio/vibro
// duration : ref:100 & test:[50-200] /  ref:2000 & test:[1000-4000]

// TRIAL:
//    FIRST_STIMULUS_DELAY=1500--------s1------delta1------s2-----ISI=1000ms-----s3------delta2-------s4-----QUESTION_DELAY=1500ms------domanda

// show -> onTrialEnd -> EVENT_GIVE_ANSWER

class TestTID(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subjectparcel: SubjectTIDParcel,
              vibrator: VibrationManager?
) : TestBasic(ctx, activity, hostfragment, subjectparcel, vibrator)
{
    override var LOG_TAG:String = TestTID::class.java.simpleName

    private lateinit var mQuest:QuestObject
    private var isUsingQuest:Boolean    = false

    private var currISI:Long            = 0L
    private var currREP_X_BLOCK:Int     = 0
    private var currNTRIALS_X_BLOCK:Int = 0
    private var currREP_X_LATENCY:Int   = 0

    companion object {

        @JvmStatic val TEST_BASIC_LABEL                 = "TID"

        @JvmStatic var NUM_BLOCKS                       = 2

        @JvmStatic val REF_STIM_DUR_SHORT:Long          = 200
        @JvmStatic val ISI_SHORT:Long                   = 1000L  // interval between pair#1 and pair#2
        @JvmStatic val NUM_FIXED_LATENCIES_SHORT        = 8
        @JvmStatic val NUM_REP_X_LATENCY_X_BLOCK_SHORT  = 4     // MUST BE ODD !!!
        @JvmStatic var NUM_TRIALS_X_BLOCK_SHORT         = NUM_FIXED_LATENCIES_SHORT * NUM_FIXED_LATENCIES_SHORT

        @JvmStatic val REF_STIM_DUR_LONG:Long           = 2000L
        @JvmStatic val ISI_LONG:Long                    = 1000L  // interval between pair#1 and pair#2
        @JvmStatic val NUM_FIXED_LATENCIES_LONG         = 8
        @JvmStatic val NUM_REP_X_LATENCY_X_BLOCK_LONG   = 4     // MUST BE ODD !!!
        @JvmStatic var NUM_TRIALS_X_BLOCK_LONG          = NUM_FIXED_LATENCIES_LONG * NUM_FIXED_LATENCIES_LONG

        @JvmStatic val recipients:Array<String>         = arrayOf(  "uvip.apptester@gmail.com",
                                                                    "tonelli.alessia@gmail.com",
                                                                    "nicola.domenici@iit.it") // "psysuite.uvip@gmail.com",

//        @JvmStatic val TEST_STIMULUS_DURATION_1_MIN = 100
//        @JvmStatic val TEST_STIMULUS_DURATION_1_MAX = 300
//
//        @JvmStatic val TEST_STIMULUS_DURATION_2_MIN = 1000
//        @JvmStatic val TEST_STIMULUS_DURATION_2_MAX = 3000

        @JvmStatic val STIMULUS_DURATION_AUDIO:Long     = 50L
        @JvmStatic val STIMULUS_DURATION_TACTILE:Long   = 50L
        @JvmStatic val QUESTION_DELAY:Long              = 50L   // interval between end of last stimulus and dialog onset
        @JvmStatic val FIRST_STIMULUS_DELAY:Long        = 1500L // ms to wait before sending the first trial

        @JvmStatic val STIMULUS_TYPE_AUDIO          = "A"
        @JvmStatic val STIMULUS_TYPE_TACTILE        = "T"

        fun getConditionsInfo(ctx: Context): List<SpinnerData> {

            val sts     = ctx.resources.getString(R.string.tid_rb_short_text)
            val stl     = ctx.resources.getString(R.string.tid_rb_long_text)

            val sts_sh  = ctx.resources.getString(R.string.tid_rb_short_text_short)
            val stl_sh  = ctx.resources.getString(R.string.tid_rb_long_text_short)

            return mutableListOf(
                SpinnerData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_$sts"    , TEST_TID_SHORT_AUDIO, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_$sts_sh"),
                SpinnerData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}_$sts"  , TEST_TID_SHORT_TACTILE, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}_$sts_sh"),
                SpinnerData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_$stl"    , TEST_TID_LONG_AUDIO, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_AUDIO}_$stl_sh"),
                SpinnerData("${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}_$stl"  , TEST_TID_LONG_TACTILE, "${TEST_BASIC_LABEL}_${STIMULUS_TYPE_TACTILE}_$stl_sh")
            )
        }

        fun getNextTrialModes():List<List<Int>>{
            return listOf(  listOf(TEST_NEXTTRIAL_ANSWER),
                            listOf(TEST_NEXTTRIAL_ANSWER),
                            listOf(TEST_NEXTTRIAL_ANSWER),
                            listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
        }

        fun getEmailRecipients():Array<String> = recipients
    }

    private val shortLatencies:List<Long>   = listOf(100, 128, 157, 185, 214, 242, 271, 300)
    private val longLatencies:List<Long>    = listOf(1000, 1280, 1570, 1850, 2140, 2420, 2710, 3000)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){

        if(vibrator == null)    throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")

        nextTrailModality   = subjectparcel.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        mQuestion           = ctx.resources.getString(R.string.tid_question_text)
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.tid_rb1_text), ctx.resources.getString(R.string.tid_rb3_text))
        
        currStimulusDuration = STIMULUS_DURATION_AUDIO
        when(subjectparcel.type){
            TEST_TID_SHORT_AUDIO, TEST_TID_LONG_AUDIO   -> currStimulusDuration = STIMULUS_DURATION_TACTILE
        }

        // set values according to chosen latency
        currISI             = ISI_SHORT
        currREP_X_BLOCK     = NUM_REP_X_LATENCY_X_BLOCK_SHORT
        currNTRIALS_X_BLOCK = NUM_TRIALS_X_BLOCK_SHORT
        currREP_X_LATENCY   = shortLatencies.size

        when(subjectparcel.type){
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE  -> {
                currISI             = ISI_LONG
                currREP_X_BLOCK     = NUM_REP_X_LATENCY_X_BLOCK_LONG
                currNTRIALS_X_BLOCK = NUM_TRIALS_X_BLOCK_LONG
                currREP_X_LATENCY   = longLatencies.size
            }
        }

        mQuest      = QuestObject()
        currTrial   = 0

        if(!subjectparcel.isDebug){
            // set question & create mTrials list
            if(isUsingQuest){
                createQuestTrials(currStimulusDuration)
                setTrialNonRefDelta(0, mQuest.getFirstValue())
            }
            else    createConstantTrials(currStimulusDuration)
        }
        else                        createTrialsDebug()

        nTrials     = mTrials.size

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subjectparcel.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(subjectparcel, TrialTID.LOG_HEADER)

        mNoise = AudioManager.getAudioResource(ctx,"wnoise_20s", 0.01f)

        mStimuliManager = StimuliManager(AudioManager(STIM_TYPE_A1, -1, duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
            TactileManager(vibrator, duration = currStimulusDuration, handler = mStimuliHandler),null)

        testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================    // set question and create trials list
    private fun createConstantTrials(duration:Long){

        var ref_delta = REF_STIM_DUR_SHORT
        when(subjectparcel.type) {
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE -> ref_delta = REF_STIM_DUR_LONG
        }

        for(b in 0 until NUM_BLOCKS){

            val block_trials:MutableList<TrialBasic> = mutableListOf()

            for(t in 0 until currREP_X_BLOCK/2){
                for(l in 0 until currREP_X_LATENCY){
                    when(subjectparcel.type) {
                        TEST_TID_SHORT_AUDIO, TEST_TID_SHORT_TACTILE    -> {
                            block_trials.add(TrialTID(-1, subjectparcel.type, b, (subjectparcel as SubjectTIDParcel).group, subjectparcel.session,  ref_delta.toInt(), shortLatencies[l].toInt(), true, duration.toInt(), validAnswers))
                            block_trials.add(TrialTID(-1, subjectparcel.type, b, subjectparcel.group, subjectparcel.session, shortLatencies[l].toInt(), ref_delta.toInt(),false, duration.toInt(), validAnswers))
                        }
                        TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE      -> {
                            block_trials.add(TrialTID(-1, subjectparcel.type, b, (subjectparcel as SubjectTIDParcel).group, subjectparcel.session,  ref_delta.toInt(), longLatencies[l].toInt(), true, duration.toInt(), validAnswers))
                            block_trials.add(TrialTID(-1, subjectparcel.type, b, subjectparcel.group, subjectparcel.session, longLatencies[l].toInt(), ref_delta.toInt(),false, duration.toInt(), validAnswers))
                        }
                    }
                }
            }
            block_trials.shuffle()
            mTrials.addAll(block_trials)
        }

        // set trial id according to its order in the list
        mTrials.mapIndexed { index, trial -> trial.id = (index + 1) }
    }

    private fun createQuestTrials(duration:Long){

        var ref_delta = REF_STIM_DUR_SHORT
        when(subjectparcel.type) {
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE -> ref_delta = REF_STIM_DUR_LONG
        }

        for(b in 0 until NUM_BLOCKS){

            val block_trials:MutableList<TrialBasic> = mutableListOf()

            for(t in 0 until NUM_TRIALS_X_BLOCK_SHORT /2){
                // TrialTID(id:Int=-1, val block:Int, val session:Int, type:Int, val modality:Int, val delta1:Int, val delta2:Int, val ref_first:Int, val duration:Int)
                block_trials.add(TrialTID(-1, subjectparcel.type, b, (subjectparcel as SubjectTIDParcel).group, subjectparcel.session,  ref_delta.toInt(), -1, true, duration.toInt(), validAnswers))
                block_trials.add(TrialTID(-1, subjectparcel.type, b, subjectparcel.group, subjectparcel.session, -1, ref_delta.toInt(),false, duration.toInt(), validAnswers))
            }
            block_trials.shuffle()
            mTrials.addAll(block_trials)
        }

        // set trial id according to its order in the list
        mTrials.mapIndexed { index, trial -> trial.id = (index + 1) }
    }

    private fun createTrialsDebug(){
        val duration = currStimulusDuration

        for(b in 0 until 10000){
            mTrials.add(TrialTID(-1, TEST_TID_SHORT_AUDIO, b,  (subjectparcel as SubjectTIDParcel).group, subjectparcel.session,  REF_STIM_DUR_SHORT.toInt(),         100, true, duration.toInt(), validAnswers))
            mTrials.add(TrialTID(-1, TEST_TID_SHORT_TACTILE, b,
                subjectparcel.group, subjectparcel.session,  REF_STIM_DUR_SHORT.toInt(),         100, true, duration.toInt(), validAnswers))

            mTrials.add(TrialTID(-1, TEST_TID_SHORT_AUDIO, b,  subjectparcel.group, subjectparcel.session,  REF_STIM_DUR_LONG.toInt(),         2000, true, duration.toInt(), validAnswers))
            mTrials.add(TrialTID(-1, TEST_TID_SHORT_TACTILE, b,
                subjectparcel.group, subjectparcel.session,  REF_STIM_DUR_LONG.toInt(),         2000, true, duration.toInt(), validAnswers))
        }
        setTrialsID()   // set trial id according to its order in the list
    }
    // =============================================================================================================================
    // MANAGE TRIALS END
    // =============================================================================================================================
    override fun onTrialEnd() {

        mNoise?.stop()
        mNoise?.prepare()

        when (nextTrailModality) {
            TEST_NEXTTRIAL_VOICE_ANSWER         ->  testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
            TEST_NEXTTRIAL_ANSWER               ->  testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER  -> {
                testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
                testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            }

            TEST_NEXTTRIAL_AUTO         -> {
                // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_SHOW_ABORT, 1000L))
                }, 1000L)
            }
        }
    }

    // in case of quest-based task, define new trial's nonref-delta & success
    override fun getNewTrial():TrialBasic{

        return  if(isUsingQuest) {
            val newdelta: Float = mQuest.getNewValue(mTrial.success)
            currTrial++
            setTrialNonRefDelta(currTrial, newdelta)
            mTrials[currTrial]
        }
        else super.getNewTrial()
    }

    // set next trial NON-ref delta and success value
    private fun setTrialNonRefDelta(trial_id:Int, nonref_delta:Float){

        if((mTrials[trial_id] as TrialTID).ref_first)       (mTrials[trial_id] as TrialTID).delta2 = nonref_delta.toInt()
        else                                                (mTrials[trial_id] as TrialTID).delta1 = nonref_delta.toInt()

        (mTrials[trial_id] as TrialTID).correct_answer =    if((mTrials[trial_id] as TrialTID).delta1 > (mTrials[trial_id] as TrialTID).delta2) validAnswers[0]
        else                                                                                validAnswers[1]
    }

    override fun initSummary(){}

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================    // a trial has this temporal line:
    //    FIRST_STIMULUS_DELAY=--1500--s1--delta1-s2-----ISI=1000ms-----s3------delta2-------s4-----QUESTION_DELAY=1500ms------domanda
    //                                  |           |                    |                    |
    // PAIR1:          FIRST_STIMULUS_DELAY
    // PAIR2:          FIRST_STIMULUS_DELAY + duration + mTrial.delta1 + duration + ISI
    // QUESTION:    FIRST_STIMULUS_DELAY + duration + mTrial.delta1 + duration + ISI + duration + mTrial.delta2 + duration + QUESTION_DELAY
    override fun show(trial:TrialBasic, isRepeat:Boolean){

        mNoise?.start()
        // PAIR 1
        mStimuliHandler.postDelayed({
            deliverPair((trial as TrialTID).type, trial.delta1.toLong())
            testEvent.accept(Pair(EVENT_STIMULI_START, null))
        }, FIRST_STIMULUS_DELAY)

        // PAIR 2
        mStimuliHandler.postDelayed({
            deliverPair((trial as TrialTID).type, trial.delta2.toLong())
        }, FIRST_STIMULUS_DELAY + currStimulusDuration + (trial as TrialTID).delta1 + currStimulusDuration + currISI)

        // send stimuli-end event
        mStimuliHandler.postDelayed({
            onTrialEnd()
        }, FIRST_STIMULUS_DELAY + currStimulusDuration + trial.delta1 + currStimulusDuration + currISI + currStimulusDuration + trial.delta2 + currStimulusDuration + QUESTION_DELAY)
    }

    private fun deliverPair(type:Int, delta:Long){

        when(type) {
            TEST_TID_SHORT_AUDIO, TEST_TID_LONG_AUDIO       -> deliverAlignedStimuliPair(delta, STIM_TYPE_A1)
            TEST_TID_SHORT_TACTILE, TEST_TID_LONG_TACTILE   -> deliverAlignedStimuliPair(delta, STIM_TYPE_T1)
        }
    }

    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================

    // =============================================================================================================================
}