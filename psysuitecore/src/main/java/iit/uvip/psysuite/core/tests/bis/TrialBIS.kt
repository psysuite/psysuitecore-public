package iit.uvip.psysuite.core.tests.bis

import iit.uvip.psysuite.core.trials.TrialBasic

// trial adopting the pattern where magnitude and stim_value does not coincide....I fix a magnitude and, through the isBefore parameter, I calculate the stim_value

open class TrialBIS(id:Int=-1, type:Int, label:String, final override var magnitude:Float, isBefore:Boolean, val conflict_type:String, val duration:Long, private val duration2:Long=0L, val mid_latency:Long = 500L, isADA:Boolean=false): TrialBasic(id, type, label, extra_param=isBefore, isADA=isADA){

    override val stim_value:Long
        get() = if(extra_param as Boolean)   mid_latency - magnitude.toLong()
                else                         mid_latency + magnitude.toLong()

    companion object {
        @JvmStatic val LOG_HEADER = "id\tlabel\tlat\tconfl\tres\tcor_ans\tuser_ans\telapsed\trep\n"
    }

    init {
        updateTrial(magnitude)
    }
    // all class exported as string
    override fun toString():String{
        return "$id\t$type\t$label\t$conflict_type\t$stim_value\t$duration\t$success\t$duration2\n"
    }

    // data exported to log file
    override fun Log():String{
        return "$id\t$label\t$stim_value\t$conflict_type\t$success\t$correct_answer\t$user_answer\t$elapsed\t$repetitions\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, pos=$stim_value, conf_type=$conflict_type"
    }

    final override fun updateTrial(newvalue: Float):Long{
        magnitude       = newvalue
        correct_answer  =   if(extra_param as Boolean)   0
                            else                         1
        return stim_value
    }

    protected fun stimvalue2magnitude():Long{
        return  if(extra_param as Boolean)  mid_latency - stim_value
                else                        stim_value - mid_latency
    }
}
