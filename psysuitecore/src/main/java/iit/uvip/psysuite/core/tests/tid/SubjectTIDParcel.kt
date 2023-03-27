package iit.uvip.psysuite.core.tests.tid

import android.content.Context
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectLongitParcel
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.getLabelLog
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.accessory.getDateString

// session
@Parcelize
class SubjectTIDParcel(

    override var classes: List<String> = listOf("iit.uvip.psysuite.core.tests.tid.TestTID"),
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
    override var whitenoise: Int = TestBasic.TEST_SWITCH_CHOOSE_OFF,
    override var trman_type: Int = TestBasic.TEST_TRMAN_CHOOSE_FIXED,
    override var showResult: Int = TestBasic.TEST_SWITCH_CHOOSE_OFF,
    override var canRepeat:Int = TestBasic.TEST_SWITCH_DISABLED,

    override var spinner_sel: Int = -1,
    override var spinner_data_resource: Int = -1,
    var group: Int = -1
) : SubjectLongitParcel(classes, label, age, gender, population, type, block, isDebug, device, vercode, stimuliDelays, nextTrailModality, whitenoise, trman_type, showResult, canRepeat, spinner_sel, spinner_data_resource){

    override fun getFilesPrefix(ctx:Context):String{

        val ci          = getCompanionObjectMethod(classes[0], "getConditionsInfo")
        val type_label  = (ci.first?.call(ci.second, ctx) as List<ConditionData>).getLabelLog(type)

        return "${label}_${population}_${group}_s${session}_$type_label"
    }

    override fun composeSubjectFileName(ctx: Context, blk:Int):String{
        if(label.isBlank() || group == -1 || type == -1 || session == -1)   return ""

        val blkstr =    if(blk > -1)    "_blk$blk"
                        else           ""

        return "${getFilesPrefix(ctx)}_${getDateString()}${blkstr}${TestBasic.SUBJFILE_EXTENSION}"
    }
}












