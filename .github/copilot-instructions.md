<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# Buckwheat Java Money Tracker - Copilot Instructions

This is a Java-based money tracking and budgeting application inspired by the original Buckwheat app. The project uses JavaFX for the UI, SQLite for data storage, and Maven for build management.

## Project Structure and Guidelines

### Architecture
- **Model-View-Controller (MVC)** pattern
- **Service Layer** for business logic
- **Repository Pattern** for data access
- **JavaFX** for modern UI components

### Key Technologies
- **Java 17+** with modern language features
- **JavaFX 19** for UI framework
- **SQLite** for local database storage
- **Maven** for dependency management and build

### Code Style and Conventions
- Follow standard Java naming conventions (camelCase for variables/methods, PascalCase for classes)
- Use meaningful, descriptive names for variables, methods, and classes
- Add comprehensive JavaDoc comments for public methods and classes
- Keep methods focused and classes cohesive (Single Responsibility Principle)
- Use BigDecimal for all monetary calculations to avoid floating-point precision issues
- Use LocalDate/LocalDateTime for date handling instead of legacy Date class
- Prefer composition over inheritance
- Use try-with-resources for database connections and file operations

### Database Guidelines
- All monetary values should use BigDecimal and be stored as DECIMAL(10,2) in database
- Use proper foreign key constraints to maintain data integrity
- Always close database resources properly using try-with-resources
- Use prepared statements to prevent SQL injection
- Handle SQLException appropriately with proper error messages

### UI/UX Guidelines
- Follow the existing card-based design pattern
- Use the established color scheme (primary: #667eea, secondary: #2ecc71)
- Ensure responsive layout that works with different window sizes
- Use proper FXML binding and separation of concerns
- Add appropriate user feedback (loading states, error messages, success notifications)
- Implement proper form validation with user-friendly error messages

### Features to Implement
When adding new features, consider these core functionalities:
- **Budget Management**: Period-based budgets with smart daily recalculation
- **Expense Tracking**: Quick expense entry with category support
- **Analytics**: Charts and insights similar to the original Buckwheat app
- **Data Management**: Import/export, backup/restore capabilities

### Testing Guidelines
- Write unit tests for service layer methods
- Test edge cases, especially for monetary calculations
- Mock database dependencies in tests
- Test UI components with appropriate JavaFX testing frameworks

### Performance Considerations
- Use Observable collections for UI data binding
- Implement pagination for large transaction lists
- Cache frequently accessed data appropriately
- Optimize database queries with proper indexing

### Error Handling
- Use specific exception types for different error conditions
- Provide meaningful error messages to users
- Log errors appropriately for debugging
- Handle database connection failures gracefully
- Validate user input before processing

### Security Considerations
- Validate all user inputs
- Use prepared statements for database queries
- Handle sensitive data appropriately
- Implement proper backup/restore security measures

When generating code, please:
1. Follow the existing project structure and patterns
2. Use the established naming conventions and code style
3. Add appropriate error handling and validation
4. Include JavaDoc comments for public methods
5. Use BigDecimal for monetary calculations
6. Follow the MVC pattern with proper separation of concerns
7. Ensure database resources are properly managed
8. Consider the user experience and provide appropriate feedback
