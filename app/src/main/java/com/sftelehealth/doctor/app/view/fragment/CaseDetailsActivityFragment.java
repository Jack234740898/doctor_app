package com.sftelehealth.doctor.app.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.sftelehealth.doctor.app.view.activity.VitalsHistoryActivity;
import com.sftelehealth.doctor.app.view.adapter.VitalsListAdapter;
import com.sftelehealth.doctor.domain.model.Vitals;
import com.squareup.picasso.Picasso;
import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.listener.CaseDetailsEventListener;
import com.sftelehealth.doctor.app.utils.Constant;
import com.sftelehealth.doctor.app.utils.PermissionsHelper;
import com.sftelehealth.doctor.app.view.Navigator;
import com.sftelehealth.doctor.app.view.activity.BaseAppCompatActivity;
import com.sftelehealth.doctor.app.view.activity.CaseDetailsActivity;
import com.sftelehealth.doctor.app.view.adapter.DocumentsListAdapter;
import com.sftelehealth.doctor.app.view.adapter.PrescriptionListAdapter;
import com.sftelehealth.doctor.app.view.helper.CircleTransform;
import com.sftelehealth.doctor.app.view.helper.SnackbarHelper;
import com.sftelehealth.doctor.app.view.helper.SpacesItemDecoration;
import com.sftelehealth.doctor.app.view.viewmodel.CaseDetailsActivityFragmentViewModel;
import com.sftelehealth.doctor.databinding.FragmentCaseDetailsActivityBinding;
import com.sftelehealth.doctor.domain.model.Prescription;
import com.sftelehealth.doctor.video.view.VideoConsultActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import io.reactivex.disposables.Disposable;

/**
 * A simple {@link Fragment} subclass.
 */
public class CaseDetailsActivityFragment extends Fragment implements CaseDetailsEventListener, DocumentsListAdapter.DocumentsListListener, PrescriptionListAdapter.PrescriptionListListener, VitalsListAdapter.VitalsListListener, PermissionsHelper.PermissionCallback {

    FragmentCaseDetailsActivityBinding binding;
    CaseDetailsActivityFragmentViewModel viewModel;

    DocumentsListAdapter documentsAdapter;
    PrescriptionListAdapter prescriptionsAdapter;
    VitalsListAdapter vitalsListAdapter;

    Disposable intervalSubscription;

    BroadcastReceiver callRequestsReceiver;

    AlertDialog selectCallTypeAlertDialog;

    public CaseDetailsActivityFragment() {
        // Required empty public constructor
    }

    public static CaseDetailsActivityFragment newInstance() {
        return new CaseDetailsActivityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_case_details_activity, container, false);

        viewModel = ((CaseDetailsActivity) getActivity()).obtainViewModel(getActivity());

        setUpData();
        setUpObservers();

        // check if data (caseObject) is already available and then do a fetch
        viewModel.getDataForScreen();
        viewModel.getSystemInfo();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(callRequestsReceiver,
                new IntentFilter("refresh_event"));

        // if call status is accepted then try to
        /*if(viewModel.hasCallStarted) {
            // start a timer to check callback request
            intervalSubscription = Observable.interval(1, 3, TimeUnit.SECONDS, Schedulers.io())
                    .takeWhile(val -> (viewModel.caseObject.get().isVideo()) ? val < 3 : val < 10).subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    viewModel.getDataForScreen();
                }
            });
        }*/

        if(viewModel.startCall) {
            viewModel.startCall = false;
            //startVideoCall();
            initiateEmergencyCall();
        }
    }

    @Override
    public void onPause() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(callRequestsReceiver);

        super.onPause();
    }

    private void setUpData() {

        callRequestsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
               /* String action = intent.getStringExtra("action");
                if(action.equalsIgnoreCase("callback"))*/
                viewModel.getDataForScreen();
            }
        };

        if (getActivity().getIntent().hasExtra(Constant.CASE_ID))
            viewModel.caseId.set(String.valueOf(getActivity().getIntent().getIntExtra(Constant.CASE_ID, 0)));

        binding.setListener(this);
    }

    private void setUpObservers() {

        viewModel.notifyCaseFetched.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean) {
                    binding.loadingContainer.setVisibility(View.GONE);

                    // load data for the prescription list as well as the documents fragment
                    binding.setCaseItem(viewModel.caseObject.get());


                    Picasso.with(getContext())
                            .load(viewModel.caseObject.get().getPatientImage())
                            .placeholder(R.drawable.profile)
                            .transform(new CircleTransform())
                            .into(binding.patientHeader.patientImage);

                    // set Adapters for the case documents and prescription
                    if (viewModel.caseObject.get().getPrescriptionAndDocuments().getDocs().size() > 0) {
                        documentsAdapter = new DocumentsListAdapter(viewModel.caseObject.get().getPrescriptionAndDocuments().getDocs(), CaseDetailsActivityFragment.this);
                        LinearLayoutManager documentsLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                        binding.documentsListContainer.documentsList.setLayoutManager(documentsLayoutManager);
                        binding.documentsListContainer.documentsList.addItemDecoration(new SpacesItemDecoration(8, 16, 8, 16));
                        binding.documentsListContainer.documentsList.setAdapter(documentsAdapter);
                    } else {
                        binding.documentsListContainer.documentsList.setVisibility(View.GONE);
                        binding.documentsListContainer.documentsDefaultText.setVisibility(View.VISIBLE);
                    }

                    binding.addPrescription.setVisibility(View.GONE);

                    if (viewModel.caseObject.get().getPrescriptionAndDocuments().getPrescriptions().size() > 0) {
                        prescriptionsAdapter = new PrescriptionListAdapter(viewModel.caseObject.get().getPrescriptionAndDocuments().getPrescriptions(), CaseDetailsActivityFragment.this);
                        LinearLayoutManager prescriptionLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        binding.prescriptionListContainer.prescriptionList.setLayoutManager(prescriptionLayoutManager);
                        binding.prescriptionListContainer.prescriptionList.addItemDecoration(new SpacesItemDecoration(8, 16, 8, 16));
                        binding.prescriptionListContainer.prescriptionList.setAdapter(prescriptionsAdapter);

                        // Check if the case is a flollow up, in this case show the prescription adding button
                        // (Last consult ID for the case is not equal to the consult ID for the latest prescription)
                        if (viewModel.caseObject.get().getLastConusltId() != viewModel.caseObject.get().getPrescriptionAndDocuments().getPrescriptions().get(0).getConsultId()) {
                            binding.addPrescription.setVisibility(View.VISIBLE);
                        }
                    } else {
                        binding.prescriptionListContainer.prescriptionList.setVisibility(View.GONE);
                        binding.prescriptionListContainer.prescriptionsDefaultText.setVisibility(View.VISIBLE);
                        binding.addPrescription.setVisibility(View.VISIBLE);
                    }

                    if (viewModel.caseObject.get().getPrescriptionAndDocuments().getVitals().size() > 0) {
                        vitalsListAdapter = new VitalsListAdapter(viewModel.caseObject.get().getPrescriptionAndDocuments().getVitals(), CaseDetailsActivityFragment.this);
                        LinearLayoutManager vitalsLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        binding.vitalsListContainer.prescriptionList.setLayoutManager(vitalsLayoutManager);
                        binding.vitalsListContainer.prescriptionList.addItemDecoration(new SpacesItemDecoration(8, 16, 8, 16));
                        binding.vitalsListContainer.prescriptionList.setAdapter(vitalsListAdapter);

                    } else {
                        binding.vitalsListContainer.prescriptionList.setVisibility(View.GONE);
                        binding.vitalsListContainer.prescriptionsDefaultText.setVisibility(View.GONE);
                    }

                    binding.assesmentListContainer.prescriptionList.setVisibility(View.GONE);
                    binding.assesmentHeader.setVisibility(View.GONE);
                    binding.assesmentListTag.setVisibility(View.GONE);

                    // Dispose off the subscription to stop interval observable
                    /*if(!viewModel.hasCallStarted && intervalSubscription != null && !intervalSubscription.isDisposed())
                        intervalSubscription.dispose();*/

                } else {
                    // some error happened so show an appropriate layout
                }
            }
        });

        viewModel.notifyEmergencyCallInitiated.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isCallInitiated) {
                if (isCallInitiated)
                    SnackbarHelper.getCustomSnackBar(binding.getRoot(), "Call has been initiated...", "", SnackbarHelper.SnackbarTypes.INFO).show();
                else
                    SnackbarHelper.getCustomSnackBar(binding.getRoot(), "Failed to initiate call, try again later.", "", SnackbarHelper.SnackbarTypes.ERROR).show();
            }
        });
    }

    @Override
    public void createNewPrescription() {
        Navigator.navigateToCreatePrescription(getContext(), Integer.parseInt(viewModel.caseId.get()), viewModel.caseObject.get().getLastConusltId(), viewModel.caseObject.get().getPrescriptionAndDocuments().getDoctorCategoryId());
    }

    @Override
    public void initiateEmergencyCall() {


        if(viewModel.caseObject.get().isVideo()) {

            PermissionsHelper ph = new PermissionsHelper();
            ph.requestCameraAudioPermissions(getContext(), getActivity(), this, this);

        } else {
            if(viewModel.caseObject.get().isAllowVideoFollowUp()) {
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
                        ph.requestCameraAudioPermissions(CaseDetailsActivityFragment.this.getContext(), CaseDetailsActivityFragment.this.getActivity(), CaseDetailsActivityFragment.this, CaseDetailsActivityFragment.this);
                    }
                });

                audioCallLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectCallTypeAlertDialog.hide();

                        if (viewModel.caseObject.get().isConsultPending()) {
                            if (((BaseAppCompatActivity) getActivity()).callInProgress) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("There is already a call in progress with " + viewModel.caseObject.get().getPatient().getName())
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setNegativeButton(android.R.string.ok, null).show();
                            } else {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("There is a pending consult with the patient, do you want to place a call? Once call is placed please do not call again. Try again if you do not get a call within 5 minutes.")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (viewModel.caseObject.get().isVideo())
                                                    startVideoCall();
                                                else
                                                    startAudioCall();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, null).show();
                            }

                        } else {
                            if (((BaseAppCompatActivity) getActivity()).callInProgress) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("There is already a call in progress with " + viewModel.caseObject.get().getPatient().getName())
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setNegativeButton(android.R.string.ok, null).show();
                            } else {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Do you want to place a call with " + viewModel.caseObject.get().getPatient().getName() + "?")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (viewModel.caseObject.get().isVideo())
                                                    startVideoEmergencyCall();
                                                else
                                                    startAudioEmergencyCall();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, null).show();
                            }
                        }
                    }
                });

                selectCallTypeAlertDialog = builder.show();
            } else {
                if (viewModel.caseObject.get().isConsultPending()) {
                    if (((BaseAppCompatActivity) getActivity()).callInProgress) {
                        new AlertDialog.Builder(getContext())
                                .setMessage("There is already a call in progress with " + viewModel.caseObject.get().getPatient().getName())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setNegativeButton(android.R.string.ok, null).show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setMessage("There is a pending consult with the patient, do you want to place a call? Once call is placed please do not call again. Try again if you do not get a call within 5 minutes.")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (viewModel.caseObject.get().isVideo())
                                            startVideoCall();
                                        else
                                            startAudioCall();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                } else {
                    if (((BaseAppCompatActivity) getActivity()).callInProgress) {
                        new AlertDialog.Builder(getContext())
                                .setMessage("There is already a call in progress with " + viewModel.caseObject.get().getPatient().getName())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setNegativeButton(android.R.string.ok, null).show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setMessage("Do you want to place a call with " + viewModel.caseObject.get().getPatient().getName() + "?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (viewModel.caseObject.get().isVideo())
                                            startVideoEmergencyCall();
                                        else
                                            startAudioEmergencyCall();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                }
            }
        }
    }

    @Override
    public void onPrescriptionClicked(Prescription prescription) {
        Gson gson = new Gson();
        String prescriptionString = gson.toJson(prescription, Prescription.class);
        // show the prescription
        Navigator.navigateToViewPrescription(getContext(), prescriptionString, viewModel.caseObject.get().getPrescriptionAndDocuments().getDoctorCategoryId());
    }

    @Override
    public void onDocumentImageClicked(String documentId) {
        // show the pager view
        ((CaseDetailsActivity) getActivity()).showDocumentPreview(documentId);
    }

    private void startVideoCall() {
        Intent videoCallIntent = new Intent(getContext(), VideoConsultActivity.class);
        videoCallIntent.putExtra(Constant.VIDEO_CALL_STATE, Constant.VIDEO_CALL_INITIATE);
        videoCallIntent.putExtra("channel_name", viewModel.countryCode + "channel_callback_id_" + viewModel.caseObject.get().getCallbackId());
        videoCallIntent.putExtra("doctor_id", viewModel.doctor.get().getId());
        videoCallIntent.putExtra("doctor_name", viewModel.doctor.get().getName());
        videoCallIntent.putExtra("doctor_image", viewModel.doctor.get().getImage());
        videoCallIntent.putExtra("patient_name", viewModel.caseObject.get().getPatient().getName());
        videoCallIntent.putExtra("patient_user_id", viewModel.caseObject.get().getPatient().getUserId());
        viewModel.hasCallStarted = true;
        startActivity(videoCallIntent);
    }

    private void startVideoEmergencyCall() {
        Intent videoCallIntent = new Intent(getContext(), VideoConsultActivity.class);
        videoCallIntent.putExtra(Constant.VIDEO_CALL_STATE, Constant.VIDEO_CALL_INITIATE);
        videoCallIntent.putExtra("channel_name", viewModel.countryCode + "channel_case_id_" + viewModel.caseObject.get().getCaseId());
        videoCallIntent.putExtra("doctor_id", viewModel.doctor.get().getId());
        videoCallIntent.putExtra("doctor_name", viewModel.doctor.get().getName());
        videoCallIntent.putExtra("doctor_image", viewModel.doctor.get().getImage());
        videoCallIntent.putExtra("patient_name", viewModel.caseObject.get().getPatient().getName());
        videoCallIntent.putExtra("patient_user_id", viewModel.caseObject.get().getPatient().getUserId());
        //videoCallIntent.putExtra("callback", viewModel.callbackRequest.get());
        // Not required to refresh UI for emergency calls
        // viewModel.hasCallStarted = true;
        startActivity(videoCallIntent);
    }

    private void startAudioCall() {
        viewModel.hasCallStarted = true;
        viewModel.initiateCall();
    }

    private void startAudioEmergencyCall() {
        viewModel.initiateEmergencyCall();
    }

    @Override
    public void permissionGranted(PermissionsHelper.PermissionTypes permissionType) {
        if(((BaseAppCompatActivity)getActivity()).callInProgress) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Call is in progress with " + viewModel.caseObject.get().getPatient().getName() + "?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton(android.R.string.ok, null).show();
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage("Do you want to place a call with " + viewModel.caseObject.get().getPatient().getName() + "?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!viewModel.caseObject.get().isConsultPending())
                                startVideoEmergencyCall();
                            else
                                startVideoCall();
                        }

                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    @Override
    public void permissionDenied(PermissionsHelper.PermissionTypes permissionType) {

    }

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

    @Override
    public void onVitalsClicked(Vitals vitals) {
        Intent intent = new Intent(getActivity(), VitalsHistoryActivity.class);
        intent.putExtra("VITALS", vitals);
        startActivity(intent);
    }
}
