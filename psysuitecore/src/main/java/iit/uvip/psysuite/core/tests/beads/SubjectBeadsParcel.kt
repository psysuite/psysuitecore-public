package iit.uvip.psysuite.core.tests.beads

import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device

// session
@Parcelize
class SubjectBeadsParcel(

    override var classes: List<String> = listOf("iit.uvip.psysuite.core.tests.beads.TestBeads"),
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
) : SubjectBasicParcel(
    classes = classes,
    label = label,
    age = age,
    gender = gender,
    population = population,
    type = type,
    block = block,
    isDebug = isDebug,
    device = device,
    vercode = vercode,
    stimuliDelays = stimuliDelays,
    nextTrailModality = nextTrailModality,
    whitenoise = whitenoise,
    trman_type = trman_type,
    showResult = showResult,
    canRepeat = canRepeat,
    doTraining = doTraining,
    showTrialID = showTrialID,
    abortMode = abortMode,
    session_spsel = session_spsel,
    session_spdatares = session_spdatares,
    date = date,
    expUniqueId = expUniqueId
)






