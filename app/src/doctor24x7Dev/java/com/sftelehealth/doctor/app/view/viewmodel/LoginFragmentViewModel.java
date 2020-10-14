package com.sftelehealth.doctor.app.view.viewmodel;

import android.util.Log;

import com.sftelehealth.doctor.BuildConfig;
import com.sftelehealth.doctor.domain.interactor.ConfirmOtp;
import com.sftelehealth.doctor.domain.interactor.GenerateOTP;
import com.sftelehealth.doctor.domain.interactor.IsUserAuthenticated;
import com.sftelehealth.doctor.domain.model.Doctor;
import com.sftelehealth.doctor.domain.model.response.IsUserAuthenticatedResponse;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;

import java.util.HashMap;

import javax.inject.Inject;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Rahul on 22/06/17.
 */

public class LoginFragmentViewModel extends ViewModel {

    GenerateOTP generateOTP;
    IsUserAuthenticated isUserAuthenticated;
    ConfirmOtp confirmOtp;
    //SystemDataRepository systemDataRepository;

    public final ObservableField<String> phoneNumber = new ObservableField<>();
    public MutableLiveData<SendOtpResponse> isRegistered = new MutableLiveData<>();
    public MutableLiveData<IsUserAuthenticatedResponse> isAuthenticated = new MutableLiveData<>();
    public MutableLiveData<Boolean> doctorDataFetched = new MutableLiveData<>();
    public Doctor doctor;

    @Inject
    public LoginFragmentViewModel (GenerateOTP generateOTP, IsUserAuthenticated isUserAuthenticated, ConfirmOtp confirmOtp) {
        this.generateOTP = generateOTP;
        //this.systemDataRepository = systemDataRepository;
        this.isUserAuthenticated = isUserAuthenticated;
        this.confirmOtp = confirmOtp;
    }


    public void generateOTP(String countryCode) {

        generateOTP.execute(new DisposableObserver<SendOtpResponse>() {
            @Override
            public void onNext(SendOtpResponse sendOtpResponse) {
                isRegistered.postValue(sendOtpResponse);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("LoginFragment", "error: generateOTP");
            }

            @Override
            public void onComplete() {}

        },phoneNumber.get());
    }

    public void isUserAuthenticated() {

        HashMap<String, String> params = new HashMap<>();

        isUserAuthenticated.execute(new DisposableObserver<IsUserAuthenticatedResponse>() {
            @Override
            public void onNext(IsUserAuthenticatedResponse isUserAuthenticatedResponse) {

                isAuthenticated.postValue(isUserAuthenticatedResponse);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("LoginFragment", "error: isUserAuthenticated");
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        }, params);
    }

    public void getDoctorObject() {

        HashMap<String, String> params = new HashMap<>();
        params.put("phone", phoneNumber.get());
        params.put("otp", "654646");
        params.put("version_name", BuildConfig.VERSION_NAME);

        confirmOtp.execute(new DisposableObserver<Doctor>() {
            @Override
            public void onNext(Doctor doctor) {
                LoginFragmentViewModel.this.doctor = doctor;
                doctorDataFetched.postValue(true);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("LoginFragment", "error: getDoctorObject");
            }

            @Override
            public void onComplete() {

            }
        }, params);
    }
}
