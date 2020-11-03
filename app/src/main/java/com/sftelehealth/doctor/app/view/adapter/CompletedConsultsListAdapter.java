package com.sftelehealth.doctor.app.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.sftelehealth.doctor.databinding.ItemCallbackRequestBinding;
import com.sftelehealth.doctor.domain.model.CallbackRequest;
import com.squareup.picasso.Picasso;
import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.view.adapter.listener.CompletedConsultsListener;
import com.sftelehealth.doctor.app.view.helper.CircleTransform;
import com.sftelehealth.doctor.app.view.helper.CloudinaryUrlHelper;
import com.sftelehealth.doctor.databinding.ItemActiveConsultBinding;
import com.sftelehealth.doctor.domain.model.Case;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rahul on 09/03/18.
 */

public class CompletedConsultsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Case> caseList;
    CompletedConsultsListener listener;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    OnLoadMoreListener onLoadMoreListener;

    int visibleItemCount, pastVisiblesItems, currentItemPosition, totalItemCount;
    private boolean loading = true, refreshCallRequestData = true;

    public CompletedConsultsListAdapter(List<Case> caseList, CompletedConsultsListener completedConsultsListener, RecyclerView recyclerView) {
        this.caseList = caseList;
        listener = completedConsultsListener;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            currentItemPosition = visibleItemCount + pastVisiblesItems;
                            loading = false;
                            if(onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                            Log.v("LastRowReached", "Last Item Wow !");
                        }
                    }

                    int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                    //Now you can use this index to manipulate your TextView
                    if(firstVisibleItem == 0) {
                        // refresh the adapter with by making a call to the server
                        if(refreshCallRequestData) {
                            //mAppointments.getRequests().clear();
                            //new RetroDocAppointments(Frg_TwoTab.FragmentCallRequests.this, mContainer, getActivity(), "1");
                            refreshCallRequestData = false;
                            onLoadMoreListener.loadFirst();
                            //page = 1;
                        }
                    } else {
                        // true again after the first child is not visible
                        refreshCallRequestData = true;
                    }
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_ITEM) {
            ItemActiveConsultBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_active_consult, parent, false);
            return new CompletedConsultsListAdapter.CasesViewHolder(binding);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progress_item, parent, false);

            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof CasesViewHolder)
            ((BindingViewHolder)holder).bind(caseList.get(position), listener);
        else
            ((CompletedConsultsListAdapter.ProgressViewHolder)holder).progressBar.setIndeterminate(true);
    }

    @Override
    public int getItemViewType(int position) {
        return caseList.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public int getItemCount() {
        return caseList.size();
    }

    public class CasesViewHolder extends BindingViewHolder {

        public CasesViewHolder(ItemActiveConsultBinding binding) {
            super(binding);
        }

        @Override
        public void bind(Object obj, Object listener) {
            super.bind(obj, listener);

//            Picasso.with(this.itemView.getContext())
//                    .load(CloudinaryUrlHelper.getProfileUrl(((Case)obj).getPatientImage()))
//                    .resize(100, 100)
//                    .placeholder(R.drawable.profile)
//                    .onlyScaleDown()
//                    .transform(new CircleTransform())
//                    .into(((ItemActiveConsultBinding)this.binding).profilePic);

            Glide
                    .with(this.itemView.getContext())
                    .load(((Case)obj).getPatientImage())
                    .centerCrop()
                    .placeholder(R.drawable.profile)
                    .into(((ItemActiveConsultBinding)this.binding).profilePic);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }
    }

    public void setOnLoadMoreListener(CompletedConsultsListAdapter.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
        void loadFirst();
    }

    public void setLoaded() {
        loading = true;
    }
}