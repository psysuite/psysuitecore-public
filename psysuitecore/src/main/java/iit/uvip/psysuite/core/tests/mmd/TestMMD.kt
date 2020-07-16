package iit.uvip.psysuite.core.tests.mmd

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import org.albaspazio.core.ui.showToast

// show -> onTrialEnd -> EVENT_GIVE_ANSWER

class TestMMD(ctx: Context,
              activity: Activity,
              hostfragment: Fragment,
              data: SubjectBasicParcel,
              isDebug:Boolean
) : TestBasic(ctx, activity, hostfragment, data, isDebug = isDebug) {

    var LOG_TAG: String = TestMMD::class.java.simpleName

    companion object {
        @JvmStatic val NUM_TRIALS = 18
        @JvmStatic val TEST_BASIC_LABEL = "MMD"

        fun getConditionsInfo(ctx: Context): List<TaskCode> {
            return mutableListOf(TaskCode(TEST_BASIC_LABEL, TEST_MUSICAL_METERS))
        }

        fun getNextTrialModes():List<List<Int>>{
            return listOf(listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
        }        
    }


    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    init{
        initTest()
    }

    override fun initTest(){
        // set question & create mTrials list
        validAnswers = mutableListOf(ctx.resources.getString(R.string.yes), ctx.resources.getString(R.string.no))
        mQuestion = ctx.resources.getString(R.string.mmeters_question_text)
        createTrials()

        nTrials     = mTrials.size
        currTrial   = 0

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == data.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(data, TrialMMD.LOG_HEADER)
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

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onTrialEnd(){
        testEvent.accept(Pair(EVENT_GIVE_ANSWER, null))
    }

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
            playbackAllAudioResource(resname){  onTrialEnd()    }
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