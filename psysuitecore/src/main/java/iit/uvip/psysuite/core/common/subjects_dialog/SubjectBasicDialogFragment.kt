package iit.uvip.psysuite.core.common.subjects_dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.synthetic.main.fragment_subject_info_basic_spinner.*
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.ui.show2MethodsDialog
import org.albaspazio.core.ui.showAlert

open class SubjectBasicDialogFragment: DialogFragment()
{
    open val LOG_TAG: String = SubjectBasicDialogFragment::class.java.simpleName
    protected var nConditions: Int = 0
    protected var selCondition: Int = -1

    protected lateinit var mTaskCodes: List<TaskCode>
    private lateinit var mNextTrialModes:List<List<Int>>
    protected lateinit var subject: SubjectBasicParcel

    companion object {
        @JvmStatic val EVENT_SUBJECT:String = "subject"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_basic, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val subj: SubjectBasicParcel? = arguments?.getParcelable(EVENT_SUBJECT)

        if (subj == null) {
            showAlert(
                requireActivity(), resources.getString(R.string.critical_error),
                "${resources.getString(R.string.empty_subject_parcel)}\n${resources.getString(R.string.restart_app_suggestion)}"
            )
            dismiss()
            return
        } else subject = subj

        val ntm         = getCompanionObjectMethod(subject.testClass, "getNextTrialModes")
        mNextTrialModes = ntm.first?.call(ntm.second) as List<List<Int>>

        val ci          = getCompanionObjectMethod(subject.testClass, "getConditionsInfo")
        mTaskCodes      = ci.first?.call(ci.second, requireContext()) as List<TaskCode>

        initData(subject)

        txtName.requestFocus()
    }

    override fun onResume() {

        val params                  = dialog?.window!!.attributes               // Get existing layout params for the window
        params.width                = WindowManager.LayoutParams.MATCH_PARENT   // Assign window properties to fill the parent
        params.height               = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window!!.attributes = params as WindowManager.LayoutParams

        super.onResume()

        bt_confirm.setOnClickListener   {confirmData()}
        bt_clear.setOnClickListener     {clear()}
        bt_cancel.setOnClickListener    {sendResult(null)}

        swInteractive?.setOnCheckedChangeListener { _, b ->
            subject.nextTrailModality = when (b) {
                true    -> TestBasic.TEST_NEXTTRIAL_BUTTON
                false   -> TestBasic.TEST_NEXTTRIAL_AUTO
            }
        }
    }

    protected open fun initData(subj: SubjectBasicParcel) {

        //------------------------------------------------------
        // SUB TASKS
        //------------------------------------------------------
        setConditions(mTaskCodes)

        //------------------------------------------------------
        // NEXT TRIAL MODALITY
        //------------------------------------------------------
        // swInteractive is visible only in TEST_NEXTTRIAL_BUTTON & TEST_NEXTTRIAL_AUTO

        subject.nextTrailModality = mNextTrialModes[selCondition][0]

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
            TestBasic.TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER,
            TestBasic.TEST_NEXTTRIAL_ANSWER -> showInteractive(false)
        }

        //------------------------------------------------------
        // SUBJECT DEMOGRAPHIC
        //------------------------------------------------------
        txtName.setText(subj.label)

        if (subj.age != -1) txtAge.setText(subj.age.toString())
        else txtAge.setText("")

        if (subj.gender != -1)  radioGroupGender.check(radioGroupGender.getChildAt(subj.gender).id)
        else                    radioGroupGender.clearCheck()
        //------------------------------------------------------
    }

    protected fun setConditions(tc:List<TaskCode>){

        val adapter: ArrayAdapter<TaskCode> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tc)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCondition.adapter = adapter
        nConditions         = adapter.count

        if (nConditions == 1) {
            // do not show condition spinner & set subject.type
            labCondition.visibility = View.GONE
            spCondition.visibility  = View.GONE
            subject.type            = mTaskCodes[0].id
            selCondition            = 0
        }
        else if (nConditions > 1) {
            if(subject.type != -1) {
                // set condition spinner to subject.type
                mTaskCodes.mapIndexed { index, taskCode ->
                    if (taskCode.id == subject.type){
                        spCondition.setSelection(index)
                        selCondition            = index
                    }
                }
            }
            else {
                // set condition spinner to first sub-task
                selCondition = 0
                spCondition.setSelection(selCondition)
                subject.type            = mTaskCodes[0].id
            }
        }
    }

    //  ---- UI presses ----------------------------------------------------------------
    protected open fun confirmData(){

        val errors = checkData()
        if(errors.isNotEmpty()){
            val str_errors = errors.joinToString("\n")
            showAlert(
                requireActivity(),
                resources.getString(R.string.warning),
                resources.getString(R.string.subject_info_notcorrected, str_errors)
            )
        }
        else {
            // data are valid => create subject object
            val subj = updateSubject()

            // in case the subject's "label_type_Date" file exists, ask user whether continue or change name
            if(manageSubjectFileExistence(subj)){
                // file is unique
                subject = subj
                sendResult(subject)
            }
        }
    }

    protected open fun clear(){

        if (nConditions > 1)
            spCondition.setSelection(-1)

        txtName.setText("")
        txtAge.setText("")
        radioGroupGender.clearCheck()

        if (subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_AUTO || subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_BUTTON) {
            swInteractive?.isChecked    = false
            subject.nextTrailModality   = TestBasic.TEST_NEXTTRIAL_AUTO
        }
    }

    //------------------------------------------------------------------------------------
    // ACCESSORY
    //------------------------------------------------------------------------------------
    // validate subject info
    protected open fun checkData():List<String>{

        val errors = mutableListOf<String>()

        if(SubjectBasicParcel.validate(txtName.text.toString(), txtAge.text.toString()).isNotBlank())
                                                                errors.add(" - " + resources.getString(R.string.select_subject_info))
        if(radioGroupGender.checkedRadioButtonId == -1)         errors.add(" - " + resources.getString(R.string.select_gender))
        if (spCondition.selectedItemPosition == -1)             errors.add(" - " + resources.getString(R.string.select_condition))

        return errors
    }

    // subject has been already validated
    protected open fun updateSubject(): SubjectBasicParcel{

        val gender:Int              = radioGroupGender.indexOfChild(radioGroupGender.findViewById(radioGroupGender.checkedRadioButtonId))

        subject.type                = mTaskCodes[spCondition.selectedItemPosition].id

        subject.label               = txtName.text.toString()
        subject.age                 = txtAge.text.toString().toInt()
        subject.gender              = gender

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

    // check whether subject's "label_type_Date" file exists, ask user whether continue or change name
    private fun manageSubjectFileExistence(subj: SubjectBasicParcel):Boolean{
        return if(subj.existSubjectFile()){
            show2MethodsDialog(
                requireActivity(),
                resources.getString(R.string.warning),
                resources.getString(R.string.subject_present),
                resources.getString(R.string.yes),
                resources.getString(R.string.no),
                {
                    // cancel press. stop. let user change data
                    txtName.requestFocus()
                },
                { // ok press, update subject, then continue
                    subject = subj
                    sendResult(subject)
                })
            false
        }
        else true
    }

    private fun showInteractive(show: Boolean) {
        if (show) {
            swInteractive?.visibility   = View.VISIBLE
            labInteractive?.visibility  = View.VISIBLE
        } else {
            swInteractive?.visibility   = View.GONE
            labInteractive?.visibility  = View.GONE
        }
    }
}