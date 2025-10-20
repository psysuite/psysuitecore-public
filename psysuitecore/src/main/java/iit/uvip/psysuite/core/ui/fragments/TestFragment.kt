package iit.uvip.psysuite.core.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.databinding.FragmentTestBinding
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.utility.TestResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.fragments.setNavigationResult
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.speech.SpeechRecognitionManager
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert
import org.albaspazio.core.ui.showToast
import java.lang.reflect.Constructor
import java.util.Date
import kotlin.reflect.KFunction

/*
Three operative modalities (mSubjectParcel.nextTrailModality):

- trial have an answer dialog, where user can also abort                                    // TEST_NEXTTRIAL_ANSWER, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER
- trial have no answer dialog, at the end of the trial, the following trial is displayed    // TEST_NEXTTRIAL_NOCHOOSE
- trial have no answer dialog, at the end of the trial, the following trial is displayed
  but a abort and pause buttons are displayed                                               // TEST_NEXTTRIAL_AUTO

- trial have no answer dialog, at the end of the trial, test stops and wait for user press. // TEST_NEXTTRIAL_BUTTON

normal trial-by-trial flow is:

here        : mTest.start()
within Test : show -> onStimuliEnd -> EVENT_GIVE_ANSWER/EVENT_SHOW_NEXT_BUTTON/EVENT_SHOW_ABORT
here        : mTest.testEvent.subscribe -> answerdialog/speechrec   -> onAnswer  |-> onNewTrial -> mTest.nextTrial ->  test end | block end | show next trial
                                        -> next button  _________________________|
within Test : mTrial.setResponse -> saveText -> doNextTrial() -> trialsmanager.getNewTrial -> show
 */

class TestFragment : BaseFragment(
    layout              = R.layout.fragment_test,
    landscape           = false,
    hideAndroidControls = true
){
    private var _binding: FragmentTestBinding? = null
    val binding get() = _binding!!

    private lateinit var mTest: TestBasic
    private lateinit var mSubjectParcel: SubjectBasicParcel

    override val LOG_TAG                            = TestFragment::class.java.simpleName
    private val ANSWER_DIALOG_TAG                   = "ANSWER_DIALOG_TAG"

    private var answerDialogFragment:DialogFragment? = null
    private var isAnswerDialogOn:Boolean            = false

    private var isBlindUser:Boolean                 = false
    private var isDeafUser:Boolean                  = false

    private var isPaused:Boolean                    = false
    private var mHandler: Handler                   = Handler(Looper.getMainLooper())
    private var mRunnable: Runnable?                = null      // runnable to be cancelled while confirming abort

    private var abortRecognition:Boolean            = false  // set true when I answer manually and speech rec is going to be restarted (e.g. rec busy or error)
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var speechManager: SpeechManager
    private var vibrator:VibrationManager?          = null

    private lateinit var onsetDate: Date

    private var currDebugInfo:String                = ""

    var showResult:Boolean                          = false

    private lateinit var answerDialogRef:Pair<KFunction<*>?, Any?>


    companion object {

        @JvmStatic val TRG_REQ_CODE_ANSWER:Int        = 1
        @JvmStatic val TRG_REQ_CODE_INSTRUCTIONS:Int  = 2

        @JvmStatic val DEFAULT_ABORT_TIME:Long  = 1000L

        @JvmStatic val EVENT_ANSWER_CODE:String         = "answer_code"
        @JvmStatic val EVENT_ANSWER_RESULT:String       = "answer_result"
        @JvmStatic val EVENT_TIME_TO_ANSWER:String      = "answer_time"
        @JvmStatic val EVENT_ANSWER_RESULT_EXTRA:String = "answer_result_extra"

        fun newIntent(resp:Int, elapsedTime:Long, resp_id:Int, resp_extra:String=""): Intent {
            val intent = Intent()
            intent.putExtra(EVENT_ANSWER_RESULT, resp)
            intent.putExtra(EVENT_TIME_TO_ANSWER, elapsedTime)
            intent.putExtra(EVENT_ANSWER_CODE, resp_id)
            intent.putExtra(EVENT_ANSWER_RESULT_EXTRA, resp_extra)
            return intent
        }
    }

    // ==========================================================================================================================
    // FRAGMENT OVERRIDES
    // ==========================================================================================================================
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        mMainView = binding.root
        setupFragmentResultListener()
        return mMainView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // main access point. it does:
    // - init & validate mSubjectParcel
    // - (try) instanciate the correct TestClass
    // - call mTest.initTest() and wait for EVENT_TEST_SETUP_COMPLETED
    // - onTestSetupComplete() -> mTest.adjustBlocks, mTest.start()
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        binding.btNext.visibility = View.INVISIBLE
        binding.btAbort.visibility = View.INVISIBLE
        binding.btPause.visibility = View.INVISIBLE
        binding.txtDebugInfo.visibility = View.INVISIBLE

        val subj: SubjectBasicParcel? = arguments?.getParcelable(TestBasic.TESTINFO_BUNDLE_LABEL)
        if (subj == null) {
            showAlert(requireActivity(), resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
            showAlert(requireActivity(), resources.getString(R.string.critical_error), "Subject Info is null")
            requireView().findNavController().popBackStack()
        } else mSubjectParcel = subj

        speechManager = SpeechManager(requireContext()) {
            try {
                vibrator = VibrationManager(requireContext()).init()
                speechRecognitionManager = SpeechRecognitionManager(requireContext())

                if (mSubjectParcel.classes[0].isNotBlank()) {
                    val testClass                   = Class.forName(mSubjectParcel.classes[0])
                    val constructor: Constructor<*> = testClass.constructors[0]
                    mTest                           = constructor.newInstance(requireContext(),
                                                                                requireActivity(),
                                                                                this,
                                                                                mSubjectParcel,
                                                                                vibrator,
                                                                                binding.circleView,
                                                                                speechManager,
                                                                                mMainView) as TestBasic
                }
                else throw Exception("mSubjectParcel.classes[0] is empty")
            }
            catch (e: Exception) {
                Log.e("TestFragment", e.message.toString())
                showAlert(requireActivity(), resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
                requireView().findNavController().popBackStack()
                return@SpeechManager
            }

            isBlindUser = mSubjectParcel.isBlindUser
            isDeafUser  = mSubjectParcel.isDeafUser

            if (isBlindUser && !speechManager.isValid) {
                showAlert(requireActivity(), resources.getString(R.string.error), resources.getString(R.string.contact_developer))
                return@SpeechManager
            }

            // get a reference to the AnswerDialogFragment
            val answerDialogClass = if (isBlindUser) {
                                        // population is visually impaired. use AnswerGestureDF
                                        "iit.uvip.psysuite.core.ui.fragments.answers.AnswerGestureDialogFragment"
                                    }
                                    else {
                                        if (mSubjectParcel.classes.size > 1 && mSubjectParcel.classes[1].isNotEmpty())
                                            mSubjectParcel.classes[1]
                                        else "iit.uvip.psysuite.core.ui.fragments.answers.TwoAFCAnswerDialogFragment"
                                    }
            answerDialogRef = getCompanionObjectMethod(answerDialogClass, "newInstance")

            setTestEventsObservable()

            mTest.initTest()    // then wait for EVENT_TEST_SETUP_COMPLETED. while the Test asynchronously load the needed resources
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(this::speechManager.isInitialized)
            speechManager.shutdown()
    }

    override fun onResume() {
        super.onResume()

        setTestEventsObservable()

        // visible when:
        // - answer dialog is not displayed
        // - next trial in shown automatically (after a short delay to let user abort the whole test)
        binding.btNext.setOnClickListener{            mTest.onNextTrial()        }

        // visible when:
        // - TEST_ABORT_ALWAYS
        // - at the end of each trial, to let user abort the whole test
        binding.btAbort.setOnClickListener{

            mHandler.removeCallbacksAndMessages(null)  // to stop the automatic move to next trial

            show2ChoisesDialog(requireActivity(),
                requireContext().resources.getString(R.string.warning),
                requireContext().resources.getString(R.string.test_want2abort),
                requireContext().resources.getString(R.string.yes),         // ok
                requireContext().resources.getString(R.string.no),
                {    onAskIfAbortTest()},
                {   mTest.onNextTrial()}
            )
        }

        binding.btPause.setOnClickListener{
            if(isPaused){               binding.btPause.text = resources.getString(R.string.pause)
                mTest.onNextTrial()
            }
            else{
                mHandler.removeCallbacksAndMessages(null)
                binding.btPause.text = resources.getString(R.string.resume)
            }
            isPaused = !isPaused
        }
    }

    override fun onPause(){
        super.onPause()
        disposable.clear()
    }

    // ==========================================================================================================================
    // observer of events emitted by test sub classes
    // here I manage all trial-by-trial behaviours invoked by Tests
    private fun setTestEventsObservable(){

        if(!this::mTest.isInitialized) return

        if(disposable.size() > 0) return

        mTest.testEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when(it.first){
                    // test can start
                    TestBasic.EVENT_TEST_SETUP_COMPLETED -> onTestSetupComplete()        // Test asynchronously loaded all its needed resources and is fully ready

                    // called every trials by Test.onNextTrial()
                    TestBasic.EVENT_TRIAL_STARTED        -> showTrial()

                    TestBasic.EVENT_SHOW_INFO            -> {
                                                            val info = (it.second ?: "INFO_MISSING") as String
                                                            showInfo(info)}
                    // events sent after last stimulus
                    TestBasic.EVENT_GIVE_ANSWER          -> showAnswerDialog(TRG_REQ_CODE_ANSWER)
                    TestBasic.EVENT_GIVE_VOCAL_ANSWER    -> {
                                                            binding.btAbort.visibility = View.VISIBLE
                                                            listenForVocalAnswer(mTest.validAnswers)}
                    TestBasic.EVENT_SHOW_NEXT_ABORT      -> pause2NextTrial()
                    TestBasic.EVENT_SHOW_PAUSE_ABORT     -> {
                                                            val dur = (it.second ?: DEFAULT_ABORT_TIME) as Long

                                                            if(dur > 0L)    continue2NextTrial(dur,{ mTest.onNextTrial() })
                                                            else            mTest.onNextTrial()}
                    TestBasic.EVENT_TRAINING_END         -> onTrainingEnded()

                    TestBasic.EVENT_BLOCK_END            -> onBlockEnded()
                    TestBasic.EVENT_TEST_END             -> onTestEnded()

                    TestBasic.EVENT_NAVIGATE_BACK        -> navigateBack(it.second as Int, it.third)

                    TestBasic.EVENT_SHOW_DEBUGINFO       -> {
                                                            val info = (it.second ?: "DEBUG_INFO_MISSING") as String
                                                            showDebugInfo(info)}
                    TestBasic.EVENT_TEST_ERROR           -> onTestError(it.second as String, it.third)

                    // unused
                    TestBasic.EVENT_STIMULI_START        -> {}
                    TestBasic.EVENT_STIMULI_END          -> {}
                }
            }
            .addTo(disposable)
    }

    // ==========================================================================================================================
    // triggered by testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null)) run on each Test class
    private fun onTestSetupComplete(){

        // block is always -1 but when is continuing a previous session (in that case if it found ..._blk2.txt => block=3 ...thus is always > 0)
        mTest.adjustBlocks(mSubjectParcel.block)     // set currTrial, mCurrBlock, mTrial

        if(isBlindUser)     showAnswerDialog(TRG_REQ_CODE_INSTRUCTIONS)
        else                mTest.start()
    }

    // ==========================================================================================================================================
    // region EVENTS ASKING USER INTERACTION (pressed abort, ask to confirm + block ended, ask to continue or stop)
    // ==========================================================================================================================================
    // user wanted to interrupt test during a block (ask whether deleting results file)
    // => greetings & mTest.abortTest (unloadStimuli) & NAVIGATEBACK
    private fun onAskIfAbortTest(){

        mHandler.removeCallbacksAndMessages(null)
        if(isBlindUser)     speechManager.speak(requireContext().resources.getString(R.string.test_aborted_blind))

        show2ChoisesDialog(requireActivity(),
            requireContext().resources.getString(R.string.warning), requireContext().resources.getString(R.string.test_aborted),
            requireContext().resources.getString(R.string.keep),         // ok
            requireContext().resources.getString(R.string.delete),       // cancel
            {   mTest.terminateTest(TestBasic.TEST_ABORTED_KEEP_RESULT)     /* okClb */ },
            {   mTest.terminateTest(TestBasic.TEST_ABORTED_DEL_RESULT)      /* cancelClb*/}
        )
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
            { /* cancelClb*/
                mHandler.removeCallbacksAndMessages(null)
                mTest.terminateTest(TestBasic.BLOCK_COMPLETED)
            })
    }

    // called when user ended a training block= > mTest.startNewBlock() OR onStoppedAfterBlock (result file is renamed "*_blkX")
    private fun onTrainingEnded(){

        if(isBlindUser) {
            mHandler.postDelayed({ // a speechManager.stop is given in OnAnswer. with a 500ms delay I start speaking after
                speechManager.speak(requireContext().resources.getString(R.string.training_ended_blind))}, 500)
        }
        mHandler.removeCallbacksAndMessages(null) // FORSE SERVE

        show2ChoisesDialog(requireActivity(),
            resources.getString(R.string.warning),
            resources.getString(R.string.training_ended),
            resources.getString(R.string.start_test),
            resources.getString(R.string.restart_training),
            { /* okClb */       mTest.startTest() },
            { /* cancelClb*/   mTest.startTraining() })
    }
    // endregion

    //=======================================================================================================================================
    //region ACCESSORY
    // TEST_ERROR  => alert & NAVIGATEBACK
    private fun onTestError(msg: String, files:List<String>){
        mHandler.removeCallbacksAndMessages(null)

        if(isBlindUser)     speechManager.speak(resources.getString(R.string.critical_error))

        showAlert(requireActivity(), resources.getString(R.string.critical_error), msg)
        navigateBack(TestBasic.TEST_ABORTED_WITH_ERROR, files)
    }

    /* called by:
     - onTestError                  TEST_ABORTED_WITH_ERROR
     - onTestEnded                  TEST_COMPLETED
     - onAbortTest -> OK/cancel     TEST_ABORTED
     - onStoppedAfterBlock          BLOCK_COMPLETED
    results_file can be empty. it can have only the first (result) file not empty or having both results and summary
     */
    private fun navigateBack(result_code: Int, results_file: List<String>){

        val files_list:ArrayList<String> = arrayListOf()
        results_file.map{
            if(it.isNotEmpty()) files_list.add(it)
        }
        // data class TestResult      (code:Int=-1, mailsubject:String, mailbody:String,                       res_files:ArrayList<String> = arrayListOf(),  testClass:String)
//        setNavigationResult(TestResult(result_code, mTest.mTestLabel, mSubjectParcel.composeSubjectFileName(requireContext()), files_list, mTest.javaClass.name, mSubjectParcel), TestBasic.TEST_BUNDLE_RESULT_LABEL)
        // TODO: depends on TestResult definition and ResultsManager in psysuite
        setNavigationResult(TestResult(result_code, mTest.mTestLabel, mSubjectParcel.composeSubjectFileName(requireContext()), files_list, mTest.javaClass.name), TestBasic.TEST_BUNDLE_RESULT_LABEL)
        requireView().findNavController().popBackStack()
    }
    // endregion

    //=======================================================================================================================================
    //region ANSWERS
    //=======================================================================================================================================
    // called by mTest.testEvent listener (when test trial stimulus and user response is needed) & onTestSetupComplete (for blind users)
    // PURE UI: create answer dialog
    // if Test could have a training session (mSubjectParcel.doTraining == true) => check mTest.mTrial.isTraining
    // else => check mSubjectParcel.canRepeat/showResult
    private fun showAnswerDialog(trg_req_code: Int){

        val b = Bundle()
        b.putInt("trial_id", mTest.currTrialID)
        b.putInt("tot_trials", mTest.nTrials)

        b.putString("question", mTest.mQuestion)
        b.putStringArrayList("answers", mTest.validAnswers as ArrayList<String>)
        b.putInt("correct_answer", mTest.getTrialCorrectAnswer())

        when(mSubjectParcel.doTraining) {
            TestBasic.TEST_SWITCH_CHOOSE_ON, TestBasic.TEST_SWITCH_ENABLED -> {         // look current trial whether is training or not

                if(mTest.mTrial.isTraining) {
                    b.putInt("can_repeat_trial", TestBasic.TEST_SWITCH_ENABLED)
                    b.putInt("show_result",      TestBasic.TEST_SWITCH_ENABLED)
                }else{
                    b.putInt("can_repeat_trial", TestBasic.TEST_SWITCH_DISABLED)
                    b.putInt("show_result",      TestBasic.TEST_SWITCH_DISABLED)
                }
            }
            else -> {                                                                   // look what was designed by developer
                b.putInt("can_repeat_trial", mSubjectParcel.canRepeat)
                b.putInt("show_result",  mSubjectParcel.showResult)
            }
        }

        b.putString("debugInfo", currDebugInfo)
        b.putBoolean("isDebug", mSubjectParcel.isDebug)

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

    // PURE UI: called by onAnswerGiven
    private fun closeAnswerDialog(){
        if(isAnswerDialogOn) {
            answerDialogFragment?.dismiss() //        val dialogFragment:DialogFragment? = parentFragmentManager.findFragmentByTag(ANSWER_DIALOG_TAG) as DialogFragment
            isAnswerDialogOn = false
        }
    }

    // answer given (onAnswerGiven ||  mTest.repeatTrial || onAskIfAbortTest)
    // or instruction given (=> startTest)
    private fun setupFragmentResultListener() {
        // Listener for answer results
        parentFragmentManager.setFragmentResultListener(TRG_REQ_CODE_ANSWER.toString(), viewLifecycleOwner) { requestKey, result ->

            try {
                when (result.getInt(EVENT_ANSWER_CODE, 0)) {
                    TestBasic.EVENT_ANSWER_GIVEN -> {
                        val res         = result.getInt(EVENT_ANSWER_RESULT, -1)
                        val elapsedTime = result.getLong(EVENT_TIME_TO_ANSWER, -1)
                        val result_extra= result.getString(EVENT_ANSWER_RESULT_EXTRA) ?: ""
                        onAnswerGiven(res, elapsedTime, result_extra)
                    }
                    TestBasic.EVENT_TRIAL_REPEAT -> mTest.repeatTrial()
                    TestBasic.EVENT_TRIAL_ABORT -> onAskIfAbortTest()
                }
            }
            catch (e: Exception) {
                Log.e("TestFragment", e.message.toString())
                showAlert(requireActivity(), resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
                requireView().findNavController().popBackStack()
            }
        }

        // Listener for instructions
        parentFragmentManager.setFragmentResultListener(TRG_REQ_CODE_INSTRUCTIONS.toString(), viewLifecycleOwner) { requestKey, _ ->
            mTest.start()
        }
    }

    // start recognizing and process response (repeat same trial or show next one)
    // => onAnswerGiven
    private fun listenForVocalAnswer(valid_results: List<String> = listOf()) {
        abortRecognition        = false
        binding.btAbort.visibility    = View.VISIBLE
        onsetDate               = Date()
        speechRecognitionManager.getSpeechInput()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    when (it.first) {
                        SpeechRecognitionManager.REC_SUCCESS -> {
                            val rec_word    = it.second!!
                            val elapsedTime = getTimeDifference(onsetDate)

                            Log.d("", "recognized word $rec_word")

                            if (valid_results.isEmpty()) {      // free answer, collect spoken word and send it as results
                                binding.btAbort.visibility = View.INVISIBLE
                                onAnswerGiven(-1, elapsedTime, rec_word)
                            }
                            else{                                // check whether given response is allowed
                                val answer_id = valid_results.indexOf(rec_word)

                                if(answer_id == -1){
                                    // text recognized but not expected
                                    speechManager.speak(
                                        resources.getString(org.albaspazio.core.R.string.char_recognition_wrong),
                                        TextToSpeech.QUEUE_FLUSH,
                                        clb = {
                                            if (!abortRecognition) listenForVocalAnswer(
                                                valid_results
                                            )
                                        })
                                }
                                else{
                                    binding.btAbort.visibility = View.INVISIBLE
                                    onAnswerGiven(answer_id, elapsedTime, rec_word)
                                }
                            }
                        }
                        else ->

                            if (it.first == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                                if (!abortRecognition)
                                    listenForVocalAnswer(valid_results)
                            } else
                            // RECOGNIZER ERROR
                                speechManager.speak(it.second!!, TextToSpeech.QUEUE_FLUSH, clb = {
                                    if (!abortRecognition) listenForVocalAnswer(valid_results)
                                })
                    }
                }
            )
            .addTo(disposable)
    }

    // called by: 1) onActivityResult after answer, 2) speechrecognition result
    // => mTest.setAnswer & mTest.onNextTrial()
    private fun onAnswerGiven(result:Int = -1, elapsed:Long = -1, extra_text:String=""){

        // dont' know whether an answer dialog was present or it was listening for vocal response or it was playbacking something. stop all!
        abortRecognition = true
        mHandler.post {
            speechRecognitionManager.stop()
            speechManager.stop()
        }
        closeAnswerDialog()

        mTest.setAnswer(result, elapsed, extra_text)
        // close trial (e.g. set answer) & check whether it was the last => test ended
        mTest.onNextTrial()
    }
    //#endregion

    //---------------------------------------------------------------------------------------------------------------------------------------
    //region ELEMENT VISIBILITY (PURE UI)
    //---------------------------------------------------------------------------------------------------------------------------------------
    // called by mTest.onNextTrial if test is not finished
    // manage buttons and TrialID text visibility
    private fun showTrial() {

        binding.txtInfo.visibility     = View.INVISIBLE
        binding.btNext.visibility      = View.INVISIBLE
        binding.btPause.visibility     = View.INVISIBLE

        binding.txtTrialId.visibility = View.INVISIBLE
        if (mSubjectParcel.showTrialID == TestBasic.TEST_SHOWTRIALS_ALWAYS) showTrialId()

        binding.btAbort.visibility = if (mSubjectParcel.abortMode == TestBasic.TEST_ABORT_ALWAYS)   View.VISIBLE
                                     else                                                           View.INVISIBLE
    }

    // called by
    // wait for waitms before calling clb() function
    // in the meanwhile show pause and abort buttons
    private fun continue2NextTrial(waitms:Long = 1000L, clb:(()->Unit)? = null){

        binding.btPause.visibility = View.VISIBLE
        binding.btAbort.visibility = View.VISIBLE

        mHandler.postDelayed({
            binding.btPause.visibility = View.INVISIBLE
            binding.btAbort.visibility = View.INVISIBLE
            clb?.invoke()
        }, waitms)
    }

    // stop indefinitely
    // in the meanwhile show next and abort buttons
    private fun pause2NextTrial(){
        binding.btNext.visibility = View.VISIBLE
        binding.btAbort.visibility = View.VISIBLE
    }

    // just show trial id info
    private fun showTrialId(remove: Long = 0){
        binding.txtTrialId.visibility   = View.VISIBLE
        binding.txtTrialId.text         = resources.getString(
            R.string.trial_id,
            (mTest.currTrialID + 1).toString()
        )
        if(remove > 0)
            mHandler.postDelayed({  binding.txtTrialId.visibility = View.INVISIBLE  }, remove)
    }

    // just show debug info
    private fun showDebugInfo(msg: String, remove: Long = 0){
        currDebugInfo           = msg
        binding.txtDebugInfo.visibility = View.VISIBLE
        binding.txtDebugInfo.text       = currDebugInfo

        if(remove > 0)
            mHandler.postDelayed({    binding.txtDebugInfo.visibility = View.INVISIBLE    }, remove)
    }

    private fun showInfo(msg: String, remove: Long = 0) {

        binding.txtInfo.text        = msg
        binding.txtInfo.visibility  = View.VISIBLE

        if(remove > 0)
            mHandler.postDelayed({    binding.txtInfo.visibility = View.INVISIBLE    }, remove)
    }

    // => greetings
    private fun onTestEnded(){
        val msg = getText(R.string.test_ended).toString()

        if(isBlindUser) speechManager.speak(msg)
        else            showToast(msg, requireContext())
    }
    //#endregion
    // ========================================================================================================================================
}



/*
    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        binding.btNext.visibility = View.INVISIBLE
        binding.btAbort.visibility = View.INVISIBLE
        binding.btPause.visibility = View.INVISIBLE
        binding.txtDebugInfo.visibility = View.INVISIBLE

        val subj: SubjectBasicParcel? = arguments?.getParcelable(TestBasic.TESTINFO_BUNDLE_LABEL)
        if (subj == null) {
            showAlert(requireActivity(), resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
            showAlert(requireActivity(), resources.getString(R.string.critical_error), "Subject Info is null")
            requireView().findNavController().popBackStack()
        } else mSubjectParcel = subj

        speechManager = SpeechManager(requireContext()) {
            try {
                vibrator = VibrationManager(requireContext()).init()
                speechRecognitionManager = SpeechRecognitionManager(requireContext())

                try {
                    if (mSubjectParcel.classes[0].isNotBlank()) {
                        val testClass = Class.forName(mSubjectParcel.classes[0])

                        // Try to find a matching constructor
                        val constructors = testClass.constructors
                        var constructor: Constructor<*>? = null

                        // Look for the most specific constructor first
                        for (ctor in constructors) {
                            val paramTypes = ctor.parameterTypes
                            if (paramTypes.size == 7 &&
                                Context::class.java.isAssignableFrom(paramTypes[0]) &&
                                Activity::class.java.isAssignableFrom(paramTypes[1]) &&
                                Fragment::class.java.isAssignableFrom(paramTypes[2]) &&
                                SubjectBasicParcel::class.java.isAssignableFrom(paramTypes[3]) &&
                                VibrationManager::class.java.isAssignableFrom(paramTypes[4]) &&
                                View::class.java.isAssignableFrom(paramTypes[5]) &&
                                SpeechManager::class.java.isAssignableFrom(paramTypes[6])
                            ) {
                                constructor = ctor
                                break
                            }
                        }

                        if (constructor != null) {
                            mTest = constructor.newInstance(
                                requireContext(),
                                requireActivity(),
                                this,
                                mSubjectParcel,
                                vibrator,
                                binding.circleView,
                                speechManager
                            ) as TestBasic
                        } else {
                            throw NoSuchMethodException("No suitable constructor found for ${mSubjectParcel.classes[0]}")
                        }
                    } else {
                        throw IllegalStateException("No test class name provided in SubjectBasicParcel")
                    }
                } catch (e: Exception) {
                    // Fallback to old method if reflection fails
                    Log.e("TestFragment", "Failed to create test instance via reflection: ${e.message}", e)
                    when (mSubjectParcel.type) {
                        TestBasic.TEST_BISECTION_AUDIO,
                        TestBasic.TEST_BISECTION_TACTILE,
                        TestBasic.TEST_BISECTION_VISUAL,
                        TestBasic.TEST_BISECTION_AUDIO_TACTILE,
                        TestBasic.TEST_BISECTION_VISUAL_TACTILE,
                        TestBasic.TEST_BISECTION_AUDIO_VISUAL,
                        TestBasic.TEST_BISECTION_AUDIO_SUPRA,
                        TestBasic.TEST_BISECTION_TACTILE_SUPRA,
                        TestBasic.TEST_BISECTION_VISUAL_SUPRA,
                        TestBasic.TEST_BISECTION_AUDIO_TACTILE_SUPRA,
                        TestBasic.TEST_BISECTION_VISUAL_TACTILE_SUPRA,
                        TestBasic.TEST_BISECTION_AUDIO_VISUAL_SUPRA -> mTest = TestBIS(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager
                        )

                        TestBasic.TEST_MUSICAL_METERS -> mTest = TestMMD(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            speechManager
                        )

                        TestBasic.TEST_TID_SHORT_AUDIO,
                        TestBasic.TEST_TID_SHORT_TACTILE,
                        TestBasic.TEST_TID_SHORT_VISUAL,
                        TestBasic.TEST_TID_LONG_AUDIO,
                        TestBasic.TEST_TID_LONG_TACTILE,
                        TestBasic.TEST_TID_LONG_VISUAL,
                        TestBasic.TEST_TID_SHORT_AUDIO_TRAIN,
                        TestBasic.TEST_TID_SHORT_TACTILE_TRAIN,
                        TestBasic.TEST_TID_SHORT_VISUAL_TRAIN -> mTest = TestTID(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel as SubjectTIDParcel,
                            vibrator,
                            binding.circleView,
                            speechManager
                        )

                        TestBasic.TEST_ATB_TIME_SINGLESTIM,
                        TestBasic.TEST_ATB_TIME_DOUBLESTIM,
                        TestBasic.TEST_ATB_TIME_SINGLESTIM_TOD,
                        TestBasic.TEST_ATB_TIME_DOUBLESTIM_TOD,
                        TestBasic.TEST_ATB_TIME_INF -> mTest = TestATB(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            speechManager
                        )

                        TestBasic.TEST_AVB_TIME_SINGLESTIM,
                        TestBasic.TEST_AVB_TIME_DOUBLESTIM,
                        TestBasic.TEST_AVB_TIME_SINGLESTIM_TOD,
                        TestBasic.TEST_AVB_TIME_DOUBLESTIM_TOD,
                        TestBasic.TEST_AVB_TIME_INF -> mTest = TestAVB(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            binding.circleView,
                            speechManager
                        )

                        TestBasic.TEST_TVB_TIME_SINGLESTIM,
                        TestBasic.TEST_TVB_TIME_DOUBLESTIM,
                        TestBasic.TEST_TVB_TIME_SINGLESTIM_TOD,
                        TestBasic.TEST_TVB_TIME_DOUBLESTIM_TOD,
                        TestBasic.TEST_TVB_TIME_INF -> mTest = TestTVB(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager
                        )

                        TestBasic.TEST_ATVB_TIME_S_UNBAL,
                        TestBasic.TEST_ATVB_TIME_S_BAL,
                        TestBasic.TEST_ATVB_TIME_S_BAL2,
                        TestBasic.TEST_ATVB_TIME_D_UNBAL,
                        TestBasic.TEST_ATVB_TIME_D_BAL -> mTest = TestATVB(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager
                        )

                        TestBasic.TEST_SAMPLE_ALIGNED,
                        TestBasic.TEST_SAMPLE_SHIFTED,
                        TestBasic.TEST_SAMPLE_PAIR -> mTest = TestSample(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel as SubjectSampleParcel,
                            vibrator,
                            binding.circleView,
                            speechManager
                        )

                        TestBasic.TEST_TFI,
                        TestBasic.TEST_TFI_BIMODAL,
                        TestBasic.TEST_TFI_AV,
                        TestBasic.TEST_TFI_TODDLERS -> mTest = TestTFI(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager
                        )


                        TestBasic.TEST_FGI_1_UNSCRAMBLED,
                        TestBasic.TEST_FGI_1_SCRAMBLED,
                        TestBasic.TEST_FGI_2_UNSCRAMBLED,
                        TestBasic.TEST_FGI_2_SCRAMBLED,
                        TestBasic.TEST_FGI_3_UNSCRAMBLED,
                        TestBasic.TEST_FGI_3_SCRAMBLED -> mTest = TestFGI(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager,
                            mMainView
                        )


                        TestBasic.TEST_RIVGRP_RIV_HF,
                        TestBasic.TEST_RIVGRP_GRP_HF,
                        TestBasic.TEST_RIVGRP_RIVGRP_HF,
                        TestBasic.TEST_RIVGRP_RIV_HC,
                        TestBasic.TEST_RIVGRP_GRP_HC,
                        TestBasic.TEST_RIVGRP_RIVGRP_HC -> mTest = TestRIVGRP(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager,
                            mMainView
                        )

                        TestBasic.TEST_BEADS_LOWUNCERT,
                        TestBasic.TEST_BEADS_MIDUNCERT -> mTest = TestBeads(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager,
                            mMainView
                        )

                        TestBasic.TEST_MOTPRE_VH,
                        TestBasic.TEST_MOTPRE_VV,
                        TestBasic.TEST_MOTPRE_VHV,
                        TestBasic.TEST_MOTPRE_VV_CUE_ARROW,
                        TestBasic.TEST_MOTPRE_VH_CUE_ARROW,
                        TestBasic.TEST_MOTPRE_VV_CUE_WEIGHT,
                        TestBasic.TEST_MOTPRE_VH_FIXSPEED,
                        TestBasic.TEST_MOTPRE_VH_VARSPEED_FIXVT,
                        TestBasic.TEST_MOTPRE_VH_VARSPEED_FIXVPL -> mTest = TestTTC(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager,
                            mMainView
                        )


                        TestBasic.TEST_TSP_A_SUB,
                        TestBasic.TEST_TSP_V_SUB,
                        TestBasic.TEST_TSP_T_SUB,
                        TestBasic.TEST_TSP_A_SUPRA,
                        TestBasic.TEST_TSP_V_SUPRA,
                        TestBasic.TEST_TSP_T_SUPRA -> mTest = TestTSP(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager,
                            mMainView
                        )

                        TestBasic.TEST_TIR_A_SUB,
                        TestBasic.TEST_TIR_V_SUB,
                        TestBasic.TEST_TIR_T_SUB,
                        TestBasic.TEST_TIR_A_SUPRA,
                        TestBasic.TEST_TIR_V_SUPRA,
                        TestBasic.TEST_TIR_T_SUPRA -> mTest = TestTIR(
                            requireContext(),
                            requireActivity(),
                            this,
                            mSubjectParcel,
                            vibrator,
                            binding.circleView,
                            speechManager,
                            mMainView
                        )

                        else -> {
                            throw IllegalStateException("Test non riconosciuto")
                        }
                    }
                }
            }
            catch (e: Exception) {
                Log.e("TestFragment", e.message.toString())
                showAlert(requireActivity(), resources.getString(R.string.critical_error), resources.getString(R.string.contact_developer))
                requireView().findNavController().popBackStack()
                return@SpeechManager
            }

            isBlindUser = mSubjectParcel.isBlindUser
            isDeafUser  = mSubjectParcel.isDeafUser

            if (isBlindUser && !speechManager.isValid) {
                showAlert(requireActivity(), resources.getString(R.string.error), resources.getString(R.string.contact_developer))
                return@SpeechManager
            }

            // get a reference to the AnswerDialogFragment
            val answerDialogClass = if (isBlindUser) {
                                        // population is visually impaired. use AnswerGestureDF
                                        "iit.uvip.psysuite.core.ui.fragments.answers.AnswerGestureDialogFragment"
                                    }
                                    else {
                                        if (mSubjectParcel.classes.size > 1 && mSubjectParcel.classes[1].isNotEmpty())
                                            mSubjectParcel.classes[1]
                                        else "iit.uvip.psysuite.core.ui.fragments.answers.TwoAFCAnswerDialogFragment"
                                    }
            answerDialogRef = getCompanionObjectMethod(answerDialogClass, "newInstance")

            setTestEventsObservable()

            mTest.initTest()    // then wait for EVENT_TEST_SETUP_COMPLETED. while the Test asynchronously load the needed resources
        }
    }
 */