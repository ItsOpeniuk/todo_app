package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import repositories.TaskRepository;
import com.typesafe.config.Config;
import models.Task;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskControllerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Config config;

    private TaskController taskController;

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Mock required configuration values
        when(config.getString("jwt.secret")).thenReturn("very-secret-key");
        when(config.getString("jwt.issuer")).thenReturn("todolist");

        // Manually instantiate the TaskController
        taskController = new TaskController(taskRepository, config);
    }

    @Test
    public void testLoginBadRequest() {
        Helpers.running(fakeApplication(), () -> {
            // Create a fake request with a null JSON body
            Http.RequestBuilder request = fakeRequest("POST", "/login").bodyJson((JsonNode) null);

            // Use route with application context
            Result result = taskController.login(request.build());

            // Verify the response status
            assertEquals(BAD_REQUEST, result.status());
        });
    }

    @Test
    public void testLoginUnauthorized() {
        Helpers.running(fakeApplication(), () -> {
            // Create a fake request with incorrect credentials
            Map<String, String> creds = new HashMap<>();
            creds.put("username", "wronguser");
            creds.put("password", "wrongpass");
            Http.RequestBuilder request = fakeRequest("POST", "/login").bodyJson(Json.toJson(creds));

            // Call the login method
            Result result = taskController.login(request.build());

            // Verify the response status
            assertEquals(UNAUTHORIZED, result.status());
        });
    }

    @Test
    public void testLoginSuccess() {
        Helpers.running(fakeApplication(), () -> {
            // Create a fake request with correct credentials
            Map<String, String> creds = new HashMap<>();
            creds.put("username", "user");
            creds.put("password", "pass");
            Http.RequestBuilder request = fakeRequest("POST", "/login").bodyJson(Json.toJson(creds));

            // Call the login method
            Result result = taskController.login(request.build());

            // Verify the response status
            assertEquals(OK, result.status());

            // Verify the response contains a token
            assertTrue(contentAsString(result).contains("token"));
        });
    }

    @Test
    public void testCreateTaskUnauthorized() {
        Helpers.running(fakeApplication(), () -> {
            // Create a fake task
            Task newTask = new Task();
            newTask.setName("Sample Task");
            newTask.setDescription("Description");

            // Create a fake request without Authorization header
            Http.RequestBuilder request = fakeRequest("POST", "/tasks").bodyJson(Json.toJson(newTask));

            // Call the createTask method
            Result result = taskController.createTask(request.build());

            // Verify the response status
            assertEquals(UNAUTHORIZED, result.status());
        });
    }

    @Test
    public void testCreateTaskAuthorized() {
        Helpers.running(fakeApplication(), () -> {
            // Generate a valid token (mocked in this test)
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0b2RvbGlzdCIsInVzZXJuYW1lIjoidXNlciJ9.SShl3_tYaOinKsg6kQwsK8ulnsjxAf4ofxLc1BIjAdA";

            // Create a fake task
            Task newTask = new Task();
            newTask.setName("Sample Task");
            newTask.setDescription("Description");
            newTask.setLabels(Arrays.asList("work", "urgent"));
            newTask.setCreatedAt(LocalDateTime.now());

            // Mock the repository method to return the created task
            when(taskRepository.createTask(any(Task.class))).thenReturn(newTask);

            // Create a fake request with Authorization header
            Http.RequestBuilder request = fakeRequest("POST", "/tasks")
                    .header("Authorization", token)
                    .bodyJson(Json.toJson(newTask));

            // Call the createTask method
            Result result = taskController.createTask(request.build());

            // Verify the response status
            assertEquals(CREATED, result.status());

            // Verify the response contains task details
            assertTrue(contentAsString(result).contains("Sample Task"));
        });
    }

    @Test
    public void testGetAllTasksAuthorized() {
        Helpers.running(fakeApplication(), () -> {
            // Generate a valid token
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0b2RvbGlzdCIsInVzZXJuYW1lIjoidXNlciJ9.SShl3_tYaOinKsg6kQwsK8ulnsjxAf4ofxLc1BIjAdA";


            // Create a fake request with Authorization header
            Http.RequestBuilder request = fakeRequest("GET", "/tasks").header("Authorization", token);

            // Call the getAllTasks method
            Result result = taskController.getTasks(request.build());

            // Verify the response status
            assertEquals(OK, result.status());

        });
    }

    @Test
    public void testGetTaskByIdNotFound() {
        Helpers.running(fakeApplication(), () -> {
            // Generate a valid token
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0b2RvbGlzdCIsInVzZXJuYW1lIjoidXNlciJ9.SShl3_tYaOinKsg6kQwsK8ulnsjxAf4ofxLc1BIjAdA";

            // Mock the repository to return null for a non-existing task
            when(taskRepository.getTaskById("123")).thenReturn(null);

            // Create a fake request with Authorization header
            Http.RequestBuilder request = fakeRequest("GET", "/tasks/123").header("Authorization", token);

            // Call the getTaskById method
            Result result = taskController.getTaskById("123", request.build());

            // Verify the response status
            assertEquals(NOT_FOUND, result.status());
        });
    }






}
