package org.albaspazio.psysuite.core.models

import org.albaspazio.psysuite.core.utils.IdLabelData

/**
 * Defines constants and collections related to different subject populations.
 * This class primarily uses a companion object to provide static access to population identifiers
 * and pre-defined lists of populations grouped by certain characteristics (e.g., sighted, hearing).
 */
class Populations {

    companion object{

        //-----------------------------------------------------------------------------------------
        // POPULATIONS
        //-----------------------------------------------------------------------------------------
        /** Identifier for Typically Developing population. */
        @JvmStatic val POPULATION_TD                    = 0

        /** Identifier for Congenitally Blind population. */
        @JvmStatic val POPULATION_CB                    = 10
        /** Identifier for Late Blind population. */
        @JvmStatic val POPULATION_LB                    = 11
        /** Identifier for Congenital Low Vision population. */
        @JvmStatic val POPULATION_CLV                   = 12
        /** Identifier for Late Low Vision population. */
        @JvmStatic val POPULATION_LLV                   = 13

        /** Identifier for Congenitally Deaf population. */
        @JvmStatic val POPULATION_CD                    = 20
        /** Identifier for Late Deaf population. */
        @JvmStatic val POPULATION_LD                    = 21
        /** Identifier for Congenital Auditory Impairment (Cochlear Implant) population. */
        @JvmStatic val POPULATION_CAI                   = 22
        /** Identifier for Late Auditory Impairment (Cochlear Implant) population. */
        @JvmStatic val POPULATION_LAI                   = 23

        /** Identifier for Attention Deficit Hyperactivity Disorder population. */
        @JvmStatic val POPULATION_ADHD                  = 30

        /** Identifier for Schizophrenia population. */
        @JvmStatic val POPULATION_SK                    = 40
        /** Identifier for Bipolar Disorder population. */
        @JvmStatic val POPULATION_BD                    = 41
        /** Identifier for Borderline Personality Disorder population. */
        @JvmStatic val POPULATION_BL                    = 42


        /** Identifier for Altzeimer Disease. */
        @JvmStatic val POPULATION_AD                    = 50
        /** Identifier for Body Levy degeneration population. */
        @JvmStatic val POPULATION_BLD                   = 51
        /** Identifier for Mild Cognitive Impairment (AD) population. */
        @JvmStatic val POPULATION_MCI_AD                = 52
        /** Identifier for Mild Cognitive Impairment (BLD) population. */
        @JvmStatic val POPULATION_MCI_BLD               = 53
        /** Identifier for Parkinson Disease population. */
        @JvmStatic val POPULATION_PD                    = 54

        /**
         * List of all defined subject populations.
         * Each entry is an [IdLabelData] object containing a label and its corresponding integer ID.
         */
        @JvmStatic val all_populations:List<IdLabelData> = listOf(
            IdLabelData("TD", POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD),
            IdLabelData("SK", POPULATION_SK),
            IdLabelData("BD", POPULATION_BD),
            IdLabelData("BL", POPULATION_BL),

            IdLabelData("CB", POPULATION_CB),
            IdLabelData("LB", POPULATION_LB),
            IdLabelData("CLV", POPULATION_CLV),
            IdLabelData("LLV", POPULATION_LLV),

            IdLabelData("CD", POPULATION_CD),
            IdLabelData("LD", POPULATION_LD),
            IdLabelData("CAI", POPULATION_CAI),
            IdLabelData("LAI", POPULATION_LAI),

            IdLabelData("AD", POPULATION_AD),
            IdLabelData("BLD", POPULATION_BLD),
            IdLabelData("MCI_AD", POPULATION_MCI_AD),
            IdLabelData("MCI_BLD", POPULATION_MCI_BLD),
            IdLabelData("PD", POPULATION_PD)
        )

        /**
         * List of populations that are typically sighted and hearing.
         */
        @JvmStatic val sighted_hearing_populations:List<IdLabelData> = listOf(
            IdLabelData("TD", POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD),
            IdLabelData("SK", POPULATION_SK),
            IdLabelData("BD", POPULATION_BD),
            IdLabelData("BL", POPULATION_BL),
            IdLabelData("AD", POPULATION_AD),
            IdLabelData("BLD", POPULATION_BLD),
            IdLabelData("MCI_AD", POPULATION_MCI_AD),
            IdLabelData("MCI_BLD", POPULATION_MCI_BLD)
        )

        /**
         * List of populations that are sighted and can perform visual tasks.
         */
        @JvmStatic val sighted_populations:List<IdLabelData> = listOf(
            IdLabelData("TD", POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD),
            IdLabelData("SK", POPULATION_SK),
            IdLabelData("BD", POPULATION_BD),
            IdLabelData("BL", POPULATION_BL),

            IdLabelData("CD", POPULATION_CD),
            IdLabelData("LD", POPULATION_LD),
            IdLabelData("CAI", POPULATION_CAI),
            IdLabelData("LAI", POPULATION_LAI),

            IdLabelData("AD", POPULATION_AD),
            IdLabelData("BLD", POPULATION_BLD),
            IdLabelData("MCI_AD", POPULATION_MCI_AD),
            IdLabelData("MCI_BLD", POPULATION_MCI_BLD)
        )

        /**
         * List of populations that are hearing and can perform acoustic tasks.
         */
        @JvmStatic val hearing_populations:List<IdLabelData> = listOf(
            IdLabelData("TD", POPULATION_TD),
            IdLabelData("ADHD", POPULATION_ADHD),
            IdLabelData("SK", POPULATION_SK),
            IdLabelData("BD", POPULATION_BD),
            IdLabelData("BL", POPULATION_BL),

            IdLabelData("CB", POPULATION_CB),
            IdLabelData("LB", POPULATION_LB),
            IdLabelData("CLV", POPULATION_CLV),
            IdLabelData("LLV", POPULATION_LLV),

            IdLabelData("AD", POPULATION_AD),
            IdLabelData("BLD", POPULATION_BLD),
            IdLabelData("MCI_AD", POPULATION_MCI_AD),
            IdLabelData("MCI_BLD", POPULATION_MCI_BLD)

        )

        /**
         * List of populations with visual impairments (cannot perform visual tasks reliably).
         */
        @JvmStatic val vi_populations:List<IdLabelData> = listOf(
            IdLabelData("CB", POPULATION_CB),
            IdLabelData("LB", POPULATION_LB),
            IdLabelData("CLV", POPULATION_CLV),
            IdLabelData("LLV", POPULATION_LLV)
        )

        /**
         * List of populations with auditory impairments (cannot perform acoustic tasks reliably).
         */
        @JvmStatic val ai_populations:List<IdLabelData> = listOf(
            IdLabelData("CD", POPULATION_CD),
            IdLabelData("LD", POPULATION_LD),
            IdLabelData("CAI", POPULATION_CAI),
            IdLabelData("LAI", POPULATION_LAI)
        )
    }
}