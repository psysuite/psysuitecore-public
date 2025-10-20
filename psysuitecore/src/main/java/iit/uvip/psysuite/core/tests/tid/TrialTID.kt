package iit.uvip.psysuite.core.tests.tid

import iit.uvip.psysuite.core.trials.TrialBasic


class TrialTID(id:Int=-1, type:Int, val block:Int, val group:Int, val session:String, val refdelta:Float, override var magnitude:Float, val ref_first:Boolean, val isBefore:Boolean, val duration:Long, val answers:List<String>, isADA:Boolean=false, isTraining:Boolean=false):
    TrialBasic(id, type, isADA=isADA, isTraining = isTraining){

    var delta1:Long = 0L
    var delta2:Long = 0L

    val ref_stim_value:Long
        get() = refdelta.toLong()

    override val stim_value:Long
        get() = if(isBefore)  ref_stim_value - magnitude.toLong()
                else          ref_stim_value + magnitude.toLong()

    companion object {
        @JvmStatic val LOG_HEADER           = "id\ttype\tbl\tgrp\tses\tanswer\tsucc\telapsed\td1\td2\tref_first\n"
    }

    init {
        setupTrial(magnitude)
    }

    override fun setupTrial(newvalue: Float):Long{
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

    // data exported to log file
    override fun Log(): String {
        return id.toString() + "\t" + type.toString() + "\t" + block.toString() + "\t" + group.toString() + "\t" + session.toString() + "\t" + user_answer + "\t" + success.toString() + "\t" + elapsed.toString() + "\t" + delta1.toString() + "\t" + delta2.toString() + "\t" + ref_first.toString() +"\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, d1=$delta1, d2=$delta2, is_ref_first=$ref_first"
    }
}
