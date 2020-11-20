package iit.uvip.psysuite.core.tests.temporalbinding.tvb

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.tests.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_T
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_TV
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_T_V
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_V
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB.Companion.TYPE_V_T


class TVBUnBalancedSummary(ctx:Context) : Summary(ctx){

    override var conditions:List<SummaryCondition> = listOf(TVBsummaryCondition())

    // type is one of those defined in ATVBUnBalancedSummary
    inner class TVBsummaryCondition: BindingsSummaryCondition(){

        override var rows:List<SummaryRow> = listOf(
            TVBsummaryRow(TYPE_T,  "T","0"),
            TVBsummaryRow(TYPE_T_V,"T_V", "1200"),
            TVBsummaryRow(TYPE_T_V,"T_V", "800"),
            TVBsummaryRow(TYPE_T_V,"T_V","400"),
            TVBsummaryRow(TYPE_T_V,"T_V","300"),
            TVBsummaryRow(TYPE_T_V,"T_V","200"),
            TVBsummaryRow(TYPE_T_V,"T_V","100"),
            TVBsummaryRow(TYPE_TV, "TV","0"),
            TVBsummaryRow(TYPE_V_T,"V_T","50"),
            TVBsummaryRow(TYPE_V_T,"V_T","100"),
            TVBsummaryRow(TYPE_V_T,"V_T","200"),
            TVBsummaryRow(TYPE_V_T,"V_T","300"),
            TVBsummaryRow(TYPE_V_T,"V_T","400"),
            TVBsummaryRow(TYPE_V_T,"V_T","800"),
            TVBsummaryRow(TYPE_V_T,"V_T","1200"),
            TVBsummaryRow(TYPE_V,  "V","0")
        )

        override fun add(trial: TrialBasic){

            when(trial.type){
                TYPE_T          ->   rows[0].add(trial)
                TYPE_T_V        -> {
                    when((trial as TrialBindingsUnBalanced).delay){
                        800L    -> rows[1].add(trial)
                        400L    -> rows[2].add(trial)
                        300L    -> rows[3].add(trial)
                        200L    -> rows[4].add(trial)
                        100L    -> rows[5].add(trial)
                    }
                }
                TYPE_TV         ->  rows[6].add(trial)
                TYPE_V_T        -> {
                    when((trial as TrialBindingsUnBalanced).delay){
                        100L    -> rows[7].add(trial)
                        200L    -> rows[8].add(trial)
                        300L    -> rows[9].add(trial)
                        400L    -> rows[10].add(trial)
                        800L    -> rows[11].add(trial)
                    }
                }
                TYPE_V          ->  rows[12].add(trial)
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
