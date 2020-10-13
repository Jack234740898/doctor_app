package com.sftelehealth.doctor.app.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.view.adapter.listener.CallbackRequestListener;
import com.sftelehealth.doctor.app.view.helper.CircleTransform;
import com.sftelehealth.doctor.app.view.helper.CloudinaryUrlHelper;
import com.sftelehealth.doctor.databinding.ItemCallbackRequestBinding;
import com.sftelehealth.doctor.domain.model.CallbackRequest;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rahul on 28/12/17.
 */

public class CallbackRequestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    List<CallbackRequest> callbackRequestList;
    CallbackRequestListener listener;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    OnLoadMoreListener onLoadMoreListener;
    // RecyclerView.OnScrollListener onScrollListener;

    int visibleItemCount, pastVisiblesItems, currentItemPosition, totalItemCount;
    private boolean loading = true, refreshCallRequestData = true;

    public CallbackRequestListAdapter(List<CallbackRequest> callbackRequestList, CallbackRequestListener callbackRequestListener, RecyclerView recyclerView) {
        this.callbackRequestList = callbackRequestList;
        listener = callbackRequestListener;

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
    public int getItemViewType(int position) {
        return callbackRequestList.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == VIEW_ITEM) {
            ItemCallbackRequestBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_callback_request, parent, false);
            return new CallbackRequestViewHolder(binding);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progress_item, parent, false);

            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof CallbackRequestViewHolder)
            ((BindingViewHolder)holder).bind(callbackRequestList.get(position), listener);
        else
            ((ProgressViewHolder)holder).progressBar.setIndeterminate(true);
    }

    @Override
    public int getItemCount() {
        return callbackRequestList.size();
    }

    public class CallbackRequestViewHolder extends BindingViewHolder {

        public CallbackRequestViewHolder(ItemCallbackRequestBinding binding) {
            super(binding);
        }

        @Override
        public void bind(Object obj, Object listener) {
            super.bind(obj, listener);

            Picasso.with(this.itemView.getContext())
                    .load(CloudinaryUrlHelper.getProfileUrl(((CallbackRequest)obj).getPatientImage()))
                    .resize(100, 100)
                    .placeholder(R.drawable.profile)
                    .onlyScaleDown()
                    .transform(new CircleTransform())
                    .into(((ItemCallbackRequestBinding)this.binding).profilePic);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
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
