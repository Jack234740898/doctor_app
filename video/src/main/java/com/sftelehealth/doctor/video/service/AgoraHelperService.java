package com.sftelehealth.doctor.video.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.sftelehealth.doctor.domain.interactor.GetMediaChannelKey;
import com.sftelehealth.doctor.domain.model.response.MediaChannelResponse;
import com.sftelehealth.doctor.video.Constant;
import com.sftelehealth.doctor.video.agora.AgoraHelper;
import com.sftelehealth.doctor.video.agora.IAgoraAPICallback;
import com.sftelehealth.doctor.video.agora.IAgoraHelper;
import com.sftelehealth.doctor.video.agora.call.CallData;
import com.sftelehealth.doctor.video.agora.key.DynamicKey4;
import com.sftelehealth.doctor.video.agora.message.AgoraMessageHelper;
import com.sftelehealth.doctor.video.helper.CallDataHelper;
import com.sftelehealth.doctor.video.internal.di.components.CoreVideoComponent;
import com.sftelehealth.doctor.video.internal.di.components.DaggerServiceComponent;
import com.sftelehealth.doctor.video.internal.di.modules.CoreVideoModule;
import com.sftelehealth.doctor.video.models.Doctor;
import com.sftelehealth.doctor.video.models.InfoMessage;
import com.sftelehealth.doctor.video.network.NetworkChangeReceiver;
import com.sftelehealth.doctor.video.receiver.CustomPhoneStateListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import io.agora.AgoraAPI;
import io.agora.rtc.RtcEngine;
import io.reactivex.observers.DisposableObserver;

import static android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED;
import static com.sftelehealth.doctor.video.Constant.USER_PREFS;
import static com.sftelehealth.doctor.video.Constant.VIDEO_CALL_STATE_INITIATED;
import static com.sftelehealth.doctor.video.Constant.VIDEO_CALL_STATE_INVITE_RECEIVED_BY_USER;
import static com.sftelehealth.doctor.video.Constant.VIDEO_CALL_STATE_ON_GOING;
import static com.sftelehealth.doctor.video.Constant.VIDEO_MESSAGE_SEND_USER_INFO;
import static com.sftelehealth.doctor.video.agora.AgoraHelper.appID;
import static com.sftelehealth.doctor.video.agora.AgoraHelper.certificate;
import static com.sftelehealth.doctor.video.network.NetworkChangeReceiver.NETWORK_AVAILABLE;
import static com.sftelehealth.doctor.video.network.NetworkChangeReceiver.NETWORK_UNAVAILABLE;

public class AgoraHelperService extends Service implements RtcEngineEventHandler.RtcEngineEventListener, CustomPhoneStateListener.PhoneStateChangedListener, NetworkChangeReceiver.ConnectivityChangeListener {

    //RelativeLayout callHeadView;
    //WindowManager windowManager;

    //WeakReference<BroadcastReceiver> activityStateReceiver;
    //IntentFilter activityStateIntentFilter;

    private final String TAG = "BackgroundService";

    AgoraHelper agoraHelper;
    AgoraHelperBinder agoraHelperBinder;

    AgoraAPI.CallBack agoraCallback;
    IAgoraAPICallback agoraAPICallback;

    RtcEngine m_agoraMedia;

    // CoreVideoComponent coreVideoComponent;
    // CallData callData;

    int inviteTryCount = 0;
    int uid = 0;
    int startId;

    CallData callData;
    Gson gson = new Gson();

    CoreVideoComponent coreVideoComponent;

    CustomPhoneStateListener customPhoneStateListener;
    TelephonyManager telephonyManager;

    NetworkChangeReceiver networkChangeReceiver;

    boolean shouldVideoStatMonitor = false;
    boolean zeroDetected = false;

    // @Inject
    SharedPreferences sp;
    @Inject
    GetMediaChannelKey getMediaChannelKey;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate called");

        initializeInjector();

        initiateRtcEngine();

        sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        // sp = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_MULTI_PROCESS);

        callData = new CallData();
        // getMediaChannelKey = new GetMediaChannelKey(new VideoDataRepository(new VideoDataStoreFactory(this)));

        // callData.setDoctorId(doctorId);

        registerPhoneStateReceiverAndGetState();
        registerNetworkStateReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.startId = startId;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called - bound to the local service");
        if (agoraHelperBinder == null) {

            setUpAPICallbackMethods();

            // Initialize the API as soon as the activity gets bound to the Service
            agoraHelper = AgoraHelper.getInstance(getApplicationContext(), agoraCallback, CallDataHelper.getCallData(sp).getDoctorAccount(sp));
            agoraHelperBinder = new AgoraHelperBinder();
        }
        return agoraHelperBinder;
    }

    @Override
    public void onDestroy() {
        unregisterPhoneStateReceiver();
        unregisterNetworkStateReceiver();
        super.onDestroy();
    }

    private void initializeInjector() {

//        coreVideoComponent = DaggerCoreVideoComponent.builder().coreVideoModule(new CoreVideoModule(this)).build();

        // coreVideoComponent.inject(this);

        DaggerServiceComponent.builder()
                //.androidModule(new AndroidModule(this))
                .coreVideoComponent(coreVideoComponent)
                .build().inject(this);
    }

    private void initiateRtcEngine() {
        // render the local view
        RtcEngineEventHandler rtcEngineEventHandler = new RtcEngineEventHandler(this);
        initializeMediaAPI(this, rtcEngineEventHandler);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        try {
            agoraAPICallback.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
        try {
            agoraAPICallback.onFirstLocalVideoFrame(width, height, elapsed);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserMuteVideo(boolean isMuted) {
        try {
            agoraAPICallback.onUserMuteVideo(isMuted);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRemoteVideoStats(int bitRate) {
        try {
            Log.d("onRemoteVideoStats", "onRemoteVideoStats: isVideoMuted - " + callData.getState().isVideoMuted() + ", zeroDetected - " + zeroDetected);
            if (shouldVideoStatMonitor) {
                if (bitRate > 100 && zeroDetected && !callData.getState().isVideoMuted()) {
                    Log.d("onRemoteVideoStats", "onRemoteVideoStats: API Callback invoked");
                    agoraAPICallback.onRemoteVideoStats(bitRate);
                    // agoraAPICallback.onFirstRemoteVideoDecoded(uid, 0, 0, 0);
                    zeroDetected = false;
                    shouldVideoStatMonitor = false;
                } else if (bitRate == 0) {
                    zeroDetected = true;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserMuteAudio(boolean isMuted) {
        try {
            agoraAPICallback.onUserMuteAudio(isMuted);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void phoneStateChanged(int phoneState) {
        CallData callData = CallDataHelper.getCallData(sp);
        switch (phoneState) {
            case TelephonyManager.CALL_STATE_IDLE:
                //when Idle i.e no call
                // Toast.makeText(this, "Phone state Idle", Toast.LENGTH_LONG).show();
                Log.i("PhoneStateListener", "Phone state Idle");
                // if was already on call the is it trying to reconnect
                callData.getState().setPhoneState(TelephonyManager.CALL_STATE_IDLE);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //when Off hook i.e in call
                //Make intent and start your service here
                // Toast.makeText(this, "Phone state Off hook", Toast.LENGTH_LONG).show();
                Log.i("PhoneStateListener", "Phone state Off hook");
                // wait for the call to reconnect for 15 secs
                callData.getState().setPhoneState(TelephonyManager.CALL_STATE_OFFHOOK);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                //when Ringing
                // Toast.makeText(this, "Phone state Ringing", Toast.LENGTH_LONG).show();
                Log.i("PhoneStateListener", "Phone state Off hook");
                // wait for the call to reconnect for 15 secs
                callData.getState().setPhoneState(TelephonyManager.CALL_STATE_RINGING);
                break;
            default:
                break;
        }

        CallDataHelper.saveCallData(callData, sp);
    }

    public void onTaskRemoved(Intent rootIntent) {
        //unregister listeners
        //do any other cleanup if required
        updateEndCallState();

        //stop service
        AgoraHelperService.this.stopSelf(startId);
    }

    @Override
    public void onConnectivityChanged(int connectivityStatus) {
        if(agoraAPICallback != null) {
            if (connectivityStatus == NETWORK_AVAILABLE) {
                agoraAPICallback.onConnectivityStatusChange(connectivityStatus);
            } else if (connectivityStatus == NETWORK_UNAVAILABLE) {
                agoraAPICallback.onConnectivityStatusChange(connectivityStatus);
            }
        }
    }

    private class AgoraHelperBinder extends Binder implements IAgoraHelper {

        public void setAgoraAPICallback(IAgoraAPICallback callback) throws RemoteException {
            agoraAPICallback = callback;
            if (CallDataHelper.getCallData(sp).getState().getVideoCallState() == VIDEO_CALL_STATE_ON_GOING) {
                agoraAPICallback.joinOnGoingCall();
            }
        }

        public RtcEngine getRtcEngine() {
            return m_agoraMedia;
        }

        @Override
        public void callStatusChanged() {
            callData = CallDataHelper.getCallData(sp);
            if (callData.getState().isVideoMuted())
                shouldVideoStatMonitor = true;
        }

        public void setCallState(int callState) throws RemoteException {

            // VIDEO_CALL_STATE = callState;
            // writeCallState(VIDEO_CALL_STATE);
        }

        public void getAgoraAPI() throws RemoteException {
            agoraHelper.getAgoraAPI();
        }

        public void getChannelID() throws RemoteException {

        }

        public void login(String account) throws RemoteException {
            // login after the API has been initialized
        }

        public void logout() throws RemoteException {
            agoraHelper.logout();
        }

        public void joinChannel(String channelName) throws RemoteException {
            agoraHelper.joinChannel(channelName);
            // the local view must be set in the activity after calling this method using the mediaChannelKey
            // m_agoraMedia.joinChannel(key, channelName, "", my_uid);
        }

        public void joinAPIChannel() throws RemoteException {

        }

        public void joinMediaChannel(String mediaChannelKey) throws RemoteException {

        }

        public void leaveAPIChannel() throws RemoteException {

        }

        public void messageChannelSend(String message) throws RemoteException {
            agoraHelper.messageChannelSend(message);
        }

        public void messageInstantSend(String account, String msg) throws RemoteException {
            agoraHelper.messageInstantSend(account, msg);
        }

        public void acceptInvite(String channelID, String account, int uid) throws RemoteException {

        }

        public void declineInvite(String channelID, String account, int uid) throws RemoteException {
            agoraHelper.declineInvite(channelID, account, uid);
        }

        public void inviteUserToJoinChannel(String patientAccount) throws RemoteException {
            sendDoctorInfo();
            agoraHelper.inviteUserToJoinChannel(patientAccount, getInfoMessage());

            inviteTryCount++;
            // CallDataHelper.getCallData(sp).setPatientUserId(); = patientAccount;
            Log.i("AgoraHelperService", "user: " + patientAccount + " - invited to join - inviteTryCount: " + inviteTryCount);
        }

        public void endInvite() throws RemoteException {
            agoraHelper.endInvite();
            // removeCallInProgressNotification();
        }

        public void setupRemoteView(int uid) throws RemoteException {

        }

        public int getCallState() {
            return CallDataHelper.getCallData(sp).getState().getVideoCallState();
        }

        public void getUID() throws RemoteException {

        }

        public void setUID(int my_uid) throws RemoteException {

        }

        public void set_state_logout() throws RemoteException {

        }

        public void set_state_login() throws RemoteException {

        }

        public void set_state_incall() throws RemoteException {

        }

        public void set_state_notincall() throws RemoteException {

        }
    }

    private void setUpAPICallbackMethods() {
        agoraCallback = new AgoraAPI.CallBack() {

            @Override
            public void onLoginSuccess(int uid, int fd) {
                super.onLoginSuccess(uid, fd);

                AgoraHelperService.this.uid = uid;

                CallData calldata = CallDataHelper.getCallData(sp);
                calldata.setUid(uid);
                calldata.getState().setVideoCallState(VIDEO_CALL_STATE_INITIATED);
                CallDataHelper.saveCallData(calldata, sp);

                // set the UID of the current doctor
                agoraHelper.setUID(uid);

                // connection had broken while the call was going
                /*if(CallDataHelper.getCallData(sp).getState().getVideoCallState() == Constant.VIDEO_CALL_STATE_ON_GOING) {

                    agoraHelper.joinAPIChannel();

                }*/ /*else if (CallDataHelper.getCallData(sp).getState().getVideoCallState() == Constant.VIDEO_CALL_STATE_ENDED) {
                    // do not join the channel instead just logout and end the service
                    agoraHelper.logout();
                }*/

                    /*try {
                        if(agoraAPICallback != null)
                            agoraAPICallback.onLoginSuccess(uid, fd);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }*/
                    getMediaChannelKey();

                // TODO this join should happen when the invite has been accepted by the peer
                // agoraHelper.joinAPIChannel();
            }

            @Override
            public void onLoginFailed(int ecode) {
                super.onLoginFailed(ecode);

                try {
                    agoraAPICallback.onLoginFailed(ecode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLogout(int ecode) {
                super.onLogout(ecode);

                /*if(CallDataHelper.getCallData(sp).getState().getVideoCallState() == Constant.VIDEO_CALL_ON_GOING) {
                    // login again
                    agoraHelper.login();
                } else if (CallDataHelper.getCallData(sp).getState().getVideoCallState() == Constant.VIDEO_CALL_ENDED) {
                    // video call login
                    AgoraHelperService.this.stopSelf();
                }*/
                //AgoraHelperService.this.stopSelf();
                try {
                    agoraAPICallback.onLogout(ecode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                AgoraHelperService.this.stopSelf(startId);
            }

            @Override
            public void onChannelJoined(String channelID) {
                super.onChannelJoined(channelID);

                // CallDataHelper.getCallData(sp).getState().setVideoCallState(Constant.VIDEO_CALL_ON_GOING);
                // VIDEO_CALL_STATE = Constant.VIDEO_CALL_ON_GOING;

                try {
                    agoraAPICallback.onChannelJoined(channelID);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                // Once the doctor has joined channel he must invite the user to join
                agoraHelper.inviteUserToJoinChannel(CallDataHelper.getCallData(sp).getUserAccount(sp), getInfoMessage());
                Log.i(TAG, "user invited to join");
            }

            @Override
            public void onChannelLeaved(String channelID, int ecode) {
                super.onChannelLeaved(channelID, ecode);

                if (CallDataHelper.getCallData(sp).getState().getVideoCallState() == Constant.VIDEO_CALL_STATE_ENDED) {
                    // Do nothing, just logout
                } else if (CallDataHelper.getCallData(sp).getState().getVideoCallState() == Constant.VIDEO_CALL_STATE_ON_GOING) {
                    updateEndCallState();
                }

                agoraHelper.logout();
            }

            @Override
            public void onChannelUserLeaved(String account, int uid) {
                super.onChannelUserLeaved(account, uid);
                try {
                    // If the user who has left channel is server check if account has "server_channel" in the account name then end call and logout
                    if (account.contains("server_channel")) {
                        // then check the state and logout
                        agoraHelper.endInvite();
                        //updateEndCallState();
                    } else {
                        if (agoraAPICallback != null)
                            agoraAPICallback.onChannelUserLeaved(account, uid);
                    }
                } catch (RemoteException e) {

                }
            }

            @Override
            public void onChannelJoinFailed(String channelID, int ecode) {
                super.onChannelJoinFailed(channelID, ecode);

                try {
                    agoraAPICallback.onChannelJoinFailed(channelID, ecode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChannelUserList(String[] accounts, int[] uids) {
                super.onChannelUserList(accounts, uids);

                List<String> accountsList = new ArrayList<>();
                List<String> uidList = new ArrayList<>();

                for (int i = 0; i < accounts.length; i++) {
                    accountsList.add(i, accounts[i]);
                    uidList.add(i, String.valueOf(uids[i]));
                }

                try {
                    agoraAPICallback.onChannelUserList(accountsList, uidList);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessageChannelReceive(String channelID, String account, int uid, String msg) {
                super.onMessageChannelReceive(channelID, account, uid, msg);
                try {
                    if (msg.equalsIgnoreCase("end_call"))
                        agoraHelper.endInvite();
                    else if (msg.equalsIgnoreCase("user_busy_on_another_call"))
                        agoraHelper.endInvite();
                    else
                        agoraAPICallback.onMessageChannelReceive(channelID, account, uid, msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessageInstantReceive(String account, int uid, String msg) {
                super.onMessageInstantReceive(account, uid, msg);

                Log.d(TAG, "instant message received");

                if (AgoraMessageHelper.getInfoMessage(msg).getMessageType().equals(VIDEO_MESSAGE_SEND_USER_INFO)) {
                    sendDoctorInfo();
                } else if (msg.equalsIgnoreCase("user_busy_on_another_call")) {
                    agoraHelper.endInvite();
                    // show message stating the user is busy on another call
                }

                try {
                    agoraAPICallback.onMessageInstantReceive(account, uid, msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onInviteMsg(String channelID, String account, int uid, String msgType, String msgData, String extra) {
                super.onInviteMsg(channelID, account, uid, msgType, msgData, extra);
            }

            @Override
            public void onInviteReceived(String channelID, String account, int uid, String extra) {
                super.onInviteReceived(channelID, account, uid, extra);
                try {
                    agoraAPICallback.onInviteReceived(channelID, account, uid, extra);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onInviteReceivedByPeer(String channelID, String account, int uid) {
                super.onInviteReceivedByPeer(channelID, account, uid);

                CallData callData = CallDataHelper.getCallData(sp);
                callData.getState().setVideoCallState(VIDEO_CALL_STATE_INVITE_RECEIVED_BY_USER);
                CallDataHelper.saveCallData(callData, sp);

                CallDataHelper.notifyAgoraCallStatus(AgoraHelperService.this);

                try {
                    agoraAPICallback.onInviteReceivedByPeer(channelID, account, uid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onInviteFailed(String channelID, String account, int uid, int ecode, String extra) {
                super.onInviteFailed(channelID, account, uid, ecode, extra);

                if (inviteTryCount < 2) {
                    agoraHelper.inviteUserToJoinChannel(CallDataHelper.getCallData(sp).getUserAccount(sp), getInfoMessage());
                    inviteTryCount++;
                    Log.i("AgoraHelperService", "user: " + CallDataHelper.getCallData(sp).getUserAccount(sp) + " - invited to join - inviteTryCount: " + inviteTryCount);
                } else {
                    updateEndCallState();
                    agoraHelper.messageChannelSend("invite_sent_failed");
                    try {
                        agoraAPICallback.onInviteFailed(channelID, account, uid, ecode, extra);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onInviteRefusedByPeer(String channelID, String account, int uid, String extra) {
                super.onInviteRefusedByPeer(channelID, account, uid, extra);

                updateEndCallState();

                try {
                    agoraAPICallback.onInviteRefusedByPeer(channelID, account, uid, extra);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onInviteAcceptedByPeer(String channelID, String account, int uid, String extra) {
                super.onInviteAcceptedByPeer(channelID, account, uid, extra);

                // showCallInProgressNotification(channelID, account, uid);
                CallData callData = CallDataHelper.getCallData(sp);
                callData.getState().setVideoCallState(VIDEO_CALL_STATE_ON_GOING);
                CallDataHelper.saveCallData(callData, sp);

                try {
                    joinMediaChannel();
                    agoraAPICallback.onInviteAcceptedByPeer(channelID, account, uid, extra);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onInviteEndByPeer(String channelID, String account, int uid, String extra) {
                super.onInviteEndByPeer(channelID, account, uid, extra);

                // removeCallInProgressNotification();
                Log.d(TAG, "onInviteEndByPeer");

                updateEndCallState();

                try {
                    // if invite is onGoing
                    // agoraHelper.endInvite();

                    agoraAPICallback.onInviteEndByPeer(channelID, account, uid, extra);
                    agoraHelper.setInviteAvailable(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onInviteEndByMyself(String channelID, String account, int uid) {
                super.onInviteEndByMyself(channelID, account, uid);

                Log.d(TAG, "onInviteEndByMyself");
                // removeCallInProgressNotification();
                updateEndCallState();

                try {
                    agoraAPICallback.onInviteEndByMyself(channelID, account, uid);
                    agoraHelper.setInviteAvailable(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLog(String txt) {
                super.onLog(txt);
                // appendLog(txt);
            }
        };
    }

    private void updateEndCallState() {

        if(m_agoraMedia != null) {
            m_agoraMedia.leaveChannel();
            m_agoraMedia.destroy();
        }

        if(agoraHelper != null)
            agoraHelper.leaveAPIChannel();

        callData = CallDataHelper.getCallData(sp);
        callData.getState().setVideoCallState(Constant.VIDEO_CALL_STATE_ENDED);
        CallDataHelper.saveCallData(callData, sp);

        CallDataHelper.notifyAgoraCallStatus(getApplicationContext());
    }

    // Initialize Rtc Engine
    /*private void initiateRtcEngine() {
        // render the local view
        RtcEngineEventHandler rtcEngineEventHandler = new RtcEngineEventHandler(this, this, viewBinding.videoContainer.remoteVideoViewContainer);

        initializeMediaAPI(this, rtcEngineEventHandler);
        renderLocalView();
    }*/

    // Initialize media engine and set callback for media engine events
    void initializeMediaAPI(Context context, RtcEngineEventHandler rtcEngineEventHandler) {

        try {
            m_agoraMedia = RtcEngine.create(context, appID, rtcEngineEventHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // set Rtc Engine to render view
        // rtcEngineEventHandler.setRtcEngine(m_agoraMedia);

        // default setup values
        m_agoraMedia.enableAudioVolumeIndication(1000, 3);
        m_agoraMedia.setParameters("{\"rtc.log_filter\":32783}");//0x800f, log to console
        m_agoraMedia.setLogFilter(32783);
    }

    /**
     * Once the remote user has accepted and join channel, doctor must also join the media channel
     * to start receiving video from user
     */
    private void joinMediaChannel() {
        // true - join call by generating a local key
        // false - use the key generated from server
        boolean enableMediaCertificate = true;

        String key = appID;
        if (enableMediaCertificate) {
            int tsWrong = (int) (new Date().getTime() / 1000);
            int ts = (int) (System.currentTimeMillis() / 1000);

            int r = new Random().nextInt();
            // long uid = uid;
            int expiredTs = 0;

            try {
                //key = DynamicKey5.generateMediaChannelKey(appID, certificate, channelName, ts, r, uid, expiredTs);
                key = DynamicKey4.generateMediaChannelKey(appID, certificate, CallDataHelper.getCallData(sp).getChannelName(), ts, r, uid, expiredTs);
            } catch (Exception e) {
                e.printStackTrace();
            }

            m_agoraMedia.joinChannel(key, CallDataHelper.getCallData(sp).getChannelName(), "", uid);

        } else {
            // m_agoraMedia.joinChannel(viewModel.mediaChannelKey.getValue(), CallDataHelper.getCallData(sp).getChannelName(), "", uid);
        }
    }

    private void sendDoctorInfo() {

        InfoMessage infoMessage = new InfoMessage();
        CallData callData = CallDataHelper.getCallData(sp);

        Doctor doctor = new Doctor(callData.getDoctorId(), callData.getDoctorName(), callData.getDoctorImage());
        infoMessage.setDoctor(doctor);
        infoMessage.setPatientName(callData.getPatientName());
        //infoMessage.setDoctor(callData.getPatientUserId());

        agoraHelper.messageInstantSend(callData.getUserAccount(sp), gson.toJson(infoMessage, InfoMessage.class));
    }

    private String getInfoMessage() {
        InfoMessage infoMessage = new InfoMessage();
        CallData callData = CallDataHelper.getCallData(sp);

        Doctor doctor = new Doctor(callData.getDoctorId(), callData.getDoctorName(), callData.getDoctorImage());
        infoMessage.setDoctor(doctor);
        infoMessage.setPatientName(callData.getPatientName());

        return gson.toJson(infoMessage);
    }

    private void registerPhoneStateReceiverAndGetState() {

        customPhoneStateListener = new CustomPhoneStateListener(getApplicationContext(), this);

        IntentFilter phoneStateFilter = new IntentFilter();
        phoneStateFilter.addAction(ACTION_PHONE_STATE_CHANGED);

        CallData callData = CallDataHelper.getCallData(sp);

        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        callData.getState().setPhoneState(telephonyManager.getCallState());
        CallDataHelper.saveCallData(callData, sp);

        telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void unregisterPhoneStateReceiver() {
        telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    private void registerNetworkStateReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        networkChangeReceiver = new NetworkChangeReceiver(this);

        this.registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void unregisterNetworkStateReceiver() {
        this.unregisterReceiver(networkChangeReceiver);
    }

    private void getMediaChannelKey() {
        HashMap<String, String> params = new HashMap<>();
        params.put("channel_name", CallDataHelper.getCallData(sp).getChannelName());
        params.put("uid", String.valueOf(uid));
        params.put("patient_user_id", CallDataHelper.getCallData(sp).getPatientUserId());

        getMediaChannelKey.execute(new DisposableObserver<MediaChannelResponse>() {
            @Override
            public void onNext(MediaChannelResponse mediaChannelResponse) {
                // this steps involve 2 steps: 1) join API channel, 2) join media channel and then render the local view container
                // agoraHelper.joinChannel(CallDataHelper.getCallData(sp).getChannelName());
            }

            @Override
            public void onError(Throwable e) {


                // some error has occurred and must be notified gracefully
                /*Log.d(TAG, "HTTP Exception occured");
                updateEndCallState();

                try {
                    agoraAPICallback.onInviteEndByMyself(callData.getChannelName(), callData.getDoctorAccount(sp), 0);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }

                agoraHelper.logout();*/

                agoraHelper.joinChannel(CallDataHelper.getCallData(sp).getChannelName());
            }

            @Override
            public void onComplete() {
                agoraHelper.joinChannel(CallDataHelper.getCallData(sp).getChannelName());
            }
        }, params);
    }
}