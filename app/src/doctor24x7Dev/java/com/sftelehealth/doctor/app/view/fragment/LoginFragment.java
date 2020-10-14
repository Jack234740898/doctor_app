package com.sftelehealth.doctor.app.view.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.securepreferences.SecurePreferences;
import com.sftelehealth.doctor.BuildConfig;
import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.listener.LoginListener;
import com.sftelehealth.doctor.app.utils.Constant;
import com.sftelehealth.doctor.app.utils.PermissionsHelper;
import com.sftelehealth.doctor.app.view.Navigator;
import com.sftelehealth.doctor.app.view.activity.LoginActivity;
import com.sftelehealth.doctor.app.view.activity.TermsAndConditionsActivity;
import com.sftelehealth.doctor.app.view.helper.SnackbarHelper;
import com.sftelehealth.doctor.app.view.viewmodel.LoginFragmentViewModel;
import com.sftelehealth.doctor.databinding.FragmentLoginBinding;
import com.sftelehealth.doctor.domain.model.response.IsUserAuthenticatedResponse;
import com.sftelehealth.doctor.domain.model.response.SendOtpResponse;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.sftelehealth.doctor.app.utils.Constant.APP_UPDATE_IMMEDIATE;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends BaseFragment implements LoginListener {

    public LoginFragmentViewModel viewModel;

    private FragmentLoginBinding binding;

    PermissionsHelper permissionsHelper;
    public static final int MY_PERMISSIONS_REQUEST_READ_SMS = 1;

    boolean canReadSMS = false;

    public LoginFragment() {}

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.getComponent(UseCaseComponent.class).inject(this);

        checkIfUpdateAvailable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        // setUpCustomLayoutForDatePicker();
        //binding = FragmentLoginBinding.inflate(inflater, container, false);
//        binding.mainContainer.setVisibility(View.VISIBLE);
//        binding.splashScreen.setVisibility(View.GONE);
        viewModel = ((LoginActivity)getActivity()).obtainViewModel(getActivity());
        //binding.setView(this);
        binding.setViewmodel(viewModel);
        binding.setListener(this);

        setUpObservables();
        viewModel.isUserAuthenticated();

        permissionsHelper = new PermissionsHelper();

        return binding.getRoot();
    }

    private void setUpObservables() {

        viewModel.isAuthenticated.observe(this, new Observer<IsUserAuthenticatedResponse>() {
            @Override
            public void onChanged(@Nullable IsUserAuthenticatedResponse isAuthenticated) {
                /*if(!isAuthenticated.isHasMigratedToDoctorCategories()) {
                    viewModel.phoneNumber.set(isAuthenticated.getPhoneNumber());
                    viewModel.getDoctorObject();
                }*/
                Context applicationContext = getContext().getApplicationContext();

                Observable.timer(2, TimeUnit.SECONDS, Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableObserver<Long>() {
                            @Override
                            public void onNext(Long aLong) {}

                            @Override
                            public void onError(Throwable e) {}

                            @Override
                            public void onComplete() {

                                if(isAuthenticated.isTokenAvailable()) {
                                    // Doctor data is available or transitioning from an older app

                                    if(isAuthenticated.isDoctorObjectAvailable()) {
                                        if(!isAuthenticated.isDoctorDataSet()) {
                                            Navigator.navigateToProfileSetup(getContext());
                                        } else if(applicationContext != null) {
                                            // If Doctor object is available then its a normally logged in user and must be redirected to the main view
                                            Navigator.navigateToMainView(applicationContext);
                                        }
                                    } else {
                                        String phoneNumber = getSharedPreferences().getString(Constant.PHONE, null);
                                        if(phoneNumber != null) {
                                            viewModel.phoneNumber.set(phoneNumber);
                                            // get the phone number from existing shared preference of the old app and fetch the doctor object and login the user
                                            viewModel.getDoctorObject();
                                        } else {
                                            // phone number not found make the doctor login again
                                            binding.mainContainer.setVisibility(View.VISIBLE);
                                            binding.splashScreen.setVisibility(View.GONE);
                                        }
                                    }
                                } else {
                                    // A new Doctor is Signing up
                                    binding.mainContainer.setVisibility(View.VISIBLE);
                                    binding.splashScreen.setVisibility(View.GONE);
                                }

                                /*if(isAuthenticated.)
                                    Navigator.navigateToMainView(getContext());
                                else {
                                    binding.mainContainer.setVisibility(View.VISIBLE);
                                    binding.splashScreen.setVisibility(View.GONE);
                                }*/
                            }
                        });
            }
        });

        viewModel.isRegistered.observe(this, new Observer<SendOtpResponse>() {
            @Override
            public void onChanged(@Nullable SendOtpResponse sendOtpResponse) {
                if(sendOtpResponse.isRegistered())
                    Navigator.navigateToOTPConfirmation(getContext(), viewModel.phoneNumber.get(), sendOtpResponse.getOtp());
                else
                    SnackbarHelper.getCustomSnackBar(binding.getRoot(), "You are not registered on our platform.\nPlease contact at team@doctor24x7.in", null, SnackbarHelper.SnackbarTypes.ERROR).show();
            }
        });

        viewModel.doctorDataFetched.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean doctorDataFetched) {
                if(doctorDataFetched)
                    Navigator.navigateToMainView(getContext());
            }
        });

       /* binding.countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                ((TextView) binding.countryCodePicker.findViewById(R.id.textView_selectedCountry)).setText(binding.countryCodePicker.getSelectedCountryCodeWithPlus());
            }
        });*/
    }

    @Override
    public void login(View login) {
        // let login happen in background even if permission is being requested at the background
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();

            } else {
                canReadSMS = true;
                viewModel.generateOTP("");
            }
        } else {
            canReadSMS = true;
            viewModel.generateOTP("");
        }
    }

    @Override
    public void viewTermsAndConditions() {
        Intent termsIntent = new Intent(getActivity(), TermsAndConditionsActivity.class);
        startActivity(termsIntent);
    }

    private void requestPermissions() {

        // Here, thisActivity is the current activity
        int readSMS = ContextCompat.checkSelfPermission(getActivity(),  Manifest.permission.READ_SMS);
        int receiveSMS = ContextCompat.checkSelfPermission(getActivity(),  Manifest.permission.RECEIVE_SMS);
        int readContacts = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);
        int writeContacts = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CONTACTS);

        final String[] listPermissionsNeeded = new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.i("Permission", "Permission required to WRITE_CONTACT");
                new AlertDialog.Builder(getActivity())
                        //.setTitle("Delete entry")
                        .setCancelable(false)
                        .setMessage("We show " + Constant.DOCTOR_CONTACT_NAME + " number on your screen when a doctor calls you. Please provide access to your contacts to save " + getActivity().getResources().getString(R.string.app_name) + " number.")   //context.getResources().getString(R.string.app_name)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(listPermissionsNeeded,
                                        MY_PERMISSIONS_REQUEST_READ_SMS);
                            }
                        })
                        .show();
            } else {

                // No explanation needed, we can request the permission.
                requestPermissions(listPermissionsNeeded,
                        MY_PERMISSIONS_REQUEST_READ_SMS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canReadSMS = true;
                }

                if (!canReadSMS) {
                    //Toast.makeText(this, "Cannot use this feature without requested permission", Toast.LENGTH_SHORT).show();

                } else {
                    // user now provided permission
                    // perform function for what you want to achieve
                }

                // login request with permission result
                if(viewModel.phoneNumber.get() != null && !TextUtils.isEmpty(viewModel.phoneNumber.get().toString())) {
                    viewModel.generateOTP("");
                }
            }
        }
    }

    public SharedPreferences getSharedPreferences() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return new SecurePreferences(getContext(), "@h>wPJ_P;YW48/z3", BuildConfig.APPLICATION_ID + ".user_prefs_encrypted");
        } else {
            return getContext().getSharedPreferences(getContext() + ".user_prefs_encrypted", Context.MODE_PRIVATE);
        }
        //return mInstance.getSharedPreferences(AppController.getPackageNameForRef(), Context.MODE_PRIVATE);
    }

    private void checkIfUpdateAvailable() {
        if (Build.VERSION.SDK_INT >= 21) {
            // Creates instance of the manager.
            AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(getContext());

            // Returns an intent object that you use to check for an update.
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                        // Only for odd version codes, make the update mandatory
                        && appUpdateInfo.availableVersionCode() % 2 != 0) {

                    Log.d("App Update", "update available - IMMEDIATE");

                    // Request the update.
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                AppUpdateType.IMMEDIATE,
                                // The current activity making the update request.
                                getActivity(),
                                // Include a request code to later monitor this update request.
                                APP_UPDATE_IMMEDIATE);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e("App Update", "error occured - " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    Log.d("App Update", "update available - FLEXIBLE");
                    // Proceed to next screen

                } else {
                    Log.d("App Update", "update not available");
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == APP_UPDATE_IMMEDIATE) {
            if (resultCode != RESULT_OK) {
                Log.e("App Update", "Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
            } else {
                checkIfUpdateAvailable();
            }
        }
    }

    /*private void setUpCustomLayoutForDatePicker() {
        // binding.countryCodePicker.findViewById(R.id.countryCodeHolder).setVisibility(View.GONE);
        // binding.countryCodePicker.findViewById(R.id.imageView_arrow);
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "Montserrat-Regular.ttf");
        binding.countryCodePicker.setTypeFace(typeface);
        binding.countryCodePicker.findViewById(R.id.image_flag).setVisibility(View.GONE);
        ((TextView) binding.countryCodePicker.findViewById(R.id.textView_selectedCountry)).setText(binding.countryCodePicker.getDefaultCountryCodeWithPlus());
    }*/
}
