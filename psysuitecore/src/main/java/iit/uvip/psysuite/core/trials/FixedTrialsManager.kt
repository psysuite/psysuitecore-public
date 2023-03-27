package iit.uvip.psysuite.core.trials

import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.TrialsManager

// this class manage all classic tasks/conditions using predetermined trials sequence
// they are defined in the test's class and passed here with initTrials
class FixedTrialsManager(trials:MutableList<TrialBasic>):
    TrialsManager(TestBasic.TEST_TRMAN_FIXED, trials) {

    init {
        if(mTrials.isEmpty())
            throw Exception("ERROR in TrialsManager. given trials list is empty")
        setTrialsID()
    }


    override fun getNewTrial(): TrialBasic {
        currTrial++
        return mTrial
    }

    private fun setTrialsID(){  mTrials.mapIndexed { index, trialBasic -> trialBasic.id = index } }
}