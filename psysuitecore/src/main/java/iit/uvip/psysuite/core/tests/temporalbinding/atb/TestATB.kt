package iit.uvip.psysuite.core.tests.temporalbinding.atb

import android.content.Context
import android.media.MediaPlayer
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.StimulusTypeDelay
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.showToast


class TestATB(ctx: Context,
              override val data: SubjectATBParcel,
              private val vibrator: VibrationManager?
) : TestBasic(ctx, data)
{
    var LOG_TAG:String = TestATB::class.java.simpleName

    private var noise: MediaPlayer? = null
    private var tone1sec: MediaPlayer
    private var tone2sec: MediaPlayer
    private lateinit var currTone: MediaPlayer

    private var curISI: Long = 0L
    private var curStimDuration: Long = 0L

    // stimuli types
    private val TYPE_AUDIO          = 0
    private val TYPE_TACTILE        = 1
    private val TYPE_AUDIOTACTILE   = 2

    // stimuli combinations
    private val STIM_TYPE_TIME_AT       = 0
    private val STIM_TYPE_TIME_A        = 1
    private val STIM_TYPE_TIME_T        = 2
    private val STIM_TYPE_TIME_A800_T   = 3
    private val STIM_TYPE_TIME_A_T800   = 4

    private val STIM_TYPE_TIME_A_Tx = 5
    private val STIM_TYPE_TIME_Ax_T = 6

    // 13 different trials
    private val lStimuli: List<StimulusTypeDelay> = listOf(
        StimulusTypeDelay(STIM_TYPE_TIME_AT, 0),
        StimulusTypeDelay(STIM_TYPE_TIME_A, 0),
        StimulusTypeDelay(STIM_TYPE_TIME_T, 0),

        StimulusTypeDelay(STIM_TYPE_TIME_A_Tx, 100),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_T, 100),

        StimulusTypeDelay(STIM_TYPE_TIME_A_Tx, 200),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_T, 200),

        StimulusTypeDelay(STIM_TYPE_TIME_A_Tx, 300),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_T, 300),

        StimulusTypeDelay(STIM_TYPE_TIME_A_Tx, 400),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_T, 400),

        StimulusTypeDelay(STIM_TYPE_TIME_A_Tx, 800),
        StimulusTypeDelay(STIM_TYPE_TIME_Ax_T, 800)
    )

    private val STIM_DURATION           = 1000L
    private val ISI                     = 2000L

    private val EVENT_SECOND_TRAIN      = 1201

    private val amplitude = 100

    private var vibration_trains_timings: MutableList<LongArray>    = mutableListOf()
    private var vibration_trains_amplitudes: MutableList<IntArray>  = mutableListOf()

    companion object {

        @JvmStatic val TEST_BASIC_LABEL         = "ATB"
        @JvmStatic val NUM_REPETITIONS_INFANTS  = 3

        @JvmStatic val recipients:Array<String> = arrayOf("uvip.apptester@gmail.com", "monica.gori.parmiggiani@gmail.com") // "psysuite.uvip@gmail.com",

        fun getConditionsInfo(ctx: Context): List<TaskCode> {
            return mutableListOf(
                TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.time)        , TEST_ATB_TIME),
                TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atv_subtask_time_infants), TEST_ATB_TIME_INF)
            )
        }

        fun getNextTrialModes():List<List<Int>> {
            return listOf(
                listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
                listOf(TEST_NEXTTRIAL_AUTO, TEST_NEXTTRIAL_BUTTON))
        }

        fun getEmailRecipients():Array<String>{
            return recipients
        }
    }
    // =============================================================================================================================

    init{

        nextTrailModality   = data.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        if (data.whitenoise) noise = MediaPlayer.create(ctx, ctx.resources.getIdentifier("wnoise_20s", "raw", ctx.packageName))

        tone1sec = MediaPlayer.create(ctx, ctx.resources.getIdentifier("tone200hz_1sec", "raw", ctx.packageName))
        tone2sec = MediaPlayer.create(ctx, ctx.resources.getIdentifier("tone200hz_2sec", "raw", ctx.packageName))

        mQuestion       = ctx.resources.getString(R.string.atvb_question_equal)
        validAnswers    = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        initTest()
    }

    //              _   _   _   _   _
    // 9 segments  | |_| |_| |_| |_| |
    private fun initTimeArrays() {

        // init here for readability. will manage amplitudes changes
        vibration_trains_amplitudes = mutableListOf(
            intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
            intArrayOf(amplitude, 0, amplitude, 0, amplitude),
            intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
            intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
            intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude, 0, amplitude)
        )
        vibration_trains_timings = mutableListOf(
            longArrayOf(curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration),
            longArrayOf(curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration),
            longArrayOf(curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration),
            longArrayOf(curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration),
            longArrayOf(curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration, curStimDuration + 800L, curStimDuration, curStimDuration + 800L))
    }

    override fun initTest() {

        // set stim duration (presently the same in the two subtasks
        when (data.type) {
            TEST_ATB_TIME,
            TEST_ATB_TIME_INF   -> {
                curISI          = ISI           // 2000L
                curStimDuration = STIM_DURATION // 1000L
            }
        }
        when (data.type) {
            TEST_ATB_TIME_INF       -> initTimeArrays()
        }

        // create trials
        when (data.type) {
            TEST_ATB_TIME       -> {
                createTrials_Time()
                createResultFile(data, TrialATB.LOG_HEADER)
            }
            TEST_ATB_TIME_INF   -> {
                createTrials_Time_Infants()
                createResultFile(data, TrialATBInfants.LOG_HEADER)
            }
        }

        currTone = tone2sec

        nTrials     = mTrials.size
        currTrial   = 0

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == data.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty())    showToast("Should not happen. given test code was not recognized", ctx)
    }

    override fun show(trial: TrialBasic, isRepeat:Boolean){

        if(isRepeat)    trial.repetitions++

        noise?.setVolume(0.4f, 0.4f)
        noise?.start()

        when(data.type) {

            TEST_ATB_TIME_INF -> {
                mStimuliHandler.postDelayed({
                    firstTrain(trial.type)     // schedule first 3 stimuli
                    secondTrain(trial.type)    // schedule second 2 stimuli
                }, 1000L)
            }
            TEST_ATB_TIME -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(STIM_TYPE_TIME_AT, 0L, sendTrialEnd=false)
                }, 1000L)
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_STIMULI_START)
                    showStimuliSingle(trial.type, (trial as TrialATB).delay, sendTrialEnd=true)
                }, (1000L + 2*curStimDuration))
            }
        }


    }

    override fun nextTrial(prev_result: String, elapsed: Int): Int {
        testEvent.accept(EVENT_UPDATE_TRIAL_ID)
        return super.nextTrial(prev_result, elapsed)
    }

    // called by secondTrain
    override fun onTrialEnd(){

        noise?.stop()
        noise?.prepare()

        when (nextTrailModality) {
            TEST_NEXTTRIAL_BUTTON ->        testEvent.accept(EVENT_SHOW_NEXT_BUTTON)

            TEST_NEXTTRIAL_AUTO -> {
                                            // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
                                            mStimuliHandler.postDelayed({
                                                testEvent.accept(EVENT_SHOW_1SECABORT)
                                            }, curStimDuration)
            }

            TEST_NEXTTRIAL_VOICE_ANSWER ->  testEvent.accept(EVENT_GIVE_VOCAL_ANSWER)
            TEST_NEXTTRIAL_ANSWER       ->  testEvent.accept(EVENT_GIVE_ANSWER)
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER -> {
                                            testEvent.accept(EVENT_GIVE_VOCAL_ANSWER)
                                            testEvent.accept(EVENT_GIVE_ANSWER)
            }
        }
    }

    private fun firstTrain(type: Int) {

        vibrator?.vibratePattern(vibration_trains_timings[type], vibration_trains_amplitudes[type])
        deliverStimulus(TYPE_AUDIO)
        testEvent.accept(EVENT_STIMULI_START)

        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIO)
        }, curISI)

        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIO)
        }, 2*curISI)
    }

    private fun deliverStimulus(type:Int){

        when(type) {
            TYPE_AUDIO -> {
                currTone.start()
                mStimuliHandler.postDelayed({
                    currTone.stop()
                    currTone.prepare()
                }, curStimDuration)
            }
            TYPE_TACTILE -> vibrator?.vibrateSingle(curStimDuration)
            TYPE_AUDIOTACTILE     -> {
                currTone.start()
                mStimuliHandler.postDelayed({
                    currTone.stop()
                    currTone.prepare()
                }, curStimDuration)
                vibrator?.vibrateSingle(curStimDuration)
            }
        }
    }

    private fun createTrials_Time_Infants() {
        var cnt = -1
        for (i in 0 until NUM_REPETITIONS_INFANTS) {

            val trials: MutableList<TrialATBInfants> = mutableListOf()

            trials.add(TrialATBInfants(++cnt, lStimuli[0].type))
            trials.add(TrialATBInfants(++cnt, lStimuli[1].type))
            trials.add(TrialATBInfants(++cnt, STIM_TYPE_TIME_A800_T))
            trials.add(TrialATBInfants(++cnt, lStimuli[2].type))
            trials.add(TrialATBInfants(++cnt, lStimuli[1].type))
            trials.add(TrialATBInfants(++cnt, lStimuli[0].type))
            trials.add(TrialATBInfants(++cnt, STIM_TYPE_TIME_A_T800))
            trials.add(TrialATBInfants(++cnt, lStimuli[2].type))

            mTrials.addAll(trials)
        }
    }

    private fun createTrials_Time() {
        var cnt = -1

        val trials: MutableList<TrialATB> = mutableListOf()

        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[6].type, lStimuli[6].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[3].type, lStimuli[3].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[7].type, lStimuli[7].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[12].type, lStimuli[12].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[5].type, lStimuli[5].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[4].type, lStimuli[4].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[8].type, lStimuli[8].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[11].type, lStimuli[11].delay, validAnswers[1]))

        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[4].type, lStimuli[4].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[5].type, lStimuli[5].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[8].type, lStimuli[8].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[11].type, lStimuli[11].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[7].type, lStimuli[7].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[6].type, lStimuli[6].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[3].type, lStimuli[3].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[12].type, lStimuli[12].delay, validAnswers[1]))

        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[12].type, lStimuli[12].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[3].type, lStimuli[3].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[5].type, lStimuli[5].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[7].type, lStimuli[7].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[6].type, lStimuli[6].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[4].type, lStimuli[4].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[8].type, lStimuli[8].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[11].type, lStimuli[11].delay, validAnswers[1]))

        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[6].type, lStimuli[6].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[3].type, lStimuli[3].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[7].type, lStimuli[7].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[12].type, lStimuli[12].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[5].type, lStimuli[5].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[4].type, lStimuli[4].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[8].type, lStimuli[8].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[11].type, lStimuli[11].delay, validAnswers[1]))

        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[4].type, lStimuli[4].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[5].type, lStimuli[5].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[8].type, lStimuli[8].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[11].type, lStimuli[11].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[0].type, lStimuli[0].delay, validAnswers[0]))
        trials.add(TrialATB(++cnt, lStimuli[7].type, lStimuli[7].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[1].type, lStimuli[1].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[6].type, lStimuli[6].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[10].type, lStimuli[10].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[2].type, lStimuli[2].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[3].type, lStimuli[3].delay, validAnswers[1]))
        trials.add(TrialATB(++cnt, lStimuli[12].type, lStimuli[12].delay, validAnswers[1]))

        mTrials.addAll(trials)
    }

    // only for infants subtest
    private fun secondTrain(type:Int){

        when(type){
            STIM_TYPE_TIME_AT -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, 4 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI)
            }
            STIM_TYPE_TIME_A -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, 4 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI)
            }
            STIM_TYPE_TIME_T -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI)
            }

            STIM_TYPE_TIME_A_T800 -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 4 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (5 * curISI + 800))
            }
            STIM_TYPE_TIME_A800_T -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (3 * curISI + 800L))
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (4 * curISI + 800))
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (5 * curISI + 800L))
            }
        }
    }

    private fun showStimuliSingle(type: Int, delay: Long, sendTrialEnd:Boolean=true) {

        when (type) {
            STIM_TYPE_TIME_AT -> {
                deliverStimulus(TYPE_AUDIOTACTILE)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration))
            }
            STIM_TYPE_TIME_A -> {
                deliverStimulus(TYPE_AUDIO)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration))
            }
            STIM_TYPE_TIME_T -> {
                deliverStimulus(TYPE_TACTILE)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration))
            }
            STIM_TYPE_TIME_A_Tx -> {
                deliverStimulus(TYPE_AUDIO)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIOTACTILE)
                }, delay)
                mStimuliHandler.postDelayed({
                    if(sendTrialEnd) onTrialEnd()
                }, (curStimDuration + delay))
            }
            STIM_TYPE_TIME_Ax_T -> {
                deliverStimulus(TYPE_AUDIOTACTILE)
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
This App perform an Audio-Tactile Binding (ATB) test:

It has two versions: infant and children/adults


1) INFANT:

It has one single experimental condition composed by 24 trials (with fixed scheme!).
Each trial consists in a pair of stimulation modalities (audio and tactle) each composed by two consecutive trains of respectively 3 and 2 either audio and/or tactile stimuli (stim duration 1sec, isi=1sec). ITI=2sec.

single trial:
       1st train    2nd train
        ___   __    __  |  __    __
A    __|  |__|  |__|  |_|_|  |__|  |__
                        |
        __    __    __  |  __    __
T    __|  |__|  |__|  |_|_|  |__|  |__
                        |
                        |

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

2) CHILDREN / ADULTS

single trial:

        __  | __
A    __|  |_|_|  |__
            |
        __  |  __
T    __|  |_|_|  |__
            |

CODE    #REP    TYPE
0       10       A,T
3       10       A
6       10       T
7       5       A+100,T
8       5       A,T+100
7       5       A+200,T
8       5       A,T+200
7       5       A+300,T
8       5       A,T+300
7       5       A+400,T
8       5       A,T+400
7       5       A+800,T
8       5       A,T+800

Tot trials = 80

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

7       3       A+1200,T
8       3       A,T+1200


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
