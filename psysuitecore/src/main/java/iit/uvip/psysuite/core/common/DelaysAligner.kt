package iit.uvip.psysuite.core.common

import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*
import java.util.Collections.max


// input params correspond to reciprocal 
@Suppress("CanBeParameter")
@Parcelize
class DelaysAligner(val a1:Long=0L, val a2:Long=0L, val a3:Long=0L, val t1:Long=0L, val t2:Long=0L, val v1:Long=0L, val v2:Long=0L) : Parcelable {

    @IgnoredOnParcel
    private val delays:HashMap<Int, StimuliDelay> = hashMapOf(
    TestBasic.STIM_TYPE_A1                 to StimuliDelay(a1, -1, -1),
    TestBasic.STIM_TYPE_A2                 to StimuliDelay(a2, -1, -1),
    TestBasic.STIM_TYPE_A3                 to StimuliDelay(a3, -1, -1),
    TestBasic.STIM_TYPE_T1                 to StimuliDelay(-1, t1, -1),
    TestBasic.STIM_TYPE_T2                 to StimuliDelay(-1, t2, -1),
    TestBasic.STIM_TYPE_V1                 to StimuliDelay(-1, -1, v1),
    TestBasic.STIM_TYPE_V2                 to StimuliDelay(-1, -1, v2),

    TestBasic.STIM_TYPE_A1T1               to StimuliDelay(a1, t1, -1),
    TestBasic.STIM_TYPE_A1T2               to StimuliDelay(a1, t2, -1),
    TestBasic.STIM_TYPE_A1V1               to StimuliDelay(a1, -1, v1),
    TestBasic.STIM_TYPE_A1V2               to StimuliDelay(a1, -1, v2),
    TestBasic.STIM_TYPE_A2T1               to StimuliDelay(a2, t1, -1),
    TestBasic.STIM_TYPE_A2T2               to StimuliDelay(a2, t2, -1),
    TestBasic.STIM_TYPE_A2V1               to StimuliDelay(a2, -1, v1),
    TestBasic.STIM_TYPE_A2V2               to StimuliDelay(a2, -1, v2),
    TestBasic.STIM_TYPE_A3T1               to StimuliDelay(a3, t1, -1),
    TestBasic.STIM_TYPE_A3T2               to StimuliDelay(a3, t2, -1),
    TestBasic.STIM_TYPE_A3V1               to StimuliDelay(a3, -1, v1),
    TestBasic.STIM_TYPE_A3V2               to StimuliDelay(a3, -1, v2),
    TestBasic.STIM_TYPE_T1V1               to StimuliDelay(-1, t1, v1),
    TestBasic.STIM_TYPE_T2V1               to StimuliDelay(-1, t2, v1),
    TestBasic.STIM_TYPE_T1V2               to StimuliDelay(-1, t1, v2),
    TestBasic.STIM_TYPE_T2V2               to StimuliDelay(-1, t2, v2),

    TestBasic.STIM_TYPE_A1T1V1             to StimuliDelay(a1, t1, v1),
    TestBasic.STIM_TYPE_A1T2V1             to StimuliDelay(a1, t2, v1),
    TestBasic.STIM_TYPE_A1T1V2             to StimuliDelay(a1, t1, v2),
    TestBasic.STIM_TYPE_A1T2V2             to StimuliDelay(a1, t2, v2),
    TestBasic.STIM_TYPE_A2T1V1             to StimuliDelay(a2, t1, v1),
    TestBasic.STIM_TYPE_A2T2V1             to StimuliDelay(a2, t2, v1),
    TestBasic.STIM_TYPE_A2T1V2             to StimuliDelay(a2, t1, v2),
    TestBasic.STIM_TYPE_A2T2V2             to StimuliDelay(a2, t2, v2),
    TestBasic.STIM_TYPE_A3T1V1             to StimuliDelay(a3, t1, v1),
    TestBasic.STIM_TYPE_A3T2V1             to StimuliDelay(a3, t2, v1),
    TestBasic.STIM_TYPE_A3T1V2             to StimuliDelay(a3, t1, v2),
    TestBasic.STIM_TYPE_A3T2V2             to StimuliDelay(a3, t2, v2))

    fun getStimuliDelay(stim_type:Int)= delays[stim_type] ?: StimuliDelay()

    // subtract system delay corrections (all positive) to given trial delays (all positive).
    // calculate min latency after correction, if negative => shift all latencies forward and report it
    // v 0.9.5.1 added type check in case delays were not set to -1. e.g. when calling deliverAlignedStimuliPair
    // a/t/v type contain the unimodal code or -1
    fun arrangeDelays(type:Int=0, a:Long, t:Long, v:Long):CorrectedStimuliDelay{

        val types = TestBasic.maintype2unimodaltypes(type)  // set -1 if modality is not in the type
        val atype = types[0]
        val ttype = types[1]
        val vtype = types[2]

        val stim_delay = delays[type] ?: StimuliDelay()

        val deltas = mutableListOf(-1L, -1L, -1L)
        var minimum = 1000000L

        if(a != -1L && atype != -1){
            deltas[0] = a - stim_delay.a
            minimum = deltas[0]
        }
        if(t != -1L && ttype != -1){
            deltas[1] = t - stim_delay.t
            minimum = Collections.min(listOf(minimum, deltas[1]))
        }
        if(v != -1L && vtype != -1){
            deltas[2] = v - stim_delay.v
            minimum = Collections.min(listOf(minimum, deltas[2]))
        }

        val corr_delays = CorrectedStimuliDelay()   // by default all set to -1

        if(minimum ==  1000000L){
            Log.e("TestBasic", "Error in arrangeDelays: none of delays/types were valid")
            return corr_delays
        }

        if(minimum < 0){
            corr_delays.a = if (a != -1L && atype != -1) deltas[0] - minimum else -1
            corr_delays.t = if (t != -1L && ttype != -1) deltas[1] - minimum else -1
            corr_delays.v = if (v != -1L && vtype != -1) deltas[2] - minimum else -1
            corr_delays.shift = -minimum
        }
        else {
            corr_delays.a = if (a != -1L && atype != -1) deltas[0] else -1
            corr_delays.t = if (t != -1L && ttype != -1) deltas[1] else -1
            corr_delays.v = if (v != -1L && vtype != -1) deltas[2] else -1
            corr_delays.shift = 0
        }
        return corr_delays
    }

    fun arrangeDelays(type:Int=0):CorrectedStimuliDelay{

        val corr_delays = CorrectedStimuliDelay()   // by default all set to -1

        val types = TestBasic.maintype2unimodaltypes(type)  // set -1 if modality is not in the type
        val atype = types[0]
        val ttype = types[1]
        val vtype = types[2]

        val stim_delay = delays[type] ?: StimuliDelay()
        val ml = mutableListOf<Long>()

        // calculate max considering only modalities contained within given type.
        if(atype != -1) ml.add(stim_delay.a)
        if(ttype != -1) ml.add(stim_delay.t)
        if(vtype != -1) ml.add(stim_delay.v)
        val max = max(ml)

        corr_delays.a = if (atype != -1) max - stim_delay.a else -1
        corr_delays.t = if (ttype != -1) max - stim_delay.t else -1
        corr_delays.v = if (vtype != -1) max - stim_delay.v else -1
        corr_delays.shift = max

        return corr_delays
    }

    fun getShift(type:Int, a:Long, t:Long, v:Long):Long{
        return arrangeDelays(type, a, t, v).shift
    }
}