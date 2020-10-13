package com.sftelehealth.doctor.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.databinding.ItemPrescriptionListBinding;
import com.sftelehealth.doctor.databinding.ItemVitalsListBinding;
import com.sftelehealth.doctor.domain.model.Vitals;

import java.util.List;

/**
 * Created by Rahul on 09/01/18.
 */

public class VitalsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Vitals> vitals;
    VitalsListListener listener;

    public VitalsListAdapter(List<Vitals> vitals, VitalsListListener listener) {
        this.vitals = vitals;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemVitalsListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_vitals_list, parent, false);
        return new VitalsHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BindingViewHolder)holder).bind(vitals.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return vitals.size();
    }

    public class VitalsHolder extends BindingViewHolder {

        public VitalsHolder(ViewDataBinding binding) {
            super(binding);
        }
    }

    public interface VitalsListListener{
        void onVitalsClicked(Vitals vitals);
    }
}
