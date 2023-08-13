package iit.uvip.psysuite.core.tests.fgi

import iit.uvip.psysuite.core.trials.TrialBasic

//                trial_id    0/1      fig_res
class TrialFGI(id: Int = -1, type: Int, label: String, var audio_name:String) :
    TrialBasic(id, type, label) {

    companion object {
        @JvmStatic val LOG_HEADER           = "id\ttype\tfig\taudio\n"
    }

    // all class exported as string
    override fun toString():String{
        return "$id\t$type\t$label\t$audio_name\tn.a.\n"
    }

    // only to validate Class
    override fun Log():String{
        return "$id\t$type\t$label\t$audio_name\n"
    }

    // data exported to log file
    fun Log(res:Int):String{
        return "$id\t$type\t$label\t$audio_name\t$res\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, audio_name=$audio_name"
    }
}
