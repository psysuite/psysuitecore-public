package iit.uvip.psysuite.core.tests.tfi

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.trials.TrialBasic

class TFISummary(ctx: Context) : Summary(ctx){

    private val SOA_1 = 0
    private val SOA_2 = 1

    override val cond_labels:List<String>           = listOf("SOA 55", "SOA 85")

    override var conditions: List<SummaryCondition> = listOf(TFISummaryCondition(SOA_1, cond_labels[0]),
                                                             TFISummaryCondition(SOA_2, cond_labels[1]))

    // after each trial, filled (with response and success) trial is added to summary
    override fun add(trial: TrialBasic){
        when((trial as TrialTFI).soa){
            TestTFI.soa_1 -> conditions[SOA_1].add(trial)
            TestTFI.soa_2 -> conditions[SOA_2].add(trial)
        }
    }

    // type is one of those defined in TFISummary
    inner class TFISummaryCondition(soa_id:Int, cond_label:String):SummaryCondition(cond_label){
        override var rows: List<SummaryRow> = listOf(
            SummaryRow(soa_id, "0,0,1"),
            SummaryRow(soa_id, "0,0,2"),
            SummaryRow(soa_id, "1,0,0"),
            SummaryRow(soa_id, "2,0,0"),
            SummaryRow(soa_id, "0,1,0"),
            SummaryRow(soa_id, "0,2,0"),

            SummaryRow(soa_id, "2,0,1"),
            SummaryRow(soa_id, "1,0,2"),
            SummaryRow(soa_id, "1,2,0"),
            SummaryRow(soa_id, "2,1,0"),
            SummaryRow(soa_id, "0,1,2"),
            SummaryRow(soa_id, "0,2,1"),

            SummaryRow(soa_id, "1,0,1"),
            SummaryRow(soa_id, "2,0,2"),
            SummaryRow(soa_id, "1,1,0"),
            SummaryRow(soa_id, "2,2,0"),
            SummaryRow(soa_id, "0,2,2"),
            SummaryRow(soa_id, "0,1,1"),

            SummaryRow(soa_id, "1,2,1"),
            SummaryRow(soa_id, "2,1,2"),
            SummaryRow(soa_id, "2,1,1"),
            SummaryRow(soa_id, "1,2,2"),
            SummaryRow(soa_id, "1,1,2"),
            SummaryRow(soa_id, "2,2,1"),

            SummaryRow(soa_id, "1,1,1"),
            SummaryRow(soa_id, "2,2,2"),
        )

        override fun add(trial: TrialBasic) {

            when((trial as TrialTFI).valid_answers[trial.correct_answer]){
                "0,0,1"    -> rows[0].add(trial)
                "0,0,2"    -> rows[1].add(trial)
                "1,0,0"    -> rows[2].add(trial)
                "2,0,0"    -> rows[3].add(trial)
                "0,1,0"    -> rows[4].add(trial)
                "0,2,0"    -> rows[5].add(trial)

                "2,0,1"    -> rows[6].add(trial)
                "1,0,2"    -> rows[7].add(trial)
                "1,2,0"    -> rows[8].add(trial)
                "2,1,0"    -> rows[9].add(trial)
                "0,1,2"    -> rows[10].add(trial)
                "0,2,1"    -> rows[11].add(trial)

                "1,0,1"    -> rows[12].add(trial)
                "2,0,2"    -> rows[13].add(trial)
                "1,1,0"    -> rows[14].add(trial)
                "2,2,0"    -> rows[15].add(trial)
                "0,2,2"    -> rows[16].add(trial)
                "0,1,1"    -> rows[17].add(trial)

                "1,2,1"    -> rows[18].add(trial)
                "2,1,2"    -> rows[19].add(trial)
                "2,1,1"    -> rows[20].add(trial)
                "1,2,2"    -> rows[21].add(trial)
                "1,1,2"    -> rows[22].add(trial)
                "2,2,1"    -> rows[23].add(trial)

                "1,1,1"    -> rows[24].add(trial)
                "2,2,2"    -> rows[25].add(trial)
            }
        }
    }

}



