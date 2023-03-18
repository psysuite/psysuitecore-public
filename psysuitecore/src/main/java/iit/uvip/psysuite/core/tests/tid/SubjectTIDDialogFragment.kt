package iit.uvip.psysuite.core.tests.tid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoBasicBinding
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoBasicSpinnerBinding
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoTidBinding
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.ui.subjects_dialog.SubjectLongitudinalDialogFragment
import iit.uvip.psysuite.core.utility.ConditionData

class SubjectTIDDialogFragment : SubjectLongitudinalDialogFragment(), AdapterView.OnItemSelectedListener
{
    override val LOG_TAG: String = SubjectTIDDialogFragment::class.java.simpleName
    private lateinit var binding: FragmentSubjectInfoTidBinding

    private var selGroup: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_subject_info_tid, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSubjectInfoTidBinding.bind(mView)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initData() {
        super.initData()

        binding.spinner.onItemSelectedListener = this
        binding.spGroup.onItemSelectedListener = this
        //------------------------------------------------------
        // GROUPS <=> mTaskCodes
        //------------------------------------------------------
        val adapter:ArrayAdapter<ConditionData> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mTaskCodeLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spGroup.adapter = adapter

        // nConditions coincided with number of available groups
        if (nConditions == 1) {
            // do not show condition spinner & set subject.type
            binding.labGroup.visibility = View.GONE
            binding.spGroup.visibility  = View.GONE
            (subject as SubjectTIDParcel).group = mTaskCodeLabels[0].id
            selGroup                            = 0
        }
        else if (nConditions > 1) {
            if((subject as SubjectTIDParcel).group != -1) {
                // set group spinner to subject.group
                mTaskCodeLabels.mapIndexed { index, taskCode ->
                    if (taskCode.id == (subject as SubjectTIDParcel).group){
                        binding.spGroup.setSelection(index, false)
                        selGroup            = index
                    }
                }
            }
            else {
                // set group spinner to first sub-task
                selGroup = 0
                binding.spGroup.setSelection(selGroup)
                (subject as SubjectTIDParcel).group = mTaskCodeLabels[0].id
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        // spGroup and spCondition data coincides.
        // when selecting training sessions => selCondition = selGroup (and condition spinner gets disabled)

        // check session change
        when((binding as FragmentSubjectInfoTidBinding).spinner.selectedItemPosition){
            in 2..6   -> {
                        setConditions(listOf(mTaskCodeLabels[binding.spGroup.selectedItemPosition])) // make condition spinner GONE
                        binding.spCondition.isEnabled = false
            }
            else      -> {

                        val selcond = mTaskCodeLabels[binding.spGroup.selectedItemPosition].id   // get current selected task
                        setConditions(mTaskCodeLabels)                                       // enable all

                        mTaskCodeLabels.mapIndexed { index, taskCode ->
                            if(taskCode.id == selcond)    binding.spCondition.setSelection(index)   // set what was selected before this change
                        }
                binding.spCondition.isEnabled = true
            }
        }
        binding.labCondition.visibility = View.VISIBLE
        binding.spCondition.visibility  = View.VISIBLE
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {}


    override fun checkData():List<String>{
        val errors = super.checkData() as MutableList<String>
        if(binding.spinner.selectedItemPosition == 0)   errors.add(resources.getString(R.string.select_session))
        return errors
    }

    override fun clear() {
        super.clear()
        binding.spinner.setSelection(0)
    }

    override fun updateSubject(): SubjectTIDParcel{
        subject  = super.updateSubject() as SubjectTIDParcel

        (subject as SubjectTIDParcel).group     = mTaskCodeLabels[binding.spGroup.selectedItemPosition].id
        (subject as SubjectTIDParcel).session   = binding.spinner.selectedItemPosition - 1

        subject.type = if(binding.spinner.selectedItemPosition in 2..6){
                                subject.showResult = true
                                mTaskCodeLabels[binding.spGroup.selectedItemPosition].id
                       }
                       else{
                                subject.showResult = false
                                mTaskCodeLabels[binding.spCondition.selectedItemPosition].id
                        }

        return subject as SubjectTIDParcel
    }
}