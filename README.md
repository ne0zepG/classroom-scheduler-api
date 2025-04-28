# 📌 Classroom Scheduling System API

A robust Spring Boot application for efficiently managing classroom schedules in
educational institutions.

## ✨ Key Features

- **Classroom Management**

  - View room availability through statistics dashboard with building, room, and date filters.
  - Browse rooms by building and view room details including capacity and available equipment.
  - Add, update, and remove classroom resources.
  - Manage room details including capacity and available equipment.
  - Hierarchical structure (Departments → Programs → Courses).

- **Scheduling System**

  - Create single or recurring classroom schedules with customizable patterns across multiple weeks.
  - View upcoming and past reservations.
  - Automated conflict detection to prevent double schedules.
  - Schedule approval workflow (Pending → Approved/Rejected).

- **User Management**

  - Role-specific access (Admin, Faculty, Student).
  - Schedule history with audit information.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.4.5
- **Database**: MySQL (with H2 for development)
- **ORM**: Spring Data JPA
- **Security**: Spring Security
- **Build** Tool: Maven
- **Java Version**: 17

## 🚀 Getting Started

Prerequisites:

- Java 17
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

The application initializes with sample data for testing purposes.
