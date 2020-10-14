package com.sftelehealth.doctor.data.repository.datastore;

import android.content.Context;

import com.sftelehealth.doctor.data.model.request.MediaChannelKeyRequest;
import com.sftelehealth.doctor.data.model.response.MediaChannelResponse;
import com.sftelehealth.doctor.data.net.RestApi;
import com.sftelehealth.doctor.data.net.RetrofitService;

import javax.inject.Inject;

import io.reactivex.Observable;

public class VideoDataStoreFactory implements VideoDataStore {

    private final Context context;
    boolean isUserRegistered;
    boolean isCached = false;

    //@Inject DoctorDatabase_Impl database;

    RetrofitService restService = new RetrofitService();
    RestApi restApi = restService.getRestApiService();

    @Inject
    public VideoDataStoreFactory(Context context) {
        this.context = context;
    }

    @Override
    public Observable<MediaChannelResponse> getMediaChannelKey(String channelName, String uid, String patientUserId) {
        MediaChannelKeyRequest request = new MediaChannelKeyRequest();
        request.setChannelName(channelName);
        request.setUid(uid);
        request.setExpiredTs(0);
        request.setUserId(Integer.valueOf(patientUserId));

//        if(channelName.contains("callback"))
//            request.setEmergencyCall(false);
//        else
            request.setEmergencyCall(true);

        return restApi.getMediaChannelKey(request);
    }
}
