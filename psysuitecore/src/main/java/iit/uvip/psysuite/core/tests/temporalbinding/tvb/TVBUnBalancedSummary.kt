package iit.uvip.psysuite.core.tests.temporalbinding.tvb

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_TV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.unbalSD
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.trials.TrialBasic


class TVBUnBalancedSummary(ctx:Context) : Summary(ctx){

    override var conditions:List<SummaryCondition> = listOf(TVBsummaryCondition())

    // type is one of those defined in ATVBUnBalancedSummary
    inner class TVBsummaryCondition: BindingsSummaryCondition(){

        override var rows:List<SummaryRow> = listOf(
            TVBsummaryRow(TYPE_T,  "T","0"),
            TVBsummaryRow(TYPE_T_V,"T_V",unbalSD[6].second),
            TVBsummaryRow(TYPE_T_V,"T_V",unbalSD[5].second),
            TVBsummaryRow(TYPE_T_V,"T_V",unbalSD[4].second),
            TVBsummaryRow(TYPE_T_V,"T_V",unbalSD[3].second),
            TVBsummaryRow(TYPE_T_V,"T_V",unbalSD[2].second),
            TVBsummaryRow(TYPE_T_V,"T_V",unbalSD[1].second),
            TVBsummaryRow(TYPE_T_V,"T_V",unbalSD[0].second),
            TVBsummaryRow(TYPE_TV, "TV", "0"),
            TVBsummaryRow(TYPE_V_T,"V_T",unbalSD[0].second),
            TVBsummaryRow(TYPE_V_T,"V_T",unbalSD[1].second),
            TVBsummaryRow(TYPE_V_T,"V_T",unbalSD[2].second),
            TVBsummaryRow(TYPE_V_T,"V_T",unbalSD[3].second),
            TVBsummaryRow(TYPE_V_T,"V_T",unbalSD[4].second),
            TVBsummaryRow(TYPE_V_T,"V_T",unbalSD[5].second),
            TVBsummaryRow(TYPE_V_T,"V_T",unbalSD[6].second),
            TVBsummaryRow(TYPE_V,  "V","0")
        )

        override fun add(trial: TrialBasic){

            when(trial.type){
                TYPE_T          ->   rows[0].add(trial)
                TYPE_T_V        -> {
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
                TYPE_TV         -> rows[8].add(trial)
                TYPE_V_T        -> {
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
                TYPE_V          ->  rows[16].add(trial)
            }
        }
    }

    // type is one of those defined in TestATVB::mTrial
    inner class TVBsummaryRow(type:Int, label:String, latency:String): BindingsSummaryRow(type, label, latency){

        override fun setPercDiscrim(trial: TrialBasic):Int{
            return  if((trial.type == TYPE_TV && trial.success) || (trial.type != TYPE_TV && !trial.success))
                            perc_discrimination + 1
                    else    perc_discrimination
        }
    }
}
