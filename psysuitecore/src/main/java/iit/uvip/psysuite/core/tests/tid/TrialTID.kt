package iit.uvip.psysuite.core.tests.tid

import iit.uvip.psysuite.core.trials.TrialBasic


class TrialTID(id:Int=-1, type:Int, val block:Int, val group:Int, val session:Int, val refdelta:Float, override var magnitude:Float, val ref_first:Boolean, isBefore:Boolean, val duration:Long, val answers:List<String>, isADA:Boolean=false): TrialBasic(id, type,"", extra_param=isBefore, isADA=isADA){

    var delta1:Long = 0L
    var delta2:Long = 0L

    val ref_stim_value:Long
        get() = refdelta.toLong()

    override val stim_value:Long
        get() = if(extra_param as Boolean)  ref_stim_value - magnitude.toLong()
        else                                ref_stim_value + magnitude.toLong()

    companion object {
        @JvmStatic val LOG_HEADER           = "id\ttype\tbl\tgrp\tses\tanswer\tsucc\telapsed\td1\td2\tref_first\n"
    }

    init {
        updateTrial(magnitude)
    }

    override fun updateTrial(newvalue: Float):Long{
        magnitude = newvalue
        if(ref_first){
            delta1 = ref_stim_value
            delta2 = stim_value
        }else{
            delta2 = ref_stim_value
            delta1 = stim_value
        }

        // in quest mode, this assignment is wrong. correct_answer is updated when the new test value is calculated on-line
        correct_answer = when (delta2 > delta1) {
            true    -> 1
            false   -> 0
        }
        return stim_value
    }

    override fun setResponse(result: Int, elapsedms: Int, extra_text:String) {
        super.setResponse(result, elapsedms, extra_text)
        user_answer = result
        elapsed     = elapsedms
        success     = (result == correct_answer)
    }

    // all class exported as string
    override fun toString():String{
        return "" //id.toString() + "\t" + type.toString() + "\t" + label + "\t" + conflict_type + "\t" + stim_value.toString() + "\t" + duration.toString() + "\t" + success.toString() + "\t" + duration2.toString()+ "\n"
    }

    // data exported to log file
    override fun Log(): String {
        return id.toString() + "\t" + type.toString() + "\t" + block.toString() + "\t" + group.toString() + "\t" + session.toString() + "\t" + user_answer + "\t" + success.toString() + "\t" + elapsed.toString() + "\t" + delta1.toString() + "\t" + delta2.toString() + "\t" + ref_first.toString() +"\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, d1=$delta1, d2=$delta2, is_ref_first=$ref_first"
    }
}
