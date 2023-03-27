package iit.uvip.psysuite.core.tests.bis

import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device

// session
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
    override var canRepeat:Int = TestBasic.TEST_SWITCH_CHOOSE_OFF
) : SubjectBasicParcel(classes, label, age, gender, population, type, block, isDebug, device, vercode, stimuliDelays, nextTrailModality, whitenoise, trman_type, showResult, canRepeat)












