package iit.uvip.psysuite.core.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TestBasic
import kotlinx.android.synthetic.main.fragment_answer.*
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.accessory.showToast
import java.util.*


class AnswerDialogFragment: DialogFragment()
{
    val LOG_TAG = AnswerDialogFragment::class.java.simpleName

    lateinit var onsetDate:Date

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
        val title       = requireArguments().getString("title", "Enter Name")
        val str_trial   = "trial " +  (requireArguments().getInt("trial_id", 0) + 1).toString() + " di " + requireArguments().getInt("tot_trials", 0)
        val question    = requireArguments().getString("question", "Enter Name")
        val answers = requireArguments().getStringArrayList("answers")


        dialog?.setTitle(title)

        txt_trials.text     = str_trial
        txt_question.text   = question

        if (answers != null)
            if (answers.isNotEmpty()) {
                rb1.text = answers[0]
                rb3.text = answers[1]
            }

        onsetDate           = Date()
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

            val elapsedms = getTimeDifference(onsetDate)
            when(radioGroupIntervals.checkedRadioButtonId != -1) {
                true -> {
                    val id                      = radioGroupIntervals.checkedRadioButtonId
                    val radioButton:RadioButton = radioGroupIntervals.findViewById(id)
                    val radioId                 = radioGroupIntervals.indexOfChild(radioButton)      // val btn = radioGroup.getChildAt(radioId) as RadioButton

                    sendResult(radioId.toString(), elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
                }
                false -> showToast("Seleziona un'opzione", requireContext())
            }
        }

        bt_clear.setOnClickListener{
            sendResult("", 0, TestBasic.EVENT_TRIAL_REPEAT)
        }

        bt_abort_test.setOnClickListener{
            sendResult("", 0, TestBasic.EVENT_TRIAL_ABORT)
            dismiss()
        }
    }

    private fun sendResult(response: String, elapsedTime: Int, response_id: Int) {
        if (targetFragment == null) return

        val intent = TestFragment.newIntent(response, elapsedTime, response_id)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }
}