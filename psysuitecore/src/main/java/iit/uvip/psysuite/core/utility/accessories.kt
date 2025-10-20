package iit.uvip.psysuite.core.utility

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

import java.util.*
import java.util.Collections.max

import iit.uvip.psysuite.core.stimuli.TactileManager
import iit.uvip.psysuite.core.tests.TestBasic.Companion.CONFLICT_TYPE_NONE
import org.albaspazio.core.accessory.VibrationManager


@Parcelize
data class StimuliDelay(val a:Long=0L, val t:Long=0L, val v:Long=0L) : Parcelable {// delay in ms of each modality wrt audio onset
    @IgnoredOnParcel
    val max:Long = max(listOf(a, t, v))
}

@Parcelize
data class CorrectedStimuliDelay(var a:Long=-1, var t:Long=-1, var v:Long=-1, var shift:Long=0) : Parcelable

@Parcelize
data class IdLabelData(val label:String, val id:Int, val label_log:String = label) : Parcelable {
    override fun toString(): String {
        return label
    }
}

@Parcelize
data class ConditionData(val label:String, val id:Int, val label_log:String = label, val allowedPopulations: List<IdLabelData>) : Parcelable {
    override fun toString(): String {
        return label
    }
}


@JvmName("getLabelLogConditionData")
fun List<ConditionData>.getLabelLog(type:Int):String{
    this.map{
        if(it.id == type)  return it.label_log
    }
    return ""
}

fun List<IdLabelData>.getLabelLog(type:Int):String{
    this.map{
        if(it.id == type)  return it.label_log
    }
    return ""
}

fun List<IdLabelData>.getId(label:String):Int{
    this.map{
        if(it.label == label)  return it.id
    }
    return -1
}

fun List<IdLabelData>.getLabel(id:Int):String{
    this.map{
        if(it.id == id)  return it.label
    }
    return ""
}

fun List<IdLabelData>.getIds():List<Int>{
    val ml = mutableListOf<Int>()
    this.map{
        ml.add(it.id)
    }
    return ml
}

@Parcelize
//data class TestResult(var code:Int=-1, var mailsubject:String, var mailbody:String, var res_files: ArrayList<String> = arrayListOf(), val testClass:String, val subject: SubjectBasicParcel) : Parcelable
data class TestResult(var code:Int=-1, var mailsubject:String, var mailbody:String, var res_files: ArrayList<String> = arrayListOf(), val testClass:String) : Parcelable

data class StimulusATBInfants(val type: Int, val tactile_pattern:Int)
data class Stimulus3delay(val type: Int, val a:Float, val t:Float, val v:Float)
data class StimulusDelay (val type: Int, val magnitude:Float)
data class StimuliSetBIS(val ntrials:Int, val magnitude:Float, val isBefore:Boolean, var conflict:String = CONFLICT_TYPE_NONE)
data class StimuliSetTSP(val ntrials:Int, val magnitude:Float, val isBefore:Boolean)
data class StimuliSetTIR(val ntrials:Int, val magnitude:Float)


fun VibrationManager.vibrateSingle(paramsT: TactileManager) {
    this.vibrateSingle(paramsT.duration, paramsT.amplitudes[0])
}

