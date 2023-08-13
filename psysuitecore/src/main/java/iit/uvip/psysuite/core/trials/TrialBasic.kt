package iit.uvip.psysuite.core.trials

// extra_param may be used to work in adaptive trials to manage the conversion from magnitude to/from stim_value
abstract class TrialBasic(var id:Int=-1, val type:Int, protected val label:String="", open var magnitude:Float=0F, val isADA:Boolean=false, var extra_param:Any? = null) {

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
    abstract fun Log():String

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



