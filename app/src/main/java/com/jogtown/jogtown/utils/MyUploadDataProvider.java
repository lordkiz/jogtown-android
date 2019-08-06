package com.jogtown.jogtown.utils;

import android.util.Log;

import org.chromium.net.UploadDataProvider;
import org.chromium.net.UploadDataSink;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MyUploadDataProvider extends UploadDataProvider {
    public int length = 0;

    @Override
    public long getLength() throws IOException {
        return this.length;
    }

    @Override
    public void read(UploadDataSink uploadDataSink, ByteBuffer byteBuffer) throws IOException {
        byte[] bytes;
        if (byteBuffer.hasArray()) {
            bytes = byteBuffer.array();
        } else {
            bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
        }
        this.length = bytes.length;
    }

    @Override
    public void rewind(UploadDataSink uploadDataSink) throws IOException {

    }

    public interface MyUploadData<String> {
        public void myUploadData(String uploadData);
    }
}
