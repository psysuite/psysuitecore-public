package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.tests.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_ATV
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_AT_V
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_AV_T
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_A_TV
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_TV_A
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_T_AV
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_V_AT


class ATVBUnBalancedSummary(ctx:Context) : Summary(ctx){

    private val A_TV = 0
    private val T_AV = 1
    private val V_AT = 2
    private val ATV  = 3

    override val cond_labels:List<String> = listOf("A_TV", "T_AV", "V_AT", "ATV")

    override var conditions:List<SummaryCondition> = listOf(ATVBsummaryCondition(A_TV, cond_labels[0]),
                                                            ATVBsummaryCondition(T_AV, cond_labels[1]),
                                                            ATVBsummaryCondition(V_AT, cond_labels[2]),
                                                            ATVBsummaryCondition(ATV,  cond_labels[3]))

    // after each trial, filled (with response and success) trial is added to summary
    override fun add(trial: TrialBasic){
        when(trial.type){
            TYPE_A_TV, TYPE_TV_A -> conditions[A_TV].add(trial as TrialBindingsUnBalanced)
            TYPE_T_AV, TYPE_AV_T -> conditions[T_AV].add(trial as TrialBindingsUnBalanced)
            TYPE_V_AT, TYPE_AT_V -> conditions[V_AT].add(trial as TrialBindingsUnBalanced)
            TYPE_ATV             -> conditions[ATV].add(trial as TrialBindingsUnBalanced)
        }
    }

    // type is one of those defined in ATVBUnBalancedSummary
    inner class ATVBsummaryCondition(inner_type:Int, cond_label:String): BindingsSummaryCondition(cond_label){

        override var rows:List<SummaryRow> = when(inner_type) {
            A_TV -> listOf(
                ATVBsummaryRow(TYPE_A_TV, "A_TV", "1200"),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", "800"),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", "400"),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", "300"),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", "200"),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", "100"),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", "50"),
                ATVBsummaryRow(TYPE_A_TV, "TV_A", "50"),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", "100"),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", "200"),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", "300"),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", "400"),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", "800"),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", "1200")
            )
            T_AV -> listOf(
                ATVBsummaryRow(TYPE_T_AV, "T_AV", "1200"),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", "800"),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", "400"),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", "300"),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", "200"),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", "100"),
                ATVBsummaryRow(TYPE_A_TV, "T_AV", "50"),
                ATVBsummaryRow(TYPE_A_TV, "AV_T", "50"),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", "100"),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", "200"),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", "300"),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", "400"),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", "800"),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", "1200")
            )
            // V_AT
            V_AT -> listOf(
                ATVBsummaryRow(TYPE_V_AT, "V_AT", "1200"),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", "800"),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", "400"),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", "300"),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", "200"),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", "100"),
                ATVBsummaryRow(TYPE_A_TV, "V_AT", "50"),
                ATVBsummaryRow(TYPE_A_TV, "AT_V", "50"),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", "100"),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", "200"),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", "300"),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", "400"),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", "800"),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", "1200")
            )
            else -> listOf(ATVBsummaryRow(TYPE_ATV, "ATV", "0"))
        }

        override fun add(trial: TrialBasic){
            when(trial.type){
                TYPE_A_TV, TYPE_T_AV, TYPE_V_AT ->{
                    when((trial as TrialBindingsUnBalanced).delay){
                        800L    -> rows[0].add(trial)
                        400L    -> rows[1].add(trial)
                        300L    -> rows[2].add(trial)
                        200L    -> rows[3].add(trial)
                        100L    -> rows[4].add(trial)
                        50L     -> rows[5].add(trial)
                    }
                }
                TYPE_TV_A, TYPE_AV_T, TYPE_AT_V ->{
                    when((trial as TrialBindingsUnBalanced).delay){
                        50L     -> rows[6].add(trial)
                        100L    -> rows[7].add(trial)
                        200L    -> rows[8].add(trial)
                        300L    -> rows[9].add(trial)
                        400L    -> rows[10].add(trial)
                        800L    -> rows[11].add(trial)
                    }
                }
                TYPE_ATV ->{
                    rows[0].add(trial)
                }
            }
        }
    }

    // type is one of those defined in TestATVB::mTrial
    inner class ATVBsummaryRow(type:Int, label:String, latency:String): BindingsSummaryRow(type, label, latency){

        override fun setPercDiscrim(trial: TrialBasic):Int{
            return  if((trial.type == TYPE_ATV && trial.success) || (trial.type != TYPE_ATV && !trial.success))
                            perc_discrimination + 1
                    else    perc_discrimination
        }
    }
}