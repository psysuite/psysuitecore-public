package iit.uvip.psysuite.core.tests.tir

import android.util.Log
import iit.uvip.psysuite.core.trials.TrialBasic
import org.albaspazio.psysuite.adaptive.ado.ADOWrapper
import kotlin.math.abs

/*
    stim_value: duration of the stimulus
    correct_answer: time to press (in ms)
    user_answer:  is the error = (time pressed by the user - correct_answer)
    succ: if abs(curr error) < abs(prev error), then success is true
 */

class TrialTIR (id:Int=-1, type:Int, label:String,
                override var magnitude:Float,
                adoWrapper: ADOWrapper?=null, isTraining: Boolean=false): TrialBasic(id, type, label, adoWrapper = adoWrapper, isTraining = isTraining) {

    companion object {
        @JvmStatic val LOG_HEADER = "id\tlabel\tdur\terror\tsuccess\n"
    }

    init {
        setupTrial(magnitude)
    }

    override fun setupTrial(newvalue:Float):Long {
        magnitude       = newvalue
        correct_answer  = newvalue.toInt()
        return stim_value
    }

    // result: user's button press duration == elapsed
    // success is true if the present error is smaller than the previous one
    // if first trial, success is always true
    override fun setResponse(result:Int, elapsedms:Long, prev_tr: TrialBasic?, extra_text:String) {
        user_answer         = (result - correct_answer)
        prev_trial          = prev_tr

        success =   if(prev_tr!= null){
            val succ = (abs(user_answer) <= abs(prev_tr.user_answer))

            val delta = user_answer - prev_tr.user_answer
            Log.d("TrialTIR", "--------------------------------------------")
            Log.d("TrialTIR", "delta=$delta,  success=$succ, curr_error=$user_answer, prev_error=${prev_tr.user_answer}, ")
            succ
        }
        else    true
        user_answer_extra   = extra_text
    }

    // data exported to log file
    //          "id\tlabel\tisi         \tonset         \terror\tsuccess\telapsed\tmagnitude\n"
    override fun Log():String{
        return "$id\t$label\t$stim_value\t$user_answer\t$success\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, pos=$stim_value"
    }
}