package com.urjcservice.backend.config;

import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class SearchIndexer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SearchIndexer.class);

    private final EntityManager entityManager;
    private final Environment environment;

    public SearchIndexer(EntityManager entityManager, Environment environment) {
        this.entityManager = entityManager;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // respect a configuration property to disable indexing (useful for tests or
        // dev)
        boolean enabled = Boolean.parseBoolean(environment.getProperty("search.index.bulk.enabled", "true"));
        if (!enabled) {
            logger.info("Bulk indexing disabled by configuration");
            return;
        }

        try {
            logger.info("Starting Hibernate Search bulk indexing...");
            SearchSession searchSession = Search.session(entityManager);

            // This reads the database and creates the indexes in the lucene-indexes folder.
            searchSession.massIndexer()
                    .idFetchSize(150)
                    .batchSizeToLoadObjects(25)
                    .threadsToLoadObjects(2)
                    .startAndWait();

            logger.info("Lucene indices successfully created.");
        } catch (InterruptedException e) {
            logger.error("Indexing was interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warn("Skipping mass indexing due to error: {}", e.getMessage());
            logger.debug("Mass indexing error details", e);
        }
    }
}