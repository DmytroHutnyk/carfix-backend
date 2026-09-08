set dotenv-filename := ".env.dev"
set unstable := true

# Machine-local recipes; not in the repo, absent on fresh clones
import? '.local/local.just'

dev:
    cd boot && mvn spring-boot:run -Dspring-boot.run.profiles=dev

test:
    mvn clean verify

build:
    mvn clean install -DskipTests

full-dev:
    mvn clean install && cd boot && mvn spring-boot:run -Dspring-boot.run.profiles=dev

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

# Run the M5 booking-endpoint checklist as a real-HTTP integration test (needs a reachable dev Postgres)
booking-checklist:
    JAVA_HOME=/opt/homebrew/opt/openjdk PATH="/opt/homebrew/opt/openjdk/bin:$PATH" \
    mvn -o test -pl boot -am \
      -Dtest=BookingEndpointChecklistIT \
      -Dsurefire.failIfNoSpecifiedTests=false \
      -Dmaven.gitcommitid.skip=true

clean-db:
    cd boot \
    && mvn flyway:clean -Dflyway.cleanDisabled=false

clean-db-skip-git:
    cd boot \
    && mvn flyway:clean -Dflyway.cleanDisabled=false -Dmaven.gitcommitid.skip=true



