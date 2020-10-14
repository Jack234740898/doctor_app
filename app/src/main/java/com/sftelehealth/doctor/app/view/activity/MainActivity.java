package com.sftelehealth.doctor.app.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.helpers.PDFUtils;
import com.sftelehealth.doctor.app.internal.di.components.DaggerUseCaseComponent;
import com.sftelehealth.doctor.app.internal.di.components.UseCaseComponent;
import com.sftelehealth.doctor.app.internal.di.modules.UseCaseModule;
import com.sftelehealth.doctor.app.services.CleanUpSpaceService;
import com.sftelehealth.doctor.app.utils.Constant;
import com.sftelehealth.doctor.app.utils.PermissionsHelper;
import com.sftelehealth.doctor.app.view.fragment.AboutUsFragment;
import com.sftelehealth.doctor.app.view.fragment.DashboardFragment;
import com.sftelehealth.doctor.app.view.fragment.DoctorProfileFragment;
import com.sftelehealth.doctor.app.view.helper.SnackbarHelper;
import com.sftelehealth.doctor.app.view.utils.ActivityUtils;
import com.sftelehealth.doctor.data.database.DatabaseObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.sftelehealth.doctor.video.Constant.REFRESH_VIDEO_CALL_STATUS;

public class MainActivity extends BaseAppCompatActivity implements View.OnClickListener, PermissionsHelper.PermissionCallback, ActivityUtils.AgoraCallStatusChangeListener {

    UseCaseComponent useCaseComponent;

    DrawerLayout drawer;
    Toolbar toolbar;

    FrameLayout homeButton;
    TextView toolbarTitle;
    PDFUtils pdfUtils;

    Menu menu;

    boolean confirmExit = false;

    BroadcastReceiver receiver;
    boolean isRegistered = false;

    PermissionsHelper ph;

    BroadcastReceiver agoraCallStatusListener;
    public SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeInjector();
        initializeView();

        // Crashlytics.getInstance().crash();

        ActivityUtils.showHideStatusBarCallStatusBar(this);
        agoraCallStatusListener = ActivityUtils.getVideoCallStatusReceiver(this);
        sp = getApplication().getSharedPreferences(Constant.USER_PREFS, Context.MODE_PRIVATE);
        ph = new PermissionsHelper();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);

                dialogBuilder.setMessage("Documents removed successfully.");

                dialogBuilder
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                //invalidateOptionsMenu();
            }
        };
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ActivityUtils.showHideStatusBarCallStatusBar(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(agoraCallStatusListener, new IntentFilter(REFRESH_VIDEO_CALL_STATUS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(agoraCallStatusListener);
    }

    @Override
    protected void onDestroy() {
        if(receiver != null && isRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            isRegistered = false;
        }
        super.onDestroy();
    }

    private void initializeView() {

        toolbarTitle = toolbar.findViewById(R.id.toolbarTitle);
        homeButton = (FrameLayout) toolbar.findViewById(R.id.homeButton);
        homeButton.setOnClickListener(this);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);

        toggle.syncState();

        displayView(0);

        pdfUtils = new PDFUtils();
    }

    private void initializeInjector() {
        this.useCaseComponent = DaggerUseCaseComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .useCaseModule(new UseCaseModule())
                .build();
    }

    @Override
    public void onClick(View v) {

        handleClick(v);
        /*if (v.getId() == R.id.home) {
            toolbarTitle.setText(R.string.app_name);
            displayView(0);
            // homeSelected();
        } else if (v.getId() == R.id.profile) {
            toolbarTitle.setText(R.string.toolbar_profile);
            displayView(1);
            // profileSelected();
        } else if (v.getId() == R.id.homeButton) {
            Log.i("MainActivity", "Home button clicked");
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        }

        if (v.getId() != R.id.homeButton)
            drawer.closeDrawer(GravityCompat.START);*/
    }

    public void handleClick(View v) {
        if (v.getId() == R.id.home) {
            toolbarTitle.setText(R.string.app_name);
            displayView(0);
            // homeSelected();
        } else if (v.getId() == R.id.profile) {
            toolbarTitle.setText(R.string.toolbar_profile);
            displayView(1);
            // profileSelected();
        } else if (v.getId() == R.id.homeButton) {
            Log.i("MainActivity", "Home button clicked");
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        } else if (v.getId() == R.id.about_us) {
            toolbarTitle.setText("About Us");
            displayView(2);
        } else if (v.getId() == R.id.contact_us) {

            //Uri uri = Uri.parse("mailto:contactus@traktion.in");
            Uri uri = Uri.parse("mailto:team@doctor24x7.in");
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
            //i.setType("message/rfc822");
            //i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"contact@doctor24x7.com", "support@doctor24x7.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Hi!");
            //i.putExtra(Intent.EXTRA_TEXT   , "body of email");
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                // Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.rate) {

            //TODO LOGOUT

            dbClear();
            sp.edit().clear().commit();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

//            final String appPackageName = getPackageName(); // from Context or Activity object
//            try {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.traktion.doctor")));
//            } catch (android.content.ActivityNotFoundException anfe) {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.traktion.doctor")));
//            }
        }

        if (v.getId() != R.id.homeButton)
            drawer.closeDrawer(GravityCompat.START);
    }

    private void dbClear() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseObject.clearAllTables();
            }
        });

    }

    private void displayView(int position) {

        String className = "";
        boolean addToBackStack = false;

        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = DashboardFragment.newInstance();
                fragment.setArguments(getIntent().getExtras());
                className = DashboardFragment.class.getSimpleName();
                break;
            case 1:
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragment = DoctorProfileFragment.newInstance(false);
                className = DoctorProfileFragment.class.getSimpleName();
                break;
            case 2:
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragment = AboutUsFragment.newInstance();
                className = AboutUsFragment.class.getSimpleName();
                break;
            default:
                break;
        }

        if (fragment != null) {
            if (addToBackStack) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, className)
                        .addToBackStack(className).commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, className)
                        .commitAllowingStateLoss();
            }
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                addToBackStack = true;
            }
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    public UseCaseComponent getUseCaseComponent() {
        return useCaseComponent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constant.PERMISSIONS_REQUEST_READ_WRITE_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted(PermissionsHelper.PermissionTypes.READ_WRITE_STORAGE);

            } else {
                permissionDenied(PermissionsHelper.PermissionTypes.READ_WRITE_STORAGE);
            }
        }
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            //super.onBackPressed();
            // check if home fragment has first tab 0 in view
            if(confirmExit) {
                super.onBackPressed();
            } else {
                confirmExit = true;
                SnackbarHelper.getCustomSnackBar(drawer, "Press back again to exit", "", SnackbarHelper.SnackbarTypes.DEFAULT).show();
            }
        } else
            getFragmentManager().popBackStack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        this.menu = menu;

        getMenuInflater().inflate(R.menu.menu_main, menu);

        // menu.findItem(R.id.action_cleanup).setVisible(!pdfUtils.isPDFFolderEmpty(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_cleanup:

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

                dialogBuilder.setMessage("Do you want to remove documents from your phone to free up space?");

                dialogBuilder
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ph.requestFileSystemPermissions(MainActivity.this, MainActivity.this, MainActivity.this);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();


                return true;
        }

                return super.onOptionsItemSelected(item);
    }

    private void startServiceToFreeUpSpace() {
        if(!isRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("directory_cleaned"));
            isRegistered = true;
        }

        startService(new Intent(this, CleanUpSpaceService.class));
    }

    @Override
    public void permissionGranted(PermissionsHelper.PermissionTypes permissionType) {
        startServiceToFreeUpSpace();
    }

    @Override
    public void permissionDenied(PermissionsHelper.PermissionTypes permissionType) {

    }

    @Override
    public void onStatusChange(int status) {
        // If status changed then change toolbar
        ActivityUtils.showHideStatusBarCallStatusBar(this);
    }
}
