package iit.uvip.psysuite.core.ui.fragments.answers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.tests.TestBasic
import kotlinx.android.synthetic.main.fragment_2afc_answer.*
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.gestures.MyGestureDetector
import org.albaspazio.core.speech.SpeechManager


class AnswerGestureDialogFragment: TwoAFCAnswerDialogFragment()
{
    override val LOG_TAG = AnswerGestureDialogFragment::class.java.simpleName

    private var selectedAnswer:String  = ""
    private lateinit var layoutView:View
    private var isAborting:Boolean = false  // when user DT this flag is set to true, only after another DT, it aborts the test
    companion object {
        fun newInstance(title: String, speechManager: SpeechManager): AnswerGestureDialogFragment {
            val frag = AnswerGestureDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)
            frag.tts = speechManager

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layoutView = inflater.inflate(R.layout.fragment_2afc_answer, container)
        return layoutView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showResult      = false
        selectedAnswer  = ""

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

    override fun onResume() {
        super.onResume()

        registerGestures()

        bt_confirm.setOnClickListener(null)
        bt_confirm.visibility = View.INVISIBLE

        bt_clear.setOnClickListener(null)
        bt_clear.visibility = View.INVISIBLE

        bt_abort_test.setOnClickListener(null)
        bt_abort_test.visibility = View.INVISIBLE

        rb_a_0.visibility = View.INVISIBLE
        rb_a_1.visibility = View.INVISIBLE
    }

    //---------------------------------------------------------------------------------------------------------------------------------------
    private fun registerGestures(){

        val detector = GestureDetectorCompat(requireContext(), MyGestureDetector(::onGestures, null))
        layoutView.setOnTouchListener { _, motionEvent -> detector.onTouchEvent(motionEvent) }
    }

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

    private fun selectAnswer(up:Boolean){
        selectedAnswer =    if(up)  mAnswers[0]
                            else    mAnswers[1]
        tts?.speak(resources.getString(R.string.answer_selected, selectedAnswer))
    }

    override fun confirm() {
        if(selectedAnswer.isNotEmpty()){

            val msg =   if(isInstructions)  resources.getString(R.string.exp_letsstart_blind)
                        else                resources.getString(R.string.new_trial)

            tts?.speak(msg){
                requireActivity().runOnUiThread {
                    val elapsedms = getTimeDifference(onsetDate)
                    sendResult(selectedAnswer, elapsedms, TestBasic.EVENT_ANSWER_GIVEN)
                }
            }
        }
        else tts?.speak(resources.getString(R.string.select_one_answer))
    }

    private fun playbackAnswer(){
        if(selectedAnswer.isEmpty())    tts?.speak(resources.getString(R.string.select_one_answer))
        else                            tts?.speak(selectedAnswer)
    }

    private fun checkAbort(){
        if(isAborting)  abort()
        else{
            isAborting = true
            tts?.speak(resources.getString(R.string.warning_dt_again2abort))
        }
    }
}