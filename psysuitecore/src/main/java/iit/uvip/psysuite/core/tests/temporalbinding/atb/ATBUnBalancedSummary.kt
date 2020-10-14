package iit.uvip.psysuite.core.tests.temporalbinding.atb

import android.content.Context
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsUnBalancedSummary
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_A
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_AT
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_A_T
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_T
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_T_A


class ATBUnBalancedSummary(ctx:Context) : BindingsUnBalancedSummary(ctx){
    override var condition:BindingsSummaryCondition = ATBsummaryCondition()
}

// type is one of those defined in ATVBUnBalancedSummary
class ATBsummaryCondition():BindingsSummaryCondition(){

    override var latencies:MutableList<BindingsSummaryRow> = mutableListOf(
        ATBsummaryRow(TYPE_A,  "A",0),
        ATBsummaryRow(TYPE_A_T,"A_T", 800),
        ATBsummaryRow(TYPE_A_T,"A_T",400),
        ATBsummaryRow(TYPE_A_T,"A_T",300),
        ATBsummaryRow(TYPE_A_T,"A_T",200),
        ATBsummaryRow(TYPE_A_T,"A_T",100),
        ATBsummaryRow(TYPE_AT, "AT",0),
        ATBsummaryRow(TYPE_T_A,"T_A",100),
        ATBsummaryRow(TYPE_T_A,"T_A",200),
        ATBsummaryRow(TYPE_T_A,"T_A",300),
        ATBsummaryRow(TYPE_T_A,"T_A",400),
        ATBsummaryRow(TYPE_T_A,"T_A",800),
        ATBsummaryRow(TYPE_T,  "T",0)
    )

    override fun add(trial: TrialBindingsUnBalanced){
        when(trial.type){
            TYPE_A          ->   latencies[0].add(trial)
            TYPE_A_T        -> {
                when(trial.delay){
                    800L    -> latencies[1].add(trial)
                    400L    -> latencies[2].add(trial)
                    300L    -> latencies[3].add(trial)
                    200L    -> latencies[4].add(trial)
                    100L    -> latencies[5].add(trial)
                }
            }
            TYPE_AT         ->  latencies[6].add(trial)
            TYPE_T_A        -> {
                when(trial.delay){
                    100L    -> latencies[7].add(trial)
                    200L    -> latencies[8].add(trial)
                    300L    -> latencies[9].add(trial)
                    400L    -> latencies[10].add(trial)
                    800L    -> latencies[11].add(trial)
                }
            }
            TYPE_T          ->  latencies[12].add(trial)
        }
    }
}

// type is one of those defined in TestATVB::mTrial
class ATBsummaryRow(type:Int, label:String, latency:Int):BindingsSummaryRow(type, label, latency){

    override fun setPercDiscrim(trial: TrialBindingsUnBalanced):Int{
        return  if((trial.type == TYPE_AT && trial.success) || (trial.type != TYPE_AT && !trial.success))
                        perc_discrimination + 1
                else    perc_discrimination
    }
}
