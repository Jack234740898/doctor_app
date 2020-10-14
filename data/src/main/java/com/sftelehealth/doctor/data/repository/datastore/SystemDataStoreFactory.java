package com.sftelehealth.doctor.data.repository.datastore;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.sftelehealth.doctor.data.Constant;
import com.sftelehealth.doctor.data.database.DatabaseObject;
import com.sftelehealth.doctor.data.database.entity.SystemInfo;
import com.sftelehealth.doctor.data.database.mapper.DBDoctorEntityMapper;
import com.sftelehealth.doctor.data.database.mapper.DBSystemInfoEntityMapper;
import com.sftelehealth.doctor.data.model.Doctor;
import com.sftelehealth.doctor.data.model.request.LoginRequest;
import com.sftelehealth.doctor.data.model.request.SendOtpRequest;
import com.sftelehealth.doctor.data.model.request.UpdateDoctorRequest;
import com.sftelehealth.doctor.data.model.response.IsUserAuthenticatedResponse;
import com.sftelehealth.doctor.data.model.response.LoginResponse;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;
import com.sftelehealth.doctor.data.model.response.ShouldUpdateResponse;
import com.sftelehealth.doctor.data.model.response.UpdateDoctorResponse;
import com.sftelehealth.doctor.data.net.AuthCodeProvider;
import com.sftelehealth.doctor.data.net.CoreApi;
import com.sftelehealth.doctor.data.net.RetrofitService;

import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Rahul on 26/06/17.
 */

public class SystemDataStoreFactory implements SystemDataStore {

    private final Context context;
    boolean isUserRegistered;
    boolean isCached = false;

    //@Inject DoctorDatabase_Impl database;

    RetrofitService restService = new RetrofitService();
    CoreApi coreApi = restService.getCoreApiService();

    @Inject
    public SystemDataStoreFactory(Context context) {
        this.context = context;
    }

    @Override
    public Observable<SendOtpResponse> checkRegistration(String phone) {

        SendOtpRequest otpRequest = new SendOtpRequest();
        otpRequest.setPhone(phone);

        return coreApi.checkRegistration(otpRequest).map(sendOtpResponse -> sendOtpResponse);
    }

    @Override
    public Observable<LoginResponse> doctorVerify(LoginRequest login) {
        return coreApi.doctorVerify(login).map(loginResponse -> {

            /*// map to LoginResponse class of data module
            LoginResponse loginResponseData = new LoginResponse();

            loginResponseData.setDoctor(loginResponse.getDoctor());
            loginResponseData.setToken(loginResponse.getToken());*/

            // if the token is not present in the doctor object then do add to it, so that its convenient to store
            loginResponse.getDoctor().setToken(loginResponse.getToken());

            AuthCodeProvider
                    .getSharedPreference(context)
                    .edit()
                    .putString("auth_code", "Bearer " + loginResponse.getToken())
                    .commit();

            // DB Mapper to map the response object to DB object
            DBDoctorEntityMapper dbMapper = new DBDoctorEntityMapper();

            // Save the entry in the doctor DB
            DatabaseObject.getInstance(context)
                    .doctorDao()
                    .insertDoctor(dbMapper.transform(loginResponse.getDoctor()));

            return loginResponse;
        });
    }

    @Override
    public Observable<IsUserAuthenticatedResponse> isUserAuthenticated() {

        /*if(AuthCodeProvider.getOldSharedPreference(context).getString("auth_code", null) != null) {
            AuthCodeProvider.getSharedPreference(context).edit().putString("auth_code", AuthCodeProvider.getOldSharedPreference(context).getString("auth_code", null)).apply();
        }*/

        return Observable.just(AuthCodeProvider.getSharedPreference(context).getString("auth_code", null) == null ? false : true).map(isTokenAvailable -> {
            DBDoctorEntityMapper mapper = new DBDoctorEntityMapper();

            IsUserAuthenticatedResponse response = new IsUserAuthenticatedResponse();

            response.setDoctorObjectAvailable(false);
            response.setTokenAvailable(isTokenAvailable);
            response.setDoctorDataSet(false);
            response.setHasMigratedToDoctorCategories(false);

            if (response.isTokenAvailable()) {
                // When the app transitions from the older app to newer app then token can be available but doctor details might not be
                // in that case check if doctor details are available and then do not
                com.sftelehealth.doctor.data.database.entity.Doctor tempDoctor = DatabaseObject.getInstance(context)
                        .doctorDao()
                        .getDoctor();

                response.setPhoneNumber(tempDoctor.getPhone());
                if (tempDoctor.getDoctorCategories() != null && tempDoctor.getDoctorCategories().size() != 0)
                    response.setHasMigratedToDoctorCategories(true);

                if (tempDoctor == null) {
                    // Then this is the first login after transitioning to the new app
                    // when this field is set to false then the doctor will be loged in with the acquired login ID and phone number.
                    response.setDoctorObjectAvailable(false);

                } else {
                    response.setDoctorObjectAvailable(true);
                    Doctor doctor = mapper.transformToData(DatabaseObject.getInstance(context)
                            .doctorDao()
                            .getDoctor());

                    if (!TextUtils.isEmpty(doctor.getImage()) && !TextUtils.isEmpty(doctor.getSignatureImage())) {
                        response.setDoctorDataSet(true);
                    } else {
                        response.setDoctorDataSet(false);
                    }
                }
            }

            return response;
        });
    }

    @Override
    public Observable<ShouldUpdateResponse> shouldUpdate() {

        return coreApi.shouldUpdate(AuthCodeProvider.getAuthCode(context)).map(systemInfoResponse -> {
            // DB Mapper to map the response object to DB object
            DBSystemInfoEntityMapper dbMapper = new DBSystemInfoEntityMapper();

            // Save the entry in the doctor DB
            DatabaseObject.getInstance(context)
                    .systemDao()
                    .insertSystemInfo(dbMapper.transform(systemInfoResponse));

            // save country code in the shared preference for easy access by other modules
            AuthCodeProvider.getSharedPreference(context).edit().putString(Constant.COUNTRY_CODE, systemInfoResponse.getCountryCode()).apply();

            return systemInfoResponse;
        });
    }

    @Override
    public Observable<UpdateDoctorResponse> updateDoctor(HashMap<String, String> params) {
        UpdateDoctorRequest request = new UpdateDoctorRequest();
        request.setAndroidId(params.get("android_id"));
        request.setDeviceDescription(params.get("device_description"));
        request.setPhoneOsVersion(params.get("phone_os_version"));
        request.setOneSignalId(params.get("onesignal_id"));
        request.setAppVersion(params.get("app_version"));
        SharedPreferences sp = AuthCodeProvider.getSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        return coreApi.updateDoctor(AuthCodeProvider.getAuthCode(context), request)
                .doOnNext(new Consumer<UpdateDoctorResponse>() {
                    @Override
                    public void accept(UpdateDoctorResponse updateDoctorResponse) throws Exception {
                        editor.putInt("callback_expiry_time", updateDoctorResponse.getTimers().getCallbackExpireSec());
                        editor.putInt("callback_rejection_time", updateDoctorResponse.getTimers().getCallbackRejectSec());
                        editor.putInt("video_callback_rejection_time", updateDoctorResponse.getTimers().getVideoCallbackRejectSec());
                        editor.apply();
                    }
                });
    }

    @Override
    public Observable<SystemInfo> getSystemInfoFromPersistence() {

        return Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(dbMapperInstance -> {
                    return DatabaseObject.getInstance(context)
                            .systemDao()
                            .getSystemInfo();
                });
    }
}
