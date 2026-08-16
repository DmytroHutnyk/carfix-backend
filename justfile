set dotenv-filename := ".env.dev"

# Machine-local recipes; not in the repo, absent on fresh clones
import? '.local/local.just'

# Start the app with dev profile
dev:
    cd boot && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run all tests (unit + integration) across all modules
test:
    mvn clean verify

# Package without running tests
build:
    mvn clean install -DskipTests

# Full build + test, then start
full-dev:
    mvn clean install && cd boot && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Full build + test, then start skip git commit plugin
full-dev-skip-git:
    mvn clean install -Dmaven.gitcommitid.skip=true \
      && cd boot \
      && mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dmaven.gitcommitid.skip=true


# Run the M4 slot-endpoint checklist as a real-HTTP integration test (needs a reachable dev Postgres)
slot-checklist:
    JAVA_HOME=/opt/homebrew/opt/openjdk PATH="/opt/homebrew/opt/openjdk/bin:$PATH" \
    mvn -o test -pl boot -am \
      -Dtest=SlotEndpointChecklistIT \
      -Dsurefire.failIfNoSpecifiedTests=false \
      -Dmaven.gitcommitid.skip=true

# Run the M6 search-availability checklist as a real-HTTP integration test (needs a reachable dev Postgres)
search-checklist:
    JAVA_HOME=/opt/homebrew/opt/openjdk PATH="/opt/homebrew/opt/openjdk/bin:$PATH" \
    mvn -o test -pl boot -am \
      -Dtest=SearchAvailabilityChecklistIT \
      -Dsurefire.failIfNoSpecifiedTests=false \
      -Dmaven.gitcommitid.skip=true

clean-db:
    cd boot \
    && mvn flyway:clean -Dflyway.cleanDisabled=false

clean-db-skip-git:
    cd boot \
    && mvn flyway:clean -Dflyway.cleanDisabled=false -Dmaven.gitcommitid.skip=true




