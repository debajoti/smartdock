package cu.axel.smartdock.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import cu.axel.smartdock.R;

public class DesktopPreferences extends PreferenceFragment 
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_desktop);
    }

}
