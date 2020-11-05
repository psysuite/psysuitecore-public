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

        @JvmStatic val all_populations:List<IdLabelData> = listOf(
            IdLabelData("TD",   POPULATION_TD),
            IdLabelData("CB",   POPULATION_CB),
            IdLabelData("LB",   POPULATION_LB),
            IdLabelData("CLV",  POPULATION_CLV),
            IdLabelData("LLV",  POPULATION_LLV),
            IdLabelData("CD",   POPULATION_CD),
            IdLabelData("LD",   POPULATION_LD),
            IdLabelData("CAI",  POPULATION_CAI),
            IdLabelData("LAI",  POPULATION_LAI),
            IdLabelData("ADHD", POPULATION_ADHD)
        )

        @JvmStatic val sighted_hearing_populations:List<IdLabelData> = listOf(
            IdLabelData("TD",   POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD)
        )

        // can do visual tasks
        @JvmStatic val sighted_populations:List<IdLabelData> = listOf(
            IdLabelData("TD",   POPULATION_TD),
            IdLabelData("CD",   POPULATION_CD),
            IdLabelData("LD",   POPULATION_LD),
            IdLabelData("CAI",  POPULATION_CAI),
            IdLabelData("LAI",  POPULATION_LAI),
            IdLabelData("ADHD", POPULATION_ADHD)
        )

        // can do acoustic tasks
        @JvmStatic val hearing_populations:List<IdLabelData> = listOf(
            IdLabelData("TD",   POPULATION_TD),
            IdLabelData("CB",   POPULATION_CB),
            IdLabelData("LB",   POPULATION_LB),
            IdLabelData("CLV",  POPULATION_CLV),
            IdLabelData("LLV",  POPULATION_LLV),
            IdLabelData("ADHD", POPULATION_ADHD)
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