package iit.uvip.psysuite.core.model.preferences

import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey

object DataStorePrefManager {

    private const val SETTINGS_PREF = "psysuitecore_preferences"

    private val DELAY_A1    = preferencesKey<Long>("pref_delay_a1")
    private val DELAY_A2    = preferencesKey<Long>("pref_delay_a2")
    private val DELAY_A3    = preferencesKey<Long>("pref_delay_a3")
    private val DELAY_T1    = preferencesKey<Long>("pref_delay_t1")
    private val DELAY_T2    = preferencesKey<Long>("pref_delay_t2")
    private val DELAY_V1    = preferencesKey<Long>("pref_delay_v1")
    private val DELAY_V2    = preferencesKey<Long>("pref_delay_v2")
    private val MAIN_EMAIL  = preferencesKey<String>("pref_main_email")

    private lateinit var dataStore:DataStore<Preferences>
    private lateinit var resources:Resources

    fun init(context: Context, name:String) {
        dataStore = context.createDataStore(SETTINGS_PREF)
        resources = context.resources
    }

    private val keyMap:HashMap<String, Preferences.Key<Long>> = hashMapOf(
        "DELAY_A1" to DELAY_A1,
        "DELAY_A2" to DELAY_A2,
        "DELAY_A3" to DELAY_A3,
        "DELAY_T1" to DELAY_T1,
        "DELAY_T2" to DELAY_T2,
        "DELAY_V1" to DELAY_V1,
        "DELAY_V2" to DELAY_V2)

//    val userPreferencesFlow: Flow<PsySuiteCorePreferences> = dataStore.data
//        .catch { exception ->
//            if (exception is IOException) {
//                emit(emptyPreferences())
//            } else {
//                throw exception
//            }
//        }.map { preferences ->
//            val a1 = preferences[DELAY_A1]      ?: resources.getInteger(R.integer.audio_delay_1)
//            val a2 = preferences[DELAY_A2]      ?: resources.getInteger(R.integer.audio_delay_2)
//            val a3 = preferences[DELAY_A3]      ?: resources.getInteger(R.integer.audio_delay_3)
//            val t1 = preferences[DELAY_T1]      ?: resources.getInteger(R.integer.tactile_delay_0)
//            val t2 = preferences[DELAY_T2]      ?: resources.getInteger(R.integer.tactile_delay_1)
//            val v1 = preferences[DELAY_V1]      ?: resources.getInteger(R.integer.visual_delay_1)
//            val v2 = preferences[DELAY_V2]      ?: resources.getInteger(R.integer.visual_delay_2)
//            val email = preferences[MAIN_EMAIL] ?: resources.getString(R.string.main_email)
//            PsySuiteCorePreferences(a1.toLong(),a2.toLong(),a3.toLong(),t1.toLong(),t2.toLong(),v1.toLong(),v2.toLong(),email)
//        }

    suspend fun updateInt(key:String, value: Long) {
        dataStore.edit { preferences ->
            preferences[keyMap.get(key) ?: return@edit] = value
        }
    }
}

data class PsySuiteCorePreferences(val delay_a1: Long,
                                   val delay_a2: Long,
                                   val delay_a3: Long,
                                   val delay_t1: Long,
                                   val delay_t2: Long,
                                   val delay_v1: Long,
                                   val delay_v2: Long,
                                   val main_email:String){
}