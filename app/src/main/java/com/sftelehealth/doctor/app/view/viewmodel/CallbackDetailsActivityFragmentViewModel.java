package com.sftelehealth.doctor.app.view.viewmodel;

import com.sftelehealth.doctor.domain.helper.DateTimeComputationHelper;
import com.sftelehealth.doctor.domain.helper.DateTimeHelper;
import com.sftelehealth.doctor.domain.interactor.AcceptRejectCallback;
import com.sftelehealth.doctor.domain.interactor.GetCallbackDetails;
import com.sftelehealth.doctor.domain.interactor.GetSystemInfo;
import com.sftelehealth.doctor.domain.interactor.GetThisDoctor;
import com.sftelehealth.doctor.domain.interactor.InitiateCall;
import com.sftelehealth.doctor.domain.interactor.InitiateEmergencyCall;
import com.sftelehealth.doctor.domain.model.CallbackRequest;
import com.sftelehealth.doctor.domain.model.Doctor;
import com.sftelehealth.doctor.domain.model.SystemInfo;

import java.util.Date;
import java.util.HashMap;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Rahul on 29/12/17.
 */

public class CallbackDetailsActivityFragmentViewModel extends ViewModel {

    public final ObservableField<CallbackRequest> callbackRequest = new ObservableField<>();
    public final ObservableField<Integer> callbackId = new ObservableField<>();

    public MutableLiveData<Boolean> notifyDataChange = new MutableLiveData<>();
    public MutableLiveData<Boolean> notifyCallbackRejected = new MutableLiveData<>();
    public MutableLiveData<Boolean> notifyCallStarted = new MutableLiveData<>();

    public boolean startCall = false;

    GetCallbackDetails getCallbackDetails;
    AcceptRejectCallback acceptRejectCallback;
    InitiateCall initiateCallback;
    InitiateEmergencyCall initiateEmergencyCall;
    GetThisDoctor getThisDoctor;
    GetSystemInfo getSystemInfo;

    public Doctor doctor;

    public int videoCallExpirationTime;

    DateTimeComputationHelper dtch;

    // accept reject call
    public final ObservableField<Boolean> isAcceptCallbackLayoutVisible = new ObservableField<>(false);
    public ObservableField<Boolean> isPriorCaseDetailsLayoutVisible = new ObservableField<>(false);
    public final ObservableField<Boolean> isEmergencyCallContainerVisible = new ObservableField<>(false);
    // call button layout
    public final ObservableField<Boolean> isCallButtonLayoutVisible = new ObservableField<>(true);
    public final MutableLiveData<Boolean> isCallButtonActive = new MutableLiveData<>();

    public final ObservableField<Boolean> isPrescriptionButtonVisible = new ObservableField<>(false);
    public final ObservableField<Boolean> isVideoCallbackTimeInfoVisible = new ObservableField<>(false);

    public boolean hasCallStarted = false;

    public String message;
    public String title;
    public String videoCallbackDate = "";
    public String videoCallbackTimeSlot = "";
    public String countryCode = "";

    public String callbackButtonText = "dialer will be activated 5mins before the appointment time.";

    public CallbackDetailsActivityFragmentViewModel(GetCallbackDetails getCallbackDetails,
                                                    AcceptRejectCallback acceptRejectCallback,
                                                    InitiateCall initiateCallback,
                                                    InitiateEmergencyCall initiateEmergencyCall,
                                                    GetThisDoctor getThisDoctor,
                                                    GetSystemInfo getSystemInfo) {

        this.getCallbackDetails = getCallbackDetails;
        this.acceptRejectCallback = acceptRejectCallback;
        this.initiateCallback = initiateCallback;
        this.initiateEmergencyCall = initiateEmergencyCall;
        this.getThisDoctor = getThisDoctor;
        this.getSystemInfo = getSystemInfo;
        dtch = new DateTimeComputationHelper();

    }

    public void getCallbackRequest() {

        HashMap<String, String> params = new HashMap<>();
        params.put("callbackId", String.valueOf(callbackId.get()));

        getCallbackDetails.execute(new DisposableObserver<CallbackRequest>() {
            @Override
            public void onNext(CallbackRequest callbackRequest) {
                CallbackDetailsActivityFragmentViewModel.this.callbackRequest.set(callbackRequest);

                if (callbackRequest.getStatus().equalsIgnoreCase("Consulted") || callbackRequest.getStatus().equalsIgnoreCase("Patient Disconnected") || callbackRequest.getStatus().equalsIgnoreCase("Doctor Disconnected")) {
                    hasCallStarted = false;
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {

                setUpMessagesAndFlags();
                // notify change of data
                notifyDataChange.postValue(true);
            }
        }, params);
    }

    public void acceptOrRejectCallback(boolean callbackAccepted) {

        HashMap<String, String> params = new HashMap<>();
        params.put("callback_id", String.valueOf(callbackId.get()));
        params.put("callback_accepted", String.valueOf(callbackAccepted));

        // accept or reject a callback request
        acceptRejectCallback.execute(new DisposableObserver<CallbackRequest>() {
            @Override
            public void onNext(CallbackRequest callbackRequest) {
                //Timber.d("acceptRejectCallback onComplete()");
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                //Timber.d("acceptRejectCallback onComplete()");
                // check the result and show appropriate message in the UI section

                if (!callbackAccepted)
                    notifyCallbackRejected.postValue(true);
                else
                    getCallbackRequest();

                // notify change of data
                // notifyDataChange.postValue(true);
            }
        }, params);
    }

    public void initiateCall() {

        HashMap<String, String> params = new HashMap<>();
        params.put("callback_id", String.valueOf(callbackId.get()));

        // start knowlarity call
        initiateCallback.execute(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean callStarted) {
                //Timber.d("initiateCall onNext()");
            }

            @Override
            public void onError(Throwable e) {
                //Timber.d("initiateCall onError()");
            }

            @Override
            public void onComplete() {
                //Timber.d("initiateCall onComplete()");
                hasCallStarted = true;
                //getCallbackRequest();
                // notify change of data
                notifyCallStarted.postValue(true);
            }
        }, params);
    }

    public void emergencyCall() {
        HashMap<String, String> params = new HashMap<>();
        params.put("case_id", String.valueOf(callbackRequest.get().getCaseId()));

        initiateEmergencyCall.execute(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                notifyCallStarted.postValue(true);
            }
        }, params);
    }

    public void getDoctor() {

        HashMap<String, String> params = new HashMap<>();

        getThisDoctor.execute(new DisposableObserver<Doctor>() {
            @Override
            public void onNext(Doctor doctor) {
                CallbackDetailsActivityFragmentViewModel.this.doctor = doctor;
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        }, null);
    }

    public void getSystemInfo() {

        HashMap<String, String> params = new HashMap<>();

        getSystemInfo.execute(new DisposableObserver<SystemInfo>() {
            @Override
            public void onNext(SystemInfo systemInfo) {
                if (!systemInfo.getCountryCode().equalsIgnoreCase("IN")) {
                    countryCode = systemInfo.getCountryCode() + "_";
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        }, params);
    }

    public boolean hasPrescription() {

        if (callbackRequest.get() == null || callbackRequest.get().getCaseId() == null || callbackRequest.get().getCaseId() == 0) {
            return false;
        } else if (callbackRequest.get().getStatus().equalsIgnoreCase("Pending") && callbackRequest.get().getAutoExpireAt() != null) {
            return false;
        } else {
            return true;
        }
    }

    private void setUpMessagesAndFlags() {

        String callbackStatus = callbackRequest.get().getStatus();
        isPriorCaseDetailsLayoutVisible.set(false);

        if (callbackStatus.equalsIgnoreCase("Pending")) {

            // show the icons for accepting and rejecting the appointment
            isAcceptCallbackLayoutVisible.set(true);

            isCallButtonLayoutVisible.set(false);

            title = " hrs left to accept";

            // String timeToAccept = DateTimeHelper.toLocaleTime(dtch.addTime(dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getCreatedAt())), 60 * 30));
            String timeToAccept = DateTimeHelper.toLocaleTime(dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getAutoRejectAt())));

            if (callbackRequest.get().getAutoExpireAt() != null && callbackRequest.get().getFollowUp() == true) {
                if (callbackRequest.get().isVideo()) {
                    Date videoStartDate = dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getVideoStartTime()));
                    Date videoEndDate = dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getVideoEndTime()));
                    message = "It's a follow up request. Please accept the request by " + timeToAccept + " today";  // "You have a Video Callback Request. It's a follow up call for " + DateTimeHelper.toLocaleDate(videoStartDate) + " from: " + DateTimeHelper.toLocaleTime(videoStartDate) + " - " + DateTimeHelper.toLocaleTime(videoEndDate) +

                    videoCallbackDate = DateTimeHelper.toLocaleDate(videoStartDate);
                    videoCallbackTimeSlot = DateTimeHelper.toLocaleTime(videoStartDate) + " - " + DateTimeHelper.toLocaleTime(videoEndDate);

                    isVideoCallbackTimeInfoVisible.set(true);

                } else {
                    message = "You have received a callback request. It's a follow up call. Please accept the request by " + timeToAccept + " if you want to take this consultation.";
                }

                isPrescriptionButtonVisible.set(hasPrescription());
            } else {
                if (callbackRequest.get().isVideo()) {
                    Date videoStartDate = dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getVideoStartTime()));
                    Date videoEndDate = dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getVideoEndTime()));
                    message = "Please accept the request by " + timeToAccept + " today"; // "You have a Video Callback Request for " + DateTimeHelper.toLocaleDate(videoStartDate) + " from: " + DateTimeHelper.toLocaleTime(videoStartDate) + " - " + DateTimeHelper.toLocaleTime(videoEndDate) +

                    videoCallbackDate = DateTimeHelper.toLocaleDate(videoStartDate);
                    videoCallbackTimeSlot = DateTimeHelper.toLocaleTime(videoStartDate) + " - " + DateTimeHelper.toLocaleTime(videoEndDate);

                    isVideoCallbackTimeInfoVisible.set(true);

                } else
                    message = "You have received a Callback Request. Please accept the request by " + timeToAccept + " if you want to take this consultation.";
            }

        } else if (callbackStatus.equalsIgnoreCase("Accepted") ||
                callbackStatus.equalsIgnoreCase("Patient Disconnected") ||
                callbackStatus.equalsIgnoreCase("Doctor Disconnected") ||
                callbackStatus.equalsIgnoreCase("InProgress")) {

            isAcceptCallbackLayoutVisible.set(false);

            isPrescriptionButtonVisible.set(hasPrescription());

            if (callbackRequest.get().isVideo()) {
                Date videoStartDate = dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getVideoStartTime()));
                Date videoEndDate = dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getVideoEndTime()));

                videoCallbackDate = DateTimeHelper.toLocaleDate(videoStartDate);
                videoCallbackTimeSlot = DateTimeHelper.toLocaleTime(videoStartDate) + " - " + DateTimeHelper.toLocaleTime(videoEndDate);

                isVideoCallbackTimeInfoVisible.set(true);
            } else {
                isVideoCallbackTimeInfoVisible.set(false);
            }

            // String timeToAccept = DateTimeHelper.toLocaleTime(dtch.addTime(dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getCreatedAt())), 60 * 60 * 4));
            String timeToAccept = DateTimeHelper.toLocaleTime(dtch.convertGMTToIST(DateTimeHelper.parseDateTime(callbackRequest.get().getAutoExpireAt())));

            // check the different status and set the message accordingly
            if (callbackStatus.equalsIgnoreCase("Patient Disconnected")) {
                message = "The last time you tried to connect this call, the patient did not answer. This could be due to network issues or unavailability of the patient. You must complete this request by " + timeToAccept;
            } else if (callbackStatus.equalsIgnoreCase("Doctor Disconnected")) {
                message = "The last time you tried to connect this call, your phone was not reachable. You must complete this request by " + timeToAccept;
            } else {
                if (callbackRequest.get().isVideo())
                    message = "Start the video call with the patient by pressing the dialer button.";   // for the scheduled time: " + DateTimeHelper.toLocaleTimeWithDay(dtch.convertGMTToIST(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(callbackRequest.get().getVideoStartTime()))) + "
                else
                    message = "You must respond to this request by " + timeToAccept + ". Please press the dialler button to initiate the consultation call.";
            }

            isCallButtonLayoutVisible.set(true);

            if(callbackStatus.equalsIgnoreCase("InProgress"))
                isCallButtonActive.postValue(false);
            else {
                // check if it was a video call
                if (callbackRequest.get().isVideo())
                    if (callbackRequest.get().isShowDialer())   //  || true add this to make dialer visible all the time
                        isCallButtonActive.postValue(true);
                    else
                        isCallButtonActive.postValue(false);
                else
                    isCallButtonActive.postValue(true);
            }

            if (!callbackRequest.get().isVideo()) {
                // If more than 4hrs has passed then the consult is expired
                if (240 > 4 * 60) {
                    message = "This request has expired on ";
                    isCallButtonLayoutVisible.set(false);
                } else
                    isCallButtonLayoutVisible.set(true);
            } else {

            }

            if(callbackStatus.equalsIgnoreCase("InProgress") && isCallButtonActive != null)
                isCallButtonActive.postValue(false);

            // show hide, button to view documents shared before consult
            if (callbackRequest.get().getDocs().size() > 0 && callbackRequest.get().getCaseId() == 0) {
                // show button to view documents
                isPriorCaseDetailsLayoutVisible.set(true);
            } else {
                // hide button to view documents
                isPriorCaseDetailsLayoutVisible.set(false);
            }

        } else if (callbackStatus.equalsIgnoreCase("Rejected") || callbackStatus.equalsIgnoreCase("Auto Rejected")) {

            isAcceptCallbackLayoutVisible.set(false);
            isCallButtonLayoutVisible.set(false);
            isPrescriptionButtonVisible.set(false);
            isVideoCallbackTimeInfoVisible.set(false);

            if (callbackStatus.equalsIgnoreCase("Rejected"))
                message = "This request was cancelled by you. The patient has been notified accordingly.";
            else
                message = "This request was auto rejected by SAJIDA TeleHealth Doctor, as it was not accepted within" + dtch.getTimeFormatted(videoCallExpirationTime * 1000);

        } else if (callbackStatus.equalsIgnoreCase("Consulted")) {

            // show emergency button and make it clickable
            isPrescriptionButtonVisible.set(hasPrescription());
            isVideoCallbackTimeInfoVisible.set(false);

            isCallButtonActive.postValue(false);

            if (callbackRequest.get().getConsultedAt() != null && !callbackRequest.get().getConsultedAt().equalsIgnoreCase("None")) {
                //isCallButtonLayoutVisible = false;
            } else {
                //isCallButtonLayoutVisible = true;
            }

            // isEmergencyCallContainerVisible.set(true);

            message = "The call request was completed. Please click below to view/add medical advice.";

        } else if (callbackStatus.equalsIgnoreCase("Expired")) {

            isAcceptCallbackLayoutVisible.set(false);
            isCallButtonLayoutVisible.set(false);
            isVideoCallbackTimeInfoVisible.set(false);

            message = "The call request expired as it was not completed within 4hrs.";

        } else if (callbackStatus.equalsIgnoreCase("Patient Cancelled")) {

            isAcceptCallbackLayoutVisible.set(false);
            isCallButtonLayoutVisible.set(false);
            isVideoCallbackTimeInfoVisible.set(false);

            message = "This request was cancelled by the patient.";

        } else if (callbackStatus.equalsIgnoreCase("Follow Up")) {

            message = "You have received a Follow up request. Please go through the last medical advice you wrote for this patient before initiating the call by tapping the View/Add Medical Advice button.";
            isPrescriptionButtonVisible.set(true);
            isCallButtonActive.postValue(true);
            isVideoCallbackTimeInfoVisible.set(false);

        }


    }
}
