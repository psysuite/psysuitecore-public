package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import android.content.Context
import iit.uvip.psysuite.core.model.summary.Summary
import iit.uvip.psysuite.core.model.summary.SummaryCondition
import iit.uvip.psysuite.core.model.summary.SummaryRow
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.balshSD
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryCondition
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsSummaryRow
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindings3latencies
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_ATV
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_T_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_A_V_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_A_V
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_T_V_A
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_A_T
import iit.uvip.psysuite.core.tests.temporalbinding.BindingsConstants.Companion.TYPE_V_T_A


class ATVBBalancedSummary(ctx:Context) : Summary(ctx){

    override val cond_labels:List<String> = listOf("T_A_V", "V_A_T", "A_T_V", "V_T_A", "A_V_T", "T_V_A")

    override var conditions:List<SummaryCondition> = listOf(
        ATVBsummaryCondition(TYPE_T_A_V, cond_labels[0]),
        ATVBsummaryCondition(TYPE_V_A_T, cond_labels[1]),
        ATVBsummaryCondition(TYPE_A_T_V, cond_labels[2]),
        ATVBsummaryCondition(TYPE_V_T_A, cond_labels[3]),
        ATVBsummaryCondition(TYPE_A_V_T, cond_labels[4]),
        ATVBsummaryCondition(TYPE_T_V_A, cond_labels[5]))

    // after each trial, filled (with response and success) trial is added to summary
    override fun add(trial: TrialBasic){
        if(trial.type == TYPE_ATV){
            // add simultaneous trial to all 6 conditions
            conditions.map{
                it.add(trial as TrialBindings3latencies)
            }
        }
        else    conditions[trial.type-10].add(trial as TrialBindings3latencies)
    }

    // type is one of those defined in ATVBUnBalancedSummary
    inner class ATVBsummaryCondition(type:Int, cond_label:String): BindingsSummaryCondition(cond_label){

        override var rows:List<SummaryRow> = listOf(
                ATVBsummaryRow(type, cond_label, "0"),
                ATVBsummaryRow(type, cond_label, balshSD[0].second),
                ATVBsummaryRow(type, cond_label, balshSD[1].second),
                ATVBsummaryRow(type, cond_label, balshSD[2].second))

        override fun add(trial: TrialBasic){
                when((trial as TrialBindingsBalanced).magnitude){
                    balshSD[2].first    -> rows[3].add(trial)
                    balshSD[1].first    -> rows[2].add(trial)
                    balshSD[0].first    -> rows[1].add(trial)
                    0.0F                -> rows[0].add(trial)
                }
            }
    }


    // type is one of those defined in TestATVB::mTrial
    inner class ATVBsummaryRow(type:Int, label:String, latency:String): BindingsSummaryRow(type, label, latency){

        override fun setPercDiscrim(trial: TrialBasic):Int{
            return  if(trial.success)
                            perc_discrimination + 1
                    else    perc_discrimination
        }
    }
}


//class ATVBBalancedSummary(ctx:Context) : Summary(ctx){
//
//    private val A_TV = 0
//    private val T_AV = 1
//    private val V_AT = 2
//    private val ATV  = 3
//
//    override val cond_labels:List<String> = listOf("A_TV", "T_AV", "V_AT", "ATV")
//
//    override var conditions:List<SummaryCondition> = listOf(ATVBsummaryCondition(A_TV, cond_labels[0]),
//                                                            ATVBsummaryCondition(T_AV, cond_labels[1]),
//                                                            ATVBsummaryCondition(V_AT, cond_labels[2]),
//                                                            ATVBsummaryCondition(ATV,  cond_labels[3]))
//
//    // after each trial, filled (with response and success) trial is added to summary
//    override fun add(trial: TrialBasic){
//        when(trial.type){
//            TYPE_T_A_V, TYPE_V_A_T  -> conditions[A_TV].add(trial as TrialBindings3latencies)
//            TYPE_A_T_V, TYPE_V_T_A  -> conditions[T_AV].add(trial as TrialBindings3latencies)
//            TYPE_A_V_T, TYPE_T_V_A  -> conditions[V_AT].add(trial as TrialBindings3latencies)
//            TYPE_ATV                -> conditions[ATV].add(trial as TrialBindings3latencies)
//        }
//    }
//
//    // type is one of those defined in ATVBUnBalancedSummary
//    inner class ATVBsummaryCondition(inner_type:Int, cond_label:String): BindingsSummaryCondition(cond_label){
//
//        override var rows:List<SummaryRow> = when(inner_type) {
//            A_TV -> listOf(
//                ATVBsummaryRow(TYPE_A_TV, "T_A_V", "300"),
//                ATVBsummaryRow(TYPE_A_TV, "T_A_V", "200"),
//                ATVBsummaryRow(TYPE_A_TV, "T_A_V", "100"),
//                ATVBsummaryRow(TYPE_A_TV, "T_A_V", "50"),
//                ATVBsummaryRow(TYPE_A_TV, "V_A_T", "50"),
//                ATVBsummaryRow(TYPE_TV_A, "V_A_T", "100"),
//                ATVBsummaryRow(TYPE_TV_A, "V_A_T", "200"),
//                ATVBsummaryRow(TYPE_TV_A, "V_A_T", "300")
//            )
//            T_AV -> listOf(
//                ATVBsummaryRow(TYPE_T_AV, "A_T_V", "300"),
//                ATVBsummaryRow(TYPE_T_AV, "A_T_V", "200"),
//                ATVBsummaryRow(TYPE_T_AV, "A_T_V", "100"),
//                ATVBsummaryRow(TYPE_A_TV, "A_T_V", "50"),
//                ATVBsummaryRow(TYPE_A_TV, "V_T_A", "50"),
//                ATVBsummaryRow(TYPE_AV_T, "V_T_A", "100"),
//                ATVBsummaryRow(TYPE_AV_T, "V_T_A", "200"),
//                ATVBsummaryRow(TYPE_AV_T, "V_T_A", "300")
//            )
//            // V_AT
//            V_AT -> listOf(
//                ATVBsummaryRow(TYPE_V_AT, "A_V_T", "300"),
//                ATVBsummaryRow(TYPE_V_AT, "A_V_T", "200"),
//                ATVBsummaryRow(TYPE_V_AT, "A_V_T", "100"),
//                ATVBsummaryRow(TYPE_A_TV, "A_V_T", "50"),
//                ATVBsummaryRow(TYPE_A_TV, "T_V_A", "50"),
//                ATVBsummaryRow(TYPE_AT_V, "T_V_A", "100"),
//                ATVBsummaryRow(TYPE_AT_V, "T_V_A", "200"),
//                ATVBsummaryRow(TYPE_AT_V, "T_V_A", "300")
//            )
//            else -> listOf(ATVBsummaryRow(TYPE_ATV, "ATV", "0"))
//        }
//
//        override fun add(trial: TrialBasic){
//            when(trial.type){
//                TYPE_T_A_V, TYPE_A_T_V, TYPE_A_V_T ->{
//                    when((trial as TrialBindingsBalanced).delay){
//                        300L    -> rows[0].add(trial)
//                        200L    -> rows[1].add(trial)
//                        100L    -> rows[2].add(trial)
//                        50L     -> rows[3].add(trial)
//                    }
//                }
//                TYPE_V_A_T, TYPE_V_T_A, TYPE_T_V_A ->{
//                    when((trial as TrialBindingsBalanced).delay){
//                        50L     -> rows[4].add(trial)
//                        100L    -> rows[5].add(trial)
//                        200L    -> rows[6].add(trial)
//                        300L    -> rows[7].add(trial)
//                    }
//                }
//                TYPE_ATV ->{
//                    rows[0].add(trial)
//                }
//            }
//        }
//    }
//
//    // type is one of those defined in TestATVB::mTrial
//    inner class ATVBsummaryRow(type:Int, label:String, latency:String): BindingsSummaryRow(type, label, latency){
//
//        override fun setPercDiscrim(trial: TrialBasic):Int{
//            return  if(trial.success)
//                            perc_discrimination + 1
//                    else    perc_discrimination
//        }
//    }
//}
