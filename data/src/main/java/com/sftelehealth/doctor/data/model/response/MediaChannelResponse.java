package com.sftelehealth.doctor.data.model.response;

public class MediaChannelResponse {

    String mediaChannelKey;

    public MediaChannelResponse(String mediaChannelKey) {
        this.mediaChannelKey = mediaChannelKey;
    }

    public String getMediaChannelKey() {
        return mediaChannelKey;
    }

    public void setMediaChannelKey(String mediaChannelKey) {
        this.mediaChannelKey = mediaChannelKey;
    }
}