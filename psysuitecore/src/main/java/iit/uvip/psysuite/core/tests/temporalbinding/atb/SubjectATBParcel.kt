package iit.uvip.psysuite.core.tests.temporalbinding.atb

import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.parcel.Parcelize


/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
 */

// base class for all tests
@Parcelize
open class SubjectATBParcel(
    override var type: Int = -1,
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var nextTrailModality: Int = -1,
    override var canRecordAudio:Boolean = false,
    override var testClass:String = "",
    var whitenoise: Boolean = true

) : SubjectBasicParcel(type, label, age, gender, nextTrailModality, canRecordAudio, testClass) {

    override fun loadSubject():SubjectATBParcel{
        return super.loadSubject() as SubjectATBParcel
    }
}












