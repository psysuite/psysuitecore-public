package iit.uvip.psysuite.core.model.preferences

import android.os.Parcelable
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import kotlinx.parcelize.Parcelize

/**
 * A data class representing project-level preferences, primarily for stimuli delays and an email address.
 * It implements [Parcelable] to allow instances to be passed between Android components (e.g., in Intents or Bundles).
 *
 * @property delaysAligner A [DelaysAligner] object containing various delay settings for stimuli.
 * @property email An optional email address associated with the project preferences. Defaults to an empty string.
 */
@Parcelize
data class ProjectPreferences(val delaysAligner: DelaysAligner, val email:String="") : Parcelable
