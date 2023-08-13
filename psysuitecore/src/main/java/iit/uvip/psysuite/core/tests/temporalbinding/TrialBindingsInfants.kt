package iit.uvip.psysuite.core.tests.temporalbinding

import iit.uvip.psysuite.core.trials.TrialBasic


//                     trial_id    0-8      "none"
class TrialBindingsInfants(id:Int=-1, type:Int, val tactile_pattern:Int): TrialBasic(id, type){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\ttype\n"
    }

    init {}

    // all class exported as string
    override fun toString():String{
        return "$id\t$type\n"
    }

    // data exported to log file
    override fun Log():String{
        return "$id\t$type\n"
    }

}
