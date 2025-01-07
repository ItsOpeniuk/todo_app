package repositories;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import models.Task;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

@Singleton
public class TaskRepository {

    private final MongoCollection<Document> collection;

    @Inject
    public TaskRepository(MongoDatabase database) {
        this.collection = database.getCollection("tasks");
    }

    // Create a new task
    public Task createTask(Task task) {
        if (task.getId() == null) {
            task.setId(UUID.randomUUID().toString());
        }
        Document doc = taskToDocument(task);
        collection.insertOne(doc);
        return task;
    }

    // Get all tasks
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        for (Document doc : collection.find()) {
            tasks.add(documentToTask(doc));
        }
        return tasks;
    }

    // Get a task by ID
    public Task getTaskById(String id) {
        Document doc = collection.find(eq("_id", id)).first();
        if (doc != null) {
            return documentToTask(doc);
        } else {
            return null;
        }
    }

    // Update a task
    public void updateTask(Task task) {
        Document updateDoc = new Document("$set", new Document()
                .append("name", task.getName())
                .append("description", task.getDescription())
                .append("labels", task.getLabels())
                .append("imagePaths", task.getImagePaths()));
        collection.updateOne(eq("_id", task.getId()), updateDoc);
    }

    // Delete a task
    public void deleteTask(String id) {
        collection.deleteOne(eq("_id", id));
    }

    // Get all tasks with optional filtering and sorting
    public List<Task> getTasks(LocalDateTime startDate, LocalDateTime endDate, List<String> labels, String sortBy, String sortOrder) {
        List<Bson> filters = new ArrayList<>();

        // Date range filter
        if (startDate != null && endDate != null) {
            filters.add(and(
                    gte("createdAt", toDate(startDate)),
                    lte("createdAt", toDate(endDate))
            ));
        } else if (startDate != null) {
            filters.add(gte("createdAt", toDate(startDate)));
        } else if (endDate != null) {
            filters.add(lte("createdAt", toDate(endDate)));
        }

        // Labels filter
        if (labels != null && !labels.isEmpty()) {
            filters.add(all("labels", labels));
        }

        Bson filter = filters.isEmpty() ? new Document() : and(filters);

        // Sorting
        Bson sort = null;
        if ("createdAt".equalsIgnoreCase(sortBy)) {
            sort = "asc".equalsIgnoreCase(sortOrder) ? ascending("createdAt") : descending("createdAt");
        } else if ("name".equalsIgnoreCase(sortBy)) {
            sort = "asc".equalsIgnoreCase(sortOrder) ? ascending("name") : descending("name");
        }

        List<Task> tasks = new ArrayList<>();
        if (sort != null) {
            for (Document doc : collection.find(filter).sort(sort)) {
                tasks.add(documentToTask(doc));
            }
        } else {
            for (Document doc : collection.find(filter)) {
                tasks.add(documentToTask(doc));
            }
        }
        return tasks;
    }


    // Helper method to convert Task to Document
    private Document taskToDocument(Task task) {
        Document doc = new Document();
        doc.append("_id", task.getId());
        doc.append("name", task.getName());
        doc.append("description", task.getDescription());
        doc.append("createdAt", toDate(task.getCreatedAt()));
        doc.append("labels", task.getLabels());
        return doc;
    }

    // Helper method to convert Document to Task
    private Task documentToTask(Document doc) {
        Task task = new Task();
        task.setId(doc.getString("_id"));
        task.setName(doc.getString("name"));
        task.setDescription(doc.getString("description"));
        task.setCreatedAt(toLocalDateTime(doc.getDate("createdAt")));
        task.setLabels((List<String>) doc.get("labels"));
        // Map imagePaths
        List<String> imagePaths = (List<String>) doc.get("imagePaths");
        task.setImagePaths(imagePaths != null ? imagePaths : new ArrayList<>());
        return task;
    }

    // Helper methods for date conversion
    private java.util.Date toDate(LocalDateTime dateTime) {
        return java.util.Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

