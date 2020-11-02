package iit.uvip.psysuite.core.common.stimuli

import android.media.MediaPlayer
import android.os.Handler

// these handlers have a null property in TestBasic
// in each subclass, their dependance (vibrator, ImageView) are validated in init.


abstract class StimulusManager(open val type:Int, open val amplitude:Any, open val duration:Long, protected val handler: Handler){

    override fun toString():String{
        return "${StimulusManager::class.java.simpleName}, ampl=$amplitude, duration=$duration"
    }
    abstract fun deliver(dur:Any?=null, id:Int=0)
    abstract fun stop()
    abstract fun getHandler():Any?
}

// used when dealing with very short sounds (50-100ms) that on their first playback are not audible
fun MediaPlayer.dummyUse(vol:Float){
    this.setVolume(0F,0F)
    this.start()
    this.stop()
    this.prepare()
    this.setVolume(vol, vol)
}


class AudioResourceException(msg:String):Exception(msg)
class VibratorNotDefinedException(msg:String):Exception(msg)
class ImageViewDefinedException(msg:String):Exception(msg)