package com.sftelehealth.doctor.video.view.call;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.sftelehealth.doctor.data.model.request.MediaChannelKeyRequest;
import com.sftelehealth.doctor.data.model.response.CancelCallbackResponse;
import com.sftelehealth.doctor.data.model.response.MediaChannelResponse;
import com.sftelehealth.doctor.data.net.RestApi;
import com.sftelehealth.doctor.data.net.RetrofitService;
import com.sftelehealth.doctor.video.Constant;
import com.sftelehealth.doctor.video.R;
import com.sftelehealth.doctor.video.agora.AgoraHelper;
import com.sftelehealth.doctor.video.helper.CallDataHelper;
import com.sftelehealth.doctor.video.view.call.rtcEngineEvent.IRtcEngineEventListener;
import com.sftelehealth.doctor.video.view.call.rtcEngineEvent.RtcEngineEventHandler;
import com.sftelehealth.doctor.video.view.call.signalEvent.AgoraSignalHandler;
import com.sftelehealth.doctor.video.view.call.signalEvent.IAgoraSignalEventListener;

import io.agora.AgoraAPI;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CallService extends Service implements IRtcEngineEventListener, IAgoraSignalEventListener, OnActionClickListener {

    private NotificationPayloadData notificationPayloadData;
    private RtcEngine mRtcEngine;
    private RetrofitService restService;
    private String token;
    private int loginUserId;
    private CompositeDisposable compositeDisposable;
    private static final String TAG = "CallService";
    private OnHandlerEventListener listener;
    private AgoraHelper agoraHelper;

    public CallService() {
        Log.d(TAG, "CallService: ");
        ActionHandler.getInstance().setActionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras != null){
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


        restService = new RetrofitService();
        compositeDisposable = new CompositeDisposable();
        setupAgoraSignalEngine();
        setupRTCEngine(initiateRtcEngine());

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void joinChannelSuccess(int uid) {
        setupVideoConfig();
        startVideoCall();
    }

    @Override
    public void setupRemoteVideo(int uid) {
        Log.d(TAG, "setupRemoteVideo: ");
        listener.setupRemoteVideo(uid);
    }

    @Override
    public void remoteUserLeft() {
        Log.d(TAG, "remoteUserLeft: ");
    }

    @Override
    public void error(int errorCode) {
        Log.d(TAG, "error: ");
    }

    @Override
    public void leaveChannel(int totalDuration) {
        Log.d(TAG, "leaveChannel: ");
    }

    @Override
    public void loginSuccess(int uId, int i1) {
        loginUserId = uId;
        getMediaChannelKey();
    }

    @Override
    public void channelUserList(String[] strings, int[] ints) {
        Log.d(TAG, "channelUserList: ");
    }

    @Override
    public void userRejoinedInChannel() {
//        joinChannel();
    }

    @Override
    public void logoutSuccess() {
        leaveChannel();
    }

    @Override
    public void channelJoined(String s) {
        Log.d(TAG, "channelJoined: "+s);
        agoraHelper.inviteUserToJoinChannel(getPatientAccount(notificationPayloadData.getPatientId()), "Hello");
    }


    private void setupVideoConfig() {
        mRtcEngine.enableVideo();
    }

    @Override
    public void setupLocalVideo(SurfaceView mLocalView) {
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    @Override
    public void switchCamera() {
        mRtcEngine.switchCamera();
    }

    @Override
    public void audioMute(boolean mMuted) {
        mRtcEngine.muteLocalAudioStream(mMuted);
    }

    @Override
    public void remoteVideo(SurfaceView mRemoteView, int uid) {
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    @Override
    public void setHandlerListener(OnHandlerEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void leaveChannel() {
        mRtcEngine.leaveChannel();
        agoraHelper.logout();
        agoraHelper.leaveAPIChannel();
        listener.destroyCallView();
        RtcEngine.destroy();
        onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupAgoraSignalEngine() {
        AgoraAPI.CallBack agoraCallback = new AgoraSignalHandler(this);
        agoraHelper = AgoraHelper.getInstance(getApplicationContext(), agoraCallback,  getUserAccount(notificationPayloadData.getDoctorId()));
        agoraHelper.joinChannel(notificationPayloadData.getChannelName());
    }

    private IRtcEngineEventHandler initiateRtcEngine() {
        return new RtcEngineEventHandler(this);
    }

    void setupRTCEngine(IRtcEngineEventHandler mRtcEventHandler) {
        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void getMediaChannelKey() {
        MediaChannelKeyRequest request = new MediaChannelKeyRequest();
        request.setChannelName(notificationPayloadData.getChannelName());
        request.setUid(String.valueOf(loginUserId));
        request.setExpiredTs(0);
        request.setUserId(notificationPayloadData.getPatientId());
        request.setEmergencyCall(false);

        RestApi myAPI = restService.getRestApiService();
        compositeDisposable.add(myAPI.getMediaChannelKey(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResults, this::handleError));

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

    public void joinChannel() {
        String channel = notificationPayloadData.getChannelName();
        try {
            mRtcEngine.joinChannel(token, channel, "Extra Optional Data", loginUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUserAccount(int doctorId) {
        return "doctor_" + doctorId;
    }

    public String getPatientAccount(int patientId) {
        return "user_" + patientId;
    }

    private void startVideoCall() {
        Intent intent = new Intent(this, VideoCallActivity.class);
        intent.putExtra("U_ID", loginUserId);
        startActivity(intent);
    }

    public class LocalBinder extends Binder {
        public CallService getServerInstance() {
            return CallService.this;
        }
    }
}
