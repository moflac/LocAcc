package com.moflac.locacc;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DataWriter {
    private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private String fileName;

    public void writeFile(ArrayList<DataRow> dlist, Context con) {
        try {
            // Creates a file in the external storage space of the app
            // If the file does not exists, it is created.
            fileName="gps_"+timeStampFormat.format( new Date() )+".csv";
            File testFile = new File(con.getExternalFilesDir(null), fileName);
            if (!testFile.exists())
                testFile.createNewFile();

            // Adds a line to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, true /*append*/));
            writer.write("This is a test file.");
            writer.close();
            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug the device to see the
            // latest changes. This is not necessary if the user should not modify
            // the files.
            MediaScannerConnection.scanFile(con,
                    new String[]{testFile.toString()},
                    null,
                    null);
        } catch (
                IOException e) {
            Log.e("ReadWriteFile", "Unable to write to the TestFile.txt file.");
        }
        String text =  con.getExternalFilesDir(null).toString()+"/"+fileName+" "+dlist.size();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(con, text, duration);
        toast.show();
    }
}
