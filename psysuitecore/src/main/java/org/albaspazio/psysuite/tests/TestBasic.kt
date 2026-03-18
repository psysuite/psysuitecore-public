package org.albaspazio.psysuite.tests

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.PublishRelay
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.logLastTwo
import org.albaspazio.core.filesystem.deleteFile
import org.albaspazio.core.filesystem.getAbsoluteFilePath
import org.albaspazio.core.filesystem.notifyFile
import org.albaspazio.core.filesystem.renameFile
import org.albaspazio.core.filesystem.saveTextQ
import org.albaspazio.core.speech.SpeechManager
import org.albaspazio.core.ui.showAlert
import org.albaspazio.psysuite.core.R
import org.albaspazio.psysuite.tests.SubjectBasicParcel
import org.albaspazio.psysuite.model.summary.Summary
import org.albaspazio.psysuite.stimuli.StimuliManager
import org.albaspazio.psysuite.trials.AdaptiveTrialsManager
import org.albaspazio.psysuite.tests.TrialBasic
import org.albaspazio.psysuite.trials.TrialsManager
import org.albaspazio.psysuite.utility.filesystem.FileSystemManager

/**
 * Base class for all psychophysical tests within the Psysuite framework.
 * It provides common functionality, constants, and lifecycle management for tests.
 *
 * This abstract class handles:
 * - Initialization of test parameters and resources.
 * - Management of trials through a [org.albaspazio.psysuite.trials.TrialsManager].
 * - Delivery of stimuli via a [org.albaspazio.psysuite.stimuli.StimuliManager].
 * - Event handling and communication with the UI (typically a Fragment).
 * - Saving test results and subject data.
 * - Configuration of test behavior like trial display modes and abort mechanisms.
 *
 * Subclasses must implement specific test logic, including how stimuli are presented (`show`),
 * what happens at the end of a trial (`onTrialEnd`), and how the summary data is initialized (`initSummary`).
 *
 * @property ctx The application context.
 * @property activity The hosting activity.
 * @property hostfragment The hosting fragment, used for UI interactions like showing alerts.
 * @property subject The [SubjectBasicParcel] containing subject information and test configuration.
 * @property vibrator An optional [org.albaspazio.core.accessory.VibrationManager] for tests involving tactile stimuli.
 * @property mImageView An optional [android.widget.ImageView] for tests involving visual stimuli.
 * @property speechManager An optional [org.albaspazio.core.speech.SpeechManager] for tests involving voice input.
 * @property outResultsDir The directory where result files will be saved. Defaults to `Environment.DIRECTORY_DOWNLOADS/FileSystemManager.RESULTS_FOLDER_NAME`.
 */
abstract class TestBasic(protected val ctx: Context,
                         protected val activity: Activity,
                         protected val hostfragment: Fragment,
                         protected val subject: SubjectBasicParcel,
                         protected val vibrator: VibrationManager? = null,
                         protected val mImageView: ImageView? = null,
                         protected val speechManager: SpeechManager? = null,
                         protected val mainView: View? = null,
                         protected val outResultsDir:String= "${Environment.DIRECTORY_DOWNLOADS}/${FileSystemManager.Companion.RESULTS_FOLDER_NAME}")
{

    /**
     * The log tag used for logging messages from this test instance.
     * Defaults to the simple name of the concrete test class.
     */
    open var LOG_TAG:String = TestBasic::class.java.simpleName

    /**
     * Companion object for [TestBasic], containing constants and static utility members
     * used across different tests.
     */
    companion object {

        /** Label used for bundling test information, typically for passing subject/test details. */
        @JvmStatic val TESTINFO_BUNDLE_LABEL            = "test"
        /** File extension for subject data files. */
        @JvmStatic val SUBJFILE_EXTENSION: String       = ".json"
        /** File extension for result files. */
        @JvmStatic val RES_EXTENSION: String            = ".txt"
        /** Label used for bundling test results. */
        @JvmStatic val TEST_BUNDLE_RESULT_LABEL: String = "result"

        /**
         * A map of audio resource durations (in milliseconds) to their corresponding raw file names.
         * These resources are typically short tone sounds used as auditory stimuli.
         */
        @JvmStatic val audioResources:HashMap<Long, String> = hashMapOf(
            7L      to "t1000hz_7ms.wav",
            10L     to "t1000hz_10ms.wav",
            17L     to "t1000hz_17ms.wav",
            20L     to "t1000hz_20ms.wav",
            30L     to "t1000hz_30ms.wav",
            35L     to "t1000hz_35ms.wav",
            50L     to "t1000hz_50ms.wav",
            100L    to "t1000hz_100ms.wav",
            1000L   to "t1000hz_1000ms.wav",
            2000L   to "t300hz_2000ms.wav",
        )

        //-----------------------------------------------------------------------------------------
        // region ---- TESTS UNIQUE CODES ---
        //-----------------------------------------------------------------------------------------

        /** Unique code for Sample test with aligned stimuli. */
        @JvmStatic val TEST_SAMPLE_ALIGNED          = 1
        /** Unique code for Sample test with shifted stimuli. */
        @JvmStatic val TEST_SAMPLE_SHIFTED          = 2
        /** Unique code for Sample test with paired stimuli. */
        @JvmStatic val TEST_SAMPLE_PAIR             = 3

        /** Unique code for Reaction Time test with audio stimulus. */
        @JvmStatic val TEST_RT_AUDIO                = 51
        /** Unique code for Reaction Time test with tactile stimulus. */
        @JvmStatic val TEST_RT_TACTILE              = 52
        /** Unique code for Reaction Time test with visual stimulus. */
        @JvmStatic val TEST_RT_VISUAL               = 53

        /** Unique code for Bisection test with audio stimulus. */
        @JvmStatic val TEST_BISECTION_AUDIO                 = 101
        /** Unique code for Bisection test with visual stimulus. */
        @JvmStatic val TEST_BISECTION_VISUAL                = 102
        /** Unique code for Bisection test with tactile stimulus. */
        @JvmStatic val TEST_BISECTION_TACTILE               = 103
        /** Unique code for Bisection test with audio-visual stimuli. */
        @JvmStatic val TEST_BISECTION_AUDIO_VISUAL          = 104
        /** Unique code for Bisection test with audio-tactile stimuli. */
        @JvmStatic val TEST_BISECTION_AUDIO_TACTILE         = 105
        /** Unique code for Bisection test with visual-tactile stimuli. */
        @JvmStatic val TEST_BISECTION_VISUAL_TACTILE        = 106
        /** Unique code for Bisection test with supra-threshold audio stimulus. */
        @JvmStatic val TEST_BISECTION_AUDIO_SUPRA           = 107
        /** Unique code for Bisection test with supra-threshold visual stimulus. */
        @JvmStatic val TEST_BISECTION_VISUAL_SUPRA          = 108
        /** Unique code for Bisection test with supra-threshold tactile stimulus. */
        @JvmStatic val TEST_BISECTION_TACTILE_SUPRA         = 109
        /** Unique code for Bisection test with supra-threshold audio-visual stimuli. */
        @JvmStatic val TEST_BISECTION_AUDIO_VISUAL_SUPRA    = 110
        /** Unique code for Bisection test with supra-threshold audio-tactile stimuli. */
        @JvmStatic val TEST_BISECTION_AUDIO_TACTILE_SUPRA   = 111
/** Unique code for Bisection test with supra-threshold visual-tactile stimuli. */
        @JvmStatic val TEST_BISECTION_VISUAL_TACTILE_SUPRA  = 112
        /** Unique code for Bisection test with audio stimulus comparing SUB vs SUPRA. */
        @JvmStatic val TEST_BISECTION_AUDIO_SUBSUPRA        = 113
        /** Unique code for Bisection test with visual stimulus comparing SUB vs SUPRA. */
        @JvmStatic val TEST_BISECTION_VISUAL_SUBSUPRA       = 114
        /** Unique code for Bisection test with tactile stimulus comparing SUB vs SUPRA. */
        @JvmStatic val TEST_BISECTION_TACTILE_SUBSUPRA      = 115


        /** Unique code for Temporal Integration Window (TID) test with short audio stimulus. */
        @JvmStatic val TEST_TID_SHORT_AUDIO         = 201
        /** Unique code for Temporal Integration Window (TID) test with short visual stimulus. */
        @JvmStatic val TEST_TID_SHORT_VISUAL        = 206
        /** Unique code for Temporal Integration Window (TID) test with short tactile stimulus. */
        @JvmStatic val TEST_TID_SHORT_TACTILE       = 202
        /** Unique code for Temporal Integration Window (TID) test with long audio stimulus. */
        @JvmStatic val TEST_TID_LONG_AUDIO          = 203
        /** Unique code for Temporal Integration Window (TID) test with long visual stimulus. */
        @JvmStatic val TEST_TID_LONG_VISUAL         = 205
        /** Unique code for Temporal Integration Window (TID) test with long tactile stimulus. */
        @JvmStatic val TEST_TID_LONG_TACTILE        = 204
        /** Unique code for Temporal Integration Window (TID) training with short audio stimulus. */
        @JvmStatic val TEST_TID_SHORT_AUDIO_TRAIN   = 207
        /** Unique code for Temporal Integration Window (TID) training with short visual stimulus. */
        @JvmStatic val TEST_TID_SHORT_VISUAL_TRAIN  = 208
        /** Unique code for Temporal Integration Window (TID) training with short tactile stimulus. */
        @JvmStatic val TEST_TID_SHORT_TACTILE_TRAIN = 209


        /** Unique code for Auditory Temporal Binding (ATB) test with single stimulus. */
        @JvmStatic val TEST_ATB_TIME_SINGLESTIM     = 301
        /** Unique code for Auditory Temporal Binding (ATB) test with double stimuli. */
        @JvmStatic val TEST_ATB_TIME_DOUBLESTIM     = 302
        /** Unique code for Auditory Temporal Binding (ATB) test with inferred timing. */
        @JvmStatic val TEST_ATB_TIME_INF            = 303
        /** Unique code for Auditory Temporal Binding (ATB) test with single stimulus for toddlers. */
        @JvmStatic val TEST_ATB_TIME_SINGLESTIM_TOD = 304
        /** Unique code for Auditory Temporal Binding (ATB) test with double stimuli for toddlers. */
        @JvmStatic val TEST_ATB_TIME_DOUBLESTIM_TOD = 305

        /** Unique code for Audio-Tactile-Visual Binding (ATVB) test, single stimulus, unbalanced. */
        @JvmStatic val TEST_ATVB_TIME_S_UNBAL       = 401
        /** Unique code for Audio-Tactile-Visual Binding (ATVB) test, double stimuli, unbalanced. */
        @JvmStatic val TEST_ATVB_TIME_D_UNBAL       = 402
        /** Unique code for Audio-Tactile-Visual Binding (ATVB) test, double stimuli, balanced. */
        @JvmStatic val TEST_ATVB_TIME_D_BAL         = 403
        /** Unique code for Audio-Tactile-Visual Binding (ATVB) test, single stimulus, balanced. */
        @JvmStatic val TEST_ATVB_TIME_S_BAL         = 404
        /** Unique code for Audio-Tactile-Visual Binding (ATVB) test, single stimulus, balanced (alternative). */
        @JvmStatic val TEST_ATVB_TIME_S_BAL2        = 405

        /** Unique code for Visual Temporal Binding (TVB) test with single stimulus. */
        @JvmStatic val TEST_TVB_TIME_SINGLESTIM     = 501
        /** Unique code for Visual Temporal Binding (TVB) test with double stimuli. */
        @JvmStatic val TEST_TVB_TIME_DOUBLESTIM     = 502
        /** Unique code for Visual Temporal Binding (TVB) test with inferred timing. */
        @JvmStatic val TEST_TVB_TIME_INF            = 503
        /** Unique code for Visual Temporal Binding (TVB) test with single stimulus for toddlers. */
        @JvmStatic val TEST_TVB_TIME_SINGLESTIM_TOD = 504
        /** Unique code for Visual Temporal Binding (TVB) test with double stimuli for toddlers. */
        @JvmStatic val TEST_TVB_TIME_DOUBLESTIM_TOD = 505

        /** Unique code for Audio-Visual Binding (AVB) test with single stimulus. */
        @JvmStatic val TEST_AVB_TIME_SINGLESTIM     = 601
        /** Unique code for Audio-Visual Binding (AVB) test with double stimuli. */
        @JvmStatic val TEST_AVB_TIME_DOUBLESTIM     = 602
        /** Unique code for Audio-Visual Binding (AVB) test with inferred timing. */
        @JvmStatic val TEST_AVB_TIME_INF            = 603
        /** Unique code for Audio-Visual Binding (AVB) test with single stimulus for toddlers. */
        @JvmStatic val TEST_AVB_TIME_SINGLESTIM_TOD = 604
        /** Unique code for Audio-Visual Binding (AVB) test with double stimuli for toddlers. */
        @JvmStatic val TEST_AVB_TIME_DOUBLESTIM_TOD = 605


        /** Unique code for Temporal Frequency Illusions (TFI) test. */
        @JvmStatic val TEST_TFI                     = 701
        /** Unique code for Temporal Frequency Illusions (TFI) test for toddlers. */
        @JvmStatic val TEST_TFI_TODDLERS            = 702
        /** Unique code for Temporal Frequency Illusions (TFI) test with bimodal stimuli. */
        @JvmStatic val TEST_TFI_BIMODAL             = 703
        /** Unique code for Temporal Frequency Illusions (TFI) test with audio-visual stimuli. */
        @JvmStatic val TEST_TFI_AV                  = 704

        /** Unique code for Figure-Ground Illusion (FGI) test, type 1, unscrambled. */
        @JvmStatic val TEST_FGI_1_UNSCRAMBLED       = 801
        /** Unique code for Figure-Ground Illusion (FGI) test, type 1, scrambled. */
        @JvmStatic val TEST_FGI_1_SCRAMBLED         = 802
        /** Unique code for Figure-Ground Illusion (FGI) test, type 2, unscrambled. */
        @JvmStatic val TEST_FGI_2_UNSCRAMBLED       = 803
        /** Unique code for Figure-Ground Illusion (FGI) test, type 2, scrambled. */
        @JvmStatic val TEST_FGI_2_SCRAMBLED         = 804
        /** Unique code for Figure-Ground Illusion (FGI) test, type 3, unscrambled. */
        @JvmStatic val TEST_FGI_3_UNSCRAMBLED       = 805
        /** Unique code for Figure-Ground Illusion (FGI) test, type 3, scrambled. */
        @JvmStatic val TEST_FGI_3_SCRAMBLED         = 806

        /** Unique code for Rivalry/Grouping (RIVGRP) test, rivalry, high frequency. */
        @JvmStatic val TEST_RIVGRP_RIV_HF           = 901
        /** Unique code for Rivalry/Grouping (RIVGRP) test, grouping, high frequency. */
        @JvmStatic val TEST_RIVGRP_GRP_HF           = 902
        /** Unique code for Rivalry/Grouping (RIVGRP) test, rivalry and grouping, high frequency. */
        @JvmStatic val TEST_RIVGRP_RIVGRP_HF        = 903
        /** Unique code for Rivalry/Grouping (RIVGRP) test, rivalry, high contrast. */
        @JvmStatic val TEST_RIVGRP_RIV_HC           = 904
        /** Unique code for Rivalry/Grouping (RIVGRP) test, grouping, high contrast. */
        @JvmStatic val TEST_RIVGRP_GRP_HC           = 905
        /** Unique code for Rivalry/Grouping (RIVGRP) test, rivalry and grouping, high contrast. */
        @JvmStatic val TEST_RIVGRP_RIVGRP_HC        = 906

        /** Unique code for Beads test with low uncertainty. */
        @JvmStatic val TEST_BEADS_LOWUNCERT         = 1001
        /** Unique code for Beads test with medium uncertainty. */
        @JvmStatic val TEST_BEADS_MIDUNCERT         = 1002

        /** Unique code for Motion Prediction (MOTPRE) test, visual-horizontal. */
        @JvmStatic val TEST_MOTPRE_VH               = 1101
        /** Unique code for Motion Prediction (MOTPRE) test, visual-vertical. */
        @JvmStatic val TEST_MOTPRE_VV               = 1102
        /** Unique code for Motion Prediction (MOTPRE) test, visual-horizontal-vertical. */
        @JvmStatic val TEST_MOTPRE_VHV              = 1103
        /** Unique code for Motion Prediction (MOTPRE) test, visual-vertical, arrow cue. */
        @JvmStatic val TEST_MOTPRE_VV_CUE_ARROW     = 1104
        /** Unique code for Motion Prediction (MOTPRE) test, visual-horizontal, arrow cue. */
        @JvmStatic val TEST_MOTPRE_VH_CUE_ARROW     = 1105
        /** Unique code for Motion Prediction (MOTPRE) test, visual-vertical, weight cue. */
        @JvmStatic val TEST_MOTPRE_VV_CUE_WEIGHT    = 1106
        /** Unique code for Motion Prediction (MOTPRE) test, visual-horizontal, fixed speed. */
        @JvmStatic val TEST_MOTPRE_VH_FIXSPEED      = 1107
        /** Unique code for Motion Prediction (MOTPRE) test, visual-horizontal, variable speed, fixed visual target. */
        @JvmStatic val TEST_MOTPRE_VH_VARSPEED_FIXVT= 1108
        /** Unique code for Motion Prediction (MOTPRE) test, visual-horizontal, variable speed, fixed visual path length. */
        @JvmStatic val TEST_MOTPRE_VH_VARSPEED_FIXVPL= 1109

        /** Unique code for Temporal Scaling Paradigm (TSP) test, audio, sub-threshold. */
        @JvmStatic val TEST_TSP_A_SUB               = 1201
        /** Unique code for Temporal Scaling Paradigm (TSP) test, visual, sub-threshold. */
        @JvmStatic val TEST_TSP_V_SUB               = 1202
        /** Unique code for Temporal Scaling Paradigm (TSP) test, tactile, sub-threshold. */
        @JvmStatic val TEST_TSP_T_SUB               = 1203
        /** Unique code for Temporal Scaling Paradigm (TSP) test, audio, supra-threshold. */
        @JvmStatic val TEST_TSP_A_SUPRA             = 1204
        /** Unique code for Temporal Scaling Paradigm (TSP) test, visual, supra-threshold. */
        @JvmStatic val TEST_TSP_V_SUPRA             = 1205
        /** Unique code for Temporal Scaling Paradigm (TSP) test, tactile, supra-threshold. */
        @JvmStatic val TEST_TSP_T_SUPRA             = 1206
        /** Unique code for Temporal Scaling Paradigm (TSP) test, audio, sub-threshold. */
        @JvmStatic val TEST_TSP_A_SUBSUPRA          = 1207
        /** Unique code for Temporal Scaling Paradigm (TSP) test, visual, sub-threshold. */
        @JvmStatic val TEST_TSP_V_SUBSUPRA          = 1208
        /** Unique code for Temporal Scaling Paradigm (TSP) test, tactile, sub-threshold. */
        @JvmStatic val TEST_TSP_T_SUBSUPRA          = 1209


        /** Unique code for Temporal Integration Range (TIR) test, audio, sub-threshold. */
        @JvmStatic val TEST_TIR_A_SUB               = 1301
        /** Unique code for Temporal Integration Range (TIR) test, visual, sub-threshold. */
        @JvmStatic val TEST_TIR_V_SUB               = 1302
        /** Unique code for Temporal Integration Range (TIR) test, tactile, sub-threshold. */
        @JvmStatic val TEST_TIR_T_SUB               = 1303
        /** Unique code for Temporal Integration Range (TIR) test, audio, supra-threshold. */
        @JvmStatic val TEST_TIR_A_SUPRA             = 1304
        /** Unique code for Temporal Integration Range (TIR) test, visual, supra-threshold. */
        @JvmStatic val TEST_TIR_V_SUPRA             = 1305
        /** Unique code for Temporal Integration Range (TIR) test, tactile, supra-threshold. */
        @JvmStatic val TEST_TIR_T_SUPRA             = 1306
        /** Unique code for Temporal Integration Range (TIR) test, audio, sub + supra thresholds. */
        @JvmStatic val TEST_TIR_A_SUBSUPRA          = 1307
        /** Unique code for Temporal Integration Range (TIR) test, visual, sub + supra thresholds. */
        @JvmStatic val TEST_TIR_V_SUBSUPRA          = 1308
        /** Unique code for Temporal Integration Range (TIR) test, tactile, sub + supra thresholds. */
        @JvmStatic val TEST_TIR_T_SUBSUPRA          = 1309

        /** Unique code for Musical Meters test. */
        @JvmStatic val TEST_MUSICAL_METERS          = 1401

        // endregion
        //-----------------------------------------------------------------------------------------

        /** Constant used in SubjectBasicParcel to indicate that test is not longitudinal. session = 1 is automatically assigned. */
        @JvmStatic val TEST_NO_LONGITUDINAL            = -1000
        @JvmStatic val TEST_LONGITUDINAL_TOBESELECTED  = -1

        // --------------------------------------------------------------------------------------------
        // region ---- USER MODIFIED, between-trial, BEHAVIOUR ----
        //-----------------------------------------------------------------------------------------
        /** Constant indicating that trial IDs should never be shown. */
        @JvmStatic val TEST_SHOWTRIALS_NEVER            = 0
        /** Constant indicating that trial IDs should be shown at the end of each trial. */
        @JvmStatic val TEST_SHOWTRIALS_TRIALEND         = 1
        /** Constant indicating that trial IDs should always be shown. */
        @JvmStatic val TEST_SHOWTRIALS_ALWAYS           = 2

        /** Constant indicating that the abort button should be shown at the end of each trial (if no answer dialog). */
        @JvmStatic val TEST_ABORT_TRIALEND              = 1
        /** Constant indicating that the abort button should always be active. */
        @JvmStatic val TEST_ABORT_ALWAYS                = 2

        /** Constant indicating that the test proceeds directly to the next trial without user choice. */
        @JvmStatic val TEST_NEXTTRIAL_NOCHOOSE          = -1
        /** Constant indicating that the user can only choose to abort/pause the test after each trial end */
        @JvmStatic val TEST_NEXTTRIAL_AUTO              = 0
        /** Constant indicating that the user can choose to wait and then press a 'NEXT' button. */
        @JvmStatic val TEST_NEXTTRIAL_BUTTON            = 1
        /** Constant indicating that the test waits for an answer dialog. */
        @JvmStatic val TEST_NEXTTRIAL_ANSWER            = 2
        /** Constant indicating that the test waits for a voice answer dialog via speech recognition. */
        @JvmStatic val TEST_NEXTTRIAL_VOICE_ANSWER      = 3
        /** Constant indicating that the test waits for either a normal answer dialog or a voice answer. */
        @JvmStatic val TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER = 4

        /** Constant indicating that a test feature/switch is disabled and cannot be enabled. */
        @JvmStatic val TEST_SWITCH_DISABLED             = 0
        /** Constant indicating that a test feature/switch can be chosen by the user and is off by default. */
        @JvmStatic val TEST_SWITCH_CHOOSE_OFF           = 1
        /** Constant indicating that a test feature/switch can be chosen by the user and is on by default. */
        @JvmStatic val TEST_SWITCH_CHOOSE_ON            = 2
        /** Constant indicating that a test feature/switch is enabled and cannot be disabled. */
        @JvmStatic val TEST_SWITCH_ENABLED              = 3

        /** Constant indicating that trials are predetermined at the start of the test. */
        @JvmStatic val TEST_TRMAN_FIXED                 = 0
        /** Constant indicating that the user can choose, with predetermined trials as the default. */
        @JvmStatic val TEST_TRMAN_CHOOSE_FIXED          = 1
        /** Constant indicating that the user can choose, with mixed (fixed and adaptive) trials as the default. */
        @JvmStatic val TEST_TRMAN_CHOOSE_MIXED          = 2
        /** Constant indicating that the user can choose, with adaptive (e.g., Quest) trials as the default. */
        @JvmStatic val TEST_TRMAN_CHOOSE_ADAPTIVE       = 3
        /** Constant indicating that some trials are adaptive (e.g., Quest) and others are predetermined. */
        @JvmStatic val TEST_TRMAN_MIXED                 = 4
        /** Constant indicating that all trials are adaptive (e.g., Quest). */
        @JvmStatic val TEST_TRMAN_ADAPTIVE              = 5


        // endregion
        //-----------------------------------------------------------------------------------------

        //-----------------------------------------------------------------------------------------
        // region ---- peri-trials EVENTS ----
        //-----------------------------------------------------------------------------------------
        /** Event code indicating that test setup has completed. */
        @JvmStatic val EVENT_TEST_SETUP_COMPLETED       = 200

        /** Event code indicating the start of stimuli presentation (currently unused). */
        @JvmStatic val EVENT_STIMULI_START              = 201   // unused
        /** Event code indicating the end of stimuli presentation (currently unused). */
        @JvmStatic val EVENT_STIMULI_END                = 202   // unused
        /** Event code indicating that an answer should be given (e.g., show answer dialog). */
        @JvmStatic val EVENT_GIVE_ANSWER                = 203
        /** Event code indicating that a vocal answer should be given. */
        @JvmStatic val EVENT_GIVE_VOCAL_ANSWER          = 204
        /** Event code indicating that an answer has been given. */
        @JvmStatic val EVENT_ANSWER_GIVEN               = 205
        /** Event code indicating that the current trial should be repeated. */
        @JvmStatic val EVENT_TRIAL_REPEAT               = 206
        /** Event code indicating that the current trial has been aborted. */
        @JvmStatic val EVENT_TRIAL_ABORT                = 207
        /** Event code indicating that the entire test has ended. */
        @JvmStatic val EVENT_TEST_END                   = -100
        /** Event code indicating that a block of trials has ended. */
        @JvmStatic val EVENT_BLOCK_END                  = -101
        /** Event code indicating an error occurred during the test. */
        @JvmStatic val EVENT_TEST_ERROR                 = -102
        /** Event code indicating that the 'NEXT' button should be shown. */
        @JvmStatic val EVENT_SHOW_NEXT_ABORT            = 209
        /** Event code indicating that the abort button should be shown. */
        @JvmStatic val EVENT_SHOW_PAUSE_ABORT           = 210
        /** Event code indicating that miscellaneous information should be shown. */
        @JvmStatic val EVENT_SHOW_INFO                  = 211
        /** Event code indicating that debug information should be shown. */
        @JvmStatic val EVENT_SHOW_DEBUGINFO             = 211
        /** Event code indicating that navigation should go back (e.g., from TestFragment to menu). */
        @JvmStatic val EVENT_TEST_COMPLETED              = 213
        /** Event code indicating that the next trial has started after the previous one ended. */
        @JvmStatic val EVENT_TRIAL_STARTED              = 214
        /** Event code indicating that the next trial has started after the previous one ended. */
        @JvmStatic val EVENT_TRAINING_END             = 215
        // endregion
        //-----------------------------------------------------------------------------------------

        // --------------------------------------------------------------------------------------------
        // region ---- end of trial management ----
        //-----------------------------------------------------------------------------------------
        /** Code indicating the test was aborted but results should be kept. */
        @JvmStatic val TEST_ABORTED_KEEP_RESULT         = 1000
        /** Code indicating the test was aborted and results should be deleted. */
        @JvmStatic val TEST_ABORTED_DEL_RESULT          = 1001
        /** Code indicating the test completed successfully. */
        @JvmStatic val TEST_COMPLETED                   = 1002
        /** Code indicating a block of trials completed successfully. */
        @JvmStatic val BLOCK_COMPLETED                  = 1003
        /** Code indicating the test was aborted due to an error. */
        @JvmStatic val TEST_ABORTED_WITH_ERROR          = 1004

        // endregion
        //-----------------------------------------------------------------------------------------

        //-----------------------------------------------------------------------------------------
        // region ---- TEST COMMON CONSTANTS ---
        // Email configuration
        /** Default email recipients for sending test results or feedback. */
        @JvmStatic val DEFAULT_EMAIL_RECIPIENTS = arrayOf("psysuite@gmail.com")

        /** Standard header for log files. */
        @JvmStatic val LOG_HEADER = "id\ttype\terror\tsuccess\telapsed\n"
        /** Default label for tests, intended to be overridden by subclasses. */
        @JvmStatic val TEST_BASIC_LABEL = "to-be-overridden"


        // Common stimulus types
        /** String identifier for visual stimulus type. */
        @JvmStatic val STIMULUS_TYPE_VISUAL             = "VISUAL"
        /** String identifier for audio stimulus type. */
        @JvmStatic val STIMULUS_TYPE_AUDIO              = "AUDIO"
        /** String identifier for tactile stimulus type. */
        @JvmStatic val STIMULUS_TYPE_TACTILE            = "TACTILE"
        /** String identifier for audio-tactile stimulus type. */
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE      = "AUDIO_TACTILE"
        /** String identifier for audio-visual stimulus type. */
        @JvmStatic val STIMULUS_TYPE_AUDIO_VISUAL       = "AUDIO_VIDEO" // Note: "AUDIO_VIDEO" might be a typo for "AUDIO_VISUAL"
        /** String identifier for visual-tactile stimulus type. */
        @JvmStatic val STIMULUS_TYPE_VISUAL_TACTILE     = "VIDEO_TACTILE" // Note: "VIDEO_TACTILE" might be a typo for "VISUAL_TACTILE"

        /** Log representation for audio stimulus. */
        @JvmStatic val STIMULUS_TYPE_AUDIO_LOG          = "A"
        /** Log representation for tactile stimulus. */
        @JvmStatic val STIMULUS_TYPE_TACTILE_LOG        = "T"
        /** Log representation for visual stimulus. */
        @JvmStatic val STIMULUS_TYPE_VISUAL_LOG         = "V"
        /** Log representation for audio-tactile stimulus. */
        @JvmStatic val STIMULUS_TYPE_AUDIO_TACTILE_LOG  = "AT"
        /** Log representation for tactile-audio stimulus. */
        @JvmStatic val STIMULUS_TYPE_TACTILE_AUDIO_LOG  = "TA"
        /** Log representation for audio-visual stimulus. */
        @JvmStatic val STIMULUS_TYPE_AUDIO_VISUAL_LOG   = "AV"
        /** Log representation for visual-audio stimulus. */
        @JvmStatic val STIMULUS_TYPE_VISUAL_AUDIO_LOG   = "VA"
        /** Log representation for visual-tactile stimulus. */
        @JvmStatic val STIMULUS_TYPE_VISUAL_TACTILE_LOG = "VT"
        /** Log representation for tactile-visual stimulus. */
        @JvmStatic val STIMULUS_TYPE_TACTILE_VISUAL_LOG = "TV"


        // Common condition types
        /** Identifier for sub-threshold Inter-Stimulus Interval (ISI) condition. */
        @JvmStatic val STIMULUS_ISI_SUB        = "SUB"
        /** Identifier for supra-threshold Inter-Stimulus Interval (ISI) condition. */
        @JvmStatic val STIMULUS_ISI_SUPRA      = "SUPRA"
        /** Identifier for mixed (sub & supra-threshold) Inter-Stimulus Interval (ISI) condition. */
        @JvmStatic val STIMULUS_ISI_SUBSUPRA      = "SUBSUPRA"

        /** Identifier for no conflict type in stimulus presentation. */
        @JvmStatic val CONFLICT_TYPE_NONE   = "none"

        // time scale for stimulus presentation
        /** Task involving sub-seconds timings. */
        @JvmStatic val TIME_SUB         = 1
        /** Task involving supra-seconds timings. */
        @JvmStatic val TIME_SUPRA       = 2
        /** Task involving sub and supra-seconds timings. */
        @JvmStatic val TIME_SUBSUPRA    = 3

        // Common durations
        /** Default duration for visual stimuli in milliseconds. */
        @JvmStatic val STIMULUS_DURATION_VISUAL     = 75L
        /** Default duration for tactile stimuli in milliseconds. */
        @JvmStatic val STIMULUS_DURATION_TACTILE    = 50L
        /** Default duration for audio stimuli in milliseconds. */
        @JvmStatic val STIMULUS_DURATION_AUDIO      = 50L

        /** Default Inter-Stimulus Interval (ISI) in milliseconds. */
        @JvmStatic val DEFAULT_ISI          = 1000L
        /** Default delay before a question is presented, in milliseconds. */
        @JvmStatic val QUESTION_DELAY       = 500L
        /** Default delay before the first stimulus is presented, in milliseconds. */
        @JvmStatic val FIRST_STIMULUS_DELAY = 500L

        // Common trial configurations
        /** Default duration for white noise in milliseconds. */
        @JvmStatic val DEFAULT_WHITE_NOISE_DURATION             = 1000L
        /** Default interval between the start of white noise and the first stimulus, in milliseconds. */
        @JvmStatic val DEFAULT_WHITE_NOISE_FIRST_STIM_INTERVAL  = 1000L
        // endregion
        //-----------------------------------------------------------------------------------------
    }

    // ===============================================================================================================
    // PUBLIC
    // ===============================================================================================================
    /** The total number of trials in the current test configuration. */
    open val nTrials:Int     get() = mTrialsManager.nTrials
    /** The index of the current trial (0-based). */
    val currTrialID:Int   get() = mTrialsManager.currTrialID

    /**
     * A [com.jakewharton.rxrelay2.PublishRelay] used to emit events from the test to observers (typically the UI).
     * The Triple contains:
     * - `Int`: The event code (e.g., [EVENT_TEST_END], [EVENT_GIVE_ANSWER]).
     * - `Any?`: Optional event data.
     * - `List<String>`: Optional list of strings, often file paths.
     */
    val testEvent: PublishRelay<Triple<Int, Any?, List<String>>> = PublishRelay.create()

    /** The label for the current test, often derived from subject type or conditions. */
    var mTestLabel: String                      = ""
    /** The question presented to the user during the test (e.g., for an answer dialog). */
    var mQuestion:String                        = ""
    /** A list of valid answers the user can provide. */
    var validAnswers: MutableList<String>       = mutableListOf()

    // Stimulus type constants that can be overridden by subclasses
    /** Protected value representing the primary audio stimulus type for this test. Defaults to [org.albaspazio.psysuite.stimuli.StimuliManager.Companion.STIM_TYPE_A4]. */
    protected open val STIM_A: Int      = StimuliManager.Companion.STIM_TYPE_A4
    /** Protected value representing the primary visual stimulus type for this test. Defaults to [StimuliManager.Companion.STIM_TYPE_V1]. */
    protected open val STIM_V: Int      = StimuliManager.Companion.STIM_TYPE_V1
    /** Protected value representing the primary tactile stimulus type for this test. Defaults to [StimuliManager.Companion.STIM_TYPE_T1]. */
    protected open val STIM_T: Int      = StimuliManager.Companion.STIM_TYPE_T1
    /** Combined stimulus type for Audio, Tactile, and Visual. */
    protected open val STIM_ATV: Int    = STIM_A or STIM_T or STIM_V
    /** Combined stimulus type for Tactile and Visual. */
    protected open val STIM_TV:Int      = STIM_T or STIM_V
    /** Combined stimulus type for Audio and Visual. */
    protected open val STIM_AV:Int      = STIM_A or STIM_V
    /** Combined stimulus type for Audio and Tactile. */
    protected open val STIM_AT:Int      = STIM_A or STIM_T

    // ===============================================================================================================
    // PUBLIC
    // ===============================================================================================================
    /**
     * Abstract method to initialize the test.
     * Subclasses must implement this to set up test-specific parameters, trials, stimuli, etc.
     * This method is typically called by the hosting Fragment after the TestBasic instance is created.
     */
    abstract fun initTest()

    /**
     * Starts the test execution.
     * This method checks if the [StimuliManager] and [org.albaspazio.psysuite.trials.TrialsManager] are valid and initialized.
     * If in debug mode, it emits debug information. Then, it calls [show] to present the first trial.
     *
     * @return `true` if the test started successfully, `false` if there was a critical error.
     */
    fun start():Boolean{
        return  try {
            if(!mStimuliManager.isValid || !this::mTrialsManager.isInitialized){
                onCriticalError(ctx.resources.getString(R.string.test_failure), true)
                return false
            }

            doNextTrial()   // TrialManager.currTrialID is set to -1, here goes to 0
                            // internally the valid list is already set
            true
        }
        catch(e:Exception){
            e.logLastTwo(LOG_TAG)
            onCriticalError(e.toString())
            false
        }
    }

    fun startTest(){
        mTrialsManager.setTest()
        doNextTrial()
    }

    fun startTraining(){
        mTrialsManager.setTraining()
        doNextTrial()
    }

    /**
     * Repeats the current trial.
     * Calls the [show] method with the current trial and the `isRepeat` flag set to true.
     */
    fun repeatTrial(){
        show(mTrial, true)
    }

    /**
     * Handles the event when an answer is given by the user.
     * If a valid result or extra text is provided, it sets the response in the [org.albaspazio.psysuite.trials.TrialsManager]
     * and adds the current trial data to the [org.albaspazio.psysuite.model.summary.Summary].
     *
     * @param result The numerical result of the answer (e.g., button index). Defaults to -1 (no answer).
     * @param elapsed The time elapsed for the answer in milliseconds. Defaults to -1.
     * @param extra_text Any additional text associated with the answer. Defaults to an empty string.
     */
    open fun setAnswer(result: Int = -1, elapsed: Long = -1L, extra_text: String = ""){

        if(mTrial.isTraining)   return

        if (result != -1 || extra_text.isNotEmpty()){
            mTrialsManager.setResponse(result, elapsed, extra_text)
            mSummary?.add(mTrial)
        }
        saveText(mTrial.Log())
    }

    /**
     * Called by:
     * - btNext/btPause/btAbort (in case user changes his idea).setOnClickListener
     * - TestFragment.onAnswerGiven
     * - showShortAbort (with remove > 0)
     * Do:
     * - logs the current trial's data.
     * - then checks if: the test has ended (all trials completed)  => it calls [terminateTest] and emits [EVENT_TEST_END].
     *                   a block ends                               => it emits [EVENT_BLOCK_END].
     *               else, If the test continues,                   => emits an [EVENT_TRIAL_STARTED] and calls [doNextTrial].
     */
    open fun onNextTrial() {

        when {
            mTrialsManager.isLastTrainingTrial ->
                testEvent.accept(Triple(EVENT_TRAINING_END, null, listOf()))

            mListBlocks.contains(currTrialID) ->
                testEvent.accept(Triple(EVENT_BLOCK_END, null, listOf()))

            currTrialID == (nTrials - 1) -> {
                terminateTest(TEST_COMPLETED)
                testEvent.accept(Triple(EVENT_TEST_END, null, listOf()))            // END !
            }

            else -> {
                testEvent.accept(Triple(EVENT_TRIAL_STARTED, null, listOf()))
                doNextTrial()
            }
        }
    }

    /**
     * called by:
     * - TestFragment.onAbortTest
     * - TestFragment.onBlockEnded
     * - this.onCriticalError
     * Terminates the current test with a given completion code.
     * This method handles cleaning up resources, closing summary files,
     * and notifying the system about result files. Depending on the `code`,
     * it may keep or delete result files. Finally, it emits an [EVENT_TEST_COMPLETED]
     * event to signal the UI to navigate away from the test screen.
     *
     * @param code The termination code, e.g., [TEST_COMPLETED], [TEST_ABORTED_KEEP_RESULT], [BLOCK_COMPLETED].
     */
    fun terminateTest(code:Int){

        var filesToReturn = listOf( absoluteResultFilePath,
                                    subject.absoluteSubjectFilePath,
                                    closeSummary())
        unloadStimuli()

        // Cleanup adaptive trials manager if needed
        if (this::mTrialsManager.isInitialized && mTrialsManager is AdaptiveTrialsManager)  (mTrialsManager as AdaptiveTrialsManager).cleanup()

        when(code){

            TEST_COMPLETED -> {
                closeSummary()
                notifyFile(mResultFile, ctx, outResultsDir)
            }
            BLOCK_COMPLETED -> {
                val renamedfiles = stopTestAfterBlock()        // change output files names and notify them
                filesToReturn = listOf(renamedfiles.first, renamedfiles.second, renamedfiles.third)
                notifyFile(renamedfiles.first, ctx, outResultsDir)

            }
            TEST_ABORTED_KEEP_RESULT -> {
                closeSummary()
                notifyFile(mResultFile, ctx, outResultsDir)
            }
            TEST_ABORTED_DEL_RESULT -> {
                deleteFile(mResultFile)
                deleteFile(subject.subjectFileName)
                deleteFile(mSummaryFile)
                filesToReturn = listOf()
            }
        }
        testEvent.accept(Triple(EVENT_TEST_COMPLETED, code, filesToReturn))
    }

    /**
     * Called by:
     * - TestFragment.onBlockEnded
     * when a new block of trials is to be started (e.g., after user confirmation).
     * Increments the current block counter and proceeds to the next trial using [doNextTrial].
     */
    fun startNewBlock(){
        mCurrBlock++
        doNextTrial()
    }

    /**
     * Adjusts the current block and trial based on a given block number.
     * This is typically called during test setup if the test is being resumed from a specific block.
     *
     * @param blk The block number to adjust to. If -1, resets to the beginning (block 0, trial 0).
     *            Otherwise, sets the current block and advances the current trial to the start of that block.
     */
    fun adjustBlocks(blk:Int){

        if((this@TestBasic.nBlocks == 1 && blk > 0) || (blk >= this@TestBasic.nBlocks)){
            // incongruent condition
            showAlert(activity, ctx.resources.getString(R.string.error), "")
            return
        }
        if(blk == -1){
            mTrialsManager.currTrialID   = -1
            mCurrBlock  = 0
        }
        else {  // if it found lab_type_blk2.txt => blk=3)
            mCurrBlock = blk

            // following trial of the previous block
            mTrialsManager.currTrialID = mListBlocks[mCurrBlock-1] + 1
        }
    }

    /**
     * Retrieves the correct answer for the current trial.
     *
     * @return The correct answer code for the current trial, or 0 if the TrialsManager is not initialized.
     */
    open fun getTrialCorrectAnswer():Int{
        return  if(!this::mTrialsManager.isInitialized) 0
                else                                    mTrial.correct_answer
    }

    // ===============================================================================================================
    // PROTECTED
    // ===============================================================================================================
    /**
     * Manages the sequence of trials for the test.
     * This must be initialized by subclasses in their `initTest` method.
     */
    protected lateinit var mTrialsManager: TrialsManager

    /** Provides access to the current [TrialBasic] object from the [mTrialsManager]. */
    val mTrial: TrialBasic get() = mTrialsManager.mTrial

    /**
     * Abstract method to show the stimuli for a given trial.
     * Subclasses must implement this to define how stimuli are presented (e.g., visual, audio, tactile).
     *
     * @param trial The [TrialBasic] object containing the parameters for the current trial.
     * @param isRepeat `true` if the trial is being repeated, `false` otherwise.
     */
    protected abstract fun show(trial: TrialBasic, isRepeat:Boolean=false)

    /**
     * Abstract method to initialize the summary object ([mSummary]).
     * Subclasses must implement this to set up the structure and content of the test summary.
     */
    protected abstract fun initSummary()

    /**
     * A list of trial indices that mark the end of a block.
     * Setting this list also updates [nBlocks].
     */
    protected var mListBlocks:MutableList<Int>  = mutableListOf()
        set(value) {
            field   = value
            this@TestBasic.nBlocks = value.size + 1
        }

    /** Optional [android.media.MediaPlayer] instance for playing background noise if required by the test. */
    protected var mNoise: MediaPlayer? = null

    /**
     * List of drawable resource IDs used by the test.
     * Subclasses can override or populate this list with specific drawables needed for visual stimuli.
     */
    protected open var mDrawablesResource:MutableList<Int>  = mutableListOf()

    /** Optional [org.albaspazio.psysuite.model.summary.Summary] object for collecting and storing summary data for the test. */
    protected var mSummary: Summary?                        = null

    /**
     * Manages the presentation of stimuli (audio, visual, tactile).
     * This must be initialized by subclasses, typically in their `initTest` method.
     * If initialization fails (e.g., audio resources can't be loaded), an exception should be thrown.
     */
    protected lateinit var mStimuliManager: StimuliManager
    /** Handler for posting delayed actions related to stimuli, running on the main looper. */
    protected var mStimuliHandler: Handler = Handler(Looper.getMainLooper())

    /** Label for the current stimulus being presented. */
    protected var currStimulusLabel:String      = ""
    /** Default duration for the current stimulus if not otherwise specified, in milliseconds. */
    protected var currStimulusDuration:Long     = 100L
    /** Default audio resource name if not otherwise specified. */
    protected var currAudioResourceName:String  = "t200hz_2s"

    /** Default Inter-Trial Interval (ITI) in milliseconds. */
    protected var ITI:Long                      = 0

    /**
     * Abstract method called when last trial stimulus is given.
     * code must solve whether setting up a response mechanism, show next button or move to next trial
     * Subclasses must implement this to handle trial completion logic, such as
     * stopping stimuli, preparing for the next trial, or triggering events.
     */
    protected open fun onStimuliEnd(){

        mNoise?.stop()
        mNoise?.prepare()

        // wait for 500 ms and then decide what to do
        mStimuliHandler.postDelayed({
            when (subject.nextTrailModality) {
                TEST_NEXTTRIAL_BUTTON               ->  testEvent.accept(Triple(EVENT_SHOW_NEXT_ABORT, null, listOf()))
                TEST_NEXTTRIAL_NOCHOOSE             ->  testEvent.accept(Triple(EVENT_SHOW_PAUSE_ABORT, 0L, listOf()))
                TEST_NEXTTRIAL_AUTO                 ->  testEvent.accept(Triple(EVENT_SHOW_PAUSE_ABORT, 1000L, listOf()))

                TEST_NEXTTRIAL_VOICE_ANSWER         ->  testEvent.accept(Triple(EVENT_GIVE_VOCAL_ANSWER, null, listOf()))
                TEST_NEXTTRIAL_ANSWER               ->  testEvent.accept(Triple(EVENT_GIVE_ANSWER, null, listOf()))
                TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER  -> {
                                                        testEvent.accept(Triple(EVENT_GIVE_VOCAL_ANSWER, null, listOf()))
                                                        testEvent.accept(Triple(EVENT_GIVE_ANSWER, null, listOf())) }
            }
        }, 500L)
    }
    /**
     * Proceeds to the next trial by fetching it from [mTrialsManager] and then displaying it using [show].
     * If in debug mode, it emits debug information.
     *
     * @return The index of the current trial, or [EVENT_TEST_ERROR] if an exception occurs.
     */
    protected fun doNextTrial():Int{
        return  try {
            show(mTrialsManager.getNewTrial())
            if(subject.isDebug) testEvent.accept(Triple(EVENT_SHOW_DEBUGINFO, mTrial.debugInfo(), listOf()))    // send debug info
            currTrialID
        }
        catch(e:Exception){
            e.logLastTwo(LOG_TAG)
            onCriticalError(e.toString())
            EVENT_TEST_ERROR
        }
    }

    /**
     * Saves text content to the result file.
     * Handles differences in file saving mechanisms for Android Q (API 29) and above.
     *
     * @param text The text to save.
     * @param overwrite If `true`, overwrites the existing file content. Otherwise, appends. Defaults to `false`.
     * @param notifyDm If `true`, notifies the Download Manager about the saved file. Defaults to `false`.
     * @return The result of the save operation, specific to the underlying file saving function.
     */
    protected fun saveText(text: String, overwrite: Boolean = false, notifyDm: Boolean = false): Any {
        return  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            saveTextQ(
                ctx,
                mResultUri!!,
                text,
                overwrite = overwrite,
                notifyDm = notifyDm,
                dir = "${Environment.DIRECTORY_DOWNLOADS}/${FileSystemManager.Companion.RESULTS_FOLDER_NAME}"
            )
        else
            org.albaspazio.core.filesystem.saveText(
                ctx,
                mResultFile,
                text,
                overwrite = overwrite,
                notifyDm = notifyDm,
                dir = "${Environment.DIRECTORY_DOWNLOADS}/${FileSystemManager.Companion.RESULTS_FOLDER_NAME}"
            )
    }

    /**
     * Creates the result file and writes the initial header to it.
     * The file is always created without block information in its name; this is added later
     * if the test is interrupted after a block.
     *
     * @param header The header string to write to the result file.
     */
    protected fun createResultFile(header:String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mResultUri = saveTextQ(
                ctx,
                mResultFile,
                header,
                dir = "${Environment.DIRECTORY_DOWNLOADS}/${FileSystemManager.Companion.RESULTS_FOLDER_NAME}"
            )
        else
            org.albaspazio.core.filesystem.saveText(
                ctx,
                mResultFile,
                header,
                dir = "${Environment.DIRECTORY_DOWNLOADS}/${FileSystemManager.Companion.RESULTS_FOLDER_NAME}"
            )
    }

    /**
     * Creates a list of integer equally spaced from start to end.
     *
     * @param start first element of the list.
     * @param end last element of the list.
     * @param count number of items.
     */
    protected fun createEquallySpacedInts(start: Int, end: Int, count: Int): List<Int> {
        val startF  = start.toFloat()
        val endF    = end.toFloat()
        val step    = (endF - startF) / (count - 1)

        return (0 until count).map { i ->
            (startF + i * step).toInt()
        }
    }
    // ===============================================================================================================
    // region PRIVATE
    // ===============================================================================================================
    private var nBlocks:Int     = 0
    private var mCurrBlock: Int = 0
    private var mResultFile: String                         = subject.composeResultFileName(ctx)
    private var mSummaryFile: String                        = subject.composeSummaryFileName(ctx)

    private var mResultUri: Uri?                            = null

    private fun closeSummary(filename:String = ""):String{
        return  if(filename.isEmpty())  mSummary?.close(mSummaryFile) ?: ""
        else                            mSummary?.close(filename) ?: ""
    }

    private fun stopTestAfterBlock():Triple<String,String,String>{

        val newresname = subject.composeResultFileName(ctx, mCurrBlock)
        renameFile(mResultFile, newresname)

        val newsubjname = subject.composeSubjectFileName(ctx, mCurrBlock)
        renameFile(subject.subjectFileName, newsubjname)

        val newsummaryname = subject.composeSummaryFileName(ctx, mCurrBlock)

        return Triple(newresname, newsubjname, newsummaryname)
    }

    private fun unloadStimuli(){
        mStimuliManager.unloadStimuli()
        mStimuliHandler.removeCallbacksAndMessages(null)
        mNoise?.stop()
    }

    private val absoluteResultFilePath: String
        get() = getAbsoluteFilePath(mResultFile, outResultsDir).second

    private fun onCriticalError(msg:String, delete:Boolean=false){
        if(delete)  terminateTest(TEST_ABORTED_DEL_RESULT)
        else        terminateTest(TEST_ABORTED_KEEP_RESULT)
        testEvent.accept(Triple(EVENT_TEST_ERROR, msg, listOf(  absoluteResultFilePath, subject.absoluteSubjectFilePath, closeSummary())))
    }

    // endregion
}