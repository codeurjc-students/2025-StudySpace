package com.urjcservice.backend.service;

import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SearchIndexer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SearchIndexer.class);

    private final EntityManager entityManager;

    public SearchIndexer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
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
        }
    }
}