package iit.uvip.psysuite.core.tests.mmd

import android.content.Context
import android.media.MediaPlayer
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel

class TestMMD(ctx: Context, override val data: SubjectBasicParcel) : TestBasic(ctx, data) {
    var LOG_TAG: String = TestMMD::class.java.simpleName

    companion object {
        @JvmStatic
        val NUM_TRIALS = 18
        @JvmStatic
        val TEST_BASIC_LABEL = "MMD"

        fun getConditionsInfo(ctx: Context): List<TaskCode> {
            return mutableListOf(TaskCode(TEST_BASIC_LABEL, TEST_MUSICAL_METERS))
        }
    }


    // =============================================================================================================================

    init{

        validAnswers = mutableListOf(
            ctx.resources.getString(R.string.mmeters_rb1_text),
            ctx.resources.getString(R.string.mmeters_rb3_text)
        )

        initTest()
    }

    override fun initTest(){
        // set question & create mTrials list
        mQuestion = ctx.resources.getString(R.string.mmeters_question_text)
        createTrials()

        nTrials     = mTrials.size
        currTrial   = 0

        createResultFile(data.label, TrialMMD.LOG_HEADER)
    }

    override fun show(trialid:Int, isRepeat:Boolean){
        mTrial = mTrials[trialid]

        if(isRepeat)    mTrial.repetitions++

        val resname = when(mTrial.type == 0){
            true -> "mmc" + (mTrial as TrialMMD).audio_id + "_same"
            false -> "mmc" + (mTrial as TrialMMD).audio_id
        }
        deliverStimulus(resname)
    }

    override fun onTrialEnd(){
        testEvent.accept(EVENT_GIVE_ANSWER)
    }

    private fun deliverStimulus(resname:String){

        val mediaPlayer = MediaPlayer.create(ctx, ctx.resources.getIdentifier(resname, "raw", ctx.packageName))
        mediaPlayer.setOnCompletionListener{
            testEvent.accept(EVENT_STIMULI_END)
        }
        mediaPlayer.start()
    }

    // class Trial(var id:Int=-1, val type:Int, val label:String, var audio_id:Int, var correct_answer:Int=-1, var user_answer:Int=-1,
    //                 var success:Boolean=false, var elapsed:Int=-1, var repetitions:Int=1)
    private fun createTrials()
    {
        for(i in 1 until (NUM_TRIALS +1) ){
            mTrials.add(TrialMMD(-1, 0, "same", i))
            mTrials.add(TrialMMD(-1, 1, "diff", i))
        }
        mTrials.shuffle()

        // set trial id according to its order in the list
        for(i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
    }
}