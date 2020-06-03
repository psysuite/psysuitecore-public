package iit.uvip.psysuite.core.tests.tid

import android.content.Context
import android.os.Build
import android.os.Parcel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iit.uvip.psysuite.core.common.TaskCode
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectLongitParcel
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.albaspazio.core.accessory.existFile
import org.albaspazio.core.accessory.readText
import org.albaspazio.core.accessory.saveText

// session
@Parcelize
class SubjectTIDParcel(
    override var type: Int = -1,
    override var label: String = "",
    override var age: Int = -1,
    override var gender: Int = -1,
    override var nextTrailModality: Int = -1,
    override var taskcodes: List<TaskCode> = listOf(),
    override var spinner_sel: Int = -1,
    override var spinner_data_resource: Int = -1,
    var modality: Int = -1,
    var interval_type: Int = -1,
    var first_modality: Int = -1
) : SubjectLongitParcel(
    type,
    label,
    age,
    gender,
    nextTrailModality,
    taskcodes,
    spinner_sel,
    spinner_data_resource
) {

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

        parcel.readInt(),
        parcel.readInt(),

        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    companion object : Parceler<SubjectTIDParcel> {

        override fun SubjectTIDParcel.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(type)
            parcel.writeString(label)
            parcel.writeInt(age)
            parcel.writeInt(gender)
            parcel.writeInt(nextTrailModality)
            if (Build.VERSION.SDK_INT >= 29) parcel.writeParcelableList(taskcodes, flags)
            else parcel.writeList(taskcodes)
            parcel.writeInt(spinner_sel)
            parcel.writeInt(spinner_data_resource)

            parcel.writeInt(modality)
            parcel.writeInt(interval_type)
            parcel.writeInt(first_modality)
        }

        override fun create(parcel: Parcel) = SubjectTIDParcel(parcel)

        private fun loadJsonText(jsontext:String): SubjectTIDParcel {
            val moshi           = Moshi.Builder().build()
            val jsonAdapter     = moshi.adapter(SubjectTIDParcel::class.java)
            return jsonAdapter.fromJson(jsontext)!!
        }

        fun loadSubject(): SubjectTIDParcel {
            val subj = existFile(CURR_SUBJ_FILE + TestBasic.FILE_EXTENSION)
            if(subj.first){
                val jsontext = readText(CURR_SUBJ_FILE + TestBasic.FILE_EXTENSION)
                return try {
                    loadJsonText(
                        jsontext
                    )
                } catch (e:Exception){
                    SubjectTIDParcel()
                }
            }
            return SubjectTIDParcel()
        }
    }

    // =============================================================================================================
    // WRITE
    // =============================================================================================================

    override fun writeJson(context: Context, filename:String){

        val moshi       = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(SubjectTIDParcel::class.java)

        return try {
            val json_subject = jsonAdapter.toJson(this)
            saveText(context, filename + TestBasic.FILE_EXTENSION, json_subject)        // var jsontext = context!!.resources.openRawResource(R.raw.script_001).bufferedReader().use { it.readText() }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return
        }
    }
    // =============================================================================================================
}












