package com.sftelehealth.doctor.video.view.call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sftelehealth.doctor.video.Constant;
import com.sftelehealth.doctor.video.R;

import java.util.concurrent.TimeUnit;

import io.agora.rtc.RtcEngine;

public class VideoCallActivity extends AppCompatActivity implements OnHandlerEventListener {

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_REQ_ID = 22;
    private OnActionClickListener listener;
    private boolean mCallEnd = false;
    private boolean mMuted = false;
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;
    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private ImageView mCallBtn;
    private ImageView mMuteBtn;
    private TextView timer;
    private ImageView mSwitchCameraBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_view);

        listener = ActionHandler.getInstance().getActionListener();
        listener.setHandlerListener(this);
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initVideoCallView();
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void initVideoCallView() {
        initUI();
        localVideo();
    }

    private void localVideo() {
        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        listener.setupLocalVideo(mLocalView);
    }

    public void onSwitchCameraClicked(View view) {
        listener.switchCamera();
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        listener.audioMute(mMuted);
        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
//            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

//    private void startCall() {
//        setupLocalVideo();
//        getMediaChannelKey();
//        joinChannel();
//    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        listener.leaveChannel();
    }

    private void removeLocalVideo() {
        if (mLocalView != null) {
            mLocalContainer.removeView(mLocalView);
        }
        mLocalView = null;
    }

    private void removeRemoteVideo() {
        if (mRemoteView != null) {
            mRemoteContainer.removeView(mRemoteView);
        }
        mRemoteView = null;
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }

    private void initUI() {
        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);
        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        timer = findViewById(R.id.time);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initVideoCallView();
        }
    }

    private void showLongToast(final String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setupRemoteVideo(int uid) {
        runOnUiThread(() -> showRemoteVideo(uid));
    }

    @Override
    public void destroyCallView() {
        finish();
    }

    private void showRemoteVideo(int uid){
        int count = mRemoteContainer.getChildCount();
        View view = null;
        for (int i = 0; i < count; i++) {
            View v = mRemoteContainer.getChildAt(i);
            if (v.getTag() instanceof Integer && ((int) v.getTag()) == uid) {
                view = v;
            }
        }

        if (view != null) {
            return;
        }
        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteContainer.addView(mRemoteView);
        // Initializes the video view of a remote user.
        listener.remoteVideo(mRemoteView, uid);
        mRemoteView.setTag(uid);
        setTimer();
    }

    @SuppressLint("DefaultLocale")
    private void setTimer(){
        new CountDownTimer(300000, 1000) { // adjust the milli seconds here
            long time = 1000;
            public void onTick(long millisUntilFinished) {
                time = time + 1000;
                timer.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes( time),
                        TimeUnit.MILLISECONDS.toSeconds(time) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))));
            }

            public void onFinish() {
                endCall();
                mCallEnd = true;
                timer.setText(R.string._00_00);
                mCallBtn.setImageResource(R.drawable.btn_startcall);
            }
        }.start();
    }
}