package com.noti;

import android.app.NotificationChannel;
import android.app.NotificationManager;

public class NotificationUtil {

    //8.0及以上系统应用通知的channel定义；
    public static final String CHANNEL_ID_1 = "channel_id_1";
    public static final String CHANNEL_NAME_1 = "Test_Channel_Common";//id和name要一一对应


    /**
     * 创建通知channel
     *
     * @param notificationManager
     * @param channelId
     * @param channelName
     * @param importance
     */
    public static void createNotificationChannel(NotificationManager notificationManager, String channelId, String channelName, int importance) {
        if (notificationManager == null) {
            return;
        }
        //重要性设置值大于IMPORTANCE_LOW，通知发送时就会震动，设置禁止震动也无效
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
        /*notificationChannel.enableVibration(false);//不允许震动
        notificationChannel.setVibrationPattern(null);*/
        notificationManager.createNotificationChannel(notificationChannel);
    }

}
