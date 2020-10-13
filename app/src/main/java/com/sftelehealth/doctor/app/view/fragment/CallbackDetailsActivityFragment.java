package com.sftelehealth.doctor.app.view.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sftelehealth.doctor.video.view.call.VideoCallViewActivity;
import com.squareup.picasso.Picasso;
import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.listener.CallbackDetailsEventListener;
import com.sftelehealth.doctor.app.utils.Constant;
import com.sftelehealth.doctor.app.utils.PermissionsHelper;
import com.sftelehealth.doctor.app.view.Navigator;
import com.sftelehealth.doctor.app.view.activity.BaseAppCompatActivity;
import com.sftelehealth.doctor.app.view.activity.CallbackDetailsActivity;
import com.sftelehealth.doctor.app.view.helper.CircleTransform;
import com.sftelehealth.doctor.app.view.viewmodel.CallbackDetailsActivityFragmentViewModel;
import com.sftelehealth.doctor.databinding.FragmentCallbackDetailsBinding;
import com.sftelehealth.doctor.video.view.VideoConsultActivity;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.sftelehealth.doctor.app.utils.Constant.DOCUMENT_ID;

/**
 * A placeholder fragment containing a simple view.
 */
public class CallbackDetailsActivityFragment extends Fragment implements CallbackDetailsEventListener, PermissionsHelper.PermissionCallback {

    FragmentCallbackDetailsBinding binding;
    CallbackDetailsActivityFragmentViewModel viewModel;

    Disposable intervalSubscription;

    boolean activityCreated = false;

    SharedPreferences sp;

    AlertDialog selectCallTypeAlertDialog;

    public CallbackDetailsActivityFragment() {}

    public static CallbackDetailsActivityFragment newInstance() {
        return new CallbackDetailsActivityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_callback_details, container, false);

        viewModel = ((CallbackDetailsActivity)getActivity()).obtainViewModel(getActivity());

        sp = ((BaseAppCompatActivity)getActivity()).getSharedPreferences();

        viewModel.videoCallExpirationTime = sp.getInt("video_callback_rejection_time", 0);

        activityCreated = true;

        setUpObservers();
        setUpData();

        return binding.getRoot();
    }

    @Override
    public void onResume() {

        super.onResume();

        // if call status is accepted then try to
        if(viewModel.hasCallStarted) {
            // start a timer to check callback request
            intervalSubscription = Observable.interval(1, 3, TimeUnit.SECONDS, Schedulers.io()).takeWhile(val -> (viewModel.callbackRequest.get().isVideo()) ? val < 3 : val < 10).subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    viewModel.getCallbackRequest();
                }
            });
        }

        if(viewModel.startCall) {
            viewModel.startCall = false;
            //startVideoCall();
            initiateCall();
        }
    }

    @Override
    public void onPause() {

        if(intervalSubscription != null && !intervalSubscription.isDisposed())
            intervalSubscription.dispose();

        super.onPause();
    }

    private void setUpObservers() {
        // Once the Callback info is loaded get the data
        viewModel.notifyDataChange.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean dataChanged) {
                if (dataChanged) {
                    binding.loadingContainer.setVisibility(View.GONE);
                    // repopulate the view based on the data that has been loaded
                    Picasso.with(getContext())
                            .load(viewModel.callbackRequest.get().getPatientImage())
                            .placeholder(R.drawable.profile)
                            .transform(new CircleTransform())
                            .into(binding.profileDetailsContainer.patientImage);

                    binding.setCallbackRequest(viewModel.callbackRequest.get());
                    binding.setViewmodel(viewModel);

                    // If this activity has been opened with the notification to indicate uploaded document
                    if(getActivity().getIntent().hasExtra(DOCUMENT_ID) && activityCreated) {
                        activityCreated = false;
                        showPriorCaseDetailsView();
                    }

                    // Dispose off the subscription to stop interval observable
                    if(!viewModel.hasCallStarted && intervalSubscription != null && !intervalSubscription.isDisposed())
                        intervalSubscription.dispose();

                    /*if(viewModel.callbackRequest.get().getStatus().equalsIgnoreCase("Accepted") && viewModel.hasCallStarted)
                        binding.message.setText("A call has been placed to the patient...");*/

                    /*binding.notifyPropertyChanged(BR.viewmodel);
                    binding.notifyPropertyChanged(BR.callbackRequest);

                    binding.profileDetailsContainer.notifyPropertyChanged(BR.callbackRequest);
                    binding.profileDetailsContainer.notifyPropertyChanged(BR.viewmodel);

                    binding.acceptCallbackRequestLayout.notifyPropertyChanged(BR.viewmodel);
                    binding.callButtonLayout.notifyPropertyChanged(BR.viewmodel);*/
                }
            }
        });

        viewModel.notifyCallbackRejected.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isCallbackRejected) {
                getActivity().onBackPressed();
            }
        });

        viewModel.notifyCallStarted.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean callStarted) {
                binding.message.setText("A call has been placed to the patient...");
            }
        });

        viewModel.isCallButtonActive.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean)
                    binding.callButtonLayout.centerButtonLayout.setSupportBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorAccent)));
                else
                    binding.callButtonLayout.centerButtonLayout.setSupportBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

                binding.callButtonLayout.centerButtonLayout.setClickable(aBoolean);
            }
        });

        binding.setListener(this);
    }

    private void setUpData() {
        viewModel.callbackId.set(getActivity().getIntent().getIntExtra(Constant.CALLBACK_ID, 0));
        viewModel.getCallbackRequest();
        viewModel.getDoctor();
        viewModel.getSystemInfo();
    }

    @Override
    public void acceptOrRejectCallback(boolean isAccepted) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        if(isAccepted)
            dialogBuilder.setMessage("Do you want to accept the call request?");
        else
            dialogBuilder.setMessage("Do you want to reject the call request?");

        dialogBuilder
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        viewModel.acceptOrRejectCallback(isAccepted);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void startVideoCall() {
        Intent videoCallIntent = new Intent(getContext(), VideoCallViewActivity.class);
        videoCallIntent.putExtra(Constant.VIDEO_CALL_STATE, Constant.VIDEO_CALL_INITIATE);
        videoCallIntent.putExtra("channel_name", "BD_channel_callback_id_"+viewModel.callbackRequest.get().getCallbackId());
        videoCallIntent.putExtra("callback_id", String.valueOf(viewModel.callbackRequest.get().getCallbackId()));
        videoCallIntent.putExtra("doctor_id", viewModel.doctor.getId());
        videoCallIntent.putExtra("doctor_name", viewModel.doctor.getName());
        videoCallIntent.putExtra("doctor_image", viewModel.doctor.getImage());
        videoCallIntent.putExtra("patient_name", viewModel.callbackRequest.get().getPatient().getName());
        videoCallIntent.putExtra("patient_user_id", viewModel.callbackRequest.get().getPatient().getUserId());
        //videoCallIntent.putExtra("callback", viewModel.callbackRequest.get());
        viewModel.hasCallStarted = true;
        startActivity(videoCallIntent);
    }

    private void startVideoEmergencyCall() {
        Intent videoCallIntent = new Intent(getContext(), VideoConsultActivity.class);
        videoCallIntent.putExtra(Constant.VIDEO_CALL_STATE, Constant.VIDEO_CALL_INITIATE);
        videoCallIntent.putExtra("channel_name", viewModel.countryCode + "channel_case_id_" + viewModel.callbackRequest.get().getCaseId());
        videoCallIntent.putExtra("doctor_id", viewModel.doctor.getId());
        videoCallIntent.putExtra("doctor_name", viewModel.doctor.getName());
        videoCallIntent.putExtra("doctor_image", viewModel.doctor.getImage());
        videoCallIntent.putExtra("patient_name", viewModel.callbackRequest.get().getPatient().getName());
        videoCallIntent.putExtra("patient_user_id", viewModel.callbackRequest.get().getPatient().getUserId());
        //videoCallIntent.putExtra("callback", viewModel.callbackRequest.get());
        viewModel.hasCallStarted = true;
        startActivity(videoCallIntent);
    }

    @Override
    public void initiateCall() {

        if(viewModel.callbackRequest.get().isVideo()) {

            PermissionsHelper ph = new PermissionsHelper();
            ph.requestCameraAudioPermissions(getContext(), getActivity(), this, this);

        } else {

            if(viewModel.callbackRequest.get().isAllowVideoFollowUp() && viewModel.callbackRequest.get().getStatus().equalsIgnoreCase("Consulted")) {
                // Ask user whether he wants to start a video or audio call using an alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View view = getLayoutInflater().inflate(R.layout.dialog_emergency_call_type, null);
                builder.setView(view);

                builder.setTitle("select call type");
                builder.setNegativeButton("cancel", null);

                View videoCallLayout = view.findViewById(R.id.video_call_layout);
                View audioCallLayout = view.findViewById(R.id.audio_call_layout);

                videoCallLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectCallTypeAlertDialog.dismiss();

                        PermissionsHelper ph = new PermissionsHelper();
                        ph.requestCameraAudioPermissions(CallbackDetailsActivityFragment.this.getContext(), CallbackDetailsActivityFragment.this.getActivity(), CallbackDetailsActivityFragment.this, CallbackDetailsActivityFragment.this);
                    }
                });

                audioCallLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectCallTypeAlertDialog.dismiss();

                        new AlertDialog.Builder(getContext())
                                .setMessage("Once call is placed please do not call again. Try again if you do not get a call within 5 minutes. Do you want to place the call? ")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        viewModel.emergencyCall();

                                        // this condition is already checked in the enclosing if statement
                                        /*if(viewModel.callbackRequest.get().getStatus().equalsIgnoreCase("Consulted")) {
                                            viewModel.emergencyCall();
                                        } else {
                                            viewModel.initiateCall();
                                        }*/
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                });

                selectCallTypeAlertDialog = builder.show();
            } else {
                new AlertDialog.Builder(getContext())
                        .setMessage("Once call is placed please do not call again. Try again if you do not get a call within 5 minutes. Do you want to place the call? ")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(viewModel.callbackRequest.get().getStatus().equalsIgnoreCase("Consulted")) {
                                    viewModel.emergencyCall();
                                } else {
                                    viewModel.initiateCall();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        }
    }

    @Override
    public void initiateEmergencyCall() {
        viewModel.initiateCall();
    }

    @Override
    public void addViewPrescription() {
        Navigator.navigateToCaseDetails(getContext(), viewModel.callbackRequest.get().getCaseId());
    }

    @Override
    public void showPriorCaseDetailsView() {
        ((CallbackDetailsActivity)getActivity()).showPriorCaseDetailsFragment();
    }

    @Override
    public void permissionGranted(PermissionsHelper.PermissionTypes permissionType) {
        // set flag to act
        // viewModel.startCall = true;
        if(((BaseAppCompatActivity)getActivity()).callInProgress) {
            new AlertDialog.Builder(getContext())
                    .setMessage("There is already a call in progress with " + viewModel.callbackRequest.get().getPatient().getName())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton(android.R.string.ok, null).show();
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage("Do you want to place a call with " + viewModel.callbackRequest.get().getPatient().getName() + "?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (viewModel.callbackRequest.get().getCaseId() != 0 && !viewModel.callbackRequest.get().getStatus().equalsIgnoreCase("Accepted"))
                                startVideoEmergencyCall();
                            else
//                                viewModel.initiateCall();
                                startVideoCall();
                        }

                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    @Override
    public void permissionDenied(PermissionsHelper.PermissionTypes permissionType) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constant.PERMISSIONS_REQUEST_CAMERA_AUDIO: {

                boolean permissionGranted = true;

                for(int grantResult: grantResults)
                    if(grantResult != PackageManager.PERMISSION_GRANTED)
                        permissionGranted = false;

                if (permissionGranted) {
                    //cameraAccepted = true;
                    /*for (int i=0; i<grantResults.length; i++) {
                        if(grantResults[i] == PackageManager.PERMISSION_DENIED)
                            cameraAccepted = false;
                    }*/
                    //showGalleryCameraChooser();
                    viewModel.startCall = true;

                } else {
                    viewModel.startCall = false;
                }
            }
            break;
            default: viewModel.startCall = false;
        }
    }
}