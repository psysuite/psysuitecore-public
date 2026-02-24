package iit.uvip.psysuite.core.tests.ttc

import android.util.Log
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.trials.TrialBasic
import org.albaspazio.core.accessory.round
import org.albaspazio.psysuite.adaptive.ado.ADOWrapper
import kotlin.math.round

// speed in px per second
// distance in px
// magnitude is the distance from correctTime = dist/speed (e.g. 250),
// stim_value is the temporal distance from start (e.g. 1000-magnitude=750ms) when target must disappear (VT)
class TrialTTC(id:Int=-1, type:Int, label:String,
                    override var magnitude:Float,
                    time:Long,
                    distance:Int,
                    val minMagnitude:Float,
                    val imageId:Int, val isHoriz:Boolean=true, val isDownRight:Boolean=true, adoWrapper: ADOWrapper?=null): TrialBasic(id, type, label, adoWrapper =adoWrapper) {
    var TT:Long = 0
    var VT:Long = 0
    var IT:Long = 0

    var TPL:Int = 0
    var VPL:Int = 0
    var IPL:Int = 0

    var SP:Double = 0.0

    var isCatch:Boolean = false

    companion object {
        @JvmStatic val LOG_HEADER = "id\tlabel\tvis_time\tisHor\tis_dr\tres\tspeed\tdist\ttime\timageid\n"
    }

    init {
        when(type){
            TestBasic.TEST_MOTPRE_VH_VARSPEED_FIXVT  -> {
                VT  = time
                IPL = distance
            }
            TestBasic.TEST_MOTPRE_VH_VARSPEED_FIXVPL -> {
                IT  = time
                VPL = distance
            }
            else -> {    // TestBasic.TEST_MOTPRE_VH_FIXSPEED
                TT  = time
                TPL = distance
            }
        }
        setupTrial(magnitude)
    }

    override fun setupTrial(newvalue:Float):Long {
        magnitude       = newvalue

        when(type){
            TestBasic.TEST_MOTPRE_VH_VARSPEED_FIXVT  -> {
                SP  = (magnitude+minMagnitude).toDouble()
                VPL = round(SP*VT).toInt()
                IT  = round(IPL/SP).toLong()

                TPL = VPL + IPL
                TT  = VT + IT
            }
            TestBasic.TEST_MOTPRE_VH_VARSPEED_FIXVPL -> {
                SP  = (magnitude+minMagnitude).toDouble()
                IPL = round(SP*IT).toInt()
                VT  = round(VPL/SP).toLong()

                TPL = VPL + IPL
                TT  = VT + IT
            }
            else -> {    // TestBasic.TEST_MOTPRE_VH_FIXSPEED
                SP  = TPL/TT.toDouble()
                VPL = ((magnitude+minMagnitude)*TPL).toInt()
                VT  = ((magnitude+minMagnitude)*TT).toLong()

                IPL = TPL - VPL
                IT  = TT - VT
            }
        }
        isCatch = (VPL == TPL)

        correct_answer  = 0
        return stim_value
    }

    override val stim_value: Long
        get() = VT

    // success is true if the present error is smaller than the previous one
    // if first trial, success is always true
    override fun setResponse(result:Int, elapsedms:Long, prev_tr: TrialBasic?, extra_text:String) {
        user_answer         = (result - TT).toInt()
        prev_trial          = prev_tr

//        var delta           = 0
        success = if(prev_tr!= null){
//                    if(prev_tr.isADA && isADA){
            val delta = user_answer - prev_tr.user_answer
            val succ = (user_answer <= prev_tr.user_answer)
            Log.d("TrialTTC", "--------------------------------------------")
            Log.d("TrialTTC", "delta=$delta,  success=$succ, curr_error=$user_answer, prev_error=${prev_tr.user_answer}, ")
            Log.d("TrialTTC", "magn=${magnitude}, vpl=$VPL,  ipl=$IPL")
            succ
//                    }
//                    else true
        }
        else    true
//        success             =   if(prev_tr != null) {
//                                }else{
//                                    delta = 0
//                                    true
//                                }
        user_answer_extra   = extra_text
    }

    fun toStringShort():String{
        return "sval=$stim_value\t,error=$user_answer\t, vel=${SP.round(3)}\t, ipl=$IPL"
    }

    // data exported to log file
    override fun Log():String{
        return "$id\t$label\t$stim_value\t$isHoriz\t$isDownRight\t$user_answer\t${SP.round(3)}\t$TPL\t$TT\t$imageId"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, pos=$stim_value, is_oriz=$isHoriz, is_down_right=$isDownRight"
    }
}
