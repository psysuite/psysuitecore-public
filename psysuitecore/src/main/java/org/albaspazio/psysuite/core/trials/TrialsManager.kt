package org.albaspazio.psysuite.core.trials

import org.albaspazio.psysuite.tests.TrialBasic

/*
type:
- 0:    predetermined trials
- 1:    quest trials
- ?:    other type of adaptative algorithms
@ init: checks if training trials are available, otherwise use test trials
 */

abstract class TrialsManager(
    val type: Int = 0, 
    private val testTrials: List<TrialBasic>,
    private val trainingTrials: List<TrialBasic> = emptyList()
) {

    companion object {
        val ADAPTIVE_VALUE:Float = -99999999.9F
    }

    var currTrialID: Int = -1       // 0-based index of current trial
    var isTrainingPhase: Boolean = false

    private var currentTrialsList: List<TrialBasic> = emptyList()

    val nTrials: Int
        get() = currentTrialsList.size

    val mTrial: TrialBasic
        get() = currentTrialsList[currTrialID]

    val mPrevTrial: TrialBasic?
        get() = if (currTrialID == 0) null
                else currentTrialsList[currTrialID - 1]

    val mNextTrial: TrialBasic?
        get() = if (currTrialID == currentTrialsList.size-1) null
                else currentTrialsList[currTrialID + 1]


    init {
        if (testTrials.isEmpty())
            throw Exception("ERROR in TrialsManager. Test trials list is empty")

        // Initialize with training trials if available, otherwise set test trials
        if (trainingTrials.isEmpty())   setTest()
        else                            setTraining()
    }

    fun setTraining() {
        isTrainingPhase     = true
        currentTrialsList   = trainingTrials
        resetTrials()
    }

    fun setTest() {
        isTrainingPhase     = false
        currentTrialsList   = testTrials
        resetTrials()
    }

    fun resetTrials() {
        currTrialID = -1
        setTrialsID()
    }

    val isLastTrainingTrial:Boolean
        get() = (currTrialID == currentTrialsList.size - 1) && isTrainingPhase

    protected fun setTrialsID() {
        currentTrialsList.forEachIndexed { index, trial -> trial.id = index }
    }

    // used in Adaptive managers to set the first value
    open fun getStimulus():Long {
        return mTrial.stim_value
    }

    open fun setResponse(result:Int, elapsedms:Long = -1L, extra_text:String = "") {
        mTrial.setResponse(result, elapsedms, mPrevTrial, extra_text)
    }

    abstract fun getNewTrial():TrialBasic

}

