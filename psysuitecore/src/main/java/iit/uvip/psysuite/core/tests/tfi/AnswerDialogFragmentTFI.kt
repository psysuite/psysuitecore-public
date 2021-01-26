package iit.uvip.psysuite.core.tests.tfi

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
import kotlinx.android.synthetic.main.fragment_answer_tfi.*
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import java.lang.Math.random
import java.util.*


class AnswerDialogFragmentTFI: DialogFragment()
{
    val LOG_TAG = AnswerDialogFragmentTFI::class.java.simpleName

    private var isDebug:Boolean = false

    lateinit var onsetDate:Date
    private val mHandler:Handler = Handler()

    private var tts: SpeechManager?                     = null

    companion object {
        fun newInstance(title: String, speechManager: SpeechManager): AnswerDialogFragmentTFI {
            val frag = AnswerDialogFragmentTFI()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)
            frag.tts = speechManager

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_answer_tfi, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch arguments from bundle and set title
        val title           = requireArguments().getString("title", "Enter Name")
        txt_trials.text     = "trial " +  (requireArguments().getInt("trial_id", 0) + 1).toString() + " of " + requireArguments().getInt("tot_trials", 0)
        txt_question.text   = requireArguments().getString("question", "Enter Name")
        txt_debug.text      = requireArguments().getString("debugInfo")
        isDebug             = requireArguments().getBoolean("isDebug", false)

        dialog?.setTitle(title)

        radioGroupAudio.check(radioGroupAudio.getChildAt(0).id)
        radioGroupTactile.check(radioGroupTactile.getChildAt(0).id)
        radioGroupVisual.check(radioGroupVisual.getChildAt(0).id)

        onsetDate           = Date()

        if(isDebug){
            mHandler.postDelayed({
                if(random() < 0.5)  sendResult("1,1,1", 100, TestBasic.EVENT_ANSWER_GIVEN)
                else                sendResult("2,1,0", 100, TestBasic.EVENT_ANSWER_GIVEN)
            }, 3000L)
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

            val elapsedms = getTimeDifference(onsetDate)
            val res = getRadioSelection()

            when(res){
                "0,0,0" ->  showToast(getText(R.string.tfi_warning_null_answer).toString(), requireContext())
                ""      ->  return@setOnClickListener
                else    ->  sendResult(res, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
            }
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

    private fun sendResult(response: String, elapsedTime: Int, response_id: Int) {
        if (targetFragment == null) return

        val intent = TestFragment.newIntent(response, elapsedTime, response_id)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    private fun getRadioSelection():String{

        var res = ""
        when(radioGroupAudio.checkedRadioButtonId != -1) {
            true -> res = radioGroupAudio.indexOfChild(radioGroupAudio.findViewById(radioGroupAudio.checkedRadioButtonId)).toString()
            false -> {
                showToast("Seleziona un'opzione per l\'audio", requireContext())
                return ""
            }
        }

        when(radioGroupTactile.checkedRadioButtonId != -1) {
            true -> res = "$res,${radioGroupTactile.indexOfChild(radioGroupTactile.findViewById(radioGroupTactile.checkedRadioButtonId))}"
            false -> {
                showToast("Seleziona un'opzione per il tatto", requireContext())
                return ""
            }
        }

        when(radioGroupVisual.checkedRadioButtonId != -1) {
            true -> res = "$res,${radioGroupVisual.indexOfChild(radioGroupVisual.findViewById(radioGroupVisual.checkedRadioButtonId))}"
            false -> {
                showToast("Seleziona un'opzione per il visivo", requireContext())
                return ""
            }
        }
        return res
    }
}