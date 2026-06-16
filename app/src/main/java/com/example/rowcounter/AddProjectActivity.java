package com.example.rowcounter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.example.rowcounter.data.Project;
import com.example.rowcounter.databinding.ActivityAddProjectBinding;

public class AddProjectActivity extends AppCompatActivity {

    private ActivityAddProjectBinding binding;
    private ProjectViewModel projectViewModel;
    private Project existingProject;
    private int selectedBgColor = 0xFFFFFFFF;
    private int selectedFontColor = 0xFF000000;
    private int selectedButtonColor = 0xFF808080;
    private String[] imageUris = new String[3];
    private int currentImageIndex = -1;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && currentImageIndex != -1) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        // Permission might already be granted or not needed for certain URIs
                    }
                    imageUris[currentImageIndex] = uri.toString();
                    ImageView iv = null;
                    if (currentImageIndex == 0) iv = binding.image1;
                    else if (currentImageIndex == 1) iv = binding.image2;
                    else if (currentImageIndex == 2) iv = binding.image3;
                    if (iv != null) iv.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        int projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        if (projectId != -1) {
            setTitle(R.string.edit_project_title);
            projectViewModel.getProjectById(projectId).observe(this, project -> {
                if (project != null) {
                    existingProject = project;
                    populateFields();
                }
            });
        }

        setupColorPickers();
        setupImagePickers();

        binding.buttonSave.setOnClickListener(v -> saveProject());
        binding.buttonDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_message)
                .setPositiveButton(R.string.delete_button, (dialog, which) -> {
                    if (existingProject != null) {
                        projectViewModel.delete(existingProject);
                        // Redirect to MainActivity or finish based on requirements
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel_button, null)
                .show();
    }

    private void populateFields() {
        binding.buttonDelete.setVisibility(View.VISIBLE);
        binding.editProjectName.setText(existingProject.getName());
        binding.editGoalCount.setText(existingProject.getGoalCount() != null ? String.valueOf(existingProject.getGoalCount()) : "");
        binding.editDescription.setText(existingProject.getDescription());

        selectedBgColor = existingProject.getBackgroundColor();
        binding.colorBgPreview.setBackgroundColor(selectedBgColor);

        selectedFontColor = existingProject.getFontColor();
        binding.colorFontPreview.setBackgroundColor(selectedFontColor);

        selectedButtonColor = existingProject.getButtonColor();
        binding.colorButtonPreview.setBackgroundColor(selectedButtonColor);

        if ("SERIF".equals(existingProject.getFontType())) binding.radioSerif.setChecked(true);
        else if ("MONOSPACE".equals(existingProject.getFontType())) binding.radioMono.setChecked(true);
        else binding.radioSans.setChecked(true);

        imageUris[0] = existingProject.getImageUri1();
        imageUris[1] = existingProject.getImageUri2();
        imageUris[2] = existingProject.getImageUri3();

        if (imageUris[0] != null) binding.image1.setImageURI(Uri.parse(imageUris[0]));
        if (imageUris[1] != null) binding.image2.setImageURI(Uri.parse(imageUris[1]));
        if (imageUris[2] != null) binding.image3.setImageURI(Uri.parse(imageUris[2]));
    }

    private void setupColorPickers() {
        // Sync initial colors with XML
        if (binding.colorBgPreview.getBackground() instanceof ColorDrawable) {
            selectedBgColor = ((ColorDrawable) binding.colorBgPreview.getBackground()).getColor();
        }
        if (binding.colorFontPreview.getBackground() instanceof ColorDrawable) {
            selectedFontColor = ((ColorDrawable) binding.colorFontPreview.getBackground()).getColor();
        }
        if (binding.colorButtonPreview.getBackground() instanceof ColorDrawable) {
            selectedButtonColor = ((ColorDrawable) binding.colorButtonPreview.getBackground()).getColor();
        }

        binding.colorBgPreview.setOnClickListener(v -> showColorPicker(0));
        binding.colorFontPreview.setOnClickListener(v -> showColorPicker(1));
        binding.colorButtonPreview.setOnClickListener(v -> showColorPicker(2));
    }

    private void showColorPicker(int type) {
        String prefName;
        int initialColor;
        if (type == 0) {
            prefName = "BgColorPicker";
            initialColor = selectedBgColor;
        } else if (type == 1) {
            prefName = "FontColorPicker";
            initialColor = selectedFontColor;
        } else {
            prefName = "ButtonColorPicker";
            initialColor = selectedButtonColor;
        }

        ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.choose_color_title))
                .setPreferenceName(prefName)
                .setPositiveButton(getString(R.string.select_button),
                        (ColorEnvelopeListener) (envelope, fromUser) -> {
                            int color = envelope.getColor();
                            if (type == 0) {
                                selectedBgColor = color;
                                binding.colorBgPreview.setBackgroundColor(color);
                            } else if (type == 1) {
                                selectedFontColor = color;
                                binding.colorFontPreview.setBackgroundColor(color);
                            } else {
                                selectedButtonColor = color;
                                binding.colorButtonPreview.setBackgroundColor(color);
                            }
                        })
                .setNegativeButton(getString(R.string.cancel_button), (dialogInterface, i) -> dialogInterface.dismiss())
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true);

        // Set initial color to the current selection
        builder.getColorPickerView().setInitialColor(initialColor);
        
        builder.show();
    }

    private void setupImagePickers() {
        binding.image1.setOnClickListener(v -> { currentImageIndex = 0; getContent.launch("image/*"); });
        binding.image2.setOnClickListener(v -> { currentImageIndex = 1; getContent.launch("image/*"); });
        binding.image3.setOnClickListener(v -> { currentImageIndex = 2; getContent.launch("image/*"); });
    }

    private void saveProject() {
        String name = binding.editProjectName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.editProjectName.setError("Name is required");
            return;
        }

        String goalStr = binding.editGoalCount.getText().toString().trim();
        Integer goal = goalStr.isEmpty() ? null : Integer.parseInt(goalStr);
        String description = binding.editDescription.getText().toString().trim();

        String fontType = "SANS_SERIF";
        if (binding.radioSerif.isChecked()) fontType = "SERIF";
        else if (binding.radioMono.isChecked()) fontType = "MONOSPACE";

        if (existingProject != null) {
            existingProject.setName(name);
            existingProject.setGoalCount(goal);
            existingProject.setDescription(description);
            existingProject.setBackgroundColor(selectedBgColor);
            existingProject.setFontColor(selectedFontColor);
            existingProject.setButtonColor(selectedButtonColor);
            existingProject.setFontType(fontType);
            existingProject.setImageUri1(imageUris[0]);
            existingProject.setImageUri2(imageUris[1]);
            existingProject.setImageUri3(imageUris[2]);
            projectViewModel.update(existingProject);
        } else {
            Project project = new Project(name, 0, goal, selectedBgColor, selectedFontColor, selectedButtonColor, description, fontType,
                    imageUris[0], imageUris[1], imageUris[2]);
            projectViewModel.insert(project);
        }
        finish();
    }
}
