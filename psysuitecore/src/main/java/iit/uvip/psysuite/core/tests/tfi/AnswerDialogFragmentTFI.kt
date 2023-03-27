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

    private var isDebug:Boolean           = false
    private var isInstructions:Boolean    = false

    private var canRepeat:Int             = TestBasic.TEST_SWITCH_DISABLED
    private var showResult:Int            = TestBasic.TEST_SWITCH_DISABLED
    private var mQuestion:String          = ""

    private val ANSWER_NOT_VALID            = -1  // combination not valid
    private val ANSWER_EMPTY                = -2  // "0,0,0"
    private val AUDIO_ANSWER_NOT_GIVEN      = -3  // one of the radio not selected
    private val TACTILE_ANSWER_NOT_GIVEN    = -4  // one of the radio not selected
    private val VISUAL_ANSWER_NOT_GIVEN     = -5  // one of the radio not selected

    private lateinit var mAnswers: ArrayList<String>
    private var correctAnswerId: Int = -1

    lateinit var onsetDate:Date
    private val mHandler:Handler = Handler()

    private var tts: SpeechManager?       = null

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mView = inflater.inflate(R.layout.fragment_answer_tfi, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnswerTfiBinding.bind(mView)

        // Fetch arguments from bundle and set title
        with(requireArguments()){
            val title                   = getString("title", "Enter Name")
            dialog?.setTitle(title)

            binding.txtTrials.text      = "trial " +  (getInt("trial_id", 0) + 1).toString() + " of " + getInt("tot_trials", 0)
            mQuestion                   = getString("question", "Enter Name")
            mAnswers                    = getStringArrayList("answers") ?: arrayListOf()
            correctAnswerId             = getInt("correct_answer", 0)

            binding.txtDebug.text       = getString("debugInfo")
            isDebug                     = getBoolean("isDebug", false)

            isInstructions              = (targetRequestCode == TestFragment.TRG_REQ_CODE_INSTRUCTIONS)

            canRepeat                   = getInt("can_repeat_trial", TestBasic.TEST_SWITCH_DISABLED)
            showResult                  = getInt("show_result", TestBasic.TEST_SWITCH_DISABLED)
        }

        binding.txtQuestion.text        = mQuestion
        binding.imgvResult.visibility   = View.INVISIBLE

        binding.radioGroupAudio.check(binding.radioGroupAudio.getChildAt(0).id)
        binding.radioGroupTactile.check(binding.radioGroupTactile.getChildAt(0).id)
        binding.radioGroupVisual.check(binding.radioGroupVisual.getChildAt(0).id)

        onsetDate           = Date()

        if(isDebug){
            mHandler.postDelayed({
                if(random() < 0.5)  sendResult(24, 100, TestBasic.EVENT_ANSWER_GIVEN)
                else                sendResult(9, 100, TestBasic.EVENT_ANSWER_GIVEN)
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

        binding.btConfirm.setOnClickListener{   confirm()   }

        if(canRepeat == TestBasic.TEST_SWITCH_ENABLED){
            binding.btClear.visibility     = View.VISIBLE
            binding.btClear.setOnClickListener{     sendResult(ANSWER_NOT_VALID, 0, TestBasic.EVENT_TRIAL_REPEAT) }
        }
        else{
            binding.btClear.visibility     = View.INVISIBLE
            binding.btClear.setOnClickListener(null)
        }

        binding.btAbortTest.setOnClickListener{
            mHandler.removeCallbacksAndMessages(null)
            sendResult(ANSWER_NOT_VALID, 0, TestBasic.EVENT_TRIAL_ABORT)
            dismiss()
        }
    }

    // called by btConfirm.setOnClickListener
    private fun confirm(){

        when(val res = getRadioSelection()){
            ANSWER_NOT_VALID            ->  showToast("Selezione non valida, riprova", requireContext())
            ANSWER_EMPTY                ->  showToast(getText(R.string.tfi_warning_null_answer).toString(), requireContext())
            AUDIO_ANSWER_NOT_GIVEN      ->  showToast("Seleziona un'opzione per l\'audio", requireContext())
            TACTILE_ANSWER_NOT_GIVEN    ->  showToast("Seleziona un'opzione per il tatto", requireContext())
            VISUAL_ANSWER_NOT_GIVEN     ->  showToast("Seleziona un'opzione per il visivo", requireContext())
            else                        ->  checkResult(res)
        }
    }

    private fun checkResult(curr_answer:Int){
        val elapsedms = getTimeDifference(onsetDate)

        if(showResult == TestBasic.TEST_SWITCH_ENABLED) {

            if (curr_answer == correctAnswerId)     binding.imgvResult.setImageResource(R.drawable.success_icon)
            else                                    binding.imgvResult.setImageResource(R.drawable.failure_icon)
            binding.imgvResult.visibility   = View.VISIBLE

            binding.btClear.visibility      = View.INVISIBLE
            binding.btConfirm.visibility    = View.INVISIBLE

            mHandler.postDelayed({
                binding.imgvResult.visibility = View.INVISIBLE
                sendResult(curr_answer, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
            }, 1000L)
        }
        else    sendResult(curr_answer, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
    }

    private fun sendResult(response: Int, elapsedTime: Int, response_id: Int) {
        if (targetFragment == null) return

        tts?.stop()

        val intent = TestFragment.newIntent(response, elapsedTime, response_id)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    private fun getRadioSelection():Int{

        var res = ""
        when(binding.radioGroupAudio.checkedRadioButtonId == -1) {
            true    -> return AUDIO_ANSWER_NOT_GIVEN
            false   -> res = binding.radioGroupAudio.indexOfChild(binding.radioGroupAudio.findViewById(binding.radioGroupAudio.checkedRadioButtonId)).toString()
        }

        when(binding.radioGroupTactile.checkedRadioButtonId == -1) {
            true    -> return TACTILE_ANSWER_NOT_GIVEN
            false   -> res = "$res,${binding.radioGroupTactile.indexOfChild(binding.radioGroupTactile.findViewById(binding.radioGroupTactile.checkedRadioButtonId))}"
        }

        when(binding.radioGroupVisual.checkedRadioButtonId == -1) {
            true    -> return VISUAL_ANSWER_NOT_GIVEN
            false   -> res = "$res,${binding.radioGroupVisual.indexOfChild(binding.radioGroupVisual.findViewById(binding.radioGroupVisual.checkedRadioButtonId))}"
        }
        return  if(res == "0,0,0")      ANSWER_EMPTY
                else                    mAnswers.indexOf(res)
    }
}