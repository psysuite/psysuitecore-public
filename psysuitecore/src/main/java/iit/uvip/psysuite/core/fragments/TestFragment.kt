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
import iit.uvip.psysuite.core.tests.temporalbinding.atb.SubjectATBParcel
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
import org.albaspazio.core.accessory.getTimeDifference
import org.albaspazio.core.accessory.showToast
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.fragments.setNavigationResult
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.speech.SpeechRecognitionManager
import java.util.*

/*
Three operative modalities:

- trial have an answer dialog, where user can also abort
- trial have no answer dialog, at the end of the trial, the following trial is displayed
- trial have no answer dialog, at the end of the trial, test stops and wait for user press.

 */

class TestFragment : BaseFragment(
    layout = R.layout.fragment_test,
    landscape = false,
    hideAndroidControls = true
){

    private lateinit var mTest: TestBasic
    override val LOG_TAG                            = TestFragment::class.java.simpleName
    private val disposable                          = CompositeDisposable()
    private val TARGET_FRAGMENT_REQUEST_CODE:Int    = 1

    private var answerDialogFragment:AnswerDialogFragment?      = null
    private var isAnswerDialogOn:Boolean            = false

    private var isPaused:Boolean                    = false
    private var mHandler: Handler                   = Handler()

    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private var abortRecognition:Boolean            = false  // set true when I answer manually and speech rec is going to be restarted (e.g. rec busy or error)

    private lateinit var speechManager: SpeechManager

    lateinit var onsetDate: Date

    private val ANSWER_DIALOG_TAG                   = "ANSWER_DIALOG_TAG"

    var vibrator: org.albaspazio.core.accessory.VibrationManager? = null

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

        val test: SubjectBasicParcel? = arguments?.getParcelable(TestBasic.TESTINFO_BUNDLE_LABEL) ?: return
        when(test!!.type)
        {
            TestBasic.TEST_BISECTION_AUDIO,
            TestBasic.TEST_BISECTION_TACTILE,
            TestBasic.TEST_BISECTION_AUDIO_TACTILE,
            TestBasic.TEST_BISECTION_AUDIO_VIDEO    -> mTest = TestBIS(requireContext(), test, vibrator, circleView)

            TestBasic.TEST_MUSICAL_METERS           -> mTest = TestMMD(requireContext(), test)

            TestBasic.TEST_TID_SHORT_AUDIO,
            TestBasic.TEST_TID_SHORT_TACTILE,
            TestBasic.TEST_TID_LONG_AUDIO,
            TestBasic.TEST_TID_LONG_TACTILE         -> mTest = TestTID(requireContext(), test as SubjectTIDParcel, vibrator)

            TestBasic.TEST_ATB_TIME,
            TestBasic.TEST_ATB_FREQUENCY,           // to be coded
            TestBasic.TEST_ATB_FREQUENCY_INF,       // to be coded
            TestBasic.TEST_ATB_TIME_INF_15s,
            TestBasic.TEST_ATB_TIME_INF             -> mTest = TestATB(requireContext(), test as SubjectATBParcel, vibrator)

            TestBasic.TEST_ATVB_TIME_SINGLESTIM,
            TestBasic.TEST_ATVB_TIME_DOUBLESTIM,
            TestBasic.TEST_ATVB_TIME_DOUBLESTIM2    -> mTest = TestATVB(requireContext(), test as SubjectATBParcel, vibrator, circleView)

        }
        bt_next.visibility  = View.INVISIBLE
        bt_abort.visibility = View.INVISIBLE
        bt_pause.visibility = View.INVISIBLE

        if (mTest.showTrialsID == TestBasic.TEST_SHOWTRIALS_ALWAYS) showTrialId(false)
        if (mTest.abortMode == TestBasic.TEST_ABORT_ALWAYS){
            bt_abort.visibility = View.VISIBLE
            bt_pause.visibility = View.VISIBLE
        }

        mHandler.postDelayed({
            mTest.show(mTest.currTrial)
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
                mTest.nextTrial()
                if(mTest.abortMode != TestBasic.TEST_ABORT_ALWAYS)  bt_abort.visibility = View.INVISIBLE
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

    private fun onTestEnded(){
        showToast(getText(R.string.test_ended).toString(),requireContext())
        navigateBack(TestBasic.TEST_COMPLETED, mTest.getAbsoluteResultFilePath())
    }

    private fun onAbortTest(deletelog:Boolean=false){
        mTest.abortTest(deletelog)
        mHandler.removeCallbacksAndMessages(null)
        navigateBack(TestBasic.TEST_ABORT, mTest.getAbsoluteResultFilePath())
    }

    // if result_file != "".... means it really exists
    private fun navigateBack(result_code:Int, result_file:String){

        setNavigationResult(TestResult(result_code, arrayListOf(result_file)), TestBasic.TEST_BUNDLE_RESULT_LABEL)
        Navigation.findNavController(requireView()).popBackStack()
    }

    // here I manage all trial-by-trial behaviours
    private fun setEventsFlow(){
        mTest.testEvent
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            when(it){
                TestBasic.EVENT_STIMULI_START               -> {}
                TestBasic.EVENT_STIMULI_END                 -> mTest.onTrialEnd()
                TestBasic.EVENT_GIVE_ANSWER                 -> showAnswerDialog()
                TestBasic.EVENT_GIVE_VOCAL_ANSWER           -> {
                                                                bt_abort.visibility = View.VISIBLE
                                                                listenForVocalAnswer(mTest.validAnswers)
                }
                TestBasic.EVENT_SHOW_NEXT_BUTTON            -> showNext()
                TestBasic.EVENT_UPDATE_TRIAL_ID             -> showTrialId(false)
                TestBasic.EVENT_UPDATE_TRIAL_ID_REMOVE      -> showTrialId(true)
                TestBasic.EVENT_SHOW_1SECABORT              -> showShortAbort()
            }
        }
        .addTo(disposable)

        // button is shown when an answer dialog is not displayed
        bt_next.setOnClickListener{

            bt_next.visibility      = View.INVISIBLE
            bt_pause.visibility     = View.INVISIBLE

            mTest.nextTrial()

            if(mTest.currTrial == TestBasic.EVENT_TEST_END) onTestEnded()
            else {
                when(mTest.showTrialsID) {
                    TestBasic.TEST_SHOWTRIALS_ALWAYS    -> showTrialId(false)
                    TestBasic.TEST_SHOWTRIALS_TRIALEND  -> showTrialId(true)
                }
                if(mTest.abortMode == TestBasic.TEST_ABORT_TRIALEND){
                    bt_abort.visibility = View.INVISIBLE
                }
            }
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    private fun onNext(prev_result: String = "", elapsed: Int = -1){

        // dont' know whether an answer dialog was present or it was listening for vocal response or it was playbacking something. stop all!
        abortRecognition = true
        speechRecognitionManager.stop()
        speechManager.stop()

        closeAnswerDialog()

        // call next trial & check whether it was the last => test ended
        if(mTest.nextTrial(prev_result, elapsed) == TestBasic.EVENT_TEST_END)    onTestEnded()
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    private fun showNext() {
        bt_next.visibility = View.VISIBLE

        if(mTest.abortMode == TestBasic.TEST_ABORT_ALWAYS || mTest.abortMode == TestBasic.TEST_ABORT_TRIALEND)
            bt_abort.visibility = View.VISIBLE
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    private fun showShortAbort(){
        bt_abort.visibility = View.VISIBLE
        bt_pause.visibility = View.VISIBLE

        mHandler.postDelayed({
            bt_abort.visibility = View.INVISIBLE
            bt_pause.visibility = View.INVISIBLE
            mTest.nextTrial()
        }, 1000L)
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    private fun showTrialId(remove:Boolean=false){
        txtTrialId.visibility   = View.VISIBLE
        txtTrialId.text         = resources.getString(R.string.trial_id, (mTest.currTrial + 1).toString())
        if(remove){
            mHandler.postDelayed({
                txtTrialId.visibility = View.INVISIBLE
            }, 1000L)
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    // create answer dialog and process response (repeat same trial or show next one
    private fun showAnswerDialog(){

        val b = Bundle()
        b.putInt("trial_id",    mTest.currTrial)
        b.putInt("tot_trials",  mTest.nTrials)
        b.putString("question", mTest.mQuestion)
        b.putStringArrayList("answers", mTest.validAnswers as ArrayList<String>)

        answerDialogFragment = AnswerDialogFragment.newInstance("Some Title")
        (answerDialogFragment as AnswerDialogFragment).setTargetFragment(this , TARGET_FRAGMENT_REQUEST_CODE)
        (answerDialogFragment as AnswerDialogFragment).arguments    = b
        (answerDialogFragment as AnswerDialogFragment).isCancelable = false
        (answerDialogFragment as AnswerDialogFragment).show(parentFragmentManager, ANSWER_DIALOG_TAG)
        isAnswerDialogOn = true
    }

    private fun closeAnswerDialog(){

        (answerDialogFragment as AnswerDialogFragment).dismiss() //        val dialogFragment:DialogFragment? = parentFragmentManager.findFragmentByTag(ANSWER_DIALOG_TAG) as DialogFragment
        isAnswerDialogOn = false
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
                    onNext(result!!, elapsedTime)
                }
                TestBasic.EVENT_TRIAL_REPEAT    -> mTest.show(mTest.currTrial, true)
                TestBasic.EVENT_TRIAL_ABORT     -> onAbortTest()
            }
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------------
    // create answer dialog and process response (repeat same trial or show next one
    private fun listenForVocalAnswer(valid_results:List<String> = listOf()) {
        abortRecognition = false
        bt_abort?.visibility = View.VISIBLE
        onsetDate           = Date()
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
                                onNext(it.second!!, elapsedTime)

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