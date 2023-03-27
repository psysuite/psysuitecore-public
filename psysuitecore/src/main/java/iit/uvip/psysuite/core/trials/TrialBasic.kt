package iit.uvip.psysuite.core.trials

abstract class TrialBasic(var id:Int=-1, val type:Int, protected val label:String="", var correct_answer:Int=0, var variable_param:Any? = null) {

    var user_answer:Int             = -1
    var repetitions:Int             =  1
    var elapsed:Int                 = -1
    var user_answer_extra:String    = ""

    var success:Boolean     =  false    // result of comparison between correct and user answer

    // data exported to log file
    abstract fun Log():String

    open fun debugInfo():String{
        return "lab=$label, type=$type, corr_answ=$correct_answer"
    }

    open fun setResponse(result: Int, elapsedms: Int, extra_text:String = "") {
        user_answer = result
        elapsed     = elapsedms
        user_answer_extra = extra_text

        success     = (result == correct_answer)
    }

    open fun updateTrial(newvalue:Float){}

}



