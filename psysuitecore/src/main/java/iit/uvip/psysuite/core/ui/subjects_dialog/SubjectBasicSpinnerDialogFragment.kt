package iit.uvip.psysuite.core.ui.subjects_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoBasicBinding
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoBasicSpinnerBinding
import iit.uvip.psysuite.core.model.parcel.SubjectBasicListParcel
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel


open class SubjectBasicSpinnerDialogFragment : SubjectBasicDialogFragment()
{
    override val LOG_TAG:String                 = SubjectBasicSpinnerDialogFragment::class.java.simpleName
    private lateinit var binding: FragmentSubjectInfoBasicSpinnerBinding

    private var nSpinnerElements: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_subject_info_basic_spinner, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSubjectInfoBasicSpinnerBinding.bind(mView)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initData() {
        super.initData()

        ArrayAdapter.createFromResource(requireContext(), (subject as SubjectBasicListParcel).spinner_data_resource, android.R.layout.simple_spinner_item)
        .also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter
            nSpinnerElements = adapter.count
        }
        binding.spinner.setSelection((subject as SubjectBasicListParcel).spinner_sel, false)
        binding.labSpinner.text = (subject as SubjectBasicListParcel).spinner_label
    }

    override fun clear(){
        super.clear()
        binding.spinner.setSelection(-1)
    }

    override fun checkData():List<String>{
        val errors = super.checkData() as MutableList<String>
        if (binding.spinner.selectedItemPosition == -1) errors.add(" - " + resources.getString(R.string.select_spinner, (binding as FragmentSubjectInfoBasicSpinnerBinding).labSpinner.text) )
        return errors
    }

    override fun updateSubject(): SubjectBasicListParcel{
        subject = super.updateSubject() as SubjectBasicListParcel

        (subject as SubjectBasicListParcel).spinner_sel = binding.spinner.selectedItemPosition
        return subject as SubjectBasicListParcel
    }
}