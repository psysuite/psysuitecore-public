package iit.uvip.psysuite.core.ui.subjects_dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.IdLabelData
import kotlinx.android.synthetic.main.fragment_subject_info_basic.*
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.filesystem.deleteFilesStartingWith
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert

open class SubjectBasicDialogFragment: DialogFragment(){

    open val LOG_TAG: String = SubjectBasicDialogFragment::class.java.simpleName


    private var allowedPopulations:List<IdLabelData> = listOf()
    private var nPopulations: Int = 0
    private var selPopulation: Int = -1

    protected var nConditions: Int = 0
    private var selCondition: Int = -1

    protected lateinit var mTaskCodeLabels: List<ConditionData>
    protected lateinit var mNextTrialModes:List<List<Int>>
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

        val ntm         = getCompanionObjectMethod(subject.classes[0], "getNextTrialModes")
        mNextTrialModes = ntm.first?.call(ntm.second) as List<List<Int>>

        val ci          = getCompanionObjectMethod(subject.classes[0], "getConditionsInfo")
        mTaskCodeLabels = ci.first?.call(ci.second, requireContext()) as List<ConditionData>

        initData(subject)

        if(txtName != null) txtName.requestFocus()      // subclasses may not have this UI elements (e.g. SampleDialog)
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

        // SUB TASKS & POPULATION
        setConditions(mTaskCodeLabels)
        setPopulation(selCondition)
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
        // noise visibility
        swWhiteNoise.visibility     = View.VISIBLE
        labWhiteNoise.visibility    = View.VISIBLE
        when(subj.whitenoise){
            TestBasic.TEST_WNOISE_DISABLED,
            TestBasic.TEST_WNOISE_ENABLED->  {
                swWhiteNoise.visibility     = View.INVISIBLE
                labWhiteNoise.visibility    = View.INVISIBLE
            }
        }
        // noise start value
        swWhiteNoise.isChecked      = true
        when(subj.whitenoise){
            TestBasic.TEST_WNOISE_DISABLED,
            TestBasic.TEST_WNOISE_CHOOSE_OFF    ->  swWhiteNoise.isChecked      = false
        }
        //------------------------------------------------------
    }

    protected fun setConditions(tc:List<ConditionData>){

        val adapter: ArrayAdapter<ConditionData> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tc)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCondition.adapter = adapter
        nConditions         = adapter.count

        if (nConditions == 1) {
            // do not show condition spinner & set subject.type
            labCondition.visibility = View.GONE
            spCondition.visibility  = View.GONE
            subject.type            = mTaskCodeLabels[0].id
            selCondition            = 0
        }
        else if (nConditions > 1) {
            if(subject.type != -1) {
                // set condition spinner to subject.type
                mTaskCodeLabels.mapIndexed { index, taskCode ->
                    if (taskCode.id == subject.type){
                        spCondition.setSelection(index, false)
                        selCondition            = index
                    }
                }
            }
            else {
                // set condition spinner to first sub-task
                selCondition = 0
                spCondition.setSelection(selCondition)
                subject.type            = mTaskCodeLabels[0].id
            }
        }

        spCondition.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setPopulation(spCondition.selectedItemPosition) }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setPopulation(pop_index:Int){

        allowedPopulations  = mTaskCodeLabels[pop_index].allowedPopulations
        nPopulations        = allowedPopulations.size

        val adapter: ArrayAdapter<IdLabelData> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, allowedPopulations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spPopulation.adapter = adapter

        // set condition spinner to subject.type
        selPopulation            = 0
        allowedPopulations.mapIndexed { index, pair ->
            if (pair.id == subject.population)  selPopulation = index
        }
        spPopulation.setSelection(selPopulation, false)
    }

    //------------------------------------------------------------------------------------
    // UI presses
    //------------------------------------------------------------------------------------
    protected open fun confirmData(){

        val errors = checkData()
        if(errors.isNotEmpty()){
            val str_errors = errors.joinToString("\n")
            showAlert(requireActivity(),resources.getString(R.string.warning), resources.getString(R.string.subject_info_notcorrected, str_errors))
        }
        else {
            // data are valid => create subject object
            val subj = updateSubject()

            // verify whether is unique. in case the subject's "label_type_Date" file exists, ask user whether continue or change name
            sendSubjectOrChangeData(subj)
        }
    }

    protected open fun clear(){

        if (nConditions > 1)
            spCondition.setSelection(-1)

        spPopulation.setSelection(-1)

        txtName.setText("")
        txtAge.setText("")
        radioGroupGender.clearCheck()

        if (subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_AUTO || subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_BUTTON) {
            swInteractive?.isChecked    = false
            subject.nextTrailModality   = TestBasic.TEST_NEXTTRIAL_AUTO
        }

        swWhiteNoise.isChecked = true
    }

    //------------------------------------------------------------------------------------
    // SUBJECT VALIDATION
    //------------------------------------------------------------------------------------
    // validate subject info
    protected open fun checkData():List<String>{

        val errors = mutableListOf<String>()

        if(SubjectBasicParcel.validate(txtName.text.toString(), txtAge.text.toString()).isNotBlank())
                                                                errors.add(" - " + resources.getString(R.string.select_subject_info))

        if(radioGroupGender.checkedRadioButtonId == -1)         errors.add(" - " + resources.getString(R.string.select_gender))
        if (spCondition.selectedItemPosition == -1)             errors.add(" - " + resources.getString(R.string.select_condition))
        if (spPopulation.selectedItemPosition == -1)            errors.add(" - " + resources.getString(R.string.select_population))

        return errors
    }

    // subject has been already validated
    protected open fun updateSubject(): SubjectBasicParcel{

        val gender:Int              = radioGroupGender.indexOfChild(radioGroupGender.findViewById(radioGroupGender.checkedRadioButtonId))

        subject.type                = mTaskCodeLabels[spCondition.selectedItemPosition].id
        subject.population          = allowedPopulations[spPopulation.selectedItemPosition].id

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

        subject.whitenoise =    if(swWhiteNoise.isChecked)  TestBasic.TEST_WNOISE_CHOOSE_ON
                                else                        TestBasic.TEST_WNOISE_CHOOSE_OFF

        return subject
    }

    // subj.block is by default always -1
    // check whether subject's "label_type_Date" file exists, ask user whether continue or change name
    // -1 no file exist, 0 just one file without block, > 0  id of the next block (if it found _blk1 => returns 2)
    private fun sendSubjectOrChangeData(subj: SubjectBasicParcel){
        val nextblock = subj.existSubjectFile(requireContext())
        when(nextblock){
            -1 -> { // file is unique
                        subject = subj
                        sendResult(subject)
            }
            0  -> { // exist only one same subjects file. overwrite it and continue with presently filled subject or don't do anything
                show2ChoisesDialog(requireActivity(), resources.getString(R.string.warning), resources.getString(R.string.subject_present), resources.getString(R.string.yes), resources.getString(R.string.no),
                    {   // ok press, update subject, then continue
                        subject = subj
                        sendResult(subject)
                    },
                    {   // cancel press. stop. let user change data
                        txtName.requestFocus()
                    })
            }
            else -> {  // exist at least n-block files.
                        // 1-based last block file  (if it finds lab_type_blk2.txt => return 3)
                show2ChoisesDialog(requireActivity(), resources.getString(R.string.warning), resources.getString(R.string.subject_block_present), resources.getString(R.string.continue_txt), resources.getString(R.string.restart),
                    { // ok press, continue next block
                        subject         = subj
                        subject.block   = nextblock     // this is the only case where block is != -1
                        sendResult(subject)
                    },
                    { // cancel press. DELETE all previous files !!! and continue with presently filled subject
                        deleteFilesStartingWith(subject.getFilesPrefix(requireContext()))
                        subject = subj
                        sendResult(subject)
                    })
            }
        }
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

    //------------------------------------------------------------------------------------
    // ACCESSORY
    //------------------------------------------------------------------------------------
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