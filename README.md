
# To-Do List RESTful API with Play Framework and MongoDB

## Description

This project is a RESTful web service for managing tasks (To-Do List), developed using the Play Framework and MongoDB. The service allows users to create, update, retrieve, and delete tasks, with authentication and authorization. Additional functionality includes PDF file uploads, processing, and task export/import.

---

## Features

### Core Features:
1. **Task Management**:
   - Create a new task.
   - Retrieve a list of all tasks.
   - Retrieve a specific task by ID.
   - Update a task.
   - Delete a task.
2. **Authentication and Authorization**:
   - Only authorized users can manage tasks.
   - Token-based authentication using JWT.
3. **Error Handling**:
   - Return proper HTTP status codes and messages for errors.
4. **Testing**:
   - Comprehensive tests to verify API functionality.

### Additional Features:
1. **PDF File Handling**:
   - Attach PDF files to tasks.
   - Convert PDF files to images, supporting multi-page PDFs.
2. **Task Export/Import**:
   - Export tasks as a ZIP archive containing JSON metadata and associated images.
   - Import previously exported tasks.

---

## Task Structure

Each task contains the following attributes:
- **ID**: A unique identifier for the task.
- **Title**: The title of the task.
- **Description**: A detailed description of the task.
- **Creation Date**: Automatically set when the task is created.

---

## Technologies Used

- **Java 17**
- **Play Framework**
- **MongoDB** (as the database)
- **JWT (JSON Web Token)** for authentication
- **Apache PDFBox** for handling PDFs
- **Java ImageIO** for image processing
- **JUnit** for testing

---

## Setup and Running

1. Clone the repository:
   ```bash
   git clone https://github.com/ItsOpeniuk/todo_app
   cd your-repository
   ```

2. Set up MongoDB:
   - Ensure MongoDB is running locally or accessible via the network.
   - Update the connection settings in the `application.conf` file.

3. Install dependencies and run the application:
   ```bash
   sbt run
   ```

4. The API will be available at:
   ```
   http://localhost:9000
   ```

---

## API Endpoints

### Static Resources
- **Map static resources from the /public folder to the /assets URL path**:  
  `GET /assets/*file`  
  `controllers.Assets.versioned(path="/public", file: Asset)`

### Authentication
- **Login**:  
  `POST /login`  
  Controller: `controllers.TaskController.login(request: Request)`

### Task Endpoints
- **Create Task**:  
  `POST /tasks`  
  Controller: `controllers.TaskController.createTask(request: Request)`

- **Get All Tasks**:  
  `GET /tasks`  
  Controller: `controllers.TaskController.getTasks(request: Request)`

- **Get Task by ID**:  
  `GET /tasks/:id`  
  Controller: `controllers.TaskController.getTaskById(id, request: Request)`

- **Update Task**:  
  `PUT /tasks/:id`  
  Controller: `controllers.TaskController.updateTask(id, request: Request)`

- **Delete Task**:  
  `DELETE /tasks/:id`  
  Controller: `controllers.TaskController.deleteTask(id, request: Request)`

- **Upload PDF for Task**:  
  `POST /tasks/:taskId/uploadPdf`  
  Controller: `controllers.TaskController.uploadPdf(taskId: String, request: Request)`

- **Export Task**:  
  `GET /tasks/:taskId/export`  
  Controller: `controllers.TaskController.exportTask(taskId: String, request: Request)`

- **Delete Image from Task**:  
  `DELETE /tasks/:taskId/images/:imageName`  
  Controller: `controllers.TaskController.deleteImage(taskId, imageName, request: Request)`

- **Update Image for Task**:  
  `POST /tasks/:taskId/images/:imageName`  
  Controller: `controllers.TaskController.updateImage(taskId, imageName, request: Request)`

---

## Testing

1. Run tests:
   ```bash
   sbt test
   ```

2. Ensure all tests pass successfully.

---

## Contact

For any questions or suggestions, feel free to contact me:  
**Email**: your-email@example.com  
**GitHub**: [ItsOpeniuk](https://github.com/ItsOpeniuk)
