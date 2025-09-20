package iit.uvip.psysuite.core.model.parcel

import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import org.albaspazio.core.accessory.Device

/**
 * Abstract base class for subject configurations intended for longitudinal tests (tests conducted over multiple sessions).
 * This class extends [SubjectBasicListParcel] and adapts spinner-related properties to represent session information.
 * The `spinner_label` is hardcoded to "session" as it always refers to the test session number.
 *
 * @param classes List of class names, typically used for reflection or identification.
 * @param label A descriptive label for this subject configuration.
 * @param age The age of the subject. Defaults to -1 (unknown).
 * @param gender The gender of the subject. Defaults to -1 (unknown).
 * @param population The population group the subject belongs to. Defaults to [Populations.POPULATION_TD].
 * @param type An integer code representing the specific type of test or configuration. Defaults to -1.
 * @param block The current block number within a session. Defaults to -1.
 * @param isDebug Flag indicating if the test is running in debug mode. Defaults to `false`.
 * @param device Information about the device running the test. Defaults to `null`.
 * @param vercode Version code of the application or test suite. Defaults to -1.
 * @param stimuliDelays Configuration for aligning stimuli delays. Defaults to a new [DelaysAligner] instance.
 * @param nextTrailModality How the test proceeds to the next trial. Defaults to [TestBasic.TEST_NEXTTRIAL_NOCHOOSE].
 * @param whitenoise Configuration for white noise. Defaults to [TestBasic.TEST_SWITCH_CHOOSE_ON].
 * @param trman_type Trial management type. Defaults to [TestBasic.TEST_TRMAN_FIXED].
 * @param showResult Configuration for showing results. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @param canRepeat Configuration for allowing trial repetition. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @param doTraining Configuration for enabling a training phase. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @param spinner_sel The selected session number (0-indexed). Also accessible via the `session` property. Defaults to -1.
 * @param spinner_data_resource The resource ID for the array defining available test sessions. Also accessible via `test_sessions_array`. Defaults to -1.
 */
abstract class SubjectLongitParcel(

    override var classes: List<String> = listOf(),
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var population: Int = Populations.POPULATION_TD,
    override var type: Int = -1,

    override var block: Int = -1,
    override var isDebug: Boolean = false,
    override var device: Device? = null,
    override var vercode: Int = -1,
    override var stimuliDelays: DelaysAligner = DelaysAligner(),

    override var nextTrailModality: Int = TestBasic.TEST_NEXTTRIAL_NOCHOOSE,
    override var whitenoise: Int = TestBasic.TEST_SWITCH_CHOOSE_ON,
    override var trman_type: Int = TestBasic.TEST_TRMAN_FIXED,
    override var showResult: Int = TestBasic.TEST_SWITCH_DISABLED,
    override var canRepeat:Int = TestBasic.TEST_SWITCH_DISABLED,
    override var doTraining: Int = TestBasic.TEST_SWITCH_DISABLED,

    override var showTrialID: Int = TestBasic.TEST_SHOWTRIALS_ALWAYS,
    override var abortMode: Int = TestBasic.TEST_ABORT_TRIALEND,

    override var spinner_sel: Int = -1, // Represents the selected session
    override var spinner_data_resource: Int = -1 // Represents the array resource for sessions
) : SubjectBasicListParcel(classes, label, age, gender, population, type, block, isDebug, device, vercode, stimuliDelays, nextTrailModality, whitenoise, trman_type, showResult, canRepeat, doTraining, showTrialID, abortMode, spinner_sel, "session", spinner_data_resource)
{
    /**
     * The currently selected session number (0-indexed).
     * This is an alias for `spinner_sel`.
     */
    var session: Int
        get() = spinner_sel
        set(value) {
            spinner_sel = value
        }

    /**
     * The resource ID for the array that defines the available test sessions (e.g., a string array from resources).
     * This is an alias for `spinner_data_resource`.
     */
    var test_sessions_array: Int
        get() = spinner_data_resource
        set(value) {
            spinner_data_resource = value
        }
}












