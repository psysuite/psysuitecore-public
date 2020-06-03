package iit.uvip.psysuite.core.tests.bis

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.ImageView
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import org.albaspazio.core.accessory.VibrationManager

class TestBIS(
    ctx: Context,
    override val data: SubjectBasicParcel,
    private val vibrator: VibrationManager?,
    private val mImageView: ImageView
) : TestBasic(ctx, data)
{
    var LOG_TAG:String = TestBIS::class.java.simpleName

    companion object {

        @JvmStatic
        val TEST_BASIC_LABEL = "BIS"

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

        @JvmStatic
        val STIMULUS_TYPE_AUDIO = "AUDIO"
        @JvmStatic
        val STIMULUS_TYPE_TACTILE = "TACTILE"
        @JvmStatic
        val STIMULUS_TYPE_AUDIO_TACTILE = "AUDIO_TACTILE"
        @JvmStatic
        val STIMULUS_TYPE_AUDIO_VIDEO = "AUDIO_VIDEO"

        @JvmStatic val CONFLICT_TYPE_NONE               = "none"
        @JvmStatic val CONFLICT_TYPE_AV                 = "av"
        @JvmStatic val CONFLICT_TYPE_VA                 = "va"

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

    private var mToneGen    = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
    private var mTone       = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE

    private val mBackgroundColours:List<Int> = listOf(R.drawable.white_circle, R.drawable.red_circle, R.drawable.grey_circle, R.drawable.blue_circle)
    // =======================================================================================================================================

    init{
        validAnswers = mutableListOf(
            ctx.resources.getString(R.string.bisection_rb1_text),
            ctx.resources.getString(R.string.bisection_rb3_text)
        )

        initTest()
    }

    override fun initTest(){
        // set question & create mTrials list
        when(data.type)
        {
            TEST_BISECTION_AUDIO -> initBisectionAudio()
            TEST_BISECTION_TACTILE -> initBisectionTactile()
            TEST_BISECTION_AUDIO_TACTILE -> initBisectionAudioTactile()
            TEST_BISECTION_AUDIO_VIDEO -> initBisectionAudioVideo()
        }
        nTrials     = mTrials.size
        currTrial   = 0

        createResultFile(data.label, TrialBIS.LOG_HEADER)
    }

    // a trial has this temporal line:
    // +  FIRST_STIMULUS_DELAY                          => 1st stim
    // + (mTrial.position + FIRST_STIMULUS_DELAY)       => 2nd stim
    // + (LAST_STIMULUS_DELAY + FIRST_STIMULUS_DELAY)   => 3rd stim
    // + (QUESTION_DELAY + FIRST_STIMULUS_DELAY)        => event : show question
    override fun show(trialid:Int, isRepeat:Boolean){
        mTrial = mTrials[trialid]

        if(isRepeat)    mTrial.repetitions++

        mStimuliHandler.postDelayed({
            deliverStimulus(mTrial as TrialBIS, TRIAL_STAGE_1)
            testEvent.accept(EVENT_STIMULI_START)
        }, FIRST_STIMULUS_DELAY.toLong())

        mStimuliHandler.postDelayed({
            deliverStimulus(mTrial as TrialBIS, TRIAL_STAGE_2)
        }, ((mTrial as TrialBIS).position + FIRST_STIMULUS_DELAY).toLong())

        mStimuliHandler.postDelayed({
            deliverStimulus(mTrial as TrialBIS, TRIAL_STAGE_3)
        }, (TrialBIS.LAST_STIMULUS_DELAY + FIRST_STIMULUS_DELAY).toLong())

        mStimuliHandler.postDelayed({
            testEvent.accept(EVENT_STIMULI_END)
        }, (QUESTION_DELAY + FIRST_STIMULUS_DELAY).toLong())
    }

    override fun onTrialEnd(){
        testEvent.accept(EVENT_GIVE_ANSWER)
    }

    private fun deliverStimulus(trial: TrialBIS, stage:Int=0){

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

    private fun deliverAVStimuli(trial: TrialBIS, stage:Int=0){

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
            mImageView.setImageResource(mBackgroundColours[stage])
        else{
            mStimuliHandler.postDelayed({
                mImageView.setImageResource(mBackgroundColours[stage])
            }, delay)
        }
        mStimuliHandler.postDelayed({
            mImageView.setImageResource(mBackgroundColours[0])
        }, delay + resetTime)
    }
    // -----------------------------------------------------------------------------------------------------------------
    // set question and create trials list
    // Trial is : (var id:Int=-1, val type:Int, val label:String, val position:Int, val conflict_type:String, val duration:Int, val duration2:Int=0, var correct_answer:Int=-1, var user_answer:Int=-1, var success:Boolean=false, var elapsed:Int=-1) {

    private fun createDefaultTrials(stim_type_label:String, duration:Int, duration2:Int=0){

        for(section in trialsDefaultSchema)
            for(i in 0 until section.first)
                mTrials.add(TrialBIS(-1, data.type, stim_type_label, section.second, section.third, duration, duration2))
        mTrials.shuffle()

        // set trial id according to its order in the list
        for(i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
    }

    private fun createAudioVideoTrials(durationAudio:Int, durationVideo:Int)
    {
        for(section in trialsAudioVideoSchema)
            for(i in 0 until section.first)
                when(section.third == CONFLICT_TYPE_AV){
                    true -> mTrials.add(
                        TrialBIS(
                            -1,
                            data.type,
                            STIMULUS_TYPE_AUDIO_VIDEO,
                            section.second,
                            section.third,
                            durationAudio,
                            durationVideo
                        )
                    )
                    false -> mTrials.add(
                        TrialBIS(
                            -1,
                            data.type,
                            STIMULUS_TYPE_AUDIO_VIDEO,
                            section.second,
                            section.third,
                            durationVideo,
                            durationAudio
                        )
                    )
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
        createDefaultTrials(
            STIMULUS_TYPE_AUDIO_TACTILE,
            STIMULUS_DURATION_AUDIO,
            STIMULUS_DURATION_TACTILE
        )
    }

    private fun initBisectionAudioVideo(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_mixed)
        createAudioVideoTrials(STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_VISUAL)
    }

    // =====================================================================================
    // DEBUG
    // =====================================================================================
    // Trial(val type:Int, val label:String, val conflict_type:String, val position:Int, val duration:Int)
    // just one trial for each latency
    private var trialsDefaultSchema_debug: List<Triple<Int, Int, String>> =
        listOf(Triple(2, 200, CONFLICT_TYPE_NONE))

    private fun createDefaultTrials_debug(stim_type_label:String, duration:Int, duration2:Int=0)
    {
        nTrials = trialsDefaultSchema_debug.size
        for(section in trialsDefaultSchema)
            for(i in 0 until 1)
                mTrials.add(
                    TrialBIS(
                        -1,
                        data.type,
                        stim_type_label,
                        section.second,
                        section.third,
                        duration,
                        duration2
                    )
                )
        mTrials.shuffle()

        // set trial id according to its order in the list
        for(i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
    }
    // =====================================================================================
}