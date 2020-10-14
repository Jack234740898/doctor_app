package com.sftelehealth.doctor.data.repository.datastore;

import com.sftelehealth.doctor.data.database.entity.SystemInfo;
import com.sftelehealth.doctor.data.model.request.LoginRequest;
import com.sftelehealth.doctor.data.model.response.IsUserAuthenticatedResponse;
import com.sftelehealth.doctor.data.model.response.LoginResponse;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;
import com.sftelehealth.doctor.data.model.response.ShouldUpdateResponse;
import com.sftelehealth.doctor.data.model.response.UpdateDoctorResponse;

import java.util.HashMap;

import io.reactivex.Observable;

/**
 * Created by Rahul on 25/06/17.
 */

public interface SystemDataStore {
    /**
     *
     * @param phone
     * @return
     */
    Observable<SendOtpResponse> checkRegistration(String phone);

    /**
     *
     * @param login
     * @return
     */
    Observable<LoginResponse> doctorVerify(LoginRequest login);

    /**
     *
     * @return
     */
    Observable<IsUserAuthenticatedResponse> isUserAuthenticated();

    /**
     *
     * @return
     */
    Observable<ShouldUpdateResponse> shouldUpdate();

    /**
     *
     * @return
     */
    Observable<UpdateDoctorResponse> updateDoctor(HashMap<String, String> params);

    /**
     *
     * @return
     */
    Observable<SystemInfo> getSystemInfoFromPersistence();
}
