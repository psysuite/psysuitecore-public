package iit.uvip.psysuite.core.common.subjects_parcel

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
    override var testClass:String = "",
    override var device: Device? = null,

    open var spinner_sel: Int = -1,
    open var spinner_label: String = "",
    open var spinner_data_resource: Int = -1
) : SubjectBasicParcel(type, label, age, gender, nextTrailModality, canRecordAudio, testClass, device)












