package com.example.rowcounter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.graphics.Typeface;
import android.widget.RemoteViews;

import com.example.rowcounter.data.AppDatabase;
import com.example.rowcounter.data.Project;
import com.example.rowcounter.data.ProjectDao;

import java.util.List;

public class RowCounterWidget extends AppWidgetProvider {

    private static final String ACTION_PLUS = "com.example.rowcounter.ACTION_PLUS";
    private static final String ACTION_MINUS = "com.example.rowcounter.ACTION_MINUS";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int projectId = WidgetConfigActivity.loadProjectPref(context, appWidgetId);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // Intent to open configuration on click
            Intent configIntent = new Intent(context, WidgetConfigActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Use a unique request code for each widget
            PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_project_name, configPendingIntent);

            ProjectDao dao = AppDatabase.getDatabase(context).projectDao();
            Project project = projectId != -1 ? dao.getProjectByIdSync(projectId) : null;
            
            if (project != null) {
                // Apply Font Style
                String fontType = project.getFontType();
                String fontFamily = "sans-serif";
                if ("SERIF".equals(fontType)) fontFamily = "serif";
                else if ("MONOSPACE".equals(fontType)) fontFamily = "monospace";

                // Helper to apply font to Spannable
                views.setTextViewText(R.id.widget_project_name, createStyledSpannable(project.getName(), fontFamily, true));
                views.setTextViewText(R.id.widget_count, createStyledSpannable(String.valueOf(project.getCurrentCount()), fontFamily, true));
                views.setTextViewText(R.id.widget_button_minus, createStyledSpannable("−", fontFamily, true));
                views.setTextViewText(R.id.widget_button_plus, createStyledSpannable("+", fontFamily, true));
                
                // Professional background styling
                views.setInt(R.id.widget_background_img, "setColorFilter", project.getBackgroundColor());
                
                views.setTextColor(R.id.widget_project_name, project.getFontColor());
                views.setTextColor(R.id.widget_count, project.getFontColor());
                views.setTextColor(R.id.widget_button_minus, project.getFontColor());
                views.setTextColor(R.id.widget_button_plus, project.getFontColor());

                // Apply button color to the rounded outline
                views.setInt(R.id.widget_button_minus_bg, "setColorFilter", project.getButtonColor());
                views.setInt(R.id.widget_button_plus_bg, "setColorFilter", project.getButtonColor());

                // Setup PendingIntents for + and -
                views.setOnClickPendingIntent(R.id.widget_button_plus, getPendingSelfIntent(context, ACTION_PLUS, project.getId()));
                views.setOnClickPendingIntent(R.id.widget_button_minus, getPendingSelfIntent(context, ACTION_MINUS, project.getId()));

                // Intent to open project details on counter click
                // Fallback to MainActivity if ProjectDetailActivity isn't the main way to view
                Intent detailIntent = new Intent(context, MainActivity.class);
                detailIntent.putExtra("PROJECT_ID", project.getId());
                detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent detailPendingIntent = PendingIntent.getActivity(context, project.getId(), detailIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_count, detailPendingIntent);
            } else {
                views.setTextViewText(R.id.widget_project_name, "Tap to setup");
                views.setTextViewText(R.id.widget_count, "—");
                
                // Clean empty state
                views.setInt(R.id.widget_background_img, "setColorFilter", 0xFFF5F5F5);
                views.setTextColor(R.id.widget_project_name, 0xFF9E9E9E);
                views.setTextColor(R.id.widget_count, 0xFF9E9E9E);
                
                views.setInt(R.id.widget_button_minus_bg, "setColorFilter", 0xFFE0E0E0);
                views.setInt(R.id.widget_button_plus_bg, "setColorFilter", 0xFFE0E0E0);
                
                // Clear +/- actions if no project
                views.setOnClickPendingIntent(R.id.widget_button_plus, null);
                views.setOnClickPendingIntent(R.id.widget_button_minus, null);
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            WidgetConfigActivity.deleteProjectPref(context, appWidgetId);
        }
        super.onDeleted(context, appWidgetIds);
    }

    private static SpannableString createStyledSpannable(String text, String fontFamily, boolean isBold) {
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new TypefaceSpan(fontFamily), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    private static PendingIntent getPendingSelfIntent(Context context, String action, int projectId) {
        Intent intent = new Intent(context, RowCounterWidget.class);
        intent.setAction(action);
        intent.putExtra("PROJECT_ID", projectId);
        return PendingIntent.getBroadcast(context, projectId + action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_PLUS.equals(action) || ACTION_MINUS.equals(action)) {
            int projectId = intent.getIntExtra("PROJECT_ID", -1);
            if (projectId != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    ProjectDao dao = AppDatabase.getDatabase(context).projectDao();
                    Project project = dao.getProjectByIdSync(projectId);
                    if (project != null) {
                        if (ACTION_PLUS.equals(action)) {
                            project.setCurrentCount(project.getCurrentCount() + 1);
                        } else {
                            if (project.getCurrentCount() > 0) {
                                project.setCurrentCount(project.getCurrentCount() - 1);
                            }
                        }
                        dao.updateProject(project);

                        // Update all widgets
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, RowCounterWidget.class));
                        for (int id : ids) {
                            updateAppWidget(context, appWidgetManager, id);
                        }
                    }
                });
            }
        }
    }
}
