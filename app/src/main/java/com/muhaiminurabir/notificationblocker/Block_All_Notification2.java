package com.muhaiminurabir.notificationblocker;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Process;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Set;

public class Block_All_Notification2 extends NotificationListenerService {

    boolean check=false;

    CancelNotificationReceiver mReceiver = new CancelNotificationReceiver();
    public static String Package_Name = "com.muhaiminurabir.notificationblocker";

    private static final String TAG = "NotifiCollectorMonitor";
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        // Implement what you want here
        // Inform the notification manager about dismissal of all notifications.
        Log.d("Msg", "Notification arrived");

        /*if (sbn.getPackageName().equals(Package_Name)){
            Log.d("paisi","paisi");
            cancelAllNotifications();
            return;
        }*/
        start_blocking();
       /* if (false){
            cancelAllNotifications();
        }*/
        //Block_All_Notification2.this.cancelAllNotifications();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        // Implement what you want here
        Log.d("Msg", "Notification Removed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ensureCollectorRunning();
        if (isnotificationserviceenable(getApplicationContext())){
            toggleNotificationListenerService();
        }
        String block = intent.getStringExtra("block");
        Log.d("checking service 1",block+"");
        if (block.equals("yes")){
            check=true;
        }else if(block.equals("no")){
            check=false;
        }
        Log.d("checking service 1",check+"");
        return START_STICKY;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Package_Name = getApplicationContext().getPackageName();
        ensureCollectorRunning();
        if (isnotificationserviceenable(getApplicationContext())){
            toggleNotificationListenerService();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Package_Name);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void start_blocking(){
        Log.d("checking service",check+"");
        if (check==true){
            cancelAllNotifications();
        }
    }

    //broadcast receiver method
    class CancelNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("received service","received");
            String action;
            if (intent != null && intent.getAction() != null) {
                action = intent.getAction();
                if (action.equals(Package_Name)) {
                    String block = intent.getStringExtra("block");
                    if (TextUtils.equals(block, "no")) {
                        Log.d("checking service 1",block+"");
                        if (block.equals("yes")){
                            check=true;
                            cancelAllNotifications();
                        }else if(block.equals("no")){
                            check=false;
                        }
                        Log.d("checking service 1",check+"");
                    } else if (TextUtils.equals(block, "yes")) {
                        Log.d("checking service 1",block+"");
                        if (block.equals("yes")){
                            check=true;
                            cancelAllNotifications();
                        }else if(block.equals("no")){
                            check=false;
                        }
                        Log.d("checking service 1",check+"");
                    }
                }
            }
        }

    }



    //check for service is runnng
    private void ensureCollectorRunning() {
        ComponentName collectorComponent = new ComponentName(this, /*NotificationListenerService Inheritance*/ Block_All_Notification2.class);
        Log.v(TAG, "ensureCollectorRunning collectorComponent: " + collectorComponent);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices == null ) {
            Log.w(TAG, "ensureCollectorRunning() runningServices is NULL");
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                Log.w(TAG, "ensureCollectorRunning service - pid: " + service.pid + ", currentPID: " + Process.myPid() + ", clientPackage: " + service.clientPackage + ", clientCount: " + service.clientCount
                        + ", clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + getResources().getString(service.clientLabel) + ")"));
                if (service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/) {
                    collectorRunning = true;
                }
            }
        }
        if (collectorRunning) {
            Log.d(TAG, "ensureCollectorRunning: collector is running");
            return;
        }
        Log.d(TAG, "ensureCollectorRunning: collector not running, reviving...");
        toggleNotificationListenerService();
    }

    private void toggleNotificationListenerService() {
        Log.d(TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ Block_All_Notification2.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }
    private static boolean isnotificationserviceenable(Context context){
        try{
            Set<String>packaageNames= NotificationManagerCompat.getEnabledListenerPackages(context);
            if (packaageNames.contains(context.getPackageName())){
                return true;
            }
        }catch (Exception e){
            Log.d("Error Line Number",Log.getStackTraceString(e));
        }
        return false;
    }

}