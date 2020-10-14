package com.sftelehealth.doctor.domain.repository;


import com.sftelehealth.doctor.domain.model.Doctor;
import com.sftelehealth.doctor.domain.model.SystemInfo;
import com.sftelehealth.doctor.domain.model.response.IsUserAuthenticatedResponse;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;
import com.sftelehealth.doctor.domain.model.response.ShouldUpdateResponse;
import com.sftelehealth.doctor.domain.model.response.UpdateDoctorResponse;


import java.util.HashMap;

import io.reactivex.Observable;

/**
 * Created by Rahul on 20/06/17.
 */

public interface SystemRepository {

    /**
     *
     * @param phone
     * @return
     */
    Observable<SendOtpResponse> checkRegistration(String phone);

    /**
     *
     * @param phone
     * @param otp
     * @return
     */
    Observable<Doctor> doctorVerify(String phone, String otp, String versionName);

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
