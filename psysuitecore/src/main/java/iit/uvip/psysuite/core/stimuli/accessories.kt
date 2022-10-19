package iit.uvip.psysuite.core.stimuli

import android.media.AudioTrack
import android.media.MediaPlayer

// these handlers have a null property in TestBasic
// in each subclass, their dependance (vibrator, ImageView) are validated in init.


interface iStimulusManager{

    val duration:Long
    val type:Int

    fun load(stim1:Any, stim2:Any?=null, clb: () -> Unit = {})
    fun deliver(dur:Any?=null, id:Int=0)
    fun stop(id:Int = -1)
    fun getHandler():Any?
    fun clear()

    val isValid:Boolean
}

// used when dealing with very short sounds (50-100ms) that on their first playback are not audible
fun MediaPlayer.dummyUse(vol:Float){
    this.setVolume(0F,0F)
    this.start()
    this.stop()
    this.prepare()
    this.setVolume(vol, vol)
}

// used when dealing with very short sounds (50-100ms) that on their first playback are not audible
fun AudioTrack.dummyUse(vol:Float){
    this.setVolume(0F)
    this.play()
    this.stop()
    this.reloadStaticData()
    this.setVolume(vol)
}


class AudioResourceException(msg:String):Exception(msg)
class VibratorNotDefinedException(msg:String):Exception(msg)
class ImageViewDefinedException(msg:String):Exception(msg)