package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import iit.uvip.psysuite.core.common.TrialBasic


//                     trial_id    0-8      "none"
class TrialATVB2(id: Int = -1, val a:Long, val t:Long, val v:Long, correct_answer:String) : TrialBasic(id, 0, "", correct_answer) {

    companion object {
        @JvmStatic
        val LOG_HEADER = "id\ttype\taudio\ttactile\tvideo\tanswer\tsuccess\telapsed\n"
    }

    // all class exported as string
    override fun toString(): String {
        return id.toString() + "\t" + a.toString() + "\t" + t.toString() + "\t" + v.toString() + "\n"
    }

    // data exported to log file
    override fun Log(): String {
        return id.toString() + "\t" + a.toString() + "\t" + t.toString() + "\t" + v.toString() + "\t" + user_answer + "\t" + success.toString() + "\t" + elapsed.toString() +"\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, a=$a, t=$t, v=$v"
    }
}
