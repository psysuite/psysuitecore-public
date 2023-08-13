package iit.uvip.psysuite.core.tests.temporalbinding.avb

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
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_AV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_A


class AVBUnBalancedSummary(ctx:Context) : Summary(ctx){

    override var conditions:List<SummaryCondition> = listOf(AVBsummaryCondition())

    // type is one of those defined in AVBUnBalancedSummary
    inner class AVBsummaryCondition: BindingsSummaryCondition(){

        override var rows:List<SummaryRow> = listOf(
            AVBsummaryRow(TYPE_A,  "A","0"),
            AVBsummaryRow(TYPE_A_V,"A_V",unbalSD[6].second),
            AVBsummaryRow(TYPE_A_V,"A_V",unbalSD[5].second),
            AVBsummaryRow(TYPE_A_V,"A_V",unbalSD[4].second),
            AVBsummaryRow(TYPE_A_V,"A_V",unbalSD[3].second),
            AVBsummaryRow(TYPE_A_V,"A_V",unbalSD[2].second),
            AVBsummaryRow(TYPE_A_V,"A_V",unbalSD[1].second),
            AVBsummaryRow(TYPE_A_V,"A_V",unbalSD[0].second),
            AVBsummaryRow(TYPE_AV, "AV","0"),
            AVBsummaryRow(TYPE_V_A,"V_A",unbalSD[0].second),
            AVBsummaryRow(TYPE_V_A,"V_A",unbalSD[1].second),
            AVBsummaryRow(TYPE_V_A,"V_A",unbalSD[2].second),
            AVBsummaryRow(TYPE_V_A,"V_A",unbalSD[3].second),
            AVBsummaryRow(TYPE_V_A,"V_A",unbalSD[4].second),
            AVBsummaryRow(TYPE_V_A,"V_A",unbalSD[5].second),
            AVBsummaryRow(TYPE_V_A,"V_A",unbalSD[6].second),
            AVBsummaryRow(TYPE_V,  "V","0"))

        override fun add(trial: TrialBasic){
            when(trial.type){
                TYPE_A          ->   rows[0].add(trial)
                TYPE_A_V        -> {
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
                TYPE_AV         -> rows[8].add(trial)
                TYPE_V_A        -> {
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
    // simply implement setPercDiscrim
    inner class AVBsummaryRow(type:Int, label:String, latency:String):BindingsSummaryRow(type, label, latency){

        override fun setPercDiscrim(trial: TrialBasic):Int{
            return  if((trial.type == TYPE_AV && trial.success) || (trial.type != TYPE_AV && !trial.success))
                perc_discrimination + 1
            else    perc_discrimination
        }
    }
}

