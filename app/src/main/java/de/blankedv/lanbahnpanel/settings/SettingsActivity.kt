package de.blankedv.lanbahnpanel.settings

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.MenuItem
import android.support.v4.app.NavUtils
import android.util.Log
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.model.*
import java.io.File
import java.util.ArrayList
import android.R.attr.key
import android.content.SharedPreferences
import android.os.*
import android.preference.*
import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication.Companion.pSett


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()

    }


    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            //if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this)
            //}
            return true
        }
        return super.onMenuItemSelected(featureId, item)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isLargeTablet(this)
    }


    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || DisplayPreferenceFragment::class.java.name == fragmentName
                || ControlPreferenceFragment::class.java.name == fragmentName
                || LocoPreferenceFragment::class.java.name == fragmentName
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            bindPreferenceSummaryToValue(findPreference(KEY_IP))
            bindPreferenceSummaryToValue(findPreference(KEY_PORT))

        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows DISPLAY preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DisplayPreferenceFragment() : PreferenceFragment()  /* ,
            SharedPreferences.OnSharedPreferenceChangeListener */ {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_display)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(KEY_STYLE_PREF))
            bindPreferenceSummaryToValue(findPreference(KEY_SCALE_PREF))
            //bindPreferenceSummaryToValue(findPreference(KEY_ENABLE_EDIT))
            // NOT USED, only result is checked after onResume in LanbahnPanelActivity
            //    bindPreferenceToBoolValue(findPreference(KEY_VIEW_VIEWS_PREF))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        override fun onResume() {
            super.onResume()
            //preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            //preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        /* no longer used
        override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
            Log.d(TAG,"CHANGED: pref=$prefs, key=$key")
            // stored panel specific preferences ALSO for the panel
            when (key) {
                KEY_STYLE_PREF -> {
                    selectedStyle = prefs!!.getString(KEY_STYLE_PREF,"US")
                    pSett.selStyle = selectedStyle
                    Log.d(TAG,"CHANGED: style=${pSett.selStyle} $selectedStyle")
                }
                KEY_SCALE_PREF -> {
                    selectedScale = prefs!!.getString(KEY_SCALE_PREF,"auto")
                    pSett.selScale = selectedScale
                }
                KEY_FIVE_VIEWS_PREF -> {
                    enableFiveViews = prefs!!.getBoolean(KEY_FIVE_VIEWS_PREF, false)
                    pSett.fiveViews = enableFiveViews
                }
                // quadrant cannot be changed in the SettingsActivity
            }
        } */
    }

    /**
     * This fragment shows Control preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class ControlPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_control)
            setHasOptionsMenu(true)

            val configFilenamePref = preferenceScreen.findPreference(KEY_CONFIG_FILE) as ListPreference
            val entries = matchingXMLFiles()
            if (entries != null) configFilenamePref!!.entries = entries
            if (entries != null) configFilenamePref!!.entryValues = entries
            bindPreferenceSummaryToValue(findPreference(KEY_CONFIG_FILE))

            //configFilenamePref!!.summary = "config loaded from " + prefs.getString(KEY_CONFIG_FILE, "-")!!

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            //bindPreferenceSummaryToValue(findPreference(KEY_ENABLE_EDIT))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun matchingXMLFiles(): Array<String?>? {

            if ((Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
                    and (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED_READ_ONLY)) {
                // Something  is wrong
                Log.d(TAG, "cannot read ExternalStorage Directory ")
                return null
            }

            val dir = File(Environment.getExternalStorageDirectory().toString() + "/" + DIRECTORY)
            Log.d(TAG, "reading directory " + dir.absolutePath)

            val files = ArrayList<String>()
            for (filename in dir.list()) {
                if (filename.endsWith(".xml")) {
                    files.add(filename)
                }
            }
            if (files.size > 0) {
                val array = arrayOfNulls<String>(files.size)
                return files.toArray(array)
            } else {
                return null
            }
        }


    }

    /**
     * This fragment shows Control preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class LocoPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_loco)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            bindPreferenceSummaryToValue(findPreference(KEY_LOCO_SYSTEM))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }


    }


    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val listPreference = preference
                val stringValue = value.toString()
                val index = listPreference.findIndexOfValue(stringValue)
                Log.d(TAG, "setting summary, key=" + preference.key)
                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null)

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                val stringValue = value.toString()
                preference.summary = stringValue
                Log.d(TAG, "setting summary, key=" + preference.key)
            }
            true
        }


        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }


    }
}
