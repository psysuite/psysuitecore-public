package iit.uvip.psysuite.core.ui.fragments.answers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat

import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.Fragment2afcAnswerBinding
import iit.uvip.psysuite.core.tests.TestBasic

import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.gestures.MyGestureDetector
import org.albaspazio.core.speech.SpeechManager


/**
 * A [TwoAFCAnswerDialogFragment] subclass that allows users to answer using gestures.
 * This fragment is designed for 2-alternative forced choice tasks where swipe gestures
 * (up, down, tap, double-tap, long-press) are used to select an answer, confirm,
 * hear the selected answer, or abort the test.
 *
 * It utilizes a [SpeechManager] for text-to-speech feedback.
 */
class AnswerGestureDialogFragment: TwoAFCAnswerDialogFragment()
{
    /**
     * The logging tag for this class.
     */
    override val LOG_TAG = AnswerGestureDialogFragment::class.java.simpleName

    private lateinit var binding: Fragment2afcAnswerBinding

    private var selectedAnswer:String  = ""
    private var selectedAnswerId:Int   = -1
    private var isAborting:Boolean = false  // when user DT this flag is set to true, only after another DT, it aborts the test

    companion object {
        /**
         * Creates a new instance of [AnswerGestureDialogFragment].
         *
         * @param title The title to be displayed (not directly used in this gesture-based dialog).
         * @param speechManager The [SpeechManager] instance for TTS feedback.
         * @return A new instance of [AnswerGestureDialogFragment].
         */
        fun newInstance(title: String, speechManager: SpeechManager): AnswerGestureDialogFragment {
            val frag = AnswerGestureDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            frag.tts = speechManager
            return frag
        }
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        mView = inflater.inflate(R.layout.fragment_2afc_answer, container, false)
//        return mView
//    }

    /**
     * Called immediately after [.onCreateView] has returned, but before any saved state has been restored in to the view.
     * This initializes the view binding and provides spoken instructions if it's an instruction trial.
     *
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Fragment2afcAnswerBinding.bind(mView)

        if(isInstructions)
            tts?.speak(listOf(  resources.getString(R.string.exp_intro_blind1),
                                resources.getString(R.string.exp_intro_blind2),
                                mQuestion,
                                resources.getString(R.string.exp_intro_blind3),
                                mAnswers[0],
                                resources.getString(R.string.exp_intro_blind4),
                                mAnswers[1],
                                resources.getString(R.string.exp_intro_blind5)))
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This method registers gestures and hides UI elements not used in this gesture-based dialog.
     */
    override fun onResume() {
        super.onResume()

        registerGestures()

        binding.btConfirm.setOnClickListener(null)
        binding.btConfirm.visibility = View.INVISIBLE

        binding.btClear.setOnClickListener(null)
        binding.btClear.visibility = View.INVISIBLE

        binding.btAbortTest.setOnClickListener(null)
        binding.btAbortTest.visibility = View.INVISIBLE

        binding.rbA0.visibility = View.INVISIBLE
        binding.rbA1.visibility = View.INVISIBLE
    }

    //---------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Registers the gesture detector for the fragment's view.
     */
    private fun registerGestures(){

        val detector = GestureDetectorCompat(requireContext(), MyGestureDetector(::onGestures, null))
        mView.setOnTouchListener { _, motionEvent -> detector.onTouchEvent(motionEvent) }
    }

    /**
     * Handles detected gestures.
     *
     * - "LP" (Long Press): Checks for abort sequence.
     * - "SU" (Swipe Up): Selects the first answer (typically "YES").
     * - "SD" (Swipe Down): Selects the second answer (typically "NO").
     * - "ST" (Single Tap): Plays back the currently selected answer.
     * - "DT" (Double Tap): Confirms the selected answer.
     *
     * @param gesture_label The label identifying the detected gesture.
     */
    private fun onGestures(gesture_label: String){

        tts!!.stop()

        if(gesture_label == "LP")   checkAbort()
        else{
            when (gesture_label) {
                "SU" ->     selectAnswer(true)              // select YES
                "SD" ->     selectAnswer(false)             // select NO
                "ST" ->     playbackAnswer()
                "DT" ->     confirm()
            }
            isAborting = false
        }
    }

    /**
     * Selects an answer based on the gesture direction.
     * Provides TTS feedback for the selected answer.
     *
     * @param up `true` if the "up" gesture was used (selects the first answer), `false` otherwise (selects the second answer).
     */
    private fun selectAnswer(up:Boolean){

        selectedAnswerId =  if(up)  0
                            else    1
        selectedAnswer   = mAnswers[selectedAnswerId]

        tts?.speak(resources.getString(R.string.answer_selected, selectedAnswer))
    }

    /**
     * Confirms the selected answer and sends the result back to the calling fragment/activity.
     * Provides TTS feedback about correctness (if applicable) and proceeds to the next trial or starts the experiment.
     * If no answer is selected, prompts the user to select one.
     */
    override fun confirm() {
        if(selectedAnswer.isNotEmpty()){

            val msgs = mutableListOf<String>()

            if(showResult == TestBasic.TEST_SWITCH_ENABLED) {
                val msg =   if (correctAnswerId == selectedAnswerId)    resources.getString(R.string.correct_answer)
                            else                                        resources.getString(R.string.wrong_answer)
                msgs.add(msg)
            }
            val msg =   if(isInstructions)  resources.getString(R.string.exp_letsstart_blind)
                        else                resources.getString(R.string.new_trial)
            msgs.add(msg)

            tts?.speak(msgs, delay = 1000L){
                requireActivity().runOnUiThread {
                    val elapsedms = getTimeDifference(onsetDate)
                    sendResult(selectedAnswerId, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
                }
            }
        }
        else tts?.speak(resources.getString(R.string.select_one_answer))
    }

    /**
     * Plays back the currently selected answer using TTS.
     * If no answer is selected, prompts the user to select one.
     */
    private fun playbackAnswer(){
        if(selectedAnswer.isEmpty())    tts?.speak(resources.getString(R.string.select_one_answer))
        else                            tts?.speak(selectedAnswer)
    }

    /**
     * Checks if the user intends to abort the test.
     * A first long press sets an aborting flag and warns the user.
     * A subsequent long press will call [abort].
     */
    private fun checkAbort(){
        if(isAborting)  abort()
        else{
            isAborting = true
            tts?.speak(resources.getString(R.string.warning_dt_again2abort))
        }
    }
}
