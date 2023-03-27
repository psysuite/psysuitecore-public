package iit.uvip.psysuite.core.model.summary

import iit.uvip.psysuite.core.trials.TrialBasic

abstract class SummaryCondition(val label:String="", val sub_label:String = ""){

    abstract var rows:List<SummaryRow>
    abstract fun add(trial: TrialBasic)

    fun close(){
        rows.map{
            it.close()
        }
    }

    override fun toString():String{

        var res =   if(sub_label.isEmpty()) "type\tntr\t%succ\trt\n"
                    else                    "type\t$sub_label\tntr\t%succ\trt\n"

        rows.map{
            res += it.toString()
        }
        return res
    }
}
