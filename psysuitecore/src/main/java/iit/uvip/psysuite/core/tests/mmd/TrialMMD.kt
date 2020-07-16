package iit.uvip.psysuite.core.tests.mmd

import iit.uvip.psysuite.core.common.TrialBasic

//                trial_id    0/1      same/diff        si/no                   1-18
class TrialMMD(id: Int = -1, type: Int, label: String, correct_answer:String, var audio_id: Int) :
    TrialBasic(id, type, label, correct_answer) {

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tres\tcor_ans\tuser_ans\telapsed\trep\taudio_id\n"
    }

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\t" + label + "\t" + success.toString() + "\t" + audio_id.toString()+ "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + label + "\t" + success.toString() + "\t" + correct_answer + "\t" + user_answer + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\t" + audio_id.toString() + "\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, id_audio=$audio_id"
    }
}
