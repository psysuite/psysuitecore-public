package iit.uvip.psysuite.core.trials

open class TrialBasic(var id:Int=-1, val type:Int, protected val label:String="", open var magnitude:Float=0F, val isADA:Boolean=false) {

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tres\tcor_ans\tuser_ans\telapsed\trep\n"
    }

    var user_answer:Int             = -1
    var repetitions:Int             =  1
    var elapsed:Int                 = -1
    var user_answer_extra:String    = ""
    open var correct_answer:Int          = 0
    var success:Boolean             =  false    // result of comparison between correct and user answer

    // value actually given to the subject
    // this properties shall be overridden in all the tasks that need to manipulate magnitude (e.g. temporal bisection)
    open val stim_value:Long
        get() = magnitude.toLong()

    // data exported to log file
    open fun Log():String{
        return id.toString() +  "\t" + label + "\t" + success.toString() + "\t" + correct_answer + "\t" + user_answer + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\n"
    }

    open fun debugInfo():String{
        return "lab=$label, type=$type, stim_value=$stim_value, corr_answ=$correct_answer"
    }

    open fun setResponse(result: Int, elapsedms: Int, extra_text:String = "") {
        user_answer         = result
        elapsed             = elapsedms
        user_answer_extra   = extra_text
        success             = (result == correct_answer)
    }

    // update the magnitude value and return the value actually given to the subject
    open fun updateTrial(newvalue:Float):Long {
        magnitude       = newvalue
        correct_answer  = 0
        return stim_value
    }
}



