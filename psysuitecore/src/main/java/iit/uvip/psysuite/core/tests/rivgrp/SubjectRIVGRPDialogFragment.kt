package iit.uvip.psysuite.core.tests.rivgrp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView

import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoBasicRivgrpBinding
import iit.uvip.psysuite.core.ui.SubjectBasicDialogFragment


class SubjectRIVGRPDialogFragment : SubjectBasicDialogFragment(), AdapterView.OnItemSelectedListener
{
    override val LOG_TAG: String = SubjectRIVGRPDialogFragment::class.java.simpleName

    private lateinit var binding: FragmentSubjectInfoBasicRivgrpBinding

    private var isRivalryFirst: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_subject_info_basic_rivgrp, container, false)
        return mView        
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSubjectInfoBasicRivgrpBinding.bind(mView)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initData() {
        super.initData()

        binding.swFirstCond.setOnCheckedChangeListener { _, isChecked ->
            binding.swFirstCond.text =   if(isChecked)  "rivalry"
            else           "grouping"
        }

        binding.txtDurBlocks.setText(((subject as SubjectRIVGRPParcel).blockDuration/1000).toString())
        binding.txtNBlocks.setText((subject as SubjectRIVGRPParcel).totBlocks.toString())

        binding.swFirstCond.isChecked   = isRivalryFirst
        binding.swFirstCond.text        = "rivalry"
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        // spGroup and spCondition data coincides.
        // when selecting training sessions => selCondition = selGroup (and condition spinner gets disabled)

        // check session change
        when(binding.spCondition.selectedItemPosition){
            2,5   -> {
                binding.swFirstCond.visibility  = View.VISIBLE
                binding.labFirstCond.visibility = View.VISIBLE
            }
            else  -> {
                binding.swFirstCond.visibility  = View.INVISIBLE
                binding.labFirstCond.visibility = View.INVISIBLE
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun checkData():List<String>{
        val errors = super.checkData() as MutableList<String>

        if(binding.txtDurBlocks.text.toString().toInt() < 5)     errors.add(resources.getString(R.string.select_session))

        val nblocks = binding.txtNBlocks.text.toString().toInt()
        if(nblocks < 2 || ((nblocks % 2) != 0))          errors.add(resources.getString(R.string.warn_blocks))

        return errors
    }

    override fun updateSubject(): SubjectRIVGRPParcel{
        subject  = super.updateSubject()

        (subject as SubjectRIVGRPParcel).blockDuration  = binding.txtDurBlocks.text.toString().toLong() * 1000
        (subject as SubjectRIVGRPParcel).rivFirst       = binding.swFirstCond.isChecked
        (subject as SubjectRIVGRPParcel).totBlocks      = binding.txtNBlocks.text.toString().toInt()

        return subject as SubjectRIVGRPParcel
    }
}