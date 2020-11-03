package com.sftelehealth.doctor.video.view.call.rtcEngineEvent;

public interface IRtcEngineEventListener {

    void joinChannelSuccess(int uid);

    void setupRemoteVideo(int uid);

    void remoteUserLeft();

    void error(int errorCode);

    void leaveChannel(int totalDuration);
}
