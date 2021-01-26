package iit.uvip.psysuite.core.stimuli

import android.os.Handler
import org.albaspazio.core.accessory.VibrationManager

class TactileManager(private val vibrator: VibrationManager, val amplitude:Int=-1,
                     val timings:List<Int> = listOf(), val amplitudes:List<Int> = listOf(),
                     override val duration:Long=-1L, val handler: Handler, override val type:Int = StimuliManager.STIM_TYPE_T1
)
    : iStimulusManager{

    override val isValid:Boolean
        get() = duration > 0

    companion object{

        fun validatePattern(pattern:String):List<Int>?{
            val timings:MutableList<Int> = mutableListOf()
            pattern.split(",").map{
                try {
                    timings.add(it.toInt())
                }
                catch(e:NumberFormatException){
                    return null
                }
            }
            return timings
        }
    }

    override fun deliver(dur: Any?, id: Int){
        val d = dur ?: duration
        vibrator.vibrateSingle(d as Long, amplitude)
    }

    override fun stop(id: Int) {
        vibrator.cancel()
    }

    override fun getHandler(): VibrationManager {
        return vibrator
    }

    override fun clear() {}
}
