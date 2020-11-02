package iit.uvip.psysuite.core.common

import android.content.Context
import android.os.Environment
import android.os.Parcelable
import iit.uvip.psysuite.core.common.stimuli.TactileManager
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.getAbsoluteFilePath
import org.albaspazio.core.accessory.saveText
import java.util.*
import java.util.Collections.max

@Parcelize
data class StimuliDelay(val a:Long=0L, val t:Long=0L, val v:Long=0L) : Parcelable {// delay in ms of each modality wrt audio onset
    @IgnoredOnParcel
    val max:Long = max(listOf(a, t, v))
}

@Parcelize
data class CorrectedStimuliDelay(var a:Long=-1, var t:Long=-1, var v:Long=-1, var shift:Long=0) : Parcelable

@Parcelize
data class SpinnerData(val label:String, val id:Int, val label_log:String = label) : Parcelable {
    override fun toString(): String {
        return label
    }
}

fun List<SpinnerData>.getLabelLog(type:Int):String{
    this.map{
        if(it.id == type)  return it.label_log
    }
    return ""
}

fun List<SpinnerData>.getLabel(type:Int):String{
    this.map{
        if(it.id == type)  return it.label
    }
    return ""
}

@Parcelize
data class TestResult(var code:Int=-1, var mailsubject:String, var mailbody:String, var res_files: ArrayList<String> = arrayListOf(), val testClass:String) : Parcelable

data class StimulusATBInfants(val type: Int, val tactile_pattern:Int)
data class Stimulus3delay(val type: Int, val a:Long, val t:Long, val v:Long)
data class StimulusBindingsUnbalanced(val type: Int, val delay:Long)
data class StimulusBIS(val ntrials:Int, val position:Int, val conflict:String)

fun VibrationManager.vibrateSingle(paramsT: TactileManager) {
    this.vibrateSingle(paramsT.duration, paramsT.amplitude)
}

abstract class Summary(private val ctx: Context){

    abstract fun add(trial:TrialBasic)
    abstract fun close(filename:String, dir:String = Environment.DIRECTORY_DOWNLOADS):String

    protected fun writeFile(summary:String, filename:String, dir:String = Environment.DIRECTORY_DOWNLOADS):String{
        return when(saveText(ctx, filename, summary, dir, true, notifyDm=true)){
            true    -> getAbsoluteFilePath(filename, dir).second
            false   -> ""
        }
    }

}