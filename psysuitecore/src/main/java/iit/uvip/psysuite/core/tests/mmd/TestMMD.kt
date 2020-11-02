package iit.uvip.psysuite.core.tests.mmd

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.SpinnerData
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import iit.uvip.psysuite.core.common.stimuli.AudioManager
import iit.uvip.psysuite.core.common.stimuli.StimuliManager
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import org.albaspazio.core.ui.showToast

// show -> onTrialEnd -> EVENT_GIVE_ANSWER

class TestMMD(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              data: SubjectBasicParcel
) : TestBasic(ctx, activity, hostfragment, data) {

    override var LOG_TAG: String = TestMMD::class.java.simpleName

    companion object {
        @JvmStatic val NUM_TRIALS = 18
        @JvmStatic val TEST_BASIC_LABEL = "MMD"

        fun getConditionsInfo(ctx: Context): List<SpinnerData> {
            return mutableListOf(SpinnerData(TEST_BASIC_LABEL, TEST_MUSICAL_METERS, TEST_BASIC_LABEL))
        }

        fun getNextTrialModes():List<List<Int>>{
            return listOf(listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
        }        
    }


    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){
        // set question & create mTrials list
        validAnswers = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))
        mQuestion = ctx.resources.getString(R.string.mmeters_question_text)

        if(!subjectparcel.isDebug)  createTrials()
        else                        createTrialsDebug()

        nTrials     = mTrials.size
        currTrial   = 0

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subjectparcel.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(subjectparcel, TrialMMD.LOG_HEADER)

        mStimuliManager = StimuliManager(AudioManager(STIM_TYPE_A1, -1,  duration = currStimulusDuration, handler = mStimuliHandler, ctx = ctx), null, null)
        testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrials(){
        for(i in 1 until (NUM_TRIALS +1) ){
            mTrials.add(TrialMMD(-1, 0, "same", validAnswers[0], i))
            mTrials.add(TrialMMD(-1, 1, "diff", validAnswers[1], i))
        }
        mTrials.shuffle()

        // set trial id according to its order in the list
        for(i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
    }

    private fun createTrialsDebug(){
        for(i in 1 until 10000 ){
            mTrials.add(TrialMMD(-1, 0, "same", validAnswers[0], i))
            mTrials.add(TrialMMD(-1, 1, "diff", validAnswers[1], i))
        }
        setTrialsID()   // set trial id according to its order in the list
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
            true -> "mmc" + (trial as TrialMMD).audio_id + "_same"
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