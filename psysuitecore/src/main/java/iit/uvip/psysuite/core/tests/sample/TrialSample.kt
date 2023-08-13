package iit.uvip.psysuite.core.tests.sample

import iit.uvip.psysuite.core.trials.TrialBasic


class TrialSample(id:Int=-1, type:Int, label:String, val source:Int, val extraTrial:Any?): TrialBasic(id,type,label){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tlat\tconflict\tres\tcor_ans\tuser_ans\telapsed\trep\n"
        @JvmStatic val LAST_STIMULUS_DELAY  = 1000
    }

    init {
    }

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\t" + label + "\t" + "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + label + "\t" + "\n"
    }
}
