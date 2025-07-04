# Buckwheat Java - Money Tracker

A comprehensive money tracking and budgeting application inspired by the original Buckwheat app, built with Java and JavaFX.

## Features

### 💰 Budget Management
- **Period-based Budgeting**: Set budgets for specific time periods with start and end dates
- **Daily Budget Calculation**: Automatic calculation of daily spending allowance
- **Budget Redistribution**: Smart recalculation of daily budgets based on spending patterns
- **Multiple Budget Support**: Track different budget periods and view historical data

### 📊 Expense Tracking
- **Easy Expense Entry**: Quick and intuitive expense recording
- **Category Organization**: Organize expenses with customizable categories
- **Transaction History**: Complete history of all transactions with search and filtering
- **Real-time Updates**: Instant updates to budget calculations and progress

### 📈 Analytics & Insights
- **Spending Analytics**: Visual charts and graphs showing spending patterns
- **Category Breakdown**: Pie charts showing spending distribution by category
- **Daily/Weekly/Monthly Views**: Multiple time period analysis
- **Budget Progress**: Real-time progress tracking with visual indicators
- **Spending Trends**: Historical data analysis and trend identification

### 🎨 Modern User Interface
- **JavaFX-based UI**: Modern, responsive interface built with JavaFX
- **Card-based Design**: Clean, organized layout with card-based components
- **Color-coded Progress**: Visual indicators for budget health
- **Responsive Layout**: Adapts to different window sizes

### 💾 Data Management
- **SQLite Database**: Local storage with reliable SQLite database
- **Data Persistence**: All data stored locally and securely
- **Backup Ready**: Easy to backup and restore data files
- **Default Categories**: Pre-configured expense categories to get started quickly

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Installation & Running

1. **Clone or download the project**
   ```bash
   git clone <repository-url>
   cd buckwheat-java
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

   Or alternatively:
   ```bash
   mvn clean javafx:run
   ```

### Building an Executable JAR

```bash
mvn clean package
java -cp target/buckwheat-java-1.0.0-shaded.jar com.moneytracker.BuckwheatApp
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/moneytracker/
│   │       ├── BuckwheatApp.java           # Main application class
│   │       ├── controller/                 # UI Controllers
│   │       │   └── MainController.java
│   │       ├── model/                      # Data Models
│   │       │   ├── Budget.java
│   │       │   ├── Transaction.java
│   │       │   └── Category.java
│   │       ├── service/                    # Business Logic
│   │       │   ├── BudgetService.java
│   │       │   ├── TransactionService.java
│   │       │   └── CategoryService.java
│   │       └── database/                   # Data Access
│   │           └── DatabaseManager.java
│   └── resources/
│       ├── fxml/                          # FXML UI Files
│       │   └── main.fxml
│       ├── css/                           # Stylesheets
│       │   └── application.css
│       └── images/                        # Application Images
└── test/                                  # Unit Tests (Future)
```

## Core Components

### Models
- **Budget**: Represents a budget period with total amount, spent amount, and date range
- **Transaction**: Represents individual transactions (expenses, income, budget changes)
- **Category**: Represents expense categories for organization

### Services
- **BudgetService**: Handles budget creation, updates, and calculations
- **TransactionService**: Manages all transaction operations and analytics
- **CategoryService**: Manages expense categories
- **DatabaseManager**: Handles SQLite database operations

### Features Implementation

#### Budget Management
Similar to the original Buckwheat app's budget system:
- Set total budget amount and period
- Automatic daily budget calculation
- Smart budget redistribution when spending changes
- Progress tracking and visual indicators

#### Daily Budget Calculation
The app implements intelligent daily budget calculation:
- Initial daily budget = Total budget ÷ Total days
- Recalculation when needed = Remaining budget ÷ Remaining days
- Considers actual spending patterns and remaining time

#### Analytics Dashboard
Comprehensive analytics similar to Buckwheat:
- Budget progress with percentage and visual bars
- Spending by category with pie charts
- Daily, weekly, monthly spending analysis
- Biggest expenses and spending trends

## Technology Stack

- **Java 17**: Core programming language
- **JavaFX 19**: Modern UI framework
- **SQLite**: Local database storage
- **Maven**: Build and dependency management
- **Jackson**: JSON processing (for future features)

## Key Features Inspired by Buckwheat

1. **Smart Budget Distribution**: Like Buckwheat's intelligent daily budget recalculation
2. **Period-based Budgeting**: Set budgets for trips, months, or custom periods
3. **Visual Progress Tracking**: Color-coded progress bars and percentage indicators
4. **Category-based Organization**: Organize and analyze spending by categories
5. **Real-time Updates**: Immediate feedback on spending and budget status

## Database Schema

### Tables
- **budgets**: Budget periods with amounts and dates
- **transactions**: All financial transactions with types and categories
- **categories**: Expense categories with colors and descriptions

### Relationships
- Transactions belong to budgets and categories
- Categories are reusable across different budgets
- Foreign key constraints ensure data integrity

## Future Enhancements

- [ ] Import/Export functionality
- [ ] Multiple currency support
- [ ] Advanced reporting and charts
- [ ] Recurring transaction support
- [ ] Budget templates
- [ ] Dark mode theme
- [ ] Mobile companion app
- [ ] Cloud sync capabilities

## Development

### Adding New Features
1. Create/modify model classes in `model/` package
2. Add business logic in appropriate service classes
3. Update UI controllers and FXML files
4. Add database schema changes in `DatabaseManager`
5. Update tests and documentation

### Testing
```bash
mvn test
```

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods focused and classes cohesive

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is open source. Feel free to use, modify, and distribute according to your needs.

## Acknowledgments

- Inspired by the original [Buckwheat app](https://github.com/danilkinkin/buckwheat) by Danil Zakhvatkin
- Built with modern Java and JavaFX technologies
- Designed with user experience and functionality in mind

---

**Note**: This is a desktop application built with JavaFX. Make sure you have Java 17+ and JavaFX runtime available on your system to run the application.
