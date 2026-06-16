package com.example.rowcounter;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.rowcounter.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ProjectViewModel projectViewModel;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Permission handled
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        ProjectAdapter adapter = new ProjectAdapter(project -> {
            Intent intent = new Intent(MainActivity.this, ProjectDetailActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            startActivity(intent);
        });

        binding.recyclerViewProjects.setAdapter(adapter);
        binding.recyclerViewProjects.setLayoutManager(new LinearLayoutManager(this));

        projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        projectViewModel.getAllProjects().observe(this, adapter::submitList);

        binding.fabAddProject.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddProjectActivity.class);
            startActivity(intent);
        });
    }
}
