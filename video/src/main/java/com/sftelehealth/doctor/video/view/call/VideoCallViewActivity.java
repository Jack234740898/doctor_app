package com.sftelehealth.doctor.video.view.call;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.sftelehealth.doctor.data.model.request.CallbackUpdateRequest;
import com.sftelehealth.doctor.data.model.request.MediaChannelKeyRequest;
import com.sftelehealth.doctor.data.model.response.CancelCallbackResponse;
import com.sftelehealth.doctor.data.model.response.MediaChannelResponse;
import com.sftelehealth.doctor.data.net.AuthCodeProvider;
import com.sftelehealth.doctor.data.net.CoreApi;
import com.sftelehealth.doctor.data.net.RestApi;
import com.sftelehealth.doctor.data.net.RetrofitService;
import com.sftelehealth.doctor.domain.interactor.GetMediaChannelKey;
import com.sftelehealth.doctor.video.Constant;
import com.sftelehealth.doctor.video.R;
import com.sftelehealth.doctor.video.agora.key.DynamicKey4;
import com.sftelehealth.doctor.video.helper.CallDataHelper;
import com.sftelehealth.doctor.video.internal.di.components.CoreVideoComponent;
import com.sftelehealth.doctor.video.internal.di.components.DaggerUseCaseComponent;
import com.sftelehealth.doctor.video.internal.di.components.UseCaseComponent;
import com.sftelehealth.doctor.video.internal.di.modules.ActivityModule;
import com.sftelehealth.doctor.video.internal.di.modules.CoreVideoModule;
import com.sftelehealth.doctor.video.internal.di.modules.UseCaseModule;
import com.sftelehealth.doctor.video.view.call.agora.media.AccessToken;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Random;

import javax.inject.Inject;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.sftelehealth.doctor.video.Constant.USER_PREFS;


public class VideoCallViewActivity extends AppCompatActivity {
    private static final String TAG = VideoCallViewActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;
    private NotificationPayloadData notificationPayloadData;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RtcEngine mRtcEngine;
    private boolean mCallEnd = false;
    private boolean mMuted;

    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;

    private ImageView mCallBtn;
    private ImageView mMuteBtn;
    private ImageView mSwitchCameraBtn;
    private String token;

    RetrofitService restService = new RetrofitService();
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Join channel success, uid: " + (uid & 0xFFFFFFFFL));

                }
            });
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "User offline, uid: " + (uid & 0xFFFFFFFFL));
                    onRemoteUserLeft();
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed ) {
            Log.d(TAG, "User Joined, uid: " + (uid & 0xFFFFFFFFL));
        }

    };

    private void setupRemoteVideo(int uid) {
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
        mRemoteView.setTag(uid);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void onRemoteUserLeft() {
        removeRemoteVideo();
    }

    private void removeRemoteVideo() {
        if (mRemoteView != null) {
            mRemoteContainer.removeView(mRemoteView);
        }
        mRemoteView = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_view);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);

        Bundle extras = getIntent().getExtras();

//        if (!RingtoneHelper.getRingToneMediaManager(this).isPlaying())
//            RingtoneHelper.playRingtoneAndVibrationInLoop(this);
//
        if (extras != null) {
            notificationPayloadData = new NotificationPayloadData();
            notificationPayloadData.setAction(extras.getString(Constant.VIDEO_CALL_STATE));
            notificationPayloadData.setChannelName(extras.getString("channel_name"));
            notificationPayloadData.setDoctorId(extras.getInt("doctor_id"));
            notificationPayloadData.setDoctorName(extras.getString("doctor_name"));
            notificationPayloadData.setDoctorImage(extras.getString("doctor_image"));
            notificationPayloadData.setPatientName(extras.getString("patient_name"));
            notificationPayloadData.setPatientId(extras.getInt("patient_user_id"));
            notificationPayloadData.setCallbackId(extras.getString("callback_id"));
        }
        initUI();

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }
    }

    private void initUI() {
        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);

        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
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
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initEngineAndJoinChannel() {
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        getMediaChannelKey();
//        joinChannel();
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        mRtcEngine.enableVideo();
    }

    private void setupLocalVideo() {
        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void joinChannel() {
        String uid = String.valueOf(notificationPayloadData.getDoctorId());
        String channel = notificationPayloadData.getChannelName();

        try {
            AccessToken accessToken = new AccessToken(getString(R.string.agora_app_id), getString(R.string.certificate), channel, uid);
            String token1 = accessToken.build();
            mRtcEngine.joinChannel(token, channel, "Extra Optional Data", Integer.parseInt(uid));
            Log.d(TAG, "joinChannel: "+notificationPayloadData.getChannelName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd) {
            leaveChannel();
        }
        compositeDisposable.clear();
        RtcEngine.destroy();
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);
    }

    public void onSwitchCameraClicked(View view) {
        // Switches between front and rear cameras.
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

    private void startCall() {
        setupLocalVideo();
        getMediaChannelKey();
//        joinChannel();
    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();
        completedAppointRequest();
    }

    private void removeLocalVideo() {
        if (mLocalView != null) {
            mLocalContainer.removeView(mLocalView);
        }
        mLocalView = null;
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }

    private void getMediaChannelKey() {
        HashMap<String, String> params = new HashMap<>();
        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);

        params.put("channel_name", notificationPayloadData.getChannelName());
        params.put("uid",String.valueOf(notificationPayloadData.getDoctorId()));
        params.put("patient_user_id", CallDataHelper.getCallData(sp).getPatientUserId());

        MediaChannelKeyRequest request = new MediaChannelKeyRequest();
        request.setChannelName(notificationPayloadData.getChannelName());
        request.setUid(String.valueOf(notificationPayloadData.getDoctorId()));
        request.setExpiredTs(0);
        request.setUserId(notificationPayloadData.getPatientId());
        request.setEmergencyCall(false);

        RestApi myAPI = restService.getRestApiService();
        compositeDisposable.add(myAPI.getMediaChannelKey(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResults, this::handleError));

    }

    private void completedAppointRequest() {
//        CallbackUpdateRequest callbackUpdateRequest = new CallbackUpdateRequest();
//        callbackUpdateRequest.setCallbackId(Integer.parseInt(notificationPayloadData.getCallbackId()));
//        CoreApi coreApi = restService.getCoreApiService();
//        compositeDisposable.add(coreApi.completedAppointment(AuthCodeProvider.getAuthCode(this), callbackUpdateRequest)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::handleResults, this::handleError));
    }

    private void handleResults(Object object) {
        if (object instanceof MediaChannelResponse){
            token = ((MediaChannelResponse) object).getMediaChannelKey();
            joinChannel();
        } else if (object instanceof CancelCallbackResponse){

        }
    }

    private void handleError(Throwable t) {
        Toast.makeText(this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }



}
