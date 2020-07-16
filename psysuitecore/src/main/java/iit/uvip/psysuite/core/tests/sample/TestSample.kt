package iit.uvip.psysuite.core.tests.sample

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.sample.TrialSample.Companion.LOG_HEADER
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.ui.showToast


/*

unimodal precision: (Audio-Vibration-Visual)

    stimulus onset
    stimulus duration
    temporal distance between two stimuli from 10 -> 500 ms  [10,15,20,25,30,35,40,50,65,70,80,90,100]
    triple stimulus (like in bisection)


*/

class TestSample(
    ctx: Context,
    activity: Activity,
    hostfragment: Fragment,
    data: SubjectBasicParcel,
    vibrator: VibrationManager?,
    mImageView: ImageView,
    isDebug:Boolean
) : TestBasic(ctx, activity, hostfragment, data, vibrator, mImageView, isDebug = isDebug)
{
    var LOG_TAG:String = TestSample::class.java.simpleName

    companion object {

        @JvmStatic val TEST_BASIC_LABEL             = "SAMPLE"

        @JvmStatic var NUM_TRIALS                   = 32
        @JvmStatic val STIMULUS_DURATION_VISUAL     = 150
        @JvmStatic val STIMULUS_DURATION_TACTILE    = 150
        @JvmStatic val STIMULUS_DURATION_AUDIO      = 50
        @JvmStatic val QUESTION_DELAY               = 1500      // latency
        @JvmStatic val FIRST_STIMULUS_DELAY         = 1000      // ms to wait before sending the first trial

        @JvmStatic val TRIAL_STAGE_1                = 1
        @JvmStatic val TRIAL_STAGE_2                = 2
        @JvmStatic val TRIAL_STAGE_3                = 3

        @JvmStatic val AV_STIMULUS_DELTA            = 200       // ms between the AV stimuli

        @JvmStatic val STIMULUS_TYPE_AUDIO          = "AUDIO"
        @JvmStatic val STIMULUS_TYPE_TACTILE        = "TACTILE"
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE  = "AUDIO_TACTILE"
        @JvmStatic val STIMULUS_TYPE_AUDIO_VIDEO    = "AUDIO_VIDEO"

        @JvmStatic val CONFLICT_TYPE_NONE           = "none"
        @JvmStatic val CONFLICT_TYPE_AV             = "av"
        @JvmStatic val CONFLICT_TYPE_VA             = "va"

        fun getConditionsInfo(ctx: Context): List<TaskCode> {
            return mutableListOf(
                TaskCode(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO, TEST_BISECTION_AUDIO),
                TaskCode(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_TACTILE, TEST_BISECTION_TACTILE),
                TaskCode(
                    TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO_TACTILE,
                    TEST_BISECTION_AUDIO_TACTILE
                ),
                TaskCode(
                    TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO_VIDEO,
                    TEST_BISECTION_AUDIO_VIDEO
                )
            )
        }
        
        fun getNextTrialModes():List<List<Int>>{
            return listOf(listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
        }
    }

    // contains : stimulus type & delay
    private var trialsDefaultSchema:List<Triple<Int,Int,String>> = listOf(
        Triple(4, 200, CONFLICT_TYPE_NONE),
        Triple(6, 300, CONFLICT_TYPE_NONE),
        Triple(6, 400, CONFLICT_TYPE_NONE),
        Triple(6, 600, CONFLICT_TYPE_NONE),
        Triple(6, 700, CONFLICT_TYPE_NONE),
        Triple(4, 800, CONFLICT_TYPE_NONE)
    )

    // first stim is delivered at the given latency. the second  AV_STIMULUS_DELTA after
                                                                        // ntrials  latency conflict-type
    private var trialsAudioVideoSchema:List<Triple<Int,Int,String>> = listOf(
        Triple(4, 200, CONFLICT_TYPE_VA),
        Triple(4, 300, CONFLICT_TYPE_VA),
        Triple(4, 400, CONFLICT_TYPE_VA),
        Triple(4, 500, CONFLICT_TYPE_VA),
        Triple(4, 600, CONFLICT_TYPE_VA),
        Triple(4, 200, CONFLICT_TYPE_AV),
        Triple(4, 300, CONFLICT_TYPE_AV),
        Triple(4, 400, CONFLICT_TYPE_AV),
        Triple(4, 500, CONFLICT_TYPE_AV),
        Triple(4, 600, CONFLICT_TYPE_AV)
    )


    private val mBackgroundColours:List<Int> = listOf(R.drawable.white_circle, R.drawable.red_circle, R.drawable.grey_circle, R.drawable.blue_circle)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    init{
        validAnswers = mutableListOf(ctx.resources.getString(R.string.bisection_rb1_text), ctx.resources.getString(R.string.bisection_rb3_text))
        initTest()
    }

    override fun initTest(){

        mImageView?.visibility = View.INVISIBLE

        // set question & create mTrials list
        when(data.type)
        {
            TEST_BISECTION_AUDIO            -> initBisectionAudio()
            TEST_BISECTION_TACTILE          -> initBisectionTactile()
            TEST_BISECTION_AUDIO_TACTILE    -> initBisectionAudioTactile()
            TEST_BISECTION_AUDIO_VIDEO      -> initBisectionAudioVideo()
        }
        nTrials     = mTrials.size
        currTrial   = 0

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == data.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast(
            "Should not happen. given test code was not recognized",
            ctx
        )

        createResultFile(data, LOG_HEADER)
    }


    // a trial has this temporal line:
    // +  FIRST_STIMULUS_DELAY                          => 1st stim
    // + (mTrial.position + FIRST_STIMULUS_DELAY)       => 2nd stim
    // + (LAST_STIMULUS_DELAY + FIRST_STIMULUS_DELAY)   => 3rd stim
    // + (QUESTION_DELAY + FIRST_STIMULUS_DELAY)        => event : show question
    override fun show(trial:TrialBasic, isRepeat:Boolean){

        if(isRepeat)    mTrial.repetitions++

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialSample, TRIAL_STAGE_1)
            testEvent.accept(Pair(EVENT_STIMULI_START, null))
        }, FIRST_STIMULUS_DELAY.toLong())

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialSample, TRIAL_STAGE_2)
        }, ((trial as TrialSample).position + FIRST_STIMULUS_DELAY).toLong())

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialSample, TRIAL_STAGE_3)
        }, (TrialSample.LAST_STIMULUS_DELAY + FIRST_STIMULUS_DELAY).toLong())

        mStimuliHandler.postDelayed({
            onTrialEnd()
        }, (QUESTION_DELAY + FIRST_STIMULUS_DELAY).toLong())
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createDefaultTrials(stim_type_label:String, duration:Int, duration2:Int=0){

        for(section in trialsDefaultSchema)
            for(i in 0 until section.first)
                mTrials.add(TrialSample(-1, data.type, stim_type_label, section.second, section.third, duration, duration2))
        mTrials.shuffle()

        // set trial id according to its order in the list
        for(i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
    }

    private fun createAudioVideoTrials(durationAudio:Int, durationVideo:Int){
        for(section in trialsAudioVideoSchema)
            for(i in 0 until section.first)
                when(section.third == CONFLICT_TYPE_AV){
                    true -> mTrials.add(TrialSample(-1, data.type, STIMULUS_TYPE_AUDIO_VIDEO, section.second, section.third, durationAudio, durationVideo))
                    false -> mTrials.add(TrialSample(-1, data.type, STIMULUS_TYPE_AUDIO_VIDEO, section.second, section.third, durationVideo, durationAudio))
                }
        mTrials.shuffle()

        // set trial id according to its order in the list
        for(i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
    }
    // ----------------------------------
    private fun initBisectionAudio(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_audio)
        createDefaultTrials(STIMULUS_TYPE_AUDIO, STIMULUS_DURATION_AUDIO)
    }

    private fun initBisectionTactile(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_tactile)
        createDefaultTrials(STIMULUS_TYPE_TACTILE, STIMULUS_DURATION_TACTILE)
    }

    private fun initBisectionAudioTactile(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_mixed)
        createDefaultTrials(STIMULUS_TYPE_AUDIO_TACTILE, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_TACTILE)
    }

    private fun initBisectionAudioVideo(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_mixed)
        createAudioVideoTrials(STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_VISUAL)
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onTrialEnd(){
        testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    private fun deliverStimulusAtTime(type:Int, onset:Long, duration:Long){
        mStimuliHandler.postDelayed({
            mImageView?.setImageResource(mBackgroundColours[0])
        }, onset)
    }

    private fun deliverStimulus(trial: TrialSample, stage:Int=0){

        when(trial.type) {
            TEST_BISECTION_AUDIO -> mToneGen.startTone(mTone, trial.duration)
            TEST_BISECTION_TACTILE -> vibrator?.vibrateSingle(trial.duration.toLong())
            TEST_BISECTION_AUDIO_TACTILE -> {
                mToneGen.startTone(mTone, trial.duration)
                vibrator?.vibrateSingle(trial.duration.toLong())
            }
            TEST_BISECTION_AUDIO_VIDEO -> deliverAVStimuli(trial, stage)
        }
    }

    private fun deliverAVStimuli(trial: TrialSample, stage:Int=0){

        when(stage == TRIAL_STAGE_2){
            true -> {
                // mid (second) stimulus: audio and video are dissociated

                when(trial.conflict_type == CONFLICT_TYPE_VA){
                    true    -> {
                        // first stimulus (and color flash)
                        deliverVideoStimulus(stage, 0, STIMULUS_DURATION_VISUAL.toLong())

                        // delayed stimulus
                        mStimuliHandler.postDelayed({
                            mToneGen.startTone(mTone, STIMULUS_DURATION_AUDIO)
                        }, AV_STIMULUS_DELTA.toLong())
                    }
                    false   -> {
                        // first stimulus (and color flash)
                        mToneGen.startTone(mTone, STIMULUS_DURATION_AUDIO)

                        // delayed stimulus
                        deliverVideoStimulus(stage, AV_STIMULUS_DELTA.toLong(), STIMULUS_DURATION_VISUAL.toLong())
                    }
                }
            }
            false -> {
                // normal stimulus: audio and video simultaneously
                mToneGen.startTone(mTone, STIMULUS_DURATION_AUDIO)
                deliverVideoStimulus(stage, 0, STIMULUS_DURATION_VISUAL.toLong())
            }
        }
    }

    private fun deliverVideoStimulus(stage:Int, delay:Long, resetTime:Long){

        if(delay.toInt() == 0)
            mImageView?.setImageResource(mBackgroundColours[stage])
        else{
            mStimuliHandler.postDelayed({
                mImageView?.setImageResource(mBackgroundColours[stage])
            }, delay)
        }
        mStimuliHandler.postDelayed({
            mImageView?.setImageResource(mBackgroundColours[0])
        }, delay + resetTime)
    }

    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================

    // =============================================================================================================================
}