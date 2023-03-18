package iit.uvip.psysuite.core.model.preferences

import android.content.Context
import android.content.res.Resources
import androidx.preference.PreferenceManager
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import org.albaspazio.core.sharedpreferences.SharedPreferenceWrapper


object ProjectPreferencesManager: SharedPreferenceWrapper() {

    // default values of each preference is stored in a specific resource
    // this HashMap maps each preference label to a specific resource ID
    // it must contains ALL the preferences values.
    // it will be used as the scheme of available preferences
    private val keysHashMap:HashMap<String,Int> = hashMapOf(
        "pref_delay_a1"     to R.integer.pref_delay_a1,
        "pref_delay_a2"     to R.integer.pref_delay_a2,
        "pref_delay_a3"     to R.integer.pref_delay_a3,
        "pref_delay_a4"     to R.integer.pref_delay_a4,
        "pref_delay_t1"     to R.integer.pref_delay_t1,
        "pref_delay_t2"     to R.integer.pref_delay_t1,
        "pref_delay_v1"     to R.integer.pref_delay_v1,
        "pref_delay_v2"     to R.integer.pref_delay_v2,
        "pref_main_email"   to R.string.main_email)

    private lateinit var resources: Resources
    lateinit var preferences: ProjectPreferences // updated at the end of any modification

    //call it once
    fun init(context:Context, defVals:ProjectPreferences?=null, overwrite:Boolean=false, pref_name:String="", mode:Int = Context.MODE_PRIVATE){

        if(isInitialized) return      // prevent multiple init

        resources       = context.resources
        prefs           =   if(pref_name.isEmpty()) PreferenceManager.getDefaultSharedPreferences(context)
                            else                    context.getSharedPreferences(pref_name, mode)

        if(defVals != null || overwrite)
            setDefaults(defVals, overwrite)
    }

    // write preferences taking values from given data.
    // it is usually called internally during init
    fun set(values:ProjectPreferences, overwrite:Boolean=true){
        // follows the schema in resourcesDefaults to fill preferences
        // with data taken from given values
        keysHashMap.map {
            if (!prefs.contains(it.key) || overwrite) {
                val value = when (it.key) {
                    "pref_delay_a1"     -> values.delaysAligner.a1
                    "pref_delay_a2"     -> values.delaysAligner.a2
                    "pref_delay_a3"     -> values.delaysAligner.a3
                    "pref_delay_a4"     -> values.delaysAligner.a4
                    "pref_delay_t1"     -> values.delaysAligner.t1
                    "pref_delay_t2"     -> values.delaysAligner.t2
                    "pref_delay_v1"     -> values.delaysAligner.v1
                    "pref_delay_v2"     -> values.delaysAligner.v2
                    "pref_main_email"   -> values.email
                    else                -> ""
                }
                write(it.key, value)
            }
        }
        update()
    }
    //==============================================================================================
    // PRIVATE (or protected)
    //==============================================================================================
    override fun read(key: String, defvalue: Any): Any?{
        return  if(!isInitialized)  null
                else                super.read(key, defvalue)
    }

    override fun write(key: String, value: Any): Any?{
        return  if(!isInitialized)  null
                else                super.write(key, value)
    }
    // write preferences first trying from defaults
    // otherwise, trying from resources.
    // by default, it overwrites existing preferences
    private fun setDefaults(extvalues:ProjectPreferences?, overwrite:Boolean=true){
        if(extvalues != null)
            set(extvalues, overwrite)
        else setFromResources(overwrite)
    }
    // write preferences taking values from resources.
    // called when defaultValues are null.
    // by default it overwrites preferences
    private fun setFromResources(overwrite:Boolean=true){
        keysHashMap.map{
            if(!prefs.contains(it.key) || overwrite)
                when(it.key){
                    "pref_delay_a1", "pref_delay_a2", "pref_delay_a3", "pref_delay_a4",
                    "pref_delay_t1", "pref_delay_t2",
                    "pref_delay_v1", "pref_delay_v2" ->
                            write(it.key, resources.getInteger(it.value))
                    "pref_main_email" ->
                            write(it.key, resources.getString(it.value))
                }
            it
        }
        update()
    }
    // since accessing preferences can be time consuming
    // external code asking for preferences, access the interval variable
    // preferences. update is called after every setFromResources/set
    private fun update()
    {
        val deviceDelays: DelaysAligner = DelaysAligner(
            read("pref_delay_a1", 0L).toString().toLong(),
            read("pref_delay_a2", 0L).toString().toLong(),
            read("pref_delay_a3", 0L).toString().toLong(),
            read("pref_delay_a4", 0L).toString().toLong(),
            read("pref_delay_t1", 0L).toString().toLong(),
            read("pref_delay_t2", 0L).toString().toLong(),
            read("pref_delay_v1", 0L).toString().toLong(),
            read("pref_delay_v2", 0L).toString().toLong()
        )
        val email = read("pref_main_email", "") as String
        preferences = ProjectPreferences(deviceDelays, email)
    }






    //==============================================================================================
}