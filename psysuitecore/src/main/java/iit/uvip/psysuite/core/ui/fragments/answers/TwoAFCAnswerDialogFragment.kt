package iit.uvip.psysuite.core.ui.fragments.answers

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.ui.fragments.TestFragment
import kotlinx.android.synthetic.main.fragment_2afc_answer.*
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import java.lang.Math.random
import java.util.*


open class TwoAFCAnswerDialogFragment: DialogFragment()
{
    open val LOG_TAG = TwoAFCAnswerDialogFragment::class.java.simpleName

    protected var isDebug:Boolean           = false
    protected var isInstructions:Boolean    = false

    protected var showResult:Boolean = false
    private var correctAnswer:String = ""
    protected var mQuestion:String              = ""
    protected var mAnswers:ArrayList<String>    = arrayListOf()

    protected var onsetDate:Date                = Date()
    private val mHandler:Handler                = Handler()

    protected var tts: SpeechManager?                     = null

    companion object {
        fun newInstance(title: String, speechManager: SpeechManager): TwoAFCAnswerDialogFragment {
            val frag = TwoAFCAnswerDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)
            frag.tts = speechManager

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_2afc_answer, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch arguments from bundle and set title
        val title           = requireArguments().getString("title", "Enter Name")
        txt_trials.text     = "trial " +  (requireArguments().getInt("trial_id", 0) + 1).toString() + " di " + requireArguments().getInt("tot_trials", 0)
        mQuestion           = requireArguments().getString("question", "Enter Name")
        mAnswers            = requireArguments().getStringArrayList("answers") ?: arrayListOf<String>()
        txt_debug.text      = requireArguments().getString("debugInfo")
        isDebug             = requireArguments().getBoolean("isDebug", false)
        isInstructions      = (targetRequestCode == TestFragment.TRG_REQ_CODE_INSTRUCTIONS)

        showResult          = requireArguments().getBoolean("show_result", false)
        if(showResult && mAnswers.size > 0)
            correctAnswer   = requireArguments().getString("correct_answer", mAnswers[0])

        dialog?.setTitle(title)
        imgvResult.visibility   = View.INVISIBLE
        bt_clear.visibility     = View.VISIBLE

        txt_question.text   = mQuestion

        if (mAnswers.isNotEmpty()) {
            rb_a_0.text = mAnswers[0]
            rb_a_1.text = mAnswers[1]
        }

        clear()

        if(isDebug){
            mHandler.postDelayed({
                if(random() < 0.5)  sendResult(mAnswers[0], 100, TestBasic.EVENT_ANSWER_GIVEN)
                else                sendResult(mAnswers[1], 100, TestBasic.EVENT_ANSWER_GIVEN)
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

        bt_confirm.setOnClickListener{
            confirm()
        }

        bt_clear.setOnClickListener{
            sendResult("", 0, TestBasic.EVENT_TRIAL_REPEAT)
        }

        bt_abort_test.setOnClickListener{
            abort()
        }
    }

    protected open fun confirm(){
        if(radioGroupAudio.checkedRadioButtonId != -1)
            checkResult(mAnswers[radioGroupAudio.indexOfChild(radioGroupAudio.findViewById(radioGroupAudio.checkedRadioButtonId))])
        else
            showToast("Seleziona un'opzione", requireContext())
    }

    protected fun abort(){
        mHandler.removeCallbacksAndMessages(null)
        sendResult("", 0, TestBasic.EVENT_TRIAL_ABORT)
        dismiss()
    }

    private fun checkResult(curr_answer:String){
        val elapsedms = getTimeDifference(onsetDate)
        if(showResult) {
            if (curr_answer == correctAnswer)   imgvResult.setImageResource(R.drawable.success_icon)
            else                                imgvResult.setImageResource(R.drawable.failure_icon)

            bt_clear.visibility = View.INVISIBLE
            bt_confirm.visibility = View.INVISIBLE
            imgvResult.visibility = View.VISIBLE
            mHandler.postDelayed({
                imgvResult.visibility = View.INVISIBLE
                sendResult(curr_answer, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
            }, 1000L)
        }
        else    sendResult(curr_answer, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
    }

    // last point of the exit/dismiss procedure
    protected fun sendResult(response: String, elapsedTime: Int, response_id: Int) {
        if (targetFragment == null) return

        tts?.stop()

        val intent = TestFragment.newIntent(response, elapsedTime, response_id)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    private fun clear(){
        radioGroupAudio.clearCheck()
        rb_a_0.isChecked = false
        rb_a_1.isChecked = false
    }
}