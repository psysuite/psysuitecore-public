package iit.uvip.psysuite.core.model.preferences

import android.content.Context
import android.content.res.Resources
import androidx.preference.PreferenceManager
import iit.uvip.psysuite.core.R
import iit.uvip.psysuite.core.stimuli.DelaysAligner
import org.albaspazio.core.sharedpreferences.SharedPreferenceWrapper

/**
 * Manages project-level preferences using Android's SharedPreferences.
 * It extends [SharedPreferenceWrapper] to provide a structured way to read and write preferences.
 *
 * This object maintains a predefined schema ([keysHashMap]) mapping preference keys (strings)
 * to their corresponding default value resource IDs (integers or strings).
 * It initializes a public [preferences] object of type [ProjectPreferences] which holds the current
 * state of all managed preferences and is updated after any modification.
 *
 * Initialization is done via the [init] method, which should be called once.
 * Default values can be provided during initialization or loaded from resources.
 */
object ProjectPreferencesManager: SharedPreferenceWrapper() {

    /**
     * A private HashMap defining the schema for all managed preferences.
     * It maps string preference keys (e.g., "pref_delay_a1") to their corresponding resource IDs
     * where default values are stored (e.g., `R.integer.pref_delay_a1`).
     * This map is crucial for initializing preferences from resources and for iterating
     * through all known preferences.
     */
    private val keysHashMap:HashMap<String,Int> = hashMapOf(
        "pref_delay_a1"     to R.integer.pref_delay_a1,
        "pref_delay_a2"     to R.integer.pref_delay_a2,
        "pref_delay_a3"     to R.integer.pref_delay_a3,
        "pref_delay_a4"     to R.integer.pref_delay_a4,
        "pref_delay_t1"     to R.integer.pref_delay_t1,
        // Note: "pref_delay_t2" currently points to R.integer.pref_delay_t1, this might be a typo
        "pref_delay_t2"     to R.integer.pref_delay_t1, 
        "pref_delay_v1"     to R.integer.pref_delay_v1,
        "pref_delay_v2"     to R.integer.pref_delay_v2,
        "pref_main_email"   to R.string.main_email)

    /** Android [Resources] instance, initialized in [init]. Used to fetch default preference values. */
    private lateinit var resources: Resources
    /** 
     * Publicly accessible [ProjectPreferences] object holding the current state of all managed preferences.
     * This instance is updated internally whenever preferences are changed (e.g., via [set] or [setFromResources]).
     * External code should read preference values from this object after [init] has been called.
     */
    lateinit var preferences: ProjectPreferences

    /**
     * Initializes the preference manager.
     * This method sets up the SharedPreferences instance and loads the initial preference values.
     * It prevents multiple initializations.
     *
     * @param context The Android [Context] used to access SharedPreferences and resources.
     * @param defVals Optional [ProjectPreferences] object containing default values. If provided, these are used.
     * @param overwrite If `true`, existing preferences will be overwritten with defaults (either from `defVals` or resources).
     *                  If `false`, defaults are only applied if the preference does not already exist.
     * @param pref_name Optional custom name for the SharedPreferences file. If empty, default SharedPreferences are used.
     * @param mode Operating mode for SharedPreferences (e.g., [Context.MODE_PRIVATE]). Used if `pref_name` is provided.
     */
    fun init(context:Context, defVals:ProjectPreferences?=null, overwrite:Boolean=false, pref_name:String="", mode:Int = Context.MODE_PRIVATE){

        if(isInitialized) return      // prevent multiple init

        resources       = context.resources
        prefs           =   if(pref_name.isEmpty()) PreferenceManager.getDefaultSharedPreferences(context)
                            else                    context.getSharedPreferences(pref_name, mode)

        // Set default values if provided or if overwrite is true
        if(defVals != null || overwrite) {
            setDefaults(defVals, overwrite)
        } else {
            // If not overwriting and no external defaults, ensure preferences are loaded (e.g. from resources if missing)
            // and then update the local `preferences` object.
            if (!prefs.contains(keysHashMap.keys.first())) { // A simple check if any known key is missing
                setFromResources(false) // Load from resources only if missing, don't overwrite existing
            } else {
                update() // Ensure `preferences` object is populated from SharedPreferences
            }
        }
    }

    /**
     * Sets all managed preferences based on the values in the provided [ProjectPreferences] object.
     * Iterates through the [keysHashMap] schema to write each preference.
     *
     * @param values The [ProjectPreferences] object containing the values to set.
     * @param overwrite If `true`, existing preference values will be overwritten.
     *                  If `false`, values are only written if the preference does not already exist.
     */
    fun set(values:ProjectPreferences, overwrite:Boolean=true){
        keysHashMap.forEach { (key, _) -> // Changed map to forEach
            if (!prefs.contains(key) || overwrite) {
                val valueToWrite = when (key) {
                    "pref_delay_a1"     -> values.delaysAligner.a1
                    "pref_delay_a2"     -> values.delaysAligner.a2
                    "pref_delay_a3"     -> values.delaysAligner.a3
                    "pref_delay_a4"     -> values.delaysAligner.a4
                    "pref_delay_t1"     -> values.delaysAligner.t1
                    "pref_delay_t2"     -> values.delaysAligner.t2
                    "pref_delay_v1"     -> values.delaysAligner.v1
                    "pref_delay_v2"     -> values.delaysAligner.v2
                    "pref_main_email"   -> values.email
                    else                -> "" // Should not happen if keysHashMap is exhaustive
                }
                write(key, valueToWrite) // `write` is from SharedPreferenceWrapper
            }
        }
        update() // Update the public `preferences` object
    }
    //==============================================================================================
    // PRIVATE (or protected)
    //==============================================================================================
    /**
     * Reads a preference value. Guarded to only operate if initialized.
     * @param key The preference key.
     * @param defvalue The default value to return if the key is not found or not initialized.
     * @return The preference value, or `null` if not initialized, or `defvalue` if key not found.
     */
    override fun read(key: String, defvalue: Any): Any?{
        return  if(!isInitialized)  null
                else                super.read(key, defvalue)
    }

    /**
     * Writes a preference value. Guarded to only operate if initialized.
     * @param key The preference key.
     * @param value The value to write.
     * @return The written value, or `null` if not initialized.
     */
    override fun write(key: String, value: Any): Any?{
        return  if(!isInitialized)  null
                else                super.write(key, value)
    }

    /**
     * Sets default preference values. 
     * If `extvalues` ([ProjectPreferences]) are provided, they are used.
     * Otherwise, defaults are loaded from Android resources defined in [keysHashMap].
     * @param extvalues Optional [ProjectPreferences] object with default values.
     * @param overwrite If `true`, existing preferences are overwritten.
     */
    private fun setDefaults(extvalues:ProjectPreferences?, overwrite:Boolean=true){
        if(extvalues != null){
            set(extvalues, overwrite)
        } else {
            setFromResources(overwrite)
        }
    }

    /**
     * Sets preference values by reading them from Android resources, based on the [keysHashMap] schema.
     * This is called if no explicit default values are provided during initialization.
     * @param overwrite If `true`, existing preferences are overwritten with values from resources.
     *                  If `false`, resource values are only applied if the preference does not already exist.
     */
    private fun setFromResources(overwrite:Boolean=true){
        keysHashMap.forEach{(key, resourceId) -> // Changed map to forEach
            if(!prefs.contains(key) || overwrite) {
                when(key){
                    "pref_delay_a1", "pref_delay_a2", "pref_delay_a3", "pref_delay_a4",
                    "pref_delay_t1", "pref_delay_t2",
                    "pref_delay_v1", "pref_delay_v2" ->
                            // Assuming integer resources for delays
                            write(key, resources.getInteger(resourceId).toLong()) // Convert to Long for consistency with DelaysAligner
                    "pref_main_email" ->
                            // Assuming string resource for email
                            write(key, resources.getString(resourceId))
                    // Add other types if necessary
                }
            }
        }
        update() // Update the public `preferences` object
    }

    /**
     * Updates the public [preferences] object ([ProjectPreferences]) by reading all managed
     * preference values from SharedPreferences.
     * This method is called internally after any modification to ensure the [preferences] object
     * reflects the current persisted state.
     */
    private fun update()
    {
        val deviceDelays = DelaysAligner(
            a1 = read("pref_delay_a1", 0L).toString().toLong(),
            a2 = read("pref_delay_a2", 0L).toString().toLong(),
            a3 = read("pref_delay_a3", 0L).toString().toLong(),
            a4 = read("pref_delay_a4", 0L).toString().toLong(),
            t1 = read("pref_delay_t1", 0L).toString().toLong(),
            t2 = read("pref_delay_t2", 0L).toString().toLong(),
            v1 = read("pref_delay_v1", 0L).toString().toLong(),
            v2 = read("pref_delay_v2", 0L).toString().toLong()
        )
        val email = read("pref_main_email", "") as String
        preferences = ProjectPreferences(deviceDelays, email)
    }
    //==============================================================================================
}