package iit.uvip.psysuite.core.tests.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.synthetic.main.fragment_subject_info_sample.*
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert


open class SubjectSampleDialogFragment: SubjectBasicDialogFragment(), AdapterView.OnItemSelectedListener
{
    override val LOG_TAG: String = SubjectSampleDialogFragment::class.java.simpleName

    companion object {
        @JvmStatic val EVENT_SUBJECT:String = "subject"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_sample, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spCondition.onItemSelectedListener  = this
        spTactile.onItemSelectedListener    = this
        spAudio.onItemSelectedListener      = this
        spVisual.onItemSelectedListener     = this
    }

    override fun onResume() {

        val params                  = dialog?.window!!.attributes               // Get existing layout params for the window
        params.width                = WindowManager.LayoutParams.MATCH_PARENT   // Assign window properties to fill the parent
        params.height               = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window!!.attributes = params as WindowManager.LayoutParams

        super.onResume()
        setListeners()
    }

    // cannot call super.initData as some UI elements are missing
    override fun initData(subj: SubjectBasicParcel) {

        //------------------------------------------------------
        // SUB TASKS
        //------------------------------------------------------
        setConditions(mTaskCodeLabels)

        etDurationAudio.isEnabled   = false
        spAudio.isEnabled           = false
        spAudioResource.isEnabled   = false

        etDurationVisual.isEnabled  = false
        spVisual.isEnabled          = false

        spTactile.isEnabled         = false
        etTactileSchema.isEnabled   = false
        etTactileAmplitude.isEnabled= false

        ArrayAdapter.createFromResource(requireContext(), R.array.sample_audio_types, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spAudio.adapter = adapter
        }
        spAudio.setSelection(0)

        ArrayAdapter.createFromResource(requireContext(), R.array.sample_visual_types, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spVisual.adapter = adapter
        }
        spVisual.setSelection(0)

        ArrayAdapter.createFromResource(requireContext(), R.array.sample_tactile_types, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spTactile.adapter = adapter
        }
        spTactile.setSelection(0)

        ArrayAdapter.createFromResource(requireContext(), R.array.sample_audioresources_array, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spAudioResource.adapter = adapter
        }
        spAudioResource.setSelection(0)

        etPairStimDistance.isEnabled = false

        etRepetitionNum.setText("10000")

        //------------------------------------------------------
        // NEXT TRIAL MODALITY
        //------------------------------------------------------
        // swInteractive is always visible
        subject.nextTrailModality = mNextTrialModes[0][0]   // button

        when (subject.nextTrailModality) {

            TestBasic.TEST_NEXTTRIAL_BUTTON -> {
                swInteractive?.isChecked = true
            }
            TestBasic.TEST_NEXTTRIAL_AUTO -> {
                swInteractive?.isChecked = false
            }
        }
    }

    private fun setListeners() {
        bt_confirm.setOnClickListener { confirmData() }
        bt_clear.setOnClickListener { clear() }
        bt_cancel.setOnClickListener { sendResult(null) }

        swAudio.setOnCheckedChangeListener { _, b ->
            etDurationAudio.isEnabled   = b
            spAudio.isEnabled           = b
            spAudioResource.isEnabled   = b

            if (b) updateAudio()
        }

        swVisual.setOnCheckedChangeListener { _, b ->
            etDurationVisual.isEnabled  = b
            spVisual.isEnabled          = b
        }

        swTactile.setOnCheckedChangeListener { _, b ->
            spTactile.isEnabled         = b
            etTactileSchema.isEnabled   = b
            etTactileAmplitude.isEnabled= b

            if (b) updateTactile()
        }

        swInteractive?.setOnCheckedChangeListener { _, b ->
            subject.nextTrailModality = when (b) {
                true -> TestBasic.TEST_NEXTTRIAL_BUTTON
                false -> TestBasic.TEST_NEXTTRIAL_AUTO
            }
        }
    }
    //==========================================================================================================
    //  UPDATE UI ELEMENTS
    //==========================================================================================================
    override fun confirmData(){

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
                subject = subj as SubjectSampleParcel
                sendResult(subject)
            }
        }
    }

    // cannot call super.onClear as some UI elements are missing
    override fun clear(){

        swAudio.isChecked   = false
        swTactile.isChecked = false
        swVisual.isChecked  = false

        updateShifted(false)

        if (nConditions > 1)
            spCondition.setSelection(-1)

        if (subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_AUTO || subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_BUTTON) {
            swInteractive?.isChecked    = false
            subject.nextTrailModality   = TestBasic.TEST_NEXTTRIAL_AUTO
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    // on change spTactile/spAudio
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        updateTactile()
        updateAudio()
        updateVisual()
        updateCondition()
    }

    private fun updateCondition(){
        when(spCondition.selectedItemPosition) {
            0   ->  {
                etPairStimDistance.isEnabled = false
                updateShifted(false)
            }
            1   ->  {
                etPairStimDistance.isEnabled = false
                updateShifted(true)
            }
            2   ->  {
                etPairStimDistance.isEnabled = true
                updateShifted(false)
            }
        }
    }

    private fun updateTactile(){
        when(spTactile.selectedItemPosition) {
            0   ->  labTactileDuration.text = resources.getString(R.string.duration)
            1   ->  labTactileDuration.text = resources.getString(R.string.pattern)
        }
    }

    private fun updateVisual(){
        when(spVisual.selectedItemPosition) {
            0   ->  {
                etVisualDrawableOff.isEnabled   = false
                etVisualDrawableOn.isEnabled    = false
            }
            1   ->  {
                etVisualDrawableOff.isEnabled   = true
                etVisualDrawableOn.isEnabled    = true
            }
        }
    }

    private fun updateAudio(){
        when(spAudio.selectedItemPosition) {
            0   ->  spAudioResource.isEnabled   = false
            1   ->  spAudioResource.isEnabled   = true
        }
    }

    private fun updateShifted(enable:Boolean){
        etShiftedAudio.isEnabled    = enable
        etShiftedVisual.isEnabled   = enable
        etShiftedTactile.isEnabled  = enable
    }

    //------------------------------------------------------------------------------------
    // ACCESSORY
    //------------------------------------------------------------------------------------

    private fun calculateSources():Int{
        var src = 0
        if(swAudio.isChecked) {
            src = when (spAudio.selectedItemPosition) {
                0       ->  src or TestBasic.STIM_TYPE_A1
                1       ->  src or TestBasic.STIM_TYPE_A2
                else    ->  src or TestBasic.STIM_TYPE_A3
            }
            (subject as SubjectSampleParcel).audioDuration   = etDurationAudio.text.toString().toLong()
            (subject as SubjectSampleParcel).audioResource   = spAudioResource.selectedItem as String
            (subject as SubjectSampleParcel).audioVolume     = etAudioVolume.text.toString().toFloat()
        }

        if(swTactile.isChecked) {
            src = when (spTactile.selectedItemPosition) {
                0       ->  src or TestBasic.STIM_TYPE_T1
                else    ->  src or TestBasic.STIM_TYPE_T2
            }
            (subject as SubjectSampleParcel).tactileAmplitude    = etTactileAmplitude.text.toString().toInt()
            (subject as SubjectSampleParcel).tactileSequence     = etTactileSchema.text.toString()
        }

        if(swVisual.isChecked) {
            src = when (spVisual.selectedItemPosition) {
                0       ->  src or TestBasic.STIM_TYPE_V1
                else    ->  src or TestBasic.STIM_TYPE_V2
            }
            (subject as SubjectSampleParcel).visualDuration      = etDurationVisual.text.toString().toLong()
            (subject as SubjectSampleParcel).visualDrawableOn    = etVisualDrawableOn.text.toString().toInt()
            (subject as SubjectSampleParcel).visualDrawableOff   = etVisualDrawableOff.text.toString().toInt()
        }
        return src
    }

    // validate subject info
    override fun checkData():List<String>{

        val errors = mutableListOf<String>()

        if(calculateSources() == 0)                 errors.add(resources.getString(R.string.select_source))
        if (spCondition.selectedItemPosition == -1) errors.add(" - " + resources.getString(R.string.select_condition))

        return errors
    }

    // subject has been already validated
     override fun updateSubject(): SubjectBasicParcel{

        subject.type                = mTaskCodeLabels[spCondition.selectedItemPosition].id

        subject.nextTrailModality = when (swInteractive?.isChecked) {
            true -> TestBasic.TEST_NEXTTRIAL_BUTTON
            false -> TestBasic.TEST_NEXTTRIAL_AUTO
            null -> subject.nextTrailModality
        }

        (subject as SubjectSampleParcel).stim_sources = calculateSources()

        when(spCondition.selectedItemPosition){
            1 -> (subject as SubjectSampleParcel).shiftedParams = listOf(   etShiftedAudio.text.toString().toLong(),
                                                                            etShiftedVisual.text.toString().toLong(),
                                                                            etShiftedTactile.text.toString().toLong())
            2 -> (subject as SubjectSampleParcel).pairDistance = etPairStimDistance.text.toString().toLong()
        }

        (subject as SubjectSampleParcel).repetitions = etRepetitionNum.text.toString().toInt()

        if((subject as SubjectSampleParcel).repetitions > 1)
            (subject as SubjectSampleParcel).iti = etITI.text.toString().toLong()

        return subject
    }

    private fun sendResult(subj: SubjectBasicParcel?) {
        if (targetFragment == null)     return

        val intent = Intent()
        intent.putExtra(EVENT_SUBJECT, subj)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    // check whether subject's "label_type_Date" file exists, ask user whether continue or change name
    private fun manageSubjectFileExistence(subj: SubjectBasicParcel):Boolean{
        return if(subj.existSubjectFile(requireContext()) > -1){
            show2ChoisesDialog(requireActivity(), resources.getString(R.string.warning),
                resources.getString(R.string.subject_present), resources.getString(R.string.yes), resources.getString(R.string.no),
                { // ok press, update subject, then continue
                    subject = subj as SubjectSampleParcel
                    sendResult(subject)
                },{})
            false
        }
        else true
    }
}