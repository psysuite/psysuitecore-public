package iit.uvip.psysuite.core.tests.temporalbinding

import iit.uvip.psysuite.core.trials.TrialBasic


//                     trial_id    0-8      "none"
open class TrialBindings3latencies(id:Int=-1, type:Int=0, var a:Long=0L, var t:Long=0L, var v:Long=0L, isADA:Boolean=false):
    TrialBasic(id, type, "", isADA=isADA) {

    companion object {
        @JvmStatic
        val LOG_HEADER = "id\ttype\taudio\ttactile\tvideo\tanswer\tsuccess\telapsed\n"
    }

    // all class exported as string
    override fun toString(): String {
        return "$id\t$type\t$a\t$t\t$v\n"
    }

    // data exported to log file
    override fun Log(): String {
        return "$id\t$type\t$a\t$t\t$v\t$user_answer\t$success\t$elapsed\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, a=$a, t=$t, v=$v"
    }
}
