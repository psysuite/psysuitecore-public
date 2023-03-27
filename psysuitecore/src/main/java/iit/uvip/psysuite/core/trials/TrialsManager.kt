package iit.uvip.psysuite.core.tests

import iit.uvip.psysuite.core.trials.TrialBasic


/*
type:
- 0:    predetermined trials
- 1:    quest trials
- ?:    other type of adaptative algorithms
 */

abstract class TrialsManager(val type:Int = 0, val mTrials:MutableList<TrialBasic>) {

    var currTrial:Int = 0

    val nTrials:Int
        get() = mTrials.size

    open var mTrial: TrialBasic
        get() = mTrials[currTrial]
        set(value) {
            mTrials[currTrial] = value
        }

    open fun setResponse(result:Int, elapsedms:Int, extra_text:String = ""){
        mTrial.setResponse(result, elapsedms, extra_text)
    }

    abstract fun getNewTrial():Any

}

