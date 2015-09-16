package com.woalk.apps.xposed.htcblinkfeedauthorizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;

public class MainPreferenceFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {
    private static Preference pathUSB;
    private static Preference pathExt;
    private static int mCount;

    public static final String EXTRA_SUBSCREEN_ID = "subscreen_id";
    public static final int SUBSCREEN_ID_ALWAYS_ACTIVE = 1;
    private File mDefaultDirectory = new File(Environment.getExternalStorageDirectory().toString());
    private Toast toast;


    /**
     * Set a preference's summary text to the value it holds.
     * <br/><br/>
     * <b>Works for:</b><br/>
     * <ul>
     * <li>{@link EditTextPreference}</li>
     * </ul>
     *
     * @param pref The {@link Preference} to check and edit, if possible.
     */
    public static void setPreferenceValueToSummary(Preference pref) {
        if (pref.getKey().contains("_dir")) {
            pref.setSummary(pref.getSharedPreferences().getString(pref.getKey(),""));
        }
    }

    /**
     * Set all preferences' summary texts to the value the respective preference holds.
     * <br/><br/>
     * <b>Works for:</b><br/>
     * <ul>
     * <li>{@link EditTextPreference}</li>
     * </ul>
     *
     */
    public static void setAllPreferenceValuesToSummary() {
                    setPreferenceValueToSummary(pathExt);
                    setPreferenceValueToSummary(pathUSB);

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null
                && getArguments().getInt(EXTRA_SUBSCREEN_ID) == SUBSCREEN_ID_ALWAYS_ACTIVE) {
            addPreferencesFromResource(R.xml.pref_always_active);
            return;
        }

        addPreferencesFromResource(R.xml.pref_general);
        Preference permpref = findPreference("create_perm");
        Preference killpref = findPreference("kill_launcher");
        mCount = 0;
        toast = null;

        pathUSB = findPreference("usb_dir");
        pathExt = findPreference("ext_dir");

        permpref.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Common.createPermFile();
                        Common common;
                        common = new Common();
                        if (!common.copyPermFile()) {
                            if (mCount != 7) {


                                toast = Toast.makeText(getActivity(), "File already exists.", Toast.LENGTH_SHORT);
                                        toast.show();
                                mCount += 1;
                            } else {
                                common.copyPermFile(true);
                                toast.cancel();
                                toast = Toast.makeText(getActivity(), "Fiiiiiine.  I'll do it already.", Toast.LENGTH_SHORT);
                                        toast.show();
                                mCount = 0;
                            }

                        }

                        return true;
                    }
                });


        killpref.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Common.killPackage("com.htc.launcher");
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);
                        return true;
                    }
                });

        pathUSB.setOnPreferenceClickListener(this);
        pathExt.setOnPreferenceClickListener(this);

        setAllPreferenceValuesToSummary();
    }



    @Override
    public boolean onPreferenceClick(final Preference preference) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        final SharedPreferences.Editor editor = preference.getEditor();
        File curPath = new File (sharedPreferences.getString(preference.getKey(), mDefaultDirectory.toString()));
        FileDialog fd = new FileDialog(getActivity(),curPath);
        fd.setSelectDirectoryOption(true);
        fd.createFileDialog();
        fd.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
            @Override
            public void directorySelected(File directory) {
                Logger.d("MainPrefFrag: Trying to put value of " + directory + " to " + preference.getKey());
                editor.putString(preference.getKey(), directory.toString());
                editor.commit();
                Logger.d("MainPrefFrag: value of " + sharedPreferences.getString(preference.getKey(),""));
                preference.setSummary(directory.toString());
            }
        });
        return true;
    }
}
