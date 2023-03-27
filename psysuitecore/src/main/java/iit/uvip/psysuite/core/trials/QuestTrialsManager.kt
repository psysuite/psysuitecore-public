package iit.uvip.psysuite.core.trials

import com.chaquo.python.PyObject
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.TrialsManager
import iit.uvip.psysuite.python.SPython
import iit.uvip.psysuite.quest.QuestWrapper


// this class manages quest-driven tasks
// trials are defined in the test's class and passed here with their dynamic (subjected to quest) dimension set to zero
// user must pass a QuestWrapper instance defining the python module/class governing the task.
// this class must have:
//      constructor with two parameters:
//          1- quest parameters
//          2- task parameters
//      two methods:
//          1- get = retrieve the dynamic value
//          2- set = set subject's answer
//
class QuestTrialsManager(trials:MutableList<TrialBasic>, private val questWrapper:QuestWrapper):TrialsManager(TestBasic.TEST_TRMAN_QUEST, trials) {

    val sPy:SPython = SPython.getInstance(null)     // singleton already initialized in TestFragment, here I dont'need a Context

    private val wrapperClass: PyObject

    init {
        val questparams_dict   = sPy.class2dict(questWrapper.qparams)
        val taskparams_dict    = sPy.class2dict(questWrapper.params)

        wrapperClass = sPy.instanciate(questWrapper.module, questWrapper.classname, questparams_dict, taskparams_dict)

        val firstvalue      = wrapperClass.callAttr("get").toFloat()

        mTrial.updateTrial(firstvalue)
    }

    override fun setResponse(result: Int, elapsedms: Int, extra_text:String){
        mTrial.setResponse(result, elapsedms)
        wrapperClass.callAttr("set", result)
    }

    // get new value, get next trial, update with new value and return it
    override fun getNewTrial(): TrialBasic {
        val newvalue = wrapperClass.callAttr("get").toFloat()
        currTrial++
        mTrial.updateTrial(newvalue)
        return mTrial
    }
}