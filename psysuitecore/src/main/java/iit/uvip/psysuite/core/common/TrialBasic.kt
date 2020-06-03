package iit.uvip.psysuite.core.common

// trial is used for psychophysics:
// - trial_id
// - stimtype_code
// - stimtype_label


// - conflict_type
// - position
// - duration
// - duration2


abstract class TrialBasic(var id:Int=-1, val type:Int, val label:String="") {

    var correct_answer: Any = ""
    var user_answer: Any = ""
    var repetitions:Int     =  1
    var success:Boolean     =  false
    var elapsed:Int         = -1

    // data exported to log file
    abstract fun Log():String

    fun setResponse(result: String, elapsedms: Int) {
        user_answer = result
        elapsed     = elapsedms
        success     = (result == correct_answer)
    }
}



