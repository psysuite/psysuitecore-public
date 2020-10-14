package iit.uvip.psysuite.core.tests.temporalbinding.avb

import android.content.Context
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsUnBalancedSummary
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_A
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_AV
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_A_V
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_V
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_V_A


class AVBUnBalancedSummary(ctx:Context) : BindingsUnBalancedSummary(ctx){
    override var condition:BindingsSummaryCondition = AVBsummaryCondition()
}

// type is one of those defined in AVBUnBalancedSummary
class AVBsummaryCondition():BindingsSummaryCondition(){

    override var latencies:MutableList<BindingsSummaryRow> = mutableListOf(
        AVBsummaryRow(TYPE_A,  "A",0),
        AVBsummaryRow(TYPE_A_V,"A_V", 800),
        AVBsummaryRow(TYPE_A_V,"A_V",400),
        AVBsummaryRow(TYPE_A_V,"A_V",300),
        AVBsummaryRow(TYPE_A_V,"A_V",200),
        AVBsummaryRow(TYPE_A_V,"A_V",100),
        AVBsummaryRow(TYPE_AV, "AV",0),
        AVBsummaryRow(TYPE_V_A,"V_A",100),
        AVBsummaryRow(TYPE_V_A,"V_A",200),
        AVBsummaryRow(TYPE_V_A,"V_A",300),
        AVBsummaryRow(TYPE_V_A,"V_A",400),
        AVBsummaryRow(TYPE_V_A,"V_A",800),
        AVBsummaryRow(TYPE_V,  "V",0))

    override fun add(trial: TrialBindingsUnBalanced){
        when(trial.type){
            TYPE_A          ->   latencies[0].add(trial)
            TYPE_A_V        -> {
                when(trial.delay){
                    800L    -> latencies[1].add(trial)
                    400L    -> latencies[2].add(trial)
                    300L    -> latencies[3].add(trial)
                    200L    -> latencies[4].add(trial)
                    100L    -> latencies[5].add(trial)
                }
            }
            TYPE_AV         -> latencies[6].add(trial)
            TYPE_V_A        -> {
                when(trial.delay){
                    100L    -> latencies[7].add(trial)
                    200L    -> latencies[8].add(trial)
                    300L    -> latencies[9].add(trial)
                    400L    -> latencies[10].add(trial)
                    800L    -> latencies[11].add(trial)
                }
            }
            TYPE_V          ->  latencies[12].add(trial)
        }
    }
}

// type is one of those defined in TestATVB::mTrial
class AVBsummaryRow(type:Int, label:String, latency:Int):BindingsSummaryRow(type, label, latency){

    override fun setPercDiscrim(trial: TrialBindingsUnBalanced):Int{
        return  if((trial.type == TYPE_AV && trial.success) || (trial.type != TYPE_AV && !trial.success))
                        perc_discrimination + 1
                else    perc_discrimination
    }
}