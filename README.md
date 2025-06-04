# Coffee Service Management System

A real-time coffee service management system built with Spring Boot, Thymeleaf, and Spring Security.

## Features

- **Authentication and Role-based Access Control**
  - Login with Spring Security
  - Role-based page access (Admin, Barman, Server, Cashier)

- **Product Management (Admin)**
  - Add/Edit/Delete products (name, category, price, ingredients)
  - Product listing with filter/search

- **Order Workflow**
  - Create new orders and assign to tables
  - Real-time order queue for baristas
  - Status updates (Pending, In Progress, Ready, Delivered)
  - Notifications for ready orders

- **Table Management**
  - View tables and their status
  - Assign tables to servers

- **Payment Processing (Cashier)**
  - Process payments for completed orders
  - Apply discounts
  - Generate receipts

- **Reporting (Admin)**
  - View best-selling products
  - Sales statistics

## Technology Stack

- Spring Boot 3.2.4
- Spring MVC & Thymeleaf
- Spring Data JPA
- Spring Security
- MySQL/H2 Database
- Bootstrap 5
- Font Awesome
- iText PDF (for receipts)

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- MySQL (or use the embedded H2 database for development)

### Running the Application

1. Clone the repository
2. Configure the database in `application.properties` (MySQL) or use `application-h2.properties` for H2
3. Run the application:
   ```
   mvn spring-boot:run
   ```
   or for H2 database:
   ```
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```

### Default Credentials

The application comes with these default users:

- **Admin**: Username: `admin`, Password: `admin123`
- **Barman**: Username: `barman1`, Password: `admin123`
- **Server**: Username: `server1`, Password: `admin123`
- **Cashier**: Username: `cashier1`, Password: `admin123`

## Project Structure

```
src/main/java/com/coffeeshop/management/
├── controller        # MVC controllers for each role
├── model             # JPA entities
├── repository        # Spring Data JPA repositories
├── service           # Business logic services
├── security          # Spring Security configuration
└── CoffeeShopApplication.java  # Main application class

src/main/resources/
├── templates/        # Thymeleaf templates
│   ├── admin/        # Admin-specific templates
│   ├── barman/       # Barman-specific templates
│   ├── server/       # Server-specific templates
│   ├── cashier/      # Cashier-specific templates
│   ├── auth/         # Authentication templates
│   └── fragments/    # Reusable Thymeleaf fragments
├── static/           # Static resources (CSS, JS)
├── application.properties      # Main configuration
└── application-h2.properties   # H2 database configuration
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.