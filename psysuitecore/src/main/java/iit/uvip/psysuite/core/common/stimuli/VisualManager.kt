package iit.uvip.psysuite.core.common.stimuli

import android.os.Handler
import android.view.View
import android.widget.ImageView
import iit.uvip.psysuite.core.common.TestBasic

class VisualManager(type:Int, private val imgV: ImageView, var drawResOn:Int=1, private val drawResOff:Int=0, override val duration:Long=-1L, handler: Handler)
    : StimulusManager(type, 0, duration, handler){

    val isValid:Boolean
        get() = duration > 0

    init {
        if(type == TestBasic.STIM_TYPE_V1){
            imgV.setImageResource(drawResOn)
            imgV.visibility = View.INVISIBLE
        }
        else{
            imgV.setImageResource(drawResOff)
            imgV.visibility = View.VISIBLE
        }
    }

    override fun deliver(dur: Any?, id: Int){
        val d = dur ?: duration
        if(type == TestBasic.STIM_TYPE_V1)  imgV.visibility = View.VISIBLE
        else                                imgV.setImageResource(drawResOn)
        handler.postDelayed({ stop() }, d as Long)
    }

    override fun stop(){
        if(type == TestBasic.STIM_TYPE_V1)  imgV.visibility = View.INVISIBLE
        else                                imgV.setImageResource(drawResOff)
    }

    override fun getHandler(): ImageView {
        return imgV
    }
}
