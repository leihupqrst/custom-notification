package com.noti;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TimerTaskService extends Service {
    private static final String Tag = TimerTaskService.class.getSimpleName();

    private ScheduledExecutorService pool;

    public static final String ACTION_TOOLBAR_START_HOME = "action_toolbar_start_home";
    public static final String ACTION_TOOLBAR_START_JUNK = "action_toolbar_start_junk";
    public static final String ACTION_TOOLBAR_START_BOOST = "action_toolbar_start_boost";
    public static final String ACTION_TOOLBAR_START_CPU = "action_toolbar_start_cpu";

    private static final int NOTIFICATION_TOOLS_ID_TOOLBAR = 23;

    private static final int REQUEST_CODE_TOOLBAR = 4;

    private RemoteViews shortCutRemoteView;
    private NotificationManager shortCutNotiManager;
    private Notification shortCutNoti;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {

        showShortCutNotification();

        pool = Executors.newSingleThreadScheduledExecutor();

        //Toolbar boost scan every second
        pool.scheduleAtFixedRate(toolbarBoostTask,  0, 1, TimeUnit.SECONDS);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if (intent != null) {
            action = intent.getAction();

            //点击Toolbar的Junk按钮跳转
            if (TextUtils.equals(action, ACTION_TOOLBAR_START_JUNK)) {

                jumpToJunkFiles(action);
                collapsingNotification(getApplicationContext());
            }
            //点击Toolbar的Boost按钮跳转
            else if (TextUtils.equals(action, ACTION_TOOLBAR_START_BOOST)) {

                jumpToBoost(action);
                collapsingNotification(getApplicationContext());

            }

            else if (TextUtils.equals(action, ACTION_TOOLBAR_START_HOME)){
                jumpToHome();
                collapsingNotification(getApplicationContext());
            }

            else if(TextUtils.equals(action, ACTION_TOOLBAR_START_CPU)){

                jumpToCpu(action);
                collapsingNotification(getApplicationContext());
            }

        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void showShortCutNotification() {

        shortCutNotiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        shortCutRemoteView = new RemoteViews(getPackageName(), R.layout.layout_noti_short_cut);
        shortCutRemoteView.setImageViewBitmap(R.id.img_boost, createPercentageBitmapByLayout(0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtil.createNotificationChannel(shortCutNotiManager, NotificationUtil.CHANNEL_ID_1, NotificationUtil.CHANNEL_NAME_1, NotificationManager.IMPORTANCE_LOW);
            shortCutNoti = new NotificationCompat.Builder(this, NotificationUtil.CHANNEL_ID_1).setContent(shortCutRemoteView).setSmallIcon(R.mipmap.ic_launcher)
                    //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())//好多手机加上此行后通知栏按钮点击无反应
                    .setShowWhen(false)
                    .build();
        } else {
            shortCutNoti = new NotificationCompat.Builder(this).setContent(shortCutRemoteView).setSmallIcon(R.mipmap.ic_launcher)
                    //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())//好多手机加上此行后通知栏按钮点击无反应
                    .setShowWhen(false)
                    .build();
        }

        shortCutNoti.flags = Notification.FLAG_ONGOING_EVENT;

        //Home
        Intent intentHome = new Intent(TimerTaskService.this, TimerTaskService.class)
                .setAction(ACTION_TOOLBAR_START_HOME);
        PendingIntent pendingIntentHome = PendingIntent.getService(TimerTaskService.this, REQUEST_CODE_TOOLBAR, intentHome, PendingIntent.FLAG_UPDATE_CURRENT);
        shortCutRemoteView.setOnClickPendingIntent(R.id.layout_home, pendingIntentHome);

        //Junk
        Intent intentJunk = new Intent(TimerTaskService.this, TimerTaskService.class)
                .setAction(ACTION_TOOLBAR_START_JUNK);
        PendingIntent pendingIntentJunk = PendingIntent.getService(TimerTaskService.this, REQUEST_CODE_TOOLBAR, intentJunk, PendingIntent.FLAG_UPDATE_CURRENT);
        shortCutRemoteView.setOnClickPendingIntent(R.id.layout_junk, pendingIntentJunk);

        //Boost
        Intent intentBoost = new Intent(TimerTaskService.this, TimerTaskService.class)
                .setAction(ACTION_TOOLBAR_START_BOOST);
        PendingIntent pendingIntentBoost = PendingIntent.getService(TimerTaskService.this, REQUEST_CODE_TOOLBAR, intentBoost, PendingIntent.FLAG_UPDATE_CURRENT);
        shortCutRemoteView.setOnClickPendingIntent(R.id.layout_boost, pendingIntentBoost);

        //Cpu
        Intent intentCpu = new Intent(TimerTaskService.this, TimerTaskService.class)
                .setAction(ACTION_TOOLBAR_START_CPU);
        PendingIntent pendingIntentCpu = PendingIntent.getService(TimerTaskService.this, REQUEST_CODE_TOOLBAR, intentCpu, PendingIntent.FLAG_UPDATE_CURRENT);
        shortCutRemoteView.setOnClickPendingIntent(R.id.layout_cpu, pendingIntentCpu);

        shortCutNotiManager.notify(NOTIFICATION_TOOLS_ID_TOOLBAR, shortCutNoti);
    }


    private void jumpToJunkFiles(String action){
        Log.d(Tag, "jumpToJunkFiles");

    }

    private void jumpToBoost(String action) {
        Log.d(Tag, "jumpToBoost");

    }

    private void jumpToHome() {
        Log.d(Tag, "jumpToHome");

    }


    private void jumpToCpu(String action){
        Log.d(Tag, "jumpToCpu");

    }


    /**
     * 折起通知页面
     *
     * @param context 必须是application context
     */
    public void collapsingNotification(Context context) {
        Object service = context.getSystemService("statusbar");
        if (null == service)
            return;
        try {
            Class<?> clazz = Class.forName("android.app.StatusBarManager");
            int sdkVersion = Build.VERSION.SDK_INT;
            Method collapse;
            if (sdkVersion <= Build.VERSION_CODES.JELLY_BEAN) {
                collapse = clazz.getMethod("collapse");
            } else {
                collapse = clazz.getMethod("collapsePanels");
            }

            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    protected Bitmap createPercentageBitmapByLayout(int progress)
    {
        RelativeLayout relativeLayout = new RelativeLayout(this);
        ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_percentage_size, relativeLayout, true);

        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        relativeLayout.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED));
        int measuredWidth = relativeLayout.getMeasuredWidth();
        int measuredHeight = relativeLayout.getMeasuredHeight();

        TextView tvRamPercent = relativeLayout.findViewById(R.id.percentage_text);
        tvRamPercent.setText(String.valueOf(progress));

        ArcProgressView stepArcView = relativeLayout.findViewById(R.id.ramProgressBar);
        stepArcView.setCurrentCount(100, progress);
        stepArcView.requestLayout();

        relativeLayout.requestLayout();
        relativeLayout.layout(0, 0, measuredWidth, measuredHeight);
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_4444);
        relativeLayout.draw(new Canvas(bitmap));
        return bitmap;
    }



    TimerTask  toolbarBoostTask = new TimerTask() {

        @Override
        public void run() {

            mHandler.sendEmptyMessage(0);

        }
    };

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(shortCutRemoteView != null){
                int progress = (int) (Math.random() * 100);

                shortCutRemoteView.setImageViewBitmap(R.id.img_boost, createPercentageBitmapByLayout(progress));
                shortCutNotiManager.notify(NOTIFICATION_TOOLS_ID_TOOLBAR, shortCutNoti);
            }
        }
    };

}
