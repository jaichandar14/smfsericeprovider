package com.smf.events.helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.smf.events.R
import com.smf.events.ui.notification.NotificationActivity

//It doesn't expire though. It renews itself if one of the following happens.
//
//According to https://firebase.google.com/docs/cloud-messaging/android/client:
//
//-The app deletes Instance ID
//-The app is restored on a new device
//-The user uninstalls/reinstall the app
//-The user clears app data.

class MyFirebaseInstanseIdService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMessagingServ"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived: ${remoteMessage.notification?.title}")
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (!Objects.equals(null, remoteMessage.notification)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val notificationChannel = NotificationChannel("NOTIFICATION_CHANNEL_ID",
//                    "NOTIFICATION_CHANNEL_NAME",
//                    NotificationManager.IMPORTANCE_HIGH)
//                notificationManager.createNotificationChannel(notificationChannel)
//            }
//            val notificationBuilder: NotificationCompat.Builder =
//                NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL_ID")
//            notificationBuilder.setAutoCancel(true)
//                .setStyle(NotificationCompat.BigTextStyle()
//                    .bigText(remoteMessage.notification!!.body))
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.drawable.festo_login_logo)
//                .setTicker(remoteMessage.notification!!.title)
//                .setContentTitle(remoteMessage.notification!!.title)
//                .setContentText(remoteMessage.notification!!.body)
//            notificationManager.notify(1, notificationBuilder.build())
//        }
        if (remoteMessage.notification != null) {
            pushNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
            Log.d(TAG, "onMessageReceived: ${remoteMessage.notification?.body}")
            remoteMessage.notification?.title?.let {
                remoteMessage.notification?.body?.let { it1 ->
                    showNotification(
                        it,
                        it1
                    )
                }
            }
            Log.d(TAG, "onMessageReceived: ${remoteMessage.data["key_1"]}")
        }
    }

    // Method to get the custom Design for the display of
    // notification.
    @SuppressLint("RemoteViewLayout")
    private fun getCustomDesign(
        title: String,
        message: String,
    ): RemoteViews? {
        val remoteViews = RemoteViews(
            applicationContext.packageName,
            R.layout.notification
        )
        remoteViews.setTextViewText(R.id.title, title)
        remoteViews.setTextViewText(R.id.message, message)
        remoteViews.setImageViewResource(
            R.id.icon,
            R.drawable.festo_nofication
        )
        return remoteViews
    }

    // Method to display the notifications
    private fun showNotification(
        title: String,
        message: String,
    ) {
        // Pass the intent to switch to the MainActivity
        val intent = Intent(this, NotificationActivity::class.java)
        // Assign channel ID
        val channel_id = "notification_channel"
        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
        // the activities present in the activity stack,
        // on the top of the Activity that is to be launched
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Pass the intent to PendingIntent to start the
        // next Activity
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        // Create a Builder object using NotificationCompat
        // class. This will allow control over all the flags
        var builder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext,
            channel_id
        )
            .setSmallIcon(R.drawable.festologo)
            .setAutoCancel(true)
            .setVibrate(
                longArrayOf(
                    1000, 1000, 1000,
                    1000, 1000
                )
            )
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        // A customized design for the notification can be
        // set only for Android versions 4.1 and above. Thus
        // condition for the same is checked here.
        if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.JELLY_BEAN
        ) {
            builder = builder.setContent(
                getCustomDesign(title, message)
            )
        } // If Android Version is lower than Jelly Beans,
        else {
            builder = builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.festologo)
        }
        // Create an object of NotificationManager class to
        // notify the
        // user of events that happen in the background.
        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        // Check if the Android Version is greater than Oreo
        if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.O
        ) {
            val notificationChannel = NotificationChannel(
                channel_id, "web_app",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(
                notificationChannel
            )
        }
        notificationManager.notify(0, builder.build())
    }

    private fun pushNotification(title: String?, body: String?) {
        Log.d(TAG, "pushNotification: $title $body")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
    }

}