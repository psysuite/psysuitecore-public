package iit.uvip.psysuite.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentSubjectInfoBasicBinding
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.TestBasic.Companion.TEST_NO_LONGITUDINAL
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.IdLabelData
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.filesystem.deleteFilesStartingWith
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert
import kotlin.properties.Delegates

open class SubjectBasicDialogFragment: DialogFragment(){

    open val LOG_TAG: String = SubjectBasicDialogFragment::class.java.simpleName

    private lateinit var binding: FragmentSubjectInfoBasicBinding
    protected lateinit var mView: View

    private var allowedPopulations:List<IdLabelData> = listOf()
    private var nPopulations: Int = 0
    private var selPopulation: Int = -1

    protected var nConditions: Int = 0
    private var selCondition: Int = -1

    protected lateinit var mTaskCodeLabels: List<ConditionData>
    private lateinit var mNextTrialModes:List<List<Int>>
    protected lateinit var subject: SubjectBasicParcel

    // Longitudinal functionality
    private var sessionsData:Array<String> = emptyArray()
    private var nSpinnerItems: Int = 0

    companion object {
        @JvmStatic val EVENT_SUBJECT:String = "subject"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_subject_info_basic, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSubjectInfoBasicBinding.bind(mView)
        initData()
    }

    protected open fun initData() {
        val subj: SubjectBasicParcel? = arguments?.getParcelable(EVENT_SUBJECT)
        if (subj == null) {
            showAlert(
                requireActivity(),
                resources.getString(R.string.critical_error),
                "${resources.getString(R.string.empty_subject_parcel)}\n${resources.getString(R.string.restart_app_suggestion)}"
            )
            dismiss()
            return
        } else subject = subj

        val ntm         = getCompanionObjectMethod(subject.classes[0], "getNextTrialModes")
        mNextTrialModes = ntm.first?.call(ntm.second, requireContext()) as List<List<Int>>

        val ci          = getCompanionObjectMethod(subject.classes[0], "getConditionsInfo")
        mTaskCodeLabels = ci.first?.call(ci.second, requireContext()) as List<ConditionData>

        //------------------------------------------------------
        // SET SPINNERS (SUB TASKS, TRIAL MANAGERS & POPULATION)
        //------------------------------------------------------
        setConditions(mTaskCodeLabels)
        setPopulation(selCondition)
        setLongitudinalSpinner()

        //------------------------------------------------------
        // SUBJECT DEMOGRAPHIC
        //------------------------------------------------------
        binding.txtName.setText(subj.label)

        if (subj.age != -1) binding.txtAge.setText(subj.age.toString())
        else binding.txtAge.setText("")

        if (subj.gender != -1)  binding.radioGroupGender.check(binding.radioGroupGender.getChildAt(subj.gender).id)
        else                    binding.radioGroupGender.clearCheck()

        //------------------------------------------------------
        // trials manager (label + spinner)
        //------------------------------------------------------
        with(binding.spTrialManager){

            if(subj.trman_type == TestBasic.Companion.TEST_TRMAN_ADAPTIVE || subj.trman_type == TestBasic.Companion.TEST_TRMAN_FIXED)
                visibility                          = View.INVISIBLE
            else{
                // is TEST_TRMAN_CHOOSE_FIXED or TEST_TRMAN_CHOOSE_ADAPTIVE
                visibility                          = View.VISIBLE
                val trial_managers                  = resources.getStringArray(R.array.trial_manager_types)
                val adapter                         = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    trial_managers
                )
                binding.spTrialManager.adapter    = adapter
                if(subj.trman_type == TestBasic.Companion.TEST_TRMAN_CHOOSE_FIXED)
                        setSelection(0)
                else    setSelection(1)
            }
        }

        with(binding.labTrialManager){
            visibility =    if(subj.trman_type == TestBasic.Companion.TEST_TRMAN_ADAPTIVE || subj.trman_type == TestBasic.Companion.TEST_TRMAN_FIXED)   View.INVISIBLE
                            else                                                                                                    View.VISIBLE
        }

        binding.spTrialManager.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setTrialManager(binding.spTrialManager.selectedItem)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        //------------------------------------------------------
        // NEXT TRIAL MODALITY
        //------------------------------------------------------
        // swInteractive is visible only in TEST_NEXTTRIAL_BUTTON & TEST_NEXTTRIAL_AUTO
        // when there isn't an answer to give
        subject.nextTrailModality = mNextTrialModes[selCondition][0]

        when (subject.nextTrailModality) {

            TestBasic.Companion.TEST_NEXTTRIAL_BUTTON -> {
                binding.swInteractive.visibility   = View.VISIBLE
                binding.swInteractive.isChecked = true
            }
            TestBasic.Companion.TEST_NEXTTRIAL_AUTO -> {
                binding.swInteractive.visibility   = View.GONE
                binding.swInteractive.isChecked = false
            }
            TestBasic.Companion.TEST_NEXTTRIAL_NOCHOOSE,
            TestBasic.Companion.TEST_NEXTTRIAL_AUTO,
            TestBasic.Companion.TEST_NEXTTRIAL_VOICE_ANSWER,
            TestBasic.Companion.TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER,
            TestBasic.Companion.TEST_NEXTTRIAL_ANSWER -> binding.swInteractive.visibility   = View.GONE
        }
        //------------------------------------------------------
        // noise visibility (switch)
        //------------------------------------------------------
        with(binding.swWhiteNoise){

            visibility = when(subj.whitenoise){
                TestBasic.Companion.TEST_SWITCH_DISABLED,
                TestBasic.Companion.TEST_SWITCH_ENABLED -> View.INVISIBLE
                else                          -> View.VISIBLE
            }

            isChecked = when(subj.whitenoise){
                TestBasic.Companion.TEST_SWITCH_DISABLED,
                TestBasic.Companion.TEST_SWITCH_CHOOSE_OFF -> false
                else                             -> true
            }
        }
        //------------------------------------------------------
        // can repeat trial (switch)
        //------------------------------------------------------
        with(binding.swRepeatTrial){

            visibility = when(subj.canRepeat){
                TestBasic.Companion.TEST_SWITCH_DISABLED,
                TestBasic.Companion.TEST_SWITCH_ENABLED -> View.INVISIBLE
                else                          -> View.VISIBLE
            }

            isChecked = when(subj.canRepeat){
                TestBasic.Companion.TEST_SWITCH_DISABLED,
                TestBasic.Companion.TEST_SWITCH_CHOOSE_OFF -> false
                else                             -> true
            }
        }
        //------------------------------------------------------
        // show result (switch)
        //------------------------------------------------------
        with(binding.swShowResult){

            visibility = when(subj.showResult){
                TestBasic.Companion.TEST_SWITCH_DISABLED,
                TestBasic.Companion.TEST_SWITCH_ENABLED -> View.INVISIBLE
                else                          -> View.VISIBLE
            }

            isChecked = when(subj.showResult){
                TestBasic.Companion.TEST_SWITCH_DISABLED,
                TestBasic.Companion.TEST_SWITCH_CHOOSE_OFF -> false
                else                             -> true
            }
        }
        //---------------------------------------------------
        // do training (switch)
        //------------------------------------------------------
        with(binding.swTraining){

            visibility = when(subj.doTraining){
                TestBasic.Companion.TEST_SWITCH_DISABLED,
                TestBasic.Companion.TEST_SWITCH_ENABLED -> View.INVISIBLE
                else                          -> View.VISIBLE
            }

            isChecked = when(subj.doTraining){
                TestBasic.Companion.TEST_SWITCH_DISABLED,
                TestBasic.Companion.TEST_SWITCH_CHOOSE_OFF -> false
                else                             -> true
            }
        }
        //------------------------------------------------------
        binding.txtName.requestFocus()      // subclasses may not have this UI elements (e.g. SampleDialog)
        //------------------------------------------------------
    }

    override fun onResume() {
        val params                  = dialog?.window!!.attributes               // Get existing layout params for the window
        params.width                = WindowManager.LayoutParams.MATCH_PARENT   // Assign window properties to fill the parent
        params.height               = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window!!.attributes = params as WindowManager.LayoutParams

        super.onResume()

        binding.btConfirm.setOnClickListener   {confirmData()}
        binding.btClear.setOnClickListener     {clear()}
        binding.btCancel.setOnClickListener    {sendResult(null)}

        binding.swInteractive.setOnCheckedChangeListener { _, b ->
            subject.nextTrailModality = when (b) {
                true -> TestBasic.Companion.TEST_NEXTTRIAL_BUTTON
                false -> TestBasic.Companion.TEST_NEXTTRIAL_AUTO
            }
        }
    }

    protected fun setConditions(tc:List<ConditionData>){

        val adapter: ArrayAdapter<ConditionData> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tc)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCondition.adapter = adapter
        nConditions         = adapter.count

        if (nConditions == 1) {
            // do not show condition spinner & set subject.type
            binding.labCondition.visibility = View.GONE
            binding.spCondition.visibility  = View.GONE
            subject.type            = mTaskCodeLabels[0].id
            selCondition            = 0
        }
        else if (nConditions > 1) {
            if(subject.type != -1) {
                // set condition spinner to subject.type
                mTaskCodeLabels.mapIndexed { index, taskCode ->
                    if (taskCode.id == subject.type){
                        binding.spCondition.setSelection(index, false)
                        selCondition            = index
                    }
                }
            }
            else {
                // set condition spinner to first sub-task
                selCondition = 0
                binding.spCondition.setSelection(selCondition)
                subject.type            = mTaskCodeLabels[0].id
            }
        }

        binding.spCondition.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setPopulation(binding.spCondition.selectedItemPosition)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setPopulation(pop_index:Int){
        allowedPopulations  = mTaskCodeLabels[pop_index].allowedPopulations
        nPopulations        = allowedPopulations.size

        val adapter: ArrayAdapter<IdLabelData> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            allowedPopulations
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPopulation.adapter = adapter

        // set condition spinner to subject.type
        selPopulation            = 0
        allowedPopulations.mapIndexed { index, pair ->
            if (pair.id == subject.population)  selPopulation = index
        }
        binding.spPopulation.setSelection(selPopulation, false)
    }

    protected open fun setTrialManager(selManager:Any){}

    private fun setLongitudinalSpinner(){

        if(!subject.isLongitudinal){
            binding.labSession.visibility       =  View.GONE
            binding.sessionSpinner.visibility   =  View.GONE
            subject.session                     = "1"

        }else{
            binding.labSession.visibility   =  View.VISIBLE
            binding.sessionSpinner.visibility      =  View.VISIBLE

            // Set up spinner data if resource is provided
            if(subject.session_spdatares != -1) {
                sessionsData = resources.getStringArray(subject.session_spdatares)
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    sessionsData
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.sessionSpinner.let { spinner ->
                    spinner.adapter = adapter
                    nSpinnerItems = adapter.count

                    if(subject.session_spsel == nSpinnerItems){
                        subject.session_spsel = nSpinnerItems -1
                        // TODO send an alert, should never happen
                    }
                    // Set selection
                    if(subject.session_spsel == TestBasic.Companion.TEST_LONGITUDINAL_TOBESELECTED)
                        binding.sessionSpinner.setSelection(0)
                    else
                        binding.sessionSpinner.setSelection(subject.session_spsel, false)
                }
            }
        }
    }

    //------------------------------------------------------------------------------------
    // UI presses
    //------------------------------------------------------------------------------------
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

            // verify whether is unique. in case the subject's "label_type_Date" file exists, ask user whether continue or change name
            sendSubjectOrChangeData(subj)
        }
    }

    protected open fun clear(){
        if (nConditions > 1)
            binding.spCondition.setSelection(-1)

        binding.spPopulation.setSelection(-1)

        binding.txtName.setText("")
        binding.txtAge.setText("")
        binding.radioGroupGender.clearCheck()

        if (subject.nextTrailModality == TestBasic.Companion.TEST_NEXTTRIAL_AUTO || subject.nextTrailModality == TestBasic.Companion.TEST_NEXTTRIAL_BUTTON) {
            binding.swInteractive.isChecked    = false
            subject.nextTrailModality   = TestBasic.Companion.TEST_NEXTTRIAL_AUTO
        }

        binding.swWhiteNoise.isChecked = true

        // Clear longitudinal spinner if visible
        if(subject.session_spsel != TEST_NO_LONGITUDINAL) binding.sessionSpinner.setSelection(0)
    }

    //------------------------------------------------------------------------------------
    // SUBJECT VALIDATION
    //------------------------------------------------------------------------------------
    // validate subject info
    protected open fun checkData():List<String>{
        val errors = mutableListOf<String>()

        if(SubjectBasicParcel.Companion.validate(binding.txtName.text.toString(), binding.txtAge.text.toString()).isNotBlank())
                                                                    errors.add(" - " + resources.getString(
                                                                        R.string.select_subject_info))

        if(binding.radioGroupGender.checkedRadioButtonId == -1)     errors.add(" - " + resources.getString(
            R.string.select_gender))
        if(binding.spCondition.selectedItemPosition == -1)          errors.add(" - " + resources.getString(
            R.string.select_condition))
        if(binding.spPopulation.selectedItemPosition == -1)         errors.add(" - " + resources.getString(
            R.string.select_population))

        // Validate longitudinal spinner if visible
        if(subject.isLongitudinal && binding.sessionSpinner.selectedItemPosition == 0)
            errors.add(" - " + "Select session")

        return errors
    }

    // subject has been already validated
    protected open fun updateSubject(): SubjectBasicParcel {
        val gender:Int              = binding.radioGroupGender.indexOfChild(binding.radioGroupGender.findViewById(
            binding.radioGroupGender.checkedRadioButtonId))

        subject.type                = mTaskCodeLabels[binding.spCondition.selectedItemPosition].id
        subject.population          = allowedPopulations[binding.spPopulation.selectedItemPosition].id

        subject.label               = binding.txtName.text.toString()
        subject.age                 = binding.txtAge.text.toString().toInt()
        subject.gender              = gender

        // only If user can select interaction modality, update his/her selection
        if (subject.nextTrailModality != TestBasic.Companion.TEST_NEXTTRIAL_NOCHOOSE &&
            subject.nextTrailModality != TestBasic.Companion.TEST_NEXTTRIAL_ANSWER &&
            subject.nextTrailModality != TestBasic.Companion.TEST_NEXTTRIAL_VOICE_ANSWER
        ) {
            subject.nextTrailModality = when (binding.swInteractive.isChecked) {
                true ->     TestBasic.Companion.TEST_NEXTTRIAL_BUTTON
                false ->    TestBasic.Companion.TEST_NEXTTRIAL_AUTO
            }
        }
        if(binding.swWhiteNoise.isVisible)
            subject.whitenoise =    if(binding.swWhiteNoise.isChecked)      TestBasic.Companion.TEST_SWITCH_ENABLED
                                    else                                    TestBasic.Companion.TEST_SWITCH_DISABLED

        if(binding.swRepeatTrial.isVisible)
            subject.canRepeat  =    if(binding.swRepeatTrial.isChecked)   TestBasic.Companion.TEST_SWITCH_ENABLED
                                    else                                  TestBasic.Companion.TEST_SWITCH_DISABLED

        if(binding.spTrialManager.isVisible)
            subject.trman_type =    if(binding.spTrialManager.selectedItemPosition == 0)    TestBasic.Companion.TEST_TRMAN_FIXED
                                    else                                                    TestBasic.Companion.TEST_TRMAN_ADAPTIVE

        if(binding.swShowResult.isVisible)
            subject.showResult =    if(binding.swShowResult.isChecked)  TestBasic.Companion.TEST_SWITCH_ENABLED
                                    else                                TestBasic.Companion.TEST_SWITCH_DISABLED

        if(binding.swTraining.isVisible)
            subject.doTraining =    if(binding.swTraining.isChecked)    TestBasic.Companion.TEST_SWITCH_ENABLED
                                    else                                TestBasic.Companion.TEST_SWITCH_DISABLED

        // Update longitudinal spinner selection if visible
        if(subject.isLongitudinal)
            subject.session = sessionsData[binding.sessionSpinner.selectedItemPosition]

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
                show2ChoisesDialog(
                    requireActivity(),
                    resources.getString(R.string.warning),
                    resources.getString(R.string.subject_present),
                    resources.getString(R.string.yes),
                    resources.getString(R.string.no),
                    {   // ok press, update subject, then continue
                        subject = subj
                        sendResult(subject)
                    },
                    {   // cancel press. stop. let user change data
                        binding.txtName.requestFocus()
                    })
            }
            else -> {  // exist at least n-block files.
                        // 1-based last block file  (if it finds lab_type_blk2.txt => return 3)
                show2ChoisesDialog(
                    requireActivity(),
                    resources.getString(R.string.warning),
                    resources.getString(R.string.subject_block_present),
                    resources.getString(R.string.continue_txt),
                    resources.getString(R.string.restart),
                    { // ok press, continue next block
                        subject = subj
                        subject.block = nextblock     // this is the only case where block is != -1
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
        val bundle = Bundle().apply {
            putParcelable(EVENT_SUBJECT, subj)
        }
        parentFragmentManager.setFragmentResult(targetRequestCode.toString(), bundle)
        dismiss()
    }
    //------------------------------------------------------------------------------------
}