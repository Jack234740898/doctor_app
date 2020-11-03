package com.sftelehealth.doctor.video.view.call.signalEvent;

import android.util.Log;

import io.agora.AgoraAPI;
import io.agora.IAgoraAPI;
import io.agora.NativeAgoraAPI;

public class AgoraSignalHandler extends NativeAgoraAPI.CallBack {

    private IAgoraSignalEventListener listener;
    private static final String TAG = "AgoraSignalHandler";

    public AgoraSignalHandler(IAgoraSignalEventListener listener) {
        this.listener = listener;
    }

    public IAgoraAPI.ICallBack getCallBack(){
        return this;
    }

    @Override
    public void onReconnecting(int i) {
        Log.d(TAG, "onReconnecting: ");
    }

    @Override
    public void onReconnected(int i) {
        Log.d(TAG, "onReconnected: ");
    }

    @Override
    public void onLoginSuccess(int uId, int i1) {
        listener.loginSuccess(uId, i1);
    }

    @Override
    public void onLogout(int i) {
        Log.d(TAG, "onLogout: ");
        listener.logoutSuccess();
    }

    @Override
    public void onLoginFailed(int i) {
        Log.d(TAG, "onLoginFailed: ");
    }

    @Override
    public void onChannelJoined(String s) {
        Log.d(TAG, "onChannelJoined: ");
    }

    @Override
    public void onChannelJoinFailed(String s, int i) {
        Log.d(TAG, "onChannelJoinFailed: ");
    }

    @Override
    public void onChannelLeaved(String s, int i) {
        Log.d(TAG, "onChannelLeaved: ");
    }

    @Override
    public void onChannelUserJoined(String s, int i) {
        Log.d(TAG, "onChannelUserJoined: ");
//        listener.userRejoinedInChannel();
    }

    @Override
    public void onChannelUserLeaved(String s, int i) {
        Log.d(TAG, "onChannelUserLeaved: ");
        listener.logoutSuccess();
    }

    @Override
    public void onChannelUserList(String[] strings, int[] ints) {
        listener.channelUserList(strings, ints);
    }

    @Override
    public void onChannelQueryUserNumResult(String s, int i, int i1) {

    }

    @Override
    public void onChannelQueryUserIsIn(String s, String s1, int i) {

    }

    @Override
    public void onChannelAttrUpdated(String s, String s1, String s2, String s3) {

    }

    @Override
    public void onInviteReceived(String s, String s1, int i, String s2) {

    }

    @Override
    public void onInviteReceivedByPeer(String s, String s1, int i) {

    }

    @Override
    public void onInviteAcceptedByPeer(String s, String s1, int i, String s2) {

    }

    @Override
    public void onInviteRefusedByPeer(String s, String s1, int i, String s2) {

    }

    @Override
    public void onInviteFailed(String s, String s1, int i, int i1, String s2) {

    }

    @Override
    public void onInviteEndByPeer(String s, String s1, int i, String s2) {

    }

    @Override
    public void onInviteEndByMyself(String s, String s1, int i) {

    }

    @Override
    public void onInviteMsg(String s, String s1, int i, String s2, String s3, String s4) {

    }

    @Override
    public void onMessageSendError(String s, int i) {

    }

    @Override
    public void onMessageSendProgress(String s, String s1, String s2, String s3) {

    }

    @Override
    public void onMessageSendSuccess(String s) {

    }

    @Override
    public void onMessageAppReceived(String s) {

    }

    @Override
    public void onMessageInstantReceive(String s, int i, String s1) {

    }

    @Override
    public void onMessageChannelReceive(String s, String s1, int i, String s2) {

    }

    @Override
    public void onLog(String s) {

    }

    @Override
    public void onInvokeRet(String s, String s1, String s2) {

    }

    @Override
    public void onMsg(String s, String s1, String s2) {

    }

    @Override
    public void onUserAttrResult(String s, String s1, String s2) {

    }

    @Override
    public void onUserAttrAllResult(String s, String s1) {

    }

    @Override
    public void onError(String s, int i, String s1) {

    }

    @Override
    public void onQueryUserStatusResult(String s, String s1) {

    }

    @Override
    public void onDbg(String s, byte[] bytes) {

    }

    @Override
    public void onBCCall_result(String s, String s1, String s2) {

    }
}
