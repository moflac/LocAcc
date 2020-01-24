package com.moflac.locacc;

import android.content.Context;
import android.content.res.Resources;
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
        Resources res = con.getResources();
        try {
            // Creates a file in the external storage space of the app
            // If the file does not exists, it is created.
            fileName="gps_"+timeStampFormat.format( new Date() )+".csv";
            File testFile = new File(con.getExternalFilesDir(null), fileName);
            if (!testFile.exists()) {
                testFile.createNewFile();
            }
            // Adds a line to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, true));
            writer.write(res.getString(R.string.h_time)+";");
            writer.write(res.getString(R.string.h_latitude)+";");
            writer.write(res.getString(R.string.h_longitude)+";");
            writer.write(res.getString(R.string.h_bearing)+";");
            writer.write(res.getString(R.string.h_speed)+";");
            writer.write(res.getString(R.string.h_accuracy)+";");
            writer.write(res.getString(R.string.h_x)+";");
            writer.write(res.getString(R.string.h_y)+";");
            writer.write(res.getString(R.string.h_z));
            writer.newLine();
            for(int i=0; i < dlist.size(); i++)
            {
                writer.write(dlist.get(i).time+";");
                writer.write(String.valueOf(dlist.get(i).latitude+";"));
                writer.write(String.valueOf(dlist.get(i).longitude+";"));
                writer.write(String.valueOf(dlist.get(i).bearing+";"));
                writer.write(String.valueOf(dlist.get(i).speed+";"));
                writer.write(String.valueOf(dlist.get(i).accuracy+";"));
                writer.write(String.valueOf(dlist.get(i).x+";"));
                writer.write(String.valueOf(dlist.get(i).y+";"));
                writer.write(String.valueOf(dlist.get(i).z));
                writer.newLine();

            }
            //writer.write("This is a test file.");
            //writer.newLine();
            //writer.write("okei");
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
            Toast toast = Toast.makeText(con, "Error writing to file", Toast.LENGTH_SHORT);
        }
        // get file directory and show it in toast
        String text =  con.getExternalFilesDir(null).toString()+"/"+fileName+" "+dlist.size();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(con, text, duration);
        toast.show();
    }
}
