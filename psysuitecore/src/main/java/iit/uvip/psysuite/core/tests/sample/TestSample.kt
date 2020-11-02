package iit.uvip.psysuite.core.tests.sample

import android.app.Activity
//import android.app.Fragment
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.common.SpinnerData
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic
import iit.uvip.psysuite.core.common.stimuli.*
import iit.uvip.psysuite.core.tests.sample.TrialSample.Companion.LOG_HEADER
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.ui.showToast


/*

unimodal precision: (Audio-Vibration-Visual)

    stimulus onset
    stimulus duration
    temporal distance between two stimuli from 10 -> 500 ms  [10,15,20,25,30,35,40,50,65,70,80,90,100]
    triple stimulus (like in bisection)


*/

class TestSample(ctx: Context, activity: Activity, hostfragment: Fragment, data: SubjectSampleParcel, vibrator: VibrationManager?, mImageView: ImageView?)
    : TestBasic(ctx, activity, hostfragment, data, vibrator, mImageView)
{
    private var curStimDuration: Long = 0L
    override var LOG_TAG:String = TestSample::class.java.simpleName

    companion object {

        @JvmStatic val TEST_BASIC_LABEL     = "SAMPLE"

        @JvmStatic val STIM_SHIFTED         = 1
        @JvmStatic val STIM_PAIR            = 2

        fun getConditionsInfo(ctx: Context): List<SpinnerData> {
            return mutableListOf(
                SpinnerData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.aligned)}" , TEST_SAMPLE_ALIGNED, "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.aligned)}"),
                SpinnerData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.shifted)}" , TEST_SAMPLE_SHIFTED, "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.shifted)}"),
                SpinnerData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.pair)}"    , TEST_SAMPLE_PAIR   , "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.pair)}")
            )
        }
        
        fun getNextTrialModes():List<List<Int>>{
            return listOf(listOf(TEST_NEXTTRIAL_BUTTON, TEST_NEXTTRIAL_AUTO))
        }
    }

    override var mDrawablesResource:MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.black_circle, R.drawable.blue_circle, R.drawable.red_circle)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){

        when {
            mImageView == null -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }

        mImageView?.visibility  = View.INVISIBLE
        curStimDuration         = 1000L
        currTrial               = 0
        validAnswers            = mutableListOf()

        ITI                     = (subjectparcel as SubjectSampleParcel).iti
        mTestLabel              = ""
        getConditionsInfo(ctx).map {
            if (it.id == subjectparcel.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        createResultFile(subjectparcel, LOG_HEADER)
        createTrials()
        nTrials                 = mTrials.size
        setStimuliManager()
    }

    private fun setStimuliManager(){

        val audioManager = when {
            (subjectparcel as SubjectSampleParcel).stim_sources and STIM_TYPE_A1 > 0 ->
                AudioManager(STIM_TYPE_A1, -1,
                    amplitude  = subjectparcel.audioVolume.toInt(),
                    duration   = subjectparcel.audioDuration,
                    handler    = mStimuliHandler,
                    ctx        = ctx)

            subjectparcel.stim_sources and STIM_TYPE_A2 > 0 ->
                try{
                    if(subjectparcel.audioResource.isEmpty())    subjectparcel.audioResource = currAudioResourceName
                    AudioManager(STIM_TYPE_A2,
                        subjectparcel.audioResource,
                        subjectparcel.audioVolume.toInt(),
                        duration = subjectparcel.audioDuration,
                        handler = mStimuliHandler,
                        ctx = ctx)

                } catch(e:Exception){
                    throw Exception("GENERIC ERROR: $e")
                } catch(e: AudioResourceException){
                    throw AudioResourceException("AUDIO_RESOURCE_ERROR: resource name = $e")
                }

            subjectparcel.stim_sources and STIM_TYPE_A3 > 0 ->
                try{
//                    if(subjectparcel.audioResource.isEmpty())                        subjectparcel.audioResource = currAudioResourceName
                    subjectparcel.audioResource = "t200hz_1s.wav"
                    AudioManager(STIM_TYPE_A3,
                        listOf(subjectparcel.audioResource),
                        subjectparcel.audioVolume.toInt(),
                        duration = subjectparcel.audioDuration,
                        handler = mStimuliHandler,
                        ctx = ctx)

                } catch(e:Exception){
                    throw Exception("GENERIC ERROR: $e")
                } catch(e: AudioResourceException){
                    throw AudioResourceException("AUDIO_RESOURCE_ERROR: resource name = $e")
                }

            else -> null
        }

        val tactileManager =    if(subjectparcel.stim_sources and STIM_TYPE_T1 > 0)
            TactileManager(vibrator!!, subjectparcel.tactileAmplitude, duration = subjectparcel.tactileSequence.toLong(), handler = mStimuliHandler)

        else if(subjectparcel.stim_sources and STIM_TYPE_T2 > 0){
            val timings = TactileManager.validatePattern(subjectparcel.tactileSequence)
            if(timings != null) TactileManager(vibrator!!, subjectparcel.tactileAmplitude, timings!!, handler = mStimuliHandler)
            else {
                // TODO: ALERT
                null
            }
        }
        else null

        val visualManager = when {
            subjectparcel.stim_sources and STIM_TYPE_V1 > 0 -> {
                val on =    if(subjectparcel.visualDrawableOn >= mDrawablesResource.size)   mDrawablesResource.size
                else                                                                        subjectparcel.visualDrawableOn
                VisualManager(STIM_TYPE_V1, mImageView!!, mDrawablesResource[on], duration = subjectparcel.visualDuration, handler = mStimuliHandler)
            }
            subjectparcel.stim_sources and STIM_TYPE_V2 > 0 -> {
                if(mImageView == null)  return
                val on =    if(subjectparcel.visualDrawableOn >= mDrawablesResource.size)   mDrawablesResource.size-1
                else                                                                        subjectparcel.visualDrawableOn
                VisualManager(STIM_TYPE_V2, mImageView, mDrawablesResource[on], mDrawablesResource[subjectparcel.visualDrawableOff], subjectparcel.visualDuration, handler = mStimuliHandler)
            }
            else -> null
        }

        mStimuliManager = StimuliManager(audioManager, tactileManager, visualManager){  testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))}
    }
    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrials(){

        val extraTrial:Any? = when(subjectparcel.type){
            TEST_SAMPLE_SHIFTED     -> (subjectparcel as SubjectSampleParcel).shiftedParams
            TEST_SAMPLE_PAIR        -> (subjectparcel as SubjectSampleParcel).pairDistance
            else                    -> null
        }

        var cnt = -1
        for(t in 0 until (subjectparcel as SubjectSampleParcel).repetitions){
            mTrials.add(TrialSample(++cnt, subjectparcel.type, "", subjectparcel.stim_sources, extraTrial))
        }
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onTrialEnd() {

        when (nextTrailModality) {
            TEST_NEXTTRIAL_BUTTON -> testEvent.accept(Pair(EVENT_SHOW_NEXT_BUTTON, null))
            TEST_NEXTTRIAL_AUTO -> {
                // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_SHOW_ABORT, 1000L))
                }, curStimDuration)
            }
        }
    }

    override fun initSummary(){}

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial:TrialBasic, isRepeat:Boolean){

        when(trial.type){

            TEST_SAMPLE_ALIGNED ->  deliverAlignedStimulus((trial as TrialSample).source){onTrialEnd()}

            TEST_SAMPLE_SHIFTED ->  {
                val corr_delays = delaysAligner.arrangeDelays((subjectparcel as SubjectSampleParcel).stim_sources,
                                                ((trial as TrialSample).extraTrial as List<Long>)[0],
                                                (trial.extraTrial as List<Long>)[1],
                                                trial.extraTrial[2])

                deliverShiftedStimulus(trial.source, corr_delays.a, corr_delays.t, corr_delays.v){onTrialEnd()}
            }
            TEST_SAMPLE_PAIR    ->  deliverAlignedStimuliPair((trial as TrialSample).extraTrial as Long, trial.source){onTrialEnd()}
        }
    }
    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================
}