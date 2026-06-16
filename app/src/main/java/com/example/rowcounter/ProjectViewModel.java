package com.example.rowcounter;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.rowcounter.data.Project;
import java.util.List;

public class ProjectViewModel extends AndroidViewModel {
    private ProjectRepository repository;
    private LiveData<List<Project>> allProjects;

    public ProjectViewModel(Application application) {
        super(application);
        repository = new ProjectRepository(application);
        allProjects = repository.getAllProjects();
    }

    public LiveData<List<Project>> getAllProjects() {
        return allProjects;
    }

    public void insert(Project project) {
        repository.insert(project);
    }

    public void update(Project project) {
        repository.update(project);
    }

    public void delete(Project project) {
        repository.delete(project);
    }

    public LiveData<Project> getProjectById(int id) {
        return repository.getProjectById(id);
    }
}
