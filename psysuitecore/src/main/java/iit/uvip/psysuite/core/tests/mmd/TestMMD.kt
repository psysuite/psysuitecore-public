package iit.uvip.psysuite.core.tests.mmd

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.AudioManager
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.utility.ConditionData
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast

// show -> onTrialEnd -> EVENT_GIVE_ANSWER

class TestMMD(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              subject: SubjectBasicParcel,
              speechManager: SpeechManager?
) : TestBasic(ctx, activity, hostfragment, subject) {

    override var LOG_TAG: String = TestMMD::class.java.simpleName

    companion object {
        @JvmStatic val NUM_TRIALS = 18
        @JvmStatic val TEST_BASIC_LABEL = "MMD"

        fun getConditionsInfo(ctx: Context): List<ConditionData> = mutableListOf(ConditionData(TEST_BASIC_LABEL, TEST_MUSICAL_METERS, TEST_BASIC_LABEL, Populations.hearing_populations))

        fun getNextTrialModes(ctx:Context):List<List<Int>> =  listOf(listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
    }

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){
        // set question & create mTrials list
        validAnswers = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))
        mQuestion = ctx.resources.getString(R.string.mmeters_question_text)

        val trials = if(!subject.isDebug)  createTrials()
                     else                  createTrialsDebug()

        mTrialsManager = FixedTrialsManager(trials as MutableList<TrialBasic>)

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(subject, TrialMMD.LOG_HEADER)

        mStimuliManager = StimuliManager(
            AudioManager(StimuliManager.STIM_TYPE_A2, "",  duration = currStimulusDuration, ctx = ctx, handler = mStimuliHandler),
            null, null,
            delaysAligner, ctx, mStimuliHandler)
        testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrials():List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(i in 1 until (NUM_TRIALS +1) ){
            trials.add(TrialMMD(-1, 0, "same", 0, i))
            trials.add(TrialMMD(-1, 1, "diff", 1, i))
        }
        trials.shuffle()
        return trials
    }

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
    override fun onTrialEnd(){
        testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
    }

    override fun initSummary(){}
    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        if(isRepeat)    trial.repetitions++

        val resname = when(trial.type == 0){
            true  -> "mmc" + (trial as TrialMMD).audio_id + "_same"
            false -> "mmc" + (trial as TrialMMD).audio_id
        }
        try {
            AudioManager.playbackAllAudioResource(ctx, resname){  onTrialEnd()    }
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