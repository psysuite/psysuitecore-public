package iit.uvip.psysuite.core.tests.tfi

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.trials.TrialBasic

class TFIBISummary(ctx: Context) : Summary(ctx){

    private val SOA_1 = 0

    override val cond_labels:List<String>           = listOf("SOA 40")

    override var conditions: List<SummaryCondition> = listOf(TFISummaryCondition(SOA_1, cond_labels[0]))

    // after each trial, filled (with response and success) trial is added to summary
    override fun add(trial: TrialBasic){
        when((trial as TrialTFI).soa){
            TestTFI.soa_1 -> conditions[SOA_1].add(trial)
        }
    }

    // type is one of those defined in TFISummary
    inner class TFISummaryCondition(soa_id:Int, cond_label:String):SummaryCondition(cond_label){
        override var rows: List<SummaryRow> = listOf(

            SummaryRow(soa_id, "2,0,1"),
            SummaryRow(soa_id, "0,2,1"),
            SummaryRow(soa_id, "0,0,1"),
            SummaryRow(soa_id, "0,0,2"),
            SummaryRow(soa_id, "2,0,0"),
            SummaryRow(soa_id, "0,2,0"),
        )

        override fun add(trial: TrialBasic) {

            when((trial as TrialTFI).valid_answers[trial.correct_answer]){
                "2,0,1"    -> rows[0].add(trial)
                "0,2,1"    -> rows[1].add(trial)
                "0,0,1"    -> rows[2].add(trial)
                "0,0,2"    -> rows[3].add(trial)
                "2,0,0"    -> rows[4].add(trial)
                "0,2,0"    -> rows[5].add(trial)
            }
        }
    }

}



