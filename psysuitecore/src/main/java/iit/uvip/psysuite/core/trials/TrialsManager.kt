package iit.uvip.psysuite.core.trials


/*
type:
- 0:    predetermined trials
- 1:    quest trials
- ?:    other type of adaptative algorithms
 */

abstract class TrialsManager(val type:Int = 0, val mTrials:MutableList<TrialBasic>) {

    companion object {
        val ADAPTIVE_VALUE:Float = -99999999.9F
    }

    init {
        if(mTrials.isEmpty())
            throw Exception("ERROR in TrialsManager. given trials list is empty")
        setTrialsID()
    }

    var currTrial:Int = 0

    val nTrials:Int
        get() = mTrials.size

    open var mTrial:TrialBasic
        get() = mTrials[currTrial]
        set(value) {
            mTrials[currTrial] = value
        }

    // used in Quest managers to set the first value
    open fun getStimulus():Long{
        return mTrial.stim_value
    }

    open fun setResponse(result:Int, elapsedms:Int, extra_text:String = ""){
        mTrial.setResponse(result, elapsedms, extra_text)
    }

    protected fun setTrialsID(){  mTrials.mapIndexed { index, trialBasic -> trialBasic.id = index } }

    abstract fun getNewTrial():Any

}

