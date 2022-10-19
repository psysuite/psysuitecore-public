package iit.uvip.psysuite.core.stimuli

// used when logging
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.*
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import iit.uvip.psysuite.core.R
import org.albaspazio.nativeaudio.PlaybackEngine
import java.io.IOException
import java.io.InputStream


// resource is:
// - A1 : Int       ToneGenerator.TONE_xxxxx
// - A2 : String    resource name
// - A3 : String    names of assets "audio.wav"
// - A4 : String    names of assets "audio.wav"
//


class AudioManager(
    override var type: Int,
    var resource: Any,
    var amplitude : Float = 1F,
    override val duration: Long = -1L,
    var handler: Handler,
    private val ctx: Context
)
    : iStimulusManager{

    val outputSampleRate:Int
        get() = getDeviceSampleRate(ctx as Activity, ctx.resources)

    val framesPerBuffer:Int
        get() = getDeviceBufferSize(ctx as Activity, ctx.resources)

    val hasLowLatencyFeature: Boolean
        get() = ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)

    val hasProFeature: Boolean
        get() = ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)

    private var mTG: ToneGenerator? = null
    private var mMP: MediaPlayer?   = null
    private var mAT: AudioTrack?    = null

    private var isResourcesLoaded:Boolean   = false
    private var loadedResource:String       = ""

    override val isValid:Boolean
        get() = (duration > 0 && isResourcesLoaded)

    companion object{

        const val TAG = "AMANHT"
        @Throws(AudioResourceException::class)
        fun getAudioResource(ctx: Context, resname: String, volume: Float = 1F, deftype: String = "raw") : MediaPlayer {
            val mp = MediaPlayer.create(ctx, ctx.resources.getIdentifier(resname,deftype,ctx.packageName)) ?: throw AudioResourceException(resname)
            mp.setVolume(volume, volume)
            return mp
        }

        // playback audioresource until its end.  do not loop, execute one cycle
        @Throws(AudioResourceException::class, Exception::class)
        fun playbackAllAudioResource(ctx: Context, resname: String, volume: Float = 1F, deftype: String = "raw", loop:Int = 1, onEnd: () -> Unit = {}){
            try{
                val mediaPlayer = getAudioResource(ctx, resname, volume, deftype)
                var currLoop = 1
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener{

                    if(currLoop >= loop) {
                        onEnd()
                        it.release()
                    }
                    else  {
                        currLoop++
                        mediaPlayer.start()
                    }
                }
            }
            catch (e: Exception){ throw AudioResourceException(resname) }
        }

        @Throws(AudioResourceException::class)
        fun loadMediaPlayerFromAsset(ctx: Context, resname: String, volume: Float = 1F, loop:Boolean=false): MediaPlayer {
            return try {
                val afd = ctx.resources.assets.openFd(resname)
                val mp = MediaPlayer()
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mp.setVolume(volume, volume)
                mp.prepare()

                mp.isLooping = loop
                mp
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                throw AudioResourceException(resname)
            }
        }

        @Throws(AudioResourceException::class)
        fun loadAudioTrackFromAsset(ctx: Context, resname: String, sampleRate: Int, volume: Float = 1F): AudioTrack {

            // read file from assets as ByteArray
            val istr: InputStream = ctx.resources.assets.open(resname)
            val fileBytes = ByteArray(istr.available())
            istr.read(fileBytes)
            istr.close()

            // create AudioTrack object (setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) is available only with android 0)
            val at = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioTrack.Builder()
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
            } else {
                AudioTrack.Builder()
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
                    .setBufferSizeInBytes(fileBytes.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()            }

            // fill its buffer
            at.write(fileBytes, 0, fileBytes.size)
            at.setVolume(volume)
            at.notificationMarkerPosition = at.bufferSizeInFrames

            return at
        }

        fun getDeviceSampleRate(activity: Activity, res: Resources):Int {
            val am = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val sampleRateStr: String? = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            return sampleRateStr?.let { str ->
                Integer.parseInt(str).takeUnless { it == 0 }
            }  ?: res.getInteger(R.integer.sampleRate) // Use a default value if property not found
        }

        fun getDeviceBufferSize(activity: Activity, res: Resources):Int {
            val am = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val framesPerBuffer: String? = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
            return framesPerBuffer?.let { str ->
                Integer.parseInt(str).takeUnless { it == 0 }
            } ?: res.getInteger(R.integer.bufferSize) // Use default
        }
    }

    init{
        load(resource)
    }

    override fun load(stim1: Any, stim2: Any?, clb: () -> Unit) {
        when(type){
            StimuliManager.STIM_TYPE_A1 -> {
                if ((resource as Int) == -1) resource = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE

                val ampl: Int = if (amplitude == 1F) ToneGenerator.MAX_VOLUME
                else (amplitude * 100).toInt()

                mTG = ToneGenerator(AudioManager.STREAM_SYSTEM, ampl)
                isResourcesLoaded = true
            }
            StimuliManager.STIM_TYPE_A2 -> {
                if ((resource as String).isNotEmpty()) {
                    loadMPResource(resource as String, amplitude)   // also set isResourcesLoaded/loadedResource...otherwise throw AudioResourceException
                    if (isValid) mMP?.dummyUse(amplitude)
                } else isResourcesLoaded = true
            }
            StimuliManager.STIM_TYPE_A3 -> {
                if ((resource as String).isNotEmpty()) {
                    loadATResource(resource as String,amplitude)   // also set isResourcesLoaded/loadedResource...otherwise throw AudioResourceException
                    if (isValid) mAT?.dummyUse(amplitude)
                }
            }
            StimuliManager.STIM_TYPE_A4 -> {
                PlaybackEngine.setupAudioStream(ctx)
                if ((resource as String).isNotEmpty()) {
                    PlaybackEngine.loadWavAsset(ctx.resources.assets, resource as String, 0)
                    isResourcesLoaded = true
                }
            }
        }    }

    override fun deliver(dur: Any?, id: Int){       // id is the index, within the list given during inizialization, of the sound to be played
        val d = when(dur){
                    null,0L  -> duration
                    else    -> dur
                }

        when(type) {
            StimuliManager.STIM_TYPE_A1 -> mTG!!.startTone(resource as Int, (d as Long).toInt())
            StimuliManager.STIM_TYPE_A2 -> {
                mMP?.start()
                mMP?.setOnCompletionListener {
                    it.stop()
                    it.prepare()
                }
            }
            StimuliManager.STIM_TYPE_A3 -> {
//                Log.d(TAG,"${getOnsetDate()}: STARTED in thread, id=$id")   //, pos $a, state=$state, playstate=$playstate, dur=$dur")

                mAT?.setPlaybackPositionUpdateListener(object :
                    AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onPeriodicNotification(track: AudioTrack?) {}

                    override fun onMarkerReached(track: AudioTrack?) {
//                        Log.d(TAG, "${getOnsetDate()}: STOPPED in thread, id=$id")
                        track?.stop()
                        track?.reloadStaticData()
                    }
                })
                mAT?.play()
            }
            StimuliManager.STIM_TYPE_A4 -> {
//                Log.d(TAG,"${getOnsetDate()}: STARTED in thread, id=$id")
                PlaybackEngine.deliver(0)
            }
        }
    }

    override fun getHandler():Any? {
        return  when(type){
            StimuliManager.STIM_TYPE_A1 -> mTG
            StimuliManager.STIM_TYPE_A2 -> mMP
            else                        -> mAT
        }
    }

    override fun stop(id: Int){

        when(type) {
            StimuliManager.STIM_TYPE_A1 -> mTG!!.stopTone()
            StimuliManager.STIM_TYPE_A2 -> {
                if (mMP!!.isPlaying) {
                    mMP!!.stop()
                    mMP!!.prepare()
                }
            }
            StimuliManager.STIM_TYPE_A3 -> {
//                Log.d(TAG, "${getOnsetDate()}: STOPPED in thread, id=$id")
                mAT?.stop()
                mAT?.reloadStaticData()
            }
            StimuliManager.STIM_TYPE_A4 -> {
                PlaybackEngine.stop(0)
            }
        }
    }

    override fun clear() {
        when(type){
            StimuliManager.STIM_TYPE_A1 -> mTG?.release()
            StimuliManager.STIM_TYPE_A2 -> mMP?.release()
            StimuliManager.STIM_TYPE_A3 -> mAT?.release()
            StimuliManager.STIM_TYPE_A4 -> PlaybackEngine.release()
        }
    }

    @Throws(AudioResourceException::class, Exception::class)
    fun loadMPResource(resname: String, volume: Float = 1F, loop:Boolean=false):MediaPlayer{
        try{
            mMP         = loadMediaPlayerFromAsset(ctx, resname, volume, loop)
            loadedResource      = resname
            isResourcesLoaded   = true
            return mMP as MediaPlayer
        }
        catch (e: Exception){
            loadedResource      = ""
            isResourcesLoaded   = false
            throw AudioResourceException(resname)
        }
    }

    @Throws(AudioResourceException::class, Exception::class)
    fun loadATResource(resname: String, volume: Float = 1F):AudioTrack{
        try{
            mAT      = loadAudioTrackFromAsset(ctx, resname, outputSampleRate, volume)
            loadedResource      = resname
            isResourcesLoaded   = true
            return mAT as AudioTrack
        }
        catch (e: Exception){
            loadedResource      = ""
            isResourcesLoaded   = false
            throw AudioResourceException(resname)
        }
    }

}

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