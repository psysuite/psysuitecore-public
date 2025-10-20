package iit.uvip.psysuite.core.trials

import android.util.Log
import com.chaquo.python.PyObject
import org.albaspazio.psysuite.adaptive.AdaptiveWrapper
import iit.uvip.psysuite.core.tests.TestBasic
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
open class AdaptiveTrialsManager(trials:MutableList<TrialBasic>, adaptiveWrapper: AdaptiveWrapper, training_trials:MutableList<TrialBasic> = mutableListOf<TrialBasic>())
            :TrialsManager(TestBasic.TEST_TRMAN_ADAPTIVE, trials, training_trials) {

    private val sPy:SPython = SPython.getInstance(null)     // singleton already initialized in TestFragment, here I dont'need a Context
    private val wrapperClass:PyObject
    private val range:Float = adaptiveWrapper.params.range

    init {

        val adaptparams_dict    = sPy.class2dict(adaptiveWrapper.qparams)
        val taskparams_dict     = sPy.class2dict(adaptiveWrapper.params)

        wrapperClass            = sPy.instanciate(adaptiveWrapper.module, adaptiveWrapper.classname, adaptparams_dict, taskparams_dict)
        wrapperClass.callAttr("get").toFloat()  // to init the model, in case of mixed design,
                                                // if the first trial is not adaptative, when i set its result it gives an error
    }

        /**
     * Sets the response for the current trial. Even If the trial is not adaptive, it updates the model with the response / magnitude pair.
     * TestBasic::OnAnswerGiven ->TrialsManager::setResponse
     * @param result The result of the trial.
     * @param elapsedms The time taken to complete the trial.
     * @param extra_text Additional text or information related to the response.
     */
    override fun setResponse(result: Int, elapsedms: Long, extra_text: String) {
        mTrial.setResponse(result, elapsedms, mPrevTrial, extra_text)
        wrapperClass.callAttr("set", mTrial.success, mTrial.magnitude)
    }

    /**
     * Increment the current trial and get a new stimulus value.
     * This method is responsible for updating the current trial, retrieving a new stimulus value, and returning the updated trial.
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
     * Get the next stimulus value. If the trial is adaptive, get it from the adaptive model and update the current trial.
     *
     * @return The next stimulus value.
     */
    override fun getStimulus():Long{
        return  if(mTrial.isADA) {
                    var magn = wrapperClass.callAttr("get").toFloat()
                    magn = magn.coerceAtMost(range)
                    magn = magn.coerceAtLeast(0.0F)
                    mTrial.setupTrial(magn)
                }
                else    mTrial.stim_value
    }
}