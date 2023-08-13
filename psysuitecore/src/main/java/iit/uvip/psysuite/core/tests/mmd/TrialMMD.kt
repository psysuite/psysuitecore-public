package iit.uvip.psysuite.core.tests.mmd

import iit.uvip.psysuite.core.trials.TrialBasic

//                trial_id    0/1      same/diff        0/1                   1-18
class TrialMMD(id: Int = -1, type: Int, label: String, override var correct_answer:Int, var audio_id: Int) :
    TrialBasic(id, type, label) {

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tres\tcor_ans\tuser_ans\telapsed\trep\taudio_id\n"
    }

    // all class exported as string
    override fun toString():String{
        return "$id\t$type\t$label\t$success\t$audio_id\n"
    }

    // data exported to log file
    override fun Log():String{
        return "$id\t$label\t$success\t$correct_answer\t$user_answer\t$elapsed\t$repetitions\t$audio_id\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, id_audio=$audio_id"
    }
}
