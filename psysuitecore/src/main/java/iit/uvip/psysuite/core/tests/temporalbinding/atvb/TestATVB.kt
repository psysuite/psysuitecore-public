package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.*
import iit.uvip.psysuite.core.tests.temporalbinding.SubjectBindingsParcel
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindings3latencies
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.ui.showToast
import kotlin.math.roundToInt

class TestATVB(
    ctx: Context,
    activity: Activity,
    hostfragment: Fragment,
    override val subjectparcel: SubjectBindingsParcel,
    vibrator: VibrationManager?,
    mImageView: ImageView?,
    isDebug:Boolean
) : TestBasic(ctx, activity, hostfragment, subjectparcel, vibrator, mImageView, isDebug = isDebug) {

    var LOG_TAG: String = TestATVB::class.java.simpleName

    private var noise: MediaPlayer? = null
    private var tone2sec:String = "t200hz_2s"

    private val UNIMODAL_AUDIO_CODE     = STIM_TYPE_A1
    private val TRIMODAL_AUDIO_CODE     = STIM_TYPE_A1T1V1

    private var curISI: Long = 0L

    // 36 different elements
    private val lStimuliUnbalanced: List<StimulusBindingsUnbalanced> = listOf(

        StimulusBindingsUnbalanced( TYPE_A_TV, 50),
        StimulusBindingsUnbalanced( TYPE_TV_A, 50),
        StimulusBindingsUnbalanced( TYPE_V_AT, 50),
        StimulusBindingsUnbalanced( TYPE_AT_V, 50),
        StimulusBindingsUnbalanced( TYPE_T_AV, 50),
        StimulusBindingsUnbalanced( TYPE_AV_T, 50),

        StimulusBindingsUnbalanced( TYPE_A_TV, 100),
        StimulusBindingsUnbalanced( TYPE_TV_A, 100),
        StimulusBindingsUnbalanced( TYPE_V_AT, 100),
        StimulusBindingsUnbalanced( TYPE_AT_V, 100),
        StimulusBindingsUnbalanced( TYPE_T_AV, 100),
        StimulusBindingsUnbalanced( TYPE_AV_T, 100),

        StimulusBindingsUnbalanced( TYPE_A_TV, 200),
        StimulusBindingsUnbalanced( TYPE_TV_A, 200),
        StimulusBindingsUnbalanced( TYPE_V_AT, 200),
        StimulusBindingsUnbalanced( TYPE_AT_V, 200),
        StimulusBindingsUnbalanced( TYPE_T_AV, 200),
        StimulusBindingsUnbalanced( TYPE_AV_T, 200),

        StimulusBindingsUnbalanced( TYPE_A_TV, 300),
        StimulusBindingsUnbalanced( TYPE_TV_A, 300),
        StimulusBindingsUnbalanced( TYPE_V_AT, 300),
        StimulusBindingsUnbalanced( TYPE_AT_V, 300),
        StimulusBindingsUnbalanced( TYPE_T_AV, 300),
        StimulusBindingsUnbalanced( TYPE_AV_T, 300),

        StimulusBindingsUnbalanced( TYPE_A_TV, 400),
        StimulusBindingsUnbalanced( TYPE_TV_A, 400),
        StimulusBindingsUnbalanced( TYPE_V_AT, 400),
        StimulusBindingsUnbalanced( TYPE_AT_V, 400),
        StimulusBindingsUnbalanced( TYPE_T_AV, 400),
        StimulusBindingsUnbalanced( TYPE_AV_T, 400),

        StimulusBindingsUnbalanced( TYPE_A_TV, 800),
        StimulusBindingsUnbalanced( TYPE_TV_A, 800),
        StimulusBindingsUnbalanced( TYPE_V_AT, 800),
        StimulusBindingsUnbalanced( TYPE_AT_V, 800),
        StimulusBindingsUnbalanced( TYPE_T_AV, 800),
        StimulusBindingsUnbalanced( TYPE_AV_T, 800)
    )

    // 72 different elements
    private val lStimuliBalanced: List<Stimulus3delay> = listOf(

        Stimulus3delay( 0,50, 100, 0),
        Stimulus3delay( 0,50, 0, 100),
        Stimulus3delay( 0,100, 50, 0),
        Stimulus3delay( 0,0, 50, 100),
        Stimulus3delay( 0,0, 100, 50),
        Stimulus3delay( 0,100, 0, 50),
        Stimulus3delay( 0,100, 50, 0),
        Stimulus3delay( 0,100, 0, 50),
        Stimulus3delay( 0,0, 100, 50),
        Stimulus3delay( 0,50, 100, 0),
        Stimulus3delay( 0,0, 50, 100),
        Stimulus3delay( 0,50, 0, 100),

        Stimulus3delay( 0,100, 200, 0),
        Stimulus3delay( 0,100, 0, 200),
        Stimulus3delay( 0,200, 100, 0),
        Stimulus3delay( 0,0, 100, 200),
        Stimulus3delay( 0,0, 200, 100),
        Stimulus3delay( 0,200, 0, 100),
        Stimulus3delay( 0,200, 100, 0),
        Stimulus3delay( 0,200, 0, 100),
        Stimulus3delay( 0,0, 200, 100),
        Stimulus3delay( 0,100, 200, 0),
        Stimulus3delay( 0,0, 100, 200),
        Stimulus3delay( 0,100, 0, 200),

        Stimulus3delay( 0,200, 400, 0),
        Stimulus3delay( 0,200, 0, 400),
        Stimulus3delay( 0,400, 200, 0),
        Stimulus3delay( 0,0, 200, 400),
        Stimulus3delay( 0,0, 400, 200),
        Stimulus3delay( 0,400, 0, 200),
        Stimulus3delay( 0,400, 200, 0),
        Stimulus3delay( 0,400, 0, 200),
        Stimulus3delay( 0,0, 400, 200),
        Stimulus3delay( 0,200, 400, 0),
        Stimulus3delay( 0,0, 200, 400),
        Stimulus3delay( 0,200, 0, 400),

        Stimulus3delay( 0,300, 600, 0),
        Stimulus3delay( 0,300, 0, 600),
        Stimulus3delay( 0,600, 300, 0),
        Stimulus3delay( 0,0, 300, 600),
        Stimulus3delay( 0,0, 600, 300),
        Stimulus3delay( 0,600, 0, 300),
        Stimulus3delay( 0,600, 300, 0),
        Stimulus3delay( 0,600, 0, 300),
        Stimulus3delay( 0,0, 600, 300),
        Stimulus3delay( 0,300, 600, 0),
        Stimulus3delay( 0,0, 300, 600),
        Stimulus3delay( 0,300, 0, 600),

        Stimulus3delay( 0,400, 800, 0),
        Stimulus3delay( 0,400, 0, 800),
        Stimulus3delay( 0,800, 400, 0),
        Stimulus3delay( 0,0, 400, 800),
        Stimulus3delay( 0,0, 800, 400),
        Stimulus3delay( 0,800, 0, 400),
        Stimulus3delay( 0,800, 400, 0),
        Stimulus3delay( 0,800, 0, 400),
        Stimulus3delay( 0,0, 800, 400),
        Stimulus3delay( 0,400, 800, 0),
        Stimulus3delay( 0,0, 400, 800),
        Stimulus3delay( 0,400, 0, 800),

        Stimulus3delay( 0,800, 1600, 0),
        Stimulus3delay( 0,800, 0, 1600),
        Stimulus3delay( 0,1600, 800, 0),
        Stimulus3delay( 0,0, 800, 1600),
        Stimulus3delay( 0,0, 1600, 800),
        Stimulus3delay( 0,1600, 0, 800),
        Stimulus3delay( 0,1600, 800, 0),
        Stimulus3delay( 0,1600, 0, 800),
        Stimulus3delay( 0,0, 1600, 800),
        Stimulus3delay( 0,800, 1600, 0),
        Stimulus3delay( 0,0, 800, 1600),
        Stimulus3delay( 0,800, 0, 1600)
    )

    private val WN_FIRSTSTIM_INTERVAL   = 1000L
    private val STIM_DURATION           = 50L
    private val ISI                     = 1000L // time between end of first stim and onset of second stim in DOUBLE STIM tasks

    private var allQuestions:MutableList<String> = mutableListOf()
    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle)

    companion object {

        @JvmStatic val TEST_BASIC_LABEL     = "ATVB"
        @JvmStatic val NUM_REPETITIONS      = 8
        @JvmStatic val NUM_REPETITIONS2     = 4

        @JvmStatic val TYPE_ATV  = 0
        @JvmStatic val TYPE_A_TV = 1
        @JvmStatic val TYPE_TV_A = 2
        @JvmStatic val TYPE_V_AT = 3
        @JvmStatic val TYPE_AT_V = 4
        @JvmStatic val TYPE_T_AV = 5
        @JvmStatic val TYPE_AV_T = 6

        @JvmStatic val recipients:Array<String> = arrayOf("psysuite.uvip@gmail.com") // "psysuite.uvip@gmail.com",

        fun getConditionsInfo(ctx: Context): List<TaskCodeLabels> {
            return mutableListOf(TaskCodeLabels(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_single),  TEST_ATVB_TIME_S_UNBAL, "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atvb_subtask_time_single_tag)}"),
                                 TaskCodeLabels(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_double), TEST_ATVB_TIME_D_UNBAL, "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atvb_subtask_time_double_tag)}"))
//                                 TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_single2), TEST_ATVB_TIME_S_BAL, "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atvb_subtask_time_single2_tag)}"),
//                                 TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_double2), TEST_ATVB_TIME_D_BAL, "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atvb_subtask_time_double2_tag)}"))
        }

        // unbalanced stimuli temporarily disabled
        fun getNextTrialModes():List<List<Int>>{
            return listOf(  listOf(TEST_NEXTTRIAL_ANSWER),
                            listOf(TEST_NEXTTRIAL_ANSWER))
//                            listOf(TEST_NEXTTRIAL_ANSWER),
//                            listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
        }

        fun getEmailRecipients():Array<String> = recipients
    }

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    init {
        if(mImageView == null)      throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
        else if(vibrator == null)   throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        else{
            initTest()

//            mMediaPlayerManager = MediaPlayerManager(ctx, tone2sec, duration = currStimulusDuration, handler = mStimuliHandler)
            mToneManager        = ToneManager(duration = currStimulusDuration, handler = mStimuliHandler)
            mTactileManager     = TactileManager(vibrator, duration = currStimulusDuration, handler = mStimuliHandler)
            mVisualManager      = VisualManager(STIM_TYPE_V1, mImageView, mDrawablesResource[1], duration = currStimulusDuration, handler = mStimuliHandler)
        }
    }

    override fun initTest() {

        nextTrailModality   = subjectparcel.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        allQuestions        = mutableListOf(ctx.resources.getString(R.string.atvb_question_synchro), ctx.resources.getString(R.string.atvb_question_equal))
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        createResultFile(subjectparcel, TrialBindings3latencies.LOG_HEADER)
        initSummary()

        curISI                  = ISI           // 1000L
        currStimulusDuration    = STIM_DURATION // 50L

        when (subjectparcel.type) {
            TEST_ATVB_TIME_S_UNBAL,
            TEST_ATVB_TIME_D_UNBAL  ->  createTrialsTimeUnbalanced()

            TEST_ATVB_TIME_S_BAL,
            TEST_ATVB_TIME_D_BAL    ->  createTrialsTimeBalanced()
        }
        when (subjectparcel.type) {
            TEST_ATVB_TIME_S_UNBAL,
            TEST_ATVB_TIME_S_BAL   -> {
                mQuestion       = allQuestions[0]
            }
            TEST_ATVB_TIME_D_UNBAL,
            TEST_ATVB_TIME_D_BAL   -> {
                mQuestion       = allQuestions[1]
            }
        }

        if (subjectparcel.whitenoise)    noise = MediaPlayerManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        // mTrials list
        nTrials         = mTrials.size
        currTrial       = 0

        mListBlocks     = mutableListOf((nTrials / 2F).roundToInt())    // define two blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)
//        mListBlocks     = mutableListOf(0,2)    // define two blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)

        mTestLabel      = ""
        getConditionsInfo(ctx).map {
            if (it.id == subjectparcel.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    // (36 + 3) * 8
    private fun createTrialsTimeUnbalanced() {
        var cnt = -1
        mTrials = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {

            val trials: MutableList<TrialBindingsUnBalanced> = mutableListOf()

            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_ATV, 0, validAnswers[0]))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_ATV, 0, validAnswers[0]))
            trials.add(TrialBindingsUnBalanced(++cnt, TYPE_ATV, 0, validAnswers[0]))

            // 36
            lStimuliUnbalanced.map {
                trials.add(TrialBindingsUnBalanced(++cnt, it.type, it.delay, validAnswers[1]))
            }
            trials.shuffle()
            mTrials.addAll(trials)
        }
        setTrialsID()   // set id according to their order
    }

    // (72 + 6) * 4
    private fun createTrialsTimeBalanced() {
        var cnt = -1
        mTrials = mutableListOf()
        for (i in 0 until NUM_REPETITIONS2) {

            val trials: MutableList<TrialBindings3latencies> = mutableListOf()

            trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, validAnswers[0]))
            trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, validAnswers[0]))
            trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, validAnswers[0]))
            trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, validAnswers[0]))
            trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, validAnswers[0]))
            trials.add(TrialBindings3latencies(++cnt, TYPE_ATV, 0L, 0L, 0L, validAnswers[0]))

            // 72
            lStimuliBalanced.map {
                trials.add(TrialBindings3latencies(++cnt, it.type, it.a, it.t, it.v, validAnswers[1]))
            }
            trials.shuffle()
            mTrials.addAll(trials)
        }
        setTrialsID()   // set id according to their order
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    // called by secondTrain
    override fun onTrialEnd() {

        noise?.stop()
        noise?.prepare()

        when (nextTrailModality) {

            TEST_NEXTTRIAL_VOICE_ANSWER         ->  testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
            TEST_NEXTTRIAL_ANSWER               ->  testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER  -> {
                                                    testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
                                                    testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            }
        }
    }

    override fun nextTrial(prev_result: String, elapsed: Int): Int {
        testEvent.accept(Pair(EVENT_UPDATE_TRIAL_ID, 0L))
        return super.nextTrial(prev_result, elapsed)
    }

    override fun initSummary(){

        mSummary = when (subjectparcel.type) {
            TEST_ATVB_TIME_S_UNBAL,
            TEST_ATVB_TIME_D_UNBAL  ->  ATVBUnBalancedSummary(ctx)
            else                    ->  null
        }
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    // get new trial info. start noise. schedule stimulations
    override fun show(trial: TrialBasic, isRepeat: Boolean) {

        if (isRepeat) trial.repetitions++

        noise?.start()

        when(subjectparcel.type) {

            TEST_ATVB_TIME_S_UNBAL -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_STIMULI_START, null))
                    deliverUnBalancedStimuli((trial as TrialBindingsUnBalanced))
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_ATVB_TIME_S_BAL -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_STIMULI_START, null))
                    deliverShiftedStimulus((trial as TrialBindings3latencies).a, trial.t, trial.v, audiotype = UNIMODAL_AUDIO_CODE, visualtype = STIM_TYPE_V2){ onTrialEnd()}
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_ATVB_TIME_D_UNBAL -> {
                val corr_delays = arrangeDelays(0,0,0, subjectparcel.stimuliDelay)
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_STIMULI_START, null))
                    deliverShiftedStimulus(TRIMODAL_AUDIO_CODE, corr_delays.a, corr_delays.t, corr_delays.v) // simult
                }, WN_FIRSTSTIM_INTERVAL)
                mStimuliHandler.postDelayed({
                    deliverUnBalancedStimuli((trial as TrialBindingsUnBalanced))
                }, (WN_FIRSTSTIM_INTERVAL + currStimulusDuration + curISI + corr_delays.shift))
            }
            TEST_ATVB_TIME_D_BAL -> {
                val corr_delays = arrangeDelays(0,0,0, subjectparcel.stimuliDelay)
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_STIMULI_START, null))
                    deliverShiftedStimulus(TRIMODAL_AUDIO_CODE, corr_delays.a, corr_delays.t, corr_delays.v) // simult
                }, WN_FIRSTSTIM_INTERVAL)
                mStimuliHandler.postDelayed({
                    deliverShiftedStimulus(TRIMODAL_AUDIO_CODE, (trial as TrialBindings3latencies).a, trial.t, trial.v){ onTrialEnd()}
                }, (WN_FIRSTSTIM_INTERVAL + currStimulusDuration + curISI + corr_delays.shift))
            }
        }
    }

    private fun deliverUnBalancedStimuli(trial:TrialBindingsUnBalanced){

        val corr_delays:CorrectedStimuliDelay = when(trial.type){
            TYPE_ATV    ->  arrangeDelays(0,0,0, subjectparcel.stimuliDelay)
            TYPE_A_TV   ->  arrangeDelays(0, trial.delay, trial.delay, subjectparcel.stimuliDelay)
            TYPE_TV_A   ->  arrangeDelays(trial.delay,0,0, subjectparcel.stimuliDelay)
            TYPE_T_AV   ->  arrangeDelays(trial.delay,0, trial.delay, subjectparcel.stimuliDelay)
            TYPE_AV_T   ->  arrangeDelays(0, trial.delay,0, subjectparcel.stimuliDelay)
            TYPE_V_AT   ->  arrangeDelays(trial.delay, trial.delay,0, subjectparcel.stimuliDelay)
            TYPE_AT_V   ->  arrangeDelays(0,0, trial.delay, subjectparcel.stimuliDelay)
            else        ->  arrangeDelays(0,0,0, subjectparcel.stimuliDelay)
        }
        deliverShiftedStimulus(TRIMODAL_AUDIO_CODE, corr_delays.a, corr_delays.t, corr_delays.v){ onTrialEnd()}
    }
    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================
}

/*
This App perform an Audio-Tactile-Visual Binding (ATV-B) test:

It has two sub-tests: SINGLE and DOUBLE stimulations.
Each composed by 390 trials.
After every trial, subjects are asked to report whether the three stimulations were simultaneous.

SINGLE:
It is a trimodal version of the Stanley et al 2019 experiment.
Each trial consists in a triple of stimulation modalities (audio, tactile and visual). that can be in synch or reciprocally shifted
each stim last 1 sec, isi=2+   (1 sec stim+delay, one sec rest)

DOUBLE:
Each trial consists in TWO triples of stimulation modalities (audio, tactile and visual).
In the first triple, all three modalities are simultaneous. In the second triple, they can be in synch or reciprocally shifted.
each stim last 1 sec, isi=4+   (1 sec stim, one sec rest, 1 sec stim+delay, one sec rest)
each composed by two consecutive trains of respectively 2 and 2 either audio and/or tactile stimuli (stim duration 2sec, isi=2sec). ITI=2sec.

single trial:
        __
A    __|  |__
         __
T     __|  |__
         __
V     __|  |__

          __
A      __|  |__
         __
T     __|  |__
         __
V     __|  |__


in the second train, one of the two modalities can be in synch with other, delayed/anticipated by 800 ms or absent
in total, there are 5 types of stimuli

CODE    #REP    TYPE
0       6       A,T
3       6       A
6       6       T
7       3       A+800,T
8       3       A,T+800

The presentation order is fixed, 3 repetitions of the following 12 trials:

codes order: 0,3,7,6,3,0,8,6

A,T
A
A+800,T
T
A
A,T
A,T+800
T

Exported Data: trial_id, type



OLD TASK

single trial:
                1st train               2nd train
        __    __    __    __    __  |  __    __    __
A    __|  |__|  |__|  |__|  |__|  |_|_|  |__|  |__|  |____
                                    |
        __    __    __    __    __  |  __    __    __
T    __|  |__|  |__|  |__|  |__|  |_|_|  |__|  |__|  |____
                                    |
                                    |

OLD SCHEMA
CODE    #REP    TYPE
0       6       A,T
1       3       A+200,T
2       3       A,T+200
3       6       A
4       3       A+500,T
5       3       A,T+500
6       6       T
7       3       A+800,T
8       3       A,T+800


A,T
A,T+200
A
A+800,T
T
A,T+500
A
A+200,T
A,T
A,T+800
T
A+500,T
 */