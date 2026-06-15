package com.example.rowcounter.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int currentCount;
    private Integer goalCount;
    private int backgroundColor;
    private int fontColor;
    private int buttonColor;
    private String description;
    private String fontType; // "SANS_SERIF", "SERIF", "MONOSPACE"
    private String imageUri1;
    private String imageUri2;
    private String imageUri3;

    public Project(String name, int currentCount, Integer goalCount, int backgroundColor, int fontColor, int buttonColor, String description, String fontType, String imageUri1, String imageUri2, String imageUri3) {
        this.name = name;
        this.currentCount = currentCount;
        this.goalCount = goalCount;
        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;
        this.buttonColor = buttonColor;
        this.description = description;
        this.fontType = fontType;
        this.imageUri1 = imageUri1;
        this.imageUri2 = imageUri2;
        this.imageUri3 = imageUri3;
    }

    // Getters and Setters
    public int getButtonColor() { return buttonColor; }
    public void setButtonColor(int buttonColor) { this.buttonColor = buttonColor; }
    public String getFontType() { return fontType; }
    public void setFontType(String fontType) { this.fontType = fontType; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }
    public Integer getGoalCount() { return goalCount; }
    public void setGoalCount(Integer goalCount) { this.goalCount = goalCount; }
    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }
    public int getFontColor() { return fontColor; }
    public void setFontColor(int fontColor) { this.fontColor = fontColor; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUri1() { return imageUri1; }
    public void setImageUri1(String imageUri1) { this.imageUri1 = imageUri1; }
    public String getImageUri2() { return imageUri2; }
    public void setImageUri2(String imageUri2) { this.imageUri2 = imageUri2; }
    public String getImageUri3() { return imageUri3; }
    public void setImageUri3(String imageUri3) { this.imageUri3 = imageUri3; }
}
