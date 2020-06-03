package iit.uvip.psysuite.core.tests.temporalbinding.atb

import iit.uvip.psysuite.core.common.TrialBasic


//                     trial_id    0-8      "none"
class TrialATB(id:Int=-1, type:Int): TrialBasic(id, type, ""){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\ttype\n"
    }

    init {}

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + type.toString() + "\n"
    }
}
