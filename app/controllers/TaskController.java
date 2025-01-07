package controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.Task;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositories.TaskRepository;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import play.libs.Files.TemporaryFile;
public class TaskController extends Controller {

    private final TaskRepository taskRepository;
    private final Algorithm jwtAlgorithm;
    private final String jwtIssuer;

    @Inject
    public TaskController(TaskRepository taskRepository, Config config) {
        this.taskRepository = taskRepository;
        // Initialize the JWT algorithm with a secret key from configuration
        String secretKey = config.getString("jwt.secret");
        this.jwtAlgorithm = Algorithm.HMAC256(secretKey);
        this.jwtIssuer = config.getString("jwt.issuer");
    }

    // Helper method to authenticate requests
    private boolean isAuthenticated(Http.Request request) {
        String authHeader = request.getHeaders().get("Authorization").orElse("");
        if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                JWT.require(jwtAlgorithm)
                        .withIssuer(jwtIssuer)
                        .build()
                        .verify(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    // Endpoint to create a new task
    public Result createTask(Http.Request request) {
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Invalid JSON");
        }
        try {
            Task task = Json.fromJson(json, Task.class);
            task.setCreatedAt(LocalDateTime.now());
            Task createdTask = taskRepository.createTask(task);
            return created(Json.toJson(createdTask));
        } catch (Exception e) {
            return internalServerError("Error creating task");
        }
    }

    // Endpoint to get tasks with filtering and sorting
    public Result getTasks(Http.Request request) {
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }
        try {
            // Retrieve query parameters
            String startDateStr = request.getQueryString("startDate");
            String endDateStr = request.getQueryString("endDate");
            String labelsStr = request.getQueryString("labels");
            String sortBy = request.getQueryString("sortBy");
            String sortOrder = request.getQueryString("sortOrder");

            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
            List<String> labels = null;

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            // Parse date parameters
            if (startDateStr != null) {
                startDate = LocalDateTime.parse(startDateStr, formatter);
            }
            if (endDateStr != null) {
                endDate = LocalDateTime.parse(endDateStr, formatter);
            }

            // Parse labels parameter
            if (labelsStr != null) {
                labels = List.of(labelsStr.split(","));
            }

            List<Task> tasks = taskRepository.getTasks(startDate, endDate, labels, sortBy, sortOrder);
            return ok(Json.toJson(tasks));
        } catch (Exception e) {
            return internalServerError("Error retrieving tasks");
        }
    }

    // Endpoint to get a task by ID
    public Result getTaskById(String id, Http.Request request) {
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }
        try {
            Task task = taskRepository.getTaskById(id);
            if (task != null) {
                return ok(Json.toJson(task));
            } else {
                return notFound("Task not found");
            }
        } catch (Exception e) {
            return internalServerError("Error retrieving task");
        }
    }

    // Endpoint to update a task
    public Result updateTask(String id, Http.Request request) {
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Invalid JSON");
        }
        try {
            Task task = Json.fromJson(json, Task.class);
            task.setId(id);
            taskRepository.updateTask(task);
            return ok("Task updated successfully");
        } catch (Exception e) {
            return internalServerError("Error updating task");
        }
    }

    // Endpoint to delete a task
    public Result deleteTask(String id, Http.Request request) {
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }
        try {
            taskRepository.deleteTask(id);
            return ok("Task deleted successfully");
        } catch (Exception e) {
            return internalServerError("Error deleting task");
        }
    }

    // Endpoint for user login to obtain JWT token

    public Result login(Http.Request request) {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Invalid JSON");
        }
        String username = json.findPath("username").asText();
        String password = json.findPath("password").asText();
        // Simple authentication logic (replace with real authentication)
        if ("user".equals(username) && "pass".equals(password)) {
            String token = JWT.create()
                    .withIssuer(jwtIssuer)
                    .withClaim("username", username)
                    .sign(jwtAlgorithm);
            return ok(Json.newObject().put("token", token));
        } else {
            return unauthorized("Invalid credentials");
        }
    }

    public Result uploadPdf(String taskId, Http.Request request) {
        // Authenticate the user
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }

        // Extract file from the request
        Http.MultipartFormData<TemporaryFile> formData = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<TemporaryFile> filePart = formData.getFile("file");

        if (filePart == null) {
            return badRequest("Missing file");
        }

        String contentType = filePart.getContentType();
        if (!"application/pdf".equals(contentType)) {
            return badRequest("Only PDF files are supported");
        }

        TemporaryFile tempFile = filePart.getRef();
        Path tempFilePath = tempFile.path(); // Get the file's path
        File pdfFile = tempFilePath.toFile(); // Convert Path to File

        Task task = taskRepository.getTaskById(taskId);
        if (task == null) {
            return notFound("Task not found");
        }

        try {
            // Create a directory to store images
            String uploadDirPath = "public/uploads/tasks/" + taskId;
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Convert PDF to images
            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer renderer = new PDFRenderer(document);
            List<String> imagePaths = new ArrayList<>();

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 300); // High quality 300 DPI
                String imagePath = uploadDirPath + "/page_" + (page + 1) + ".png";
                File imageFile = new File(imagePath);
                ImageIO.write(image, "png", imageFile);
                imagePaths.add(imageFile.getAbsolutePath());
            }

            document.close();

            // Associate image paths with the task
            if (task.getImagePaths() == null) {
                task.setImagePaths(new ArrayList<>());
            }
            task.getImagePaths().addAll(imagePaths);
            taskRepository.updateTask(task);

            return ok("PDF uploaded and processed successfully");
        } catch (IOException e) {
            return internalServerError("Error processing PDF: " + e.getMessage());
        }
    }

    public Result exportTask(String taskId, Http.Request request) {
        // Authenticate the user
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }

        Task task = taskRepository.getTaskById(taskId);
        if (task == null) {
            return notFound("Task not found");
        }

        System.out.println("Task object: " + Json.stringify(Json.toJson(task)));


        try {
            // Create a temporary ZIP file
            File zipFile = File.createTempFile("task_" + taskId, ".zip");

            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
                // Add task metadata as a JSON file
                ZipEntry metadataEntry = new ZipEntry("task_metadata.json");
                zipOut.putNextEntry(metadataEntry);
                String taskMetadataJson = Json.stringify(Json.toJson(task));
                zipOut.write(taskMetadataJson.getBytes());
                zipOut.closeEntry();

                // Add task images to the ZIP file
                if (task.getImagePaths() != null) {
                    for (String imagePath : task.getImagePaths()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            ZipEntry imageEntry = new ZipEntry("images/" + imageFile.getName());
                            zipOut.putNextEntry(imageEntry);
                            Files.copy(imageFile.toPath(), zipOut);
                            zipOut.closeEntry();
                        }
                    }
                }
            }

            // Return the ZIP file as a downloadable response
            return ok(zipFile)
                    .as("application/zip")
                    .withHeader("Content-Disposition", "attachment; filename=task_" + taskId + ".zip");
        } catch (IOException e) {
            return internalServerError("Error exporting task: " + e.getMessage());
        }
    }


    public Result deleteImage(String taskId, String imageName, Http.Request request) {
        // Authenticate the user
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }

        Task task = taskRepository.getTaskById(taskId);
        if (task == null) {
            return notFound("Task not found");
        }

        if (task.getImagePaths() == null || task.getImagePaths().isEmpty()) {
            return badRequest("No images associated with this task");
        }

        // Find and delete the image
        String imagePathToDelete = null;
        for (String imagePath : task.getImagePaths()) {
            if (imagePath.endsWith(imageName)) {
                imagePathToDelete = imagePath;
                break;
            }
        }

        if (imagePathToDelete == null) {
            return notFound("Image not found");
        }

        File imageFile = new File(imagePathToDelete);
        if (imageFile.exists() && imageFile.delete()) {
            // Remove the image path from the task
            task.getImagePaths().remove(imagePathToDelete);
            taskRepository.updateTask(task);
            return ok("Image deleted successfully");
        } else {
            return internalServerError("Failed to delete image");
        }
    }

    public Result updateImage(String taskId, String imageName, Http.Request request) {
        // Authenticate the user
        if (!isAuthenticated(request)) {
            return unauthorized("Unauthorized");
        }

        Http.MultipartFormData<TemporaryFile> formData = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<TemporaryFile> filePart = formData.getFile("file");

        if (filePart == null) {
            return badRequest("Missing file");
        }

        String contentType = filePart.getContentType();
        if (!contentType.startsWith("image/")) {
            return badRequest("Only image files are supported");
        }

        Task task = taskRepository.getTaskById(taskId);
        if (task == null) {
            return notFound("Task not found");
        }

        if (task.getImagePaths() == null) {
            task.setImagePaths(new ArrayList<>());
        }

        // Find the image to replace or create a new one
        String imagePathToUpdate = null;
        for (String imagePath : task.getImagePaths()) {
            if (imagePath.endsWith(imageName)) {
                imagePathToUpdate = imagePath;
                break;
            }
        }

        // Path where the new or updated image will be saved
        String imageDirectory = "public/uploads/tasks/" + taskId;
        File imageDir = new File(imageDirectory);
        if (!imageDir.exists()) {
            imageDir.mkdirs(); // Create directory if it doesn't exist
        }
        File imageFile = new File(imageDir, imageName);

        TemporaryFile tempFile = filePart.getRef();
        try {
            // If the image exists, replace it; otherwise, create a new one
            Files.copy(tempFile.path(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (imagePathToUpdate == null) {
                // Add the new image path to the task
                task.getImagePaths().add(imageFile.getAbsolutePath());
            }

            // Update the task in the database
            taskRepository.updateTask(task);

            return ok(imagePathToUpdate == null ? "Image created successfully" : "Image updated successfully");
        } catch (IOException e) {
            return internalServerError("Error saving image: " + e.getMessage());
        }
    }





}



