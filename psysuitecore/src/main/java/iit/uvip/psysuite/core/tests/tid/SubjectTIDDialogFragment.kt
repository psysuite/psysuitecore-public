package iit.uvip.psysuite.core.tests.tid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.subjects_dialog.SubjectLongitudinalDialogFragment
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.synthetic.main.fragment_subject_info_tid.*
import org.albaspazio.core.accessory.showToast

class SubjectTIDDialogFragment : SubjectLongitudinalDialogFragment() {
    override val LOG_TAG: String = SubjectTIDDialogFragment::class.java.simpleName
//    private var subject: SubjectTIDParcel? = null


    companion object {
        fun newInstance(title: String): SubjectTIDDialogFragment {
            val frag = SubjectTIDDialogFragment()
            val args        = Bundle()
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subject_info_tid, container)
    }

    override fun updateGUI(subj: SubjectBasicParcel) {

        super.updateGUI(subj)
        radioGroupFirstModality.check(radioGroupFirstModality.getChildAt((subj as SubjectTIDParcel).first_modality).id)
    }

    override fun clear() {
        super.clear()
        radioGroupFirstModality.clearCheck()
    }

    override fun updateSubject(): SubjectTIDParcel? {

        subject = super.updateSubject() as SubjectTIDParcel

        when(radioGroupFirstModality.checkedRadioButtonId != -1) {
            true -> {
                val id = radioGroupFirstModality.checkedRadioButtonId
                val radioButton: RadioButton = radioGroupFirstModality.findViewById(id)
                (subject as SubjectTIDParcel).first_modality =
                    radioGroupFirstModality.indexOfChild(radioButton)      // val btn = radioGroup.getChildAt(radioId) as RadioButton
            }
            false -> {
                showToast("Seleziona un'opzione per la modalità iniziale di training", requireContext())
                return null
            }
        }
        return subject as SubjectTIDParcel
    }
}