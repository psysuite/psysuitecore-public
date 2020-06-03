package iit.uvip.psysuite.core.tests.tid

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import iit.uvip.psysuite.core.utility.QuestObject
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.getTimeDifference
import java.util.*


// type     : audio/vibro
// duration : ref:100 & test:[50-200] /  ref:2000 & test:[1000-4000]

// TRIAL:
//    FIRST_STIMULUS_DELAY=1500--------s1------delta1------s2-----ISI=1000ms-----s3------delta2-------s4-----QUESTION_DELAY=1500ms------domanda

//class TIDTest(ctx: Context, mType:Int, mSubjLabel:String, private val test_time:Int, private val session:Int) : Test(ctx, mType, mSubjLabel)
class TestTID(ctx: Context,
              override val data: SubjectTIDParcel,
              private val vibrator: VibrationManager?
) : TestBasic(ctx, data)
{
    var LOG_TAG:String = TestTID::class.java.simpleName

    private lateinit var mQuest: QuestObject

    lateinit var onsetDate: Date

    companion object {

        @JvmStatic
        val TEST_BASIC_LABEL = "TID"

        @JvmStatic var NUM_BLOCKS                   = 6
        @JvmStatic var NUM_TRIALS                   = 50
        @JvmStatic val ISI                          = 1000      // interval between couple1 and couple2

        @JvmStatic val REF_STIM_DUR_SHORT           = 100
        @JvmStatic val REF_STIM_DUR_LONG            = 2000

        @JvmStatic val TEST_STIMULUS_DURATION_1_MIN = 50
        @JvmStatic val TEST_STIMULUS_DURATION_1_MAX = 200

        @JvmStatic val TEST_STIMULUS_DURATION_2_MIN = 1000
        @JvmStatic val TEST_STIMULUS_DURATION_2_MAX = 4000

        @JvmStatic val STIMULUS_DURATION_AUDIO      = 50
        @JvmStatic val STIMULUS_DURATION_TACTILE    = 50
        @JvmStatic val QUESTION_DELAY               = 50      // interval between end of last stimulus and dialog onset
        @JvmStatic val FIRST_STIMULUS_DELAY         = 1500      // ms to wait before sending the first trial

        @JvmStatic val STIMULUS_TYPE_AUDIO          = "a"
        @JvmStatic val STIMULUS_TYPE_TACTILE        = "t"

        @JvmStatic val STIMULUS_DURATION_SHORT      = "short"
        @JvmStatic val STIMULUS_DURATION_LONG       = "long"

        fun getConditionsInfo(ctx: Context): List<TaskCode> {
            return mutableListOf(
                TaskCode(
                    TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO + "_" + STIMULUS_DURATION_SHORT,
                    TEST_TID_SHORT_AUDIO
                ),
                TaskCode(
                    TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_TACTILE + "_" + STIMULUS_DURATION_SHORT,
                    TEST_TID_SHORT_TACTILE
                ),
                TaskCode(
                    TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO + "_" + STIMULUS_DURATION_LONG,
                    TEST_TID_LONG_AUDIO
                ),
                TaskCode(
                    TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_TACTILE + "_" + STIMULUS_DURATION_LONG,
                    TEST_TID_LONG_TACTILE
                )
            )
        }
    }

    private var mToneGen    = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
    private var mTone       = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE

    // =======================================================================================================================================

    init{


        validAnswers = mutableListOf(
            ctx.resources.getString(R.string.tid_rb1_text),
            ctx.resources.getString(R.string.tid_rb3_text)
        )

        initTest()
    }

    override fun initTest(){

        mQuest      = QuestObject()
        currTrial   = 0
        // set question & create mTrials list
        when(data.type)
        {
            TEST_TID_SHORT_AUDIO, TEST_TID_LONG_AUDIO -> initAudio()
            TEST_TID_SHORT_TACTILE, TEST_TID_LONG_TACTILE -> initTactile()
        }
        setFirstDelta()
        nTrials     = mTrials.size
        createResultFile(data.label, TrialTID.LOG_HEADER)
    }

    private fun vars2code(): Int {
        return if (data.interval_type == 0) {
            if (data.modality == 0) TEST_TID_SHORT_AUDIO
            else TEST_TID_SHORT_TACTILE
        } else {
            if (data.modality == 0) TEST_TID_LONG_AUDIO
            else TEST_TID_LONG_TACTILE
        }
    }

    private fun code2vars(): Pair<Int, Int> {

        when (data.type) {
            TEST_TID_SHORT_AUDIO -> {
                data.modality = 0
                data.interval_type = 0
            }
            TEST_TID_SHORT_TACTILE -> {
                data.modality = 1
                data.interval_type = 0

            }
            TEST_TID_LONG_AUDIO -> {
                data.modality = 0
                data.interval_type = 1

            }
            TEST_TID_LONG_TACTILE -> {
                data.modality = 1
                data.interval_type = 1
            }
        }
        return Pair(data.modality, data.interval_type)
    }

    // a trial has this temporal line:
    // S1:          FIRST_STIMULUS_DELAY
    // S2:          FIRST_STIMULUS_DELAY + mTrial.delta1
    // S3:          FIRST_STIMULUS_DELAY + mTrial.delta1 + ISI
    // S4:          FIRST_STIMULUS_DELAY + mTrial.delta1 + ISI + mTrial.delta2
    // QUESTION:    FIRST_STIMULUS_DELAY + mTrial.delta1 + ISI + mTrial.delta2 + QUESTION_DELAY

    override fun show(trialid:Int, isRepeat:Boolean){

        onsetDate           = Date()
        mTrial = mTrials[trialid]

        // S1
        mStimuliHandler.postDelayed({
            deliverStimulus(mTrial as TrialTID, 1)
            testEvent.accept(EVENT_STIMULI_START)
        }, FIRST_STIMULUS_DELAY.toLong())

        // S2
        mStimuliHandler.postDelayed({
            deliverStimulus(mTrial as TrialTID, 2)
        }, ((mTrial as TrialTID).delta1 + FIRST_STIMULUS_DELAY).toLong())

        // S3
        mStimuliHandler.postDelayed({
            deliverStimulus(mTrial as TrialTID, 3)
        }, ((mTrial as TrialTID).delta1 + FIRST_STIMULUS_DELAY + ISI).toLong())

        // S4
        mStimuliHandler.postDelayed({
            deliverStimulus(mTrial as TrialTID, 4)
        }, ((mTrial as TrialTID).delta1 + FIRST_STIMULUS_DELAY + ISI + (mTrial as TrialTID).delta2).toLong())

        // send stimuli-end event
        mStimuliHandler.postDelayed({
            testEvent.accept(EVENT_STIMULI_END)
        }, ((mTrial as TrialTID).delta1 + FIRST_STIMULUS_DELAY + ISI + (mTrial as TrialTID).delta2 + (mTrial as TrialTID).duration + QUESTION_DELAY).toLong())
    }

    override fun onTrialEnd(){
        testEvent.accept(EVENT_GIVE_ANSWER)
    }

    private fun deliverStimulus(trial: TrialTID, id:Int=0){

        val elapsedms = getTimeDifference(onsetDate)
        Log.d(LOG_TAG,"stim num $id, elapsed: $elapsedms")
        when(trial.type) {
            TEST_TID_SHORT_AUDIO, TEST_TID_LONG_AUDIO -> mToneGen.startTone(mTone, trial.duration)
            TEST_TID_SHORT_TACTILE, TEST_TID_LONG_TACTILE -> vibrator?.vibrateSingle(trial.duration.toLong())
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // set question and create trials list
    // Trial is : (var id:Int=-1, val type:Int, val label:String, val position:Int, val conflict_type:String, val duration:Int, val duration2:Int=0, var correct_answer:Int=-1, var user_answer:Int=-1, var success:Boolean=false, var elapsed:Int=-1) {

    private fun createDefaultTrials(modality:String, session:Int, duration:Int){

        var ref_delta = REF_STIM_DUR_SHORT

        when(data.type) {
            TEST_TID_LONG_AUDIO, TEST_TID_LONG_TACTILE -> ref_delta = REF_STIM_DUR_LONG
        }

        for(b in 0 until NUM_BLOCKS){

            val block_trials:MutableList<TrialBasic> = mutableListOf()

            for(t in 0 until NUM_TRIALS /2){
                // TrialTID(id:Int=-1, val block:Int, val session:Int, type:Int, val modality:Int, val delta1:Int, val delta2:Int, val ref_first:Int, val duration:Int)
                block_trials.add(TrialTID(-1, b, session, data.type, modality, ref_delta, -1, 1, duration))
                block_trials.add(TrialTID(-1, b, session, data.type, modality, -1, ref_delta,0, duration))
            }
            block_trials.shuffle()
            mTrials.addAll(block_trials)
        }

        // set trial id according to its order in the list
        mTrials.mapIndexed { index, trial -> trial.id = (index + 1) }
    }

    private fun setFirstDelta(){
        // set first trial's test delta
        val firstdelta:Float = mQuest.getFirstValue()
        when((mTrials[0] as TrialTID).ref_first == 0) {
            true ->     (mTrials[0] as TrialTID).delta2 = firstdelta.toInt()
            else ->     (mTrials[0] as TrialTID).delta1 = firstdelta.toInt()
        }
    }

    override fun nextTrial(prev_result: String, elapsed: Int): Int {

        val newdelta: Float = mQuest.getNewValue((prev_result != ""))
        when((mTrials[currTrial+1] as TrialTID).ref_first == 1) {
            true ->     (mTrials[currTrial+1] as TrialTID).delta2 = newdelta.toInt()
            else ->     (mTrials[currTrial+1] as TrialTID).delta1 = newdelta.toInt()
        }
        return super.nextTrial(prev_result, elapsed)
    }

    // ----------------------------------
    private fun initAudio(){
        mQuestion = ctx.resources.getString(R.string.tid_question_text_audio)
        createDefaultTrials(
            STIMULUS_TYPE_AUDIO, data.session,
            STIMULUS_DURATION_AUDIO
        )
    }

    private fun initTactile(){
        mQuestion = ctx.resources.getString(R.string.tid_question_text_tactile)
        createDefaultTrials(
            STIMULUS_TYPE_TACTILE, data.session,
            STIMULUS_DURATION_TACTILE
        )
    }
    // =====================================================================================
}