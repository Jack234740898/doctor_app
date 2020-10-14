package com.sftelehealth.doctor.data.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sftelehealth.doctor.data.model.CallbackRequest;
import com.sftelehealth.doctor.data.model.Case;
import com.sftelehealth.doctor.data.model.MedicineType;
import com.sftelehealth.doctor.data.model.Prescription;
import com.sftelehealth.doctor.data.model.TimeSlot;
import com.sftelehealth.doctor.data.model.request.CallbackUpdateRequest;
import com.sftelehealth.doctor.data.model.request.CreatePrescriptionRequest;
import com.sftelehealth.doctor.data.model.request.EmergencyCallRequest;
import com.sftelehealth.doctor.data.model.request.LoginRequest;
import com.sftelehealth.doctor.data.model.request.PrescriptionRequest;
import com.sftelehealth.doctor.data.model.request.SendOtpRequest;
import com.sftelehealth.doctor.data.model.request.UpdateDoctorRequest;
import com.sftelehealth.doctor.data.model.response.CallbackRequestResponse;
import com.sftelehealth.doctor.data.model.response.CancelCallbackResponse;
import com.sftelehealth.doctor.data.model.response.CasePrescriptionsAndDocumentsResponse;
import com.sftelehealth.doctor.data.model.response.CreatePrescriptionResponse;
import com.sftelehealth.doctor.data.model.response.DoctorCasesResponse;
import com.sftelehealth.doctor.data.model.response.DoctorImageUpdateResponse;
import com.sftelehealth.doctor.data.model.response.GetAllTimeSlotsResponse;
import com.sftelehealth.doctor.data.model.response.LoginResponse;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;
import com.sftelehealth.doctor.data.model.response.ShouldUpdateResponse;
import com.sftelehealth.doctor.data.model.response.StartDoctorCallResponse;
import com.sftelehealth.doctor.data.model.response.UpdateDoctorResponse;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by Rahul on 26/09/17.
 */

public interface CoreApi {

    /**
     * Check whether doctor is already registered on the platform and simultaneously generate OTP
     * @param phone, phone - the phone number for which the OTP has to be generated
     * @return {@link SendOtpResponse}, isRegistered - whether the doctor is registered on our platform
     */
    @Headers({"Authorization:Bearer p2S80tdxdrVjGC9F6wbn","Content-Type:application/json"})
    @POST("doctor/sendOtp")
    Observable<SendOtpResponse> checkRegistration(@Body SendOtpRequest phone);

    /**
     * Verify OTP
     * @param login, set phone, otp, email and appVersion in this model
     * @return {@link LoginResponse}
     */
    @Headers({"Authorization:Bearer p2S80tdxdrVjGC9F6wbn","Content-Type:application/json"})
    @POST("doctor/login")
    Observable<LoginResponse> doctorVerify(@Body LoginRequest login);

    /**
     * Update doctor's image
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param id
     * @param modelProperty
     * @param path
     * @param model
     * @param image
     * @return {@link DoctorImageUpdateResponse}
     */
    @Multipart
    @POST("doctor/uploadImage")
    Observable<DoctorImageUpdateResponse> doctorImageUpdate (
            @Header("Authorization") String authCode,
            @Part("id") RequestBody id,
            @Part("modelProperty") RequestBody modelProperty,
            @Part("path") RequestBody path,
            @Part("model") RequestBody model,
            @Part MultipartBody.Part image
    );

    /**
     * Get all cases for the doctor
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param params, must contain 2 params with following keys, page - the page number to be fetched, size - size of the page to be fetched
     * @return {@link DoctorCasesResponse}
     */
    @GET("doctor/getCases")
    Observable<DoctorCasesResponse> getDoctorCases(
            @Header("Authorization") String authCode,
            @QueryMap Map<String,String> params
    );

    /**
     * Get a case by ID
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param params, must contain 1 parameter with key, caseId - the ID for the case
     * @return {@link Case}
     */
    @GET("doctor/getCases")
    Observable<DoctorCasesResponse> getDoctorCaseById(
            @Header("Authorization") String authCode,
            @QueryMap Map<String,String> params
    );

    /**
     * Get Appointments for the doctor
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param options, must contain 2 params with following keys, page - the page number to be fetched, size - size of the page to be fetched
     * @return {@link ArrayList<CallbackRequest>}
     */
    @GET("doctor/getCallbackRequests")
    Observable<CallbackRequestResponse> getAppointments(
            @Header("Authorization") String authCode,
            @QueryMap Map<String, String> options
    );

    /**
     * Get all timeslots
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @return {@link GetAllTimeSlotsResponse}
     */
    @GET("categorytimeslot/getAllTimeslotsDoctor")
    Observable<GetAllTimeSlotsResponse> getAllTimeSlots (@Header("Authorization") String authCode);

    /**
     * Get timeslots for which the given doctor is available
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param docId, params.put("doctorId",docId)
     * @return
     */
    @GET("doctortimeslot")
    Observable<ArrayList<TimeSlot>>  getDoctorSlots(
            @Header("Authorization") String authCode,
            @QueryMap Map<String,String> docId
    );

    /**
     * Get list of prescriptions for a case
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param options, must contain 2 params with following keys, page - the page number to be fetched, size - size of the page to be fetched
     * @return {@link CasePrescriptionsAndDocumentsResponse}
     */
    @GET("doctor/getConsultsForCase")
    Observable<CasePrescriptionsAndDocumentsResponse> getPrescriptionsList (
            @Header("Authorization") String authCode,
            @QueryMap Map<String, Integer> options
    );

    /**
     * Create a new prescription
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param prescription, data for the prescription to be created
     * @return {@link CreatePrescriptionResponse}
     */
    @POST("prescription")
    Observable<Prescription> createPrescription (
            @Header("Authorization") String authCode,
            @Body PrescriptionRequest prescription
    );

    /**
     * Update an already existing prescription
     * @param prescriptionId, ID of the prescription to be edited
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param prescription, data for the prescription to be created
     * @return {@link CreatePrescriptionResponse}
     */
    @PUT("prescription/{id}")
    Observable<CreatePrescriptionResponse> editPrescription(
            @Path("id") String prescriptionId,
            @Header("Authorization") String authCode,
            @Body CreatePrescriptionRequest prescription
    );

    /**
     * Accept a Callback Request
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param callbackUpdateRequest, request body contains - ID of the callback request that has to be cancelled
     * @return {@link Void}
     */
    @POST("doctor/acceptCallback")
    Observable<List<CallbackRequest>> acceptAppointment(
            @Header("Authorization") String authCode,
            @Body CallbackUpdateRequest callbackUpdateRequest
    );

    /**
     * Cancel a Callback Request
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param callbackUpdateRequest, request body contains - ID of the callback request that has to be cancelled
     * @return {@link Void}
     */
    @POST("doctor/cancelCallback")
    Observable<CancelCallbackResponse> cancelAppointment(
            @Header("Authorization") String authCode,
            @Body CallbackUpdateRequest callbackUpdateRequest
    );

    /**
     * Start a call for an accepted Callback Request
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param callbackUpdateRequest, request body contains - ID of the callback request that has to be cancelled
     * @return {@link StartDoctorCallResponse}}
     */
    @POST("doctor/placeDoctorCall")
    Observable<StartDoctorCallResponse> startDoctorCall(
            @Header("Authorization") String authCode,
            @Body CallbackUpdateRequest callbackUpdateRequest
    );

    /**
     * Start a call to the patient for an already consulted Case in case doctor forgets to discuss something
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param emergencyCallRequest, request body contains - case ID of the callback request that has to be cancelled
     * @return {@link StartDoctorCallResponse}
     */
    @POST("doctor/doctorEmergencyCall")
    Observable<StartDoctorCallResponse> startDoctorEmergencyCall(
            @Header("Authorization") String authCode,
            @Body EmergencyCallRequest emergencyCallRequest
    );

    /**
     * Update OneSignal ID for a doctor's device
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @param updateDoctorRequest, request body contains - oneSignal ID of the callback request that has to be cancelled
     * @return {@link UpdateDoctorResponse}
     */
    @POST("doctor/updateDoctor")
    Observable<UpdateDoctorResponse> updateDoctor (
            @Header("Authorization") String authCode,
            @Body UpdateDoctorRequest updateDoctorRequest
    );

    /**
     * Get info about whether the app needs to be updated
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @return {@link ShouldUpdateResponse}
     */
    @GET("dmisc/findOneDoctor")
    Observable<ShouldUpdateResponse> shouldUpdate (@Header("Authorization") String authCode);

    /**
     * Get medicine types for filling a prescription
     * @param authCode, the generated authentication code for the doctor to access APIs
     * @return {@link ArrayList< MedicineType >}
     */
    @GET("prescription/getMedicineTypes")
    Observable<List<MedicineType>> getMedicineTypes (@Header("Authorization") String authCode);

    @POST("doctor/callbackCompleted")
    Observable<CancelCallbackResponse> completedAppointment (
            @Header("Authorization") String authCode,
            @Body CallbackUpdateRequest callbackUpdateRequest
    );
}