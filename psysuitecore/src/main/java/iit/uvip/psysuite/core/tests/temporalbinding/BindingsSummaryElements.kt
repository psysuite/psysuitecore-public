package iit.uvip.psysuite.core.tests.temporalbinding

import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.trials.TrialBasic
import kotlin.math.roundToInt


abstract class BindingsSummaryCondition(label:String=""):SummaryCondition(label, "delay"){

    override fun toString():String{

        var res = "type\t$sub_label\tntr\t%yes\t%succ\trt\n"

        rows.map{
            res += it.toString()
        }
        return res
    }
}


abstract class BindingsSummaryRow(type:Int, label:String, latency:String): SummaryRow(type, label, latency){

    protected var perc_discrimination:Int = 0

    abstract fun setPercDiscrim(trial: TrialBasic):Int

    override fun close(){
        super.close()
        if(ntrial > 0){
            perc_discrimination = (((perc_discrimination*1F)/ntrial)*100F).roundToInt()
        }
    }

    override fun add(trial: TrialBasic){
        super.add(trial)
        perc_discrimination = setPercDiscrim(trial)
    }

    override fun toString():String{

        return  if(sub_label.isEmpty()) "$label\t$ntrial\t$perc_discrimination\t$perc_succ\t$rt\n"
        else                            "$label\t$sub_label\t$ntrial\t$perc_discrimination\t$perc_succ\t$rt\n"
    }
}