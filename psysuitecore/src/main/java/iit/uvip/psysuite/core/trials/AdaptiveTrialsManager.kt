package iit.uvip.psysuite.core.trials

import android.util.Log
import com.chaquo.python.PyObject
import iit.uvip.psysuite.adaptive.AdaptiveWrapper
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.python.SPython


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
open class AdaptiveTrialsManager(trials:MutableList<TrialBasic>, adaptiveWrapper: AdaptiveWrapper):TrialsManager(TestBasic.TEST_TRMAN_ADAPTIVE, trials) {

    private val sPy:SPython = SPython.getInstance(null)     // singleton already initialized in TestFragment, here I dont'need a Context

    protected val wrapperClass:PyObject

    val range:Float

    init {

        range                   = adaptiveWrapper.params.range

        val adaptparams_dict    = sPy.class2dict(adaptiveWrapper.qparams)
        val taskparams_dict     = sPy.class2dict(adaptiveWrapper.params)

        wrapperClass            = sPy.instanciate(adaptiveWrapper.module, adaptiveWrapper.classname, adaptparams_dict, taskparams_dict)
    }

    // increment trial, get new value, update with new value and return it
    override fun getNewTrial():TrialBasic {
        val prev_resp = mTrial.user_answer
        val prev_succ = mTrial.success

        currTrial++

        val newvalue = getStimulus()

        Log.d("QUEST_VALUE", "${newvalue} , prev resp: $prev_resp , prev succ: $prev_succ")

        return mTrial
    }

    // get next stim value from adaptive model and update current trial
    override fun getStimulus():Long{
        return  if(mTrial.isADA) {
                    var magn = wrapperClass.callAttr("get").toFloat()
                    magn = magn.coerceAtMost(range)
                    magn = magn.coerceAtLeast(0.0F)
                    mTrial.updateTrial(magn)
                }
                else    mTrial.stim_value
    }

    override fun setResponse(result: Int, elapsedms: Int, extra_text:String){
        mTrial.setResponse(result, elapsedms)   // it updates mTrial.success
        if(mTrial.isADA)   wrapperClass.callAttr("set", mTrial.success, mTrial.magnitude)
    }
}