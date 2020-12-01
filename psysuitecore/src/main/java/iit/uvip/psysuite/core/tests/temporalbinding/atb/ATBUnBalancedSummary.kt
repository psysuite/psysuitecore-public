package iit.uvip.psysuite.core.tests.temporalbinding.atb

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.tests.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_A
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_AT
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_A_T
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_T
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB.Companion.TYPE_T_A


class ATBUnBalancedSummary(ctx:Context) : Summary(ctx){

    override var conditions:List<SummaryCondition> = listOf(ATBsummaryCondition())

    // type is one of those defined in ATVBUnBalancedSummary
    inner class ATBsummaryCondition: BindingsSummaryCondition(){

        override var rows:List<SummaryRow> = listOf(
            ATBsummaryRow(TYPE_A,  "A","0"),
            ATBsummaryRow(TYPE_A_T,"A_T","1200"),
            ATBsummaryRow(TYPE_A_T,"A_T", "800"),
            ATBsummaryRow(TYPE_A_T,"A_T","400"),
            ATBsummaryRow(TYPE_A_T,"A_T","300"),
            ATBsummaryRow(TYPE_A_T,"A_T","200"),
            ATBsummaryRow(TYPE_A_T,"A_T","100"),
            ATBsummaryRow(TYPE_A_T,"A_T","50"),
            ATBsummaryRow(TYPE_AT, "AT","0"),
            ATBsummaryRow(TYPE_T_A,"T_A","50"),
            ATBsummaryRow(TYPE_T_A,"T_A","100"),
            ATBsummaryRow(TYPE_T_A,"T_A","200"),
            ATBsummaryRow(TYPE_T_A,"T_A","300"),
            ATBsummaryRow(TYPE_T_A,"T_A","400"),
            ATBsummaryRow(TYPE_T_A,"T_A","800"),
            ATBsummaryRow(TYPE_T_A,"T_A","1200"),
            ATBsummaryRow(TYPE_T,  "T","0")
        )

        override fun add(trial: TrialBasic){
            when(trial.type){
                TYPE_A          ->   rows[0].add(trial)
                TYPE_A_T        -> {
                    when((trial as TrialBindingsUnBalanced).delay){
                        1200L   -> rows[1].add(trial)
                        800L    -> rows[2].add(trial)
                        400L    -> rows[3].add(trial)
                        300L    -> rows[4].add(trial)
                        200L    -> rows[5].add(trial)
                        100L    -> rows[6].add(trial)
                        50L     -> rows[7].add(trial)
                    }
                }
                TYPE_AT         -> rows[8].add(trial)
                TYPE_T_A        -> {
                    when((trial as TrialBindingsUnBalanced).delay){
                        50L     -> rows[9].add(trial)
                        100L    -> rows[10].add(trial)
                        200L    -> rows[11].add(trial)
                        300L    -> rows[12].add(trial)
                        400L    -> rows[13].add(trial)
                        800L    -> rows[14].add(trial)
                        1200L   -> rows[15].add(trial)
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

