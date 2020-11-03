package com.sftelehealth.doctor.video.view.call;

import android.view.SurfaceView;

public interface OnActionClickListener {
    void setupLocalVideo(SurfaceView mLocalView);

    void switchCamera();

    void audioMute(boolean mMuted);

    void remoteVideo(SurfaceView mRemoteView, int uid);

    void setHandlerListener(OnHandlerEventListener listener);

    void leaveChannel();

}
