package iit.uvip.psysuite.core.model

import iit.uvip.psysuite.core.utility.IdLabelData

class Populations {

    companion object{

        //-----------------------------------------------------------------------------------------
        // POPULATIONS
        //-----------------------------------------------------------------------------------------
        @JvmStatic val POPULATION_TD                    = 0

        @JvmStatic val POPULATION_CB                    = 10
        @JvmStatic val POPULATION_LB                    = 11
        @JvmStatic val POPULATION_CLV                   = 12
        @JvmStatic val POPULATION_LLV                   = 13

        @JvmStatic val POPULATION_CD                    = 20
        @JvmStatic val POPULATION_LD                    = 21
        @JvmStatic val POPULATION_CAI                   = 22
        @JvmStatic val POPULATION_LAI                   = 23

        @JvmStatic val POPULATION_ADHD                  = 30

        @JvmStatic val POPULATION_SK                    = 40
        @JvmStatic val POPULATION_BD                    = 41
        @JvmStatic val POPULATION_BL                    = 42

        @JvmStatic val all_populations:List<IdLabelData> = listOf(
            IdLabelData("TD",   POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD),
            IdLabelData("SK",   POPULATION_SK),
            IdLabelData("BD",   POPULATION_BD),
            IdLabelData("BL",   POPULATION_BL),

            IdLabelData("CB",   POPULATION_CB),
            IdLabelData("LB",   POPULATION_LB),
            IdLabelData("CLV",  POPULATION_CLV),
            IdLabelData("LLV",  POPULATION_LLV),

            IdLabelData("CD",   POPULATION_CD),
            IdLabelData("LD",   POPULATION_LD),
            IdLabelData("CAI",  POPULATION_CAI),
            IdLabelData("LAI",  POPULATION_LAI)

        )

        @JvmStatic val sighted_hearing_populations:List<IdLabelData> = listOf(
            IdLabelData("TD",   POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD),
            IdLabelData("SK",   POPULATION_SK),
            IdLabelData("BD",   POPULATION_BD),
            IdLabelData("BL",   POPULATION_BL)
        )

        // can do visual tasks
        @JvmStatic val sighted_populations:List<IdLabelData> = listOf(
            IdLabelData("TD",   POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD),
            IdLabelData("SK",   POPULATION_SK),
            IdLabelData("BD",   POPULATION_BD),
            IdLabelData("BL",   POPULATION_BL),

            IdLabelData("CD",   POPULATION_CD),
            IdLabelData("LD",   POPULATION_LD),
            IdLabelData("CAI",  POPULATION_CAI),
            IdLabelData("LAI",  POPULATION_LAI)
        )

        // can do acoustic tasks
        @JvmStatic val hearing_populations:List<IdLabelData> = listOf(
            IdLabelData("TD",   POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD),
            IdLabelData("SK",   POPULATION_SK),
            IdLabelData("BD",   POPULATION_BD),
            IdLabelData("BL",   POPULATION_BL),

            IdLabelData("CB",   POPULATION_CB),
            IdLabelData("LB",   POPULATION_LB),
            IdLabelData("CLV",  POPULATION_CLV),
            IdLabelData("LLV",  POPULATION_LLV)

        )

        // cannot do visual tasks
        @JvmStatic val vi_populations:List<IdLabelData> = listOf(
            IdLabelData("CB",   POPULATION_CB),
            IdLabelData("LB",   POPULATION_LB),
            IdLabelData("CLV",  POPULATION_CLV),
            IdLabelData("LLV",  POPULATION_LLV)
        )

        // cannot do acoustic tasks
        @JvmStatic val ai_populations:List<IdLabelData> = listOf(
            IdLabelData("CD",   POPULATION_CD),
            IdLabelData("LD",   POPULATION_LD),
            IdLabelData("CAI",  POPULATION_CAI),
            IdLabelData("LAI",  POPULATION_LAI)
        )
    }
}