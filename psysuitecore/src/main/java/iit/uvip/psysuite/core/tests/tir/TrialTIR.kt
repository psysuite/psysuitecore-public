package iit.uvip.psysuite.core.tests.tir

import iit.uvip.psysuite.core.trials.TrialBasic

/*
    correct_answer: time to press (in ms)
    user_answer:  is the error = (time pressed by the user - correct_answer)
    stim_value: the actual trial pace (main_isi +- magnitude)
    succ: if abs(curr error) < abs(prev error), then success is true
 */

class TrialTIR (id:Int=-1, type:Int, label:String,
                override var magnitude:Float,
                val isBefore:Boolean,
                isADA:Boolean=false): TrialBasic(id, type, label, isADA=false) {

    companion object {
        @JvmStatic val LOG_HEADER = "id\tlabel\tisi\tonset\terror\tsuccess\telapsed\tmagnitude\n"
    }
}