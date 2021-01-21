package iit.uvip.psysuite.core.stimuli

// used for logging
import android.content.Context
import android.os.Handler
import iit.uvip.psysuite.core.R
import java.util.*


class StimuliManager(
    val mAudioManager: iStimulusManager? = null,
    val mTactileManager: TactileManager? = null,
    val mVisualManager: VisualManager? = null,
    private val delaysAligner: DelaysAligner,
    private val ctx: Context,
    private var mStimuliHandler: Handler,
    private val clb: () -> Unit = {}
) {

    companion object {
        const val TAG = "STIMMAN"
        //-----------------------------------------------------------------------------------------
        // STIMULUS TYPES UNIQUE CODES
        //-----------------------------------------------------------------------------------------
        // A1: tone, A2:resource, V1: view made visible/invisible, V2: imageview with different color frame (one as background), T1: single, T2:sequence
        @JvmStatic val STIM_TYPE_A1                 = 1     //  000 000 001
        @JvmStatic val STIM_TYPE_A2                 = 2     //  000 000 010
        @JvmStatic val STIM_TYPE_A3                 = 4     //  000 000 100
        @JvmStatic val STIM_TYPE_T1                 = 8     //  000 001 000
        @JvmStatic val STIM_TYPE_T2                 = 16    //  000 010 000
        @JvmStatic val STIM_TYPE_V1                 = 64    //  001 000 000
        @JvmStatic val STIM_TYPE_V2                 = 128   //  010 000 000

        @JvmStatic val STIM_TYPE_A1T1               = 9     //  000 001 001
        @JvmStatic val STIM_TYPE_A1T2               = 17    //  000 010 001
        @JvmStatic val STIM_TYPE_A1V1               = 65    //  001 000 001
        @JvmStatic val STIM_TYPE_A1V2               = 129   //  010 000 001
        @JvmStatic val STIM_TYPE_A2T1               = 10    //  000 001 010
        @JvmStatic val STIM_TYPE_A2T2               = 18    //  000 010 010
        @JvmStatic val STIM_TYPE_A2V1               = 66    //  001 000 010
        @JvmStatic val STIM_TYPE_A2V2               = 131   //  010 000 011
        @JvmStatic val STIM_TYPE_A3T1               = 12    //  000 001 100
        @JvmStatic val STIM_TYPE_A3T2               = 20    //  000 010 100
        @JvmStatic val STIM_TYPE_A3V1               = 68    //  001 000 100
        @JvmStatic val STIM_TYPE_A3V2               = 131   //  010 000 100
        @JvmStatic val STIM_TYPE_T1V1               = 72    //  001 001 000
        @JvmStatic val STIM_TYPE_T2V1               = 80    //  001 010 000
        @JvmStatic val STIM_TYPE_T1V2               = 136   //  010 001 000
        @JvmStatic val STIM_TYPE_T2V2               = 144   //  010 010 000

        @JvmStatic val STIM_TYPE_A1T1V1             = 73    //  001 001 001
        @JvmStatic val STIM_TYPE_A1T2V1             = 81    //  001 010 001
        @JvmStatic val STIM_TYPE_A1T1V2             = 137   //  010 001 001
        @JvmStatic val STIM_TYPE_A1T2V2             = 145   //  010 010 001
        @JvmStatic val STIM_TYPE_A2T1V1             = 74    //  001 001 010
        @JvmStatic val STIM_TYPE_A2T2V1             = 82    //  001 010 010
        @JvmStatic val STIM_TYPE_A2T1V2             = 138   //  010 001 010
        @JvmStatic val STIM_TYPE_A2T2V2             = 146   //  010 010 010
        @JvmStatic val STIM_TYPE_A3T1V1             = 76    //  001 001 100
        @JvmStatic val STIM_TYPE_A3T2V1             = 84    //  001 010 100
        @JvmStatic val STIM_TYPE_A3T1V2             = 140   //  010 001 100
        @JvmStatic val STIM_TYPE_A3T2V2             = 148   //  010 010 100

        fun maintype2unimodaltypes(source:Int):List<Int>{

            val a = when {
                source and STIM_TYPE_A1 > 0 -> STIM_TYPE_A1
                source and STIM_TYPE_A2 > 0 -> STIM_TYPE_A2
                source and STIM_TYPE_A3 > 0 -> STIM_TYPE_A3
                else -> -1
            }

            val t = when {
                source and STIM_TYPE_T1 > 0 -> STIM_TYPE_T1
                source and STIM_TYPE_T2 > 0 -> STIM_TYPE_T2
                else -> -1
            }

            val v = when {
                source and STIM_TYPE_V1 > 0 -> STIM_TYPE_V1
                source and STIM_TYPE_V2 > 0 -> STIM_TYPE_V2
                else -> -1
            }
            return listOf(a,t,v)
        }        
    }

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
                if(mAudioManager?.isValid ?: true && mTactileManager?.isValid ?: true && mVisualManager?.isValid ?: true ){
                    mStimuliHandler.removeCallbacksAndMessages(null)
                    clb()
                }
                else    mStimuliHandler.postDelayed(this, 100)
            }
        }
        mStimuliHandler.post(runTask)  // Start the initial runnable task by posting through the handler
    }

    fun unloadStimuli(){}

    fun getValidAudioManager(manager: iStimulusManager?):iStimulusManager?{

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
    fun getDuration(managerA: iStimulusManager? = null, managerT: iStimulusManager? = null, managerV: iStimulusManager? = null):Triple<Long, Long, Long>?{

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

    // =============================================================================================================================
    // STIMULUS DELIVERY
    // =============================================================================================================================
    // ---------------------------------------------------------------------------------------------
    // PAIRS  (deliverAlignedStimuliPair -> deliverShiftedStimuliPair -> 2 deliverShiftedStimulus)
    // ---------------------------------------------------------------------------------------------
    // deliver a pair of identical aligned stimuli, separated by "isi" ms.
    // calculate delay correction and call the following function
    fun deliverAlignedStimuliPair(isi:Long, type:Int, onEnd:()-> Unit = {}) {
        val corr_delays = delaysAligner.arrangeDelays(type) // here type filters (can set to -1) corr_delays values
        deliverShiftedStimuliPair(isi, type, corr_delays.a, corr_delays.t, corr_delays.v){ onEnd()}
    }

    private fun deliverShiftedStimuliPair(isi:Long, type:Int,
                                          a:Long, t:Long, v:Long,
                                          onEnd:() -> Unit = {}){

        deliverShiftedStimulus(type, a, t, v, 1)
        mStimuliHandler.postDelayed({
            deliverShiftedStimulus(type, a, t, v, 2){ onEnd() }
        }, isi)
    }
    // ---------------------------------------------------------------------------------------------
    // SHIFTED STIMULUS (call 1-to-3 deliverUnimodalStimulus at different latencies, receive already corrected shifting)
    // THIS IS THE ONLY METHOD THAT CALLS UNIMODAL STIMULI !!!!!!!
    // ---------------------------------------------------------------------------------------------
    fun deliverShiftedStimulus(type:Int, a:Long, t:Long, v:Long, id:Int = -1, onEnd:() -> Unit = {}) {

        val unimodal_types  = maintype2unimodaltypes(type)
        val atype           = unimodal_types[0]
        val ttype           = unimodal_types[1]
        val vtype           = unimodal_types[2]

        val durlist         = mutableListOf<Long>()

        val onsetDate           = Date()
//        Log.d(TAG, "${getOnsetDate()}: NEW STIMULUS, reciprocal delays A=$a, T=$t, V=$v")

        try{
            if(a > -1 && atype > -1) {
                if(mAudioManager == null)          throw Exception("error in deliverShiftedStimulus: a valid audio manager was not found")
                if(mAudioManager.type != atype)    throw Exception(ctx.resources.getString(R.string.error_audiomanager))

                durlist.add(mAudioManager.duration + a)

                mStimuliHandler.postDelayed({
//                    val elapsedms1 = getTimeDifference(onsetDate)
                    deliverAStimulus()
//                    val elapsedms2 = getTimeDifference(onsetDate)
//                    Log.d(TAG, "${getOnsetDate()}: audio issued, type=${mAudioManager.type}, onset=$a, elapsedPre=$elapsedms1, elapsedPost=$elapsedms2")
                }, t)

            }

            if(t > -1 && ttype > -1) {
                if(mTactileManager == null)          throw Exception("error in deliverShiftedStimulus: a valid tactile manager was not found")
                if(mTactileManager.type != ttype)    throw Exception(ctx.resources.getString(R.string.error_tactilemanager))

                durlist.add(mTactileManager.duration + t)
                mStimuliHandler.postDelayed({
//                    val elapsedms1 = getTimeDifference(onsetDate)
                    deliverTStimulus()
//                    val elapsedms2 = getTimeDifference(onsetDate)
//                    Log.d(TAG, "${getOnsetDate()}: tactile issued, type=${mTactileManager.type}, onset=$t, elapsedPre=$elapsedms1, elapsedPost=$elapsedms2")
                }, t)
            }

            if(v > -1 && vtype > -1) {
                if(mVisualManager == null)          throw Exception("error in deliverShiftedStimulus: a valid visual manager was not found")
                if(mVisualManager.type != vtype)    throw Exception(ctx.resources.getString(R.string.error_visualmanager))

                durlist.add(mVisualManager.duration + v)
                mStimuliHandler.postDelayed({
//                    val elapsedms1 = getTimeDifference(onsetDate)
                    deliverVStimulus()
//                    val elapsedms2 = getTimeDifference(onsetDate)
//                    Log.d(TAG, "${getOnsetDate()}: visual issued, type=${mVisualManager.type}, onset=$v, elapsedPre=$elapsedms1, elapsedPost=$elapsedms2")
                }, v)
            }

            val end:Long = Collections.max(durlist)
            mStimuliHandler.postDelayed({ onEnd() }, end)
        }
        catch (e:Exception){
            throw Exception(e.message)
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ALIGNED STIMULUS (correct delays and call deliverShiftedStimulus)
    // ---------------------------------------------------------------------------------------------
    fun deliverAlignedStimulus(type:Int, onEnd:()-> Unit = {}){

        val corr_delays = delaysAligner.arrangeDelays(type)
        deliverShiftedStimulus(type, corr_delays.a, corr_delays.t, corr_delays.v){onEnd()}
    }

    // --------------------------------------------------------------------------------------------------------------
    // UNIMODAL STIMULUS
    // here I give the final deliver command, latencies and delays corrections have been already defined
    // --------------------------------------------------------------------------------------------------------------
    fun deliverUnimodalStimulus(type:Int, onEnd:() -> Unit = {}){
        when(type){
            STIM_TYPE_A1, STIM_TYPE_A2, STIM_TYPE_A3 -> deliverAStimulus(onEnd)
            STIM_TYPE_T1, STIM_TYPE_T2 -> deliverTStimulus(onEnd)
            STIM_TYPE_V1, STIM_TYPE_V2 -> deliverVStimulus(onEnd)
        }
    }

    fun deliverAStimulus(onEnd:() -> Unit = {}){
        try {
            if (mAudioManager == null) throw Exception("deliverAStimulus: mAudioManager is null")
            if (!mAudioManager.isValid) throw Exception("deliverAStimulus: mAudioManager is not valid")

            mAudioManager.deliver()
            mStimuliHandler.postDelayed({ onEnd() }, mAudioManager.duration)
        }
        catch (e:Exception){
            throw Exception(e.message)
        }
    }

    fun deliverTStimulus(onEnd:() -> Unit = {}){
        try {
            if(mTactileManager == null)   throw Exception("deliverTStimulus: mTactileManager is null")
            if(!mTactileManager.isValid)  throw Exception("deliverTStimulus: mTactileManager is not valid")

            mTactileManager.deliver()
            mStimuliHandler.postDelayed({ onEnd() }, mTactileManager.duration)
        }
        catch (e:Exception){
            throw Exception(e.message)
        }
    }

    private fun deliverVStimulus(onEnd:() -> Unit = {}){

        try {
            if(mVisualManager == null)   throw Exception("deliverVStimulus: mVisualManager is null")
            if(!mVisualManager.isValid)  throw Exception("deliverVStimulus: mVisualManager is not valid")

            mVisualManager.deliver()
            mStimuliHandler.postDelayed({ onEnd() }, mVisualManager.duration)
        }
        catch (e:Exception){
            throw Exception(e.message)
        }
    }
}