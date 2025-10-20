package iit.uvip.psysuite.core.tests.mmd

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.AudioManager
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.utility.ConditionData
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast

// show -> onStimuliEnd -> EVENT_GIVE_ANSWER

/**
 * Manages the Musical Meter Discrimination (MMD) psychophysical test.
 * This test presents pairs of auditory stimuli, and the subject has to determine if they are the same or different.
 *
 * The test flow involves:
 * 1. Initialization ([initTest]): Sets up trials, stimuli manager, and result logging.
 * 2. Stimulus Presentation ([show]): Plays an auditory stimulus for the current trial.
 * 3. Trial End ([onStimuliEnd]): Triggers an event to prompt for the subject's answer after stimulus playback.
 *
 * @param ctx The Android [Context].
 * @param activity The hosting [Activity].
 * @param hostfragment The hosting [Fragment].
 * @param subject The [SubjectBasicParcel] containing subject-specific configuration for the test.
 * @param speechManager An optional [SpeechManager] for voice input capabilities (not actively used in this test).
 */
class TestMMD(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              speechManager: SpeechManager?
) : TestBasic(ctx, activity, hostfragment, subject) {

    /**
     * Log tag for this class, typically the class name.
     */
    override var LOG_TAG: String = TestMMD::class.java.simpleName

    /**
     * Companion object for [TestMMD] holding constants and static methods.
     */
    companion object {
        /**
         * Basic label for the MMD test.
         */
        @JvmStatic val TEST_BASIC_LABEL = "MMD"
        /**
         * Default number of unique stimuli pairs for the MMD test (before repetition or shuffling for 'same'/'diff').
         */
        @JvmStatic val NUM_TRIALS = 18

        /**
         * Provides information about the conditions available for this test.
         * Currently, it defines a single condition for MMD, associated with [TEST_MUSICAL_METERS]
         * and applicable to [Populations.hearing_populations].
         *
         * @param ctx The Android [Context].
         * @return A list of [ConditionData] objects describing the test conditions.
         */
        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(ConditionData(TEST_BASIC_LABEL, TEST_MUSICAL_METERS, TEST_BASIC_LABEL, Populations.hearing_populations))

        /**
         * Specifies the allowed modes for proceeding to the next trial.
         * For MMD, it's configured to wait for an answer dialog ([TestBasic.TEST_NEXTTRIAL_ANSWER]).
         *
         * @param ctx The Android [Context].
         * @return A list containing a list of integer constants representing next trial modes.
         */
        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
    }

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    /**
     * Initializes the MMD test environment.
     * This method sets up the valid answers, the question presented to the user, the list of trials
     * using a [FixedTrialsManager], the test label, the result file, and the [StimuliManager]
     * for audio playback.
     * Finally, it emits an [EVENT_TEST_SETUP_COMPLETED] event.
     */
    override fun initTest(){
        // set question & create mTrials list
        validAnswers = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))
        mQuestion = ctx.resources.getString(R.string.mmeters_question_text) // Note: Uses mmeters_question_text, consider MMD specific text

        val trials = if(!subject.isDebug)  createTrials()
                     else                  createTrialsDebug()

        mTrialsManager = FixedTrialsManager(trials as MutableList<TrialBasic>)

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(TrialMMD.LOG_HEADER)

        mStimuliManager = StimuliManager(
            AudioManager(StimuliManager.STIM_TYPE_A2, "",  duration = currStimulusDuration, ctx = ctx, handler = mStimuliHandler),
            null, null,
            subject.stimuliDelays, ctx, mStimuliHandler)
        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    /**
     * Creates a list of trials for the standard MMD test.
     * It generates [NUM_TRIALS] pairs of "same" and "different" trials, then shuffles them.
     *
     * @return A list of [TrialBasic] objects for the MMD test.
     */
    private fun createTrials():List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(i in 1 until (NUM_TRIALS +1) ){
            trials.add(TrialMMD(-1, 0, "same", 0, i))
            trials.add(TrialMMD(-1, 1, "diff", 1, i))
        }
        trials.shuffle()
        return trials
    }

    /**
     * Creates an extended list of trials for debugging the MMD test.
     * It generates a large number (10000) of "same" and "different" trial pairs.
     *
     * @return A list of [TrialBasic] objects for debugging the MMD test.
     */
    private fun createTrialsDebug():List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(i in 1 until 10000 ){
            trials.add(TrialMMD(-1, 0, "same", 0, i))
            trials.add(TrialMMD(-1, 1, "diff", 1, i))
        }
        return trials
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    /**
     * Initializes the summary data for the test. This method is currently empty for MMD.
     */
    override fun initSummary(){}
    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    /**
     * Presents the auditory stimulus for the given MMD trial.
     * It determines the audio resource name based on whether the trial is "same" or "different"
     * and the audio ID from the [TrialMMD] object.
     * After playback completion, [onStimuliEnd] is called.
     *
     * @param trial The current [TrialBasic] (expected to be [TrialMMD]) to be presented.
     * @param isRepeat Boolean indicating if this trial is a repetition.
     */
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        if(isRepeat)    trial.repetitions++

        val resname = when(trial.type == 0){ // type 0 for "same", 1 for "different"
            true  -> "mmc" + (trial as TrialMMD).audio_id + "_same"
            false -> "mmc" + (trial as TrialMMD).audio_id
        }
        try {
            AudioManager.playbackAllAudioResource(ctx, resname){  onStimuliEnd()    }
        }
        catch(e:Exception){
            e.printStackTrace()
        }
    }

    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================

    // =============================================================================================================================
}