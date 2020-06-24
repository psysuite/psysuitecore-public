package iit.uvip.psysuite.core.tests.tid

import iit.uvip.psysuite.core.common.subjects_parcel.SubjectLongitParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device

// session
@Parcelize
class SubjectTIDParcel(
    override var type: Int = -1,
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var nextTrailModality: Int = -1,
    override var canRecordAudio:Boolean = false,
    override var testClass:String = "",
    override var device: Device? = null,

    override var spinner_sel: Int = -1,
    override var spinner_data_resource: Int = -1,
    var modality: Int = -1,
    var interval_type: Int = -1,
    var first_modality: Int = -1
) : SubjectLongitParcel(type, label, age, gender, nextTrailModality, canRecordAudio, testClass, device, spinner_sel, spinner_data_resource)












