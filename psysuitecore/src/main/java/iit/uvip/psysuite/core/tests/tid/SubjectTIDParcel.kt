package iit.uvip.psysuite.core.tests.tid

import android.content.Context
import iit.uvip.psysuite.core.common.DelaysAligner
import iit.uvip.psysuite.core.common.SpinnerData
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.getLabelLog
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectLongitParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.accessory.getDateString

// session
@Parcelize
class SubjectTIDParcel(
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
    override var whitenoise: Int = TestBasic.TEST_WNOISE_DISABLED,
    override var vercode: Int = -1,
    override var showResult: Boolean = false,
    override var population: Int = TestBasic.POPULATION_TD,
    override var isDebug: Boolean = false,

    override var spinner_sel: Int = -1,
    override var spinner_data_resource: Int = -1,
    var group: Int = -1
) : SubjectLongitParcel(type, label, age, gender, nextTrailModality, canRecordAudio, classes, device, block, stimuliDelays, whitenoise, vercode, showResult, population, isDebug, spinner_sel, spinner_data_resource){

    override fun getFilesPrefix(ctx:Context):String{

        val ci          = getCompanionObjectMethod(classes[0], "getConditionsInfo")
        val type_label  = (ci.first?.call(ci.second, ctx) as List<SpinnerData>).getLabelLog(type)

        return "${label}_${population}_${group}_s${session}_$type_label"
    }

    override fun composeSubjectFileName(ctx: Context, blk:Int):String{
        if(label.isBlank() || group == -1 || type == -1 || session == -1)   return ""

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""

        return "${getFilesPrefix(ctx)}_${getDateString()}${blkstr}${TestBasic.FILE_EXTENSION}"
    }
}












