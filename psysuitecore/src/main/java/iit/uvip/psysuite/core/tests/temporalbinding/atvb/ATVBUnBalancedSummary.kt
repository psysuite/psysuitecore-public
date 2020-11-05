package iit.uvip.psysuite.core.tests.temporalbinding.atvb

import android.content.Context
import iit.uvip.psysuite.core.tests.TrialBasic
import iit.uvip.psysuite.core.tests.temporalbinding.TrialBindingsUnBalanced
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_ATV
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_AT_V
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_AV_T
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_A_TV
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_TV_A
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_T_AV
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB.Companion.TYPE_V_AT
import iit.uvip.psysuite.core.utility.Summary
import kotlin.math.roundToInt


class ATVBUnBalancedSummary(ctx:Context) : Summary(ctx){

    private val A_TV = 0
    private val T_AV = 1
    private val V_AT = 2
    private val ATV  = 3

    private val cond_labels:List<String> = listOf("A_TV", "T_AV", "V_AT", "ATV")

    private var conditions:MutableList<ATVBsummaryCondition> = mutableListOf(   ATVBsummaryCondition(A_TV, cond_labels[0]),
                                                                                ATVBsummaryCondition(T_AV, cond_labels[1]),
                                                                                ATVBsummaryCondition(V_AT, cond_labels[2]),
                                                                                ATVBsummaryCondition(ATV,  cond_labels[3]))
    private var summary:String = ""

    // after each trial, filled (with response and success) trial is added to summary
    override fun add(trial: TrialBasic){
        when(trial.type){
            TYPE_A_TV, TYPE_TV_A -> conditions[A_TV].add(trial as TrialBindingsUnBalanced)
            TYPE_T_AV, TYPE_AV_T -> conditions[T_AV].add(trial as TrialBindingsUnBalanced)
            TYPE_V_AT, TYPE_AT_V -> conditions[V_AT].add(trial as TrialBindingsUnBalanced)
            TYPE_ATV             -> conditions[ATV].add(trial as TrialBindingsUnBalanced)
        }
    }

    override fun close(filename:String, dir:String):String{
        conditions.map{
            it.close()
        }
        conditions.map{
            summary += "Condition ${it.label}\n"
            summary += it.toString()
        }
        return writeFile(summary, filename, dir)
    }

    // type is one of those defined in ATVBUnBalancedSummary
    inner class ATVBsummaryCondition(val innertype:Int, val label:String){

        private var latencies:MutableList<ATVBsummaryRow> = when(innertype) {
            A_TV -> mutableListOf(
                ATVBsummaryRow(TYPE_A_TV, "A_TV", 800),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", 400),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", 300),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", 200),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", 100),
                ATVBsummaryRow(TYPE_A_TV, "A_TV", 50),
                ATVBsummaryRow(TYPE_A_TV, "TV_A", 50),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", 100),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", 200),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", 300),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", 400),
                ATVBsummaryRow(TYPE_TV_A, "TV_A", 800)
            )
            T_AV -> mutableListOf(
                ATVBsummaryRow(TYPE_T_AV, "T_AV", 800),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", 400),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", 300),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", 200),
                ATVBsummaryRow(TYPE_T_AV, "T_AV", 100),
                ATVBsummaryRow(TYPE_A_TV, "T_AV", 50),
                ATVBsummaryRow(TYPE_A_TV, "AV_T", 50),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", 100),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", 200),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", 300),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", 400),
                ATVBsummaryRow(TYPE_AV_T, "AV_T", 800)
            )
            // V_AT
            V_AT -> mutableListOf(
                ATVBsummaryRow(TYPE_V_AT, "V_AT", 800),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", 400),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", 300),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", 200),
                ATVBsummaryRow(TYPE_V_AT, "V_AT", 100),
                ATVBsummaryRow(TYPE_A_TV, "V_AT", 50),
                ATVBsummaryRow(TYPE_A_TV, "AT_V", 50),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", 100),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", 200),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", 300),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", 400),
                ATVBsummaryRow(TYPE_AT_V, "AT_V", 800)
            )
            else -> mutableListOf(ATVBsummaryRow(TYPE_ATV, "ATV", 0))
        }

        fun add(trial: TrialBindingsUnBalanced){
            when(trial.type){
                TYPE_A_TV, TYPE_T_AV, TYPE_V_AT ->{
                    when(trial.delay){
                        800L    -> latencies[0].add(trial)
                        400L    -> latencies[1].add(trial)
                        300L    -> latencies[2].add(trial)
                        200L    -> latencies[3].add(trial)
                        100L    -> latencies[4].add(trial)
                        50L     -> latencies[5].add(trial)
                    }
                }
                TYPE_TV_A, TYPE_AV_T, TYPE_AT_V ->{
                    when(trial.delay){
                        50L     -> latencies[6].add(trial)
                        100L    -> latencies[7].add(trial)
                        200L    -> latencies[8].add(trial)
                        300L    -> latencies[9].add(trial)
                        400L    -> latencies[10].add(trial)
                        800L    -> latencies[11].add(trial)
                    }
                }
                TYPE_ATV ->{
                    latencies[0].add(trial)
                }
            }
        }

        fun close(){
            latencies.map{
                it.close()
            }
        }

        override fun toString():String{
            var res = "type\tlat\tntr\t%yes\t%succ\trt\n"
            latencies.map{
                res += it.toString()
            }
            return res
        }
    }

    // type is one of those defined in TestATVB::mTrial
    inner class ATVBsummaryRow(val type:Int, val label:String, val latency:Int){

        private var ntrial:Int              = 0
        private var perc_discrimination:Int = 0
        private var rt:Int                  = 0
        private var perc_succ:Int           = 0

        fun add(trial: TrialBindingsUnBalanced){
            ntrial++
            rt += trial.elapsed
            if(trial.success)   perc_succ++

            if((trial.type == TYPE_ATV && trial.success) || (trial.type != TYPE_ATV && !trial.success)) perc_discrimination++
        }

        fun close(){

            if(ntrial > 0){
                rt                  = (((rt*1F) / ntrial) * 1F).roundToInt()
                perc_succ           = (((perc_succ*1F) / ntrial) * 100F).roundToInt()
                perc_discrimination = (((perc_discrimination*1F) / ntrial) * 100F).roundToInt()
            }
        }

        override fun toString():String{
            return "$label\t$latency\t$ntrial\t$perc_discrimination\t$perc_succ\t$rt\n"
        }
    }
}