package iit.uvip.psysuite.core.ui.fragments.answers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.Fragment3afcAnswerBinding

import org.albaspazio.core.speech.SpeechManager

class ThreeAFCAnswerDialogFragment: TwoAFCAnswerDialogFragment() {

    override val LOG_TAG = ThreeAFCAnswerDialogFragment::class.java.simpleName
    private lateinit var binding: Fragment3afcAnswerBinding

    companion object {
        fun newInstance(title: String, speechManager: SpeechManager): ThreeAFCAnswerDialogFragment {
            val frag = ThreeAFCAnswerDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            frag.tts = speechManager

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mView = inflater.inflate(R.layout.fragment_3afc_answer, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Fragment3afcAnswerBinding.bind(mView)

        binding.rbA2.text = mAnswers[2]
    }
}