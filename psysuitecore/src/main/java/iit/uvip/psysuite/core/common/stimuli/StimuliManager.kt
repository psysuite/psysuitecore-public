package iit.uvip.psysuite.core.common.stimuli

import android.os.Handler
import java.util.*

/*
some tests can deliver a combination of stimuli (uni-bi-trimodal).
I set the way I deliver each of them and then expose method allowing to select my modality
*/

class StimuliManager(
    val mAudioManager: AudioManager? = null,
    val mTactileManager: TactileManager? = null,
    val mVisualManager: VisualManager? = null,
    private val clb: () -> Unit = {}
) {

    private var mHandler: Handler = Handler()

    init{
        checkResourcesLoading()
    }
    // to be used when one single modalities-combination is used
    val type:Int
        get() {
            var t = 0
            t = t or (mAudioManager?.type ?: t)
            t = t or (mTactileManager?.type ?: t)
            return t or (mVisualManager?.type ?: t)
        }

    // get type separated by modalities-combinations
    val typeA:Int
        get() = mAudioManager?.type ?: -1
    val typeT:Int
        get() = mTactileManager?.type ?: -1
    val typeV:Int
        get() = mVisualManager?.type ?: -1
    val typeAT:Int
        get(){
            var t = 0
            t = t or (mAudioManager?.type ?: t)
            return t or (mTactileManager?.type ?: t)
        }
    val typeAV:Int
        get(){
            var t = 0
            t = t or (mAudioManager?.type ?: t)
            return t or (mVisualManager?.type ?: t)
        }
    val typeTV:Int
        get(){
            var t = 0
            t = t or (mTactileManager?.type ?: t)
            return t or (mVisualManager?.type ?: t)
        }

    val audioDuration:Long
        get() = mAudioManager?.duration ?: -1L
    val tactileDuration:Long
        get() = mTactileManager?.duration ?: -1L
    val visualDuration:Long
        get() = mVisualManager?.duration ?: -1L

    // TRUE if at least one manager is valid
    val isValid:Boolean
        get() = (mAudioManager?.isValid ?: false || mTactileManager?.isValid ?: false || mVisualManager?.isValid ?: false)

    private fun checkResourcesLoading(){

        val runTask: Runnable = object : Runnable {
            override fun run() {
                if(mAudioManager?.isValid ?: true && mTactileManager?.isValid ?: true && mAudioManager?.isValid ?: true ){
                    mHandler.removeCallbacksAndMessages(null)
                    clb()
                }
                else    mHandler.postDelayed(this, 100)
            }
        }
        mHandler.post(runTask)  // Start the initial runnable task by posting through the handler
    }

    fun getValidAudioManager(manager: AudioManager?):AudioManager?{

        val mam_dur =  mAudioManager?.isValid ?: false
        val am_dur  =  manager?.isValid ?: false

        return when {
            am_dur  -> manager
            mam_dur -> mAudioManager
            else    -> null
        }
    }

    fun getValidTactileManager(manager: TactileManager?):TactileManager?{

        val mtm_dur =  mTactileManager?.isValid ?: false
        val tm_dur  =  manager?.isValid ?: false

        return when {
            tm_dur  -> manager
            mtm_dur -> mTactileManager
            else    -> null
        }
    }

    fun getValidVisualManager(manager: VisualManager?):VisualManager?{

        val mvm_dur =  mVisualManager?.isValid ?: false
        val vm_dur  =  manager?.isValid ?: false

        return when {
            vm_dur  -> manager
            mvm_dur -> mVisualManager
            else    -> null
        }
    }

    // returns MAX, MIN, MEAN durations when they are defined in the single call or retrieve the test's default values
    fun getDuration(managerA: StimulusManager? = null, managerT: StimulusManager? = null, managerV: StimulusManager? = null):Triple<Long, Long, Long>?{

        val durs:MutableList<Long> = mutableListOf()

        if((managerA?.duration ?: -1) > 0)  durs.add(managerA!!.duration)
        if(managerT != null)    if(managerT.duration > 0)    durs.add(managerT.duration)
        if(managerV != null)    if(managerV.duration > 0)    durs.add(managerV.duration)

        if(durs.isEmpty())  return null

        var mean:Long = 0
        var cnt = 0
        durs.map {
            mean += it
            cnt++
        }
        mean /= cnt
        return Triple(Collections.max(durs) as Long, Collections.min(durs) as Long, mean)
    }
}