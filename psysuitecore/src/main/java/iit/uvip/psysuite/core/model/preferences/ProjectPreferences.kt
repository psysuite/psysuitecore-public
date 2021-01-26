package iit.uvip.psysuite.core.model.preferences

import android.content.Context
import android.content.res.Resources
import androidx.preference.PreferenceManager
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import org.albaspazio.core.sharedpreferences.SharedPreferenceWrapper


object ProjectPreferences: SharedPreferenceWrapper() {


    // init values of each Preference is stored in a specific resource
    private val keysHashMap:HashMap<String,Int> = hashMapOf(
        "pref_delay_a1" to R.integer.pref_delay_a1,
        "pref_delay_a2" to R.integer.pref_delay_a2,
        "pref_delay_a3" to R.integer.pref_delay_a3,
        "pref_delay_t1" to R.integer.pref_delay_t1,
        "pref_delay_t2" to R.integer.pref_delay_t1,
        "pref_delay_v1" to R.integer.pref_delay_v1,
        "pref_delay_v2" to R.integer.pref_delay_v2,
        "pref_main_email" to R.string.main_email)

    private lateinit var resources: Resources
    private lateinit var defaultDelays: DelaysAligner

    //call it once
    fun init(context:Context, defDel:DelaysAligner, pref_name:String="", mode:Int = Context.MODE_PRIVATE){

        if(isInitialized()) return      // prevent multiple init

        defaultDelays   = defDel
        resources       = context.resources
        prefs           =   if(pref_name.isEmpty()) PreferenceManager.getDefaultSharedPreferences(context)
                            else                    context.getSharedPreferences(pref_name, mode)

        setDefault()
    }

    override fun read(key: String, value: Any): Any?{
        return  if(!keysHashMap.contains(key) || !isInitialized()) null
                else super.read(key, value)
    }

    override fun write(key: String, value: Any): Any?{
        return  if(!keysHashMap.contains(key) || !isInitialized())    null
                else super.write(key, value)
    }
    //==============================================================================================
    fun getSystemDelays():DelaysAligner = DelaysAligner(
            (read("pref_delay_a1","") as String).toLong(),
            (read("pref_delay_a2","") as String).toLong(),
            (read("pref_delay_a3","") as String).toLong(),
            (read("pref_delay_t1","") as String).toLong(),
            (read("pref_delay_t2","") as String).toLong(),
            (read("pref_delay_v1","") as String).toLong(),
            (read("pref_delay_v2","") as String).toLong())

    // write preferences from defaultonly if still unset
    private fun setDefault(){

        keysHashMap.map {
            if (!prefs.contains(it.key)) {
                val value = when (it.key) {
                    "pref_delay_a1" -> defaultDelays.a1.toString()
                    "pref_delay_a2" -> defaultDelays.a2.toString()
                    "pref_delay_a3" -> defaultDelays.a3.toString()
                    "pref_delay_t1" -> defaultDelays.t1.toString()
                    "pref_delay_t2" -> defaultDelays.t2.toString()
                    "pref_delay_v1" -> defaultDelays.v1.toString()
                    "pref_delay_v2" -> defaultDelays.v2.toString()
                    else            -> resources.getString(R.string.main_email)
                }
                write(it.key, value)
            }
        }
    }
    // write preferences only if still unset
    private fun setDefaultFromResources(){
        keysHashMap.map{

            if(!prefs.contains(it.key))
                when(it.key){
                    "pref_delay_a1", "pref_delay_a2", "pref_delay_a3",
                    "pref_delay_t1", "pref_delay_t2",
                    "pref_delay_v1", "pref_delay_v2" ->
                        write(it.key, resources.getInteger(it.value).toString())
                    "pref_main_email" ->
                        write(it.key, resources.getString(R.string.main_email))
                }
            it
        }
    }
    //==============================================================================================
}