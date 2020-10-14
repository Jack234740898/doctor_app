package com.sftelehealth.doctor.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.squareup.picasso.Picasso;
import com.sftelehealth.doctor.BR;
import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.helpers.FileDownloadHelper;
import com.sftelehealth.doctor.app.helpers.PDFUtils;
import com.sftelehealth.doctor.databinding.ItemDocumentPreviewBinding;
import com.sftelehealth.doctor.domain.model.Document;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;

// import com.android.databinding.library.baseAdapters.BR;

/**
 * Created by Rahul on 09/01/18.
 */

public class DocumentsViewPagerAdapter extends PagerAdapter {

    List<Document> documents;
    DocumentPreviewListener listener;
    PDFUtils pdfUtils;

    public DocumentsViewPagerAdapter(List<Document> documents, DocumentPreviewListener listener) {
        this.documents = documents;
        this.listener = listener;
        pdfUtils = new PDFUtils();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //return super.instantiateItem(container, position);
        ItemDocumentPreviewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(container.getContext()), R.layout.item_document_preview, container, false);
        binding.setListener(listener);
        binding.setItem(documents.get(position));

        binding.notifyPropertyChanged(BR.item);

        FileDownloadHelper fdh = new FileDownloadHelper(container.getContext());
        if(pdfUtils.isPDFDownloaded(Integer.parseInt(documents.get(position).getId()), fdh)) {
            binding.downloadPDF.setVisibility(View.GONE);
            binding.openPDF.setVisibility(View.VISIBLE);
        } else {
            binding.downloadPDF.setVisibility(View.VISIBLE);
            binding.openPDF.setVisibility(View.GONE);
        }

        if(documents.get(position).getPreviewUrl() != null)
            Picasso.with(container.getContext())
                .load(documents.get(position).getPreviewUrl())
                .placeholder(R.drawable.circular_loading_animation)
                .into(binding.documentImage);
        else
            Picasso.with(container.getContext())
                    .load(documents.get(position).getPreviewUrl())
                    .placeholder(R.drawable.circular_loading_animation)
                    .into(binding.documentImage);

        //binding.getRoot().setTag(position);
        container.addView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return documents.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // super.destroyItem(container, position, object);
        container.removeView((FrameLayout)object);
    }

    public interface DocumentPreviewListener {
        void onDocumentDownloadClicked(String documentId);
        void onDocumentOpenClicked(String documentId);
    }

    /*public class DocumentsPreviewHolder extends BindingViewHolder{

        public DocumentsPreviewHolder(ViewDataBinding binding) {
            super(binding);
        }
    }*/
}