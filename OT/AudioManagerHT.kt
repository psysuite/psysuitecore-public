package iit.uvip.psysuite.core.stimuli
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.*
import android.media.AudioManager
import android.media.AudioTrack
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import iit.uvip.psysuite.core.R
import org.albaspazio.core.accessory.getOnsetDate
import java.io.IOException
import java.io.InputStream
import java.util.*


class AudioManagerHT(
    override var type: Int,
    var resource: Any,
    var amplitude: Float = 1F,
    override var duration: Long = -1L,
    var ctx: Context,
    name: String = "AudioManagerHT", priority: Int = Process.THREAD_PRIORITY_URGENT_AUDIO

) : HandlerThread(name, priority), iStimulusManager, Handler.Callback {

    private var mInternalHandler: Handler? = null // manage internal messages

    var outputSampleRate:Int                = getDeviceSampleRate(ctx as Activity, ctx.resources)
    var framesPerBuffer:Int                 = getDeviceBufferSize(ctx as Activity, ctx.resources)

    val hasLowLatencyFeature: Boolean       = ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
    val hasProFeature: Boolean              = ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)

    private var mToneGen: ToneGenerator?    = null
    private var currMPAudio: MediaPlayer?   = null
    private var currAudioTrack: AudioTrack? = null

    private var isResourcesLoaded:Boolean   = false
    private var loadedResource:String       = ""

    private var onsets: LongArray           = longArrayOf()
    private lateinit var onsetDate:Date

    override val isValid:Boolean
        get() = (duration > 0 && isResourcesLoaded)

    companion object {

        const val TAG = "AMANHT"

        const val MSG_ENQUEUE_SINGLE    = 1
        const val MSG_ENQUEUE_MULTI     = 2
        const val MSG_STOP              = 3

        @Throws(AudioResourceException::class)
        fun getAudioResource(
            ctx: Context,
            resname: String,
            volume: Float = 1F,
            deftype: String = "raw"
        )
        : MediaPlayer {
            val mp = MediaPlayer.create(
                ctx, ctx.resources.getIdentifier(
                    resname,
                    deftype,
                    ctx.packageName
                )
            ) ?: throw AudioResourceException(resname)
            mp.setVolume(volume, volume)
            return mp
        }

        // playback audioresource until its end
        @Throws(AudioResourceException::class, Exception::class)
        fun playbackAllAudioResource(
            ctx: Context,
            resname: String,
            volume: Float = 1F,
            deftype: String = "raw",
            onEnd: () -> Unit = {}
        ){
            try{
                val mediaPlayer = getAudioResource(ctx, resname, volume, deftype)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener{
                    onEnd()
                    it.release()
                }
            }
            catch (e: Exception){ throw AudioResourceException(resname) }
        }

        @Throws(AudioResourceException::class)
        fun loadMediaPlayerFromAsset(ctx: Context, resname: String, volume: Float = 1F): MediaPlayer {
            return try {
                val afd = ctx.resources.assets.openFd(resname)
                val mp = MediaPlayer()
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mp.setVolume(volume, volume)
                mp.prepare()

                mp.isLooping = false
                mp
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                throw AudioResourceException(resname)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        @Throws(AudioResourceException::class)
        fun loadAudioTrackFromAsset(
            ctx: Context,
            resname: String,
            sampleRate: Int,
            volume: Float = 1F
        ): AudioTrack {

            // read file from assets as ByteArray
            val istr: InputStream = ctx.resources.assets.open(resname)
            val fileBytes = ByteArray(istr.available())
            istr.read(fileBytes)
            istr.close()

            // create AudioTrack object
            val at = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .setBufferSizeInBytes(fileBytes.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            // fill its buffer
            at.write(fileBytes, 0, fileBytes.size)
            at.setVolume(volume)
            at.notificationMarkerPosition = at.bufferSizeInFrames

            return at
        }

        fun getDeviceSampleRate(activity: Activity, res: Resources):Int {
            val am = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val sampleRateStr: String? = am.getProperty(android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            return sampleRateStr?.let { str ->
                Integer.parseInt(str).takeUnless { it == 0 }
            }  ?: res.getInteger(R.integer.sampleRate) // Use a default value if property not found
        }

        fun getDeviceBufferSize(activity: Activity, res: Resources):Int {
            val am = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val framesPerBuffer: String? = am.getProperty(android.media.AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
            return framesPerBuffer?.let { str ->
                Integer.parseInt(str).takeUnless { it == 0 }
            } ?: res.getInteger(R.integer.bufferSize) // Use default
        }
    }

    init{

        when(type){
            StimuliManager.STIM_TYPE_A1 -> {
                if ((resource as Int) == -1) resource = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE

                val ampl: Int = if (amplitude == 1F) ToneGenerator.MAX_VOLUME
                else (amplitude * 100).toInt()

                mToneGen = ToneGenerator(android.media.AudioManager.STREAM_SYSTEM, ampl)
                isResourcesLoaded = true
            }
            StimuliManager.STIM_TYPE_A2 -> {
                if ((resource as String).isNotEmpty()) {
                    loadMPResource(
                        resource as String,
                        amplitude
                    )   // also set isResourcesLoaded/loadedResource...otherwise throw AudioResourceException
                    if (isValid) currMPAudio?.dummyUse(amplitude)
                } else isResourcesLoaded = true
            }
            StimuliManager.STIM_TYPE_A3 -> {
                if ((resource as String).isNotEmpty()) {
                    loadATResource(resource as String,amplitude)   // also set isResourcesLoaded/loadedResource...otherwise throw AudioResourceException
                }
            }
        }
    }

    //================================================================================================================
    // calls from other threads
    //================================================================================================================
    fun deliverMultiStimuli(
        onsets: LongArray = longArrayOf(),
        durations: LongArray = longArrayOf(),
        id: Int = -1
    ){

        val bundle = Bundle()

        bundle.putLongArray("onsets", onsets)
        bundle.putLongArray("durations", durations)
        bundle.putInt("id", id)

        val message     = mInternalHandler!!.obtainMessage()
        message.what    = MSG_ENQUEUE_MULTI
        message.data    = bundle
        mInternalHandler!!.sendMessage(message)
    }

    fun deliverStimulus(onset: Long = 0, duration: Long = 0, id: Int = -1){

        val bundle = Bundle()

        bundle.putLong("onset", onset)
        bundle.putLong("duration", duration)
        bundle.putInt("id", id)

        val message     = mInternalHandler!!.obtainMessage()
        message.what    = MSG_ENQUEUE_SINGLE
        message.data    = bundle
        mInternalHandler!!.sendMessage(message)
    }

    fun stopAudio(){
        val message     = mInternalHandler!!.obtainMessage()
        message.what    = MSG_STOP
        mInternalHandler!!.sendMessage(message)
    }
    //================================================================================================================
    // messages handler
    //================================================================================================================
    override fun handleMessage(msg: Message): Boolean {

        return try {
            val b: Bundle = msg.data
            when (msg.what) {
                MSG_ENQUEUE_SINGLE -> {
                    val dur = if (b.getLong("duration") == 0L) duration
                    else b.getLong("duration")
                    val onset = b.getLong("onset")
                    val id = b.getInt("id")

                    if (onset == 0L) deliverSingle(onset, dur, id)
                    else mInternalHandler?.postDelayed(
                        { deliverSingle(onset, dur, id) },
                        onset
                    )
                }
                MSG_ENQUEUE_MULTI -> deliverMulti(
                    b.getLongArray("onsets") ?: longArrayOf(), b.getLongArray(
                        "durations"
                    ) ?: longArrayOf()
                )
                MSG_STOP -> _stop()
            }
            true
        } catch (e: Exception) {
            // TODO : implement Messaging.sendErrorString(mWlCb, e.message, ERRORS.CAPTURE_ERROR, true)
            false
        }
    }

    private fun deliverMulti(
        onsets: LongArray = longArrayOf(),
        durations: LongArray = longArrayOf()
    ){
        // TODO to be implemented
    }

    private fun deliverSingle(onset: Long, dur: Long, id: Int = -1){       // id is the index, within the list given during inizialization, of the sound to be played

        onsetDate           = Date()
//        Log.d("AudioHT", "audio stim: type=$type, local onset=${getOnsetDate()}, dur=$dur, id=$id")
        when(type) {
            StimuliManager.STIM_TYPE_A1 -> mToneGen!!.startTone(resource as Int, dur.toInt())
            StimuliManager.STIM_TYPE_A2 -> {
                currMPAudio?.start()
                currMPAudio?.setOnCompletionListener {
                    it.stop()
                    it.prepare()
                }
            }
            StimuliManager.STIM_TYPE_A3 -> {
//                val a           = currAudioTrack?.playbackHeadPosition
//                val state       = currAudioTrack?.state
//                val playstate   = currAudioTrack?.playState
                Log.d(TAG,"${getOnsetDate()}: STARTED in thread, id=$id")   //, pos $a, state=$state, playstate=$playstate, dur=$dur")

                currAudioTrack?.setPlaybackPositionUpdateListener(object :
                    AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onPeriodicNotification(track: AudioTrack?) {}

                    override fun onMarkerReached(track: AudioTrack?) {
                        Log.d(TAG, "${getOnsetDate()}: STOPPED in thread, id=$id")
                        track?.stop()
                        track?.reloadStaticData()
                    }
                })
                currAudioTrack?.play()
//                mInternalHandler?.postDelayed({ _stop(id) }, dur)
            }
        }
    }

    override fun onLooperPrepared() {
        mInternalHandler = Handler(looper, this)
    }

    private fun _stop(id: Int = -1){

        when(type){
            StimuliManager.STIM_TYPE_A1 -> mToneGen!!.stopTone()
            StimuliManager.STIM_TYPE_A2 -> {
                if (currMPAudio!!.isPlaying) {
                    currMPAudio!!.stop()
                    currMPAudio!!.prepare()
                }
            }
            StimuliManager.STIM_TYPE_A3 -> {
                Log.d(TAG, "${getOnsetDate()}: STOPPED in thread, id=$id")
                currAudioTrack?.stop()
                currAudioTrack?.reloadStaticData()
            }
        }
    }

    @Throws(AudioResourceException::class, Exception::class)
    fun loadMPResource(resname: String, volume: Float = 1F):MediaPlayer{
        try{
            currMPAudio         = loadMediaPlayerFromAsset(ctx, resname, volume)
            loadedResource      = resname
            isResourcesLoaded   = true
            return currMPAudio as MediaPlayer
        }
        catch (e: Exception){
            loadedResource      = ""
            isResourcesLoaded   = false
            throw AudioResourceException(resname)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(AudioResourceException::class, Exception::class)
    fun loadATResource(resname: String, volume: Float = 1F):AudioTrack{
        try{
            currAudioTrack      = loadAudioTrackFromAsset(ctx, resname, outputSampleRate, volume)
            loadedResource      = resname
            isResourcesLoaded   = true
            return currAudioTrack as AudioTrack
        }
        catch (e: Exception){
            loadedResource      = ""
            isResourcesLoaded   = false
            throw AudioResourceException(resname)
        }
    }

    override fun deliver(dur: Any?, id: Int) {
        TODO("Not yet implemented")
    }

    override fun stop(id: Int) {
        TODO("Not yet implemented")
    }

    override fun getHandler(): Any? {
        TODO("Not yet implemented")
    }

}


//    fun getHandlerLooper(): Handler? {
//        if (mInternalHandler == null) Log.w("", "AudioHandlerThread mInternalHandler is NULL !!!!!!!!!!!!!!!!!")
//        return mInternalHandler
//    }

//    fun isLoaded(res: String):Boolean{
//        return when(type){
//            StimuliManager.STIM_TYPE_A2 -> (res == loadedResource && currMPAudio != null)
//            StimuliManager.STIM_TYPE_A3 -> (loadedAssets == (resource as List<*>).size && sndPool != null)
//            else  -> true
//        }
//    }
//
//    fun stopAssetsLoad(){
//        mInternalHandler?.removeCallbacksAndMessages(null)
//    }


/*
SNDPOOL

    private var sndPool: SoundPool?          = null
    private var sndPoolIDs:MutableList<Int> = mutableListOf()

    private var sndPoolPlayingID:Int        = -1
    private var loadedAssets:Int            = 0
    private var totAssets:Int               = 0
        const val TIMEOUT   = 5000L  // when loading assets in STIM_TYPE_A3 after

// COMPANION
        @Throws(AudioResourceException::class)
        fun loadSndPoolFromAsset(
            ctx: Context, resname: String, sndPool: SoundPool, volume: Float = 1F
        ):Int {
            try {
                val afd = ctx.resources.assets.openFd(resname)
                val sndp = sndPool.load(afd, 1)
                sndPool.setVolume(sndp, volume, volume)
                afd.close()
                return sndp
            } catch (e: IOException) {
                throw AudioResourceException(resname)
            }
        }

// INIT
//            StimuliManager.STIM_TYPE_A3 -> {
//                try {
//                    // is a not null/empty list + first element is not empty string
//                    if (resource is List<*> && !(resource as List<*>).isNullOrEmpty() && (resource as List<*>)[0] is String && ((resource as List<*>)[0] as String).isNotEmpty()) {
//
//                        totAssets = (resource as List<*>).size
//
//                        val audioAttributes = AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                            .build()
//                        sndPool = SoundPool.Builder()
//                            .setMaxStreams(totAssets)
//                            .setAudioAttributes(audioAttributes)
//                            .build()
//                        sndPool!!.setOnLoadCompleteListener { _: SoundPool, id: Int, status: Int ->
//                            loadedAssets++
//                            if (loadedAssets == totAssets) {
//                                handler.removeCallbacksAndMessages(null)
//                                isResourcesLoaded = true
//                                Log.d("AudioManager", "all assets loaded !!")
////                                clb(StimuliManager.AUDIO_SUCCESS)
//                            }
//                            Log.d("AudioManager", "loaded assets $id with status $status")
//                        }
////                        handler.postDelayed({ clb(StimuliManager.AUDIO_FAILURE) }, TIMEOUT*totAssets)
//                        (resource as List<*>).map {
//                            sndPoolIDs.add(loadSndPoolFromAsset(ctx, it as String, sndPool!!))
//                        }
//                    } else throw AudioResourceException(resource.toString())
//                } catch (e: IOException) {
//                    throw AudioResourceException(resource.toString())
//                }
//            }

// DELIVER
//            StimuliManager.STIM_TYPE_A3 -> {
//                sndPoolPlayingID = sndPool!!.play( sndPoolIDs[id], (amplitude * 1.0F) / 100, (amplitude * 1.0F) / 100, 1, 0, 1.0f)
//                handler.postDelayed({ stop() }, d as Long)
//            }

// STOP
//            else -> {
//                    if(sndPoolPlayingID != -1) {
//                        sndPool!!.stop(sndPoolPlayingID)
//                        sndPoolPlayingID = -1
//                    }
//            }
 */