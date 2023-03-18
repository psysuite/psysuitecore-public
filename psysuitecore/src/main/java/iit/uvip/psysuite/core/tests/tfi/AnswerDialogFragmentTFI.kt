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
import iit.uvip.psysuite.core.databinding.FragmentAnswerTfiBinding
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.ui.fragments.TestFragment
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast
import java.lang.Math.random
import java.util.*


class AnswerDialogFragmentTFI: DialogFragment()
{
    val LOG_TAG = AnswerDialogFragmentTFI::class.java.simpleName
    
    private lateinit var binding:FragmentAnswerTfiBinding
    private lateinit var mView:View

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_answer_tfi, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnswerTfiBinding.bind(mView)

        // Fetch arguments from bundle and set title
        val title           = requireArguments().getString("title", "Enter Name")
        binding.txtTrials.text     = "trial " +  (requireArguments().getInt("trial_id", 0) + 1).toString() + " of " + requireArguments().getInt("tot_trials", 0)
        binding.txtQuestion.text   = requireArguments().getString("question", "Enter Name")
        binding.txtDebug.text      = requireArguments().getString("debugInfo")
        isDebug             = requireArguments().getBoolean("isDebug", false)

        dialog?.setTitle(title)

        binding.radioGroupAudio.check(binding.radioGroupAudio.getChildAt(0).id)
        binding.radioGroupTactile.check(binding.radioGroupTactile.getChildAt(0).id)
        binding.radioGroupVisual.check(binding.radioGroupVisual.getChildAt(0).id)

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

        binding.btConfirm.setOnClickListener{

            val elapsedms = getTimeDifference(onsetDate)
            val res = getRadioSelection()

            when(res){
                "0,0,0" ->  showToast(getText(R.string.tfi_warning_null_answer).toString(), requireContext())
                ""      ->  return@setOnClickListener
                else    ->  sendResult(res, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
            }
        }

        binding.btClear.setOnClickListener{
            sendResult("", 0, TestBasic.EVENT_TRIAL_REPEAT)
        }

        binding.btAbortTest.setOnClickListener{
            mHandler.removeCallbacksAndMessages(null)
            sendResult("", 0, TestBasic.EVENT_TRIAL_ABORT)
            dismiss()
        }
    }

    private fun sendResult(response: String, elapsedTime: Int, response_id: Int) {
        if (targetFragment == null) return

        val intent = TestFragment.newIntent(-1, elapsedTime, response_id, response)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    private fun getRadioSelection():String{

        var res = ""
        when(binding.radioGroupAudio.checkedRadioButtonId != -1) {
            true -> res = binding.radioGroupAudio.indexOfChild(binding.radioGroupAudio.findViewById(binding.radioGroupAudio.checkedRadioButtonId)).toString()
            false -> {
                showToast("Seleziona un'opzione per l\'audio", requireContext())
                return ""
            }
        }

        when(binding.radioGroupTactile.checkedRadioButtonId != -1) {
            true -> res = "$res,${binding.radioGroupTactile.indexOfChild(binding.radioGroupTactile.findViewById(binding.radioGroupTactile.checkedRadioButtonId))}"
            false -> {
                showToast("Seleziona un'opzione per il tatto", requireContext())
                return ""
            }
        }

        when(binding.radioGroupVisual.checkedRadioButtonId != -1) {
            true -> res = "$res,${binding.radioGroupVisual.indexOfChild(binding.radioGroupVisual.findViewById(binding.radioGroupVisual.checkedRadioButtonId))}"
            false -> {
                showToast("Seleziona un'opzione per il visivo", requireContext())
                return ""
            }
        }
        return res
    }
}