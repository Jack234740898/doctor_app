package com.sftelehealth.doctor.app.view.viewmodel;

import android.text.TextUtils;
import android.util.Log;

import com.sftelehealth.doctor.BuildConfig;
import com.sftelehealth.doctor.data.repository.SystemDataRepository;
import com.sftelehealth.doctor.domain.interactor.ConfirmOtp;
import com.sftelehealth.doctor.domain.interactor.GenerateOTP;
import com.sftelehealth.doctor.domain.model.Doctor;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Rahul on 14/12/17.
 */

public class ConfirmationFragmentViewModel extends ViewModel {

    SystemDataRepository systemDataRepository;

    public final ObservableField<String> phoneNumber = new ObservableField<>();
    public final ObservableField<String> otp = new ObservableField<>();
    public MutableLiveData<Doctor> doctor = new MutableLiveData<>();

    public MutableLiveData<Boolean> navigateToMainView = new MutableLiveData<>();
    public MutableLiveData<Long> countDowntime = new MutableLiveData<>();
    public MutableLiveData<Boolean> isRegistered = new MutableLiveData<>();
    public MutableLiveData<String> generatedOTP = new MutableLiveData<>();

    ConfirmOtp confirmOtp;
    GenerateOTP generateOTP;

    Long otpCountDownBackOff = 30L;

    public Disposable timerSubscription;

    @Inject
    public ConfirmationFragmentViewModel (GenerateOTP generateOTP, ConfirmOtp confirmOtp) {
        this.confirmOtp = confirmOtp;
        this.generateOTP = generateOTP;
    }

    public void loginUser() {

        HashMap<String, String> params = new HashMap<>();
        params.put("phone", phoneNumber.get());
        params.put("otp", otp.get());
        params.put("version_name", BuildConfig.VERSION_NAME);

        confirmOtp.execute(new DisposableObserver<Doctor>() {
            @Override
            public void onNext(Doctor doctor) {
                if(TextUtils.isEmpty(doctor.getImage()) || TextUtils.isEmpty(doctor.getSignatureImage())) {
                    // must go to Profile Setup screen
                    navigateToMainView.postValue(false);
                } else {
                    // must be redirected to Main View
                    navigateToMainView.postValue(true);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("Confirm OTP", e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        }, params);
    }

    public void generateOTP() {

        generateOTP.execute(new DisposableObserver<SendOtpResponse>() {
            @Override
            public void onNext(SendOtpResponse sendOtpResponse) {
                generatedOTP.postValue(sendOtpResponse.getOtp());
                isRegistered.postValue(sendOtpResponse.isRegistered());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        }, phoneNumber.get().toString());

        // start the count down timer
        beginCountDown();
    }

    public void beginCountDown() {
        timerSubscription = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        countDowntime.postValue(otpCountDownBackOff - aLong);
                        if(aLong == otpCountDownBackOff)
                            timerSubscription.dispose();
                    }
                });
    }


}
