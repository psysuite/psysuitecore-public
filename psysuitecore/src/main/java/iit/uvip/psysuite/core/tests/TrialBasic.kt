package iit.uvip.psysuite.core.tests

abstract class TrialBasic(var id:Int=-1, val type:Int, protected val label:String="", var correct_answer:String) {

    var user_answer:String  = ""
    var repetitions:Int     =  1
    var elapsed:Int         = -1

    var success:Boolean     =  false    // result of comparison between correct and user answer

    // data exported to log file
    abstract fun Log():String

    open fun debugInfo():String{
        return "lab=$label, type=$type, corr_answ=$correct_answer"
    }

    open fun setResponse(result: String, elapsedms: Int) {
        user_answer = result
        elapsed     = elapsedms
        success     = (result == correct_answer)
    }

    fun getCorrectAnswer():String{
        return correct_answer
    }
}



