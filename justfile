set dotenv-filename := ".env.dev"

# Start the app with dev profile
dev:
    cd boot && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run all tests (unit + integration) across all modules
test:
    mvn clean verify

# Package without running tests
build:
    mvn clean package -DskipTests

# Full build + test, then start
ci-dev:
    mvn clean verify && cd boot && mvn spring-boot:run -Dspring-boot.run.profiles=dev


