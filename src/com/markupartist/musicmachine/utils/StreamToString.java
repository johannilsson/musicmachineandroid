package com.markupartist.musicmachine.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamToString {

    public static void main(String[] args) throws Exception {
        StreamToString sts = new StreamToString();

        /*
        * Get input stream of our data file. This file can be in the root of
        * you application folder or inside a jar file if the program is packed
        * as a jar.
        */
        InputStream is = sts.getClass().getResourceAsStream("/data.txt");

        /*
        * Call the method to convert the stream to string
        */
        System.out.println(sts.convertStreamToString(is));
    }

    public String convertStreamToString(InputStream is) throws IOException {
        /*
        * To convert the InputStream to String we use the BufferedReader.readLine()
        * method. We iterate until the BufferedReader return null which means
        * there's no more data to read. Each line will appended to a StringBuilder
        * and returned as String.
        */
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }
}