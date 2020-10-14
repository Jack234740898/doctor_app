package com.sftelehealth.doctor.data.net;

/**
 * Created by Rahul on 24/01/18.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sftelehealth.doctor.data.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Class to get the transfer progress for a request
 * @link {http://stackoverflow.com/questions/33338181/is-it-possible-to-show-progress-bar-when-upload-image-via-retrofit-2}
 */

public class ProgressRequestBody extends RequestBody {
    private File mFile;
    private String mPath;
    private UploadCallbacks mListener;

//    public static final String STATUS_UPDATE = BuildConfig.APPLICATION_ID + ".services.action.TEXT_TO_SPEECH";

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
        //void onError();
        void onFinish();
    }

    public ProgressRequestBody(final File file, final  UploadCallbacks listener) {
        mFile = file;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        // i want to upload only images
        return MediaType.parse("image*//*");
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new ProgressUpdater(uploaded, fileLength));

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;
        private int percentageUploaded;
        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            percentageUploaded = (int)(100 * mUploaded / mTotal);
            if(percentageUploaded == 100) {
                mListener.onProgressUpdate(percentageUploaded);
                mListener.onFinish();
            } else
                mListener.onProgressUpdate(percentageUploaded);
        }

        private void broadcast(int percentage, Context context) {
            Intent statusUpdateIntent = new Intent("doctor_image_upload_status_broadcast");
            statusUpdateIntent.putExtra("upload_status", percentage);
            LocalBroadcastManager.getInstance(context).sendBroadcast(statusUpdateIntent);
            Log.d("upload_status", "uploading: " + percentage);
        }
    }
}