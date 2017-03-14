package org.opendatakit.services.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;

import org.apache.commons.io.FileUtils;
import org.opendatakit.services.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class SettingsUtils {

    private Context context;

    public SettingsUtils(Context context){
        this.context = context;
    }

    public void saveOfficeIdToFile(String value)
    {
        try {
            String filename = "settings.txt";
            FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(file);
            if(value.equals(""))
                value = "null";
            writer.write(value);
            writer.close();
            file.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getOfficeIdFromFile() {
        try {
            String filename = "settings.txt";
            FileInputStream file = context.getApplicationContext().openFileInput(filename);
            InputStreamReader reader = new InputStreamReader(file);
            char[] inputBuffer = new char[100];
            int i = reader.read(inputBuffer);
            String content = new String(inputBuffer, 0, i);
            reader.close();
            file.close();
            return content;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteAllForms()
    {
        File tables = new File(Environment.getExternalStorageDirectory() + "/opendatakit/default/config/tables");
        File data = new File(Environment.getExternalStorageDirectory() + "/opendatakit/default/data");
        try {
            FileUtils.cleanDirectory(tables);
            FileUtils.cleanDirectory(data);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
