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

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        when((spCondition.selectedItem as ConditionData).id){

            TestBasic.TEST_TVB_TIME_INF,
            TestBasic.TEST_ATB_TIME_INF,
            TestBasic.TEST_AVB_TIME_INF -> {
                swInteractive.visibility = View.VISIBLE
                labInteractive.visibility = View.VISIBLE
                if (subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_AUTO || subject.nextTrailModality == TestBasic.TEST_NEXTTRIAL_BUTTON) {
                    swInteractive?.isChecked = false
                    subject.nextTrailModality = TestBasic.TEST_NEXTTRIAL_AUTO
                }
            }
            else -> {
                swInteractive.visibility = View.GONE
                labInteractive.visibility = View.GONE
            }
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun updateSubject(): SubjectBasicParcel {

        subject = super.updateSubject()

        subject.nextTrailModality = when(subject.type) {                // could choose whether pausing each trial
            TestBasic.TEST_AVB_TIME_INF,
            TestBasic.TEST_TVB_TIME_INF,
            TestBasic.TEST_ATB_TIME_INF         ->  if(swInteractive.isChecked) TestBasic.TEST_NEXTTRIAL_BUTTON
                                                    else                        TestBasic.TEST_NEXTTRIAL_AUTO


            else                                ->   subject.nextTrailModality
        }


        if(subject.type == TestBasic.TEST_ATVB_TIME_S_BAL || subject.type == TestBasic.TEST_ATVB_TIME_S_BAL2)
            subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB",
                                            "iit.uvip.psysuite.core.ui.fragments.answers.ThreeAFCAnswerDialogFragment")

        return subject
    }
}