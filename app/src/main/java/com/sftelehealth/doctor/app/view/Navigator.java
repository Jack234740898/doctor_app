package com.sftelehealth.doctor.app.view;

import android.content.Context;
import android.content.Intent;

import com.sftelehealth.doctor.app.utils.Constant;
import com.sftelehealth.doctor.app.view.activity.CallbackDetailsActivity;
import com.sftelehealth.doctor.app.view.activity.CaseDetailsActivity;
import com.sftelehealth.doctor.app.view.activity.ConfirmationActivity;
import com.sftelehealth.doctor.app.view.activity.MainActivity;
import com.sftelehealth.doctor.app.view.activity.PrescriptionActivity;
import com.sftelehealth.doctor.app.view.activity.ProfileDetailsActivity;
import com.sftelehealth.doctor.app.view.activity.SignatureActivity;

/**
 * Created by Rahul on 12/12/17.
 */

public class Navigator {

    public static void navigateToOTPConfirmation(Context context, String phone, String otp) {
        Intent intent = new Intent();
        intent.setClass(context, ConfirmationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constant.PHONE, phone);
        intent.putExtra(Constant.OTP, otp);
        context.startActivity(intent);
    }

    public static void navigateToMainView(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void navigateToProfileSetup(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, ProfileDetailsActivity.class);
        context.startActivity(intent);
    }

    public static void navigateToCaseDetails(Context context, int caseId) {
        Intent intent = new Intent();
        intent.setClass(context, CaseDetailsActivity.class);
        intent.putExtra(Constant.CASE_ID, caseId);
        context.startActivity(intent);
    }

    public static void navigateToCallbackDetails(Context context, int callbackId) {
        Intent intent = new Intent();
        intent.setClass(context, CallbackDetailsActivity.class);
        intent.putExtra(Constant.CALLBACK_ID, callbackId);
        context.startActivity(intent);
    }

    public static void navigateToCreatePrescription(Context context, int caseId, int consultId, int doctorCategoryId) {
        Intent intent = new Intent();
        intent.setClass(context, PrescriptionActivity.class);
        intent.putExtra(Constant.CASE_ID, caseId);
        intent.putExtra(Constant.CONSULT_ID, consultId);
        intent.putExtra(Constant.DOCTOR_CATEGORY_ID, doctorCategoryId);
        context.startActivity(intent);
    }

    public static void navigateToViewPrescription(Context context, String prescription, int doctorCategoryId) {
        Intent intent = new Intent();
        intent.setClass(context, PrescriptionActivity.class);
        intent.putExtra(Constant.PRESCRIPTION, prescription);
        intent.putExtra(Constant.DOCTOR_CATEGORY_ID, doctorCategoryId);
        context.startActivity(intent);
    }

    public static void navigateToSignatureScreen(Context context, int doctorId) {
        Intent intent = new Intent();
        intent.setClass(context, SignatureActivity.class);
        intent.putExtra(Constant.DOCTOR_ID, doctorId);
        context.startActivity(intent);
    }
}
