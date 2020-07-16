package iit.uvip.psysuite.core.tests.tid

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
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
              override val data: SubjectTIDParcel,
              vibrator: VibrationManager?,
              isDebug:Boolean
) : TestBasic(ctx, activity, hostfragment, data, vibrator, isDebug = isDebug)
{
    var LOG_TAG:String = TestTID::class.java.simpleName

    private lateinit var mQuest:QuestObject
    private var isUsingQuest:Boolean    = false

    private var currStimDuration:Long   = 0L
    private var currISI:Long            = 0L
    private var currREP_X_BLOCK:Int     = 0
    private var currNTRIALS_X_BLOCK:Int = 0
    private var currREP_X_LATENCY:Int   = 0

    companion object {

        @JvmStatic var NUM_BLOCKS                       = 2

        @JvmStatic val REF_STIM_DUR_SHORT:Long          = 100L
        @JvmStatic val ISI_SHORT:Long                   = 1000L  // interval between pair#1 and pair#2
        @JvmStatic val NUM_FIXED_LATENCIES_SHORT        = 8
        @JvmStatic val NUM_REP_X_LATENCY_X_BLOCK_SHORT  = 8     // MUST BE ODD !!!
        @JvmStatic var NUM_TRIALS_X_BLOCK_SHORT         = NUM_FIXED_LATENCIES_SHORT * NUM_FIXED_LATENCIES_SHORT

        @JvmStatic val REF_STIM_DUR_LONG:Long           = 2000L
        @JvmStatic val ISI_LONG:Long                    = 1000L  // interval between pair#1 and pair#2
        @JvmStatic val NUM_FIXED_LATENCIES_LONG         = 8
        @JvmStatic val NUM_REP_X_LATENCY_X_BLOCK_LONG   = 8     // MUST BE ODD !!!
        @JvmStatic var NUM_TRIALS_X_BLOCK_LONG          = NUM_FIXED_LATENCIES_LONG * NUM_FIXED_LATENCIES_LONG

        @JvmStatic val recipients:Array<String>         = arrayOf(  "uvip.apptester@gmail.com",
                                                                    "tonelli.alessia@gmail.com",
                                                                    "nicola.domenici@iit.it") // "psysuite.uvip@gmail.com",

//        @JvmStatic val TEST_STIMULUS_DURATION_1_MIN = 50
//        @JvmStatic val TEST_STIMULUS_DURATION_1_MAX = 200
//
//        @JvmStatic val TEST_STIMULUS_DURATION_2_MIN = 1000
//        @JvmStatic val TEST_STIMULUS_DURATION_2_MAX = 4000

        @JvmStatic val STIMULUS_DURATION_AUDIO:Long     = 50L
        @JvmStatic val STIMULUS_DURATION_TACTILE:Long   = 50L
        @JvmStatic val QUESTION_DELAY:Long              = 50L   // interval between end of last stimulus and dialog onset
        @JvmStatic val FIRST_STIMULUS_DELAY:Long        = 1500L // ms to wait before sending the first trial

        @JvmStatic val STIMULUS_TYPE_AUDIO          = "A"
        @JvmStatic val STIMULUS_TYPE_TACTILE        = "T"

        fun getConditionsInfo(ctx: Context): List<TaskCode> {

            val label   = ctx.resources.getString(R.string.tid_label_short)
            val sts     = ctx.resources.getString(R.string.tid_rb_short_text)
            val stl     = ctx.resources.getString(R.string.tid_rb_long_text)

            return mutableListOf(
                TaskCode(label + "_" + STIMULUS_TYPE_AUDIO + "_" + sts    , TEST_TID_SHORT_AUDIO),
                TaskCode(label + "_" + STIMULUS_TYPE_TACTILE + "_" + sts  , TEST_TID_SHORT_TACTILE),
                TaskCode(label + "_" + STIMULUS_TYPE_AUDIO + "_" + stl    , TEST_TID_LONG_AUDIO),
                TaskCode(label + "_" + STIMULUS_TYPE_TACTILE + "_" + stl  , TEST_TID_LONG_TACTILE)
            )
        }

        fun getNextTrialModes():List<List<Int>>{
            return listOf(  listOf(TEST_NEXTTRIAL_ANSWER),
                            listOf(TEST_NEXTTRIAL_ANSWER),
                            listOf(TEST_NEXTTRIAL_ANSWER),
                            listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
        }

        fun getEmailRecipients():Array<String>{
            return recipients
        }
    }

    private val shortLatencies:List<Long>   = listOf(100, 128, 157, 185, 214, 242, 271, 300)
    private val longLatencies:List<Long>    = listOf(1000, 1280, 1570, 1850, 2140, 2420, 2710, 3000)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    init{
        if(vibrator == null)   throw Exception("VIBRATOR_NOT_DEFINED")
        else
        {
            nextTrailModality   = data.nextTrailModality
            abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
            showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

            mQuestion           = ctx.resources.getString(R.string.tid_question_text)
            validAnswers        = mutableListOf(ctx.resources.getString(R.string.tid_rb1_text), ctx.resources.getString(R.string.tid_rb3_text))

            initTest()
        }
    }

    override fun initTest(){

        currStimDuration    = STIMULUS_DURATION_AUDIO

        when(data.type){
            TEST_TID_SHORT_AUDIO, TEST_TID_LONG_AUDIO   -> currStimDuration = STIMULUS_DURATION_TACTILE
        }

        // set values according to chosen latency
        currISI             = ISI_SHORT
        currREP_X_BLOCK     = NUM_REP_X_LATENCY_X_BLOCK_SHORT
        currNTRIALS_X_BLOCK = NUM_TRIALS_X_BLOCK_SHORT
        currREP_X_LATENCY   = shortLatencies.size

        when(data.type){
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE  -> {
                currISI             = ISI_LONG
                currREP_X_BLOCK     = NUM_REP_X_LATENCY_X_BLOCK_LONG
                currNTRIALS_X_BLOCK = NUM_TRIALS_X_BLOCK_LONG
                currREP_X_LATENCY   = longLatencies.size
            }
        }

        mQuest      = QuestObject()
        currTrial   = 0

        // set question & create mTrials list
        if(isUsingQuest){
                createQuestTrials(currStimDuration)
                setTrialNonRefDelta(0, mQuest.getFirstValue())
        }
        else    createConstantTrials(currStimDuration)

        nTrials     = mTrials.size

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == data.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(data, TrialTID.LOG_HEADER)
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================    // set question and create trials list
    private fun createConstantTrials(duration:Long){

        var ref_delta = REF_STIM_DUR_SHORT
        when(data.type) {
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE -> ref_delta = REF_STIM_DUR_LONG
        }

        for(b in 0 until NUM_BLOCKS){

            val block_trials:MutableList<TrialBasic> = mutableListOf()

            for(t in 0 until currREP_X_BLOCK/2){
                for(l in 0 until currREP_X_LATENCY){
                    when(data.type) {
                        TEST_TID_SHORT_AUDIO, TEST_TID_SHORT_TACTILE    -> {
                            block_trials.add(TrialTID(-1, data.type, b, data.group, data.session,  ref_delta.toInt(), shortLatencies[l].toInt(), true, duration.toInt(), validAnswers))
                            block_trials.add(TrialTID(-1, data.type, b, data.group, data.session, shortLatencies[l].toInt(), ref_delta.toInt(),false, duration.toInt(), validAnswers))
                        }
                        TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE      -> {
                            block_trials.add(TrialTID(-1, data.type, b, data.group, data.session,  ref_delta.toInt(), longLatencies[l].toInt(), true, duration.toInt(), validAnswers))
                            block_trials.add(TrialTID(-1, data.type, b, data.group, data.session, longLatencies[l].toInt(), ref_delta.toInt(),false, duration.toInt(), validAnswers))
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
        when(data.type) {
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE -> ref_delta = REF_STIM_DUR_LONG
        }

        for(b in 0 until NUM_BLOCKS){

            val block_trials:MutableList<TrialBasic> = mutableListOf()

            for(t in 0 until NUM_TRIALS_X_BLOCK_SHORT /2){
                // TrialTID(id:Int=-1, val block:Int, val session:Int, type:Int, val modality:Int, val delta1:Int, val delta2:Int, val ref_first:Int, val duration:Int)
                block_trials.add(TrialTID(-1, data.type, b, data.group, data.session,  ref_delta.toInt(), -1, true, duration.toInt(), validAnswers))
                block_trials.add(TrialTID(-1, data.type, b, data.group, data.session, -1, ref_delta.toInt(),false, duration.toInt(), validAnswers))
            }
            block_trials.shuffle()
            mTrials.addAll(block_trials)
        }

        // set trial id according to its order in the list
        mTrials.mapIndexed { index, trial -> trial.id = (index + 1) }
    }

    // =============================================================================================================================
    // MANAGE TRIALS END
    // =============================================================================================================================
    override fun onTrialEnd() {

        when (nextTrailModality) {
            TEST_NEXTTRIAL_VOICE_ANSWER         ->  testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
            TEST_NEXTTRIAL_ANSWER               ->  testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER  -> {
                testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
                testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
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

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================    // a trial has this temporal line:
    //    FIRST_STIMULUS_DELAY=--1500--s1--delta1-s2-----ISI=1000ms-----s3------delta2-------s4-----QUESTION_DELAY=1500ms------domanda
    //                                  |           |                    |                    |
    // S1:          FIRST_STIMULUS_DELAY
    // S2:          FIRST_STIMULUS_DELAY + duration + mTrial.delta1
    // S3:          FIRST_STIMULUS_DELAY + duration + mTrial.delta1 + duration + ISI
    // S4:          FIRST_STIMULUS_DELAY + duration + mTrial.delta1 + duration + ISI + duration + mTrial.delta2
    // QUESTION:    FIRST_STIMULUS_DELAY + duration + mTrial.delta1 + duration + ISI + duration + mTrial.delta2 + duration + QUESTION_DELAY
    override fun show(trial:TrialBasic, isRepeat:Boolean){

        // S1
        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialTID)
            testEvent.accept(Pair(EVENT_STIMULI_START, null))
        }, FIRST_STIMULUS_DELAY)

        // S2
        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialTID)
        }, FIRST_STIMULUS_DELAY + currStimDuration + (trial as TrialTID).delta1 )

        // S3
        mStimuliHandler.postDelayed({
            deliverStimulus(trial)
        }, FIRST_STIMULUS_DELAY + currStimDuration + trial.delta1 + currStimDuration + currISI)

        // S4
        mStimuliHandler.postDelayed({
            deliverStimulus(trial)
        }, FIRST_STIMULUS_DELAY + currStimDuration + trial.delta1 + currStimDuration + currISI + currStimDuration + (mTrial as TrialTID).delta2)

        // send stimuli-end event
        mStimuliHandler.postDelayed({
            onTrialEnd()
        }, FIRST_STIMULUS_DELAY + currStimDuration + trial.delta1 + currStimDuration + currISI + currStimDuration + trial.delta2 + currStimDuration + QUESTION_DELAY)
    }

    private fun deliverStimulus(trial: TrialTID){

        when(trial.type) {
            TEST_TID_SHORT_AUDIO, TEST_TID_LONG_AUDIO       -> deliverA1Stimulus(trial.duration.toLong())
            TEST_TID_SHORT_TACTILE, TEST_TID_LONG_TACTILE   -> deliverTStimulus(trial.duration.toLong())
        }
    }

    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================

    // =============================================================================================================================
}