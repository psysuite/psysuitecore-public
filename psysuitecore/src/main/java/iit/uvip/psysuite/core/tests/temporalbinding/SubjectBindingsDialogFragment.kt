package iit.uvip.psysuite.core.tests.temporalbinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.ui.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.utility.ConditionData
import kotlinx.android.synthetic.main.fragment_subject_info_basic.*

// add whitenoise check button
class SubjectBindingsDialogFragment : SubjectBasicDialogFragment(), AdapterView.OnItemSelectedListener
{
    override val LOG_TAG: String = SubjectBindingsDialogFragment::class.java.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_basic, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spCondition.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        when((spCondition.selectedItem as ConditionData).id){
             TestBasic.TEST_ATB_TIME_DOUBLESTIM,
             TestBasic.TEST_ATB_TIME_SINGLESTIM,
             TestBasic.TEST_ATVB_TIME_D_UNBAL,
             TestBasic.TEST_ATVB_TIME_D_BAL,
             TestBasic.TEST_ATVB_TIME_S_UNBAL,
             TestBasic.TEST_ATVB_TIME_S_BAL    -> {
                swInteractive.visibility = View.GONE
                labInteractive.visibility = View.GONE
             }
             else -> {
                swInteractive.visibility = View.VISIBLE
                labInteractive.visibility = View.VISIBLE
                if (subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_AUTO || subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_BUTTON) {
                    swInteractive?.isChecked = false
                    subject.nextTrailModality = TestBasic.TEST_NEXTTRIAL_AUTO
                }
            }
        }
    }

    override fun updateSubject(): SubjectBasicParcel {

        subject = super.updateSubject()

        subject.nextTrailModality = when(subject.type) {                // could choose whether pausing each trial
            TestBasic.TEST_ATB_TIME_INF         ->  if(swInteractive.isChecked) TestBasic.TEST_NEXTTRIAL_BUTTON
                                                    else                        TestBasic.TEST_NEXTTRIAL_AUTO

            TestBasic.TEST_ATB_TIME_SINGLESTIM,
            TestBasic.TEST_ATB_TIME_DOUBLESTIM,
            TestBasic.TEST_ATVB_TIME_D_UNBAL,
            TestBasic.TEST_ATVB_TIME_D_BAL,
            TestBasic.TEST_ATVB_TIME_S_UNBAL,
            TestBasic.TEST_ATVB_TIME_S_BAL ->  if(subject.canRecordAudio)  TestBasic.TEST_NEXTTRIAL_ANSWER //TEST_NEXTTRIAL_VOICE_NORMAL_ANSWER
                                               else                        TestBasic.TEST_NEXTTRIAL_ANSWER

            else                                ->   subject.nextTrailModality
        }

        return subject
    }
}