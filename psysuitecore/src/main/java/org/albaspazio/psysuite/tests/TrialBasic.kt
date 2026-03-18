package org.albaspazio.psysuite.tests

import org.albaspazio.psysuite.adaptive.ado.ADOWrapper

open class TrialBasic(
    var id: Int = -1,
    val type: Int,
    protected val label: String = "",
    open var magnitude: Float = 0F,
    val adoWrapper: ADOWrapper? = null,
    val isTraining: Boolean = false
) {

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tres\tcor_ans\tuser_ans\telapsed\trep\n"
    }

    var user_answer:Int             = -1
    var repetitions:Int             =  1
    var elapsed:Long                = -1L
    var prev_trial: TrialBasic?     = null
    var user_answer_extra:String    = ""
    open var correct_answer:Int     = 0 // TODO: should become Any. so i can store also durations (e.g. TTC e TSP),
                                        // TODO: must solve the fact that in 2AFC task the int regulates also
    var success:Boolean             = false    // result of comparison between correct and user answer

    // - update the magnitude value
    // - define, whether applicable (when results depends only on the present trial's user response), the correct answer
    // - return the value actually given to the subject
    open fun initTrial(newvalue:Float):Long {
        magnitude       = newvalue
        correct_answer  = 0
        return stim_value
    }

    // value actually given to the subject (may or may not coincide with magnitude)
    // this properties shall be overridden in all the tasks that need to control magnitude-stimulus coupling
    open val stim_value:Long
        get() = magnitude.toLong()

    // - contains user response (result, elapsed, extra)
    // calculate success
    // this method shall be overridden in all the tasks where success also depends on previous trial (e.g. TTC, TSP)
    open fun setResponse(result: Int, elapsedms: Long = -1L, prev_tr: TrialBasic? = null, extra_text:String="") {
        user_answer         = result
        elapsed             = elapsedms
        prev_trial          = prev_tr
        success             = (result == correct_answer)
        user_answer_extra   = extra_text
    }

    // determine which property (in general: success or user_response) is used to update the ADO model
    // HERE trial subclasses, overriding this method, can implement their own logic
    open fun getAdoUpdatingPropr(): Any {
        return user_answer  // default behavior
    }

    override fun toString():String{
        return Log()
    }

    // data exported to log file
    open fun Log():String{
        return id.toString() +  "\t" + label + "\t" + success.toString() + "\t" + correct_answer + "\t" + user_answer + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\n"
    }

    open fun debugInfo():String{
        return "lab=$label, type=$type, stim_value=$stim_value, corr_answ=$correct_answer"
    }

}