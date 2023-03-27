package iit.uvip.psysuite.core.ui.fragments.answers

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

import java.lang.Math.random
import java.util.*

import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.Fragment2afcAnswerBinding
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.ui.fragments.TestFragment

import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast


open class TwoAFCAnswerDialogFragment: DialogFragment()
{
    open val LOG_TAG = TwoAFCAnswerDialogFragment::class.java.simpleName

    private lateinit var binding: Fragment2afcAnswerBinding
    protected lateinit var mView:View

    protected var isDebug:Boolean           = false
    protected var isInstructions:Boolean    = false

    protected var canRepeat:Int             = TestBasic.TEST_SWITCH_DISABLED

    protected var showResult:Int            = TestBasic.TEST_SWITCH_DISABLED
    protected var correctAnswerId:Int         = 0
    protected var mQuestion:String          = ""
    protected var mAnswers:ArrayList<String> = arrayListOf()

    protected var onsetDate:Date            = Date()
    private val mHandler:Handler            = Handler()

    protected var tts: SpeechManager?       = null

    companion object {
        fun newInstance(title: String, speechManager: SpeechManager): TwoAFCAnswerDialogFragment {
            val frag = TwoAFCAnswerDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            frag.tts = speechManager
            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_2afc_answer, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Fragment2afcAnswerBinding.bind(mView)

        // Fetch arguments from bundle and set title
        with(requireArguments()){
            binding.txtTrials.text  = "trial ${(getInt("trial_id", 0) + 1)} di ${getInt("tot_trials", 0)}"
            mQuestion               = getString("question", "Enter Name")
            mAnswers                = getStringArrayList("answers") ?: arrayListOf<String>()
            binding.txtDebug.text   = getString("debugInfo")
            isDebug                 = getBoolean("isDebug", false)
            isInstructions          = (targetRequestCode == TestFragment.TRG_REQ_CODE_INSTRUCTIONS)

            canRepeat               = getInt("can_repeat_trial", TestBasic.TEST_SWITCH_DISABLED)

            showResult              = getInt("show_result", TestBasic.TEST_SWITCH_DISABLED)
            if(showResult == TestBasic.TEST_SWITCH_ENABLED && mAnswers.size > 0)
                correctAnswerId     = getInt("correct_answer", 0)

            val title               = getString("title", "Enter Name")
            dialog?.setTitle(title)
        }
        binding.imgvResult.visibility   = View.INVISIBLE

        binding.txtQuestion.text       = mQuestion

        if (mAnswers.isNotEmpty()) {
            binding.rbA0.text = mAnswers[0]
            binding.rbA1.text = mAnswers[1]
        }

        clear()

        if(isDebug){
            mHandler.postDelayed({
                if(random() < 0.5)  sendResult(0, 100, TestBasic.EVENT_ANSWER_GIVEN)
                else                sendResult(1, 100, TestBasic.EVENT_ANSWER_GIVEN)
            }, 1000L)
        }
    }

    override fun onResume() {
        // Get existing layout params for the window
        val params = dialog?.window!!.attributes
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window!!.attributes = params as WindowManager.LayoutParams

        super.onResume()

        if(canRepeat == TestBasic.TEST_SWITCH_ENABLED){
            binding.btClear.visibility     = View.VISIBLE
            binding.btClear.setOnClickListener{     sendResult(-1, 0, TestBasic.EVENT_TRIAL_REPEAT) }
        }
        else{
            binding.btClear.visibility     = View.INVISIBLE
            binding.btClear.setOnClickListener(null)
        }

        binding.btConfirm.setOnClickListener{   confirm()}
        binding.btAbortTest.setOnClickListener{ abort() }
    }

    protected open fun confirm(){
        if(binding.radioGroupAudio.checkedRadioButtonId != -1)
            checkResult(binding.radioGroupAudio.indexOfChild(binding.radioGroupAudio.findViewById(binding.radioGroupAudio.checkedRadioButtonId)))
        else
            showToast("Seleziona un'opzione", requireContext())
    }

    protected fun abort(){
        mHandler.removeCallbacksAndMessages(null)
        sendResult(-1, 0, TestBasic.EVENT_TRIAL_ABORT)
        dismiss()
    }

    private fun checkResult(curr_answer:Int){

        val elapsedms = getTimeDifference(onsetDate)
        if(showResult == TestBasic.TEST_SWITCH_ENABLED) {
            if (curr_answer == correctAnswerId)     binding.imgvResult.setImageResource(R.drawable.success_icon)
            else                                    binding.imgvResult.setImageResource(R.drawable.failure_icon)

            binding.btClear.visibility = View.INVISIBLE
            binding.btConfirm.visibility = View.INVISIBLE
            binding.imgvResult.visibility = View.VISIBLE
            mHandler.postDelayed({
                binding.imgvResult.visibility = View.INVISIBLE
                sendResult(curr_answer, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
            }, 1000L)
        }
        else    sendResult(curr_answer, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
    }

    // last point of the exit/dismiss procedure
    protected fun sendResult(response: Int, elapsedTime: Int, response_id: Int) {
        if (targetFragment == null) return

        tts?.stop()

        val intent = TestFragment.newIntent(response, elapsedTime, response_id)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    private fun clear(){
        binding.radioGroupAudio.clearCheck()
        binding.rbA0.isChecked = false
        binding.rbA1.isChecked = false
    }
}