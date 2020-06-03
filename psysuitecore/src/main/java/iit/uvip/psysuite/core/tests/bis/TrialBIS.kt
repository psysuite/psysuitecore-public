package iit.uvip.psysuite.core.tests.bis

import iit.uvip.psysuite.core.common.TrialBasic


class TrialBIS(id:Int=-1, type:Int, label:String, val position:Int, val conflict_type:String, val duration:Int, val duration2:Int=0): TrialBasic(id,type,label){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tlat\tconflict\tres\tcor_ans\tuser_ans\telapsed\trep\n"
        @JvmStatic val LAST_STIMULUS_DELAY  = 1000
    }

    init {
        correct_answer = when (position >= LAST_STIMULUS_DELAY / 2) {
            true -> 1
            false -> 0
        }
    }

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\t" + label + "\t" + conflict_type + "\t" + position.toString() + "\t" + duration.toString() + "\t" + success.toString() + "\t" + duration2.toString()+ "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + label + "\t" + position.toString() + "\t" + conflict_type + "\t" + success.toString() + "\t" + correct_answer.toString() + "\t" + user_answer.toString() + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\n"
    }
}
