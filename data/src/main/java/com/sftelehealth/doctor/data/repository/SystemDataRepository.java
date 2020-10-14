package com.sftelehealth.doctor.data.repository;

import com.sftelehealth.doctor.data.entity.mapper.DoctorEntityMapper;
import com.sftelehealth.doctor.data.model.request.LoginRequest;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;
import com.sftelehealth.doctor.data.repository.datastore.SystemDataStore;
import com.sftelehealth.doctor.domain.model.Doctor;
import com.sftelehealth.doctor.domain.model.SystemInfo;
import com.sftelehealth.doctor.domain.model.response.IsUserAuthenticatedResponse;
import com.sftelehealth.doctor.domain.model.response.ShouldUpdateResponse;
import com.sftelehealth.doctor.domain.model.response.UpdateDoctorResponse;
import com.sftelehealth.doctor.domain.repository.SystemRepository;

import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by Rahul on 25/06/17.
 */

public class SystemDataRepository implements SystemRepository {

    private final SystemDataStore systemDataStore;

    @Inject
    SystemDataRepository(SystemDataStore systemDataStore) {
        this.systemDataStore = systemDataStore;
    }

    @Override
    public Observable<SendOtpResponse> checkRegistration(String phone) {
        return systemDataStore.checkRegistration(phone);
    }

    @Override
    public Observable<Doctor> doctorVerify(String phone, String otp, String versionName) {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPhone(phone);
        loginRequest.setOtp(otp);
        loginRequest.setAppVersion(versionName);

        /*
        return systemDataStoreFactory.doctorVerify(loginRequest).map(new Function<LoginResponse, Doctor>() {
            @Override
            public Doctor apply(LoginResponse loginResponse) throws Exception {
                return loginResponse.getDoctor();
            }
        });
        */

        return systemDataStore.doctorVerify(loginRequest).map(loginResponse -> {

            DoctorEntityMapper mapper = new DoctorEntityMapper();

            return mapper.transform(loginResponse.getDoctor());
        });
    }

    @Override
    public Observable<IsUserAuthenticatedResponse> isUserAuthenticated() {

        return systemDataStore.isUserAuthenticated().map(isUserAuthenticatedResponse -> {

            IsUserAuthenticatedResponse response = new IsUserAuthenticatedResponse();

            response.setTokenAvailable(isUserAuthenticatedResponse.isTokenAvailable());
            response.setDoctorObjectAvailable(isUserAuthenticatedResponse.isDoctorObjectAvailable());
            response.setDoctorDataSet(isUserAuthenticatedResponse.isDoctorDataSet());

            return response;
        });
    }

    @Override
    public Observable<ShouldUpdateResponse> shouldUpdate() {
        return systemDataStore.shouldUpdate().map(shouldUpdateResponse -> {

            ShouldUpdateResponse response = new ShouldUpdateResponse();

            response.setMustUpdate(shouldUpdateResponse.isMustUpdate());
            response.setShouldUpdate(shouldUpdateResponse.isShouldUpdate());
            response.setPhone(shouldUpdateResponse.getPhone());
            response.setCountryCode(shouldUpdateResponse.getCountryCode());
            response.setCountryISDCode(shouldUpdateResponse.getCountryISDCode());
            response.setDialerButtonVisibleTime(shouldUpdateResponse.getDialerButtonVisibleTime());
            response.setFreeFollowUpTime(shouldUpdateResponse.getFreeFollowUpTime());
            response.setAppointmentDaysLimit(shouldUpdateResponse.getAppointmentDaysLimit());
            response.setVideoAcceptWaitTime(shouldUpdateResponse.getVideoAcceptWaitTime());

            return response;
        });
    }

    @Override
    public Observable<UpdateDoctorResponse> updateDoctor(HashMap<String, String> params) {
        return systemDataStore.updateDoctor(params).map(updateDoctorResponse -> {
            UpdateDoctorResponse response = new UpdateDoctorResponse();

            return response;
        });
    }

    @Override
    public Observable<SystemInfo> getSystemInfoFromPersistence() {
        return systemDataStore.getSystemInfoFromPersistence().map(systemInfo -> {
            SystemInfo response = new SystemInfo();
            response.setShouldUpdate(systemInfo.isShouldUpdate());
            response.setMustUpdate(systemInfo.isMustUpdate());
            response.setPhone(systemInfo.getPhone());
            response.setVideoAcceptWaitTime(systemInfo.getVideoAcceptWaitTime());
            response.setFreeFollowUpTime(systemInfo.getFreeFollowUpTime());
            response.setDialerButtonVisibleTime(systemInfo.getDialerButtonVisibleTime());
            response.setCountryISDCode(systemInfo.getCountryISDCode());
            response.setCountryCode(systemInfo.getCountryCode());

            return response;
        });
    }
}
