package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import android.content.Context
import android.media.MediaPlayer
import android.widget.ImageView
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.*
import iit.uvip.psysuite.core.tests.temporalbinding.atb.SubjectATBParcel
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.showToast
import java.util.Collections.max

class TestATVB(
    ctx: Context,
    override val data: SubjectATBParcel,
    private val vibrator: VibrationManager?,
    private val mImageView: ImageView
) : TestBasic(ctx, data) {

    var LOG_TAG: String = TestATVB::class.java.simpleName

    private var noise: MediaPlayer? = null
    private var tone1sec: MediaPlayer
    private var tone2sec: MediaPlayer
    private lateinit var currTone: MediaPlayer
    private val mBackgroundColours: List<Int> = listOf(
        R.drawable.white_circle,
        R.drawable.red_circle,
        R.drawable.grey_circle,
        R.drawable.blue_circle
    )

    private var curISI: Long = 0L
    private var curStimDuration: Long = 0L

    private val TYPE_AUDIO = 0
    private val TYPE_TACTILE = 1
    private val TYPE_VISUAL = 2
    private val TYPE_AUDIOVISUAL = 3
    private val TYPE_AUDIOTACTILE = 4
    private val TYPE_VISUALTACTILE = 5
    private val TYPE_AUDIOVISUALTACTILE = 6

    // 7 stimuli types
    private val STIM_TYPE_TIME_ATV = 0
    private val STIM_TYPE_TIME_A_TVx = 1
    private val STIM_TYPE_TIME_Ax_TV = 2
    private val STIM_TYPE_TIME_V_ATx = 3
    private val STIM_TYPE_TIME_Vx_AT = 4
    private val STIM_TYPE_TIME_T_AVx = 5
    private val STIM_TYPE_TIME_Tx_AV = 6

    // 37 different elements
    private val lStimuli: List<StimulusTypeDelay> = listOf(
        StimulusTypeDelay(STIM_TYPE_TIME_ATV, 0),

        StimulusTypeDelay(STIM_TYPE_TIME_A_TVx, 100),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_TV, 100),
        StimulusTypeDelay(STIM_TYPE_TIME_V_ATx, 100),
        StimulusTypeDelay(STIM_TYPE_TIME_Vx_AT, 100),
        StimulusTypeDelay(STIM_TYPE_TIME_T_AVx, 100),
        StimulusTypeDelay(STIM_TYPE_TIME_Tx_AV, 100),

        StimulusTypeDelay(STIM_TYPE_TIME_A_TVx, 200),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_TV, 200),
        StimulusTypeDelay(STIM_TYPE_TIME_V_ATx, 200),
        StimulusTypeDelay(STIM_TYPE_TIME_Vx_AT, 200),
        StimulusTypeDelay(STIM_TYPE_TIME_T_AVx, 200),
        StimulusTypeDelay(STIM_TYPE_TIME_Tx_AV, 200),

        StimulusTypeDelay(STIM_TYPE_TIME_A_TVx, 300),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_TV, 300),
        StimulusTypeDelay(STIM_TYPE_TIME_V_ATx, 300),
        StimulusTypeDelay(STIM_TYPE_TIME_Vx_AT, 300),
        StimulusTypeDelay(STIM_TYPE_TIME_T_AVx, 300),
        StimulusTypeDelay(STIM_TYPE_TIME_Tx_AV, 300),

        StimulusTypeDelay(STIM_TYPE_TIME_A_TVx, 400),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_TV, 400),
        StimulusTypeDelay(STIM_TYPE_TIME_V_ATx, 400),
        StimulusTypeDelay(STIM_TYPE_TIME_Vx_AT, 400),
        StimulusTypeDelay(STIM_TYPE_TIME_T_AVx, 400),
        StimulusTypeDelay(STIM_TYPE_TIME_Tx_AV, 400),

        StimulusTypeDelay(STIM_TYPE_TIME_A_TVx, 800),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_TV, 800),
        StimulusTypeDelay(STIM_TYPE_TIME_V_ATx, 800),
        StimulusTypeDelay(STIM_TYPE_TIME_Vx_AT, 800),
        StimulusTypeDelay(STIM_TYPE_TIME_T_AVx, 800),
        StimulusTypeDelay(STIM_TYPE_TIME_Tx_AV, 800),

        StimulusTypeDelay(STIM_TYPE_TIME_A_TVx, 1200),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_TV, 1200),
        StimulusTypeDelay(STIM_TYPE_TIME_V_ATx, 1200),
        StimulusTypeDelay(STIM_TYPE_TIME_Vx_AT, 1200),
        StimulusTypeDelay(STIM_TYPE_TIME_T_AVx, 1200),
        StimulusTypeDelay(STIM_TYPE_TIME_Tx_AV, 1200)
    )
    // 37 different elements
    private val lStimuli3delay: List<Stimulus3delay> = listOf(

        Stimulus3delay( 100, 200, 0),
        Stimulus3delay( 100, 0, 200),
        Stimulus3delay( 200, 100, 0),
        Stimulus3delay( 0, 100, 200),
        Stimulus3delay( 0, 200, 100),
        Stimulus3delay( 200, 0, 100),
        Stimulus3delay( 200, 100, 0),
        Stimulus3delay( 200, 0, 100),
        Stimulus3delay( 0, 200, 100),
        Stimulus3delay( 100, 200, 0),
        Stimulus3delay( 0, 100, 200),
        Stimulus3delay( 100, 0, 200),

        Stimulus3delay( 200, 400, 0),
        Stimulus3delay( 200, 0, 400),
        Stimulus3delay( 400, 200, 0),
        Stimulus3delay( 0, 200, 400),
        Stimulus3delay( 0, 400, 200),
        Stimulus3delay( 400, 0, 200),
        Stimulus3delay( 400, 200, 0),
        Stimulus3delay( 400, 0, 200),
        Stimulus3delay( 0, 400, 200),
        Stimulus3delay( 200, 400, 0),
        Stimulus3delay( 0, 200, 400),
        Stimulus3delay( 200, 0, 400),

        Stimulus3delay( 300, 600, 0),
        Stimulus3delay( 300, 0, 600),
        Stimulus3delay( 600, 300, 0),
        Stimulus3delay( 0, 300, 600),
        Stimulus3delay( 0, 600, 300),
        Stimulus3delay( 600, 0, 300),
        Stimulus3delay( 600, 300, 0),
        Stimulus3delay( 600, 0, 300),
        Stimulus3delay( 0, 600, 300),
        Stimulus3delay( 300, 600, 0),
        Stimulus3delay( 0, 300, 600),
        Stimulus3delay( 300, 0, 600),

        Stimulus3delay( 400, 800, 0),
        Stimulus3delay( 400, 0, 800),
        Stimulus3delay( 800, 400, 0),
        Stimulus3delay( 0, 400, 800),
        Stimulus3delay( 0, 800, 400),
        Stimulus3delay( 800, 0, 400),
        Stimulus3delay( 800, 400, 0),
        Stimulus3delay( 800, 0, 400),
        Stimulus3delay( 0, 800, 400),
        Stimulus3delay( 400, 800, 0),
        Stimulus3delay( 0, 400, 800),
        Stimulus3delay( 400, 0, 800),

        Stimulus3delay( 800, 1600, 0),
        Stimulus3delay( 800, 0, 1600),
        Stimulus3delay( 1600, 800, 0),
        Stimulus3delay( 0, 800, 1600),
        Stimulus3delay( 0, 1600, 800),
        Stimulus3delay( 1600, 0, 800),
        Stimulus3delay( 1600, 800, 0),
        Stimulus3delay( 1600, 0, 800),
        Stimulus3delay( 0, 1600, 800),
        Stimulus3delay( 800, 1600, 0),
        Stimulus3delay( 0, 800, 1600),
        Stimulus3delay( 800, 0, 1600),

        Stimulus3delay( 1200, 2400, 0),
        Stimulus3delay( 1200, 0, 2400),
        Stimulus3delay( 2400, 1200, 0),
        Stimulus3delay( 0, 1200, 2400),
        Stimulus3delay( 0, 2400, 1200),
        Stimulus3delay( 2400, 0, 1200),
        Stimulus3delay( 2400, 1200, 0),
        Stimulus3delay( 2400, 0, 1200),
        Stimulus3delay( 0, 2400, 1200),
        Stimulus3delay( 1200, 2400, 0),
        Stimulus3delay( 0, 1200, 2400),
        Stimulus3delay( 1200, 0, 2400)

    )

    private val STIM_DURATION   = 1000L
    private val ISI             = 2000L

    private val EVENT_SECOND_TRAIN = 1201

    private val amplitude = 100


    private var allQuestions:MutableList<String> = mutableListOf()

    companion object {

        @JvmStatic val recipients:Array<String> = arrayOf(  "uvip.apptester@gmail.com") //, "monica.gori.parmiggiani@gmail.com") // "psysuite.uvip@gmail.com",

        @JvmStatic val TEST_BASIC_LABEL     = "ATVB"
        @JvmStatic val NUM_REPETITIONS      = 10
        @JvmStatic val NUM_REPETITIONS2     = 5

        fun getConditionsInfo(ctx: Context): List<TaskCode> {
            return mutableListOf(TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_single),  TEST_ATVB_TIME_SINGLESTIM),
                                 TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_single2), TEST_ATVB_TIME_SINGLESTIM2),
                                 TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_double), TEST_ATVB_TIME_DOUBLESTIM),
                                 TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_double2), TEST_ATVB_TIME_DOUBLESTIM2))
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
    // =============================================================================================================================

    init {

        nextTrailModality   = data.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        if (data.whitenoise)
            noise = MediaPlayer.create(ctx, ctx.resources.getIdentifier("wnoise_20s", "raw", ctx.packageName))

        tone1sec        = MediaPlayer.create(ctx, ctx.resources.getIdentifier("tone200hz_1sec", "raw", ctx.packageName))
        tone2sec        = MediaPlayer.create(ctx, ctx.resources.getIdentifier("tone200hz_2sec", "raw", ctx.packageName))


        allQuestions    = mutableListOf(ctx.resources.getString(R.string.atvb_question_synchro),
                                        ctx.resources.getString(R.string.atvb_question_equal))

        validAnswers    = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        initTest()
    }

    override fun initTest() {

        when (data.type) {
            TEST_ATVB_TIME_SINGLESTIM,
            TEST_ATVB_TIME_DOUBLESTIM   -> {
                curISI          = ISI           // 2000L
                curStimDuration = STIM_DURATION // 1000L
                createTrials_Time()
                createResultFile(data, TrialATVB.LOG_HEADER)
            }
            TEST_ATVB_TIME_SINGLESTIM2,
            TEST_ATVB_TIME_DOUBLESTIM2   -> {
                curISI          = ISI           // 2000L
                curStimDuration = STIM_DURATION // 1000L
                createTrials_Time2()
                createResultFile(data, TrialATVB2.LOG_HEADER)
            }
        }
        when (data.type) {
            TEST_ATVB_TIME_SINGLESTIM,
            TEST_ATVB_TIME_SINGLESTIM2   -> {
                mQuestion       = allQuestions[0]
            }
            TEST_ATVB_TIME_DOUBLESTIM,
            TEST_ATVB_TIME_DOUBLESTIM2   -> {
                createResultFile(data, TrialATVB2.LOG_HEADER)
                mQuestion       = allQuestions[1]
            }
        }
        // mTrials list
        currTone = tone2sec

        nTrials = mTrials.size
        currTrial = 0

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == data.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty())    showToast("Should not happen. given test code was not recognized", ctx)
    }

    // get new trial info. start noise. schedule stimulations
    override fun show(trial: TrialBasic, isRepeat: Boolean) {

        if (isRepeat) trial.repetitions++

        noise?.setVolume(0.5f, 0.5f)
        noise?.start()

        when(data.type) {

            TEST_ATVB_TIME_SINGLESTIM -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(trial.type, (mTrial as TrialATVB).delay, sendTrialEnd=true)
                }, 1000L)
            }
            TEST_ATVB_TIME_SINGLESTIM2 -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showThreeStimuliSingle((trial as TrialATVB2).a, (trial as TrialATVB2).t, (trial as TrialATVB2).v, sendTrialEnd=true)
                }, 1000L)
            }
            TEST_ATVB_TIME_DOUBLESTIM -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(STIM_TYPE_TIME_ATV, 0L, sendTrialEnd=false)
                }, 1000L)
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(trial.type, (trial as TrialATVB).delay, sendTrialEnd=true)
                }, (1000L + 2*curStimDuration))
            }
            TEST_ATVB_TIME_DOUBLESTIM2 -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(STIM_TYPE_TIME_ATV, 0L, sendTrialEnd=false)
                }, 1000L)
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showThreeStimuliSingle((trial as TrialATVB2).a, (trial as TrialATVB2).t, (trial as TrialATVB2).v, sendTrialEnd=true)
                }, (1000L + 2*curStimDuration))
            }
        }
    }

    override fun nextTrial(prev_result: String, elapsed: Int): Int {
        testEvent.accept(EVENT_UPDATE_TRIAL_ID)
        return super.nextTrial(prev_result, elapsed)
    }

    // called by secondTrain
    override fun onTrialEnd() {

        noise?.stop()
        noise?.prepare()

        when (nextTrailModality) {
            TEST_NEXTTRIAL_BUTTON               ->  testEvent.accept(EVENT_SHOW_NEXT_BUTTON)
            TEST_NEXTTRIAL_AUTO                 ->  testEvent.accept(EVENT_SHOW_1SECABORT)

            TEST_NEXTTRIAL_VOICE_ANSWER         ->  testEvent.accept(EVENT_GIVE_VOCAL_ANSWER)
            TEST_NEXTTRIAL_ANSWER               ->  testEvent.accept(EVENT_GIVE_ANSWER)
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER -> {
                                                    testEvent.accept(EVENT_GIVE_VOCAL_ANSWER)
                                                    testEvent.accept(EVENT_GIVE_ANSWER)
            }
        }
    }

    private fun deliverStimulus(type: Int) {

        when (type) {
            TYPE_AUDIO -> {
                currTone.start()
                mStimuliHandler.postDelayed({
                    currTone.stop()
                    currTone.prepare()
                }, curStimDuration)
            }
            TYPE_TACTILE -> vibrator?.vibrateSingle(curStimDuration)
            TYPE_VISUAL -> {
                mImageView.setImageResource(mBackgroundColours[1])
                mStimuliHandler.postDelayed({
                    mImageView.setImageResource(mBackgroundColours[0])
                }, curStimDuration)
            }
            TYPE_AUDIOVISUALTACTILE -> {
                currTone.start()
                vibrator?.vibrateSingle(curStimDuration)
                mImageView.setImageResource(mBackgroundColours[1])

                mStimuliHandler.postDelayed({
                    currTone.stop()
                    mImageView.setImageResource(mBackgroundColours[0])
                    vibrator?.cancel()
                    currTone.prepare()
                }, curStimDuration)
            }
            TYPE_VISUALTACTILE -> {
                vibrator?.vibrateSingle(curStimDuration)
                mImageView.setImageResource(mBackgroundColours[1])
                mStimuliHandler.postDelayed({
                    mImageView.setImageResource(mBackgroundColours[0])
                    vibrator?.cancel()
                }, curStimDuration)
            }
            TYPE_AUDIOTACTILE -> {
                currTone.start()
                vibrator?.vibrateSingle(curStimDuration)
                mStimuliHandler.postDelayed({
                    currTone.stop()
                    vibrator?.cancel()
                    currTone.prepare()
                }, curStimDuration)
            }
            TYPE_AUDIOVISUAL -> {
                currTone.start()
                mImageView.setImageResource(mBackgroundColours[1])
                mStimuliHandler.postDelayed({
                    currTone.stop()
                    mImageView.setImageResource(mBackgroundColours[0])
                    currTone.prepare()
                }, curStimDuration)
            }
        }
    }

    // class Trial(var id:Int=-1, val type:Int, val label:String, var audio_id:Int, var correct_answer:Int=-1, var user_answer:Int=-1,
    //                 var success:Boolean=false, var elapsed:Int=-1, var repetitions:Int=1)
    private fun createTrials_Time() {
        var cnt = -1
        mTrials = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {

            val trials: MutableList<TrialATVB> = mutableListOf()

            trials.add(TrialATVB(++cnt, STIM_TYPE_TIME_ATV,  0L,  validAnswers[0]))
            trials.add(TrialATVB(++cnt, STIM_TYPE_TIME_ATV,  0L,  validAnswers[0]))
            trials.add(TrialATVB(++cnt, STIM_TYPE_TIME_ATV,  0L,  validAnswers[0]))

            lStimuli.map {
                trials.add(TrialATVB(++cnt, it.type, it.delay,  validAnswers[1]))
            }
            trials.shuffle()
            mTrials.addAll(trials)
        }
        setTrialsID()   // set id according to their order
    }

    private fun createTrials_Time2() {
        var cnt = -1
        mTrials = mutableListOf()
        for (i in 0 until NUM_REPETITIONS2) {

            val trials: MutableList<TrialATVB2> = mutableListOf()

            trials.add(TrialATVB2(++cnt, 0L, 0L, 0L,  validAnswers[0]))
            trials.add(TrialATVB2(++cnt, 0L, 0L, 0L,  validAnswers[0]))
            trials.add(TrialATVB2(++cnt, 0L, 0L, 0L,  validAnswers[0]))
            trials.add(TrialATVB2(++cnt, 0L, 0L, 0L,  validAnswers[0]))
            trials.add(TrialATVB2(++cnt, 0L, 0L, 0L,  validAnswers[0]))
            trials.add(TrialATVB2(++cnt, 0L, 0L, 0L,  validAnswers[0]))

            lStimuli3delay.map {
                trials.add(TrialATVB2(++cnt, it.a, it.t, it.v,  validAnswers[1]))
            }
            trials.shuffle()
            mTrials.addAll(trials)
        }
        setTrialsID()   // set id according to their order
    }

    private fun showThreeStimuliSingle(a:Long, t:Long, v:Long, sendTrialEnd:Boolean=true) {

        val end:Long = max(listOf(a, t, v))

        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIO)
        }, a)
        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_TACTILE)
        }, t)
        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_VISUAL)
        }, v)
        mStimuliHandler.postDelayed({
            if(sendTrialEnd) onTrialEnd()
        }, (curStimDuration + end))
    }

    private fun showStimuliSingle(type: Int, delay: Long, sendTrialEnd:Boolean=true) {

        when (type) {
            STIM_TYPE_TIME_ATV -> {
                deliverStimulus(TYPE_AUDIOVISUALTACTILE)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration))
            }
            STIM_TYPE_TIME_A_TVx -> {
                deliverStimulus(TYPE_AUDIO)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_VISUALTACTILE)
                }, delay)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration + delay))
            }
            STIM_TYPE_TIME_Ax_TV -> {
                deliverStimulus(TYPE_VISUALTACTILE)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, delay)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration + delay))
            }
            STIM_TYPE_TIME_T_AVx -> {
                deliverStimulus(TYPE_AUDIO)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_VISUALTACTILE)
                }, delay)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration + delay))
            }
            STIM_TYPE_TIME_Tx_AV -> {
                deliverStimulus(TYPE_VISUALTACTILE)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, delay)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration + delay))
            }
            STIM_TYPE_TIME_V_ATx -> {
                deliverStimulus(TYPE_AUDIO)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_VISUALTACTILE)
                }, delay)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration + delay))
            }
            STIM_TYPE_TIME_Vx_AT -> {
                deliverStimulus(TYPE_VISUALTACTILE)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, delay)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration + delay))
            }
        }
    }
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
