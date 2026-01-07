# Banking System

A Java-based banking system for secure transaction management and account operations.

## Table of Contents
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Getting Started](#getting-started)
- [Docker Operations](#docker-operations)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Monitoring](#monitoring)

## Technology Stack
- Java 17
- Spring Boot with Jakarta EE
- PostgreSQL
- Docker
- Spring Data JPA
- Lombok

## System Architecture

### Core Components
1. **Transaction Management**
   - Secure money transfers
   - Account balance management
   - Transaction history tracking

2. **Audit System**
   - Operation logging
   - Transaction tracking
   - Performance monitoring

3. **Database Layer**
   - PostgreSQL for data persistence
   - Transaction isolation
   - Data integrity constraints

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 17 JDK (for local development)
- Maven (for local development)

### Environment Setup
Configure in docker-compose.yml:

yaml SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/my_bank SPRING_DATASOURCE_USERNAME=your_username SPRING_DATASOURCE_PASSWORD=your_password

## Docker Operations

### Starting the Application

# Start all services in background

```bash 
 docker-compose up -d app
```

# Force recreate containers

```bash 
 docker-compose up -d --force-recreate
```

### Monitoring

# View running containers

```bash 
 docker ps
```

### Managing Services

# Stop all services

```bash  
  docker-compose down
```

# Stop all and remove volumes
```bash  
  docker-compose down -v
```

# Restart services
```bash  
 docker-compose restart
```

# Stop specific service
```bash 
 docker-compose stop app
 ```
# Start specific service
```bash 
 docker-compose start app
 ```
# Remove unused data
```bash 
 docker system prune
 ```

## API Documentation

### Endpoints
Access Swagger UI: `http://localhost:8081/swagger-ui.html`

Main endpoints:
- Account Management: `/api/accounts`
   - Create account
   - Get account details
   - Update account status
- Transactions: `/api/accounts/transfer`
   - Transfer funds
   - Deposit
   - Withdrawal

## Development

### Local Setup
1. Clone repository:

```bash 
  git clone <repository-url> 
```
2. Build project:

```bash 
  ./mvnw clean install
```

3. Run locally:
```bash 
  ./mvnw spring-boot:run 
```

4. Run tests
```bash 
  ./mvnw test
```

### Database
- Automatic schema creation
- Flyway migrations
- Initial data seeding

## Monitoring

### Health Checks
- Endpoint: `/actuator/health`
- Monitors:
   - Application status
   - Database connectivity
   - System resources

### Logs
Access application logs:
```bash 
  docker-compose logs -f app
```
    
Access database logs:
```bash
  docker-compose logs -f db
```
### Error Handling
- Comprehensive error logging
- Transaction rollback on failures
- Detailed error responses
- Audit trail for all operations

For additional support or questions, contact the development team.

This single README file provides a comprehensive overview of the system, from setup to maintenance, organized in a logical flow. You can copy this entire block and use it as your README.md file. It covers all essential aspects while maintaining clarity and usability.
