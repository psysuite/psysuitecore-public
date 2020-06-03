package iit.uvip.psysuite.core.common.subjects_dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.synthetic.main.fragment_subject_info_basic_spinner.*

import org.albaspazio.core.accessory.showToast

open class SubjectBasicDialogFragment: DialogFragment()
{
    open val LOG_TAG: String = SubjectBasicDialogFragment::class.java.simpleName
    protected var nConditions: Int = 0

    protected lateinit var subject: SubjectBasicParcel

    companion object {
        fun newInstance(title: String): SubjectBasicDialogFragment {
            val frag = SubjectBasicDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
        @JvmStatic val EVENT_SUBJECT:String                             = "subject"

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_basic, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val subj: SubjectBasicParcel? = arguments?.getParcelable("subject")

        if (subj == null) {
            showToast("ERRORE IRRECUPERABILE. SOGGETTO E' VUOTO", requireContext())
            dismiss()
            return
        } else subject = subj

        // Fetch arguments from bundle and set title
        val title       = requireArguments().getString("title", "Enter Name")

        dialog?.setTitle(title)

        initData()

        updateGUI(subject)
    }

    protected open fun initData() {

        val adapter: ArrayAdapter<TaskCode> = ArrayAdapter<TaskCode>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            subject.taskcodes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spCondition.adapter = adapter
        nConditions = adapter.count

        if (nConditions == 1) {
            // do not show condition spinner & set subject.type
            labCondition.visibility = View.GONE
            spCondition.visibility = View.GONE
            subject.type = subject.taskcodes[0].id
        }
    }

    override fun onResume() {

        val params =
            dialog?.window!!.attributes               // Get existing layout params for the window
        params.width =
            WindowManager.LayoutParams.MATCH_PARENT   // Assign window properties to fill the parent
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window!!.attributes = params as WindowManager.LayoutParams

        super.onResume()

        bt_confirm.setOnClickListener{

            val subj = updateSubject()
            if(subj != null) {
                subject = subj
                sendResult(subject)
            }
        }

        bt_clear.setOnClickListener{
            clear()
        }

        bt_cancel.setOnClickListener{
            sendResult(null)
        }

        swInteractive?.setOnCheckedChangeListener { _, b ->
            subject.nextTrailModality = when (b) {
                true    -> TestBasic.TEST_NEXTTRIAL_BUTTON
                false   -> TestBasic.TEST_NEXTTRIAL_AUTO
            }
        }

        spCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if ((spCondition.selectedItem as TaskCode).id == TestBasic.TEST_ATB_TIME) {
                    swInteractive.visibility = View.GONE
                    labInteractive.visibility = View.GONE
                    subject.nextTrailModality = TestBasic.TEST_NEXTTRIAL_ANSWER
                } else {
                    swInteractive.visibility = View.VISIBLE
                    labInteractive.visibility = View.VISIBLE
                    if (subject.nextTrailModality != TestBasic.TEST_NEXTTRIAL_NOCHOOSE && subject.nextTrailModality != TestBasic.TEST_NEXTTRIAL_ANSWER) {
                        swInteractive?.isChecked = false
                        subject.nextTrailModality = TestBasic.TEST_NEXTTRIAL_AUTO
                    }
                }
            }
        }

    }

    private fun showInteractive(show: Boolean) {
        if (show) {
            swInteractive?.visibility = View.VISIBLE
            labInteractive?.visibility = View.VISIBLE
        } else {
            swInteractive?.visibility = View.GONE
            labInteractive?.visibility = View.GONE
        }
    }

    protected open fun updateGUI(subj: SubjectBasicParcel){

        when (subject.nextTrailModality) {
            TestBasic.TEST_NEXTTRIAL_BUTTON -> {
                showInteractive(true)
                swInteractive?.isChecked = true
            }
            TestBasic.TEST_NEXTTRIAL_AUTO -> {
                showInteractive(true)
                swInteractive?.isChecked = false
            }
            TestBasic.TEST_NEXTTRIAL_NOCHOOSE,
            TestBasic.TEST_NEXTTRIAL_VOICE_ANSWER,
            TestBasic.TEST_NEXTTRIAL_ANSWER -> showInteractive(false)
        }
        txtName.setText(subj.label)

        if (subj.age != -1) txtAge.setText(subj.age.toString())
        else txtAge.setText("")

        if (subj.gender != -1) radioGroupGender.check(radioGroupGender.getChildAt(subj.gender).id)
        else radioGroupGender.clearCheck()

        if (nConditions > 1)
            if (subject.type != -1) {
                subject.taskcodes.mapIndexed { index, taskCode ->
                    if (taskCode.id == subject.type) spCondition.setSelection(index)
                }
            } else spCondition.setSelection(-1)
    }

    protected open fun clear(){

        if (nConditions > 1)
            spCondition.setSelection(-1)

        txtName.setText("")
        txtAge.setText("")
        radioGroupGender.clearCheck()

        if (subject.nextTrailModality != TestBasic.TEST_NEXTTRIAL_NOCHOOSE && subject.nextTrailModality != TestBasic.TEST_NEXTTRIAL_ANSWER) {
            swInteractive?.isChecked = false
            subject.nextTrailModality = TestBasic.TEST_NEXTTRIAL_AUTO
        }
    }

    protected open fun updateSubject(): SubjectBasicParcel?{

        val name = txtName.text.toString()
        val age = txtAge.text.toString()

        if(SubjectBasicParcel.validate(name, age).isNotBlank()){
            showToast("Avviso: indicare le informazioni del soggetto", requireContext())
            return null
        }

        var gender:Int      = -1
        when(radioGroupGender.checkedRadioButtonId != -1) {
            true -> {
                val id                      = radioGroupGender.checkedRadioButtonId
                val radioButton:RadioButton = radioGroupGender.findViewById(id)
                gender                      = radioGroupGender.indexOfChild(radioButton)      // val btn = radioGroup.getChildAt(radioId) as RadioButton
            }
            false -> {
                showToast("Seleziona un'opzione per il sesso", requireContext())
                return null
            }
        }
        if (spCondition.selectedItemPosition == -1) {
            showToast("Seleziona una condizione sperimentale", requireContext())
            return null
        } else subject.type = subject.taskcodes[spCondition.selectedItemPosition].id

        subject.label = name
        subject.age = age.toInt()
        subject.gender = gender

        // only If user can select interaction modality, update his/her selection
        if (subject.nextTrailModality != TestBasic.TEST_NEXTTRIAL_NOCHOOSE &&
            subject.nextTrailModality != TestBasic.TEST_NEXTTRIAL_ANSWER &&
            subject.nextTrailModality != TestBasic.TEST_NEXTTRIAL_VOICE_ANSWER
        ) {

            subject.nextTrailModality = when (swInteractive?.isChecked) {
                true -> TestBasic.TEST_NEXTTRIAL_BUTTON
                false -> TestBasic.TEST_NEXTTRIAL_AUTO
                null -> subject.nextTrailModality
            }
        }
        return subject
    }

    private fun sendResult(subj: SubjectBasicParcel?) {
        if (targetFragment == null) {
            return
        }
        val intent = Intent()
        intent.putExtra(EVENT_SUBJECT, subj)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }
}