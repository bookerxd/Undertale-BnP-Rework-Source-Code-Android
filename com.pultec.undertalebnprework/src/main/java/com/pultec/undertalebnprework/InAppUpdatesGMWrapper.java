package com.pultec.undertalebnprework;
/*+*/import com.yoyogames.runner.RunnerJNILib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

public class InAppUpdatesGMWrapper implements IExtensionBase {

    private final static int EVENT_OTHER_SOCIAL = 70;

    private final static int ASYNC_RESPONSE_UPDATE_INFO = 621870;
    private final static int UPDATE_AVAILABILITY_NOT_AVAILABLE = 1;
    private final static int UPDATE_AVAILABILITY_AVAILABLE = 2;
    private final static int UPDATE_AVAILABILITY_DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS = 3;

    private final static int REQ_CODE_START_UPDATE = 47842;

    private static boolean isInBackground = false;

    private static AppUpdateManager appUpdateManager;
    private static AppUpdateInfo appUpdateInfo;

    private static final InstallStateUpdatedListener installStateUpdatedListener = installState -> {
        if (installState.installStatus() == InstallStatus.DOWNLOADED) { //update downloaded - display snackbar notification
            if (isInBackground)
                appUpdateManager.completeUpdate();
            //else //app in in foreground - need to ask for an update
            //    showUpdateDownloadedMessage();
        }
    };

    public static void checkForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(RunnerActivity.CurrentActivity);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            InAppUpdatesGMWrapper.appUpdateInfo = appUpdateInfo;

            int map = RunnerJNILib.jCreateDsMap(null, null, null);
            RunnerJNILib.DsMapAddDouble(map, "id", ASYNC_RESPONSE_UPDATE_INFO);
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                RunnerJNILib.DsMapAddDouble(map, "UpdateAvailability", UPDATE_AVAILABILITY_AVAILABLE);
            }
            else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                RunnerJNILib.DsMapAddDouble(map, "UpdateAvailability", UPDATE_AVAILABILITY_DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS);
            }
            else { //update not available
                RunnerJNILib.DsMapAddDouble(map, "UpdateAvailability", UPDATE_AVAILABILITY_NOT_AVAILABLE);
            }
            RunnerJNILib.CreateAsynEventWithDSMap(map, EVENT_OTHER_SOCIAL);
        });
    }

    public static void startUpdateFlow() {
        if (appUpdateManager == null)
            return;

        try {
            appUpdateManager.registerListener(installStateUpdatedListener);
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, RunnerActivity.CurrentActivity,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
                            .setAllowAssetPackDeletion(true)
                            .build(),
                    REQ_CODE_START_UPDATE);

        } catch (IntentSender.SendIntentException e) {
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    //private static void showUpdateDownloadedMessage() {
    //    //!!!Snackbar does not work because GameMaker's activity not using AppCompat theme
    //    ViewGroup viewGroup = RunnerActivity.CurrentActivity.findViewById(android.R.id.content);
    //    Snackbar snackbar = Snackbar.make(viewGroup, "<Update downloaded text>", Snackbar.LENGTH_LONG);
    //    snackbar.setAction("<RESTART>", view -> appUpdateManager.completeUpdate());
    //    snackbar.setActionTextColor(Color.parseColor("#ff0000"));
    //    snackbar.show();
    //}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_START_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                appUpdateManager.unregisterListener(installStateUpdatedListener);
            }
        }
    }

    @Override
    public void onResume() {
        if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED)
                    appUpdateManager.completeUpdate();
                //showUpdateDownloadedMessage();
            });
        }
    }

    @Override
    public void onStop() {
        isInBackground = true;

        if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate();
                }
            });
        }
    }


    @Override
    public void onStart() {
        isInBackground = false;
    }

    @Override
    public void onRestart() {}

    @Override
    public void onDestroy() {}

    @Override
    public void onPause() {}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {}

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {return false;}

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {return false;}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {return false;}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {return false;}

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {return false;}

    @Override
    public Dialog onCreateDialog(int id) {return null;}

    @Override
    public boolean onTouchEvent(MotionEvent event) {return false;}

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {return false;}

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {return false;}

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {return false;}

    @Override
    public boolean performClick() {return false;}

    @Override
    public void onNewIntent(Intent newIntent) {}
}
