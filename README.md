# Jira-like — JIRA-like Project Management

A full-stack project management application with a Kanban board and ticketing system.

## Stack

| Layer    | Technology                                |
| -------- | ----------------------------------------- |
| Backend  | Spring Boot 3.2, Spring Data JPA, Java 17 |
| Database | PostgreSQL                                |
| Build    | Gradle 8.6                                |
| Frontend | React 18, dnd-kit (drag-and-drop)         |

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL running locally

## Database Setup

```sql
CREATE USER jira_like_app WITH PASSWORD 'password1';
CREATE DATABASE jira_like OWNER jira_like_app;
GRANT ALL PRIVILEGES ON DATABASE jira_like TO jira_like_app;
```

## Backend (`jira-like-backend/`)

```bash
cd jira-like-backend
./gradlew bootRun          # Linux/macOS
gradlew.bat bootRun        # Windows
```

Runs on **http://localhost:8080**

### API Endpoints

| Method | Path                       | Description                    |
| ------ | -------------------------- | ------------------------------ |
| GET    | /api/projects              | List all projects              |
| POST   | /api/projects              | Create project                 |
| GET    | /api/projects/{id}         | Get project                    |
| PUT    | /api/projects/{id}         | Update project                 |
| DELETE | /api/projects/{id}         | Delete project                 |
| GET    | /api/projects/{id}/columns | Get board columns with tickets |
| POST   | /api/columns               | Create column                  |
| PUT    | /api/columns/{id}          | Update column                  |
| DELETE | /api/columns/{id}          | Delete column                  |
| GET    | /api/projects/{id}/tickets | Get all tickets in project     |
| GET    | /api/tickets/{id}          | Get ticket                     |
| POST   | /api/tickets               | Create ticket                  |
| PUT    | /api/tickets/{id}          | Update ticket                  |
| PATCH  | /api/tickets/{id}/move     | Move ticket to column/position |
| DELETE | /api/tickets/{id}          | Delete ticket                  |
| GET    | /api/tickets/{id}/comments | Get comments                   |
| POST   | /api/tickets/{id}/comments | Add comment                    |
| PUT    | /api/comments/{id}         | Update comment                 |
| DELETE | /api/comments/{id}         | Delete comment                 |
| GET    | /api/users                 | List users                     |
| POST   | /api/users                 | Create user                    |
| PUT    | /api/users/{id}            | Update user                    |
| DELETE | /api/users/{id}            | Delete user                    |

## Frontend (`jira-like-frontend/`)

```bash
cd jira-like-frontend
npm install
npm start
```

Runs on **http://localhost:3000** (proxies API calls to :8080)

## Features

- **Kanban Board** — Drag and drop tickets between columns
- **Ticket Management** — Create, edit, delete tickets with full details
- **Ticket Types** — Story, Bug, Task, Epic, Subtask
- **Priority Levels** — Low, Medium, High, Critical
- **Assignee/Reporter** — Assign users to tickets
- **Story Points & Due Dates** — Agile estimation support
- **Comments** — Threaded comments on tickets
- **Project Columns** — Customizable board columns (default: To Do → In Progress → In Review → Done)
- **Multiple Projects** — Manage multiple projects from a dashboard
