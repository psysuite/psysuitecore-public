package iit.uvip.psysuite.core.tests.ttc

import iit.uvip.psysuite.core.trials.TrialBasic
import org.albaspazio.core.accessory.round

// speed in px per second
// distance in px
// magnitude is the distance from correctTime = dist/speed (e.g. 250),
// stim_value is the temporal distance from start (e.g. 1000-magnitude=750ms) when target must disappear
open class TrialTTC(id:Int=-1, type:Int, label:String,
                    final override var magnitude:Float,
                    val time:Long,
                    val distance:Int,
                    val imageId:Int, val isHoriz:Boolean=true, val isDownRight:Boolean=true, isADA:Boolean=false): TrialBasic(id, type, label, isADA=isADA) {

    var pxPerMs:Double = distance/time.toDouble()

    private var delta:Int = 0

    override val stim_value: Long
        get() = time-magnitude.toLong()

    companion object {
        @JvmStatic val LOG_HEADER = "id\tlabel\tvis_time\tisHor\tis_dr\tres\terror\tspeed\tdist\ttime\timageid\n"
    }

    init {
        updateTrial(magnitude)
    }
    // all class exported as string
    override fun toString():String{
        return "$id\t$label\t$stim_value\t$isHoriz\t$isDownRight\t$user_answer\t$delta\t${pxPerMs.round(3)}\t$distance\t$time\t$imageId"
    }

    // data exported to log file
    override fun Log():String{
        return toString() + "\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, pos=$stim_value, is_oriz=$isHoriz, is_down_right=$isDownRight"
    }

    override fun setResponse(result: Int, elapsedms: Int, extra_text:String) {
        super.setResponse(result, elapsedms, extra_text)
        delta = (result - time).toInt()
    }

    final override fun updateTrial(newvalue: Float):Long{
        magnitude = newvalue
        return stim_value
    }

}
