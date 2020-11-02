package iit.uvip.psysuite.core.tests.temporalbinding.tvb

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.*
import iit.uvip.psysuite.core.common.stimuli.*
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsInfants
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.ui.showToast
import kotlin.math.roundToInt


class TestTVB(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subjectparcel: SubjectBasicParcel,
              vibrator: VibrationManager?,
              mImageView: ImageView?
) : TestBasic(ctx, activity, hostfragment, subjectparcel, vibrator, mImageView)
{
    override var LOG_TAG:String = TestTVB::class.java.simpleName

    private var curISI: Long = 0L

    // stimuli combinations
    private val STIM_TYPE_TIME_T800_V   = 100
    private val STIM_TYPE_TIME_T_V800   = 101

    private val BIMODAL_CODE            = STIM_TYPE_T1V1

    private var allQuestions:MutableList<String> = mutableListOf()
    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.blue_circle)

    // 5   different trials
    private val lStimuli: List<StimulusATBInfants> = listOf(
        StimulusATBInfants(BIMODAL_CODE,0),
        StimulusATBInfants(STIM_TYPE_T1, 1),
        StimulusATBInfants(STIM_TYPE_V1, 2),
        StimulusATBInfants(STIM_TYPE_TIME_T_V800,  3),
        StimulusATBInfants(STIM_TYPE_TIME_T800_V,  4)
    )

    // 26 different elements
    private val lStimuliUnBalanced: List<StimulusBindingsUnbalanced> = listOf(

        StimulusBindingsUnbalanced( TYPE_T_V, 50),
        StimulusBindingsUnbalanced( TYPE_V_T, 50),
        StimulusBindingsUnbalanced( TYPE_T_V, 50),
        StimulusBindingsUnbalanced( TYPE_V_T, 50),

        StimulusBindingsUnbalanced( TYPE_T_V, 100),
        StimulusBindingsUnbalanced( TYPE_V_T, 100),
        StimulusBindingsUnbalanced( TYPE_T_V, 100),
        StimulusBindingsUnbalanced( TYPE_V_T, 100),

        StimulusBindingsUnbalanced( TYPE_T_V, 200),
        StimulusBindingsUnbalanced( TYPE_V_T, 200),
        StimulusBindingsUnbalanced( TYPE_T_V, 200),
        StimulusBindingsUnbalanced( TYPE_V_T, 200),

        StimulusBindingsUnbalanced( TYPE_T_V, 300),
        StimulusBindingsUnbalanced( TYPE_V_T, 300),
        StimulusBindingsUnbalanced( TYPE_T_V, 300),
        StimulusBindingsUnbalanced( TYPE_V_T, 300),

        StimulusBindingsUnbalanced( TYPE_T_V, 400),
        StimulusBindingsUnbalanced( TYPE_V_T, 400),
        StimulusBindingsUnbalanced( TYPE_T_V, 400),
        StimulusBindingsUnbalanced( TYPE_V_T, 400),

        StimulusBindingsUnbalanced( TYPE_T_V, 800),
        StimulusBindingsUnbalanced( TYPE_V_T, 800),
        StimulusBindingsUnbalanced( TYPE_T_V, 800),
        StimulusBindingsUnbalanced( TYPE_V_T, 800),

        StimulusBindingsUnbalanced( TYPE_T_V, 1200),
        StimulusBindingsUnbalanced( TYPE_V_T, 1200)
    )

    private val WN_FIRSTSTIM_INTERVAL   = 1000L
    private val STIM_DURATION_INF       = 1000L
    private val STIM_DURATION_TOD       = 200L
    private val STIM_DURATION           = 50L
    private val ISI                     = 1000L
    private val ISI_INF                 = 2000L // distance between stimuli onsets

    private val EVENT_SECOND_TRAIN      = 1201

    private val amplitude = 100

    private var vibration_trains_timings: MutableList<LongArray>    = mutableListOf()
    private var vibration_trains_amplitudes: MutableList<IntArray>  = mutableListOf()

    companion object {

        @JvmStatic val TEST_BASIC_LABEL         = "TVB"
        @JvmStatic val NUM_REPETITIONS_INFANTS  = 3
        @JvmStatic val NUM_REPETITIONS          = 5

        @JvmStatic val TYPE_TV     = 0
        @JvmStatic val TYPE_T      = 1
        @JvmStatic val TYPE_V      = 2
        @JvmStatic val TYPE_T_V    = 3
        @JvmStatic val TYPE_V_T    = 4

         @JvmStatic val recipients:Array<String> = arrayOf("psysuite.uvip@gmail.com")

        fun getConditionsInfo(ctx: Context): List<SpinnerData> {
            return mutableListOf(
                SpinnerData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_single)}" , TEST_TVB_TIME_SINGLESTIM          ,"${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atb_subtask_time_single_tag)}"),
                SpinnerData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_double)}" , TEST_TVB_TIME_DOUBLESTIM          ,"${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atb_subtask_time_double_tag)}"),
                SpinnerData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_single_tod)}" , TEST_TVB_TIME_SINGLESTIM_TOD  ,"${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atb_subtask_time_single_tod_tag)}"),
                SpinnerData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_double_tod)}" , TEST_TVB_TIME_DOUBLESTIM_TOD  ,"${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atb_subtask_time_double_tod_tag)}"),
                SpinnerData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.atb_subtask_time_infants)}", TEST_ATB_TIME_INF                 ,"${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.atb_subtask_time_infants_tag)}"))
        }

        fun getNextTrialModes():List<List<Int>> {
            return listOf(
                listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
                listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
                listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
                listOf(TEST_NEXTTRIAL_ANSWER), //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
                listOf(TEST_NEXTTRIAL_AUTO, TEST_NEXTTRIAL_BUTTON))
        }

        fun getEmailRecipients():Array<String> = recipients
    }

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest() {

        when {
            mImageView == null -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }
        nextTrailModality   = subjectparcel.nextTrailModality
        abortMode           = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        allQuestions        = mutableListOf(ctx.resources.getString(R.string.atvb_question_synchro), ctx.resources.getString(R.string.atvb_question_equal))
        validAnswers        = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))

        // set stim duration (presently the same in the two subtasks
        when (subjectparcel.type) {
            TEST_TVB_TIME_SINGLESTIM ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI           // 1000L
                currStimulusDuration    = STIM_DURATION // 50L
            }
            TEST_TVB_TIME_DOUBLESTIM ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI           // 1000L
                currStimulusDuration    = STIM_DURATION // 50L
            }
            TEST_TVB_TIME_SINGLESTIM_TOD ->{
                mQuestion               = allQuestions[0]
                curISI                  = ISI               // 1000L
                currStimulusDuration    = STIM_DURATION_TOD // 200L
            }
            TEST_TVB_TIME_DOUBLESTIM_TOD ->{
                mQuestion               = allQuestions[1]
                curISI                  = ISI               // 1000L
                currStimulusDuration    = STIM_DURATION_TOD // 200L
            }
            TEST_TVB_TIME_INF   -> {
                curISI                  = ISI_INF           // 2000L
                currStimulusDuration    = STIM_DURATION_INF // 1000L
            }
        }

        if(!subjectparcel.isDebug) {
            // create trials/summary
            when (subjectparcel.type) {
                TEST_TVB_TIME_DOUBLESTIM_TOD,
                TEST_TVB_TIME_DOUBLESTIM ->{
                    createTrialsTimeDouble()
                    createResultFile(subjectparcel, TrialBindingsUnBalanced.LOG_HEADER)
                    initSummary()

                }
                TEST_TVB_TIME_SINGLESTIM_TOD,
                TEST_TVB_TIME_SINGLESTIM       -> {
                    createTrialsTimeSingle()
                    createResultFile(subjectparcel, TrialBindingsUnBalanced.LOG_HEADER)
                    initSummary()
                }
                TEST_TVB_TIME_INF   -> {
                    initTimeArrays()
                    createTrialsTimeInfants()
                    createResultFile(subjectparcel, TrialBindingsInfants.LOG_HEADER)
                }
            }
        }
        else{
            createResultFile(subjectparcel, TrialBindingsUnBalanced.LOG_HEADER)
            createTrialsDebug()
        }
        nTrials     = mTrials.size
        currTrial   = 0

        mListBlocks = mutableListOf((3*nTrials / 5F).roundToInt(), (4*nTrials / 5F).roundToInt())    // define two blocks, at the end of the first a window ask use whether continuing or ending (to be later continued)

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subjectparcel.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        if (subjectparcel.whitenoise > TEST_WNOISE_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        mStimuliManager = StimuliManager(null,
            TactileManager(vibrator!!, duration = currStimulusDuration, handler = mStimuliHandler),
            VisualManager(STIM_TYPE_V1, mImageView!!, mDrawablesResource[1], duration = currStimulusDuration, handler = mStimuliHandler))

        testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))
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

            val trials: MutableList<TrialBindingsInfants> = mutableListOf()

            trials.add(TrialBindingsInfants(++cnt, lStimuli[0].type, lStimuli[0].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[1].type, lStimuli[1].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[4].type, lStimuli[4].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[2].type, lStimuli[2].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[1].type, lStimuli[1].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[0].type, lStimuli[0].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[3].type, lStimuli[3].tactile_pattern))
            trials.add(TrialBindingsInfants(++cnt, lStimuli[2].type, lStimuli[2].tactile_pattern))

            mTrials.addAll(trials)
        }
    }

    // [(4x2) x 6lat + 4 + 4 + 4 + 4] = 64
    private fun createTrialsTimeDouble() {
        var cnt = -1
        mTrials = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {
            val trials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
            for (j in 0 until 2) {

                // 6
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0, validAnswers[0]))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0, validAnswers[0]))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0, validAnswers[1]))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T, 0, validAnswers[1]))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0, validAnswers[1]))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V, 0, validAnswers[1]))

                // 26
                lStimuliUnBalanced.map {
                    trials.add(TrialBindingsUnBalanced(++cnt, it.type, it.delay, validAnswers[1]))
                }
            }
            trials.shuffle()
            mTrials.addAll(trials)
        }
        setTrialsID()   // set id according to their order
    }

    // only-A & only-T were removed in single stimulus sub-task. 7/8/2020
    private fun createTrialsTimeSingle() {
        var cnt = -1
        mTrials = mutableListOf()
        for (i in 0 until NUM_REPETITIONS) {
            val trials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
            for (j in 0 until 2) {

                // 2
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0, validAnswers[0]))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0, validAnswers[0]))

                // 26
                lStimuliUnBalanced.map {
                    trials.add(TrialBindingsUnBalanced(++cnt, it.type, it.delay, validAnswers[1]))
                }
            }
            trials.shuffle()
            mTrials.addAll(trials)
        }
        setTrialsID()   // set id according to their order
    }

    private fun createTrialsDebug(){
        var cnt = -1
        mTrials = mutableListOf()
        for (i in 0 until 100000) {

            val trials: MutableList<TrialBindingsUnBalanced> = mutableListOf()
            for (j in 0 until 2) {
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_TV, 0, validAnswers[0]))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_T_V, 50, validAnswers[0]))
                trials.add(TrialBindingsUnBalanced(++cnt, TYPE_V_T, 50, validAnswers[0]))
            }
            mTrials.addAll(trials)
        }
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

        mNoise?.stop()
        mNoise?.prepare()

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

    override fun initSummary(){

        mSummary = when (subjectparcel.type) {
            TEST_TVB_TIME_DOUBLESTIM,
            TEST_TVB_TIME_SINGLESTIM,
            TEST_TVB_TIME_DOUBLESTIM_TOD,
            TEST_TVB_TIME_SINGLESTIM_TOD    ->  TVBUnBalancedSummary(ctx)

            else                            ->  TVBUnBalancedSummary(ctx)
        }
    }
    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        if(isRepeat)    trial.repetitions++

        mNoise?.start()

        when(subjectparcel.type) {

            TEST_TVB_TIME_INF -> {
                mStimuliHandler.postDelayed({
                    firstTrain((trial as TrialBindingsInfants).tactile_pattern)     // schedule first 3 stimuli
                    secondTrain(trial.type)    // schedule second 2 stimuli
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_TVB_TIME_SINGLESTIM,
            TEST_TVB_TIME_SINGLESTIM_TOD -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_STIMULI_START, null))
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced)
                }, WN_FIRSTSTIM_INTERVAL)
            }
            TEST_TVB_TIME_DOUBLESTIM,
            TEST_TVB_TIME_DOUBLESTIM_TOD -> {

                // since I have to apply the possible shift, I calculate here the correction and thus call deliverShiftedStimulus for the 1st stim.
                // for the second I call instead deliverUnBalancedStimuli
                val corr_delays = delaysAligner.arrangeDelays(BIMODAL_CODE, -1,0,0)
                val shift       = WN_FIRSTSTIM_INTERVAL - corr_delays.shift

                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_STIMULI_START, null))
                    deliverShiftedStimulus(BIMODAL_CODE, corr_delays.a, corr_delays.t, corr_delays.v) // simult
                }, shift)
                mStimuliHandler.postDelayed({
                    deliverUnBalancedStimuli(trial as TrialBindingsUnBalanced)
                }, shift + curISI)     // to preserve the desired ISI between 1st and 2nd stimuli,
                                                                                                    // I also add the shift that could be eventually imposed to the fastest modality
            }
        }
    }

    // tactile are programmed once, visual are programmed with postDelayed
    private fun firstTrain(tactile_pattern: Int) {

        // assuming vibro is faster than visual, I delay the former
        var V_delay     = delaysAligner.getStimuliDelay(BIMODAL_CODE).v - delaysAligner.getStimuliDelay(BIMODAL_CODE).t
        val timings = vibration_trains_timings[tactile_pattern]

        if(V_delay > 0) {
            vibration_trains_timings[tactile_pattern].mapIndexed { index, it ->
                timings[index] = it + V_delay
            }
        }
        else        // vibro delayed wrt visual: delay visual timings and preserve vibro onsets
            V_delay = 0


        vibrator?.vibratePattern(timings, vibration_trains_amplitudes[tactile_pattern])

        if(V_delay > 0L){
            mStimuliHandler.postDelayed({
                deliverUnimodalStimulus(STIM_TYPE_V1)
                testEvent.accept(Pair(EVENT_STIMULI_START, null))
            }, V_delay)
        }
        else {
            deliverUnimodalStimulus(STIM_TYPE_V1)
            testEvent.accept(Pair(EVENT_STIMULI_START, null))
        }

        mStimuliHandler.postDelayed({   deliverUnimodalStimulus(STIM_TYPE_V1)    }, curISI + V_delay)

        mStimuliHandler.postDelayed({   deliverUnimodalStimulus(STIM_TYPE_V1)    }, 2*curISI + V_delay)
    }

    // only for infants subtest
    // tactile have been already programmed at the beginning of the trial => just playback audio and take care of events
    private fun secondTrain(type:Int){

        // assuming audio is faster than vibro, I delay the former
        var A_delay    = delaysAligner.getStimuliDelay(BIMODAL_CODE).t - delaysAligner.getStimuliDelay(BIMODAL_CODE).a
        if(A_delay < 0L)   A_delay = 0L    // audio delayed wrt vibro: I previoulsy delayed vibro timings and now I preserve audio

        when(type){
            BIMODAL_CODE,
            STIM_TYPE_V1,
            STIM_TYPE_TIME_T_V800   -> {
                mStimuliHandler.postDelayed({
                    deliverUnimodalStimulus(STIM_TYPE_V1)
                    testEvent.accept(Pair(EVENT_SECOND_TRAIN, null))
                }, 3 * curISI + A_delay)
                mStimuliHandler.postDelayed({
                    deliverUnimodalStimulus(STIM_TYPE_V1)
                }, 4 * curISI + A_delay)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI + A_delay)
            }

            STIM_TYPE_T1 -> {
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_SECOND_TRAIN, null))
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 5 * curISI)
            }

            STIM_TYPE_TIME_T800_V -> {
                mStimuliHandler.postDelayed({
                    deliverUnimodalStimulus(STIM_TYPE_V1)
                    testEvent.accept(Pair(EVENT_SECOND_TRAIN, null))
                }, (3 * curISI + 800L + A_delay))
                mStimuliHandler.postDelayed({
                    deliverUnimodalStimulus(STIM_TYPE_V1)
                    testEvent.accept(Pair(EVENT_SECOND_TRAIN, null))
                }, (4 * curISI + 800 + A_delay))
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (5 * curISI + 800L + A_delay))
            }
        }
    }

    private fun deliverUnBalancedStimuli(trial:TrialBindingsUnBalanced){

        var type = 0
        val corr_delays:CorrectedStimuliDelay = when(trial.type) {
            TYPE_TV     -> {
                type = mStimuliManager.typeTV
                delaysAligner.arrangeDelays(type, -1,0, 0)
            }
            TYPE_T      -> {
                type = mStimuliManager.typeT
                CorrectedStimuliDelay(-1, 0, -1)
            }
            TYPE_V      -> {
                type = mStimuliManager.typeV
                CorrectedStimuliDelay(-1, -1, 0)
            }
            TYPE_T_V    -> {
                type = mStimuliManager.typeTV
                delaysAligner.arrangeDelays(type, -1, 0, trial.delay)
            }
            TYPE_V_T    -> {
                type = mStimuliManager.typeTV
                delaysAligner.arrangeDelays(type, -1, trial.delay,0)
            }
            else        -> {
                type = mStimuliManager.typeTV
                CorrectedStimuliDelay(-1, 0, 0)
            }
        }
        deliverShiftedStimulus(type, corr_delays.a, corr_delays.t, corr_delays.v){ onTrialEnd()}
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
