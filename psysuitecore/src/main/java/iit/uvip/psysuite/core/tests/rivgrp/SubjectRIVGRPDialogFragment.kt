package iit.uvip.psysuite.core.tests.rivgrp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.ui.subjects_dialog.SubjectBasicDialogFragment
import kotlinx.android.synthetic.main.fragment_subject_info_basic_rivgrp.*
import kotlinx.android.synthetic.main.fragment_subject_info_basic_rivgrp.spCondition

class SubjectRIVGRPDialogFragment : SubjectBasicDialogFragment(), AdapterView.OnItemSelectedListener
{
    override val LOG_TAG: String = SubjectRIVGRPDialogFragment::class.java.simpleName

    private var isRivalryFirst: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_basic_rivgrp, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spCondition.onItemSelectedListener = this

        swFirstCond.setOnCheckedChangeListener { _, isChecked ->
            swFirstCond.text =  if(isChecked)  "rivalry"
                                else           "grouping"
        }
    }

    override fun initData(subj: SubjectBasicParcel) {
        super.initData(subj)

        txtDurBlocks.setText(((subj as SubjectRIVGRPParcel).blockDuration/1000).toString())
        txtNBlocks.setText(subj.totBlocks.toString())

        swFirstCond.isChecked   = isRivalryFirst
        swFirstCond.text        = "rivalry"
    }


    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        // spGroup and spCondition data coincides.
        // when selecting training sessions => selCondition = selGroup (and condition spinner gets disabled)

        // check session change
        when(spCondition.selectedItemPosition){
            2,5   -> {
                swFirstCond.visibility  = View.VISIBLE
                labFirstCond.visibility = View.VISIBLE
            }
            else  -> {
                swFirstCond.visibility  = View.INVISIBLE
                labFirstCond.visibility = View.INVISIBLE
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun checkData():List<String>{
        val errors = super.checkData() as MutableList<String>

        if(txtDurBlocks.text.toString().toInt() < 5)     errors.add(resources.getString(R.string.select_session))

        val nblocks = txtNBlocks.text.toString().toInt()
        if(nblocks < 2 || ((nblocks % 2) != 0))          errors.add(resources.getString(R.string.warn_blocks))

        return errors
    }
//
//    override fun clear() {
//        super.clear()
//        spinner.setSelection(0)
//    }

    override fun updateSubject(): SubjectRIVGRPParcel{

        subject  = super.updateSubject()

        (subject as SubjectRIVGRPParcel).blockDuration  = txtDurBlocks.text.toString().toLong() * 1000
        (subject as SubjectRIVGRPParcel).rivFirst       = swFirstCond.isChecked
        (subject as SubjectRIVGRPParcel).totBlocks      = txtNBlocks.text.toString().toInt()

        return subject as SubjectRIVGRPParcel
    }
}