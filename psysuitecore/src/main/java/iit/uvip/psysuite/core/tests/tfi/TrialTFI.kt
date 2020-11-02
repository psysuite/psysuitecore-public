package iit.uvip.psysuite.core.tests.tfi

import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TrialBasic

/*
    the answer is represented by a comma-separated string: e.g. 1,0,2
    corresponding to the number of audio/tactile/visual stimuli present in the trial.
*/

class TrialTFI(id:Int=-1, type:Int, label:String, corr_answer:String, val soa:Long)
    : TrialBasic(id,type,label, corr_answer){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tsoa\tres\tcor_ans\tuser_ans\telapsed\n"
        val A: Int = 0
        val T: Int = 1
        val V: Int = 2
    }
    val stims:MutableList<Int> = mutableListOf(0,0,0)

    init{
        processModalities(corr_answer.split(","))
    }

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\t" + label + "\t" + soa + "\t" + success.toString() + "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + label + "\t" + soa.toString() + "\t"+ success.toString() + "\t" + correct_answer + "\t" + user_answer + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\n"
    }

    override fun debugInfo():String{
        return "${super.debugInfo()}, soa=$soa"
    }

    // e.g. codes = [2,2,2]
    private fun processModalities(codes:List<String>){
        // e.g.   1,2,2  =>  stims[V2T1, A1, V2T1]
        //        0,1,2  =>  stims[V2, T1, V2]
        //        1,1,2  =>  stims[V2, A1T1, V2]

        //              0:a, 1:t, 2:v      never, only second, first & third
        codes.mapIndexed { modality, occurrence ->
            when(occurrence.toInt()){
                1 -> {
                    when(modality){
                        0 ->    stims[1] = stims[1] or TestTFI.UNIMODAL_AUDIO_CODE
                        1 ->    stims[1] = stims[1] or TestBasic.STIM_TYPE_T1
                        2 ->    stims[1] = stims[1] or TestBasic.STIM_TYPE_V2
                    }
                }
                2 -> {
                    when(modality){
                        0 ->    {
                            stims[0] = stims[0] or TestTFI.UNIMODAL_AUDIO_CODE
                            stims[2] = stims[2] or TestTFI.UNIMODAL_AUDIO_CODE
                        }
                        1 ->    {
                            stims[0] = stims[0] or TestBasic.STIM_TYPE_T1
                            stims[2] = stims[2] or TestBasic.STIM_TYPE_T1
                        }
                        2 ->    {
                            stims[0] = stims[0] or TestBasic.STIM_TYPE_V2
                            stims[2] = stims[2] or TestBasic.STIM_TYPE_V2
                        }
                    }
                }
            }
        }



//        when(codes[0].toInt()){
//            1 ->    stims[1] = A
//            2 -> {
//                    stims[0] = A
//                    stims[2] = A
//            }
//        }
//        when(codes[1].toInt()){
//            1 ->    stims[1] = T
//            2 -> {
//                    stims[0] = T
//                    stims[2] = T
//            }
//        }
//        when(codes[2].toInt()){
//            1 ->    stims[1] = V
//            2 -> {
//                    stims[0] = V
//                    stims[2] = V
//            }
//        }
    }
}
