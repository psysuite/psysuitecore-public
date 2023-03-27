package iit.uvip.psysuite.core.tests.temporalbinding

import iit.uvip.psysuite.core.trials.TrialBasic


//                     trial_id    0-8      "none"
class TrialBindingsUnBalanced(id:Int=-1, type:Int=0, var delay:Long=0L, correct_answer:Int=-1):
    TrialBasic(id, type, "", correct_answer) {

    companion object {
        @JvmStatic
        val LOG_HEADER = "id\ttype\tdelay\tanswer\tsuccess\telapsed\n"
    }

    // all class exported as string
    override fun toString(): String {
        return id.toString() + "\t" + type.toString() + "\t" + delay.toString() + "\n"
    }

    // data exported to log file
    override fun Log(): String {
        return id.toString() + "\t" + type.toString() + "\t" + delay.toString() + "\t" + user_answer + "\t" + success.toString() + "\t" + elapsed.toString() +"\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, delay=$delay"
    }

    override fun updateTrial(newvalue:Float){
        delay = newvalue.toLong()
    }
}
