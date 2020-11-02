package iit.uvip.psysuite.core.tests.bis

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.SpinnerData
import iit.uvip.psysuite.core.common.StimulusBIS
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import iit.uvip.psysuite.core.common.stimuli.*
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.ui.showToast

class TestBIS(
    ctx: Context,
    activity: Activity,
    hostfragment: Fragment,
    data: SubjectBasicParcel,
    vibrator: VibrationManager?,
    mImageView: ImageView?
) : TestBasic(ctx, activity, hostfragment, data, vibrator, mImageView){

    override var LOG_TAG:String = TestBIS::class.java.simpleName

    companion object {

        @JvmStatic val TEST_BASIC_LABEL                 = "BIS"

        @JvmStatic var NUM_TRIALS                       = 32
        @JvmStatic val STIMULUS_DURATION_VISUAL:Long    = 150
        @JvmStatic val STIMULUS_DURATION_TACTILE:Long   = 150
        @JvmStatic val STIMULUS_DURATION_AUDIO:Long     = 50
        @JvmStatic val QUESTION_DELAY                   = 1500      // latency
        @JvmStatic val FIRST_STIMULUS_DELAY             = 1000L      // ms to wait before sending the first trial
        @JvmStatic val LAST_STIMULUS_DELAY              = 1000      // ms of the third stimulus wrt first

        @JvmStatic val TRIAL_STAGE_1                = 1
        @JvmStatic val TRIAL_STAGE_2                = 2
        @JvmStatic val TRIAL_STAGE_3                = 3

        @JvmStatic val AV_STIMULUS_DELTA            = 200       // ms between the AV stimuli

        @JvmStatic val STIMULUS_TYPE_AUDIO          = "AUDIO"
        @JvmStatic val STIMULUS_TYPE_TACTILE        = "TACTILE"
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE  = "AUDIO_TACTILE"
        @JvmStatic val STIMULUS_TYPE_AUDIO_VIDEO    = "AUDIO_VIDEO"

        @JvmStatic val STIMULUS_TYPE_AUDIO_LOG          = "A"
        @JvmStatic val STIMULUS_TYPE_TACTILE_LOG        = "T"
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE_LOG  = "AT"
        @JvmStatic val STIMULUS_TYPE_AUDIO_VIDEO_LOG    = "AV"
        @JvmStatic val STIMULUS_TYPE_VIDEO_AUDIO_LOG    = "VA"

        @JvmStatic val CONFLICT_TYPE_NONE           = "none"

        fun getConditionsInfo(ctx: Context): List<SpinnerData> {
            return mutableListOf(
                SpinnerData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO           , TEST_BISECTION_AUDIO          , "${TEST_BASIC_LABEL}_$STIMULUS_TYPE_AUDIO_LOG"),
                SpinnerData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_TACTILE         , TEST_BISECTION_TACTILE        , "${TEST_BASIC_LABEL}_$STIMULUS_TYPE_TACTILE_LOG"),
                SpinnerData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO_TACTILE   , TEST_BISECTION_AUDIO_TACTILE  , "${TEST_BASIC_LABEL}_$STIMULUS_TYPE_AUDIO_TACTILE_LOG"),
                SpinnerData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO_VIDEO     , TEST_BISECTION_AUDIO_VIDEO    , "${TEST_BASIC_LABEL}_$STIMULUS_TYPE_AUDIO_VIDEO_LOG")
            )
        }
        
        fun getNextTrialModes():List<List<Int>>{
            return listOf(listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
        }
    }

    // contains : stimulus type & delay
    private var trialsDefaultSchema:List<StimulusBIS> = listOf(
        StimulusBIS(4, 200, CONFLICT_TYPE_NONE),
        StimulusBIS(6, 300, CONFLICT_TYPE_NONE),
        StimulusBIS(6, 400, CONFLICT_TYPE_NONE),
        StimulusBIS(6, 600, CONFLICT_TYPE_NONE),
        StimulusBIS(6, 700, CONFLICT_TYPE_NONE),
        StimulusBIS(4, 800, CONFLICT_TYPE_NONE)
    )

    // first stim is delivered at the given latency. the second  AV_STIMULUS_DELTA after
                                                                        // ntrials  latency conflict-type
    private var trialsAudioVideoSchema:List<StimulusBIS> = listOf(
        StimulusBIS(4, 200, STIMULUS_TYPE_VIDEO_AUDIO_LOG),
        StimulusBIS(4, 300, STIMULUS_TYPE_VIDEO_AUDIO_LOG),
        StimulusBIS(4, 400, STIMULUS_TYPE_VIDEO_AUDIO_LOG),
        StimulusBIS(4, 500, STIMULUS_TYPE_VIDEO_AUDIO_LOG),
        StimulusBIS(4, 600, STIMULUS_TYPE_VIDEO_AUDIO_LOG),
        StimulusBIS(4, 200, STIMULUS_TYPE_AUDIO_VIDEO_LOG),
        StimulusBIS(4, 300, STIMULUS_TYPE_AUDIO_VIDEO_LOG),
        StimulusBIS(4, 400, STIMULUS_TYPE_AUDIO_VIDEO_LOG),
        StimulusBIS(4, 500, STIMULUS_TYPE_AUDIO_VIDEO_LOG),
        StimulusBIS(4, 600, STIMULUS_TYPE_AUDIO_VIDEO_LOG)
    )

    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.red_circle, R.drawable.grey_circle, R.drawable.blue_circle)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){
        // set stimuli default & create mTrials list
        when {
            mImageView == null -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }

        validAnswers    = mutableListOf(ctx.resources.getString(R.string.bisection_rb1_text), ctx.resources.getString(R.string.bisection_rb3_text))

        if(!subjectparcel.isDebug)
            when(subjectparcel.type)
            {
                TEST_BISECTION_AUDIO            -> initBisectionAudio()
                TEST_BISECTION_TACTILE          -> initBisectionTactile()
                TEST_BISECTION_AUDIO_TACTILE    -> initBisectionAudioTactile()
                TEST_BISECTION_AUDIO_VIDEO      -> initBisectionAudioVideo()
            }
        else                        createTrialsDebug()

        nTrials     = mTrials.size
        currTrial   = 0

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subjectparcel.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast(
            "Should not happen. given test code was not recognized",
            ctx
        )
        createResultFile(subjectparcel, TrialBIS.LOG_HEADER)

        mNoise = AudioManager.getAudioResource(ctx,"wnoise_20s", 0.01f)

        mStimuliManager = StimuliManager(AudioManager(STIM_TYPE_A1, -1, duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx),
            TactileManager(vibrator!!, duration = STIMULUS_DURATION_TACTILE, handler = mStimuliHandler),
            VisualManager(STIM_TYPE_V2, mImageView!!, mDrawablesResource[1], mDrawablesResource[0], duration = STIMULUS_DURATION_VISUAL, handler = mStimuliHandler))

        testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createDefaultTrials(stim_type_label:String, duration:Int, duration2:Int=0){

        for(section in trialsDefaultSchema)
            for(i in 0 until section.ntrials){
                val corr_answ = if(section.position < LAST_STIMULUS_DELAY/2)    validAnswers[0]
                                else                                            validAnswers[1]
                //                      id   type       label,          corr_answ, position          conflict_type     duration  duration2
                mTrials.add(TrialBIS(-1, subjectparcel.type, stim_type_label, corr_answ, section.position, section.conflict, duration, duration2))
            }
        mTrials.shuffle()
        setTrialsID()   // set trial id according to its order in the list
    }

    private fun createAudioVideoTrials(durationAudio:Int, durationVideo:Int){
        for(section in trialsAudioVideoSchema)
            for(i in 0 until section.ntrials){
                val corr_answ = if(section.position < LAST_STIMULUS_DELAY/2)    validAnswers[0]
                else                                                            validAnswers[1]
                when(section.conflict == STIMULUS_TYPE_AUDIO_VIDEO_LOG){
                    //                                 id   type        label,                   corr_answ, position          conflict_type   duration       duration2
                    true    -> mTrials.add(TrialBIS(-1, subjectparcel.type, STIMULUS_TYPE_AUDIO_VIDEO, corr_answ, section.position, section.conflict, durationAudio, durationVideo))
                    false   -> mTrials.add(TrialBIS(-1, subjectparcel.type, STIMULUS_TYPE_AUDIO_VIDEO, corr_answ, section.position, section.conflict, durationVideo, durationAudio))
                }
            }
        mTrials.shuffle()
        setTrialsID()   // set trial id according to its order in the list
    }

    // ----------------------------------
    private fun initBisectionAudio(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_audio)
        createDefaultTrials(STIMULUS_TYPE_AUDIO, STIMULUS_DURATION_AUDIO.toInt())
    }

    private fun initBisectionTactile(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_tactile)
        createDefaultTrials(STIMULUS_TYPE_TACTILE, STIMULUS_DURATION_TACTILE.toInt())
    }

    private fun initBisectionAudioTactile(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_mixed)
        createDefaultTrials(STIMULUS_TYPE_AUDIO_TACTILE, STIMULUS_DURATION_AUDIO.toInt(), STIMULUS_DURATION_TACTILE.toInt())
    }

    private fun initBisectionAudioVideo(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_mixed)
        createAudioVideoTrials(STIMULUS_DURATION_AUDIO.toInt(), STIMULUS_DURATION_VISUAL.toInt())
    }

    private fun createTrialsDebug(){
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_mixed)
        for(i in 0 until 10000){
            val corr_answ = validAnswers[0]
                //                     id   type                        label,                        corr_answ, position          conflict_type   duration       duration2
                mTrials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_TACTILE, STIMULUS_TYPE_AUDIO_TACTILE, corr_answ, 100, CONFLICT_TYPE_NONE, STIMULUS_DURATION_AUDIO.toInt(), STIMULUS_DURATION_TACTILE.toInt()))
                mTrials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_TACTILE, STIMULUS_TYPE_AUDIO_TACTILE, corr_answ, 900, CONFLICT_TYPE_NONE, STIMULUS_DURATION_AUDIO.toInt(), STIMULUS_DURATION_TACTILE.toInt()))

                mTrials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VIDEO, STIMULUS_TYPE_AUDIO_VIDEO, corr_answ, 100, STIMULUS_TYPE_VIDEO_AUDIO_LOG, STIMULUS_DURATION_AUDIO.toInt(), STIMULUS_DURATION_VISUAL.toInt()))
                mTrials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VIDEO, STIMULUS_TYPE_AUDIO_VIDEO, corr_answ, 900, STIMULUS_TYPE_VIDEO_AUDIO_LOG, STIMULUS_DURATION_AUDIO.toInt(), STIMULUS_DURATION_VISUAL.toInt()))
                mTrials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VIDEO, STIMULUS_TYPE_AUDIO_VIDEO, corr_answ, 100, STIMULUS_TYPE_AUDIO_VIDEO_LOG, STIMULUS_DURATION_VISUAL.toInt(), STIMULUS_DURATION_AUDIO.toInt()))
                mTrials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VIDEO, STIMULUS_TYPE_AUDIO_VIDEO, corr_answ, 900, STIMULUS_TYPE_AUDIO_VIDEO_LOG, STIMULUS_DURATION_VISUAL.toInt(), STIMULUS_DURATION_AUDIO.toInt()))
        }
        setTrialsID()   // set trial id according to its order in the list
    }
    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onTrialEnd(){

        mNoise?.stop()
        mNoise?.prepare()

        testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
    }

    override fun initSummary(){}

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================

    // a trial has this temporal line:
    // +  FIRST_STIMULUS_DELAY                          => 1st stim
    // + (FIRST_STIMULUS_DELAY + mTrial.position)       => 2nd stim
    // + (FIRST_STIMULUS_DELAY + LAST_STIMULUS_DELAY)   => 3rd stim
    // + (QUESTION_DELAY + FIRST_STIMULUS_DELAY)        => event : show question
    override fun show(trial:TrialBasic, isRepeat:Boolean){

        mNoise?.start()
        if(isRepeat)    mTrial.repetitions++

        // to align bimodal stimuli, I have to delay the fastest modality by time_shift ms.
        // Thus I anticipate all main onsets by the same ms.
        // Since this code act for every kind of stimulus combination, I assume a trimodal stim
        val time_shift = when(trial.type){
            TEST_BISECTION_AUDIO_TACTILE    -> delaysAligner.getShift(STIM_TYPE_A1T1, 0,0,-1)
            TEST_BISECTION_AUDIO_VIDEO      -> delaysAligner.getShift(STIM_TYPE_A1V2, 0,-1,0)
            else                            -> 0
        }

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialBIS, TRIAL_STAGE_1)
            testEvent.accept(Pair(EVENT_STIMULI_START, null))
        }, FIRST_STIMULUS_DELAY - time_shift)

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialBIS, TRIAL_STAGE_2)
        }, (FIRST_STIMULUS_DELAY - time_shift + (trial as TrialBIS).position))

        mStimuliHandler.postDelayed({
            deliverStimulus(trial, TRIAL_STAGE_3)
        }, (FIRST_STIMULUS_DELAY - time_shift + LAST_STIMULUS_DELAY))

        mStimuliHandler.postDelayed({
            onTrialEnd()
        }, (FIRST_STIMULUS_DELAY - time_shift + QUESTION_DELAY))
    }

    private fun deliverStimulus(trial: TrialBIS, stage:Int=0){

        when(trial.type) {
            TEST_BISECTION_AUDIO            ->  deliverA1Stimulus()
            TEST_BISECTION_TACTILE          ->  deliverTStimulus()
            TEST_BISECTION_AUDIO_TACTILE    ->  deliverAlignedStimulus(STIM_TYPE_A1T1)
            TEST_BISECTION_AUDIO_VIDEO      ->  deliverAVStimuli(trial, stage)
        }
    }

    private fun deliverAVStimuli(trial:TrialBIS, stage:Int=0){

        mStimuliManager.mVisualManager!!.drawResOn = mDrawablesResource[stage]
        if(stage == TRIAL_STAGE_2){
            // mid (second) stimulus: audio and video are dissociated
            if(trial.conflict_type == STIMULUS_TYPE_VIDEO_AUDIO_LOG){
                val corr_delays = delaysAligner.arrangeDelays(STIM_TYPE_A1V2, AV_STIMULUS_DELTA.toLong(),-1,0)
                deliverShiftedStimulus(STIM_TYPE_A1V2, corr_delays.a, -1, corr_delays.v)
            }
            else{
                val corr_delays = delaysAligner.arrangeDelays(STIM_TYPE_A1V2,0, -1, AV_STIMULUS_DELTA.toLong())
                deliverShiftedStimulus(STIM_TYPE_A1V2, corr_delays.a, -1, corr_delays.v)
            }
        }
        // normal stimulus (1st or 3rd): audio and video simultaneously
        else    deliverAlignedStimulus(STIM_TYPE_A1V2)
    }

    // =====================================================================================
    // DEBUG
    // =====================================================================================
    // Trial(val type:Int, val label:String, val conflict_type:String, val position:Int, val duration:Int)
    // just one trial for each latency
    private var trialsDefaultSchema_debug: List<StimulusBIS> = listOf(StimulusBIS(2, 200, CONFLICT_TYPE_NONE))

    private fun createDefaultTrials_debug(stim_type_label:String, duration:Int, duration2:Int=0){
        nTrials = trialsDefaultSchema_debug.size
        for(section in trialsDefaultSchema)
            for(i in 0 until 1){
                val corr_answ = if(section.position < LAST_STIMULUS_DELAY/2)  validAnswers[0]
                else                                        validAnswers[1]
                mTrials.add(TrialBIS(-1, subjectparcel.type, stim_type_label, corr_answ, section.position, section.conflict, duration, duration2))
            }
        mTrials.shuffle()

        // set trial id according to its order in the list
        for(i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
    }
    // =============================================================================================================================
}