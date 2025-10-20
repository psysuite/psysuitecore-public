package iit.uvip.psysuite.core.tests.bis

import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device

/**
 * Parcelable data class representing the configuration for a Bisection (BIS) test session.
 *
 * This class extends [SubjectBasicParcel] and holds all the necessary parameters
 * to define a specific BIS test instance, including subject details, test conditions,
 * and hardware/software configurations.
 *
 * @property classes List of fully qualified names of the test classes to be used. For BIS, this typically defaults to ["iit.uvip.psysuite.core.tests.bis.TestBIS"].
 * @property label A descriptive label for the test session (e.g., subject ID, specific condition).
 * @property age The age of the participant.
 * @property gender The gender of the participant (coded as an Int).
 * @property population The population group the participant belongs to (e.g., typically developing, specific clinical group). See [Populations].
 * @property type The specific type or variant of the BIS test to be run (e.g., auditory, visual, bimodal).
 * @property block The current block number if the test is divided into multiple blocks.
 * @property isDebug If `true`, the test will run in debug mode, potentially with more logging or specific debug functionalities.
 * @property device Information about the device running the test. See [Device].
 * @property vercode The version code of the application.
 * @property stimuliDelays Configuration for aligning stimuli delays. See [DelaysAligner].
 * @property nextTrailModality Defines how the next trial is initiated (e.g., after user answer, timed). See [TestBasic.TEST_NEXTTRIAL_ANSWER].
 * @property whitenoise Configuration for using white noise during the test (e.g., on, off, user-configurable). See [TestBasic.TEST_SWITCH_CHOOSE_ON].
 * @property trman_type The type of trials manager to use (e.g., fixed, adaptive). See [TestBasic.TEST_TRMAN_CHOOSE_FIXED].
 * @property showResult Defines whether to show the result of each trial to the participant. See [TestBasic.TEST_SWITCH_CHOOSE_OFF].
 * @property canRepeat Defines whether the participant can request a repetition of a trial. See [TestBasic.TEST_SWITCH_CHOOSE_OFF].
 * @property doTraining Defines whether a training phase should be run before the main test. See [TestBasic.TEST_SWITCH_CHOOSE_OFF].
 */
@Parcelize
class SubjectBISParcel(

    override var classes: List<String> = listOf("iit.uvip.psysuite.core.tests.bis.TestBIS"),
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

    override var nextTrailModality: Int = TestBasic.TEST_NEXTTRIAL_ANSWER,
    override var whitenoise: Int = TestBasic.TEST_SWITCH_CHOOSE_ON,
    override var trman_type: Int = TestBasic.TEST_TRMAN_CHOOSE_FIXED,
    override var showResult: Int = TestBasic.TEST_SWITCH_CHOOSE_OFF,
    override var canRepeat:Int = TestBasic.TEST_SWITCH_CHOOSE_OFF,
    override var doTraining: Int = TestBasic.TEST_SWITCH_CHOOSE_OFF,
    
    override var showTrialID: Int = TestBasic.TEST_SHOWTRIALS_NEVER,
    override var abortMode: Int = TestBasic.TEST_ABORT_TRIALEND,

    override var session_spsel: Int = TestBasic.Companion.TEST_LONGITUDINAL_TOBESELECTED,
    override var session_spdatares: Int = R.array.sessions_array,
    override var date: String = "",
    override var expUniqueId: String = ""
) : SubjectBasicParcel(classes, label, age, gender, population, type, block, isDebug, device, vercode, stimuliDelays, nextTrailModality, whitenoise, trman_type, showResult, canRepeat, doTraining, showTrialID, abortMode, session_spsel, session_spdatares, date, expUniqueId)






