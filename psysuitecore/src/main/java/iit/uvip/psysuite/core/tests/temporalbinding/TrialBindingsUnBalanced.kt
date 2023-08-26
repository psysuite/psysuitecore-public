package iit.uvip.psysuite.core.tests.temporalbinding

import iit.uvip.psysuite.core.trials.TrialBasic


//                     trial_id    0-8      "none"
class TrialBindingsUnBalanced(id:Int=-1, type:Int=0, final override var magnitude:Float, isADA:Boolean=false):
    TrialBasic(id, type, "", isADA=isADA) {

    companion object {
        @JvmStatic val LOG_HEADER = "id\ttype\tdelay\tanswer\tsuccess\telapsed\n"
    }

    init {
        updateTrial(magnitude)
    }

    // all class exported as string
    override fun toString():String {
        return "$id\t$type\t$stim_value\n"
    }

    // data exported to log file
    override fun Log():String {
        return "$id\t$type\t$stim_value\t$user_answer\t$success\t$elapsed\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, delay=$stim_value"
    }

    override fun updateTrial(newvalue: Float): Long {
        magnitude       = newvalue
        correct_answer  =   if(magnitude == 0.0F)   0
                            else                    1
        return stim_value
    }
}
