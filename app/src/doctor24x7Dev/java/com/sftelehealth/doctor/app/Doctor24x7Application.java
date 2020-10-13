package com.sftelehealth.doctor.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.onesignal.OneSignal;
import com.sftelehealth.doctor.app.internal.di.components.ApplicationComponent;
import com.sftelehealth.doctor.app.internal.di.components.DaggerApplicationComponent;
import com.sftelehealth.doctor.app.internal.di.modules.ApplicationModule;
import com.sftelehealth.doctor.app.utils.onesignal.IdsHandler;
import com.sftelehealth.doctor.app.utils.onesignal.NotificationHandler;
import com.sftelehealth.doctor.app.utils.onesignal.NotificationOpenAction;
import com.sftelehealth.doctor.video.agora.call.CallData;
import com.sftelehealth.doctor.video.helper.AgoraStatusHelper;
import com.sftelehealth.doctor.video.helper.CallDataHelper;
import com.sftelehealth.doctor.video.service.AgoraHelperService;

import javax.inject.Inject;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.InitializationCallback;
import timber.log.Timber;

import static com.sftelehealth.doctor.video.Constant.VIDEO_CALL_STATE_ENDED;

/**
 * Created by Rahul on 21/06/17.
 */

public class Doctor24x7Application extends MultiDexApplication {

    private ApplicationComponent applicationComponent;

    @Inject
    SharedPreferences sp;

    @Override public void onCreate() {
        super.onCreate();

        Fabric.with(new Fabric.Builder(this).initializationCallback(new InitializationCallback<Fabric>() {
            @Override
            public void success(Fabric fabric) {

                Thread.UncaughtExceptionHandler mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(Thread t, Throwable e) {

                        Log.e("CrashLogger","Exception",e);

                        CallData callData = CallDataHelper.getCallData(sp);
                        callData.getState().setVideoCallState(VIDEO_CALL_STATE_ENDED);
                        CallDataHelper.saveCallData(callData,sp);

                        if(AgoraStatusHelper.isServiceRunning(getApplicationContext())) {
                            Intent stopAgoraService = new Intent(getApplicationContext(), AgoraHelperService.class);
                            stopService(stopAgoraService);
                        }

                        mDefaultUEH.uncaughtException(t, e);
                    }
                });
            }

            @Override
            public void failure(Exception e) {
                Log.e("CrashLogger", "Error during initialization ", e);
            }

        }).build());

        initializeInjector();
        initializeLeakDetection();
        initializeLogging();
        initializeDatabase();
        initializeOneSignal();

        applicationComponent.inject(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    private void initializeInjector() {
        this.applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return this.applicationComponent;
    }

    private void initializeLeakDetection() {
    }

    private void initializeLogging() {

        if (com.crashlytics.android.core.BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            // Stetho.initializeWithDefaults(this);
        } else {
            //Timber.plant(new CrashlyticsTree());
        }
    }

    private void initializeDatabase() {

    }

    private void initializeOneSignal() {
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationOpenAction(this))  // callback for handling when notification is received
                .setNotificationReceivedHandler(new NotificationHandler(this))   // callback for handling when notification is opened
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)   // this will show notification always, popup when app is opened is disabled
                .init();

        OneSignal.idsAvailable(new IdsHandler(this));
    }
}
