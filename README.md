# Employee & Department Management System

## 📌 Overview

This project is a **Spring Boot-based RESTful API** for managing employees and departments within an organization. It provides endpoints to perform CRUD operations, manage relationships, and handle bulk operations efficiently.

The system is designed with clean architecture principles, focusing on scalability, maintainability, and performance.

---

## 🚀 Features

### 👨‍💼 Employee Management

- Create, update, delete, and retrieve employees  
- Assign employees to departments  
- Bulk employee creation  
- Fetch employees by department  

### 🏢 Department Management

- Create and manage departments  
- Retrieve department details  
- List employees under a department  

---

## 🛠️ Tech Stack

- **Java 21+**  
- **Spring Boot**  
- **Spring Data JPA**  
- **REST APIs**  
- **Maven**  
- **H2**  
- **Liquibase**  

---

## 📂 Project Structure

```
src/
 ├── controller        # REST Controllers
 ├── service           # Business logic
 ├── repository        # Data access layer
 ├── entity            # JPA entities
 ├── dto               # Data Transfer Objects
 ├── mapper            # Entity-DTO mapping
 └── exception         # Global exception handling
```

---

## ⚙️ How to Run the Application

### 1 Clone the Repository

```bash
git clone https://github.com/ahmedmamdouh20/Employee-Department-Management-System.git
cd <Employee-Department-Management-System>
```

### 2 Build the Project

```bash
mvn clean install
```

---

### 3 Run the Application

```bash
mvn spring-boot:run
```

Application will start on:

```
http://localhost:8080
```

### 4 Authorized user to access  
username -> admin  , password-> admin123  

---

## 📡 API Endpoints

### Employee APIs

| Method | Endpoint                        | Description                               |
|--------|--------------------------------|-------------------------------------------|
| POST   | `/employees`                   | Create employee                           |
| POST   | `/employees/bulk`              | Create multiple employees                 |
| GET    | `/employees`                   | Get all employees                         |
| GET    | `/employees/{id}`              | Get employee by ID                        |
| PUT    | `/employees/{id}`              | Update employee                           |
| DELETE | `/employees/{id}`              | Delete employee                           |
| GET    | `/departments/{departmentId}`  | Get all employees retrieved by department |

---

### Department APIs

| Method | Endpoint              | Description               |
|--------|----------------------|---------------------------|
| POST   | `/departments`       | Create department         |
| GET    | `/departments`       | Get all departments       |
| GET    | `/departments/{id}`  | Get department by ID      |
| PUT    | `/departments/{id}`  | Update department         |
| DELETE | `/departments/{id}`  | Delete department         |

---

## 🧠 Key Design Decisions

- **DTO Pattern**: Used to separate internal entity structure from API responses.  
- **Layered Architecture**: Controller → Service → Repository.  
- **Exception Handling**: Centralized using `@ControllerAdvice`.  
- **Bulk Processing**: Implemented for better performance in large datasets.  
- **LiquiBase**: Used it to manage database table creations .  
- **H2**: As database memory that help on small-scale embedded applications.  

### Use of computeIfAbsent:

- Used to efficiently group employees by department  
- Avoids redundant checks and improves readability  

---

## 🔧 Can do on future

- Use JWT token and encryption for all personal data to cover best security  
- Use indexes on DB to faster the query when scale will be high  
- Add caching (Redis)  
- Dockerize the application  

---

## 👤 Author

Ahmed Mamdouh  

---

## 💡 Notes

- Ensure Java and Maven are properly installed  
- You can test APIs using Postman or Swagger (if added)  
- Modify configurations as per your environment  

---

