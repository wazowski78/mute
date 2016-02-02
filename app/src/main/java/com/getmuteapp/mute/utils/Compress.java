package com.getmuteapp.mute.utils;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compress {
    private static final String LOG_TAG = Compress.class.getSimpleName();
    private static final int BUFFER = 1024;

    private String[] files;
    private String zipFile;

    public Compress(String[] files) {
        this.files = files;
        this.zipFile = getZipFilePath(files[0]);
    }

    private String getZipFilePath(String filePath) {
        Log.d(LOG_TAG,filePath);
        int fileNameIndex = filePath.lastIndexOf('/');
        StringBuilder sb = new StringBuilder();
        sb.append(filePath.substring(0,fileNameIndex+1));
        //sb.append("storage/emulated/0/Android/data/com.getmuteapp.mute/files/");
        sb.append(filePath.substring(fileNameIndex+1, filePath.lastIndexOf(".")+1));
        sb.append("zip");
        Log.d(LOG_TAG,sb.toString());
        return sb.toString();

    }

    public void zip() {
        try {
            Log.d(LOG_TAG,zipFile);
            FileOutputStream destination = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(destination);
            byte data[] = new byte[BUFFER];

            for(int i = 0; i< files.length; i++) {
                FileInputStream fileInputStream = new FileInputStream(files[i]);
                ZipEntry zipEntry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                out.putNextEntry(zipEntry);
                int count;
                while ((count = fileInputStream.read(data)) > 0) {
                    out.write(data, 0, count);
                }
                fileInputStream.close();
                out.closeEntry();
                out.close();
            }

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getZipFile() {
        return zipFile;
    }
}
