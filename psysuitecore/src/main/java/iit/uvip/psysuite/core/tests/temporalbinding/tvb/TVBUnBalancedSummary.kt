package iit.uvip.psysuite.core.tests.temporalbinding.tvb

import android.content.Context
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsUnBalancedSummary
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_T
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_TV
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_T_V
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_V
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_V_T


class TVBUnBalancedSummary(ctx:Context) : BindingsUnBalancedSummary(ctx){
    override var condition:BindingsSummaryCondition = ATBsummaryCondition()
}

// type is one of those defined in ATVBUnBalancedSummary
class ATBsummaryCondition():BindingsSummaryCondition(){

    override var latencies:MutableList<BindingsSummaryRow> = mutableListOf(
        TVBsummaryRow(TYPE_T,  "T",0),
        TVBsummaryRow(TYPE_T_V,"T_V", 800),
        TVBsummaryRow(TYPE_T_V,"T_V",400),
        TVBsummaryRow(TYPE_T_V,"T_V",300),
        TVBsummaryRow(TYPE_T_V,"T_V",200),
        TVBsummaryRow(TYPE_T_V,"T_V",100),
        TVBsummaryRow(TYPE_TV, "TV",0),
        TVBsummaryRow(TYPE_V_T,"V_T",100),
        TVBsummaryRow(TYPE_V_T,"V_T",200),
        TVBsummaryRow(TYPE_V_T,"V_T",300),
        TVBsummaryRow(TYPE_V_T,"V_T",400),
        TVBsummaryRow(TYPE_V_T,"V_T",800),
        TVBsummaryRow(TYPE_V,  "V",0)
    )

    override fun add(trial: TrialBindingsUnBalanced){

        when(trial.type){
            TYPE_T          ->   latencies[0].add(trial)
            TYPE_T_V        -> {
                when(trial.delay){
                    800L    -> latencies[1].add(trial)
                    400L    -> latencies[2].add(trial)
                    300L    -> latencies[3].add(trial)
                    200L    -> latencies[4].add(trial)
                    100L    -> latencies[5].add(trial)
                }
            }
            TYPE_TV         ->  latencies[6].add(trial)
            TYPE_V_T        -> {
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
class TVBsummaryRow(type:Int, label:String, latency:Int): BindingsSummaryRow(type, label, latency){

    override fun setPercDiscrim(trial: TrialBindingsUnBalanced):Int{
        return  if((trial.type == TYPE_TV && trial.success) || (trial.type != TYPE_TV && !trial.success))
                        perc_discrimination + 1
                else    perc_discrimination
    }
}
