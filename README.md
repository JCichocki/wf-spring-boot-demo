# wf-spring-boot-demo



Method 1: Edit Run/Debug Configuration (Recommended)

1. Open Run Configuration
   - Click the dropdown next to the run/debug button (top toolbar)
   - Select "Edit Configurations..."
2. Modify Spring Boot Configuration
   - Find your Spring Boot application configuration
   - In the configuration window, look for one of these options:

Option A - Active profiles field:
- Find "Active profiles" field
- Enter: local

Option B - Environment variables:
- Find "Environment variables" field
- Add: SPRING_PROFILES_ACTIVE=local

Option C - VM options:
- Find "VM options" field
- Add: -Dspring.profiles.active=local
3. Apply and Save
   - Click "Apply" then "OK"
   - Now debug as usual - it will use the local profile

Method 2: Using Spring Boot Run Dashboard

If you have the Spring Boot plugin:
1. Open the Spring Boot Run Dashboard (View → Tool Windows → Spring Boot)
2. Right-click your application
3. Select "Edit Configuration"
4. Add local to the "Active profiles" field

Method 3: Application Properties in IntelliJ

1. Go to File → Project Structure → Modules
2. Select your module → Spring
3. Add local to the default profiles

Your application-local.yml will automatically be loaded when you debug with the local profile active, overriding any settings from application.yml.
