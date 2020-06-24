package iit.uvip.psysuite.core.common.subjects_parcel

import kotlinx.android.parcel.Parcelize

// base class for all longitudinal tests
@Parcelize
open class SubjectLongitParcel(
    override var type: Int = -1,
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var nextTrailModality: Int = -1,
    override var canRecordAudio:Boolean = false,
    override var testClass:String = "",
    override var spinner_sel: Int = -1,
    override var spinner_data_resource: Int = -1
) : SubjectBasicListParcel(type, label, age, gender, nextTrailModality, canRecordAudio, testClass,
    spinner_sel,
    "session",
    spinner_data_resource
) {

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

    override fun loadSubject(): SubjectLongitParcel {
        return super.loadSubject() as SubjectLongitParcel
    }
}












