package iit.uvip.psysuite.core.model.preferences

import android.os.Parcelable
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProjectPreferences(val delaysAligner: DelaysAligner, val email:String="") : Parcelable
