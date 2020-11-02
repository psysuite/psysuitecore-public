package iit.uvip.psysuite.core.common.stimuli

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Handler
import android.util.Log
import iit.uvip.psysuite.core.common.TestBasic
import java.io.IOException


// resource is:
// - A1 : Int     ToneGenerator.TONE_xxxxx
// - A2 : String  resource name
// - A3 : List<String> names of assets files
//
// in A3 assets are loaded asynchronously at object creation. Thus, we start the loading process and when all are loaded the clb(AUDIO_SUCCESS) is called.
// I also set a timeout interval (TIMEOUT * #assets) to free the app and raise an exception.
// at its end a clb(failure) is called


class AudioManager(type: Int, var resource: Any, override var amplitude: Int = -1, override val duration: Long = -1L, handler: Handler, private val ctx: Context)
    : StimulusManager(type, amplitude, duration, handler){

    private var mToneGen:ToneGenerator?     = null
    private var currMPAudio: MediaPlayer?   = null


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
        fun getAudioResource(
            ctx: Context, resname: String, volume: Float = 1F, deftype: String = "raw"
        ): MediaPlayer {
            val mp = MediaPlayer.create(ctx, ctx.resources.getIdentifier(resname, deftype, ctx.packageName)) ?: throw AudioResourceException(resname)
            mp.setVolume(volume, volume)
            return mp
        }

        // playback audioresource until its end
        @Throws(AudioResourceException::class, Exception::class)
        fun playbackAllAudioResource(ctx: Context, resname: String, volume: Float = 1F, deftype: String = "raw", onEnd: () -> Unit = {}){
            try{
                val mediaPlayer = getAudioResource(ctx, resname, volume, deftype)
                mediaPlayer.setOnCompletionListener{
                    onEnd()
                    it.release()
                }
                mediaPlayer.start()
            }
            catch (e: Exception){ throw AudioResourceException(resname) }
        }

        @Throws(AudioResourceException::class)
        fun loadAsset(ctx: Context, resname: String, sndPool: SoundPool):Int {
            try {
                val afd = ctx.resources.assets.openFd(resname)
                return sndPool.load(afd, 1)
            } catch (e: IOException) {
                throw AudioResourceException(resname)
            }
        }
    }

    init{
        when(type){
            TestBasic.STIM_TYPE_A1 -> {
                if ((resource as Int) == -1) resource = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE
                if (amplitude == -1) amplitude = ToneGenerator.MAX_VOLUME

                mToneGen            = ToneGenerator(AudioManager.STREAM_SYSTEM, amplitude)
                isResourcesLoaded   = true
            }
            TestBasic.STIM_TYPE_A2 -> {
                if ((resource as String).isNotEmpty()) {
                    loadResource(resource as String, amplitude.toFloat())   // also set isResourcesLoaded/loadedResource...otherwise throw AudioResourceException
                    if(isValid) currMPAudio?.dummyUse(amplitude.toFloat())
                }
            }
            TestBasic.STIM_TYPE_A3 -> {
                try {
                    // is a not null/empty list + first element is not empty string
                    if (resource is List<*> && !(resource as List<*>).isNullOrEmpty() && (resource as List<*>)[0] is String && ((resource as List<*>)[0] as String).isNotEmpty()) {

                        totAssets = (resource as List<*>).size

                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        sndPool = SoundPool.Builder()
                            .setMaxStreams(totAssets)
                            .setAudioAttributes(audioAttributes)
                            .build()
                        sndPool!!.setOnLoadCompleteListener{ _: SoundPool, id: Int, status: Int ->
                            loadedAssets++
                            if(loadedAssets == totAssets) {
                                handler.removeCallbacksAndMessages(null)
                                isResourcesLoaded = true
                                Log.d("AudioManager", "all assets loaded !!")
//                                clb(StimuliManager.AUDIO_SUCCESS)
                            }
                            Log.d("AudioManager", "loaded assets $id with status $status")
                        }
//                        handler.postDelayed({ clb(StimuliManager.AUDIO_FAILURE) }, TIMEOUT*totAssets)
                        (resource as List<*>).map {    sndPoolIDs.add(loadAsset(ctx, it as String, sndPool!!))   }
                    }
                    else throw AudioResourceException(resource.toString())
                } catch (e: IOException) {
                    throw AudioResourceException(resource.toString())
                }
            }
        }
    }

    override fun deliver(dur: Any?, id: Int){       // id is the index, within the list given during inizialization, of the sound to be played
        val d = dur ?: duration

        when(type) {
            TestBasic.STIM_TYPE_A1 -> mToneGen!!.startTone(resource as Int, (d as Long).toInt())
            TestBasic.STIM_TYPE_A2 -> {
                currMPAudio?.start()
                handler.postDelayed({ stop() }, d as Long)
            }
            TestBasic.STIM_TYPE_A3 -> {
                sndPoolPlayingID = sndPool!!.play(sndPoolIDs[id], (amplitude*1.0F)/100, (amplitude*1.0F)/100, 1, 0, 1.0f)
                handler.postDelayed({ stop() }, d as Long)
            }
        }
    }

    override fun getHandler():Any? {
        return  when(type){
                TestBasic.STIM_TYPE_A1  -> mToneGen
                TestBasic.STIM_TYPE_A2  -> currMPAudio
                else                    -> sndPool
        }
    }

    override fun stop(){

        when(type){
            TestBasic.STIM_TYPE_A1  ->  mToneGen!!.stopTone()
            TestBasic.STIM_TYPE_A2  -> {
                                        currMPAudio!!.stop()
                                        currMPAudio!!.prepare()
            }
            else                    -> {
                                        if(sndPoolPlayingID != -1) {
                                            sndPool!!.stop(sndPoolPlayingID)
                                            sndPoolPlayingID = -1
                                        }
            }
        }
    }

    fun isLoaded(res: String):Boolean{
        return when(type){
            TestBasic.STIM_TYPE_A2  -> (res == loadedResource && currMPAudio != null)
            TestBasic.STIM_TYPE_A3  -> (loadedAssets == (resource as List<*>).size && sndPool != null)
            else  -> true
        }
    }

    fun stopAssetsLoad(){
        handler.removeCallbacksAndMessages(null)
    }

    @Throws(AudioResourceException::class, Exception::class)
    fun loadResource(resname: String, volume: Float = 1F, deftype: String = "raw"):MediaPlayer{
        try{
            currMPAudio         = getAudioResource(ctx, resname, volume, deftype)
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
}