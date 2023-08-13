package iit.uvip.psysuite.core.tests.temporalbinding

import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_T_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_V_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_A_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_V_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_A_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_T_A


class TrialBindingsBalanced(id:Int=-1, type:Int=0, override var magnitude:Float, private val correct_answers:List<String>, isADA:Boolean=false)
     :TrialBindings3latencies(id, type, 0L,0L,0L, isADA=isADA) {

    companion object {
        @JvmStatic
        val LOG_HEADER = "id\ttype\tdelay\tanswer\tsuccess\telapsed\n"
    }

    init {
        updateTrial(magnitude)
    }

    override fun updateTrial(newvalue: Float):Long{
        magnitude       = newvalue

        when(type){
            TYPE_T_A_V -> {
                t = 0
                a = stim_value
                v = stim_value*2
                correct_answer = 0
            }
            TYPE_V_A_T ->{
                v = 0
                a = stim_value
                t = stim_value*2
                correct_answer = 0
            }
            TYPE_A_T_V ->{
                a = 0
                t = stim_value
                v = stim_value*2
                correct_answer = 1
            }
            TYPE_V_T_A ->{
                v = 0
                t = stim_value
                a = stim_value*2
                correct_answer = 1
            }

            TYPE_A_V_T -> {
                a = 0
                v = stim_value
                t = stim_value*2
                correct_answer = 2
            }
            TYPE_T_V_A -> {
                t = 0
                v = stim_value
                a = stim_value*2
                correct_answer = 2
            }
        }
        return stim_value
    }

    // all class exported as string
    override fun toString():String {
        return "$id\t$type\t$label\t$stim_value\n"
    }

    // data exported to log file
    override fun Log():String {
        return "$id\t$type\t$stim_value\t${correct_answers[user_answer]}\t$success\t$elapsed\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, label=$label, delay=$stim_value"
    }
}
