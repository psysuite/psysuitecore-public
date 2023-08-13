package iit.uvip.psysuite.core.tests.temporalbinding.atb

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.unbalSD
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_AT
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_A


class ATBUnBalancedSummary(ctx:Context) : Summary(ctx){

    override var conditions:List<SummaryCondition> = listOf(ATBsummaryCondition())

    // type is one of those defined in ATVBUnBalancedSummary
    inner class ATBsummaryCondition: BindingsSummaryCondition(){

        override var rows:List<SummaryRow> = listOf(
            ATBsummaryRow(TYPE_A,  "A","0"),
            ATBsummaryRow(TYPE_A_T,"A_T",unbalSD[6].second),
            ATBsummaryRow(TYPE_A_T,"A_T",unbalSD[5].second),
            ATBsummaryRow(TYPE_A_T,"A_T",unbalSD[4].second),
            ATBsummaryRow(TYPE_A_T,"A_T",unbalSD[3].second),
            ATBsummaryRow(TYPE_A_T,"A_T",unbalSD[2].second),
            ATBsummaryRow(TYPE_A_T,"A_T",unbalSD[1].second),
            ATBsummaryRow(TYPE_A_T,"A_T",unbalSD[0].second),
            ATBsummaryRow(TYPE_AT, "AT","0"),
            ATBsummaryRow(TYPE_T_A,"T_A",unbalSD[0].second),
            ATBsummaryRow(TYPE_T_A,"T_A",unbalSD[1].second),
            ATBsummaryRow(TYPE_T_A,"T_A",unbalSD[2].second),
            ATBsummaryRow(TYPE_T_A,"T_A",unbalSD[3].second),
            ATBsummaryRow(TYPE_T_A,"T_A",unbalSD[4].second),
            ATBsummaryRow(TYPE_T_A,"T_A",unbalSD[5].second),
            ATBsummaryRow(TYPE_T_A,"T_A",unbalSD[6].second),
            ATBsummaryRow(TYPE_T,  "T","0")
        )

        override fun add(trial: TrialBasic){
            when(trial.type){
                TYPE_A          ->   rows[0].add(trial)
                TYPE_A_T        -> {
                    when((trial as TrialBindingsUnBalanced).magnitude){
                        unbalSD[6].first    -> rows[1].add(trial)
                        unbalSD[5].first    -> rows[2].add(trial)
                        unbalSD[4].first    -> rows[3].add(trial)
                        unbalSD[3].first    -> rows[4].add(trial)
                        unbalSD[2].first    -> rows[5].add(trial)
                        unbalSD[1].first    -> rows[6].add(trial)
                        unbalSD[0].first    -> rows[7].add(trial)
                    }
                }
                TYPE_AT         -> rows[8].add(trial)
                TYPE_T_A        -> {
                    when((trial as TrialBindingsUnBalanced).magnitude){
                        unbalSD[0].first    -> rows[9].add(trial)
                        unbalSD[1].first    -> rows[10].add(trial)
                        unbalSD[2].first    -> rows[11].add(trial)
                        unbalSD[3].first    -> rows[12].add(trial)
                        unbalSD[4].first    -> rows[13].add(trial)
                        unbalSD[5].first    -> rows[14].add(trial)
                        unbalSD[6].first    -> rows[15].add(trial)
                    }
                }
                TYPE_T          ->  rows[16].add(trial)
            }
        }
    }

    // type is one of those defined in TestATVB::mTrial
    inner class ATBsummaryRow(type:Int, label:String, latency:String): BindingsSummaryRow(type, label, latency){

        override fun setPercDiscrim(trial: TrialBasic):Int{
            return  if((trial.type == TYPE_AT && trial.success) || (trial.type != TYPE_AT && !trial.success))
                perc_discrimination + 1
            else    perc_discrimination
        }
    }
}

