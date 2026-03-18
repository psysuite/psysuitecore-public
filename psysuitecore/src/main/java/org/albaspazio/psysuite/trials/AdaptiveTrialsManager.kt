package org.albaspazio.psysuite.trials

import android.util.Log
import com.chaquo.python.PyObject
import kotlinx.coroutines.*
import org.albaspazio.psysuite.adaptive.ado.ADOWrapper
import org.albaspazio.psysuite.tests.TestBasic
import org.albaspazio.psysuite.tests.TrialBasic
import org.albaspazio.psysuite.python.SPython

// this class manages adaptive tasks
// trials are defined in the test's class and passed here with their dynamic (subjected to quest) dimension set to ADAPTIVE_VALUE
// user must pass a AdaptiveWrapper instance defining the python module/class governing the task.
// this class must have:
//      constructor with two parameters:
//          1- quest parameters
//          2- task parameters
//      two methods:
//          1- get = retrieve the dynamic value
//          2- set = set subject's answer
//
open class AdaptiveTrialsManager(trials: MutableList<TrialBasic>,
                                 training_trials: MutableList<TrialBasic> = mutableListOf<TrialBasic>())
                                : TrialsManager(TestBasic.TEST_TRMAN_ADAPTIVE, trials, training_trials) {

    private val sPy: SPython = SPython.getInstance(null)        // singleton already initialized in TestFragment
    private val wrapperCache = mutableMapOf<ADOWrapper, PyObject>()  // Cache PyObject wrappers per ADOWrapper instance
    
    // Coroutine scope for background operations
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun getOrCreateWrapper(adoWrapper: ADOWrapper): PyObject {
        return wrapperCache.getOrPut(adoWrapper) {
            // This block only executes if adoWrapper is NOT in cache
            val adaptparams_dict    = sPy.class2dict(adoWrapper.qparams)
            val taskparams_dict     = sPy.class2dict(adoWrapper.params)
            val wrapper             = sPy.instanciate(adoWrapper.module, adoWrapper.classname, adaptparams_dict, taskparams_dict)
            wrapper.callAttr("get").toFloat()  // Initialize
            wrapper                                 // Return value stored in cache
        }
    }

    /**
     * Sets the response for the current trial asynchronously. Even If the trial is not adaptive, it updates the model with the response / magnitude pair.
     * The Python call is executed on a background thread to avoid blocking the UI.
     * TestBasic::OnAnswerGiven -> TrialsManager::setResponse
     * @param result The result of the trial.
     * @param elapsedms The time taken to complete the trial.
     * @param extra_text Additional text or information related to the response.
     */
    override fun setResponse(result: Int, elapsedms: Long, extra_text: String) {
        mTrial.setResponse(result, elapsedms, mPrevTrial, extra_text)
        
        // Run Python call asynchronously on background thread (fire and forget)
        mTrial.adoWrapper?.let { ado ->
            ioScope.launch {
                try {
                    val wrapperClass = getOrCreateWrapper(ado)
                    wrapperClass.callAttr("set", mTrial.getAdoUpdatingPropr(), mTrial.magnitude)
                } catch (e: Exception) {
                    Log.e("AdaptiveTrialsManager", "Error setting ADO response: ${e.message}")
                }
            }
        }
    }

    /**
     * Increment the current trial and get a new stimulus value.
     * This method is responsible for updating the current trial, retrieving a new stimulus value and returning the updated trial.
     *
     * @return The updated trial with a new stimulus value.
     */
    override fun getNewTrial(): TrialBasic {

        currTrialID++
        val prev_resp = mPrevTrial?.user_answer ?: -1
        val prev_succ = mPrevTrial?.success ?: true

        val newvalue = getStimulus()
        Log.d("QUEST_VALUE", "${newvalue} , prev resp: $prev_resp , prev succ: $prev_succ")

        return mTrial
    }

    /**
     * Get the next stimulus value synchronously but efficiently.
     * If the trial is adaptive, get it from the adaptive model and update the current trial.
     * 
     * NOTE: This is called from doNextTrial() which is on the main thread.
     * We use runBlocking with IO dispatcher to move Python call off main thread.
     * 
     * @return The next stimulus value.
     */
    override fun getStimulus(): Long {
        return mTrial.adoWrapper?.let { ado ->
            // Use runBlocking with IO dispatcher to run Python call off main thread
            runBlocking(Dispatchers.IO) {
                try {
                    val wrapperClass    = getOrCreateWrapper(ado)
                    val magn            = wrapperClass.callAttr("get").toFloat().coerceIn(0.0f, ado.params.range)
                    Log.d("AdaptiveTrialsManager", "calculated next adaptive stimulus: $magn")
                    mTrial.initTrial(magn) // setupTrial updates and returns the new value
                } catch (e: Exception) {
                    Log.e("AdaptiveTrialsManager", "Error getting adaptive stimulus: ${e.message}")
                    mTrial.stim_value  // Fallback to current value on error
                }
            }
        } ?: mTrial.stim_value  // If adoWrapper was null, return the default stimulus value
    }

    /**
     * Clean up coroutine scope when manager is no longer needed.
     * Call this from TestBasic.terminateTest() or similar cleanup method.
     */
    fun cleanup() {
        ioScope.cancel()
    }
}
