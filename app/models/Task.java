package models;

import play.libs.Json;

import java.time.LocalDateTime;
import java.util.List;

public class Task {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<String> labels;

    private List<String> imagePaths;




    public Task() {
    }

    public Task(String id, String name, String description, LocalDateTime createdAt, List<String> labels) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.labels = labels;
    }

    public Task(String id, String name, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Task(String id, String name, String description, LocalDateTime createdAt, List<String> labels, List<String> imagePaths) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.labels = labels;
        this.imagePaths = imagePaths;
    }

    // Getters and Setters


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return Json.stringify(Json.toJson(this));
    }

    public List<String> getImagePaths() { return imagePaths; }

    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
}


