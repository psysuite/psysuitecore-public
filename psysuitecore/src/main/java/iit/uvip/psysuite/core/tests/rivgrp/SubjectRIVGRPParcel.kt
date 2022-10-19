package iit.uvip.psysuite.core.tests.rivgrp

import android.content.Context
import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.model.parcel.SubjectLongitParcel
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.TestBasic.Companion.TEST_RIVGRP_RIVGRP_HC
import iit.uvip.psysuite.core.tests.TestBasic.Companion.TEST_RIVGRP_RIVGRP_HF
import iit.uvip.psysuite.core.utility.ConditionData
import iit.uvip.psysuite.core.utility.getLabelLog
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.accessory.getDateString

// session
@Parcelize
class SubjectRIVGRPParcel(
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
    override var population: Int = Populations.POPULATION_TD,
    override var isDebug: Boolean = false,

    var rivFirst:Boolean        = true,
    var blockDuration:Long      = 150000,
    var minImagesXblock:Int     = 2,
    var defaultBlocks:List<Int> = listOf(2,2,4,2,2,4),
    var totBlocks:Int           = 4

) : SubjectBasicParcel(type, label, age, gender, nextTrailModality, canRecordAudio, classes, device, block, stimuliDelays, whitenoise, vercode, showResult, population, isDebug){

    override fun getFilesPrefix(ctx:Context):String{

        val ci          = getCompanionObjectMethod(classes[0], "getConditionsInfo")
        val type_label  = (ci.first?.call(ci.second, ctx) as List<ConditionData>).getLabelLog(type)

        val first =     if(type == TEST_RIVGRP_RIVGRP_HF || type == TEST_RIVGRP_RIVGRP_HC) {
                            if (rivFirst)   "riv"
                            else            "grp"
                        }else               ""

        return "${label}_${population}_${first}_${totBlocks}_${blockDuration}_$type_label"
    }
}












