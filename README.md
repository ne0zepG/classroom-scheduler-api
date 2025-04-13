# 📌 Classroom Scheduling System API

A robust Spring Boot application for managing classroom scheduling in an
educational environment. The system allows for room management, scheduling, and
user-based access control.

## 📋 Features

- **Room Management**
    - View available rooms.
    - Filter rooms by capacity, equipment, and availability.
    - Add, update, and remove classroom resources.
- **Scheduling System**

    - Create, update, and cancel room schedules.
    - View upcoming and past reservations.
    - Check room availability by date and time slots.
    - Automated conflict detection to prevent double schedules.

- **User Management**
    - Role-based access control (Admin, Faculty, Student).
    - User authentication and authorization.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.4.4
- **Database**: MySQL (with H2 for development)
- **ORM**: Spring Data JPA
- **Security**: Spring Security
- **Build** Tool: Maven
- **Java Version**: 21

## 🚀 Getting Started

Prerequisites:

- Java 21 or higher
- Maven
- MySQL Server

Configuration:
Database connection settings can be modified in `application.properties`

Installation:

1. Clone the repository

```bash
git clone https://github.com/yourusername/classroom-scheduler-api.git
```

2. Navigate to the project directory
3. Build the project using Maven:

```bash
./mvnw clean install
```

4. Run the application:

```bash
./mvnw spring-boot:run
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Clone the repository and make sure to check out from `dev` branch to add
   or update features.
2. Use the naming convention `feature_logic` for your branch name.

```bash
git clone https://github.com/yourusername/classroom-scheduler-api.git
git checkout dev
git checkout -b feature_logic
```

3. Make your changes and commit them.

```bash
git add .
git commit -m "Clear description of changes"
```

4. Create a pull request from your feature_branch to dev branch.

    - Provide a clear description of the changes.
    - Reference any related issues.
    - Request review from maintainers.
    - Address any feedback from code reviews.

## 🔒 Security

The current configuration allows all requests without authentication for
development purposes. In a production environment, you should enable proper
authentication and authorization.

## 📊 Sample Data

The application initializes with sample data including:

- Admin and faculty users
- Several classrooms with different capacities and equipment
- Sample schedules for demonstration purposes

Default credentials:

- Admin: admin@college.edu / admin123
- Faculty: faculty@college.edu / faculty123
