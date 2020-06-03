package iit.uvip.psysuite.core.tests.temporalbinding.atb

import android.content.Context
import android.os.Build
import android.os.Parcel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.existFile
import org.albaspazio.core.accessory.readText
import org.albaspazio.core.accessory.saveText

/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
 */

// base class for all tests
@Parcelize
open class SubjectATBParcel(
    override var type: Int = -1,
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var nextTrailModality: Int = -1,
    override var taskcodes: List<TaskCode> = listOf(),
    var whitenoise: Boolean = true
) : SubjectBasicParcel(type, label, age, gender, nextTrailModality, taskcodes) {

    private constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        listOf<TaskCode>().apply {
            if (Build.VERSION.SDK_INT >= 29) parcel.readParcelableList(
                this,
                TaskCode::class.java.classLoader
            )
            else parcel.readList(this, TaskCode::class.java.classLoader)
        },
        parcel.readInt() > 0
    )

    companion object : Parceler<SubjectATBParcel> {


        override fun SubjectATBParcel.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(type)
            parcel.writeString(label)
            parcel.writeInt(age)
            parcel.writeInt(gender)
            parcel.writeInt(nextTrailModality)
            if (Build.VERSION.SDK_INT >= 29) parcel.writeParcelableList(taskcodes, flags)
            else parcel.writeList(taskcodes)

            if (whitenoise) parcel.writeInt(1)
            else parcel.writeInt(0)
        }

        override fun create(parcel: Parcel) = SubjectATBParcel(parcel)

        private fun loadJsonText(jsontext: String): SubjectATBParcel {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(SubjectATBParcel::class.java)
            return jsonAdapter.fromJson(jsontext)!!
        }

        fun loadSubject(): SubjectATBParcel {
            val subj = existFile(CURR_SUBJ_FILE + TestBasic.FILE_EXTENSION)
            if (subj.first) {
                val jsontext = readText(CURR_SUBJ_FILE + TestBasic.FILE_EXTENSION)
                return try {
                    loadJsonText(jsontext)
                } catch (e: Exception) {
                    SubjectATBParcel()
                }
            }
            return SubjectATBParcel()
        }

    }

    // =============================================================================================================
    // WRITE
    // =============================================================================================================
    override fun writeJson(context: Context, filename: String) {

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(SubjectATBParcel::class.java)

        return try {
            val json_subject = jsonAdapter.toJson(this)
            saveText(context, filename + TestBasic.FILE_EXTENSION, json_subject)        // var jsontext = context!!.resources.openRawResource(R.raw.script_001).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }
}












