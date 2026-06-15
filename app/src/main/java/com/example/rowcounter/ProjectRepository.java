package com.example.rowcounter;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.rowcounter.data.AppDatabase;
import com.example.rowcounter.data.Project;
import com.example.rowcounter.data.ProjectDao;
import java.util.List;

public class ProjectRepository {
    private ProjectDao projectDao;
    private LiveData<List<Project>> allProjects;

    public ProjectRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        projectDao = db.projectDao();
        allProjects = projectDao.getAllProjects();
    }

    public LiveData<List<Project>> getAllProjects() {
        return allProjects;
    }

    public void insert(Project project) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            projectDao.insertProject(project);
        });
    }

    public void update(Project project) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            projectDao.updateProject(project);
        });
    }

    public LiveData<Project> getProjectById(int id) {
        return projectDao.getProjectById(id);
    }

    public Project getProjectByIdSync(int id) {
        return projectDao.getProjectByIdSync(id);
    }
}
