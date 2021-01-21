package iit.uvip.psysuite.core.stimuli

import android.content.Context
import android.os.*
import android.util.Log

/*
it's a layer which call the VAD functions on a new thread
sends the following messages to Plugin Activity:

*/
class AudioManagerHTonly(
    var type: Int,
    var resource: Any,
    var amplitude: Int = -1,
    var duration: Long = -1L,
    var ctx: Context,
    name: String = "AudioManagerHT", priority: Int = Process.THREAD_PRIORITY_URGENT_AUDIO
) : HandlerThread(name, priority), Handler.Callback {

    private var mInternalHandler: Handler? = null // manage internal messages

    private lateinit var mAudioManager: AudioManagerOLD

    companion object {

        const val MSG_ENQUEUE_SINGLE = 1
        const val MSG_ENQUEUE_MULTI = 2
        const val MSG_STOP = 3

        private const val LOG_TAG = "AudioManagerHT"

    }

    fun init(){

        mInternalHandler = Handler(looper, this)        // if HT started, looper is blocking until is initialized
        mAudioManager = AudioManagerOLD(type, resource, amplitude, duration, mInternalHandler!!, ctx)

    }
    //================================================================================================================
    // calls from other threads
    //================================================================================================================
    fun deliverStimulus(onset:Long=0, duration:Long=0){

        val bundle = Bundle()

        bundle.putLong("onset", onset)
        bundle.putLong("duration", duration)

        val message     = mInternalHandler!!.obtainMessage()
        message.what    = MSG_ENQUEUE_SINGLE
        message.data    = bundle
        mInternalHandler!!.sendMessage(message)
    }

    fun deliverMultiStimuli(onsets: LongArray = longArrayOf(), durations: LongArray = longArrayOf()){

        val bundle = Bundle()

        bundle.putLongArray("onsets", onsets)
        bundle.putLongArray("durations", durations)

        val message     = mInternalHandler!!.obtainMessage()
        message.what    = MSG_ENQUEUE_MULTI
        message.data    = bundle
        mInternalHandler!!.sendMessage(message)
    }

    fun stopAudio(){
        val message     = mInternalHandler!!.obtainMessage()
        message.what    = MSG_STOP
        mInternalHandler!!.sendMessage(message)
    }

    val isValid:Boolean
        get()   = mAudioManager.isValid

    //================================================================================================================
    // messages handler
    //================================================================================================================
    override fun handleMessage(msg: Message): Boolean {

        return try {
            val b: Bundle = msg.data
            when (msg.what) {
                MSG_ENQUEUE_SINGLE  -> {
                    val onset   = b.getLong("onset")
                    val dur     = b.getLong("duration")

                    if(onset == 0L) mAudioManager.deliver(dur)
                    else            mInternalHandler?.postDelayed({
                                        mAudioManager.deliver(dur)
                                    }, onset)

                    mInternalHandler?.postDelayed({ mAudioManager.stop() }, onset + dur)

                }
                MSG_ENQUEUE_MULTI   -> deliverMulti(b.getLongArray("onsets") ?: longArrayOf(), b.getLongArray("durations") ?: longArrayOf())
                MSG_STOP            -> mAudioManager.stop()
            }
            true
        } catch (e: Exception) {
//            Messaging.sendErrorString2Web(mWlCb, e.message, ERRORS.CAPTURE_ERROR, true)
            false
        }
    }

//    private fun deliverSingle(onset:Long, dur: Long, id: Int = -1){
//
//        if(onset == 0L) mAudioManager.deliver(dur)
//        else            mInternalHandler?.postDelayed({ mAudioManager.deliver(dur) }, onset)
//
//        mInternalHandler?.postDelayed({ mAudioManager.stop() }, onset + dur)
//    }

    private fun deliverMulti(onset:LongArray, dur: LongArray, id: Int = -1){
//        mInternalHandler?.postDelayed({ _stop() }, onset + dur)
    }

    fun getHandlerLooper(): Handler? {
        if (mInternalHandler == null) Log.w("", "AudioHandlerThread mInternalHandler is NULL !!!!!!!!!!!!!!!!!")
        return mInternalHandler
    }
}