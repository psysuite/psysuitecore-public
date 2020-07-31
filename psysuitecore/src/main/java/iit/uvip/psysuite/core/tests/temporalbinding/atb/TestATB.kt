package iit.uvip.psysuite.core.tests.temporalbinding.atb

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.*
import iit.uvip.psysuite.core.tests.temporalbinding.SubjectBindingsParcel
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindings
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.ui.showToast


class TestATB(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              override val subjectparcel: SubjectBindingsParcel,
              vibrator: VibrationManager?,
              isDebug:Boolean
) : TestBasic(ctx, activity, hostfragment, subjectparcel, vibrator, isDebug = isDebug)
{
    var LOG_TAG:String = TestATB::class.java.simpleName

    private var noise: MediaPlayer? = null
    private var tone2sec:String     = "t200hz_2s"

    private var curISI: Long = 0L


    private val TYPE_AT     = 0
    private val TYPE_A      = 1
    private val TYPE_T      = 2
    private val TYPE_A_T    = 3
    private val TYPE_T_A    = 4
    
    // stimuli combinations
    private val STIM_TYPE_TIME_A800_T   = 100
    private val STIM_TYPE_TIME_A_T800   = 101

    private var allQuestions:MutableList<String> = mutableListOf()

    // 5   different trials
    private val lStimuli: List<StimulusATBInfants> = listOf(
        StimulusATBInfants(STIM_TYPE_A2T1,0),
        StimulusATBInfants(STIM_TYPE_A2, 1),
        StimulusATBInfants(STIM_TYPE_T1, 2),
        StimulusATBInfants(STIM_TYPE_TIME_A_T800,  3),
        StimulusATBInfants(STIM_TYPE_TIME_A800_T,  4)
    )

    // 13 different elements
    private val lStimuliUnBalanced: List<StimulusBindingsUnbalanced> = listOf(

        StimulusBindingsUnbalanced( TYPE_AT, 0),
        StimulusBindingsUnbalanced( TYPE_A, 0),
        StimulusBindingsUnbalanced( TYPE_T, 0),

        StimulusBindingsUnbalanced( TYPE_A_T, 100),
        StimulusBindingsUnbalanced( TYPE_T_A, 100),
        
        StimulusBindingsUnbalanced( TYPE_A_T, 200),
        StimulusBindingsUnbalanced( TYPE_T_A, 200),
        
        StimulusBindingsUnbalanced( TYPE_A_T, 300),
        StimulusBindingsUnbalanced( TYPE_T_A, 300),
        
        StimulusBindingsUnbalanced( TYPE_A_T, 400),
        StimulusBindingsUnbalanced( TYPE_T_A, 400),
        
        StimulusBindingsUnbalanced( TYPE_A_T, 800),
        StimulusBindingsUnbalanced( TYPE_T_A, 800)
    )    
//    private val lStimuli3delay: List<Stimulus3delay> = listOf(
//
//        Stimulus3delay( 0, 0, 0, -1),
//        Stimulus3delay( 0, 0, -1, -1),
//        Stimulus3delay( 0, -1, 0, -1),
//        
//        Stimulus3delay( 0, 0, 100, -1),
//        Stimulus3delay( 0, 100, 0, -1),
//        
//        Stimulus3delay( 0, 0, 200, -1),
//        Stimulus3delay( 0, 200, 0, -1),
//        
//        Stimulus3delay( 0, 0, 300, -1),
//        Stimulus3delay( 0, 300, 0, -1),
//        
//        Stimulus3delay( 0, 0, 400, -1),
//        Stimulus3delay( 0, 400, 0, -1),
//        
//        Stimulus3delay( 0, 0, 800, -1),
//        Stimulus3delay( 0, 800, 0, -1)
//    )    

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
                TaskCode("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atvb_subtask_time_single)}" , TEST_ATB_TIME_SINGLESTIM),
                TaskCode("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atvb_subtask_time_double)}" , TEST_ATB_TIME_DOUBLESTIM),
                TaskCode(TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.atv_subtask_time_infants), TEST_ATB_TIME_INF)
            )
        }

        fun getNextTrialModes():List<List<Int>> {
            return listOf(
                listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
                listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
                listOf(TEST_NEXTTRIAL_AUTO, TEST_NEXTTRIAL_BUTTON))
        }

        fun getEmailRecipients():Array<String> = recipients
    }

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    init{
        if(vibrator == null)    throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        else{
            initTest()
            mMediaPlayerManager = MediaPlayerManager(ctx, tone2sec, duration = currStimulusDuration, handler = mStimuliHandler)
            mTactileManager     = TactileManager(vibrator, duration = currStimulusDuration, handler = mStimuliHandler)
        }
    }

    // no need to redefine mAudioParams & mTactileParams. they use a same duration and amplited and currMPAudio does not change
    override fun initTest() {

        nextTrailModality   = subjectparcel.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        allQuestions    = mutableListOf(ctx.resources.getString(R.string.atvb_question_synchro), ctx.resources.getString(R.string.atvb_question_equal))
        validAnswers    = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        // set stim duration (presently the same in the two subtasks
        when (subjectparcel.type) {
            TEST_ATB_TIME_SINGLESTIM ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI           // 2000L
                currStimulusDuration    = STIM_DURATION // 1000L
            }
            TEST_ATB_TIME_DOUBLESTIM ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI           // 2000L
                currStimulusDuration    = STIM_DURATION // 1000L
            }
            TEST_ATB_TIME_INF   -> {
                curISI                  = ISI           // 2000L
                currStimulusDuration    = STIM_DURATION // 1000L
            }
        }
        when (subjectparcel.type) {
            TEST_ATB_TIME_INF       -> initTimeArrays()
        }

        // create trials
        when (subjectparcel.type) {
            TEST_ATB_TIME_SINGLESTIM,
            TEST_ATB_TIME_DOUBLESTIM       -> {
                createTrialsTime()
                createResultFile(subjectparcel, TrialBindings.LOG_HEADER)
            }
            TEST_ATB_TIME_INF   -> {
                createTrialsTimeInfants()
                createResultFile(subjectparcel, TrialATBInfants.LOG_HEADER)
            }
        }
        nTrials     = mTrials.size
        currTrial   = 0

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subjectparcel.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        if (subjectparcel.whitenoise)
            noise = MediaPlayerManager.getAudioResource(ctx,"wnoise_20s", 0.4f)

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
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration),
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration),
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration),
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration),
            longArrayOf(currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration, currStimulusDuration + 800L, currStimulusDuration, currStimulusDuration + 800L))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrialsTimeInfants() {
        var cnt = -1
        for (i in 0 until NUM_REPETITIONS_INFANTS) {

            val trials: MutableList<TrialATBInfants> = mutableListOf()

            trials.add(TrialATBInfants(++cnt, lStimuli[0].type, lStimuli[0].tactile_pattern))
            trials.add(TrialATBInfants(++cnt, lStimuli[1].type, lStimuli[1].tactile_pattern))
            trials.add(TrialATBInfants(++cnt, lStimuli[4].type, lStimuli[4].tactile_pattern))
            trials.add(TrialATBInfants(++cnt, lStimuli[2].type, lStimuli[2].tactile_pattern))
            trials.add(TrialATBInfants(++cnt, lStimuli[1].type, lStimuli[1].tactile_pattern))
            trials.add(TrialATBInfants(++cnt, lStimuli[0].type, lStimuli[0].tactile_pattern))
            trials.add(TrialATBInfants(++cnt, lStimuli[3].type, lStimuli[3].tactile_pattern))
            trials.add(TrialATBInfants(++cnt, lStimuli[2].type, lStimuli[2].tactile_pattern))

            mTrials.addAll(trials)
        }
    }

    private fun createTrialsTime() {
        var cnt = -1

        val trials: MutableList<TrialBindingsUnBalanced> = mutableListOf()

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[6].type, lStimuliUnBalanced[6].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[9].type, lStimuliUnBalanced[9].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[3].type, lStimuliUnBalanced[3].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[7].type, lStimuliUnBalanced[7].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[12].type, lStimuliUnBalanced[12].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[5].type, lStimuliUnBalanced[5].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[10].type, lStimuliUnBalanced[10].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[4].type, lStimuliUnBalanced[4].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[8].type, lStimuliUnBalanced[8].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[11].type, lStimuliUnBalanced[11].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[4].type, lStimuliUnBalanced[4].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[5].type, lStimuliUnBalanced[5].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[8].type, lStimuliUnBalanced[8].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[11].type, lStimuliUnBalanced[11].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[10].type, lStimuliUnBalanced[10].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[7].type, lStimuliUnBalanced[7].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[6].type, lStimuliUnBalanced[6].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[9].type, lStimuliUnBalanced[9].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[3].type, lStimuliUnBalanced[3].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[12].type, lStimuliUnBalanced[12].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[12].type, lStimuliUnBalanced[12].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[3].type, lStimuliUnBalanced[3].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[9].type, lStimuliUnBalanced[9].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[5].type, lStimuliUnBalanced[5].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[7].type, lStimuliUnBalanced[7].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[6].type, lStimuliUnBalanced[6].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[4].type, lStimuliUnBalanced[4].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[10].type, lStimuliUnBalanced[10].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[8].type, lStimuliUnBalanced[8].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[11].type, lStimuliUnBalanced[11].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[6].type, lStimuliUnBalanced[6].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[10].type, lStimuliUnBalanced[10].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[3].type, lStimuliUnBalanced[3].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[7].type, lStimuliUnBalanced[7].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[12].type, lStimuliUnBalanced[12].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[5].type, lStimuliUnBalanced[5].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[9].type, lStimuliUnBalanced[9].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[4].type, lStimuliUnBalanced[4].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[8].type, lStimuliUnBalanced[8].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[11].type, lStimuliUnBalanced[11].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[4].type, lStimuliUnBalanced[4].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[5].type, lStimuliUnBalanced[5].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[8].type, lStimuliUnBalanced[8].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[11].type, lStimuliUnBalanced[11].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[10].type, lStimuliUnBalanced[10].delay, validAnswers[1]))

        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[0].type, lStimuliUnBalanced[0].delay, validAnswers[0]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[7].type, lStimuliUnBalanced[7].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[9].type, lStimuliUnBalanced[9].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[6].type, lStimuliUnBalanced[6].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[1].type, lStimuliUnBalanced[1].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[2].type, lStimuliUnBalanced[2].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[3].type, lStimuliUnBalanced[3].delay, validAnswers[1]))
        trials.add(TrialBindingsUnBalanced(++cnt, lStimuliUnBalanced[12].type, lStimuliUnBalanced[12].delay, validAnswers[1]))
        mTrials.addAll(trials)
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun nextTrial(prev_result: String, elapsed: Int): Int {
        testEvent.accept(Pair(EVENT_UPDATE_TRIAL_ID, 0L))
        return super.nextTrial(prev_result, elapsed)
    }

    // called by secondTrain
    override fun onTrialEnd(){

        noise?.stop()
        noise?.prepare()

        when (nextTrailModality) {
            TEST_NEXTTRIAL_BUTTON       ->  testEvent.accept(Pair(EVENT_SHOW_NEXT_BUTTON, null))
            TEST_NEXTTRIAL_AUTO         ->  {
                // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_SHOW_ABORT, 1000L))
                }, currStimulusDuration)
            }

            TEST_NEXTTRIAL_VOICE_ANSWER ->  testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
            TEST_NEXTTRIAL_ANSWER       ->  testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER -> {
                testEvent.accept(Pair(EVENT_GIVE_VOCAL_ANSWER, null))
                testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
            }
        }
    }

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        if(isRepeat)    trial.repetitions++

        noise?.start()

        when(subjectparcel.type) {

            TEST_ATB_TIME_INF -> {
                mStimuliHandler.postDelayed({
                    firstTrain((trial as TrialATBInfants).tactile_pattern)     // schedule first 3 stimuli
                    secondTrain(trial.type)    // schedule second 2 stimuli
                }, 1000L)
            }
            TEST_ATB_TIME_SINGLESTIM -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_STIMULI_START, null))
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced)
                }, (1000L))
            }
            TEST_ATB_TIME_DOUBLESTIM -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_STIMULI_START, null))
                    deliverAlignedStimulus(STIM_TYPE_A2T1) // simult
                    // aneous
                }, 1000L)
                mStimuliHandler.postDelayed({
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced)
                }, (1000L + 2*currStimulusDuration))
            }
        }
    }

    // tactile are programmed once, audio are programmed with postDelayed
    private fun firstTrain(tactile_pattern: Int) {

        vibrator?.vibratePattern(vibration_trains_timings[tactile_pattern], vibration_trains_amplitudes[tactile_pattern])
        deliverAlignedStimulus(STIM_TYPE_A2)
        testEvent.accept(Pair(EVENT_STIMULI_START, null))

        mStimuliHandler.postDelayed({
            deliverAlignedStimulus(STIM_TYPE_A2)
        }, curISI)

        mStimuliHandler.postDelayed({
            deliverAlignedStimulus(STIM_TYPE_A2)
        }, 2*curISI)
    }

    // only for infants subtest
    // tactile have been already programmed at the beginning of the trial => just playback audio and take care of events
    private fun secondTrain(type:Int){

        when(type){
            STIM_TYPE_A2T1,
            STIM_TYPE_A2,
            STIM_TYPE_TIME_A_T800   -> {
                mStimuliHandler.postDelayed({
                    deliverAlignedStimulus(STIM_TYPE_A2)
                    testEvent.accept(Pair(EVENT_SECOND_TRAIN, null))
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    deliverAlignedStimulus(STIM_TYPE_A2)
                }, 4 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI)
            }

            STIM_TYPE_T1 -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_SECOND_TRAIN, null))
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI)
            }

            STIM_TYPE_TIME_A800_T -> {
                mStimuliHandler.postDelayed({
                    deliverAlignedStimulus(STIM_TYPE_A2)
                    testEvent.accept(Pair(EVENT_SECOND_TRAIN, null))
                }, (3 * curISI + 800L))
                mStimuliHandler.postDelayed({
                    deliverAlignedStimulus(STIM_TYPE_A2)
                    testEvent.accept(Pair(EVENT_SECOND_TRAIN, null))
                }, (4 * curISI + 800))
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (5 * curISI + 800L))
            }
        }
    }


    private fun deliverUnBalancedStimuli(trial:TrialBindingsUnBalanced){
        when(trial.type){
            TYPE_AT     ->  deliverShiftedStimulus(0, 0, -1, audiotype = STIM_TYPE_A2){ onTrialEnd()}
            TYPE_A      ->  deliverShiftedStimulus(0, -1, -1, audiotype = STIM_TYPE_A2){ onTrialEnd()}
            TYPE_T      ->  deliverShiftedStimulus(-1, 0, -1, audiotype = STIM_TYPE_A2){ onTrialEnd()}
            TYPE_A_T    ->  deliverShiftedStimulus(0, trial.delay, -1, audiotype = STIM_TYPE_A2){ onTrialEnd()}
            TYPE_T_A    ->  deliverShiftedStimulus(trial.delay, 0, -1, audiotype = STIM_TYPE_A2){ onTrialEnd()}
        }
    }
    // =============================================================================================================================
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
