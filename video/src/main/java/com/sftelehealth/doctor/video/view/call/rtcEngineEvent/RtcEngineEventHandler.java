package com.sftelehealth.doctor.video.view.call.rtcEngineEvent;

import io.agora.rtc.IRtcEngineEventHandler;

public class RtcEngineEventHandler extends IRtcEngineEventHandler {

    private IRtcEngineEventListener iRtcEngineEventListener;
    private boolean isChannelJoined = false;

    public RtcEngineEventHandler(IRtcEngineEventListener iRtcEngineEventListener) {
        this.iRtcEngineEventListener = iRtcEngineEventListener;
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onJoinChannelSuccess(channel, uid, elapsed);
        if (!isChannelJoined) {
            isChannelJoined = true;
            iRtcEngineEventListener.joinChannelSuccess(uid);
        }
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
        iRtcEngineEventListener.setupRemoteVideo(uid);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        super.onUserOffline(uid, reason);
        iRtcEngineEventListener.remoteUserLeft();
    }

    @Override
    public void onLeaveChannel(RtcStats rtcStats) {
        super.onLeaveChannel(rtcStats);
        isChannelJoined = false;
        iRtcEngineEventListener.leaveChannel(rtcStats.totalDuration);
    }

    @Override
    public void onError(int errorCode) {
        super.onError(errorCode);
        iRtcEngineEventListener.error(errorCode);
    }





}
