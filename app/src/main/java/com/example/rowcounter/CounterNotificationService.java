package com.example.rowcounter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.rowcounter.data.AppDatabase;
import com.example.rowcounter.data.Project;
import com.example.rowcounter.data.ProjectDao;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.graphics.Typeface;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

public class CounterNotificationService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_PLUS = "ACTION_PLUS";
    public static final String ACTION_MINUS = "ACTION_MINUS";
    public static final String EXTRA_PROJECT_ID = "PROJECT_ID";

    private static final String CHANNEL_ID = "counter_channel";
    private static final int NOTIFICATION_ID = 1001;

    private int currentProjectId = -1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            int projectId = intent.getIntExtra(EXTRA_PROJECT_ID, -1);

            if (ACTION_START.equals(action) && projectId != -1) {
                currentProjectId = projectId;
                startForegroundService();
                updateNotification();
            } else if (ACTION_STOP.equals(action)) {
                stopForeground(true);
                stopSelf();
            } else if (ACTION_PLUS.equals(action) || ACTION_MINUS.equals(action)) {
                handleCounterUpdate(action.equals(ACTION_PLUS));
            }
        }
        return START_STICKY;
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Project Tracker",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        Notification notification = createNotification(null);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void handleCounterUpdate(boolean isPlus) {
        if (currentProjectId == -1) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            ProjectDao dao = AppDatabase.getDatabase(this).projectDao();
            Project project = dao.getProjectByIdSync(currentProjectId);
            if (project != null) {
                if (isPlus) {
                    project.setCurrentCount(project.getCurrentCount() + 1);
                } else if (project.getCurrentCount() > 0) {
                    project.setCurrentCount(project.getCurrentCount() - 1);
                }
                dao.updateProject(project);
                updateNotification();
                
                // Update all home screen widgets to keep them in sync
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(this, RowCounterWidget.class));
                for (int id : ids) {
                    RowCounterWidget.updateAppWidget(this, appWidgetManager, id);
                }
            }
        });
    }

    private void updateNotification() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ProjectDao dao = AppDatabase.getDatabase(this).projectDao();
            Project project = dao.getProjectByIdSync(currentProjectId);
            if (project != null) {
                Notification notification = createNotification(project);
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) nm.notify(NOTIFICATION_ID, notification);
            }
        });
    }

    private Notification createNotification(@Nullable Project project) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification_layout);

        if (project != null) {
            String fontType = project.getFontType();
            String fontFamily = "sans-serif";
            if ("SERIF".equals(fontType)) fontFamily = "serif";
            else if ("MONOSPACE".equals(fontType)) fontFamily = "monospace";

            views.setTextViewText(R.id.notification_project_name, createStyledSpannable(project.getName(), fontFamily, true));
            views.setTextViewText(R.id.notification_count, createStyledSpannable(String.valueOf(project.getCurrentCount()), fontFamily, true));
            views.setTextViewText(R.id.notification_button_minus, createStyledSpannable("−", fontFamily, true));
            views.setTextViewText(R.id.notification_button_plus, createStyledSpannable("+", fontFamily, true));
            
            // Dynamic styling
            views.setInt(R.id.notification_background_img, "setColorFilter", project.getBackgroundColor());
            views.setTextColor(R.id.notification_project_name, project.getFontColor());
            views.setTextColor(R.id.notification_count, project.getFontColor());
            views.setTextColor(R.id.notification_button_plus, project.getFontColor());
            views.setTextColor(R.id.notification_button_minus, project.getFontColor());
            views.setInt(R.id.notification_button_plus_bg, "setColorFilter", project.getButtonColor());
            views.setInt(R.id.notification_button_minus_bg, "setColorFilter", project.getButtonColor());

            // Button Intents
            views.setOnClickPendingIntent(R.id.notification_button_plus, getServiceAction(ACTION_PLUS, project.getId()));
            views.setOnClickPendingIntent(R.id.notification_button_minus, getServiceAction(ACTION_MINUS, project.getId()));
        }

        Intent mainIntent = new Intent(this, ProjectDetailActivity.class);
        if (project != null) mainIntent.putExtra("PROJECT_ID", project.getId());
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mainPI = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomContentView(views)
                .setCustomBigContentView(views)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(mainPI)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }

    private static SpannableString createStyledSpannable(String text, String fontFamily, boolean isBold) {
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new TypefaceSpan(fontFamily), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    private PendingIntent getServiceAction(String action, int projectId) {
        Intent intent = new Intent(this, CounterNotificationService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        return PendingIntent.getService(this, action.hashCode() + projectId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopForeground(true);
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
