package iit.uvip.psysuite.core.tests.temporalbinding

class BindingsConstants {

    companion object {

        @JvmStatic val balshSD:List<Pair<Float, String>> = listOf(
            Pair(200.0F,    "200"),
            Pair(250.0F,    "250"),
            Pair(300.0F,    "300"))

        @JvmStatic val unbalSD:List<Pair<Float, String>> = listOf(
            Pair(50.0F,     "50"),
            Pair(100.0F,    "100"),
            Pair(200.0F,    "200"),
            Pair(300.0F,    "300"),
            Pair(400.0F,    "400"),
            Pair(800.0F,    "800"),
            Pair(1200.0F,   "1200"))

        @JvmStatic val WN_FIRSTSTIM_INTERVAL   = 1000L
        @JvmStatic val STIM_DURATION_INF       = 1000L
        @JvmStatic val STIM_DURATION_TOD       = 200L
        @JvmStatic val STIM_DURATION           = 50L
        @JvmStatic val ISI                     = 1000L
        @JvmStatic val ISI_INF                 = 2000L // distance between stimuli onsets

        // unimodal
        @JvmStatic val TYPE_A      = 1
        @JvmStatic val TYPE_T      = 2
        @JvmStatic val TYPE_V      = 3

        // bimodal
        @JvmStatic val TYPE_AT     = 12
        @JvmStatic val TYPE_AV     = 13
        @JvmStatic val TYPE_TV     = 23

        @JvmStatic val TYPE_A_V    = 103
        @JvmStatic val TYPE_V_A    = 301

        @JvmStatic val TYPE_T_V    = 203
        @JvmStatic val TYPE_V_T    = 302

        @JvmStatic val TYPE_A_T    = 102
        @JvmStatic val TYPE_T_A    = 201

        @JvmStatic val STIM_TYPE_TIME_A800_T   = 2018
        @JvmStatic val STIM_TYPE_TIME_A_T800   = 1028

        @JvmStatic val STIM_TYPE_TIME_T_V800   = 2038
        @JvmStatic val STIM_TYPE_TIME_T800_V   = 3028

        // trimodal
        @JvmStatic val TYPE_ATV  = 123

        @JvmStatic val TYPE_A_TV = 1023
        @JvmStatic val TYPE_TV_A = 2301
        @JvmStatic val TYPE_V_AT = 3012
        @JvmStatic val TYPE_AT_V = 1203
        @JvmStatic val TYPE_T_AV = 2013
        @JvmStatic val TYPE_AV_T = 1302

        @JvmStatic val TYPE_T_A_V  = 20103
        @JvmStatic val TYPE_V_A_T  = 30102
        @JvmStatic val TYPE_A_T_V  = 10203
        @JvmStatic val TYPE_V_T_A  = 30201
        @JvmStatic val TYPE_A_V_T  = 10302
        @JvmStatic val TYPE_T_V_A  = 20301

    }
}



/*
        // ATVB
        @JvmStatic val TYPE_ATV  = 0

        @JvmStatic val TYPE_A_TV = 1
        @JvmStatic val TYPE_TV_A = 2
        @JvmStatic val TYPE_V_AT = 3
        @JvmStatic val TYPE_AT_V = 4
        @JvmStatic val TYPE_T_AV = 5
        @JvmStatic val TYPE_AV_T = 6

        @JvmStatic val TYPE_T_A_V  = 10
        @JvmStatic val TYPE_V_A_T  = 11
        @JvmStatic val TYPE_A_T_V  = 12
        @JvmStatic val TYPE_V_T_A  = 13
        @JvmStatic val TYPE_A_V_T  = 14
        @JvmStatic val TYPE_T_V_A  = 15

        // AV
        @JvmStatic val TYPE_AV     = 0
        @JvmStatic val TYPE_A      = 1
        @JvmStatic val TYPE_V      = 2
        @JvmStatic val TYPE_A_V    = 3
        @JvmStatic val TYPE_V_A    = 4

        // TV
        @JvmStatic val TYPE_TV     = 0
        @JvmStatic val TYPE_T      = 1
        @JvmStatic val TYPE_V      = 2
        @JvmStatic val TYPE_T_V    = 3
        @JvmStatic val TYPE_V_T    = 4

        // AT
        @JvmStatic val TYPE_AT     = 0
        @JvmStatic val TYPE_A      = 1
        @JvmStatic val TYPE_T      = 2
        @JvmStatic val TYPE_A_T    = 3
        @JvmStatic val TYPE_T_A    = 4
 */