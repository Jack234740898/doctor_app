package com.sftelehealth.doctor.video.agora;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.sftelehealth.doctor.video.BuildConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import androidx.annotation.UiThread;
import io.agora.AgoraAPI;
import io.agora.AgoraAPIOnlySignal;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

// import io.agora.AgoraAPI;
// import io.agora.AgoraAPIOnlySignal;

/**
 * Created by Rahul on 27/06/17.
 */

public class AgoraHelper {

    public static final String appID = BuildConfig.AGORA_APP_ID;                //"1f2f1b04d7904603ad97d1a373f45153";
    public static final String certificate = BuildConfig.AGORA_CERTIFICATE;     //"4c387d04005b429da3774ad63c4d9445";
    public static final boolean enableMediaCertificate =  true;

    private AgoraAPIOnlySignal m_agoraAPI;
    private RtcEngine m_agoraMedia;

    int msgid = 0;
    private int video_peer_uid = 0;
    private SurfaceView remoteView;

    boolean isLogin = false;
    boolean m_iscalling = false;
    boolean m_isjoin = false;
    boolean m_env_dbg = false;
    SurfaceView mLocalView = null;

    private String channelName = "";
    private String account = "";
    private String peerAccount = "";

    boolean inviteAvailable = false;

    int my_uid = 0;

    private final String TAG = "AgoraHelper";

    protected static AgoraHelper instance = null;
    private Context context;

    public static AgoraHelper getInstance(Context context, AgoraAPI.CallBack agoraAPICallback, String account) {

        if(instance == null) {
            instance = new AgoraHelper(context);
            instance.setAgoraAPICallback(agoraAPICallback);
            //remove instance.initializeMediaAPI(context, rtcEngineEventHandler);
        } else {
            instance.setAgoraAPICallback(agoraAPICallback);
            //remove instance.initializeMediaAPI(context, rtcEngineEventHandler);
        }

        instance.context = context;
        //TODO NEED LOGOUT
        if(!instance.isLogin || !instance.account.equals(account)) {
            instance.account = account;
            instance.login();
        }
        return instance;
    }

    // Create Agora instance
    AgoraHelper(Context context) {
        m_agoraAPI = AgoraAPIOnlySignal.getInstance(context, appID);
    }

    // set callback for Agora API signal instance
    void setAgoraAPICallback(AgoraAPI.CallBack agoraAPICallback) {
        m_agoraAPI.callbackSet(agoraAPICallback);
    }


    public AgoraAPIOnlySignal getAgoraAPI() {
        return m_agoraAPI;
    }

    public void login() {

        set_state_login();

        Log.d(TAG, "Login : appID=" + appID + ", account=" + account);
        long expiredTimeWrong = new Date().getTime()/1000 + 3600;
        long expiredTime = System.currentTimeMillis()/1000 + 3600;
        String token = calcToken(appID, certificate, account, expiredTime);
        // m_agoraAPI.login(appID, account, token, 0, "");
        m_agoraAPI.login2(appID, this.account, token, 0, "", 60, 5);
    }

    public void logout() {

        set_state_logout();

        Log.d(TAG,"Logout");
        m_agoraAPI.logout();
    }

    public void endInvite() {

        m_agoraAPI.channelInviteEnd(channelName, peerAccount, 0);
        m_agoraAPI.channelLeave(channelName);

        //m_agoraAPI.logout();
    }

    /**
     * Join a channel in the service
     * @param channelName
     */
    public void joinChannel(String channelName) {

        this.channelName = channelName;

        joinAPIChannel();
    }

    /**
     * Function to join API channel
     */
    public void joinAPIChannel() {
        m_isjoin = true;

        Log.d(TAG, "Join channel " + channelName);
        m_agoraAPI.channelJoin(channelName);
    }

    /**
     * Function to leave API channel
     */
    public void leaveAPIChannel() {
        m_agoraAPI.channelLeave(channelName);
        instance.setAgoraAPICallback(null);
    }

    /**
     * Function to leave a media channel
     * @param remoteViewContainer
     */
    @UiThread
    private void leaveMediaChannel(FrameLayout remoteViewContainer) {
        m_isjoin = false;

        video_peer_uid = 0;
        if (remoteView != null){
            remoteViewContainer.removeAllViews();
            remoteView = null;
        }

        Log.d(TAG, "Leave channel " + channelName);
        m_agoraMedia.leaveChannel();

        // call {@link leaveAPIChannel}
    }

    public void acceptInvite(String channelID, String account, int uid) {

        channelName = channelID;
        peerAccount = account;

        m_agoraAPI.channelInviteAccept(channelID, account, uid, "");
    }

    public void declineInvite(String channelID, String account, int uid) {

        channelName = channelID;
        peerAccount = account;

        m_agoraAPI.channelInviteRefuse(channelID, account, uid, "");
    }

    public boolean isInviteAvailable() {
        return inviteAvailable;
    }

    public void setInviteAvailable(boolean inviteAvailable) {
        this.inviteAvailable = inviteAvailable;
    }

    public void inviteUserToJoinChannel(String patientAccount, String jsonMessage) {

        // set the peer account for invite
        peerAccount = patientAccount;
        m_agoraAPI.channelInviteUser2(channelName, peerAccount, jsonMessage);

        //m_agoraAPI.channelInvitePhone3(channelName, peerAccount, account, "{\"sip_header:myheader\":\"gogo\"}");
        inviteAvailable = true;
    }

    public void messageChannelSend(String message) {
        m_agoraAPI.messageChannelSend(channelName, message, "");
    }

    public void messageInstantSend(String account, String msg) {
        m_agoraAPI.messageInstantSend(account, 0, msg, "");
    }

    @UiThread
    public void setupRemoteView(final int uid, FrameLayout remoteViewContainer) {
        if (video_peer_uid == uid) {
            return;
        }

        video_peer_uid = uid;
        if (remoteView == null) {
            remoteView = RtcEngine.CreateRendererView(context);
            remoteView.setZOrderMediaOverlay(true);
            remoteViewContainer.removeAllViews();
            remoteViewContainer.addView(remoteView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }

        int successCode = m_agoraMedia.setupRemoteVideo(new VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));

        if (successCode < 0) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    m_agoraMedia.setupRemoteVideo(new VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
                    remoteView.invalidate();
                }
            }, 500);
        }
    }

    public int getUID() {
        return my_uid;
    }

    public void setUID(int my_uid) {
        this.my_uid = my_uid;
    }

    public void set_state_logout() {
        isLogin = false;
    }

    public void set_state_login() {
        isLogin = true;
    }

    public void set_state_incall() {
        m_iscalling = true;
    }

    public void set_state_notincall() {
        m_iscalling = false;
    }

    public static String hexlify(byte[] data){
        char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        /**
         * 用于建立十六进制字符的输出的大写字符数组
         */
        char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        char[] toDigits = DIGITS_LOWER;
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return String.valueOf(out);
    }

    public static String md5hex(byte[] s){
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(s);
            return hexlify(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String calcToken(String appID, String certificate, String account, long expiredTime){
        // Token = 1:appID:expiredTime:sign
        // Token = 1:appID:expiredTime:md5(account + vendorID + certificate + expiredTime)

        String sign = md5hex((account + appID + certificate + expiredTime).getBytes());
        return "1:" + appID + ":" + expiredTime + ":" + sign;
    }
}