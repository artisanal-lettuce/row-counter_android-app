package com.example.rowcounter.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProjectDao {
    @Query("SELECT * FROM projects")
    LiveData<List<Project>> getAllProjects();

    @Query("SELECT * FROM projects")
    List<Project> getAllProjectsSync();

    @Query("SELECT * FROM projects WHERE id = :id")
    LiveData<Project> getProjectById(int id);

    @Query("SELECT * FROM projects WHERE id = :id")
    Project getProjectByIdSync(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProject(Project project);

    @Update
    void updateProject(Project project);

    @Delete
    void deleteProject(Project project);
}
