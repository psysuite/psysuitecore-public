package iit.uvip.psysuite.core.tests.temporalbinding.avb

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.tests.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_A
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_AV
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_A_V
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_V
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB.Companion.TYPE_V_A


class AVBUnBalancedSummary(ctx:Context) : Summary(ctx){

    override var conditions:List<SummaryCondition> = listOf(AVBsummaryCondition())

    // type is one of those defined in AVBUnBalancedSummary
    inner class AVBsummaryCondition: BindingsSummaryCondition(){

        override var rows:List<SummaryRow> = listOf(
            AVBsummaryRow(TYPE_A,  "A","0"),
            AVBsummaryRow(TYPE_A_V,"A_V", "1200"),
            AVBsummaryRow(TYPE_A_V,"A_V", "800"),
            AVBsummaryRow(TYPE_A_V,"A_V","400"),
            AVBsummaryRow(TYPE_A_V,"A_V","300"),
            AVBsummaryRow(TYPE_A_V,"A_V","200"),
            AVBsummaryRow(TYPE_A_V,"A_V","100"),
            AVBsummaryRow(TYPE_A_V,"A_V","50"),
            AVBsummaryRow(TYPE_AV, "AV","0"),
            AVBsummaryRow(TYPE_V_A,"V_A","50"),
            AVBsummaryRow(TYPE_V_A,"V_A","100"),
            AVBsummaryRow(TYPE_V_A,"V_A","200"),
            AVBsummaryRow(TYPE_V_A,"V_A","300"),
            AVBsummaryRow(TYPE_V_A,"V_A","400"),
            AVBsummaryRow(TYPE_V_A,"V_A","800"),
            AVBsummaryRow(TYPE_V_A,"V_A","1200"),
            AVBsummaryRow(TYPE_V,  "V","0"))

        override fun add(trial: TrialBasic){
            when(trial.type){
                TYPE_A          ->   rows[0].add(trial)
                TYPE_A_V        -> {
                    when((trial as TrialBindingsUnBalanced).delay){
                        800L    -> rows[1].add(trial)
                        400L    -> rows[2].add(trial)
                        300L    -> rows[3].add(trial)
                        200L    -> rows[4].add(trial)
                        100L    -> rows[5].add(trial)
                    }
                }
                TYPE_AV         -> rows[6].add(trial)
                TYPE_V_A        -> {
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
    // simply implement setPercDiscrim
    inner class AVBsummaryRow(type:Int, label:String, latency:String):BindingsSummaryRow(type, label, latency){

        override fun setPercDiscrim(trial: TrialBasic):Int{
            return  if((trial.type == TYPE_AV && trial.success) || (trial.type != TYPE_AV && !trial.success))
                perc_discrimination + 1
            else    perc_discrimination
        }
    }
}

