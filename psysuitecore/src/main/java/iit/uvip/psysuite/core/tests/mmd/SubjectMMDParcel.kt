package iit.uvip.psysuite.core.tests.mmd

import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device
import iit.uvip.psysuite.core.R

/**
 * Parcelable data class holding subject-specific configuration for the Motion-Defined Motion (MMD) test.
 * This class extends [SubjectBasicParcel] to include parameters relevant to the MMD test.
 *
 * @property classes List of fully qualified class names that this subject configuration applies to.
 *                   Defaults to a list containing only "iit.uvip.psysuite.core.tests.mmd.TestMMD".
 * @property label A descriptive label for the test session (e.g., subject ID, session number). Defaults to an empty string.
 * @property age The age of the subject in years. Defaults to -1 (unknown).
 * @property gender The gender of the subject. See [Populations] for gender constants. Defaults to -1 (unknown).
 * @property population The population group the subject belongs to (e.g., typically developing, specific clinical group).
 *                      See [Populations] for population constants. Defaults to [Populations.POPULATION_TD].
 * @property type The specific type or variant of the MMD test to be conducted. Defaults to -1 (unspecified).
 * @property block The current block number if the test is divided into blocks. Defaults to -1.
 * @property isDebug Flag indicating whether the test is running in debug mode. Defaults to false.
 * @property device Information about the device running the test. Defaults to null.
 * @property vercode The version code of the application. Defaults to -1.
 * @property stimuliDelays Configuration for aligning stimuli delays. Defaults to a new [DelaysAligner] instance.
 * @property nextTrailModality How the test proceeds to the next trial (e.g., button press, automatic).
 *                            See [TestBasic.TEST_NEXTTRIAL_BUTTON], etc. Defaults to [TestBasic.TEST_NEXTTRIAL_BUTTON].
 * @property whitenoise Configuration for playing white noise during the test.
 *                      See [TestBasic.TEST_SWITCH_DISABLED], etc. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @property trman_type The trial management type (e.g., fixed, adaptive).
 *                      See [TestBasic.TEST_TRMAN_FIXED], etc. Defaults to [TestBasic.TEST_TRMAN_FIXED].
 * @property showResult Configuration for showing results at the end of the test.
 *                       See [TestBasic.TEST_SWITCH_DISABLED], etc. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @property canRepeat Configuration for allowing trial repeats.
 *                     See [TestBasic.TEST_SWITCH_DISABLED], etc. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 * @property doTraining Configuration for including a training phase.
 *                      See [TestBasic.TEST_SWITCH_DISABLED], etc. Defaults to [TestBasic.TEST_SWITCH_DISABLED].
 */
@Parcelize
class SubjectMMDParcel(

    override var classes: List<String> = listOf("iit.uvip.psysuite.core.tests.mmd.TestMMD"),
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

    override var nextTrailModality: Int = TestBasic.TEST_NEXTTRIAL_BUTTON,
    override var whitenoise: Int = TestBasic.TEST_SWITCH_DISABLED,
    override var trman_type: Int = TestBasic.TEST_TRMAN_FIXED,
    override var showResult: Int = TestBasic.TEST_SWITCH_DISABLED,
    override var canRepeat:Int = TestBasic.TEST_SWITCH_DISABLED,
    override var doTraining: Int = TestBasic.TEST_SWITCH_DISABLED,
    
    override var showTrialID: Int = TestBasic.TEST_SHOWTRIALS_NEVER,
    override var abortMode: Int = TestBasic.TEST_ABORT_TRIALEND,

    override var session_spsel: Int = TestBasic.Companion.TEST_NO_LONGITUDINAL,
    override var session_spdatares: Int = R.array.sessions_array,
    override var date: String = "",
    override var expUniqueId: String = ""
) : SubjectBasicParcel(classes, label, age, gender, population, type, block, isDebug, device, vercode, stimuliDelays, nextTrailModality, whitenoise, trman_type, showResult, canRepeat, doTraining, showTrialID, abortMode, session_spsel, session_spdatares, date, expUniqueId)
