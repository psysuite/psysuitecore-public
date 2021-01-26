package iit.uvip.psysuite.core.stimuli

import android.os.Handler
import android.view.View
import android.widget.ImageView

class VisualManager(override val type:Int, private val imgV: ImageView, var drawResOn:Int=1, private val drawResOff:Int=0, override val duration:Long=-1L, val handler: Handler)
    : iStimulusManager{

    override val isValid:Boolean
        get() = duration > 0

    init {
        if(type == StimuliManager.STIM_TYPE_V1){
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
        if(type == StimuliManager.STIM_TYPE_V1)  imgV.visibility = View.VISIBLE
        else                                imgV.setImageResource(drawResOn)
        handler.postDelayed({ stop() }, d as Long)
    }

    override fun stop(id: Int) {
        if(type == StimuliManager.STIM_TYPE_V1)  imgV.visibility = View.INVISIBLE
        else                                imgV.setImageResource(drawResOff)    }

    override fun getHandler(): ImageView {
        return imgV
    }

    override fun clear() {
        imgV.setImageDrawable(null)
    }
}
