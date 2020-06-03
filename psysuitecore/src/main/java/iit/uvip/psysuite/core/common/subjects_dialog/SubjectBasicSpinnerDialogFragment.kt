package iit.uvip.psysuite.core.common.subjects_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicListParcel
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.synthetic.main.fragment_subject_info_basic_spinner.*

open class SubjectBasicSpinnerDialogFragment : SubjectBasicDialogFragment()
{
    override val LOG_TAG:String                 = SubjectBasicSpinnerDialogFragment::class.java.simpleName
    protected var nSpinnerElements: Int = 0

    companion object {
        fun newInstance(title: String): SubjectBasicSpinnerDialogFragment {
            val frag = SubjectBasicSpinnerDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_basic, container)
    }

    override fun initData() {

        super.initData()

        ArrayAdapter.createFromResource(requireContext(), (subject as SubjectBasicListParcel).spinner_data_resource, android.R.layout.simple_spinner_item)
        .also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            nSpinnerElements = adapter.count
        }
        labSpinner.text = (subject as SubjectBasicListParcel).spinner_label
    }

    override fun updateGUI(subj: SubjectBasicParcel){
        super.updateGUI(subj)
        spinner.setSelection((subj as SubjectBasicListParcel).spinner_sel)
    }

    override fun clear(){
        super.clear()
        spinner.setSelection(-1)
    }

    override fun updateSubject(): SubjectBasicListParcel?{
        subject = super.updateSubject() as SubjectBasicListParcel
        (subject as SubjectBasicListParcel).spinner_sel = spinner.selectedItemPosition
        return subject as SubjectBasicListParcel
    }
}