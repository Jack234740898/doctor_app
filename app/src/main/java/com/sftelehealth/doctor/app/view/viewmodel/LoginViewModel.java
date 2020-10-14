package com.sftelehealth.doctor.app.view.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.databinding.ObservableField;
import android.util.Log;
import android.view.View;

import javax.inject.Inject;

import com.sftelehealth.doctor.domain.interactor.GenerateOTP;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by Rahul on 10/12/17.
 */

public class LoginViewModel extends ViewModel {

    GenerateOTP generateOTPUseCase;

    public final ObservableField<String> phoneNumber = new ObservableField<>();

    @Inject
    public LoginViewModel (GenerateOTP generateOTP) {
        generateOTPUseCase = generateOTP;
    }

    public void generateOTP(View verifyButtonView) {

        generateOTPUseCase.execute(new DisposableObserver<SendOtpResponse>() {
            @Override
            public void onNext(SendOtpResponse aBoolean) {
                Log.d("TAG", "otp generated: " + aBoolean.isRegistered());
                // send message from the activity to change the fragment to verify the OTP
                // navigator.verifyOtp();
            }

            @Override
            public void onError(Throwable e) {
                Log.d("TAG", "some error occurred: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d("TAG", "completed");
            }
        }, phoneNumber.get().toString());
    }
}
