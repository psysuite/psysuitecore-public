package iit.uvip.psysuite.core.model.summary

import iit.uvip.psysuite.core.trials.TrialBasic
import kotlin.math.roundToInt

open class SummaryRow(val type:Int, val label:String, val sub_label:String = ""){

    protected var ntrial:Int              = 0
    protected var rt:Int                  = 0
    protected var perc_succ:Int           = 0

    open fun close(){

        if(ntrial > 0){
            rt                  = ((rt*1F)/ntrial).roundToInt()
            perc_succ           = (((perc_succ*1F)/ntrial)*100F).roundToInt()
        }
    }

    open fun add(trial: TrialBasic){
        ntrial++
        rt += trial.elapsed
        if(trial.success)   perc_succ++
    }

    override fun toString():String{
        return  if(sub_label.isEmpty())     "$label\t$ntrial\t$perc_succ\t$rt\n"
                else                        "$label\t$sub_label\t$ntrial\t$perc_succ\t$rt\n"
    }
}