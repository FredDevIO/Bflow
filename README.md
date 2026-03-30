# 🌊 Bflow - Personal finance & transaction manager

![Bflow](https://img.shields.io/badge/Status-Active-success) ![Docker](https://img.shields.io/badge/Docker-Ready-blue) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-green) ![React](https://img.shields.io/badge/React-18-blue)

---

### 📖 About the project
**Bflow** is a comprehensive full-stack web application designed to help users manage their personal finances. It allows you to track daily transactions, organize expenses into dynamic categories, and maintain a clear view of your cash flow. Built with a robust Java Spring Boot backend and a modern React frontend, Bflow is fully containerized to run seamlessly on any environment (Windows, macOS, Linux, or NAS systems like TerraMaster).

### ✨ Key features
* **Advanced security:** Full authentication and authorization system using JWT (JSON Web Tokens).
* **Transaction management:** Complete CRUD operations for incomes and expenses, keeping your financial history organized.
* **Dynamic categories:** Create and manage custom categories to filter and group your cash flow accurately.
* **Automated admin seeding:** The system automatically initializes a default Administrator account on the first run using environment variables.
* **SPA routing native support:** Custom Nginx configuration ensures React Router works flawlessly, preventing `404 Not Found` errors upon page refresh.
* **Multi-Platform deployment:** 100% Dockerized architecture. It works with a single command without needing Java installed on your host machine.

### 🛠️ Tech stack
* **Backend:** Java 17, Spring Boot, Spring Security (JWT), Maven, Spring Data JPA.
* **Frontend:** React 19, Vite 8, Tailwind CSS 4, React Router 7.
* **UI & Data viz:** Recharts, SweetAlert2.
* **Web server:** Nginx.
* **Database:** MySQL 8.0.
* **Infrastructure:** Docker, Docker Compose.

### 📂 Project structure
```text
Bflow/
├── backend/            # Spring Boot API source code & Dockerfile
├── frontend/           # React App, Nginx config & Dockerfile
├── docker-compose.yml  # Multi-container orchestration
└── README.md
```

### 📥 Installation & deployment (Docker)

You only need **Git** and **Docker** installed on your machine.

**1. Clone the repository:**
```bash
git clone https://github.com/FredDevIO/Bflow.git
cd Bflow
```

**2. Start the application:**
```bash
docker compose up -d --build
```
*(Note: The first build might take a few minutes as Maven downloads all Java dependencies inside the container).*

**3. Access the application:**
* **Frontend UI:** `http://localhost` (or your Server/NAS/VPS IP)
* **Backend API:** `http://localhost:8080/api` (or your Server/NAS/VPS IP)

### 🔑 Default administrator credentials
On the first run, Bflow creates a default admin user. You can log in with:
* **Username:** `admin`
* **Password:** `bflow`

### ⚙️ Configuration (Environment variables)
If you are deploying on a local network (e.g., a NAS), update your `docker-compose.yml`:
* `JWT_SECRET`: Change it to a secure random string for production.
* `ADMIN_NAME` / `ADMIN_EMAIL` / `ADMIN_PASSWORD`: Change the default admin credentials.


---
_Developed by Fred_ 💻
