package iit.uvip.psysuite.core.model.parcel

import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic

import org.albaspazio.core.accessory.Device

/**
 * Abstract base class for subject configurations that involve a list or spinner-based selection for test conditions.
 * This class extends [SubjectBasicParcel] and adds properties to manage spinner-related data and selections.
 *
 * Subclasses are expected to implement logic to resolve specific test conditions based on the spinner selection
 * or other internal variables.
 *
 * @param classes List of class names, typically used for reflection or identification.
 * @param label A descriptive label for this subject configuration.
 * @param age The age of the subject. Defaults to -1 (unknown).
 * @param gender The gender of the subject. Defaults to -1 (unknown).
 * @param population The population group the subject belongs to (e.g., [Populations.POPULATION_TD]). Defaults to [Populations.POPULATION_TD].
 * @param type An integer code representing the specific type of test or configuration. Defaults to -1.
 * @param block The current block number in a series of tests. Defaults to -1.
 * @param isDebug Flag indicating if the test is running in debug mode. Defaults to `false`.
 * @param device Information about the device running the test. Defaults to `null`.
 * @param vercode Version code of the application or test suite. Defaults to -1.
 * @param stimuliDelays Configuration for aligning stimuli delays. Defaults to a new [DelaysAligner] instance.
 * @param nextTrailModality How the test proceeds to the next trial (e.g., [TestBasic.TEST_NEXTTRIAL_NOCHOOSE]). Defaults to [TestBasic.TEST_NEXTTRIAL_NOCHOOSE].
 * @param whitenoise Configuration for white noise during the test (e.g., [TestBasic.TEST_SWITCH_CHOOSE_ON]). Defaults to [TestBasic.TEST_SWITCH_CHOOSE_ON].
 * @param trman_type Trial management type (e.g., [TestBasic.TEST_TRMAN_FIXED]). Defaults to [TestBasic.TEST_TRMAN_FIXED].
 * @param showResult Configuration for showing results after a trial/test. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @param canRepeat Configuration for allowing trial repetition. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @param doTraining Configuration for enabling a training phase. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @property spinner_sel The currently selected item's index in the spinner. Defaults to -1.
 * @property spinner_label The label associated with the current spinner selection. Defaults to an empty string.
 * @property spinner_data_resource The resource ID for the data populating the spinner (e.g., a string array). Defaults to -1.
 */
abstract class SubjectBasicListParcel(

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

    open var spinner_sel: Int = -1,
    open var spinner_label: String = "",
    open var spinner_data_resource: Int = -1
) : SubjectBasicParcel(classes, label, age, gender, population, type, block, isDebug, device, vercode, stimuliDelays, nextTrailModality, whitenoise, trman_type, showResult, canRepeat, doTraining)












