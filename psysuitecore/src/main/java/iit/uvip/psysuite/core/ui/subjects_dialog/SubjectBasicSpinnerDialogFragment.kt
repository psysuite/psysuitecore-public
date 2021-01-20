package iit.uvip.psysuite.core.ui.subjects_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.parcel.SubjectBasicListParcel
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import kotlinx.android.synthetic.main.fragment_subject_info_basic_spinner.*

open class SubjectBasicSpinnerDialogFragment : SubjectBasicDialogFragment()
{
    override val LOG_TAG:String                 = SubjectBasicSpinnerDialogFragment::class.java.simpleName
    private var nSpinnerElements: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_basic, container)
    }

    override fun initData(subj: SubjectBasicParcel) {

        super.initData(subj)

        ArrayAdapter.createFromResource(requireContext(), (subject as SubjectBasicListParcel).spinner_data_resource, android.R.layout.simple_spinner_item)
        .also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            nSpinnerElements = adapter.count
        }
        spinner.setSelection((subj as SubjectBasicListParcel).spinner_sel, false)

        labSpinner.text = (subject as SubjectBasicListParcel).spinner_label
    }

    override fun clear(){
        super.clear()
        spinner.setSelection(-1)
    }

    override fun checkData():List<String>{

        val errors = super.checkData() as MutableList<String>
        if (spinner.selectedItemPosition == -1) errors.add(" - " + resources.getString(R.string.select_spinner, labSpinner.text) )
        return errors
    }

    override fun updateSubject(): SubjectBasicListParcel{

        subject = super.updateSubject() as SubjectBasicListParcel

        (subject as SubjectBasicListParcel).spinner_sel = spinner.selectedItemPosition
        return subject as SubjectBasicListParcel
    }
}