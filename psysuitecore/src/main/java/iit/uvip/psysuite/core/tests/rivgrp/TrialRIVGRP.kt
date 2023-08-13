package iit.uvip.psysuite.core.tests.rivgrp

import iit.uvip.psysuite.core.trials.TrialBasic

//                trial_id    0/1      fig_res
class TrialRIVGRP(id: Int = -1, type: Int, label: String, var img_res:Int, var img_name:String, private var resp_type:String) :
    TrialBasic(id, type, label) {

    companion object {
        @JvmStatic val LOG_HEADER = "id\ttype\tfig\timg_name\n"
    }

    // all class exported as string
    override fun toString():String{
        return "$id\t$type\t$label\t$img_name\t$resp_type\n"
    }

    // only to validate Class
    override fun Log():String{
        return "$id\t$type\t$label\t$img_name\t$resp_type\n"
    }

    // data exported to log file
    fun Log(res:Int):String{
        return "$id\t$type\t$label\t$img_name\t$resp_type\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, img_name=$img_name, resp_type=$resp_type"
    }
}
