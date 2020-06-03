package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import iit.uvip.psysuite.core.common.TrialBasic


//                     trial_id    0-8      "none"
class TrialATVB(id: Int = -1, type: Int, val delay: Long) : TrialBasic(id, type, "") {

    companion object {
        @JvmStatic
        val LOG_HEADER = "id\ttype\n"
    }

    init {
    }

    // all class exported as string
    override fun toString(): String {
        return id.toString() + "\t" + type.toString() + "\t" + delay.toString() + "\n"
    }

    // data exported to log file
    override fun Log(): String {
        return id.toString() + "\t" + type.toString() + "\t" + delay.toString() + "\n"
    }
}
