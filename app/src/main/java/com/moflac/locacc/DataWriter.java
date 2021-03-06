package com.moflac.locacc;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Writes data on file after recordings
public class DataWriter {
    // time of writing
    private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private String fileName;


    public void writeFile(ArrayList<DataRow> dlist, Context con) {
        Resources res = con.getResources();
        try {
            // Creates a file in the external storage space of the app
            // If the file does not exists, it is created.
            // file name constructed from gps prefix and time of writing
            fileName="gps_"+timeStampFormat.format( new Date() )+".csv";
            File tFile = new File(con.getExternalFilesDir(null), fileName);
            if (!tFile.exists()) {
                tFile.createNewFile();
            }
            // write headers
            BufferedWriter writer = new BufferedWriter(new FileWriter(tFile, true));
            writer.write(res.getString(R.string.h_time)+";");
            writer.write(res.getString(R.string.h_latitude)+";");
            writer.write(res.getString(R.string.h_longitude)+";");
            writer.write(res.getString(R.string.h_bearing)+";");
            writer.write(res.getString(R.string.h_altitude)+";");
            writer.write(res.getString(R.string.h_speed)+";");
            writer.write(res.getString(R.string.h_accuracy)+";");
            writer.write(res.getString(R.string.h_x)+";");
            writer.write(res.getString(R.string.h_y)+";");
            writer.write(res.getString(R.string.h_z));
            writer.newLine();
            // write values
            for(int i=0; i < dlist.size(); i++)
            {
                writer.write(dlist.get(i).getTime()+";");
                writer.write(String.valueOf(dlist.get(i).getLatitude()+";"));
                writer.write(String.valueOf(dlist.get(i).getLongitude()+";"));
                writer.write(String.valueOf(dlist.get(i).getBearing()+";"));
                writer.write(String.valueOf(dlist.get(i).getAltitude()+";"));
                writer.write(String.valueOf(dlist.get(i).getSpeed()+";"));
                writer.write(String.valueOf(dlist.get(i).getAccuracy()+";"));
                writer.write(String.valueOf(dlist.get(i).getX()+";"));
                writer.write(String.valueOf(dlist.get(i).getY()+";"));
                writer.write(String.valueOf(dlist.get(i).getZ()));
                writer.newLine();

            }
            writer.close();
            // Refresh the data
            MediaScannerConnection.scanFile(con,  new String[]{tFile.toString()}, null, null);
        } catch (
                IOException e) {
            Toast toast = Toast.makeText(con, "Error writing to file", Toast.LENGTH_SHORT);
        }
        // get file directory and show it in toast
        String text =  con.getResources().getString(R.string.to_file_saved)+" "+con.getExternalFilesDir(null).toString()+"/"+fileName;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(con, text, duration);
        toast.show();
    }
}
