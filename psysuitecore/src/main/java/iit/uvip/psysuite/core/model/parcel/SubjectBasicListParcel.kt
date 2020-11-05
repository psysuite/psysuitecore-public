package iit.uvip.psysuite.core.model.parcel

import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device


/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
 */

// base class for all tests
@Parcelize
open class SubjectBasicListParcel(
    override var type: Int = -1,
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var nextTrailModality: Int = -1,
    override var canRecordAudio:Boolean = false,
    override var classes:List<String> = listOf(),
    override var device: Device? = null,
    override var block:Int = -1,
    override var stimuliDelays: DelaysAligner = DelaysAligner(),
    override var whitenoise: Int = TestBasic.TEST_WNOISE_CHOOSE_ON,
    override var vercode: Int = -1,
    override var showResult: Boolean = false,
    override var population: Int = Populations.POPULATION_TD,
    override var isDebug: Boolean = false,

    open var spinner_sel: Int = -1,
    open var spinner_label: String = "",
    open var spinner_data_resource: Int = -1
) : SubjectBasicParcel(type, label, age, gender, nextTrailModality, canRecordAudio, classes, device, block, stimuliDelays, whitenoise, vercode, showResult, population, isDebug)












