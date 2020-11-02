package iit.uvip.psysuite.core.fragments

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TestBasic
import kotlinx.android.synthetic.main.fragment_answer.*
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.ui.showToast
import java.lang.Math.random
import java.util.*


class AnswerDialogFragment: DialogFragment()
{
    val LOG_TAG = AnswerDialogFragment::class.java.simpleName

    private var isDebug:Boolean = false

    private var showResult:Boolean = false
    private var correctAnswer:String = ""

    private lateinit var mAnswers:ArrayList<String>
    lateinit var onsetDate:Date
    private val mHandler:Handler = Handler()

    companion object {
        fun newInstance(title: String): AnswerDialogFragment {
            val frag = AnswerDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_answer, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch arguments from bundle and set title
        val title           = requireArguments().getString("title", "Enter Name")
        val str_trial       = "trial " +  (requireArguments().getInt("trial_id", 0) + 1).toString() + " di " + requireArguments().getInt("tot_trials", 0)
        val question        = requireArguments().getString("question", "Enter Name")
        val answers         = requireArguments().getStringArrayList("answers")
        val debug_info      = requireArguments().getString("debug")
        isDebug             = requireArguments().getBoolean("isDebug", false)

        showResult          = requireArguments().getBoolean("show_result", false)
        if(showResult)
            correctAnswer   = requireArguments().getString("correct_answer", answers?.get(0))

        dialog?.setTitle(title)
        imgvResult.visibility   = View.INVISIBLE
        bt_clear.visibility     = View.VISIBLE

        txt_trials.text     = str_trial
        txt_question.text   = question
        txt_debug.text      = debug_info

        if (answers != null)
            if (answers.isNotEmpty()) {
                mAnswers = answers
                rb_a_0.text = mAnswers[0]
                rb_a_1.text = mAnswers[1]
            }

        onsetDate           = Date()

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

            if(radioGroupAudio.checkedRadioButtonId != -1)
                checkResult(mAnswers[radioGroupAudio.indexOfChild(radioGroupAudio.findViewById(radioGroupAudio.checkedRadioButtonId))])
            else
                showToast("Seleziona un'opzione", requireContext())
        }

        bt_clear.setOnClickListener{
            sendResult("", 0, TestBasic.EVENT_TRIAL_REPEAT)
        }

        bt_abort_test.setOnClickListener{
            mHandler.removeCallbacksAndMessages(null)
            sendResult("", 0, TestBasic.EVENT_TRIAL_ABORT)
            dismiss()
        }
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

    private fun sendResult(response: String, elapsedTime: Int, response_id: Int) {
        if (targetFragment == null) return

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