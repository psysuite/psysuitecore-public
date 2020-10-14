package iit.uvip.psysuite.core.tests.temporalbinding

import android.content.Context
import iit.uvip.psysuite.core.common.Summary
import iit.uvip.psysuite.core.common.TrialBasic
import kotlin.math.roundToInt


abstract class BindingsUnBalancedSummary(ctx:Context) : Summary(ctx){

    abstract var condition:BindingsSummaryCondition

    private var summary:String = ""

    // after each trial, filled (with response and success) trial is added to summary
    override fun add(trial: TrialBasic){
        condition.add(trial as TrialBindingsUnBalanced)
    }

    override fun close(filename:String, dir:String):String{
        condition.close()
        summary = condition.toString()
        return writeFile(summary, filename, dir)
    }
}

// type is one of those defined in ATVBUnBalancedSummary
abstract class BindingsSummaryCondition(){

    abstract var latencies:MutableList<BindingsSummaryRow>
    abstract fun add(trial: TrialBindingsUnBalanced)

    fun close(){
        latencies.map{
            it.close()
        }
    }

    override fun toString():String{
        var res = "type\tlat\tntr\t%yes\t%succ\trt\n"
        latencies.map{
            res += it.toString()
        }
        return res
    }
}


// type is one of those defined in TestATVB::mTrial
abstract class BindingsSummaryRow(val type:Int, val label:String, val latency:Int){

    protected var ntrial:Int              = 0
    protected var perc_discrimination:Int = 0
    protected var rt:Int                  = 0
    protected var perc_succ:Int           = 0

    abstract fun setPercDiscrim(trial: TrialBindingsUnBalanced):Int

    fun close(){

        if(ntrial > 0){
            rt                  = ((rt*1F)/ntrial).roundToInt()
            perc_succ           = (((perc_succ*1F)/ntrial)*100F).roundToInt()
            perc_discrimination = (((perc_discrimination*1F)/ntrial)*100F).roundToInt()
        }
    }

    fun add(trial: TrialBindingsUnBalanced){
        ntrial++
        rt += trial.elapsed
        if(trial.success)   perc_succ++

        perc_discrimination = setPercDiscrim(trial)
    }

    override fun toString():String{
        return "$label\t$latency\t$ntrial\t$perc_discrimination\t$perc_succ\t$rt\n"
    }
}