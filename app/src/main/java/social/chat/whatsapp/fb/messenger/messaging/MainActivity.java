package social.chat.whatsapp.fb.messenger.messaging;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;

import social.chat.whatsapp.fb.messenger.messaging.Adapters.AppListAdapter;
import social.chat.whatsapp.fb.messenger.messaging.Events.PostEvent;
import social.chat.whatsapp.fb.messenger.messaging.Events.PostNotificationData;
import social.chat.whatsapp.fb.messenger.messaging.Models.NotificationModel;
import social.chat.whatsapp.fb.messenger.messaging.Models.NotificationWear;

public class MainActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    ArrayList<String> data;
    FloatingActionButton githubAction;
    ImageView rateapp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        recyclerView = (RecyclerView) findViewById(R.id.appList);
        githubAction = (FloatingActionButton) findViewById(R.id.github);
        rateapp = (ImageView) findViewById(R.id.rate);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AppListAdapter(this, data));

        /*
        if (Settings.Secure.getInt(this.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1)
            githubAction.setVisibility(View.VISIBLE);
        else
            githubAction.setVisibility(View.GONE);
        */
        githubAction.setVisibility(View.VISIBLE);

        notificationPermission();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
            overlayPermission();

        githubAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                showAlertBox("Developer ? \nHelp me maintain this project", "This project is open sourced on GitHub and I need your support to make it better.")
                        .setPositiveButton("Show Project", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Uri webpage = Uri.parse("https://github.com/mohak1712/ChatBubbleForAll");
                                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                                startActivity(webIntent);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                }).show();
                */
                testUI();
            }
        });

        rateapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rateOnPlayStore();
            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * show alert explaining need for notification permission and ask for the same
     */
    private void notificationPermission() {

        ComponentName cn = new ComponentName(this, NotificationReader.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        boolean enabled = flat != null && flat.contains(cn.flattenToString());

        if (enabled)
            return;

        showAlertBox("Read Notification Permission Required", "App wont work without this permission")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                        else
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        }).show().setCanceledOnTouchOutside(false);

    }

    /**
     * show alert explaining need for overlay permission and ask for the same
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void overlayPermission() {

        showAlertBox("Overlay Permission Required", "Overlay permission is required so that you can chat from anywhere.App wont work without this permission.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (!Settings.canDrawOverlays(MainActivity.this)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 101);
                        }

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        }).show().setCanceledOnTouchOutside(false);

    }

    /**
     * creates alert dialog builder
     *
     * @param title   Title of alert dialog
     * @param message Message inside alert dialog
     * @return AlertDialog.Builder object
     */
    public AlertDialog.Builder showAlertBox(String title, String message) {

        return new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);
    }


    /**
     * open app directly in play store for rating
     */
    private void rateOnPlayStore() {

        final String appPackageName = "social.chat.whatsapp.fb.messenger.messaging";
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }

    }

    private void testUI () {

        /**tes
         * Store notifications in a list
         */
        NotificationModel model = new NotificationModel();
        model.setUserName("test");
        model.setMsg("message");
        model.setGroup("-null_123");
        model.setTime(new Date().getTime());

        botMsgs.add(model);

        if (!FloatingBubble.isServiceRunning) {

            Intent startChat = new Intent(this, FloatingBubble.class);
            startChat.putParcelableArrayListExtra(Constants.msgs, botMsgs);
            startService(startChat);

        } else {

            EventBus.getDefault().post(new PostNotificationData(botMsgs));
        }
    }


    ArrayList<NotificationModel> botMsgs = new ArrayList<>();

    @Subscribe
    public void onConsumePostEvent(PostEvent event) {

        Date date = new Date();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.setMsg("autorResponse_"+date.getTime());
        notificationModel.setTime(date.getTime());
        notificationModel.setUserName("test");
        notificationModel.setGroup("-null_123");

        botMsgs.add(notificationModel);
        EventBus.getDefault().post(new PostNotificationData(botMsgs));
    }

}
