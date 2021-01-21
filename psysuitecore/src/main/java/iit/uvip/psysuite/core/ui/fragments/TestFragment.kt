package iit.uvip.psysuite.core.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.bis.TestBIS
import iit.uvip.psysuite.core.tests.mmd.TestMMD
import iit.uvip.psysuite.core.tests.sample.SubjectSampleParcel
import iit.uvip.psysuite.core.tests.sample.TestSample
import iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB
import iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB
import iit.uvip.psysuite.core.tests.tfi.TestTFI
import iit.uvip.psysuite.core.tests.tid.SubjectTIDParcel
import iit.uvip.psysuite.core.tests.tid.TestTID
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.core.utility.getIds
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_test.*
import org.albaspazio.core.accessory.*
import org.albaspazio.core.filesystem.getAbsoluteFilePath
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.fragments.setNavigationResult
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.speech.SpeechRecognitionManager
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert
import org.albaspazio.core.ui.showToast
import java.util.*
import kotlin.reflect.KFunction


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

    private lateinit var mTest: TestBasic
    private var mSubjectParcel:SubjectBasicParcel?  = null

    override val LOG_TAG                     = TestFragment::class.java.simpleName
    private val ANSWER_DIALOG_TAG                   = "ANSWER_DIALOG_TAG"

    private var answerDialogFragment:DialogFragment? = null
    private var isAnswerDialogOn:Boolean            = false

    private var userPopulation:Int                  = Populations.POPULATION_TD
    private var isBlindUser:Boolean                 = false
    private var isDeafUser:Boolean                  = false

    private var isPaused:Boolean                    = false
    private var mHandler: Handler                   = Handler(Looper.getMainLooper())

    private var abortRecognition:Boolean            = false  // set true when I answer manually and speech rec is going to be restarted (e.g. rec busy or error)
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var speechManager: SpeechManager
    private var vibrator:VibrationManager?          = null

    private lateinit var onsetDate: Date

    private var currDebugInfo:String                = ""

    var showResult:Boolean                          = false

    private lateinit var answerDialogRef:Pair<KFunction<*>?, Any?>
    // ==========================================================================================================================
    // ==========================================================================================================================
    companion object {

        @JvmStatic val TRG_REQ_CODE_ANSWER:Int        = 1
        @JvmStatic val TRG_REQ_CODE_INSTRUCTIONS:Int  = 2

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

    // main access point. it does:
    // - (try) instanciate the correct TestClass
    // - call mTest.initTest() and wait for EVENT_TEST_SETUP_COMPLETED
    // - onTestSetupComplete() -> mTest.adjustBlocks, mTest.start()
    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        bt_next.visibility      = View.INVISIBLE
        bt_abort.visibility     = View.INVISIBLE
        bt_pause.visibility     = View.INVISIBLE
        txtDebugInfo.visibility = View.INVISIBLE

        vibrator                    = VibrationManager(requireContext()).init()
        speechRecognitionManager    = SpeechRecognitionManager(requireContext())
        mSubjectParcel              = arguments?.getParcelable(TestBasic.TESTINFO_BUNDLE_LABEL) ?: return
        speechManager               = SpeechManager(requireContext()){
            try{
                when(mSubjectParcel!!.type){

                    TestBasic.TEST_BISECTION_AUDIO,
                    TestBasic.TEST_BISECTION_TACTILE,
                    TestBasic.TEST_BISECTION_AUDIO_TACTILE,
                    TestBasic.TEST_BISECTION_AUDIO_VIDEO -> mTest = TestBIS(
                        requireContext(), requireActivity(), this, mSubjectParcel!!, vibrator, circleView, speechManager)

                    TestBasic.TEST_MUSICAL_METERS -> mTest = TestMMD(
                        requireContext(), requireActivity(), this, mSubjectParcel!!, speechManager)

                    TestBasic.TEST_TID_SHORT_AUDIO,
                    TestBasic.TEST_TID_SHORT_TACTILE,
                    TestBasic.TEST_TID_LONG_AUDIO,
                    TestBasic.TEST_TID_LONG_TACTILE -> mTest = TestTID(
                        requireContext(), requireActivity(), this, mSubjectParcel as SubjectTIDParcel, vibrator, speechManager)

                    TestBasic.TEST_ATB_TIME_SINGLESTIM,
                    TestBasic.TEST_ATB_TIME_DOUBLESTIM,
                    TestBasic.TEST_ATB_TIME_SINGLESTIM_TOD,
                    TestBasic.TEST_ATB_TIME_DOUBLESTIM_TOD,
                    TestBasic.TEST_ATB_TIME_INF -> mTest = TestATB(
                        requireContext(), requireActivity(), this, mSubjectParcel!!, vibrator, speechManager)

                    TestBasic.TEST_AVB_TIME_SINGLESTIM,
                    TestBasic.TEST_AVB_TIME_DOUBLESTIM,
                    TestBasic.TEST_AVB_TIME_SINGLESTIM_TOD,
                    TestBasic.TEST_AVB_TIME_DOUBLESTIM_TOD,
                    TestBasic.TEST_AVB_TIME_INF -> mTest = TestAVB(
                        requireContext(), requireActivity(), this, mSubjectParcel!!, circleView, speechManager)

                    TestBasic.TEST_TVB_TIME_SINGLESTIM,
                    TestBasic.TEST_TVB_TIME_DOUBLESTIM,
                    TestBasic.TEST_TVB_TIME_SINGLESTIM_TOD,
                    TestBasic.TEST_TVB_TIME_DOUBLESTIM_TOD,
                    TestBasic.TEST_TVB_TIME_INF -> mTest = TestTVB(
                        requireContext(), requireActivity(), this, mSubjectParcel!!, vibrator, circleView, speechManager)

                    TestBasic.TEST_ATVB_TIME_S_UNBAL,
                    TestBasic.TEST_ATVB_TIME_S_BAL,
                    TestBasic.TEST_ATVB_TIME_S_BAL2,
                    TestBasic.TEST_ATVB_TIME_D_UNBAL,
                    TestBasic.TEST_ATVB_TIME_D_BAL -> mTest = TestATVB(
                        requireContext(), requireActivity(), this, mSubjectParcel!!, vibrator, circleView, speechManager)

                    TestBasic.TEST_SAMPLE_ALIGNED,
                    TestBasic.TEST_SAMPLE_SHIFTED,
                    TestBasic.TEST_SAMPLE_PAIR -> mTest = TestSample(
                        requireContext(), requireActivity(), this, mSubjectParcel as SubjectSampleParcel, vibrator, circleView, speechManager)

                    TestBasic.TEST_TFI,
                    TestBasic.TEST_TFI_TODDLERS -> mTest = TestTFI(
                        requireContext(), requireActivity(), this, mSubjectParcel!!, vibrator, circleView, speechManager)

                    else    -> {
                        Log.e("TestFragment", "Test non riconosciuto")
                        showAlert(requireActivity(), resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
                        Navigation.findNavController(requireView()).popBackStack()
                        return@SpeechManager
                    }
                }

                userPopulation  = mSubjectParcel!!.population
                isBlindUser     = Populations.vi_populations.getIds().contains(userPopulation)
                isDeafUser      = Populations.ai_populations.getIds().contains(userPopulation)

                if(isBlindUser && !speechManager.isValid){
                    showAlert(requireActivity(), resources.getString(R.string.error), resources.getString(R.string.contact_developer))
                    return@SpeechManager
                }


                // get a reference to the AnswerDialogFragment
                val answerDialogClass = if(isBlindUser)
                                            // population is visually impaired. use AnswerGestureDF
                                            "iit.uvip.psysuite.core.ui.fragments.answers.AnswerGestureDialogFragment"
                                        else {
                                            if (mSubjectParcel!!.classes.size > 1 && mSubjectParcel!!.classes[1].isNotEmpty())
                                                mSubjectParcel!!.classes[1]
                                            else "iit.uvip.psysuite.core.ui.fragments.answers.TwoAFCAnswerDialogFragment"
                                        }
                answerDialogRef       = getCompanionObjectMethod(answerDialogClass, "newInstance")

                setTestEventsObservable()

                mTest.initTest()    // then wait for EVENT_TEST_SETUP_COMPLETED. while the Test asynchronously load the needed resources
            }
            catch (e: Exception){
                e.logLastTwo(LOG_TAG)
                showAlert(requireActivity(), resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
                showAlert(requireActivity(), resources.getString(R.string.critical_error), e.toString())
                Navigation.findNavController(requireView()).popBackStack()
                return@SpeechManager
            }
        }
    }

    // triggered by testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null)) run on each Test class
    private fun onTestSetupComplete(){

        // block is always -1 but when is continuing a previous session (in that case if it found ..._blk2.txt => block=3 ...thus is always > 0)
        mTest.adjustBlocks(mSubjectParcel!!.block)     // set currTrial, mCurrBlock, mTrial

        if(isBlindUser)     showAnswerDialog(TRG_REQ_CODE_INSTRUCTIONS)
        else                startTest()
    }

    private fun startTest(){

        mHandler.postDelayed({
            if (mTest.abortMode == TestBasic.TEST_ABORT_ALWAYS) {
                bt_abort.visibility = View.VISIBLE
                bt_pause.visibility = View.VISIBLE
            }
            mTest.start()
            if (mTest.showTrialsID == TestBasic.TEST_SHOWTRIALS_ALWAYS) showTrialId()

        }, 1000L)
    }

    override fun onDestroy() {
        super.onDestroy()

        if(this::speechManager.isInitialized)
            speechManager.shutdown()
    }

    override fun onResume() {
        super.onResume()

        setTestEventsObservable()

        // button is shown when an answer dialog is not displayed
        bt_next.setOnClickListener{

            bt_next.visibility      = View.INVISIBLE
            bt_pause.visibility     = View.INVISIBLE

            onNewTrial()
        }

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
    private fun setTestEventsObservable(){

        if(!this::mTest.isInitialized) return

        if(disposable.size() > 0) return

        mTest.testEvent
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            when(it.first){

                TestBasic.EVENT_TEST_SETUP_COMPLETED -> onTestSetupComplete()        // Test asynchronously loaded all its needed resources and is fully ready
                TestBasic.EVENT_GIVE_ANSWER -> showAnswerDialog(TRG_REQ_CODE_ANSWER)
                TestBasic.EVENT_GIVE_VOCAL_ANSWER -> {
                    bt_abort.visibility = View.VISIBLE
                    listenForVocalAnswer(mTest.validAnswers)
                }
                TestBasic.EVENT_SHOW_NEXT_BUTTON -> showNext()

                // called by SubTests' nextTrial
                TestBasic.EVENT_UPDATE_TRIAL_ID -> {
                    try {
                        val dur = it.second as Long
                        showTrialId(dur)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showTrialId(1000L)
                    }
                }
                TestBasic.EVENT_SHOW_ABORT -> {
                    try {
                        val dur = it.second as Long
                        showShortAbort(dur)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showShortAbort(1000L)
                    }
                }
                TestBasic.EVENT_SHOW_DEBUGINFO -> {
                    try {
                        val info = it.second as String
                        showDebugInfo(info)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showDebugInfo(e.toString())
                    }
                }
                TestBasic.EVENT_TEST_ERROR -> onTestError(it.second as String)

                TestBasic.EVENT_STIMULI_START -> {}
                TestBasic.EVENT_STIMULI_END -> {}
            }
        }
        .addTo(disposable)
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    // called by: 1) onActivityResult after answer, 2) speechrecognition result
    private fun onAnswer(prev_result: String = "", elapsed: Int = -1){

        // dont' know whether an answer dialog was present or it was listening for vocal response or it was playbacking something. stop all!
        abortRecognition = true
        mHandler.post {
            speechRecognitionManager.stop()
            speechManager.stop()
        }
        closeAnswerDialog()

        // call next trial & check whether it was the last => test ended
        onNewTrial(prev_result, elapsed)
    }

    // called by: onAnswer, bt_next click, bt_pause click
    // define whether: onTestEnded() or onBlockEnded()  or nothing (test continued or closed sending event error)
    private fun onNewTrial(prev_result: String = "", elapsed: Int = -1){
        when(mTest.nextTrial(prev_result, elapsed)){
            TestBasic.EVENT_TEST_END    -> onTestEnded()
            TestBasic.EVENT_BLOCK_END   -> onBlockEnded()       // ask whether interrupting the test
            TestBasic.EVENT_TEST_ERROR  -> {}                   // do nothing, test class close the test and send EVENT_TEST_ERROR with error message
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

    // ==========================================================================================================================================
    // ==========================================================================================================================================
    //  (READY TO) TERMINATE TEST or CONTINUE
    // ==========================================================================================================================================
    // ==========================================================================================================================================
    // => greetings & mTest.unloadStimuli() & NAVIGATEBACK
    private fun onTestEnded(){
        val msg = getText(R.string.test_ended).toString()

        mTest.unloadStimuli()

        if(isBlindUser) speechManager.speak(msg)
        else            showToast(msg, requireContext())

        navigateBack(TestBasic.TEST_COMPLETED, listOf(  mTest.getAbsoluteResultFilePath(),
            mSubjectParcel!!.getAbsoluteSubjectFilePath(),
            mTest.closeSummary()))
    }

    // user wanted to interrupt test during a block (ask whether deleting results file and it)
    // => greetings & mTest.abortTest (unloadStimuli) & NAVIGATEBACK
    private fun onAbortTest(){

        mHandler.removeCallbacksAndMessages(null)

        if(isBlindUser)     speechManager.speak(requireContext().resources.getString(R.string.test_aborted_blind))

        show2ChoisesDialog(requireActivity(),
            requireContext().resources.getString(R.string.warning),
            requireContext().resources.getString(R.string.test_aborted),
            requireContext().resources.getString(R.string.keep),         // ok
            requireContext().resources.getString(R.string.delete),       // cancel
            { /* okClb */
                mTest.abortTest(false)
                navigateBack(TestBasic.TEST_ABORTED, listOf(mTest.getAbsoluteResultFilePath(),
                                                            mSubjectParcel!!.getAbsoluteSubjectFilePath(),
                                                            mTest.closeSummary()))
            },
            { /* cancelClb*/
                mTest.abortTest(true)
                navigateBack(TestBasic.TEST_ABORTED, listOf())
            })
    }

    // TEST_EVENTS  => alert & NAVIGATEBACK
    private fun onTestError(msg: String){
        mHandler.removeCallbacksAndMessages(null)

        if(isBlindUser)     speechManager.speak(resources.getString(R.string.critical_error))

        showAlert(requireActivity(), resources.getString(R.string.critical_error), msg)
        navigateBack(TestBasic.TEST_ABORTED_WITH_ERROR, listOf( mTest.getAbsoluteResultFilePath(),
                                                                mSubjectParcel!!.getAbsoluteSubjectFilePath(),
                                                                mTest.closeSummary()))
    }

    // called when user ended a planned block= > mTest.startNewBlock() OR onStoppedAfterBlock (result file is renamed "*_blkX")
    private fun onBlockEnded(){

        if(isBlindUser) {
            mHandler.postDelayed({ // a speechManager.stop is given in OnAnswer. with a 500ms delay I start speaking after
                speechManager.speak(requireContext().resources.getString(R.string.block_ended_blind))}, 500)
        }

        show2ChoisesDialog(requireActivity(),
            resources.getString(R.string.warning),
            resources.getString(R.string.block_ended),
            resources.getString(R.string.continue_label),
            resources.getString(R.string.stop),
            { /* okClb */       mTest.startNewBlock() },
            { /* cancelClb*/    onStoppedAfterBlock() })
    }

    // user wanted to interrupt test after an end block (send data)
    // => mTest.stopTestAfterBlock (unloadStimuli + rename current res & subject files) & NAVIGATEBACK
    private fun onStoppedAfterBlock(){
        val newfilenames = mTest.stopTestAfterBlock()
        mHandler.removeCallbacksAndMessages(null)
        navigateBack(TestBasic.BLOCK_COMPLETED, listOf( getAbsoluteFilePath(newfilenames.first).second,
                                                        getAbsoluteFilePath(newfilenames.second).second,
                                                        mTest.closeSummary(newfilenames.third))) // closeSummary return absolute path or ""
    }

    /* called by:
     - onTestEnded                  TEST_COMPLETED
     - onAbortTest -> OK/cancel     TEST_ABORTED
     - onTestError                  TEST_ABORTED_WITH_ERROR
     - onStoppedAfterBlock          BLOCK_COMPLETED
    results_file can be empty. it can have only the first (result) file not empty or having both results and summary
     */
    private fun navigateBack(result_code: Int, results_file: List<String>){

        val files_list:ArrayList<String> = arrayListOf()
        results_file.map{
            if(it.isNotEmpty()) files_list.add(it)
        }
        // data class TestResult      (code:Int=-1, mailsubject:String, mailbody:String,                       res_files:ArrayList<String> = arrayListOf(),  testClass:String)
        setNavigationResult(TestResult(result_code, mTest.mTestLabel, mSubjectParcel!!.composeSubjectFileName(requireContext()), files_list, mTest.javaClass.name),
                            TestBasic.TEST_BUNDLE_RESULT_LABEL)
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

    private fun showTrialId(remove: Long = 0){
        txtTrialId.visibility   = View.VISIBLE
        txtTrialId.text         = resources.getString(
            R.string.trial_id,
            (mTest.currTrial + 1).toString()
        )
        if(remove > 0)
            mHandler.postDelayed({  txtTrialId.visibility = View.INVISIBLE  }, remove)
    }

    private fun showDebugInfo(msg: String, remove: Long = 0){
        currDebugInfo           = msg
        txtDebugInfo.visibility = View.VISIBLE
        txtDebugInfo.text       = currDebugInfo

        if(remove > 0)
            mHandler.postDelayed({    txtDebugInfo.visibility = View.INVISIBLE    }, remove)
    }

    //=======================================================================================================================================
    // ANSWERS
    //=======================================================================================================================================
    // create answer dialog and process response (repeat same trial or show next one
    private fun showAnswerDialog(trg_req_code: Int){

        val b = Bundle()
        b.putInt("trial_id", mTest.currTrial)
        b.putInt("tot_trials", mTest.nTrials)
        b.putString("question", mTest.mQuestion)
        b.putStringArrayList("answers", mTest.validAnswers as ArrayList<String>)
        b.putString("debug", currDebugInfo)
        b.putBoolean("isDebug", mSubjectParcel?.isDebug ?: false)

        b.putBoolean("show_result", mTest.showResult)
        b.putString("correct_answer", mTest.getTrialCorrectAnswer())

        answerDialogFragment = answerDialogRef.first?.call(answerDialogRef.second, "", speechManager) as DialogFragment
        if(answerDialogFragment == null){
            showAlert(requireActivity(), resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer) + "\nAnswer dialog was not available")
            return
        }
        answerDialogFragment?.setTargetFragment(this, trg_req_code)
        answerDialogFragment?.arguments    = b
        answerDialogFragment?.isCancelable = false
        answerDialogFragment?.show(parentFragmentManager, ANSWER_DIALOG_TAG)
        isAnswerDialogOn = true
    }

    private fun closeAnswerDialog(){
        if(isAnswerDialogOn) {
            answerDialogFragment?.dismiss() //        val dialogFragment:DialogFragment? = parentFragmentManager.findFragmentByTag(ANSWER_DIALOG_TAG) as DialogFragment
            isAnswerDialogOn = false
        }
    }

    // answer !
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Make sure fragment codes match up
        when(requestCode) {
            TRG_REQ_CODE_ANSWER -> {

                when (data?.getIntExtra(EVENT_ANSWER_CODE, 0)) {
                    TestBasic.EVENT_ANSWER_GIVEN -> {
                        val result = data.getStringExtra(EVENT_ANSWER_RESULT)
                        val elapsedTime = data.getIntExtra(EVENT_TIME_TO_ANSWER, -1)
                        onAnswer(result!!, elapsedTime)
                    }
                    TestBasic.EVENT_TRIAL_REPEAT -> mTest.repeatTrial()
                    TestBasic.EVENT_TRIAL_ABORT -> onAbortTest()
                }
            }
            TRG_REQ_CODE_INSTRUCTIONS -> startTest()
        }
    }

    // start recognizing and process response (repeat same trial or show next one)
    private fun listenForVocalAnswer(valid_results: List<String> = listOf()) {
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
                            val res: Boolean = if (valid_results.isEmpty()) true
                            else valid_results.contains(
                                it.second
                            )

                            if (res) {
                                bt_abort.visibility = View.INVISIBLE
                                val elapsedTime = getTimeDifference(onsetDate)
                                onAnswer(it.second!!, elapsedTime)

                            } else
                            // text recognized but not allowed
                                speechManager.speak(
                                    resources.getString(org.albaspazio.core.R.string.char_recognition_wrong),
                                    TextToSpeech.QUEUE_FLUSH,
                                    clb = {
                                        if (!abortRecognition) listenForVocalAnswer(
                                            valid_results
                                        )
                                    })
                        }
                        else ->

                            if (it.first == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                                if (!abortRecognition)
                                    listenForVocalAnswer(valid_results)
                            } else
                            // RECOGNIZER ERROR
                                speechManager.speak(it.second!!, TextToSpeech.QUEUE_FLUSH, clb = {
                                    if (!abortRecognition) listenForVocalAnswer(
                                        valid_results
                                    )
                                })
                    }
                }
            )
            .addTo(disposable)
    }
    // ========================================================================================================================================
}