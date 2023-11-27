package io.crnk.data.jpa.internal;

import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QueryFactoryDiscoveryTest {

    @Test
    public void checkDiscoverQueryDsl() {
        QueryFactoryDiscovery discovery = new QueryFactoryDiscovery();
        Assertions.assertEquals(QuerydslQueryFactory.class, discovery.discoverDefaultFactory().getClass());
    }

    @Test
    public void checkDiscoverCriteriaApi() {
        QueryFactoryDiscovery discovery = new QueryFactoryDiscovery();

        ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(bootstrapClassLoader);
        try {
            Assertions.assertEquals(JpaCriteriaQueryFactory.class, discovery.discoverDefaultFactory().getClass());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Test
    public void checkJpaModuleIntegration() {
        JpaModuleConfig config = new JpaModuleConfig();
        Assertions.assertEquals(QuerydslQueryFactory.class, config.getQueryFactory().getClass());
    }
}
