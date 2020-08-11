package iit.uvip.psysuite.core.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.navigation.Navigation
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TestResult
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.bis.TestBIS
import iit.uvip.psysuite.core.tests.mmd.TestMMD
import iit.uvip.psysuite.core.tests.sample.SubjectSampleParcel
import iit.uvip.psysuite.core.tests.sample.TestSample
import iit.uvip.psysuite.core.tests.temporalbinding.SubjectBindingsParcel
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB
import iit.uvip.psysuite.core.tests.tid.SubjectTIDParcel
import iit.uvip.psysuite.core.tests.tid.TestTID
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_test.*
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.getAbsoluteFilePath
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.fragments.setNavigationResult
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.speech.SpeechRecognitionManager
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert
import org.albaspazio.core.ui.showToast
import java.util.*

/*
Three operative modalities:

- trial have an answer dialog, where user can also abort
- trial have no answer dialog, at the end of the trial, the following trial is displayed
- trial have no answer dialog, at the end of the trial, test stops and wait for user press.

normal trial-by-trial flow is:

here        : mTest.start()
within Test : show -> onTrialEnd -> EVENT_GIVE_ANSWER/EVENT_SHOW_NEXT
here        : mTest.testEvent.subscribe -> answerdialog/speechrec   -> onAnswer  |-> onNewTrial -> mTest.nextTrial ->  test end | block end | show next trial
                                        -> next button  _________________________|
within Test : mTrial.setResponse -> saveText -> doNextTrial() -> getNewTrial -> show
 */

class TestFragment : BaseFragment(
    layout              = R.layout.fragment_test,
    landscape           = false,
    hideAndroidControls = true
){

    private lateinit var mTest:TestBasic
    private var mSubjectParcel:SubjectBasicParcel?  = null

    override val LOG_TAG                            = TestFragment::class.java.simpleName
    private val ANSWER_DIALOG_TAG                   = "ANSWER_DIALOG_TAG"
    private val TARGET_FRAGMENT_REQUEST_CODE:Int    = 1

    private val disposable                          = CompositeDisposable()

    private var answerDialogFragment:AnswerDialogFragment?      = null
    private var isAnswerDialogOn:Boolean            = false

    private var isPaused:Boolean                    = false
    private var mHandler: Handler                   = Handler()

    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private var abortRecognition:Boolean            = false  // set true when I answer manually and speech rec is going to be restarted (e.g. rec busy or error)
    private lateinit var speechManager: SpeechManager
    private var vibrator:VibrationManager?          = null

    private lateinit var onsetDate: Date

    private val isDebug:Boolean                     = false
    private var currDebugInfo:String                = ""

    // ==========================================================================================================================
    // ==========================================================================================================================
    companion object {

        @JvmStatic val EVENT_ANSWER_CODE:String     = "answer_code"
        @JvmStatic val EVENT_ANSWER_RESULT:String   = "answer_result"
        @JvmStatic val EVENT_TIME_TO_ANSWER:String  = "answer_time"

        fun newIntent(resp: String, elapsedTime: Int, resp_id: Int): Intent {
            val intent = Intent()
            intent.putExtra(EVENT_ANSWER_RESULT, resp)
            intent.putExtra(EVENT_TIME_TO_ANSWER, elapsedTime)
            intent.putExtra(EVENT_ANSWER_CODE, resp_id)
            return intent
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        speechRecognitionManager    = SpeechRecognitionManager(requireContext())
        speechManager               = SpeechManager(resources, requireContext())
        vibrator                    = VibrationManager(requireContext()).init()

        mSubjectParcel              = arguments?.getParcelable(TestBasic.TESTINFO_BUNDLE_LABEL) ?: return

        try{
            when(mSubjectParcel!!.type)
            {
                TestBasic.TEST_BISECTION_AUDIO,
                TestBasic.TEST_BISECTION_TACTILE,
                TestBasic.TEST_BISECTION_AUDIO_TACTILE,
                TestBasic.TEST_BISECTION_AUDIO_VIDEO    -> mTest = TestBIS(requireContext(), requireActivity(), this, mSubjectParcel!!, vibrator, circleView, isDebug)

                TestBasic.TEST_MUSICAL_METERS           -> mTest = TestMMD(requireContext(), requireActivity(), this, mSubjectParcel!!, isDebug)

                TestBasic.TEST_TID_SHORT_AUDIO,
                TestBasic.TEST_TID_SHORT_TACTILE,
                TestBasic.TEST_TID_LONG_AUDIO,
                TestBasic.TEST_TID_LONG_TACTILE         -> mTest = TestTID(requireContext(), requireActivity(), this, mSubjectParcel as SubjectTIDParcel, vibrator, isDebug)

                TestBasic.TEST_ATB_TIME_SINGLESTIM,
                TestBasic.TEST_ATB_TIME_DOUBLESTIM,
                TestBasic.TEST_ATB_TIME_SINGLESTIM_TOD,
                TestBasic.TEST_ATB_TIME_DOUBLESTIM_TOD,
                TestBasic.TEST_ATB_TIME_INF             -> mTest = TestATB(requireContext(), requireActivity(), this, mSubjectParcel as SubjectBindingsParcel, vibrator, isDebug)

                TestBasic.TEST_ATVB_TIME_S_UNBAL,
                TestBasic.TEST_ATVB_TIME_S_BAL,
                TestBasic.TEST_ATVB_TIME_D_UNBAL,
                TestBasic.TEST_ATVB_TIME_D_BAL          -> mTest = TestATVB(requireContext(), requireActivity(), this, mSubjectParcel as SubjectBindingsParcel, vibrator, circleView, isDebug)

                TestBasic.TEST_SAMPLE_ALIGNED,
                TestBasic.TEST_SAMPLE_SHIFTED,
                TestBasic.TEST_SAMPLE_PAIR              -> mTest = TestSample(requireContext(), requireActivity(), this, mSubjectParcel as SubjectSampleParcel, vibrator, circleView, isDebug)

                else    -> {
                    showAlert(requireActivity(),resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
                    Navigation.findNavController(requireView()).popBackStack()
                    return
                }
            }
        }
        catch (e:Exception){
            showAlert(requireActivity(),resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
            showAlert(requireActivity(),resources.getString(R.string.critical_error), e.toString())
            Navigation.findNavController(requireView()).popBackStack()
            return
        }

        bt_next.visibility      = View.INVISIBLE
        bt_abort.visibility     = View.INVISIBLE
        bt_pause.visibility     = View.INVISIBLE
        txtDebugInfo.visibility = View.INVISIBLE

        if (mTest.abortMode == TestBasic.TEST_ABORT_ALWAYS){
            bt_abort.visibility = View.VISIBLE
            bt_pause.visibility = View.VISIBLE
        }

        mHandler.postDelayed({
            mTest.start()

            if (mTest.showTrialsID == TestBasic.TEST_SHOWTRIALS_ALWAYS) showTrialId()

        }, 1000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.shutdown()
    }

    override fun onResume() {
        super.onResume()

        setEventsFlow()

        bt_abort.setOnClickListener{
            onAbortTest()
        }

        bt_pause.setOnClickListener{
            if(isPaused){
                bt_pause.text = resources.getString(R.string.pause)
                bt_pause.visibility = View.INVISIBLE
                onNewTrial()
            }
            else{
                mHandler.removeCallbacksAndMessages(null)
                bt_pause.text = resources.getString(R.string.resume)
            }
            isPaused = !isPaused
        }
    }

    override fun onPause(){
        super.onPause()
        disposable.clear()
    }

    // here I manage all trial-by-trial behaviours invoked by Tests
    // normal flow is
    private fun setEventsFlow(){

        // button is shown when an answer dialog is not displayed
        bt_next.setOnClickListener{

            bt_next.visibility      = View.INVISIBLE
            bt_pause.visibility     = View.INVISIBLE

            onNewTrial()
        }

        if(!this::mTest.isInitialized) return

        mTest.testEvent
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            when(it.first){
                TestBasic.EVENT_STIMULI_START       -> {}
                TestBasic.EVENT_STIMULI_END         -> {}

                TestBasic.EVENT_GIVE_ANSWER         -> showAnswerDialog()
                TestBasic.EVENT_GIVE_VOCAL_ANSWER   -> {
                                                        bt_abort.visibility = View.VISIBLE
                                                        listenForVocalAnswer(mTest.validAnswers)
                }
                TestBasic.EVENT_SHOW_NEXT_BUTTON    -> showNext()

                // called by SubTests' nextTrial
                TestBasic.EVENT_UPDATE_TRIAL_ID     -> {
                                                        try {
                                                            val dur = it.second as Long
                                                            showTrialId(dur)
                                                        }catch(e:Exception){
                                                            e.printStackTrace()
                                                            showTrialId(1000L)
                                                        }
                }
                TestBasic.EVENT_SHOW_ABORT          -> {
                                                        try {
                                                            val dur = it.second as Long
                                                            showShortAbort(dur)
                                                        }catch(e:Exception){
                                                            e.printStackTrace()
                                                            showShortAbort(1000L)
                                                        }
                }
                TestBasic.EVENT_SHOW_DEBUGINFO      -> {
                                                        try {
                                                            val info = it.second as String
                                                            showDebugInfo(info)
                                                        }catch(e:Exception){
                                                            e.printStackTrace()
                                                            showDebugInfo(e.toString())
                                                        }
                }
            }
        }
        .addTo(disposable)
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    // called by: 1) onActivityResult after answer, 2) speechrecognition result
    private fun onAnswer(prev_result: String = "", elapsed: Int = -1){

        // dont' know whether an answer dialog was present or it was listening for vocal response or it was playbacking something. stop all!
        abortRecognition = true
        speechRecognitionManager.stop()
        speechManager.stop()

        closeAnswerDialog()

        // call next trial & check whether it was the last => test ended
        onNewTrial(prev_result, elapsed)
    }

    // called by: onAnswer, bt_next click, bt_pause click
    private fun onNewTrial(prev_result: String = "", elapsed: Int = -1){
        when(mTest.nextTrial(prev_result, elapsed)){
            TestBasic.EVENT_TEST_END    -> onTestEnded()
            TestBasic.EVENT_BLOCK_END   -> onBlockEnded()       // ask whether interrupting the test
            else                        -> onBeforeTrialShow()  // next trial has been started
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    // manage TrialID text and abort button, called after mTest.nextTrial if test is not finished
    private fun onBeforeTrialShow(){

        txtTrialId.visibility   = View.INVISIBLE
        bt_abort.visibility     = View.INVISIBLE

        when(mTest.showTrialsID) {
            TestBasic.TEST_SHOWTRIALS_ALWAYS    -> showTrialId()
            TestBasic.TEST_SHOWTRIALS_TRIALEND  -> showTrialId(1000L)
        }
        if(mTest.abortMode == TestBasic.TEST_ABORT_ALWAYS)  bt_abort.visibility = View.VISIBLE

    }

    private fun onTestEnded(){
        showToast(getText(R.string.test_ended).toString(), requireContext())
        navigateBack(TestBasic.TEST_COMPLETED, listOf(mTest.getAbsoluteResultFilePath(), mTest.closeSummary()))
    }

    // user wanted to interrupt test during a block (ask whether deleting results file and it)
    private fun onAbortTest(){

        mHandler.removeCallbacksAndMessages(null)
        show2ChoisesDialog(requireActivity(),
            requireContext().resources.getString(R.string.warning),
            requireContext().resources.getString(R.string.test_aborted),
            requireContext().resources.getString(R.string.keep),         // ok
            requireContext().resources.getString(R.string.delete),       // cancel
            { /* okClb */
                mTest.abortTest(false)
                navigateBack(TestBasic.TEST_ABORT, listOf(mTest.getAbsoluteResultFilePath(), mTest.closeSummary()))
            },
            { /* cancelClb*/
                mTest.abortTest(true)
                navigateBack(TestBasic.TEST_ABORT, listOf())
            })
    }

    // user ended a planned block. can continue or stop (result file is renamed "*_blkX")
    private fun onBlockEnded(){
        show2ChoisesDialog(requireActivity(),resources.getString(R.string.warning),resources.getString(R.string.block_ended), resources.getString(R.string.continue_label), resources.getString(R.string.stop),
            { /* okClb */       mTest.startNewBlock() },
            { /* cancelClb*/    onStoppedAfterBlock() })
    }

    // user wanted to interrupt test after an end block (send data). rename current res file
    private fun onStoppedAfterBlock(){
        val newresfilename = mTest.stopTestAfterBlock()
        mHandler.removeCallbacksAndMessages(null)
        navigateBack(TestBasic.BLOCK_COMPLETED, listOf(getAbsoluteFilePath(newresfilename).second, mTest.closeSummary()))
    }

    // results_file can be empty. it can have only the first (result) file not empty or having both results and summary
    private fun navigateBack(result_code:Int, results_file:List<String>){

        val files_list:ArrayList<String> = when(results_file.isEmpty()){
            true -> arrayListOf()
            false -> when(results_file[0].isEmpty()){
                        true    -> arrayListOf()
                        false   -> {
                                    val l = arrayListOf(results_file[0])
                                    if(results_file[1].isNotEmpty())    l.add(results_file[1])
                                    l
                                }
                    }
        }

        // data class TestResult      (code:Int=-1, mailsubject:String, mailbody:String,                       res_files:ArrayList<String> = arrayListOf(),  testClass:String)
        setNavigationResult(TestResult(result_code, mTest.mTestLabel, mSubjectParcel!!.composeSubjectFileName(requireContext()), files_list, mTest.javaClass.name), TestBasic.TEST_BUNDLE_RESULT_LABEL)
        Navigation.findNavController(requireView()).popBackStack()
    }

    //---------------------------------------------------------------------------------------------------------------------------------------
    // ELEMENT VISIBILITY
    //---------------------------------------------------------------------------------------------------------------------------------------
    // called by TestBasic.EVENT_SHOW_NEXT_BUTTON
    private fun showNext() {
        bt_next.visibility = View.VISIBLE

        if(mTest.abortMode == TestBasic.TEST_ABORT_ALWAYS || mTest.abortMode == TestBasic.TEST_ABORT_TRIALEND)
            bt_abort.visibility = View.VISIBLE
    }

    private fun showShortAbort(remove: Long = 1000L){
        bt_abort.visibility = View.VISIBLE
        bt_pause.visibility = View.VISIBLE

        if(remove > 0){
            mHandler.postDelayed({
                bt_abort.visibility = View.INVISIBLE
                bt_pause.visibility = View.INVISIBLE
                onNewTrial()
            }, remove)
        }
    }

    private fun showTrialId(remove:Long=0){
        txtTrialId.visibility   = View.VISIBLE
        txtTrialId.text         = resources.getString(R.string.trial_id, (mTest.currTrial + 1).toString())
        if(remove > 0){
            mHandler.postDelayed({
                txtTrialId.visibility = View.INVISIBLE
            }, remove)
        }
    }

    private fun showDebugInfo(msg:String, remove:Long=0){
        currDebugInfo           = msg
        txtDebugInfo.visibility = View.VISIBLE
        txtDebugInfo.text       = currDebugInfo
        if(remove > 0){
            mHandler.postDelayed({
                txtDebugInfo.visibility = View.INVISIBLE
            }, remove)
        }
    }

    //=======================================================================================================================================
    // ANSWERS
    //=======================================================================================================================================
    // create answer dialog and process response (repeat same trial or show next one
    private fun showAnswerDialog(){

        val b = Bundle()
        b.putInt("trial_id",    mTest.currTrial)
        b.putInt("tot_trials",  mTest.nTrials)
        b.putString("question", mTest.mQuestion)
        b.putStringArrayList("answers", mTest.validAnswers as ArrayList<String>)
        b.putString("debug", currDebugInfo)

        answerDialogFragment = AnswerDialogFragment.newInstance("Some Title")
        (answerDialogFragment as AnswerDialogFragment).setTargetFragment(this , TARGET_FRAGMENT_REQUEST_CODE)
        (answerDialogFragment as AnswerDialogFragment).arguments    = b
        (answerDialogFragment as AnswerDialogFragment).isCancelable = false
        (answerDialogFragment as AnswerDialogFragment).show(parentFragmentManager, ANSWER_DIALOG_TAG)
        isAnswerDialogOn = true
    }

    private fun closeAnswerDialog(){
        if(isAnswerDialogOn) {
            (answerDialogFragment as AnswerDialogFragment).dismiss() //        val dialogFragment:DialogFragment? = parentFragmentManager.findFragmentByTag(ANSWER_DIALOG_TAG) as DialogFragment
            isAnswerDialogOn = false
        }
    }

    // answer !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        // Make sure fragment codes match up
        if(requestCode == TARGET_FRAGMENT_REQUEST_CODE)
        {
            when(data?.getIntExtra(EVENT_ANSWER_CODE, 0))
            {
                TestBasic.EVENT_ANSWER_GIVEN -> {
                    val result      = data.getStringExtra(EVENT_ANSWER_RESULT)
                    val elapsedTime = data.getIntExtra(EVENT_TIME_TO_ANSWER, -1)
                    onAnswer(result!!, elapsedTime)
                }
                TestBasic.EVENT_TRIAL_REPEAT    -> mTest.repeatTrial()
                TestBasic.EVENT_TRIAL_ABORT     -> onAbortTest()
            }
        }
    }

    // start recognizing and process response (repeat same trial or show next one)
    private fun listenForVocalAnswer(valid_results:List<String> = listOf()) {
        abortRecognition        = false
        bt_abort?.visibility    = View.VISIBLE
        onsetDate               = Date()
        speechRecognitionManager.getSpeechInput()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    when (it.first) {
                        SpeechRecognitionManager.REC_SUCCESS -> {
                            Log.d("", "recognized word $it")

                            // check whether given response is allowed
                            val res:Boolean =   if(valid_results.isEmpty())     true
                                                else                            valid_results.contains(it.second)

                            if (res) {
                                bt_abort.visibility = View.INVISIBLE
                                val elapsedTime     = getTimeDifference(onsetDate)
                                onAnswer(it.second!!, elapsedTime)

                            } else
                                // text recognized but not allowed
                                speechManager.speak(resources.getString(org.albaspazio.core.R.string.char_recognition_wrong), TextToSpeech.QUEUE_FLUSH, clb={ if(!abortRecognition)   listenForVocalAnswer(valid_results)})
                        }
                        else ->

                            if(it.first == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                                if (!abortRecognition)
                                    listenForVocalAnswer(valid_results)
                            }
                            else
                            // RECOGNIZER ERROR
                                speechManager.speak(it.second!!, TextToSpeech.QUEUE_FLUSH, clb={ if(!abortRecognition)   listenForVocalAnswer(valid_results) })
                    }
                }
            )
            .addTo(disposable)
    }
    // ========================================================================================================================================
}