package iit.uvip.psysuite.core.model.parcel

import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import org.albaspazio.core.accessory.Device

// base class for all longitudinal tests
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
    override var canRepeat:Int = TestBasic.TEST_SWITCH_CHOOSE_OFF,

    override var spinner_sel: Int = -1,
    override var spinner_data_resource: Int = -1
) : SubjectBasicListParcel(classes, label, age, gender, population, type, block, isDebug, device, vercode, stimuliDelays, nextTrailModality, whitenoise, trman_type, showResult, canRepeat, spinner_sel, "session", spinner_data_resource)
{
    var session: Int
        get() = spinner_sel
        set(value) {
            spinner_sel = value
        }

    var test_sessions_array: Int
        get() = spinner_data_resource
        set(value) {
            spinner_data_resource = value
        }
}












