package iit.uvip.psysuite.core.tests.beads

import iit.uvip.psysuite.core.trials.TrialBasic

//                trial_id    0/1      fig_res
class TrialBeads(id: Int = -1, type: Int, label: String, var img_res:Int, var beads_types:List<Boolean>) :
    TrialBasic(id, type, label) {

    companion object {
        @JvmStatic val LOG_HEADER           = "id\ttype\tfig\n"
    }

    // all class exported as string
    override fun toString():String{
        return "$id\t$type\t$label\n"
    }

    // only to validate Class
    override fun Log():String{
        return "$id\t$type\t$label\n"
    }

    // data exported to log file
    fun Log(res:Int):String{
        return "$id\t$type\t$label\n"
    }
}
