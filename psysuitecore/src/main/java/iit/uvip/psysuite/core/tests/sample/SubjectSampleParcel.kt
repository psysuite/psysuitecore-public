package iit.uvip.psysuite.core.tests.sample

import iit.uvip.psysuite.core.common.StimuliDelay
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.Device


/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
 */

// base class for all tests
@Parcelize
open class SubjectSampleParcel(
    override var type: Int              = -1,
    override var label: String          = "",
    override var age: Int               = -1,
    override var gender: Int            = -1,
    override var nextTrailModality: Int = -1,
    override var canRecordAudio:Boolean = false,
    override var testClass:String       = "",
    override var device: Device?        = null,
    override var block:Int = -1,
    override var stimuliDelay: StimuliDelay = StimuliDelay(),

    var stim_sources:Int = 0,       // according to modalities selection

    var audioDuration:Long = 0,     //
    var audioVolume:Float = 100F,   //
    var audioResource:String = "",  // resource name (string)

    var visualDuration:Long     = 0,    //
    var visualDrawableOff:Int   = 0,    //
    var visualDrawableOn:Int    = 0,    //

    var tactileAmplitude:Int = -1,   // amplitude of vibration. default to MAX_AMPLITUDE (-1)
    var tactileSequence:String = "", // array of off-set, onset or duration (Long)

    var shiftedParams:List<Long> = listOf(0L,0L,0L),
    var pairDistance:Long = 0,

    var repetitions:Int = 1,
    var iti:Long = 1000

) : SubjectBasicParcel(type, label, age, gender, nextTrailModality, canRecordAudio, testClass, device, block, stimuliDelay)












