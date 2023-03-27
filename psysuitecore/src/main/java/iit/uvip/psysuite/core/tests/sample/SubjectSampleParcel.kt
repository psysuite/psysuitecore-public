package iit.uvip.psysuite.core.tests.sample

import iit.uvip.psysuite.core.model.Populations
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import iit.uvip.psysuite.core.tests.TestBasic
import kotlinx.parcelize.Parcelize
import org.albaspazio.core.accessory.Device


/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
 */

// base class for all tests
@Parcelize
open class SubjectSampleParcel(

    override var classes: List<String> = listOf("iit.uvip.psysuite.core.tests.sample.TestSample"),
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var population: Int = Populations.POPULATION_TD,
    override var type: Int = -1,

    override var block: Int = -1,
    override var isDebug: Boolean = false,
    override var device: Device? = null,
    override var vercode: Int = -1,
    override var stimuliDelays: DelaysAligner = DelaysAligner(),

    override var nextTrailModality: Int = TestBasic.TEST_NEXTTRIAL_NOCHOOSE,
    override var whitenoise: Int = TestBasic.TEST_SWITCH_CHOOSE_ON,
    override var trman_type: Int = TestBasic.TEST_TRMAN_FIXED,
    override var showResult: Int = TestBasic.TEST_SWITCH_DISABLED,
    override var canRepeat:Int = TestBasic.TEST_SWITCH_DISABLED,


    var stim_sources:Int = 0,       // according to modalities selection

    var audioDuration:Long = 0,     //
    var audioVolume:Int = 100,   //
    var audioResource:String = "",  // resource name (string)

    var visualDuration:Long     = 0,    //
    var visualDrawableOff:Int   = 0,    //
    var visualDrawableOn:Int    = 0,    //

    var tactileAmplitudes:String = "-1",    // array of amplitudes of vibration. default to "-1" ( a.k.a.  MAX_AMPLITUDE (-1))
    var tactileTimings:String = "",         // array of periods (Long)

    var shiftedParams:List<Long> = listOf(0L,0L,0L),
    var pairDistance:Long = 0,

    var repetitions:Int = 1,
    var iti:Long = 1000

) : SubjectBasicParcel(classes, label, age, gender, population, type, block, isDebug, device, vercode, stimuliDelays, nextTrailModality, whitenoise, trman_type, showResult, canRepeat)












