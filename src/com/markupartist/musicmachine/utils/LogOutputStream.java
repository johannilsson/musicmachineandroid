package com.markupartist.musicmachine.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LogOutputStream extends OutputStream {
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private String name;

    public LogOutputStream(String name) {
        this.name = name;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == (int) '\n') {
            String s = new String(this.bos.toByteArray());
            Log.d(this.name, s);
            this.bos = new ByteArrayOutputStream();
        } else {
            this.bos.write(b);
        }
    }
}
