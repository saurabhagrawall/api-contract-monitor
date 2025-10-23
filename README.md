# API Contract Monitor

AI-powered microservices dependency analyzer and breaking change detector for maintaining API compatibility across distributed systems.

## 🎯 Project Overview

This project demonstrates a complete microservices architecture with an intelligent monitoring system that analyzes API contracts, detects breaking changes, and predicts impact across services using AI.

**Built as a portfolio project for FAANG+ internship applications.**

## 🏗️ Architecture

The system consists of two main components:

### 1. Microservices (Demo Application)
Four independent Spring Boot microservices that simulate a real-world e-commerce system:

- **User Service** (Port 8081) - User management and authentication
- **Order Service** (Port 8082) - Order processing and management
- **Product Service** (Port 8083) - Product catalog and inventory
- **Notification Service** (Port 8084) - Notification delivery system

### 2. Contract Monitor Tool (Coming Soon)
Intelligent API monitoring system that:
- Analyzes OpenAPI/Swagger specifications
- Detects breaking changes in API contracts
- Maps service dependencies
- Predicts impact radius of changes
- Provides AI-powered suggestions for backward compatibility

## 🛠️ Tech Stack

### Backend
- **Java 17** - Modern LTS version
- **Spring Boot 3.5.6** - Application framework
- **Spring Data JPA** - Database abstraction
- **PostgreSQL 15** - Relational database
- **Hibernate** - ORM
- **Lombok** - Boilerplate reduction
- **Bean Validation** - Input validation

### API & Documentation
- **Spring Web** - REST API framework
- **SpringDoc OpenAPI 3** - API documentation
- **Swagger UI** - Interactive API explorer

### DevOps & Tools
- **Docker & Docker Compose** - Containerization
- **Maven** - Build automation
- **Git & GitHub** - Version control

### Planned Additions
- **OpenAI API** - AI-powered change analysis
- **React** - Frontend dashboard
- **Neo4j** (Optional) - Graph database for dependency mapping

## 📁 Project Structure
```
api-contract-monitor/
├── services/
│   ├── user-service/          # User management microservice
│   ├── order-service/         # Order processing microservice
│   ├── product-service/       # Product catalog microservice
│   └── notification-service/  # Notification system microservice
├── contract-monitor/          # Contract monitoring tool (coming soon)
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker Desktop
- Git

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/saurabhagrawall/api-contract-monitor.git
cd api-contract-monitor
```

2. **Start a microservice (example: User Service)**
```bash
cd services/user-service

# Start PostgreSQL database
docker-compose up -d

# Run the service
mvn spring-boot:run
```

3. **Access Swagger UI**
```
http://localhost:8081/swagger-ui.html
```

### Running All Services

Each service runs on a different port with its own database:

| Service | Port | Database Port | Swagger UI |
|---------|------|---------------|------------|
| User Service | 8081 | 5432 | http://localhost:8081/swagger-ui.html |
| Order Service | 8082 | 5433 | http://localhost:8082/swagger-ui.html |
| Product Service | 8083 | 5434 | http://localhost:8083/swagger-ui.html |
| Notification Service | 8084 | 5435 | http://localhost:8084/swagger-ui.html |

## 📊 API Endpoints

### User Service
- `POST /api/users` - Create user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Order Service
- `POST /api/orders` - Create order
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{userId}` - Get orders by user
- `GET /api/orders/status/{status}` - Get orders by status
- `PUT /api/orders/{id}/status` - Update order status
- `PUT /api/orders/{id}/cancel` - Cancel order
- `DELETE /api/orders/{id}` - Delete order

### Product Service
- `POST /api/products` - Create product
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{category}` - Get by category
- `GET /api/products/search?name=X` - Search products
- `GET /api/products/low-stock` - Get low stock products
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Notification Service
- `POST /api/notifications` - Create notification
- `GET /api/notifications` - Get all notifications
- `GET /api/notifications/{id}` - Get notification by ID
- `GET /api/notifications/user/{userId}` - Get by user
- `GET /api/notifications/status/{status}` - Get by status
- `PUT /api/notifications/{id}/sent` - Mark as sent
- `DELETE /api/notifications/{id}` - Delete notification

## 🎓 Learning Outcomes

This project demonstrates proficiency in:

- **Microservices Architecture** - Service decomposition and inter-service communication
- **RESTful API Design** - Best practices for REST endpoints
- **Database Design** - Entity modeling and relationships
- **Transaction Management** - ACID properties and consistency
- **Containerization** - Docker for development and deployment
- **API Documentation** - OpenAPI/Swagger specifications
- **Git Workflow** - Feature branches, PRs, code reviews
- **Clean Code** - SOLID principles, separation of concerns

## 🔮 Roadmap

- [x] User Service implementation
- [x] Order Service implementation
- [x] Product Service implementation
- [x] Notification Service implementation
- [ ] Contract Monitor core engine
- [ ] OpenAPI spec parsing and comparison
- [ ] Breaking change detection algorithms
- [ ] Service dependency graph
- [ ] AI-powered impact analysis
- [ ] React dashboard for visualization
- [ ] Integration tests
- [ ] CI/CD pipeline

## 👤 Author

**Saurabh Agrawal**
- GitHub: [@saurabhagrawall](https://github.com/saurabhagrawall)
- University: UMASS Amherst (MSCS)
- LinkedIn: linkedinhttps://www.linkedin.com/in/saurabh-agrawal-0839ab206/
- University Email: saurabhagraw@umass.edu
- Personal Email: agrawalsaurabh2000@gmail.com

## 📝 License

This project is open source and available for educational purposes.

## 🙏 Acknowledgments

This project was born from real-world experience working at Oracle and Bajaj, where I observed how developers—both new and experienced—struggle to understand the ripple effects of API changes across microservices. A single field removal or type change can break dependent services, leading to production incidents and regression bugs.

The goal of this project is to build an intelligent system that:
- Makes the impact of changes visible before deployment
- Reduces regression bugs caused by API incompatibilities
- Helps teams move faster with confidence
- Bridges the gap between local development and distributed system complexity

This is a problem I'm genuinely passionate about solving, and this project represents my approach to addressing it through automation and AI.

---

**⭐ If you find this project interesting, please consider giving it a star!**