package com.sftelehealth.doctor.app.view.viewmodel;

import com.sftelehealth.doctor.data.repository.datasource.CallbackRequestFactory;
import com.sftelehealth.doctor.domain.interactor.GetAppointmentsList;
import com.sftelehealth.doctor.domain.interactor.ShouldUpdateApp;
import com.sftelehealth.doctor.domain.model.CallbackRequest;
import com.sftelehealth.doctor.domain.model.response.ShouldUpdateResponse;
import com.sftelehealth.doctor.domain.repository.CallbackRequestRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;
import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Rahul on 20/12/17.
 */

public class CallbackListFragmentViewModel extends ViewModel {

    public final ObservableField<List<CallbackRequest>> callbackRequestList = new ObservableField<>();
    public List<CallbackRequest> tempCallbackRequestList;
    public Observable<PagedList<CallbackRequest>> callbackRequests;

    public MutableLiveData<Boolean> notifyDataChange = new MutableLiveData<>();

    public String phone;
    public boolean mustUpdateApp;
    int page;

    GetAppointmentsList getAppointmentsList;
    ShouldUpdateApp shouldUpdateApp;

    public MutableLiveData<Boolean> systemDataResponseFetched = new MutableLiveData<>();

    CallbackRequestRepository callbackRequestRepository;
    CallbackRequestFactory callbackRequestFactory;

    CallbackListFragmentViewModel(GetAppointmentsList getAppointmentsList, ShouldUpdateApp shouldUpdateApp, CallbackRequestFactory callbackRequestFactory) {
        this.getAppointmentsList = getAppointmentsList;
        this.shouldUpdateApp = shouldUpdateApp;
        this.callbackRequestFactory = callbackRequestFactory;

        callbackRequestList.set(new ArrayList<CallbackRequest>());
        tempCallbackRequestList = new ArrayList<>();

        // Paged list initialization
        /*callbackRequests = new RxPagedListBuilder(
                callbackRequestFactory, //callbackRequestRepository.getCallbacks(params),
                20 // page size  50
        ).buildObservable();*/
    }

    public void getCallbackRequests(int page) {

        HashMap<String, String> params = new HashMap<>();
        params.put("page","" + page);
        params.put("size","20");

        getAppointmentsList.execute(new DisposableObserver<List<CallbackRequest>>() {
            @Override
            public void onNext(List<CallbackRequest> callbackRequests) {
                tempCallbackRequestList.clear();
                tempCallbackRequestList.addAll(callbackRequests);
                /*callbackRequestList.get().clear();
                callbackRequestList.get().addAll(callbackRequests);
                */
                // tempCallbackRequestList.postValue(callbackRequests);

                notifyDataChange.postValue(true);
            }

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onComplete() {}
        }, params);
    }

    public void shouldUpdate() {

        HashMap<String, String> params = new HashMap<>();

        shouldUpdateApp.execute(new DisposableObserver<ShouldUpdateResponse>() {
            @Override
            public void onNext(ShouldUpdateResponse shouldUpdateResponse) {
                phone = shouldUpdateResponse.getPhone();
                mustUpdateApp = shouldUpdateResponse.isMustUpdate();

//                systemDataResponseFetched.postValue(true);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        }, params);
    }

    public void setUpCallBackListWithPagedAdapter () {

        HashMap<String, String> params = new HashMap<>();
        params.put("page","" + page);
        params.put("size","20");

        /*callbackRequests = new RxPagedListBuilder(
                callbackRequestFactory, //callbackRequestRepository.getCallbacks(params),
                20 // page size  50
        ).buildObservable();*/

    }

}
