package com.example.rowcounter;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.rowcounter.data.AppDatabase;
import com.example.rowcounter.data.Project;
import com.example.rowcounter.databinding.ActivityProjectDetailBinding;

import android.content.Intent;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ProjectDetailActivity extends AppCompatActivity {

    private ActivityProjectDetailBinding binding;
    private ProjectViewModel projectViewModel;
    private Project currentProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        int projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        if (projectId == -1) {
            finish();
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        db.projectDao().getProjectById(projectId).observe(this, project -> {
            if (project != null) {
                currentProject = project;
                updateUI();
                startNotificationService();
            }
        });

        binding.buttonPlus.setOnClickListener(v -> {
            if (currentProject != null) {
                currentProject.setCurrentCount(currentProject.getCurrentCount() + 1);
                projectViewModel.update(currentProject);
                updateUI();
            }
        });

        binding.buttonMinus.setOnClickListener(v -> {
            if (currentProject != null && currentProject.getCurrentCount() > 0) {
                currentProject.setCurrentCount(currentProject.getCurrentCount() - 1);
                projectViewModel.update(currentProject);
                updateUI();
            }
        });

        binding.buttonSettings.setOnClickListener(v -> {
            if (currentProject != null) {
                Intent intent = new Intent(this, AddProjectActivity.class);
                intent.putExtra("PROJECT_ID", currentProject.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (currentProject != null) {
                startNotificationService();
            }
        }
    }

    private void startNotificationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }
        
        Intent intent = new Intent(this, CounterNotificationService.class);
        intent.setAction(CounterNotificationService.ACTION_START);
        intent.putExtra(CounterNotificationService.EXTRA_PROJECT_ID, currentProject.getId());
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, CounterNotificationService.class);
        intent.setAction(CounterNotificationService.ACTION_STOP);
        stopService(intent);
        super.onDestroy();
    }

    private void updateUI() {
        binding.detailRoot.setBackgroundColor(currentProject.getBackgroundColor());
        binding.textDetailName.setText(currentProject.getName());
        binding.textDetailName.setTextColor(currentProject.getFontColor());
        binding.textDetailCount.setText(String.valueOf(currentProject.getCurrentCount()));
        binding.textDetailCount.setTextColor(currentProject.getFontColor());
        binding.textDetailDescription.setText(currentProject.getDescription());
        binding.textDetailDescription.setTextColor(currentProject.getFontColor());
        
        Typeface tf = Typeface.DEFAULT;
        if ("SERIF".equals(currentProject.getFontType())) tf = Typeface.SERIF;
        else if ("MONOSPACE".equals(currentProject.getFontType())) tf = Typeface.MONOSPACE;
        
        binding.textDetailName.setTypeface(tf);
        binding.textDetailCount.setTypeface(tf);
        binding.textDetailDescription.setTypeface(tf);

        binding.buttonPlus.setBackgroundTintList(ColorStateList.valueOf(currentProject.getButtonColor()));
        binding.buttonMinus.setBackgroundTintList(ColorStateList.valueOf(currentProject.getButtonColor()));

        if (currentProject.getGoalCount() != null && currentProject.getGoalCount() > 0) {
            binding.detailProgressBar.setVisibility(View.VISIBLE);
            binding.detailProgressBar.setMax(currentProject.getGoalCount());
            binding.detailProgressBar.setProgress(currentProject.getCurrentCount());
            binding.detailProgressBar.setProgressTintList(ColorStateList.valueOf(currentProject.getFontColor()));
        } else {
            binding.detailProgressBar.setVisibility(View.GONE);
        }

        loadImage(currentProject.getImageUri1(), binding.detailImage1);
        loadImage(currentProject.getImageUri2(), binding.detailImage2);
        loadImage(currentProject.getImageUri3(), binding.detailImage3);
    }

    private void loadImage(String uriStr, android.widget.ImageView imageView) {
        if (uriStr != null && !uriStr.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(Uri.parse(uriStr)).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }
}
