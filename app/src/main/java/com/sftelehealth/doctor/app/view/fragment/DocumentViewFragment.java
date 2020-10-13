package com.sftelehealth.doctor.app.view.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.helpers.FileDownloadHelper;
import com.sftelehealth.doctor.app.helpers.PDFUtils;
import com.sftelehealth.doctor.app.utils.Constant;
import com.sftelehealth.doctor.app.utils.PermissionsHelper;
import com.sftelehealth.doctor.app.view.activity.CallbackDetailsActivity;
import com.sftelehealth.doctor.app.view.activity.CaseDetailsActivity;
import com.sftelehealth.doctor.app.view.activity.PDFViewerActivity;
import com.sftelehealth.doctor.app.view.adapter.DocumentsViewPagerAdapter;
import com.sftelehealth.doctor.databinding.FragmentDocumentViewBinding;
import com.sftelehealth.doctor.domain.model.Document;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import static android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentViewFragment extends Fragment implements DocumentsViewPagerAdapter.DocumentPreviewListener, PermissionsHelper.PermissionCallback {

    FragmentDocumentViewBinding binding;
    // CaseDetailsActivityFragmentViewModel viewModel;

    DocumentsViewPagerAdapter pagerAdapter;

    Document selectedDocument;
    int selectedDocumentPosition;

    ArrayList<Document> documents;

    PermissionsHelper ph;

    PDFUtils pdfUtils;
    FileDownloadHelper fdh;

    BroadcastReceiver downloadStatusReceiver;
    IntentFilter downloadManagerIntent;
    boolean isReceiverRegistered;
    boolean startFileDownload = false;

    public static DocumentViewFragment newInstance(String documentId) {
        DocumentViewFragment instance = new DocumentViewFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("document_id", Integer.parseInt(documentId));
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_document_view, container, false);

        if(getActivity() instanceof CaseDetailsActivity) {
            documents = (ArrayList<Document>) ((CaseDetailsActivity)getActivity()).obtainViewModel(getActivity()).caseObject.get().getPrescriptionAndDocuments().getDocs();
        } else if(getActivity() instanceof CallbackDetailsActivity) {
            documents = ((CallbackDetailsActivity)getActivity()).obtainViewModel(getActivity()).callbackRequest.get().getDocs();
        }

        //viewModel = ((CaseDetailsActivity)getActivity()).obtainViewModel(getActivity());

        pdfUtils = new PDFUtils();
        ph = new PermissionsHelper();
        fdh = new FileDownloadHelper(getContext());

        setUpData();

        return binding.getRoot();
    }

    private void setUpData() {
        //binding.setViewmodel(viewModel);

        // set Adapters for the case documents and prescription
        pagerAdapter = new DocumentsViewPagerAdapter(documents, DocumentViewFragment.this);
        binding.documentsViewPager.setAdapter(pagerAdapter);

        selectedDocumentPosition = getDocumentPosition(this.getArguments().getInt("document_id"));
        selectedDocument = getSelectedDocument(selectedDocumentPosition);

        binding.documentsViewPager.setCurrentItem(selectedDocumentPosition);

        downloadManagerIntent = new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE");
        downloadStatusReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //carouselAdapter.notifyItemChanged(selectedDocument);
                //documentSelected(selectedDocument);
                if(intent.getAction() == ACTION_DOWNLOAD_COMPLETE) {
                    selectedDocumentPosition = binding.documentsViewPager.getCurrentItem();

                    binding.documentsViewPager.getAdapter().notifyDataSetChanged();
                    binding.documentsViewPager.setCurrentItem(selectedDocumentPosition);

                    // open the document with the required intent for a PDF
                    openSelectedDocument(fdh.getInternalFilePath(fdh.getLastDownloadedDocumentPath(intent)));
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.documentsViewPager.getAdapter().notifyDataSetChanged();
        if(startFileDownload)
            permissionGranted(PermissionsHelper.PermissionTypes.READ_WRITE_STORAGE);
    }

    @Override
    public void onDestroy() {
        if(downloadStatusReceiver != null && isReceiverRegistered) {
            getActivity().unregisterReceiver(downloadStatusReceiver);
            isReceiverRegistered = false;
        }
        super.onDestroy();
    }

    @Override
    public void onDocumentDownloadClicked(String documentId) {
        // start document download
        selectedDocumentPosition = getDocumentPosition(Integer.parseInt(documentId));
        selectedDocument = getSelectedDocument(selectedDocumentPosition);
        ph.requestFileSystemPermissions(getContext(), getActivity(), this, this);
    }

    @Override
    public void onDocumentOpenClicked(String documentId) {

        selectedDocument = getSelectedDocument(getDocumentPosition(Integer.parseInt(documentId)));
        openSelectedDocument(fdh.getInternalFilePath(selectedDocument.getId() + "." + selectedDocument.getDocumentFileExtension(), true));
    }

    private Document getSelectedDocument(int position) {
        return documents.get(position);
    }

    private int getDocumentPosition(int documentId) {
        for(int i=0; i < documents.size(); i++) {
            if(Integer.parseInt(documents.get(i).getId()) == documentId)
                return i;
        }
        return -1;
    }

    @Override
    public void permissionGranted(PermissionsHelper.PermissionTypes permissionType) {
        startFileDownload = false;
        if(permissionType == PermissionsHelper.PermissionTypes.READ_WRITE_STORAGE) {
            // show the download button option here
            if(pdfUtils.isPDFDownloaded(Integer.parseInt(selectedDocument.getId()), fdh)) {
                //mPosition = binding.documentsViewPager.getCurrentItem();
                // then open the PDF
                openSelectedDocument(fdh.getInternalFilePath(selectedDocument.getId() + "." + selectedDocument.getDocumentFileExtension(), true));
            } else {
                // else download the PDF and put a receiver when the download completes
                fdh.downloadFile(selectedDocument.getUrl(), selectedDocument.getTitle(), "download in progress...", Integer.parseInt(selectedDocument.getId()), selectedDocument.getDocumentFileExtension());
                getActivity().registerReceiver(downloadStatusReceiver, downloadManagerIntent);
                isReceiverRegistered = true;
            }
        }
    }

    @Override
    public void permissionDenied(PermissionsHelper.PermissionTypes permissionType) {}

    private void openSelectedDocument(String filePath) {

        Intent intent = new Intent(getActivity(), PDFViewerActivity.class);
        intent.putExtra("file_path", filePath);
        intent.putExtra("document_type", selectedDocument.getDocumentCategoryId());
        intent.putExtra("document_title", selectedDocument.getTitle());
        intent.putExtra("document_format", selectedDocument.getDocumentType());
        startActivity(intent);

        // These permissions are used to provide permission to external app to access the internal files of this app
        // FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.PERMISSIONS_REQUEST_READ_WRITE_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //cameraAccepted = true;
                    /*for (int i=0; i<grantResults.length; i++) {
                        if(grantResults[i] == PackageManager.PERMISSION_DENIED)
                            cameraAccepted = false;
                    }*/
                    //showGalleryCameraChooser();
                    startFileDownload = true;

                } else {
                    startFileDownload = false;
                }
            }
        }
    }
}