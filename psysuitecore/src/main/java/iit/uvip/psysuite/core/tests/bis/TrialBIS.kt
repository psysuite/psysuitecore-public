package iit.uvip.psysuite.core.tests.bis

import iit.uvip.psysuite.core.tests.TrialBasic


class TrialBIS(id:Int=-1, type:Int, label:String, corr_answer:String, val position:Int, val conflict_type:String, val duration:Int, private val duration2:Int=0): TrialBasic(id,type,label, corr_answer){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tlat\tconfl\tres\tcor_ans\tuser_ans\telapsed\trep\n"
    }

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\t" + label + "\t" + conflict_type + "\t" + position.toString() + "\t" + duration.toString() + "\t" + success.toString() + "\t" + duration2.toString()+ "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + label + "\t" + position.toString() + "\t" + conflict_type + "\t" + success.toString() + "\t" + correct_answer + "\t" + user_answer + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, pos=$position, conf_type=$conflict_type"
    }
}
