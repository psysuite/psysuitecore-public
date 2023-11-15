package iit.uvip.psysuite.core.tests.bis

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import iit.uvip.psysuite.adaptive.AdaptiveWrapper
import iit.uvip.psysuite.adaptive.TaskADAParams
import iit.uvip.psysuite.adaptive.ado.ADOParams
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.AudioManager
import iit.uvip.psysuite.core.stimuli.ImageViewDefinedException
import iit.uvip.psysuite.core.stimuli.StimuliManager
import iit.uvip.psysuite.core.stimuli.TactileManager
import iit.uvip.psysuite.core.stimuli.VibratorNotDefinedException
import iit.uvip.psysuite.core.stimuli.VisualManager
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.AdaptiveTrialsManager
import iit.uvip.psysuite.core.trials.FixedTrialsManager
import iit.uvip.psysuite.core.trials.TrialBasic
import iit.uvip.psysuite.core.trials.TrialsManager
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.StimuliSetBIS
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showToast

class TestBIS(
    ctx: Context,
    activity: Activity,
    hostfragment: Fragment,
    subject: SubjectBasicParcel,
    vibrator: VibrationManager?,
    mImageView: ImageView?,
    speechManager:SpeechManager?
) : TestBasic(ctx, activity, hostfragment, subject, vibrator, mImageView, speechManager){

    override var LOG_TAG:String = TestBIS::class.java.simpleName

    companion object {

        @JvmStatic val TEST_BASIC_LABEL                 = "BIS"

        @JvmStatic val STIMULUS_DURATION_VISUAL:Long    = 50
        @JvmStatic val STIMULUS_DURATION_TACTILE:Long   = 50
        @JvmStatic val STIMULUS_DURATION_AUDIO:Long     = 50
        @JvmStatic val QUESTION_DELAY                   = 1500      // latency
        @JvmStatic val FIRST_STIMULUS_DELAY             = 1000L     // ms to wait before sending the first trial
        @JvmStatic val LAST_STIMULUS_DELAY              = 1000L     // ms of the third stimulus wrt first
        @JvmStatic val MID_LATENCY                      = 500L      // ms of the mid latecny

        @JvmStatic val TRIAL_STAGE_1                    = 1
        @JvmStatic val TRIAL_STAGE_2                    = 2
        @JvmStatic val TRIAL_STAGE_3                    = 3

        @JvmStatic val AV_STIMULUS_DELTA                = 200       // ms between the AV stimuli

        @JvmStatic val STIMULUS_TYPE_AUDIO              = "AUDIO"
        @JvmStatic val STIMULUS_TYPE_TACTILE            = "TACTILE"
        @JvmStatic val STIMULUS_TYPE_VISUAL             = "VISUAL"
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE      = "AUDIO_TACTILE"
        @JvmStatic val STIMULUS_TYPE_AUDIO_VISUAL        = "AUDIO_VIDEO"
        @JvmStatic val STIMULUS_TYPE_VISUAL_TACTILE     = "VIDEO_TACTILE"

        @JvmStatic val STIMULUS_TYPE_AUDIO_LOG          = "A"
        @JvmStatic val STIMULUS_TYPE_TACTILE_LOG        = "T"
        @JvmStatic val STIMULUS_TYPE_VISUAL_LOG         = "V"
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE_LOG  = "AT"
        @JvmStatic val STIMULUS_TYPE_TACTILE_AUDIO_LOG  = "TA"
        @JvmStatic val STIMULUS_TYPE_AUDIO_VISUAL_LOG   = "AV"
        @JvmStatic val STIMULUS_TYPE_VISUAL_AUDIO_LOG   = "VA"
        @JvmStatic val STIMULUS_TYPE_VISUAL_TACTILE_LOG = "VT"
        @JvmStatic val STIMULUS_TYPE_TACTILE_VISUAL_LOG = "TV"

        @JvmStatic val CONFLICT_TYPE_NONE               = "none"

        fun getConditionsInfo(ctx: Context): List<ConditionData>{
            return if(VibrationManager.sysHasVibrator(ctx))
                mutableListOf(
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO           , TEST_BISECTION_AUDIO           , "${TEST_BASIC_LABEL}$STIMULUS_TYPE_AUDIO_LOG"           , Populations.hearing_populations),
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_TACTILE         , TEST_BISECTION_TACTILE         , "${TEST_BASIC_LABEL}$STIMULUS_TYPE_TACTILE_LOG"         , Populations.all_populations),
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_VISUAL          , TEST_BISECTION_VISUAL          , "${TEST_BASIC_LABEL}$STIMULUS_TYPE_VISUAL_LOG"          , Populations.sighted_populations),
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO_TACTILE   , TEST_BISECTION_AUDIO_TACTILE   , "${TEST_BASIC_LABEL}$STIMULUS_TYPE_AUDIO_TACTILE_LOG"   , Populations.hearing_populations),
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO_VISUAL     , TEST_BISECTION_AUDIO_VISUAL    , "${TEST_BASIC_LABEL}$STIMULUS_TYPE_AUDIO_VISUAL_LOG"    , Populations.sighted_hearing_populations),
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_VISUAL_TACTILE  , TEST_BISECTION_VISUAL_TACTILE  , "${TEST_BASIC_LABEL}$STIMULUS_TYPE_VISUAL_TACTILE_LOG"  , Populations.sighted_populations))
            else
                mutableListOf(
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO           , TEST_BISECTION_AUDIO          , "${TEST_BASIC_LABEL}$STIMULUS_TYPE_AUDIO_LOG"           , Populations.hearing_populations),
                    ConditionData(TEST_BASIC_LABEL + "_" + STIMULUS_TYPE_AUDIO_VISUAL     , TEST_BISECTION_AUDIO_VISUAL    , "${TEST_BASIC_LABEL}$STIMULUS_TYPE_AUDIO_VISUAL_LOG"     , Populations.sighted_hearing_populations))
        }
        fun getNextTrialModes(ctx:Context):List<List<Int>> = listOf(listOf(TEST_NEXTTRIAL_ANSWER)) //, TEST_NEXTTRIAL_VOICE_ANSWER, TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER))
    }

    // contains : stimulus type & delay
    private var trialsDefaultSchema:List<StimuliSetBIS> = listOf(
        StimuliSetBIS(4, 300F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(6, 200F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(6, 100F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(4, 50F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 15F, true, CONFLICT_TYPE_NONE),
        StimuliSetBIS(2, 15F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(4, 50F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(6, 100F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(6, 200F, false, CONFLICT_TYPE_NONE),
        StimuliSetBIS(4, 300F, false, CONFLICT_TYPE_NONE)
    )

    private val bimodalStimuliDelta = listOf(0F,100F,200F)       // ms between the AV stimuli

    // first stim is delivered at the given latency. the second  AV_STIMULUS_DELTA after
                                                                        // ntrials  latency conflict-type
    // 64 trials
    private var trialsAudioVisualSchema:List<StimuliSetBIS> = listOf(
        StimuliSetBIS(2, 300F, true,  STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(4, 200F, true,  STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(4, 100F, true,  STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(4, 50F,  true,  STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(2, 15F,  true,  STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(2, 15F,  false, STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(4, 50F,  false, STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(4, 100F, false, STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(4, 200F, false, STIMULUS_TYPE_VISUAL_AUDIO_LOG),
        StimuliSetBIS(2, 300F, false, STIMULUS_TYPE_VISUAL_AUDIO_LOG),

        StimuliSetBIS(2, 300F, true,  STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(4, 200F, true,  STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(4, 100F, true,  STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(4, 50F,  true,  STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(2, 15F,  true,  STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(2, 15F,  false, STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(4, 50F,  false, STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(4, 100F, false, STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(4, 200F, false, STIMULUS_TYPE_AUDIO_VISUAL_LOG),
        StimuliSetBIS(2, 300F, false, STIMULUS_TYPE_AUDIO_VISUAL_LOG)
    )
    private var trialsVisualTactileSchema:List<StimuliSetBIS> = listOf(
        StimuliSetBIS(2, 300F, true,  STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(4, 200F, true,  STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(4, 100F, true,  STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(4, 50F,  true,  STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(2, 15F,  true,  STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(2, 15F,  false, STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(4, 50F,  false, STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(4, 100F, false, STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(4, 200F, false, STIMULUS_TYPE_VISUAL_TACTILE_LOG),
        StimuliSetBIS(2, 300F, false, STIMULUS_TYPE_VISUAL_TACTILE_LOG),

        StimuliSetBIS(2, 300F, true,  STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(4, 200F, true,  STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(4, 100F, true,  STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(4, 50F,  true,  STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(4, 15F,  true,  STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(2, 15F,  false, STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(2, 50F,  false, STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(4, 100F, false, STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(4, 200F, false, STIMULUS_TYPE_TACTILE_VISUAL_LOG),
        StimuliSetBIS(2, 300F, false, STIMULUS_TYPE_TACTILE_VISUAL_LOG)
    )
    private var trialsAudioTactileSchema:List<StimuliSetBIS> = listOf(
        StimuliSetBIS(2, 300F, true,  STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(4, 200F, true,  STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(4, 100F, true,  STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(4, 50F,  true,  STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(4, 15F,  true,  STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(2, 15F,  false, STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(2, 50F,  false, STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(4, 100F, false, STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(4, 200F, false, STIMULUS_TYPE_AUDIO_TACTILE_LOG),
        StimuliSetBIS(2, 300F, false, STIMULUS_TYPE_AUDIO_TACTILE_LOG),

        StimuliSetBIS(2, 300F, true,  STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(4, 200F, true,  STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(4, 100F, true,  STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(4, 50F,  true,  STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(4, 15F,  true,  STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(2, 15F,  false, STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(2, 50F,  false, STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(4, 100F, false, STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(4, 200F, false, STIMULUS_TYPE_TACTILE_AUDIO_LOG),
        StimuliSetBIS(2, 300F, false, STIMULUS_TYPE_TACTILE_AUDIO_LOG)
    )

    private var STIM_A  = StimuliManager.STIM_TYPE_A4
    private var STIM_V  = StimuliManager.STIM_TYPE_V2
    private var STIM_T  = StimuliManager.STIM_TYPE_T1

    private var STIM_AV = STIM_A or STIM_V
    private var STIM_AT = STIM_A or STIM_T
    private var STIM_VT = STIM_V or STIM_T

    private var conflictType:String = ""
    private var currStimulus:String = ""
    private var currStimulusDuration2:Long     = 0L          // default value to be used when second stimulus duration in not given

    override var mDrawablesResource: MutableList<Int> = mutableListOf(R.drawable.white_circle, R.drawable.red_circle, R.drawable.grey_circle, R.drawable.blue_circle)

    private val nQuestTrials                = 30
    private val adoParams                   = ADOParams(guess_rate=0.5F, lapse_rate=0.04F, noise_perc=0.1F)
    private val taskADAParams               = TaskADAParams(400.0F, nQuestTrials+10)
    private val adoWrapper:AdaptiveWrapper  = AdaptiveWrapper("adopywrapper.AdopyWrapper", "AdopyWrapper", adoParams, taskADAParams)

    // =============================================================================================================================
    // INIT
    // =============================================================================================================================
    override fun initTest() {
        // set stimuli default & create mTrials list
        when {
            mImageView == null -> throw ImageViewDefinedException("IMAGE_VIEW_NOT_DEFINED")
            vibrator == null && (subject.type == TEST_BISECTION_TACTILE || subject.type == TEST_BISECTION_AUDIO_TACTILE) -> throw VibratorNotDefinedException("VIBRATOR_NOT_DEFINED")
        }
        validAnswers = mutableListOf(ctx.resources.getString(R.string.bisection_rb1_text), ctx.resources.getString(R.string.bisection_rb3_text))

        // set mQuestion/ currStimulusDuration/ currStimulusDuration2/ currStimulusLabel
        when (subject.type) {
            TEST_BISECTION_AUDIO            -> initBisectionAudio()
            TEST_BISECTION_TACTILE          -> initBisectionTactile()
            TEST_BISECTION_VISUAL           -> initBisectionVisual()
            TEST_BISECTION_AUDIO_TACTILE    -> initBisectionAudioTactile()
            TEST_BISECTION_AUDIO_VISUAL     -> initBisectionAudioVisual()
            else                            -> initBisectionVisualTactile()
        }

        mTrialsManager =
            when (subject.trman_type) {
                TEST_TRMAN_FIXED -> {
                    val trials = if (!subject.isDebug)
                                    when (subject.type) {
                                        TEST_BISECTION_AUDIO,
                                        TEST_BISECTION_TACTILE,
                                        TEST_BISECTION_VISUAL   -> createUnimodalTrials()
                                        else                    -> createBimodalTrials(subject.type)
                                    }
                                 else createTrialsDebug()

                    FixedTrialsManager(trials as MutableList<TrialBasic>)
                }
                else ->

                    when (subject.type) {
                        TEST_BISECTION_AUDIO,TEST_BISECTION_TACTILE,TEST_BISECTION_VISUAL -> {
                                val trials = createTrialsAdaptive()
                                val trman = AdaptiveTrialsManager(trials as MutableList<TrialBasic>, adoWrapper)
                                trman.getStimulus()
                                trman
                            }
                        else  -> throw ImageViewDefinedException("CONDITION NOT ALLOWED")
                    }
            }

        mTestLabel = ""
        getConditionsInfo(ctx).map {
            if (it.id == subject.type) mTestLabel = it.label
        }
        if(mTestLabel.isEmpty()) showToast("Should not happen. given test code was not recognized", ctx)
        createResultFile(TrialBIS.LOG_HEADER)

        mNoise = AudioManager.getAudioResource(ctx,"wnoise_20s", 0.01f)

        mStimuliManager =   if(vibrator != null)
                                StimuliManager(
                                    AudioManager(STIM_A, audioResources[STIMULUS_DURATION_AUDIO] ?: "t1000hz_50ms.wav",  duration = STIMULUS_DURATION_AUDIO, handler = mStimuliHandler, ctx = ctx),
                                    TactileManager(vibrator, duration = STIMULUS_DURATION_TACTILE, handler = mStimuliHandler),
                                    VisualManager(STIM_V, mImageView!!, mDrawablesResource[1], mDrawablesResource[0], duration = STIMULUS_DURATION_VISUAL, handler = mStimuliHandler),
                                    delaysAligner, ctx, mStimuliHandler)
                            else
                                StimuliManager(
                                    AudioManager(STIM_A, audioResources[STIMULUS_DURATION_AUDIO] ?: "t1000hz_50ms.wav",  duration = STIMULUS_DURATION_AUDIO, handler = mStimuliHandler, ctx = ctx),
                                    null,
                                    VisualManager(STIM_V, mImageView!!, mDrawablesResource[1], mDrawablesResource[0], duration = STIMULUS_DURATION_VISUAL, handler = mStimuliHandler),
                                    delaysAligner, ctx, mStimuliHandler)

        testEvent.accept(Triple(EVENT_TEST_SETUP_COMPLETED, null, listOf()))
    }

    // =============================================================================================================================
    // INIT TRIALS
    // =============================================================================================================================
    private fun initBisectionAudio(){
        mQuestion               = ctx.resources.getString(R.string.bisection_question_text_audio)
        currStimulusDuration    = STIMULUS_DURATION_AUDIO
        currStimulusLabel       = STIMULUS_TYPE_AUDIO
        conflictType            = CONFLICT_TYPE_NONE
    }

    private fun initBisectionTactile(){
        mQuestion               = ctx.resources.getString(R.string.bisection_question_text_tactile)
        currStimulusDuration    = STIMULUS_DURATION_TACTILE
        currStimulusLabel       = STIMULUS_TYPE_TACTILE
        conflictType            = CONFLICT_TYPE_NONE
    }

    private fun initBisectionVisual(){
        mQuestion               = ctx.resources.getString(R.string.bisection_question_text_visual)
        currStimulusDuration    = STIMULUS_DURATION_VISUAL
        currStimulusLabel       = STIMULUS_TYPE_VISUAL
        conflictType            = CONFLICT_TYPE_NONE
    }

    private fun initBisectionAudioTactile(){
        mQuestion               = ctx.resources.getString(R.string.bisection_question_text_mixed)
        currStimulusDuration    = STIMULUS_DURATION_AUDIO
        currStimulusDuration2   = STIMULUS_DURATION_TACTILE
        currStimulusLabel       = STIMULUS_TYPE_AUDIO_TACTILE
        conflictType            = STIMULUS_TYPE_AUDIO_TACTILE_LOG
    }

    private fun initBisectionAudioVisual(){
        mQuestion               = ctx.resources.getString(R.string.bisection_question_text_mixed)
        currStimulusDuration    = STIMULUS_DURATION_AUDIO
        currStimulusDuration2   = STIMULUS_DURATION_VISUAL
        currStimulusLabel       = STIMULUS_TYPE_AUDIO_VISUAL
        conflictType            = STIMULUS_TYPE_AUDIO_VISUAL_LOG
    }

    private fun initBisectionVisualTactile(){
        mQuestion               = ctx.resources.getString(R.string.bisection_question_text_mixed)
        currStimulusDuration    = STIMULUS_DURATION_VISUAL
        currStimulusDuration2   = STIMULUS_DURATION_TACTILE
        currStimulusLabel       = STIMULUS_TYPE_VISUAL_TACTILE
        conflictType            = STIMULUS_TYPE_VISUAL_TACTILE_LOG
    }

    // =============================================================================================================================
    // CREATE TRIALS
    // =============================================================================================================================
    private fun createUnimodalTrials():List<TrialBasic>{

        val trials:MutableList<TrialBasic> = mutableListOf()
        for(section in trialsDefaultSchema)
            for(i in 0 until section.ntrials)
                //                      id   type       label,          corr_answ, stim_value          conflict_type     duration  duration2
                trials.add(TrialBIS(-1, subject.type, currStimulusLabel, section.magnitude, section.isBefore, section.conflict, currStimulusDuration))

        trials.shuffle()
        return trials
    }

    private fun createTrialsAdaptive():List<TrialBasic>{
        var cnt = -1
        val trials: MutableList<TrialBasic> = mutableListOf()
        for (i in 0 until nQuestTrials/2){
            trials.add(TrialBIS(++cnt, subject.type, STIMULUS_TYPE_AUDIO, TrialsManager.ADAPTIVE_VALUE, true , CONFLICT_TYPE_NONE,STIMULUS_DURATION_AUDIO, isADA=true))
            trials.add(TrialBIS(++cnt, subject.type, STIMULUS_TYPE_AUDIO, TrialsManager.ADAPTIVE_VALUE, false, CONFLICT_TYPE_NONE,STIMULUS_DURATION_AUDIO, isADA=true))
        }

        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 300F, true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 200F, true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 100F, true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 50F , true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 15F , true, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 15F , false, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 50F , false, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 100F, false, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 200F, false, conflictType, currStimulusDuration, currStimulusDuration2))
        trials.add(TrialBIS(-1, subject.type, currStimulusLabel, 300F, false, conflictType, currStimulusDuration, currStimulusDuration2))

        trials.shuffle()
        return trials
    }

    private fun createBimodalTrials(type:Int):List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        val schema = when(type){
            TEST_BISECTION_AUDIO_TACTILE    -> trialsAudioTactileSchema
            TEST_BISECTION_AUDIO_VISUAL     -> trialsAudioVisualSchema
            else                            -> trialsVisualTactileSchema
        }
        bimodalStimuliDelta.map{
            for(section in schema)
                for(i in 0 until section.ntrials){
                    when(section.conflict == conflictType){
                        //                                 id   type        label,                   corr_answ, stim_value          conflict_type   duration       duration2
                        true    -> trials.add(TrialBIS(-1, subject.type, currStimulusLabel, section.magnitude, section.isBefore, section.conflict, currStimulusDuration, currStimulusDuration2, conflict_magn=it))
                        false   -> trials.add(TrialBIS(-1, subject.type, currStimulusLabel, section.magnitude, section.isBefore, section.conflict, currStimulusDuration2, currStimulusDuration, conflict_magn=it))
                    }
                }
        }
        trials.shuffle()
        return trials
    }

    private fun createTrialsDebug():List<TrialBasic>{
        mQuestion = ctx.resources.getString(R.string.bisection_question_text_mixed)

        val trials:MutableList<TrialBasic> = mutableListOf()
        for(i in 0 until 10000){
            //                     id   type                        label,                        corr_answ, stim_value          conflict_type   duration       duration2
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_TACTILE, STIMULUS_TYPE_AUDIO_TACTILE, 400F, true, CONFLICT_TYPE_NONE, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_TACTILE))
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_TACTILE, STIMULUS_TYPE_AUDIO_TACTILE, 400F, false, CONFLICT_TYPE_NONE, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_TACTILE))

            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VISUAL, STIMULUS_TYPE_AUDIO_VISUAL, 400F, true, STIMULUS_TYPE_VISUAL_AUDIO_LOG, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_VISUAL))
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VISUAL, STIMULUS_TYPE_AUDIO_VISUAL, 400F, false, STIMULUS_TYPE_VISUAL_AUDIO_LOG, STIMULUS_DURATION_AUDIO, STIMULUS_DURATION_VISUAL))
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VISUAL, STIMULUS_TYPE_AUDIO_VISUAL, 400F, true, STIMULUS_TYPE_AUDIO_VISUAL_LOG, STIMULUS_DURATION_VISUAL, STIMULUS_DURATION_AUDIO))
            trials.add(TrialBIS(-1, TEST_BISECTION_AUDIO_VISUAL, STIMULUS_TYPE_AUDIO_VISUAL, 400F, false, STIMULUS_TYPE_AUDIO_VISUAL_LOG, STIMULUS_DURATION_VISUAL, STIMULUS_DURATION_AUDIO))
        }
        return trials
    }

    // =============================================================================================================================
    // MANAGE TRIALS STIMULI
    // =============================================================================================================================
    override fun onTrialEnd(){

        mNoise?.stop()
        mNoise?.prepare()

        testEvent.accept(Triple(EVENT_GIVE_ANSWER, null, listOf()))
    }

    override fun initSummary(){}

    // =============================================================================================================================
    // DELIVER STIMULI
    // =============================================================================================================================

    // a trial has this temporal line:
    // +  FIRST_STIMULUS_DELAY                          => 1st stim
    // + (FIRST_STIMULUS_DELAY + mTrial.stim_value)     => 2nd stim
    // + (FIRST_STIMULUS_DELAY + LAST_STIMULUS_DELAY)   => 3rd stim
    // + (QUESTION_DELAY + FIRST_STIMULUS_DELAY)        => event : show question
    override fun show(trial: TrialBasic, isRepeat:Boolean){

        mNoise?.start()
        if(isRepeat)    mTrial.repetitions++

        // to align bimodal stimuli, I have to delay the fastest modality by time_shift ms.
        // Thus I anticipate all main onsets by the same ms.
        // Since this code act for every kind of stimulus combination, I assume a trimodal stim
        val time_shift = when(trial.type){
            TEST_BISECTION_AUDIO_TACTILE    -> delaysAligner.getShift(STIM_AT, 0,0,-1)
            TEST_BISECTION_AUDIO_VISUAL      -> delaysAligner.getShift(STIM_AV, 0,-1,0)
            else                            -> 0
        }

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialBIS, TRIAL_STAGE_1)
            testEvent.accept(Triple(EVENT_STIMULI_START, null, listOf()))
        }, FIRST_STIMULUS_DELAY - time_shift)

        mStimuliHandler.postDelayed({
            deliverStimulus(trial as TrialBIS, TRIAL_STAGE_2)
        }, (FIRST_STIMULUS_DELAY - time_shift + (trial as TrialBIS).stim_value))

        mStimuliHandler.postDelayed({
            deliverStimulus(trial, TRIAL_STAGE_3)
        }, (FIRST_STIMULUS_DELAY - time_shift + LAST_STIMULUS_DELAY))

        mStimuliHandler.postDelayed({
            onTrialEnd()
        }, (FIRST_STIMULUS_DELAY - time_shift + QUESTION_DELAY))
    }

    private fun deliverStimulus(trial: TrialBIS, stage:Int=0){

        when(trial.type) {
            TEST_BISECTION_AUDIO            ->  mStimuliManager.deliverAStimulus()
            TEST_BISECTION_TACTILE          ->  mStimuliManager.deliverTStimulus()
            TEST_BISECTION_VISUAL           ->  mStimuliManager.deliverVStimulus()
            TEST_BISECTION_AUDIO_TACTILE    ->  deliverATStimuli(trial, stage)
            TEST_BISECTION_AUDIO_VISUAL     ->  deliverAVStimuli(trial, stage)
            TEST_BISECTION_VISUAL_TACTILE   ->  deliverVTStimuli(trial, stage)
        }
    }

    private fun deliverATStimuli(trial:TrialBIS, stage:Int=0){

        if(stage == TRIAL_STAGE_2){
            // mid (second) stimulus: audio and video are dissociated
            val corr_delays =   if(trial.conflict_type == STIMULUS_TYPE_AUDIO_TACTILE_LOG)
                                    delaysAligner.arrangeDelays(STIM_AT, 0, trial.conflict_magn.toLong(),-1)
                                else
                                    delaysAligner.arrangeDelays(STIM_AT, trial.conflict_magn.toLong(),0, -1)

            mStimuliManager.deliverShiftedStimulus(STIM_AT, corr_delays.a, -1, corr_delays.v)
        }
        // normal stimulus (1st or 3rd): audio and video simultaneously
        else    mStimuliManager.deliverAlignedStimulus(STIM_AT)
    }

    private fun deliverAVStimuli(trial:TrialBIS, stage:Int=0){

        mStimuliManager.mVisualManager!!.drawResOn = mDrawablesResource[stage]
        if(stage == TRIAL_STAGE_2){
            // mid (second) stimulus: audio and video are dissociated
            val corr_delays =   if(trial.conflict_type == STIMULUS_TYPE_VISUAL_AUDIO_LOG)
                                    delaysAligner.arrangeDelays(STIM_AV, AV_STIMULUS_DELTA.toLong(),-1,0)
                                else
                                    delaysAligner.arrangeDelays(STIM_AV,0, -1, AV_STIMULUS_DELTA.toLong())
            mStimuliManager.deliverShiftedStimulus(STIM_AV, corr_delays.a, -1, corr_delays.v)
        }
        // normal stimulus (1st or 3rd): audio and video simultaneously
        else    mStimuliManager.deliverAlignedStimulus(STIM_AV)
    }

    private fun deliverVTStimuli(trial:TrialBIS, stage:Int=0){

        mStimuliManager.mVisualManager!!.drawResOn = mDrawablesResource[stage]
        if(stage == TRIAL_STAGE_2){
            // mid (second) stimulus: audio and video are dissociated
            val corr_delays =   if(trial.conflict_type == STIMULUS_TYPE_VISUAL_TACTILE_LOG)
                                    delaysAligner.arrangeDelays(STIM_VT, -1, trial.conflict_magn.toLong(),0)
                                else
                                    delaysAligner.arrangeDelays(STIM_VT,-1, 0, trial.conflict_magn.toLong())
            mStimuliManager.deliverShiftedStimulus(STIM_VT, -1, corr_delays.v, corr_delays.v)
        }
        // normal stimulus (1st or 3rd): audio and video simultaneously
        else    mStimuliManager.deliverAlignedStimulus(STIM_VT)
    }

    // =====================================================================================
    // DEBUG
    // =====================================================================================
    // Trial(val type:Int, val label:String, val conflict_type:String, val stim_value:Int, val duration:Int)
    // just one trial for each latency
    private var trialsDefaultSchema_debug: List<StimuliSetBIS> = listOf(StimuliSetBIS(2, 200F, true, CONFLICT_TYPE_NONE))

    private fun createDefaultTrials_debug(stim_type_label:String, duration:Long, duration2:Long=0L):List<TrialBasic>{
        val trials:MutableList<TrialBasic> = mutableListOf()
        for(section in trialsDefaultSchema)
            for(i in 0 until 1)
                trials.add(TrialBIS(-1, subject.type, stim_type_label, section.magnitude, section.isBefore, section.conflict, duration, duration2))

        trials.shuffle()
        return trials
    }
    // =============================================================================================================================
}