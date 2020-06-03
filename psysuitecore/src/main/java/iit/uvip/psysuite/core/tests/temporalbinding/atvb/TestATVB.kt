package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import android.content.Context
import android.media.MediaPlayer
import android.widget.ImageView
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.tests.temporalbinding.atb.SubjectATBParcel
import org.albaspazio.core.accessory.VibrationManager

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
    private val lStimuli: List<Stimulus> = listOf(
        Stimulus(STIM_TYPE_TIME_ATV, 0),

        Stimulus(STIM_TYPE_TIME_A_TVx, 100),
        Stimulus(STIM_TYPE_TIME_Ax_TV, 100),
        Stimulus(STIM_TYPE_TIME_V_ATx, 100),
        Stimulus(STIM_TYPE_TIME_Vx_AT, 100),
        Stimulus(STIM_TYPE_TIME_T_AVx, 100),
        Stimulus(STIM_TYPE_TIME_Tx_AV, 100),

        Stimulus(STIM_TYPE_TIME_A_TVx, 200),
        Stimulus(STIM_TYPE_TIME_Ax_TV, 200),
        Stimulus(STIM_TYPE_TIME_V_ATx, 200),
        Stimulus(STIM_TYPE_TIME_Vx_AT, 200),
        Stimulus(STIM_TYPE_TIME_T_AVx, 200),
        Stimulus(STIM_TYPE_TIME_Tx_AV, 200),

        Stimulus(STIM_TYPE_TIME_A_TVx, 300),
        Stimulus(STIM_TYPE_TIME_Ax_TV, 300),
        Stimulus(STIM_TYPE_TIME_V_ATx, 300),
        Stimulus(STIM_TYPE_TIME_Vx_AT, 300),
        Stimulus(STIM_TYPE_TIME_T_AVx, 300),
        Stimulus(STIM_TYPE_TIME_Tx_AV, 300),

        Stimulus(STIM_TYPE_TIME_A_TVx, 400),
        Stimulus(STIM_TYPE_TIME_Ax_TV, 400),
        Stimulus(STIM_TYPE_TIME_V_ATx, 400),
        Stimulus(STIM_TYPE_TIME_Vx_AT, 400),
        Stimulus(STIM_TYPE_TIME_T_AVx, 400),
        Stimulus(STIM_TYPE_TIME_Tx_AV, 400),

        Stimulus(STIM_TYPE_TIME_A_TVx, 800),
        Stimulus(STIM_TYPE_TIME_Ax_TV, 800),
        Stimulus(STIM_TYPE_TIME_V_ATx, 800),
        Stimulus(STIM_TYPE_TIME_Vx_AT, 800),
        Stimulus(STIM_TYPE_TIME_T_AVx, 800),
        Stimulus(STIM_TYPE_TIME_Tx_AV, 800),

        Stimulus(STIM_TYPE_TIME_A_TVx, 1200),
        Stimulus(STIM_TYPE_TIME_Ax_TV, 1200),
        Stimulus(STIM_TYPE_TIME_V_ATx, 1200),
        Stimulus(STIM_TYPE_TIME_Vx_AT, 1200),
        Stimulus(STIM_TYPE_TIME_T_AVx, 1200),
        Stimulus(STIM_TYPE_TIME_Tx_AV, 1200)
    )


    private val STIM_DURATION = 1000L
    private val ISI = 2000L

    private val EVENT_SECOND_TRAIN = 1201

    private val amplitude = 100

    companion object {

        @JvmStatic val TEST_BASIC_LABEL = "ATVB"
        @JvmStatic val NUM_REPETITIONS = 10

        fun getConditionsInfo(ctx: Context): List<TaskCode> {
            return mutableListOf(TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_single), TEST_ATVB_TIME_SINGLESTIM),
                                 TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atvb_subtask_time_double), TEST_ATVB_TIME_DOUBLESTIM))
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

        mQuestion       = ctx.resources.getString(R.string.atvb_question)
        validAnswers    = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        initTest()
    }

    override fun initTest() {

        when (data.type) {
            TEST_ATVB_TIME_SINGLESTIM,
            TEST_ATVB_TIME_DOUBLESTIM   -> {
                curISI = ISI           // 2000L
                curStimDuration = STIM_DURATION // 1000L
                createTrials_Time()
            }
        }

        // mTrials list
        currTone = tone2sec

        nTrials = mTrials.size
        currTrial = 0

        createResultFile(data.label, TrialATVB.LOG_HEADER)
    }

    // get new trial info. start noise. schedule stimulations
    override fun show(trialid: Int, isRepeat: Boolean) {

        mTrial = mTrials[trialid]

        if (isRepeat) mTrial.repetitions++

        noise?.setVolume(0.5f, 0.5f)
        noise?.start()

        when(data.type) {

            TEST_ATVB_TIME_SINGLESTIM -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(mTrial.type, (mTrial as TrialATVB).delay, sendTrialEnd=true)
                }, 1000L)
            }
            TEST_ATVB_TIME_DOUBLESTIM -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(STIM_TYPE_TIME_ATV, 0L, sendTrialEnd=false)
                }, 1000L)
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(mTrial.type, (mTrial as TrialATVB).delay, sendTrialEnd=true)
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
            TEST_NEXTTRIAL_BUTTON       -> testEvent.accept(EVENT_SHOW_NEXT_BUTTON)
            TEST_NEXTTRIAL_ANSWER       -> testEvent.accept(EVENT_GIVE_ANSWER)
            TEST_NEXTTRIAL_AUTO         -> testEvent.accept(EVENT_SHOW_1SECABORT)
            TEST_NEXTTRIAL_VOICE_ANSWER -> testEvent.accept(EVENT_GIVE_VOCAL_ANSWER)
            EVENT_GIVE_VOCAL_NORMAL_ANSWER -> {
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
                mImageView.setImageResource(mBackgroundColours[0])
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
            trials.add(TrialATVB(++cnt, lStimuli[0].type, lStimuli[0].delay))
            trials.add(TrialATVB(++cnt, lStimuli[0].type, lStimuli[0].delay))
            trials.add(TrialATVB(++cnt, lStimuli[0].type, lStimuli[0].delay))

            trials.add(TrialATVB(++cnt, lStimuli[1].type, lStimuli[1].delay))
            trials.add(TrialATVB(++cnt, lStimuli[2].type, lStimuli[2].delay))
            trials.add(TrialATVB(++cnt, lStimuli[3].type, lStimuli[3].delay))
            trials.add(TrialATVB(++cnt, lStimuli[4].type, lStimuli[4].delay))
            trials.add(TrialATVB(++cnt, lStimuli[5].type, lStimuli[5].delay))
            trials.add(TrialATVB(++cnt, lStimuli[6].type, lStimuli[6].delay))

            trials.add(TrialATVB(++cnt, lStimuli[7].type, lStimuli[7].delay))
            trials.add(TrialATVB(++cnt, lStimuli[8].type, lStimuli[8].delay))
            trials.add(TrialATVB(++cnt, lStimuli[9].type, lStimuli[9].delay))
            trials.add(TrialATVB(++cnt, lStimuli[10].type, lStimuli[10].delay))
            trials.add(TrialATVB(++cnt, lStimuli[11].type, lStimuli[11].delay))
            trials.add(TrialATVB(++cnt, lStimuli[12].type, lStimuli[12].delay))

            trials.add(TrialATVB(++cnt, lStimuli[13].type, lStimuli[13].delay))
            trials.add(TrialATVB(++cnt, lStimuli[14].type, lStimuli[14].delay))
            trials.add(TrialATVB(++cnt, lStimuli[15].type, lStimuli[15].delay))
            trials.add(TrialATVB(++cnt, lStimuli[16].type, lStimuli[16].delay))
            trials.add(TrialATVB(++cnt, lStimuli[17].type, lStimuli[17].delay))
            trials.add(TrialATVB(++cnt, lStimuli[18].type, lStimuli[18].delay))

            trials.add(TrialATVB(++cnt, lStimuli[19].type, lStimuli[19].delay))
            trials.add(TrialATVB(++cnt, lStimuli[20].type, lStimuli[20].delay))
            trials.add(TrialATVB(++cnt, lStimuli[21].type, lStimuli[21].delay))
            trials.add(TrialATVB(++cnt, lStimuli[22].type, lStimuli[22].delay))
            trials.add(TrialATVB(++cnt, lStimuli[23].type, lStimuli[23].delay))
            trials.add(TrialATVB(++cnt, lStimuli[24].type, lStimuli[24].delay))

            trials.add(TrialATVB(++cnt, lStimuli[25].type, lStimuli[25].delay))
            trials.add(TrialATVB(++cnt, lStimuli[26].type, lStimuli[26].delay))
            trials.add(TrialATVB(++cnt, lStimuli[27].type, lStimuli[27].delay))
            trials.add(TrialATVB(++cnt, lStimuli[28].type, lStimuli[28].delay))
            trials.add(TrialATVB(++cnt, lStimuli[29].type, lStimuli[29].delay))
            trials.add(TrialATVB(++cnt, lStimuli[30].type, lStimuli[30].delay))

            trials.add(TrialATVB(++cnt, lStimuli[31].type, lStimuli[31].delay))
            trials.add(TrialATVB(++cnt, lStimuli[32].type, lStimuli[32].delay))
            trials.add(TrialATVB(++cnt, lStimuli[33].type, lStimuli[33].delay))
            trials.add(TrialATVB(++cnt, lStimuli[34].type, lStimuli[34].delay))
            trials.add(TrialATVB(++cnt, lStimuli[35].type, lStimuli[35].delay))
            trials.add(TrialATVB(++cnt, lStimuli[36].type, lStimuli[36].delay))

            trials.shuffle()
            mTrials.addAll(trials)
        }

        // set trial id according to its order in the list
        mTrials.mapIndexed { index, trialBasic ->
            trialBasic.id = index
        }
        for (i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
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

    data class Stimulus(val type: Int, val delay: Long) {}
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
