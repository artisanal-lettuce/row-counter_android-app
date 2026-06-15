package com.example.rowcounter;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rowcounter.data.Project;
import com.example.rowcounter.databinding.ItemProjectBinding;

public class ProjectAdapter extends ListAdapter<Project, ProjectAdapter.ProjectViewHolder> {

    private final OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    protected ProjectAdapter(OnProjectClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Project> DIFF_CALLBACK = new DiffUtil.ItemCallback<Project>() {
        @Override
        public boolean areItemsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getCurrentCount() == newItem.getCurrentCount() &&
                    oldItem.getBackgroundColor() == newItem.getBackgroundColor() &&
                    oldItem.getFontColor() == newItem.getFontColor() &&
                    oldItem.getButtonColor() == newItem.getButtonColor();
        }
    };

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProjectBinding binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemProjectBinding binding;

        public ProjectViewHolder(ItemProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Project project, OnProjectClickListener listener) {
            binding.textProjectName.setText(project.getName());
            binding.textProjectName.setTextColor(project.getFontColor());
            
            binding.textCurrentCount.setText(String.valueOf(project.getCurrentCount()));
            binding.textCurrentCount.setTextColor(project.getFontColor());
            
            Typeface tf = Typeface.DEFAULT;
            if ("SERIF".equals(project.getFontType())) tf = Typeface.SERIF;
            else if ("MONOSPACE".equals(project.getFontType())) tf = Typeface.MONOSPACE;
            
            binding.textProjectName.setTypeface(tf);
            binding.textCurrentCount.setTypeface(tf);

            binding.itemContainer.setBackgroundColor(project.getBackgroundColor());
            
            if (project.getGoalCount() != null && project.getGoalCount() > 0) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.setMax(project.getGoalCount());
                binding.progressBar.setProgress(project.getCurrentCount());
                binding.progressBar.setProgressTintList(ColorStateList.valueOf(project.getButtonColor()));
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onProjectClick(project));
        }
    }
}
