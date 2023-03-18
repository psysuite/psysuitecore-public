package iit.uvip.psysuite.core.ui.subjects_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoBasicSpinnerBinding
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoTidBinding
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.model.parcel.SubjectLongitParcel


open class SubjectLongitudinalDialogFragment : SubjectBasicSpinnerDialogFragment()
{
    override val LOG_TAG: String = SubjectLongitudinalDialogFragment::class.java.simpleName
    private lateinit var binding: FragmentSubjectInfoBasicSpinnerBinding

    private var nSessions: Int = 0

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

        ArrayAdapter.createFromResource(requireContext(), (subject as SubjectLongitParcel).test_sessions_array, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinner.adapter = adapter
                nSessions = adapter.count
            }
        binding.spinner.setSelection((subject as SubjectLongitParcel).session)

        binding.labSpinner.text = resources.getString(R.string.session)
    }

    override fun checkData():List<String>{

        val errors = super.checkData() as MutableList<String>
        if (binding.spinner.selectedItemPosition == -1) errors.add(" - " + resources.getString(R.string.select_session))
        return errors
    }

    override fun updateSubject(): SubjectLongitParcel {
        subject = super.updateSubject() as SubjectLongitParcel

        (subject as SubjectLongitParcel).session = binding.spinner.selectedItemPosition
        return subject as SubjectLongitParcel
    }
}