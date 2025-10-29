# API Contract Monitor

An intelligent system for detecting breaking changes in microservices APIs and understanding their impact across distributed systems.

## 💡 The Problem

During my time at Oracle and Bajaj, I noticed a recurring pattern: developers would make seemingly innocent API changes—removing a field, changing a type, deprecating an endpoint—without fully understanding which other services depended on those contracts. This led to:

- **Production incidents** from unexpected API incompatibilities
- **Regression bugs** that were hard to trace back to API changes
- **Slow deployment cycles** due to fear of breaking things
- **Communication gaps** between teams owning different services

In a microservices architecture with dozens of services, a single field removal can create a ripple effect across multiple teams. The question that drove this project was: **"How can we make API change impacts visible before they reach production?"**

## 🎯 The Solution

This project implements a contract monitoring system that:

1. **Automatically fetches** OpenAPI specifications from all microservices
2. **Compares versions** to detect breaking changes (field removals, type changes, endpoint deprecations)
3. **Stores historical data** to track API evolution over time
4. **Provides REST APIs** to query breaking changes and analysis reports
5. **Uses AI (OpenAI GPT-4o-mini)** to suggest backward-compatible alternatives and predict impact

## 🏗️ Architecture

### Current Implementation
```
┌─────────────────────────────────────────────────────────────┐
│                    Contract Monitor Tool                    │
│                         (Port 8085)                         │
│                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐     │
│  │ Analysis     │   │ Breaking     │   │ API Spec     │     │
│  │ Controller   │   │ Change       │   │ Controller   │     │
│  │              │   │ Controller   │   │              │     │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘     │
│         │                  │                  │             │
│  ┌──────▼──────────────────▼──────────────────▼─────────┐   │
│  │              Service Layer                           │   │
│  │  • OpenApiClient    • ApiSpecService                 │   │
│  │  • AnalysisService  • BreakingChangeService          │   │
│  └──────────────────────────┬───────────────────────────┘   │
│                             │                               │
│  ┌──────────────────────────▼───────────────────────────┐   │
│  │            PostgreSQL Database                       │   │
│  │  • api_specs  • breaking_changes  • analysis_reports │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                             │
                             │ Fetches OpenAPI specs
                             ▼
    ┌────────────────────────────────────────────────────┐
    │              Microservices Ecosystem               │
    │                                                    │
    │  ┌──────────┐   ┌──────────┐   ┌──────────┐        │
    │  │  User    │   │  Order   │   │ Product  │  ...   │
    │  │ Service  │   │ Service  │   │ Service  │        │
    │  │  :8081   │   │  :8082   │   │  :8083   │        │
    │  └──────────┘   └──────────┘   └──────────┘        │
    └────────────────────────────────────────────────────┘
```

### Demo Microservices

Four independent Spring Boot microservices that simulate a real e-commerce system:

- **User Service** (8081) - User management with authentication
- **Order Service** (8082) - Order processing and lifecycle management
- **Product Service** (8083) - Product catalog with inventory tracking
- **Notification Service** (8084) - Multi-channel notification delivery

Each service:
- Exposes REST APIs with full CRUD operations
- Auto-generates OpenAPI/Swagger documentation
- Runs with its own PostgreSQL database
- Follows layered architecture (Controller → Service → Repository → Entity)

## 🛠️ Tech Stack

**Backend Framework:** Spring Boot 3.5.6, Java 17  
**Database:** PostgreSQL 15 with Spring Data JPA  
**API Documentation:** SpringDoc OpenAPI 3, Swagger UI  
**AI Integration:** Spring AI with OpenAI GPT-4o-mini  
**Build & Deploy:** Maven, Docker, Docker Compose  
**Libraries:** Lombok, Jackson (JSON parsing), Bean Validation  

**Planned:** React (Frontend), Neo4j (Dependency graphs)

## 📁 Project Structure
```
api-contract-monitor/
├── services/                      # Demo microservices
│   ├── user-service/             # User management
│   ├── order-service/            # Order processing
│   ├── product-service/          # Product catalog
│   └── notification-service/     # Notifications
│
├── contract-monitor/             # The monitoring tool
│   ├── src/main/java/com/contractmonitor/contractmonitor/
│   │   ├── entity/              # Database entities
│   │   │   ├── ApiSpec.java     # Stores OpenAPI specs
│   │   │   ├── BreakingChange.java  # Records detected changes
│   │   │   └── AnalysisReport.java  # Analysis summaries
│   │   │
│   │   ├── repository/          # Data access layer
│   │   │   ├── ApiSpecRepository.java
│   │   │   ├── BreakingChangeRepository.java
│   │   │   └── AnalysisReportRepository.java
│   │   │
│   │   ├── service/             # Business logic
│   │   │   ├── OpenApiClient.java        # Fetches specs
│   │   │   ├── ApiSpecService.java       # Manages specs
│   │   │   ├── BreakingChangeService.java # Records changes
│   │   │   ├── AnalysisService.java      # Core comparison engine
│   │   │   └── AiService.java            # AI-powered insights
│   │   │
│   │   └── controller/          # REST API endpoints
│   │       ├── AnalysisController.java
│   │       ├── BreakingChangeController.java
│   │       └── ApiSpecController.java
│   │
│   ├── docker-compose.yml       # PostgreSQL for Contract Monitor
│   └── pom.xml
│
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop)
- **Git** - [Download](https://git-scm.com/downloads)
- **OpenAI API Key** - [Get one here](https://platform.openai.com/api-keys) (for AI features)

### Quick Start

**1. Clone the repository**
```bash
git clone https://github.com/saurabhagrawall/api-contract-monitor.git
cd api-contract-monitor
```

**2. Configure OpenAI API Key (Required for AI Features)**
```bash
# Set your OpenAI API key as an environment variable
export OPENAI_API_KEY="sk-proj-your-key-here"

# Make it permanent (add to ~/.zshrc or ~/.bash_profile)
echo 'export OPENAI_API_KEY="sk-proj-your-key-here"' >> ~/.zshrc
source ~/.zshrc
```

**Get your API key**: https://platform.openai.com/api-keys

**Note**: AI features will gracefully degrade if the key is not set (breaking changes still detected, but without AI insights).

**3. Start Contract Monitor**
```bash
cd contract-monitor

# Start PostgreSQL database
docker-compose up -d

# Run the service
mvn spring-boot:run
```

**4. Start a demo microservice (e.g., User Service)**

Open a new terminal:
```bash
cd services/user-service

# Start its database
docker-compose up -d

# Run the service
mvn spring-boot:run
```

**5. Trigger analysis**
```bash
# Analyze User Service
curl -X POST http://localhost:8085/api/analysis/user-service

# View breaking changes
curl http://localhost:8085/api/breaking-changes/user-service
```

**6. Access Swagger UI**
- Contract Monitor: http://localhost:8085/swagger-ui.html
- User Service: http://localhost:8081/swagger-ui.html

### Service Ports

| Service | Application | Database | Swagger UI |
|---------|------------|----------|------------|
| Contract Monitor | 8085 | 5436 | http://localhost:8085/swagger-ui.html |
| User Service | 8081 | 5432 | http://localhost:8081/swagger-ui.html |
| Order Service | 8082 | 5433 | http://localhost:8082/swagger-ui.html |
| Product Service | 8083 | 5434 | http://localhost:8083/swagger-ui.html |
| Notification Service | 8084 | 5435 | http://localhost:8084/swagger-ui.html |

## 📊 API Endpoints

### Contract Monitor APIs

#### Analysis Operations
- `POST /api/analysis/{serviceName}` - Trigger analysis for a service
- `POST /api/analysis/all` - Analyze all services
- `GET /api/analysis/{serviceName}/latest` - Get latest analysis report
- `GET /api/analysis/{serviceName}/history` - Get analysis history
- `GET /api/analysis/status/{serviceName}` - Check if service is available
- `GET /api/analysis/status` - Check all services status

#### Breaking Changes
- `GET /api/breaking-changes/{serviceName}` - Get all breaking changes
- `GET /api/breaking-changes/{serviceName}/type/{type}` - Filter by type
- `GET /api/breaking-changes/{serviceName}/count` - Get count
- `GET /api/breaking-changes/{serviceName}/summary` - Get summary by type
- `GET /api/breaking-changes/{serviceName}/recent?limit=5` - Get recent changes
- `GET /api/breaking-changes/statistics` - System-wide statistics

#### API Spec History
- `GET /api/specs/{serviceName}/latest` - Get latest OpenAPI spec
- `GET /api/specs/{serviceName}/history` - Get all historical specs
- `GET /api/specs/{serviceName}/version/{version}` - Get specific version
- `POST /api/specs/{serviceName}/fetch` - Manually fetch and save spec
- `DELETE /api/specs/{serviceName}/cleanup?keep=10` - Cleanup old specs

### Microservice APIs

<details>
<summary><b>User Service (Port 8081)</b></summary>

- `POST /api/users` - Create user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

</details>

<details>
<summary><b>Order Service (Port 8082)</b></summary>

- `POST /api/orders` - Create order
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{userId}` - Get orders by user
- `GET /api/orders/status/{status}` - Get orders by status
- `PUT /api/orders/{id}/status` - Update order status
- `PUT /api/orders/{id}/cancel` - Cancel order
- `DELETE /api/orders/{id}` - Delete order

</details>

<details>
<summary><b>Product Service (Port 8083)</b></summary>

- `POST /api/products` - Create product
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{category}` - Get by category
- `GET /api/products/search?name=X` - Search products
- `GET /api/products/low-stock` - Get low stock products
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

</details>

<details>
<summary><b>Notification Service (Port 8084)</b></summary>

- `POST /api/notifications` - Create notification
- `GET /api/notifications` - Get all notifications
- `GET /api/notifications/{id}` - Get notification by ID
- `GET /api/notifications/user/{userId}` - Get by user
- `GET /api/notifications/status/{status}` - Get by status
- `PUT /api/notifications/{id}/sent` - Mark as sent
- `DELETE /api/notifications/{id}` - Delete notification

</details>

## 🔍 How It Works

### Breaking Change Detection

The system detects several types of breaking changes:

**1. Endpoint Removed**
```
Old: GET /api/users/{id}
New: [endpoint missing]
Impact: All clients calling this endpoint will get 404 errors
```

**2. HTTP Method Removed**
```
Old: DELETE /api/users/{id}
New: [method missing]
Impact: Clients trying to delete users will fail
```

**3. Field Removed from Response**
```
Old: {"id": 1, "name": "John", "email": "john@test.com", "phone": "123"}
New: {"id": 1, "name": "John", "email": "john@test.com"}
Impact: Clients expecting "phone" field will break
```

**4. Field Type Changed**
```
Old: "age": 25 (integer)
New: "age": "25" (string)
Impact: Type mismatch errors in strongly-typed clients
```

**5. Schema Removed**
```
Old: User schema exists
New: User schema missing
Impact: All endpoints using User model affected
```

## 🤖 AI-Powered Intelligent Analysis

When a breaking change is detected, the system automatically generates three types of AI-powered insights:

### 1. **Smart Migration Suggestions**
AI provides comprehensive, actionable strategies for backward-compatible migration:
- Step-by-step deprecation guides (9-13 steps)
- API versioning strategies (v1 → v2 transition)
- Transition timelines (typically 6-12 months)
- Documentation and communication guidelines

**Example**: For a removed field, AI suggests keeping it as `@Deprecated`, adding a new field alongside it, supporting both during a grace period, and providing client migration guides.

### 2. **Cross-Service Impact Prediction**
AI analyzes your microservices architecture and predicts which services will be affected:
- Confidence scores (30-100%) for each potentially impacted service
- Detailed reasoning for why each service might break
- Prioritized list of services requiring updates

**Example**: Removing a `phone` field predicts 85% confidence that notification-service will break (needs phone for SMS alerts), 75% for order-service (uses phone for delivery notifications).

### 3. **Plain English Explanations**
AI translates technical changes into business-friendly language:
- One-sentence summary for stakeholders
- Impact on API users and clients
- Business consequences and risks

**Example**: *"The user's phone number field has been deleted. Customer support teams won't be able to contact users by phone, and SMS notifications will fail."*

### Technology Stack
- **Spring AI Framework**: Enterprise-grade AI integration layer
- **OpenAI GPT-4o-mini**: Cost-effective, high-quality language model (~$0.001 per analysis)
- **Structured Prompting**: Template-based prompt engineering for consistent, reliable results

### Analysis Workflow
```
1. User triggers analysis: POST /api/analysis/user-service

2. Contract Monitor fetches current OpenAPI spec:
   GET http://localhost:8081/api-docs
   
3. Saves spec to database with timestamp version

4. Retrieves previous spec from database

5. Compares both specs:
   ├─ Compare paths (endpoints)
   │  └─ Compare HTTP methods
   └─ Compare schemas (data models)
      └─ Compare field properties and types

6. Detects breaking changes and saves to database

7. FOR EACH breaking change, AI generates:
   ├─ Backward-compatible migration strategy
   ├─ Impact prediction for all services
   └─ Plain English explanation

8. Generates analysis report with summary

9. Returns results via REST API
```

### Example: Detecting a Breaking Change

**Initial State:**
```bash
# First analysis - creates baseline
curl -X POST http://localhost:8085/api/analysis/user-service
# Response: "Baseline spec saved, no changes to compare yet"
```

**Make a change in User Service:**
```java
// Comment out the phone field in User.java
// private String phone;
```

**Restart User Service and analyze again:**
```bash
curl -X POST http://localhost:8085/api/analysis/user-service
```

**Response:**
```json
{
  "message": "Analysis completed successfully",
  "report": {
    "serviceName": "user-service",
    "breakingChangesCount": 1,
    "summary": "Analysis of user-service: v1 → v2\nBreaking changes: 1\n\n- FIELD_REMOVED at /components/schemas/User: Field 'phone' removed from 'User' schema"
  }
}
```

**Query the breaking change with AI insights:**
```bash
curl http://localhost:8085/api/breaking-changes/user-service
```

**Response:**
```json
[{
  "id": 1,
  "serviceName": "user-service",
  "changeType": "FIELD_REMOVED",
  "path": "/components/schemas/User",
  "description": "Field 'phone' removed from 'User' schema",
  "oldVersion": "2025-10-29T07:18:41",
  "newVersion": "2025-10-29T07:19:12",
  "detectedAt": "2025-10-29T07:19:46",
  "aiSuggestion": "Backward-Compatible Alternative:\n1. Keep the existing 'phone' field but mark it as @Deprecated\n2. Add a new field 'phoneNumber' with the same data\n3. Support both fields for a transition period (6-12 months)\n4. Document the deprecation timeline in API docs\n5. Add migration guide for consumers\n\nThis approach allows:\n- Existing clients continue working without breaking\n- New clients adopt the new field name\n- Gradual migration without coordination overhead",
  "predictedImpact": "Service Name | Confidence | Reason\norder-service | 75% | Uses phone for order confirmations\nnotification-service | 85% | Needs phone for SMS alerts\nprofile-service | 60% | Displays phone in user profiles",
  "plainEnglishExplanation": "The user's phone number field has been deleted. Any application that displays or uses phone numbers will stop working. Customer support teams won't be able to contact users by phone, and SMS notifications will fail."
}]
```

## 🎓 Key Learnings & Design Decisions

### Architecture Patterns

**Layered Architecture:**
- **Controller** - HTTP endpoints, request/response handling
- **Service** - Business logic, orchestration
- **Repository** - Data access abstraction
- **Entity** - Domain models

**Benefits:** Clear separation of concerns, easy testing, maintainable code

**Dependency Injection:**
- Constructor-based injection with `@RequiredArgsConstructor`
- Spring manages bean lifecycle
- Easy to mock for unit testing

**Transaction Management:**
- `@Transactional` ensures ACID properties
- Automatic rollback on exceptions
- Data consistency guaranteed

### Technical Decisions

**Why Jackson for JSON parsing?**
- Navigate OpenAPI specs as tree structures (`JsonNode`)
- No need for POJOs for every spec variation
- Flexible comparison without tight coupling to spec versions

**Why timestamp-based versioning?**
- Simple, automatic, always unique
- Chronological ordering built-in
- Can upgrade to semantic versioning later based on change types

**Why PostgreSQL over MongoDB?**
- Structured data with clear relationships
- ACID compliance for consistency
- Complex queries (GROUP BY, aggregations) work efficiently
- Familiar SQL for most developers

**Why separate databases per service?**
- True microservice independence
- Can scale/migrate services individually
- Reflects production scenarios
- Prevents tight coupling through shared databases

**Why OpenAI GPT-4o-mini?**
- Cost-effective (~$0.15 per 1M input tokens, ~$0.60 per 1M output tokens)
- High-quality responses comparable to GPT-4
- Fast response times suitable for real-time analysis
- Balances performance and cost for production use

## 🔮 Roadmap

### ✅ Completed

- [x] **Complete Microservices Stack**
  - User Service with authentication endpoints
  - Order Service with order lifecycle management
  - Product Service with inventory tracking
  - Notification Service for alerts
  
- [x] **Contract Monitor Core**
  - OpenAPI spec fetching from microservices
  - Automatic breaking change detection engine (5 types)
  - Historical spec storage and versioning
  - Complete REST API for queries
  - Database persistence with PostgreSQL
  
- [x] **AI-Powered Analysis** ⭐
  - OpenAI GPT-4o-mini integration via Spring AI
  - Automatic migration strategy generation (9-13 step guides)
  - Cross-service impact prediction with confidence scores (30-100%)
  - Plain English explanations for non-technical stakeholders
  - Comprehensive error handling with graceful degradation
  
- [x] **Breaking Change Types**
  - Endpoint removals
  - HTTP method removals
  - Schema removals
  - Field removals
  - Type changes
  
- [x] **Professional Development**
  - Layered architecture with clear separation
  - Feature branch workflow with PRs
  - Comprehensive commit history
  - Production-ready error handling
  - Type-safe enum refactoring

### 🚀 Phase 3: React Dashboard (Next)

- [ ] **Visual Dependency Graph**
  - Interactive D3.js/React Flow visualization
  - Shows which services call which endpoints
  - Highlight affected services when change detected
  
- [ ] **Real-time Monitoring**
  - WebSocket-based live updates
  - Push notifications when breaking changes detected
  - Dashboard updates without refresh
  
- [ ] **Historical Trends**
  - Line charts showing API stability over time
  - Breaking changes per week/month
  - Most frequently changed endpoints
  
- [ ] **Comparative Analysis**
  - Side-by-side spec comparison with diff highlighting
  - Visual indicators for added/removed/changed fields
  - Timeline slider to see API evolution
  
- [ ] **Alert Configuration**
  - Configure notification rules (Slack, email, Teams)
  - Custom severity thresholds
  - Team-specific alert routing

- [ ] **AI Insights Visualization**
  - Display AI-generated suggestions in cards
  - Impact prediction heatmap
  - Service dependency graph with confidence scores

### 🔧 Phase 4: Advanced Features

- [ ] **CI/CD Integration**
  - GitHub Actions workflow
  - Automatic analysis on every PR
  - Block merges if breaking changes detected (configurable)
  - Comment on PRs with impact analysis
  
- [ ] **Webhook Support**
  - Services push notifications when they deploy
  - Automatic analysis triggered on deployment
  - No manual intervention needed
  
- [ ] **Non-Breaking Change Detection**
  - Track new endpoints (features added)
  - Track new optional fields
  - Track deprecation warnings
  - Semantic versioning suggestions (major/minor/patch)
  
- [ ] **Custom Rules Engine**
  - Define team-specific breaking change policies
  - Example: "Required field additions are allowed if default provided"
  - Configurable severity levels
  
- [ ] **Multi-environment Support**
  - Compare dev vs staging vs production APIs
  - Detect configuration drift
  - Environment-specific analysis
  
- [ ] **Performance Impact Analysis**
  - Estimate query performance implications
  - Flag changes that might cause N+1 queries
  - Suggest indexing strategies

### 🏢 Phase 5: Enterprise Features

- [ ] **Multi-tenancy**
  - Support multiple teams/organizations
  - Isolated data per tenant
  - Tenant-specific configurations
  
- [ ] **Role-Based Access Control (RBAC)**
  - Admin, Developer, Viewer roles
  - Service-level permissions
  - Audit who triggered analysis and when
  
- [ ] **Complete Audit Logs**
  - Track all API changes with author attribution
  - Integration with Git commits
  - Compliance reporting
  
- [ ] **Team Communication Integration**
  - Slack: Channel-specific notifications
  - Microsoft Teams: Adaptive cards with action buttons
  - PagerDuty: Alert on critical breaking changes
  
- [ ] **API Compatibility Score**
  - Quantitative measure of API stability (0-100)
  - Trend over time
  - Benchmarking across services
  
- [ ] **Automated Test Generation**
  - Generate integration tests for breaking changes
  - Contract tests between services
  - Regression test suggestions

## 🎯 Real-World Use Cases

**Scenario 1: Pre-Deployment Safety Check**
```
Developer modifies User API → CI/CD runs analysis → 
Breaking change detected → AI suggests backward-compatible alternative →
Developer implements suggestion → CI passes → Safe to deploy
```

**Scenario 2: Onboarding New Developers**
```
New dev: "What's the Order Service API?"
Contract Monitor: Shows latest spec + change history + AI explanations
New dev: "What changed recently?"
Contract Monitor: "3 new fields added, 1 deprecated" + plain English impact
```

**Scenario 3: Cross-Team Communication**
```
Team A changes User API → 
Contract Monitor detects dependent services (Order, Product) →
AI predicts 85% confidence notification-service will break →
Notifies Team B and Team C via Slack with migration guide →
Teams coordinate deployment with AI-generated strategy
```

**Scenario 4: Production Incident Investigation**
```
Order Service starts failing →
Contract Monitor shows User Service removed 'email' field 2 hours ago →
AI explains: "Applications expecting email field will break" →
Root cause identified in minutes instead of hours
```

## 🏆 What This Project Demonstrates

**System Design:**
- Microservices architecture with service independence
- Event-driven thinking (analyzing on changes)
- Scalability considerations (separate databases, stateless services)
- AI integration patterns with enterprise frameworks

**Backend Development:**
- RESTful API design following best practices
- Database modeling and relationships
- Transaction management and data consistency
- JSON parsing and tree navigation
- Error handling and logging
- AI/LLM integration with structured prompting

**Software Engineering:**
- Clean code principles (SOLID, DRY)
- Layered architecture
- Dependency injection
- Design patterns (Repository, Service, Controller)
- Type-safe enums and refactoring

**DevOps:**
- Containerization with Docker
- Multi-container orchestration with Docker Compose
- Port management and service discovery
- Local development environment setup
- Environment variable management for secrets

**Problem Solving:**
- Identifying real-world pain points
- Designing automated solutions
- Thinking about edge cases (service down, malformed specs, AI failures)
- Iterative development approach
- Graceful degradation strategies

**AI/ML Engineering:**
- Prompt engineering for consistent outputs
- Context management for LLM queries
- Error handling for AI service failures
- Cost optimization (choosing appropriate models)
- Structured output parsing from AI responses

## 👤 Author

**Saurabh Agrawal**

🎓 **Education:** MS in Computer Science, UMass Amherst  
💼 **Experience:** Oracle (Software Engineer), Bajaj Finserv (Software Engineer II)  
🔗 **LinkedIn:** [linkedin.com/in/saurabh-agrawal-0839ab206](https://www.linkedin.com/in/saurabh-agrawal-0839ab206/)  
📧 **Email:** saurabhagraw@umass.edu | agrawalsaurabh2000@gmail.com  
🐙 **GitHub:** [@saurabhagrawall](https://github.com/saurabhagrawall)

## 📝 License

This project is open source and available for educational and portfolio purposes.

## 🙏 Why This Project Exists

This isn't just another CRUD application. This project was born from genuine frustration with a real problem I experienced at Oracle and Bajaj.

**The Problem I Observed:**

Working on microservices architectures, I saw talented engineers, both junior and senior, struggle with a fundamental question: *"If I change this API, what will break?"*

The typical workflow was:
1. Make an API change (seems innocent)
2. Deploy to dev/staging
3. **Something breaks** in a different service
4. Spend hours debugging
5. Realize the breaking change
6. Rollback or hotfix
7. Coordinate with other teams

This cycle wastes time, creates stress, and slows down velocity. Teams become afraid to make changes, leading to technical debt accumulation.

**What I'm Building:**

A system that makes the invisible visible. Before deploying, developers should **know**:
- What's changing in their API
- What the impact radius is
- Which services will break
- How to fix it proactively

This project is my attempt to solve this problem using automation and intelligence. It's a problem I'm genuinely passionate about, and I believe it can save teams countless hours and reduce production incidents.

**The Vision:**

In the future, when a developer changes an API:
1. Contract Monitor analyzes the change
2. AI suggests backward-compatible alternatives ✅ **NOW COMPLETE**
3. Shows exactly which services will break and why ✅ **NOW COMPLETE**
4. Generates migration guides for affected teams ✅ **NOW COMPLETE**
5. Confidence replaces fear

This is what I'm building toward. **Phase 2 (AI Integration) is now complete.**

---

## 📈 Development Approach

This project follows professional software development practices:

**Git Workflow:**
- Feature-based branching (one feature per branch)
- Descriptive commit messages following conventional commits
- Pull request workflow with detailed descriptions
- Code review process (even for personal projects)
- `develop` branch for integration, `main` for releases

**Incremental Development:**
- Started with one microservice, validated the approach
- Added more services incrementally
- Built Contract Monitor in phases (entities → repositories → services → controllers)
- Integrated AI capabilities in dedicated feature branch
- Each commit represents a logical unit of work

**Documentation:**
- Inline code comments explaining complex logic
- Comprehensive README with real-world context
- API documentation via Swagger
- Commit messages as development diary
- AI prompt engineering documentation

**Testing Philosophy:**
- Manual testing during development
- Real breaking change detection verified
- AI features tested with multiple change types
- Future: Automated tests for regression prevention

---

**⭐ If you find this project interesting or useful, please consider giving it a star! It helps others discover it and shows recruiters that the work resonates with the community.**

---

## 🤝 Contributing

While this is primarily a personal portfolio project, I'm open to discussions and suggestions. Feel free to:
- Open issues for bugs or feature ideas
- Share how you might solve similar problems
- Suggest improvements to the architecture or AI prompts

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Microservices Patterns](https://microservices.io/patterns/index.html)
- [Docker Documentation](https://docs.docker.com/)
- [OpenAI API Documentation](https://platform.openai.com/docs)

---

*This project represents my approach to solving real-world problems through thoughtful engineering. It's not just about writing code, it's about understanding pain points and building solutions that matter.*