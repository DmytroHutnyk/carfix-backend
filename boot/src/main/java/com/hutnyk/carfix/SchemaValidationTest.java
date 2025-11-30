package com.hutnyk.carfix;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaValidationTest {

    private final EntityManagerFactory entityManagerFactory;

    @EventListener(ApplicationReadyEvent.class)
    public void validate() {
        Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();

        log.info("=== JPA Metamodel Entity Listing ===");
        log.info("Found {} JPA entities", entities.size());

        for (EntityType<?> e : entities) {
            log.info(" - {}", e.getName());
        }

        log.info("=== End of entity list ===");
    }
}


