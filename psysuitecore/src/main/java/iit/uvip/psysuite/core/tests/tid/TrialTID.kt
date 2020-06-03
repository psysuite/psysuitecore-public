package iit.uvip.psysuite.core.tests.tid

import iit.uvip.psysuite.core.common.TrialBasic


class TrialTID(id:Int=-1, val block:Int, val session:Int, type:Int, val modality:String, var delta1:Int, var delta2:Int, val ref_first:Int, val duration:Int): TrialBasic(id, type,""){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tblock\tsession\ttype\tmodality\trt\tuser_ans\tcor_ans\ttestinterv\tref_first\n"
    }

    init {
        correct_answer = when (delta2 > delta1) {
            true -> 1
            false -> 0
        }
    }

    // all class exported as string
    override fun toString():String{
        return "" //id.toString() + "\t" + type.toString() + "\t" + label + "\t" + conflict_type + "\t" + position.toString() + "\t" + duration.toString() + "\t" + success.toString() + "\t" + duration2.toString()+ "\n"
    }

    // data exported to log file
    override fun Log():String{
        return "" //id.toString() +  "\t" + label + "\t" + position.toString() + "\t" + conflict_type + "\t" + success.toString() + "\t" + correct_answer.toString() + "\t" + user_answer.toString() + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\n"
    }
}
