package iit.uvip.psysuite.core.tests.temporalbinding

import iit.uvip.psysuite.core.common.StimuliDelay
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device


/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
 */

// base class for all tests
@Parcelize
open class SubjectBindingsParcel(
    override var type: Int = -1,
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var nextTrailModality: Int = -1,
    override var canRecordAudio:Boolean = false,
    override var testClass:String = "",
    override var device: Device? = null,
    override var block:Int = -1,
    override var stimuliDelay: StimuliDelay = StimuliDelay(),

    var whitenoise: Boolean = true

) : SubjectBasicParcel(type, label, age, gender, nextTrailModality, canRecordAudio, testClass, device, block, stimuliDelay)












