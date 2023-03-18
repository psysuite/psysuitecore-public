package iit.uvip.psysuite.core.tests.sample

//import android.app.Fragment
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.stimuli.*
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.TrialBasic
import iit.uvip.psysuite.core.tests.sample.TrialSample.Companion.LOG_HEADER
import iit.uvip.psysuite.core.utility.ConditionData
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast


/*

unimodal precision: (Audio-Vibration-Visual)

    stimulus onset
    stimulus duration
    temporal distance between two stimuli from 10 -> 500 ms  [10,15,20,25,30,35,40,50,65,70,80,90,100]
    triple stimulus (like in bisection)


*/

class TestSample(ctx: Context, activity: Activity, hostfragment: Fragment, subject: SubjectSampleParcel, vibrator: VibrationManager?, mImageView: ImageView?, speechManager: SpeechManager?)
    : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager)
{
    private var curStimDuration: Long = 0L
    override var LOG_TAG:String = TestSample::class.java.simpleName

    companion object {

        @JvmStatic val TEST_BASIC_LABEL     = "SAMPLE"

        fun getConditionsInfo(ctx: Context): List<ConditionData> {
            return mutableListOf(
                ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.aligned)}"  , TEST_SAMPLE_ALIGNED, "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.aligned)}", Populations.sighted_hearing_populations),
                ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.shifted)}"    , TEST_SAMPLE_SHIFTED, "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.shifted)}", Populations.sighted_hearing_populations),
                ConditionData("$TEST_BASIC_LABEL ${ctx.resources.getString(R.string.pair)}"     , TEST_SAMPLE_PAIR   , "${TEST_BASIC_LABEL}_${ctx.resources.getString(R.string.pair)}", Populations.sighted_hearing_populations)
            )
        }
        
        fun getNextTrialModes(ctx:Context):List<List<Int>>{
            return listOf(listOf(TEST_NEXTTRIAL_BUTTON, TEST_NEXTTRIAL_AUTO))
        }
    }

    override var mDrawablesResource:MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.black_circle, R.drawable.blue_circle, R.drawable.red_circle)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest(){

        if(mImageView == null) throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")

        // vibrator == null    -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")

        mImageView.visibility   = View.INVISIBLE
        curStimDuration         = 1000L
        validAnswers            = mutableListOf()

        ITI                     = (subject as SubjectSampleParcel).iti
        mTestLabel              = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)

        if (subject.whitenoise > TEST_WNOISE_CHOOSE_OFF)    mNoise = AudioManager.getAudioResource(ctx, "wnoise_20s", 0.01f)

        createResultFile(subject, LOG_HEADER)
        createTrials()
        setStimuliManager()
    }

    private fun setStimuliManager(){

        val audioManager = when {
            (subject as SubjectSampleParcel).stim_sources and StimuliManager.STIM_TYPE_A1 > 0 ->
                AudioManager(StimuliManager.STIM_TYPE_A1, -1,
                    amplitude  = (subject.audioVolume*1.0F)/100,
                    duration   = subject.audioDuration,
                    ctx        = ctx, handler = mStimuliHandler)

            subject.stim_sources and StimuliManager.STIM_TYPE_A2 > 0 ->
                try{
                    if(subject.audioResource.isEmpty())    subject.audioResource = currAudioResourceName
                    AudioManager(StimuliManager.STIM_TYPE_A2,
                        subject.audioResource,
                        (subject.audioVolume*1.0F)/100,
                        duration = subject.audioDuration,
                        ctx = ctx, handler = mStimuliHandler)

                } catch(e:Exception){
                    throw Exception("GENERIC ERROR: $e")
                } catch(e: AudioResourceException){
                    throw AudioResourceException("AUDIO_RESOURCE_ERROR: resource name = $e")
                }

            subject.stim_sources and StimuliManager.STIM_TYPE_A3 > 0 ->
                try{
                    if(subject.audioResource.isEmpty())                        subject.audioResource = currAudioResourceName
                    AudioManager(StimuliManager.STIM_TYPE_A3,
                        subject.audioResource,
                        (subject.audioVolume*1.0F)/100,
                        duration = subject.audioDuration,
                        ctx = ctx, handler = mStimuliHandler)

                } catch(e:Exception){
                    throw Exception("GENERIC ERROR: $e")
                } catch(e: AudioResourceException){
                    throw AudioResourceException("AUDIO_RESOURCE_ERROR: resource name = $e")
                }
            subject.stim_sources and StimuliManager.STIM_TYPE_A4 > 0 ->
                try{
                    if(subject.audioResource.isEmpty())                        subject.audioResource = currAudioResourceName
                    AudioManager(StimuliManager.STIM_TYPE_A4,
                        subject.audioResource,
                        (subject.audioVolume*1.0F)/100,
                        duration = subject.audioDuration,
                        ctx = ctx, handler = mStimuliHandler)

                } catch(e:Exception){
                    throw Exception("GENERIC ERROR: $e")
                } catch(e: AudioResourceException){
                    throw AudioResourceException("AUDIO_RESOURCE_ERROR: resource name = $e")
                }

            else -> null
        }

        val tact_amplitudes = TactileManager.validateAmplitudes(subject.tactileAmplitudes)
        val tact_timings    = TactileManager.validateTimings(subject.tactileTimings)

        val tactileManager  =   if(subject.stim_sources and StimuliManager.STIM_TYPE_T1 > 0)
            TactileManager(vibrator!!, tact_amplitudes, duration = subject.tactileTimings.toLong(), handler = mStimuliHandler)
        else if(subject.stim_sources and StimuliManager.STIM_TYPE_T2 > 0)
            TactileManager(vibrator!!, tact_amplitudes, tact_timings, type = StimuliManager.STIM_TYPE_T2, handler = mStimuliHandler)
        else throw Exception()



        val visualManager = when {
            subject.stim_sources and StimuliManager.STIM_TYPE_V1 > 0 -> {
                val on =    if(subject.visualDrawableOn >= mDrawablesResource.size)   mDrawablesResource.size
                            else                                                      subject.visualDrawableOn
                VisualManager(StimuliManager.STIM_TYPE_V1, mImageView!!, mDrawablesResource[on], duration = subject.visualDuration, handler = mStimuliHandler)
            }
            subject.stim_sources and StimuliManager.STIM_TYPE_V2 > 0 -> {
                if(mImageView == null)  return
                val on =    if(subject.visualDrawableOn >= mDrawablesResource.size)   mDrawablesResource.size-1
                            else                                                      subject.visualDrawableOn
                VisualManager(StimuliManager.STIM_TYPE_V2, mImageView, mDrawablesResource[on], mDrawablesResource[subject.visualDrawableOff], subject.visualDuration, handler = mStimuliHandler)
            }
            else -> null
        }

        mStimuliManager = StimuliManager(audioManager, tactileManager, visualManager, delaysAligner, ctx, mStimuliHandler){  testEvent.accept(Pair(EVENT_TEST_SETUP_COMPLETED, null))}
    }
    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createTrials():List<TrialBasic>{

        val extraTrial:Any? = when(subject.type){
            TEST_SAMPLE_SHIFTED     -> (subject as SubjectSampleParcel).shiftedParams
            TEST_SAMPLE_PAIR        -> (subject as SubjectSampleParcel).pairDistance
            else                    -> null
        }

        var cnt = -1
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(t in 0 until (subject as SubjectSampleParcel).repetitions){
            trials.add(TrialSample(++cnt, subject.type, "", subject.stim_sources, extraTrial))
        }
        return trials
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onTrialEnd() {

        mNoise?.stop()
        mNoise?.prepare()

        when (nextTrailModality) {
            TEST_NEXTTRIAL_BUTTON -> testEvent.accept(Pair(EVENT_SHOW_NEXT_BUTTON, null))
            TEST_NEXTTRIAL_AUTO -> {
                // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
                mStimuliHandler.postDelayed({
                    testEvent.accept(Pair(EVENT_SHOW_ABORT, 1000L))
                }, ITI)
            }
        }
    }

    override fun initSummary(){}

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        Log.d(LOG_TAG, "---------------------")
        mNoise?.start()

        mStimuliHandler.postDelayed({
            when(trial.type){

                TEST_SAMPLE_ALIGNED ->  mStimuliManager.deliverAlignedStimulus((trial as TrialSample).source){onTrialEnd()}

                TEST_SAMPLE_SHIFTED ->  {
                    val corr_delays = delaysAligner.arrangeDelays((subject as SubjectSampleParcel).stim_sources,
                        ((trial as TrialSample).extraTrial as List<*>)[0] as Long,
                        (trial.extraTrial as List<*>)[1] as Long,
                        trial.extraTrial[2] as Long
                    )

                    mStimuliManager.deliverShiftedStimulus(trial.source, corr_delays.a, corr_delays.t, corr_delays.v){onTrialEnd()}
                }
                TEST_SAMPLE_PAIR    ->  mStimuliManager.deliverAlignedStimuliPair((trial as TrialSample).extraTrial as Long, trial.source){onTrialEnd()}
            }
        }, 1000)

    }
    // =============================================================================================================================
    // DEBUG
    // =============================================================================================================================
}