package org.albaspazio.psysuite.trials

import org.albaspazio.psysuite.tests.TestBasic
import org.albaspazio.psysuite.tests.TrialBasic

// this class manage all classic tasks/conditions using predetermined trials sequence
// they are defined in the test's class and passed here with initTrials
class FixedTrialsManager(trials:MutableList<TrialBasic>, training_trials:MutableList<TrialBasic> = mutableListOf<TrialBasic>()):
    TrialsManager(TestBasic.TEST_TRMAN_FIXED, trials, training_trials) {

    override fun getNewTrial(): TrialBasic {
        currTrialID++
        return mTrial
    }
}