package iit.uvip.psysuite.core.stimuli

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
import java.io.IOException
import java.io.InputStream


// resource is:
// - A1 : Int     ToneGenerator.TONE_xxxxx
// - A2 : String  resource name
// - A3 : List<String> names of assets files
//
// in A3 assets are loaded asynchronously at object creation. Thus, we start the loading process and when all are loaded the clb(AUDIO_SUCCESS) is called.
// I also set a timeout interval (TIMEOUT * #assets) to free the app and raise an exception.
// at its end a clb(failure) is called


class AudioManagerOLD(
    type: Int,
    var resource: Any,
    override var amplitude: Int = -1,
    override val duration: Long = -1L,
    handler: Handler,
    private val ctx: Context
)
    : StimulusManager(type, amplitude, duration, handler){

    var outputSampleRate:Int                = getDeviceSampleRate(ctx as Activity, ctx.resources)
    var framesPerBuffer:Int                 = getDeviceBufferSize(ctx as Activity, ctx.resources)

    val hasLowLatencyFeature: Boolean       = ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
    val hasProFeature: Boolean              = ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)

    private var mToneGen:ToneGenerator?     = null
    private var currMPAudio: MediaPlayer?   = null
    private var currAudioTrack: AudioTrack? = null

    private var sndPool:SoundPool?          = null
    private var sndPoolIDs:MutableList<Int> = mutableListOf()

    private var isResourcesLoaded:Boolean   = false
    private var loadedResource:String       = ""
    private var sndPoolPlayingID:Int        = -1


    private var loadedAssets:Int            = 0
    private var totAssets:Int               = 0

    val isValid:Boolean
        get() = (duration > 0 && isResourcesLoaded)

    companion object{

        const val TIMEOUT   = 5000L  // when loading assets in STIM_TYPE_A3 after
        @Throws(AudioResourceException::class)
        fun getAudioResource(ctx: Context, resname: String, volume: Float = 1F, deftype: String = "raw")
        : MediaPlayer {
            val mp = MediaPlayer.create(ctx, ctx.resources.getIdentifier(resname, deftype, ctx.packageName)
            ) ?: throw AudioResourceException(resname)
            mp.setVolume(volume, volume)
            return mp
        }

        // playback audioresource until its end
        @Throws(AudioResourceException::class, Exception::class)
        fun playbackAllAudioResource(ctx: Context, resname: String, volume: Float = 1F, deftype: String = "raw", onEnd: () -> Unit = {}){
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
        fun loadSndPoolFromAsset(ctx: Context, resname: String, sndPool: SoundPool, volume: Float = 1F ):Int {
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
        fun loadAudioTrackFromAsset(ctx: Context, resname: String, volume: Float = 1F, deftype: String = "raw"
        ): AudioTrack {

            // read file from assets as ByteArray
            val istr: InputStream = ctx.resources.assets.open(resname)
            val fileBytes = ByteArray(istr.available())
            istr.read(fileBytes)
            istr.close()

            // create AudioTrack object
            val at = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                )
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(48000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
                )
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .setBufferSizeInBytes(fileBytes.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            // fill its buffer
            val byte_written = at.write(fileBytes, 0, fileBytes.size)

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

        when(type){
            StimuliManager.STIM_TYPE_A1 -> {
                if ((resource as Int) == -1)    resource  = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE
                if (amplitude == -1)            amplitude = ToneGenerator.MAX_VOLUME

                mToneGen = ToneGenerator(AudioManager.STREAM_SYSTEM, amplitude)
                isResourcesLoaded = true
            }
            StimuliManager.STIM_TYPE_A2 -> {
                if ((resource as String).isNotEmpty()) {
                    loadMPResource(
                        resource as String,
                        amplitude.toFloat()
                    )   // also set isResourcesLoaded/loadedResource...otherwise throw AudioResourceException
                    if (isValid) currMPAudio?.dummyUse(amplitude.toFloat())
                }
            }
            StimuliManager.STIM_TYPE_A3 -> {
                if ((resource as String).isNotEmpty()) {
                    loadATResource(resource as String, amplitude.toFloat())   // also set isResourcesLoaded/loadedResource...otherwise throw AudioResourceException
//                    if (isValid) currMPAudio?.dummyUse(amplitude.toFloat())
                }
            }
        }
    }

    override fun deliver(dur: Any?, id: Int){       // id is the index, within the list given during inizialization, of the sound to be played
        val d = when(dur){
            null,0L  -> duration
            else    -> dur
        }

        when(type) {
            StimuliManager.STIM_TYPE_A1 -> mToneGen!!.startTone(
                resource as Int,
                (d as Long).toInt()
            )
            StimuliManager.STIM_TYPE_A2 -> {
                currMPAudio?.start()
                handler.postDelayed({ stop() }, d as Long)
            }
            StimuliManager.STIM_TYPE_A3 -> {
                currAudioTrack?.play()
                handler.postDelayed({ stop() }, d as Long)
            }
        }
    }

    override fun getHandler():Any? {
        return  when(type){
            StimuliManager.STIM_TYPE_A1 -> mToneGen
            StimuliManager.STIM_TYPE_A2 -> currMPAudio
                else                    -> currAudioTrack
        }
    }

    override fun stop(){

        when(type){
            StimuliManager.STIM_TYPE_A1 -> mToneGen!!.stopTone()
            StimuliManager.STIM_TYPE_A2 -> {
                if (currMPAudio!!.isPlaying) currMPAudio!!.stop()
                currMPAudio!!.prepare()
            }
//            else -> {
//                    if(sndPoolPlayingID != -1) {
//                        sndPool!!.stop(sndPoolPlayingID)
//                        sndPoolPlayingID = -1
//                    }
//            }
            else -> {
                currAudioTrack?.stop()
                currAudioTrack?.reloadStaticData()

            }
        }
    }

    fun isLoaded(res: String):Boolean{
        return when(type){
            StimuliManager.STIM_TYPE_A2 -> (res == loadedResource && currMPAudio != null)
            StimuliManager.STIM_TYPE_A3 -> (loadedAssets == (resource as List<*>).size && sndPool != null)
            else  -> true
        }
    }

    fun stopAssetsLoad(){
        handler.removeCallbacksAndMessages(null)
    }

    @Throws(AudioResourceException::class, Exception::class)
    fun loadMPResource(resname: String, volume: Float = 1F, deftype: String = "raw"):MediaPlayer{
        try{
//            currMPAudio         = getAudioResource(ctx, resname, volume, deftype)
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
    fun loadATResource(resname: String, volume: Float = 1F, deftype: String = "raw"):AudioTrack{
        try{
            currAudioTrack      = loadAudioTrackFromAsset(ctx, resname, volume)
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
}

/*
INIT
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

DELIVER
//            StimuliManager.STIM_TYPE_A3 -> {
//                sndPoolPlayingID = sndPool!!.play(
//                    sndPoolIDs[id],
//                    (amplitude * 1.0F) / 100,
//                    (amplitude * 1.0F) / 100,
//                    1,
//                    0,
//                    1.0f
//                )
//                handler.postDelayed({ stop() }, d as Long)
//            }
 */