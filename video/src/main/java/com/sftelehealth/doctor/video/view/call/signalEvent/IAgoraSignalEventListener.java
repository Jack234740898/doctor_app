package com.sftelehealth.doctor.video.view.call.signalEvent;

public interface IAgoraSignalEventListener {

    void loginSuccess(int uId, int i1);

    void channelUserList(String[] strings, int[] ints);

    void userRejoinedInChannel();

    void logoutSuccess();

    void channelJoined(String s);
}
