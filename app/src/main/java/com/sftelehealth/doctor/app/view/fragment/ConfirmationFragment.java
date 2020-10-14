package com.sftelehealth.doctor.app.view.fragment;


import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.helpers.AppSignatureHelper;
import com.sftelehealth.doctor.app.receiver.SMSReceiver;
import com.sftelehealth.doctor.app.utils.Constant;
import com.sftelehealth.doctor.app.view.Navigator;
import com.sftelehealth.doctor.app.view.activity.ConfirmationActivity;
import com.sftelehealth.doctor.app.view.viewmodel.ConfirmationFragmentViewModel;
import com.sftelehealth.doctor.databinding.FragmentConfirmationBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmationFragment extends Fragment implements SMSReceiver.OTPReceiver {

    private ConfirmationFragmentViewModel viewModel;
    private FragmentConfirmationBinding binding;

    SMSReceiver smsReceiver;
    SmsRetrieverClient client;
    Task<Void> task;

    private final int RESOLVE_HINT = 123;

    public ConfirmationFragment() {}

    public static ConfirmationFragment newInstance() {
        return new ConfirmationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_confirmation, container, false);

        viewModel = ((ConfirmationActivity)getActivity()).obtainViewModel(getActivity());
        binding.setViewmodel(viewModel);

        setUpObservables();
        setUpData();

        viewModel.beginCountDown();

        setUpSMSReceiver();

        AppSignatureHelper signatureHelper = new AppSignatureHelper(getContext());
        for(String signature: signatureHelper.getAppSignatures())
            Log.i("Signature Hash", signature);

        return binding.getRoot();
    }

    private void setUpData() {
        if(getActivity().getIntent().hasExtra(Constant.PHONE))
            binding.getViewmodel().phoneNumber.set(getActivity().getIntent().getStringExtra(Constant.PHONE));

        if(getActivity().getIntent().hasExtra(Constant.OTP))
            viewModel.generatedOTP.postValue(getActivity().getIntent().getStringExtra(Constant.OTP));
    }

    private void setUpObservables() {
        viewModel.navigateToMainView.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean navigateToMainView) {
                if(navigateToMainView)
                    Navigator.navigateToMainView(getContext());
                else
                    Navigator.navigateToProfileSetup(getContext());
            }
        });

        viewModel.countDowntime.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long aLong) {
                if(aLong > 0) {
                    binding.waitingForOTP.setText(getResources().getString(R.string.waiting_for_otp));
                    binding.timerText.setVisibility(View.VISIBLE);
                    binding.timerText.setText(String.valueOf(aLong));
                    binding.waitingForOTP.setClickable(false);
                } else {
                    binding.waitingForOTP.setClickable(true);
                    binding.waitingForOTP.setText("Resend OTP");
                    binding.timerText.setVisibility(View.INVISIBLE);
                }
            }
        });

        viewModel.generatedOTP.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.verificationCode.setText(s);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        smsReceiver = new SMSReceiver(this);
        IntentFilter smsInterceptorIntentFilter = new IntentFilter("com.google.android.gms.auth.api.phone.SMS_RETRIEVED");
        getActivity().registerReceiver(smsReceiver, smsInterceptorIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.timerSubscription.dispose();
        getActivity().unregisterReceiver(smsReceiver);
    }

    @Override
    public void otpReceived(String otp) {
        binding.verificationCode.setText(otp);
        viewModel.loginUser();
    }

    private void setUpSMSReceiver() {

        client = SmsRetriever.getClient(getContext());
        task = client.startSmsRetriever();

        // Listen for success/failure of the start Task.
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("SMSRetriever","Successfully started retriever");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SMSRetriever", "Failed to start retriever");
            }
        });
    }

    // Construct a request for phone numbers and show the picker
    /*private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                getApiClient(), hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }*/

    // Obtain the phone number from the result
    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId();  <-- will need to process phone number string
            }
        }
    }

    private GoogleApiClient getApiClient() {
        GoogleApiClient apiClient = new GoogleApiClient
                .Builder(getContext()).addApi(Auth.CREDENTIALS_API).enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e("ConfirmationActivity", "Client connection failed: " + connectionResult.getErrorMessage());
                    }
                }
        ).build();

        return apiClient;
    }*/



    /*public void resendOtp(View v) {
        viewModel.generateOTP();
        binding.waitingForOTP.setText(getResources().getString(R.string.waiting_for_otp));
        binding.waitingForOTP.setClickable(true);
        binding.timerText.setVisibility(View.VISIBLE);
    }*/
}