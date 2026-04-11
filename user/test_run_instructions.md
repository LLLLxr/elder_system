# Testing H2 Database Configuration

## Changes Made
1. **Fixed table name issue**: Changed `user` table to `sys_user` in:
   - schema-h2.sql
   - data-h2.sql
   - User.java entity (@Table annotation)

2. **Updated foreign key references**: Changed in user_role table

## How to Test
Since Maven is not available in the system PATH, you have two options:

### Option 1: Use IDE
1. Open the project in IntelliJ IDEA or Eclipse
2. Navigate to `UserApplication.java`
3. Right-click and select "Run UserApplication.main()" with the test profile

### Option 2: Install Maven and Run
1. Download and install Apache Maven
2. Add Maven to the system PATH
3. Run: `mvn spring-boot:run -Dspring-boot.run.profiles=test`

## Expected Results
- The application should start successfully on port 8084
- H2 database should be initialized with test data
- Access the H2 console at: http://localhost:8084/user-service/h2-console
- Use the following credentials to test APIs:
  - Admin: username=admin, password=admin123
  - User: username=user, password=user123
  - Elder: username=elder, password=elder123

## Verification
1. Check that the application starts without errors
2. Access the H2 console and verify tables are created (sys_user, role, permission, etc.)
3. Test the authentication endpoints with the provided credentials