package iit.uvip.psysuite.core.tests.tid

import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectLongitParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.existFileStartingWith
import org.albaspazio.core.accessory.getDateString
import org.albaspazio.core.accessory.getFullDateString

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
    var group: Int = -1
//    var interval_type: Int = -1
//    var first_modality: Int = -1

) : SubjectLongitParcel(type, label, age, gender, nextTrailModality, canRecordAudio, testClass, device, spinner_sel, spinner_data_resource){

    override fun composeSubjectFileName():String{
        if(label.isBlank() || group == -1 || type == -1 || session == -1)   return ""

        return "${label}_${group}_${session}_${type}_${getDateString()}${TestBasic.FILE_EXTENSION}"
    }


    override fun existSubjectFile():Boolean{
        return existFileStartingWith("${label}_${group}_${session}_${type}", allowedext = listOf(".json"))
    }

    override fun composeResultFileName():String{
        return "${label}_${group}_${session}_${type}_${getFullDateString()}${TestBasic.RES_EXTENSION}"
    }

}












