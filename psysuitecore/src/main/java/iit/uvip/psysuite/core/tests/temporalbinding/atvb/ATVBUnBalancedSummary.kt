package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.unbalSD
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_ATV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_AT_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_AV_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_TV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_TV_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_AV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_AT


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
                ATVBsummaryRow(TYPE_A_TV, "A_TV", unbalSD[6].second),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", unbalSD[5].second),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", unbalSD[4].second),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", unbalSD[3].second),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", unbalSD[2].second),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", unbalSD[1].second),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", unbalSD[0].second),
                ATVBsummaryRow(TYPE_A_TV, "TV_A", unbalSD[0].second),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", unbalSD[1].second),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", unbalSD[2].second),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", unbalSD[3].second),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", unbalSD[4].second),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", unbalSD[5].second),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", unbalSD[6].second)
            )
            T_AV -> listOf(
                ATVBsummaryRow(TYPE_T_AV, "T_AV", unbalSD[6].second),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", unbalSD[5].second),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", unbalSD[4].second),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", unbalSD[3].second),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", unbalSD[2].second),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", unbalSD[1].second),
                ATVBsummaryRow(TYPE_A_TV, "T_AV", unbalSD[0].second),
                ATVBsummaryRow(TYPE_A_TV, "AV_T", unbalSD[0].second),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", unbalSD[1].second),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", unbalSD[2].second),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", unbalSD[3].second),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", unbalSD[4].second),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", unbalSD[5].second),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", unbalSD[6].second)
            )
            // V_AT
            V_AT -> listOf(
                ATVBsummaryRow(TYPE_V_AT, "V_AT", unbalSD[6].second),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", unbalSD[5].second),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", unbalSD[4].second),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", unbalSD[3].second),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", unbalSD[2].second),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", unbalSD[1].second),
                ATVBsummaryRow(TYPE_A_TV, "V_AT", unbalSD[0].second),
                ATVBsummaryRow(TYPE_A_TV, "AT_V", unbalSD[0].second),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", unbalSD[1].second),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", unbalSD[2].second),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", unbalSD[3].second),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", unbalSD[4].second),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", unbalSD[5].second),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", unbalSD[6].second)
            )
            else -> listOf(ATVBsummaryRow(TYPE_ATV, "ATV", "0"))
        }

        override fun add(trial: TrialBasic){
            when(trial.type){
                TYPE_A_TV, TYPE_T_AV, TYPE_V_AT ->{
                    when((trial as TrialBindingsUnBalanced).magnitude){
                        unbalSD[6].first    -> rows[0].add(trial)
                        unbalSD[5].first    -> rows[1].add(trial)
                        unbalSD[4].first    -> rows[2].add(trial)
                        unbalSD[3].first    -> rows[3].add(trial)
                        unbalSD[2].first    -> rows[4].add(trial)
                        unbalSD[1].first    -> rows[5].add(trial)
                        unbalSD[0].first    -> rows[6].add(trial)
                    }
                }
                TYPE_TV_A, TYPE_AV_T, TYPE_AT_V ->{
                    when((trial as TrialBindingsUnBalanced).magnitude){
                        unbalSD[0].first    -> rows[7].add(trial)
                        unbalSD[1].first    -> rows[8].add(trial)
                        unbalSD[2].first    -> rows[9].add(trial)
                        unbalSD[3].first    -> rows[10].add(trial)
                        unbalSD[4].first    -> rows[11].add(trial)
                        unbalSD[5].first    -> rows[12].add(trial)
                        unbalSD[6].first    -> rows[13].add(trial)
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