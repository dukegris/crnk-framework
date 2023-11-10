package io.crnk.data.jpa;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListenerBase;
import io.crnk.data.facet.FacetModule;
import io.crnk.data.jpa.meta.JpaMetaProvider;
import io.crnk.data.jpa.model.CountryTranslationEntity;
import io.crnk.data.jpa.query.AbstractJpaTest;
import io.crnk.data.jpa.util.EntityManagerProducer;
import io.crnk.data.jpa.util.JpaTestConfig;
import io.crnk.data.jpa.util.SpringTransactionRunner;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.TestModule;
import okhttp3.OkHttpClient.Builder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public abstract class AbstractJpaJerseyTest extends JerseyTestBase {

    protected ResourceMetaProvider resourceMetaProvider;

    protected JpaMetaProvider jpaMetaProvider;

    protected CrnkClient client;

    protected AnnotationConfigApplicationContext context;

    protected MetaModule metaModule;

    public static void setNetworkTimeout(CrnkClient client, final int timeout, final TimeUnit timeUnit) {
        OkHttpAdapter httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
        httpAdapter.addListener(new OkHttpAdapterListenerBase() {

            @Override
            public void onBuild(Builder builder) {
                builder.readTimeout(timeout, timeUnit);
            }
        });
    }

    @Before
    public void setup() {
        client = new CrnkClient(getBaseUri().toString());
        client.getObjectMapper().registerModule(new JavaTimeModule());

        JpaModule module = JpaModule.newClientModule();
        client.addModule(module);

        MetaModuleConfig config = new MetaModuleConfig();
        config.addMetaProvider(new ResourceMetaProvider());
        MetaModule clientMetaModule = MetaModule.createServerModule(config);
        client.addModule(clientMetaModule);

        ((MetaLookupImpl) metaModule.getLookup()).initialize();

        setNetworkTimeout(client, 10000, TimeUnit.SECONDS);
    }

    protected void setupModule(JpaModuleConfig config, boolean server, EntityManager em) {

    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);
        transactionRunner.doInTransaction(new Callable<Object>() {

            @Override
            public Object call() {
                EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
                AbstractJpaTest.clear(em);
                return null;
            }
        });

        if (context != null) {
			// RCS deprecated
			// context.destroy();
			context.close();
        }
    }

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @ApplicationPath("/")
    private class TestApplication extends ResourceConfig {

        public TestApplication() {

            Assert.assertNull(context);

            context = new AnnotationConfigApplicationContext(JpaTestConfig.class);
            context.start();
            EntityManagerFactory emFactory = context.getBean(EntityManagerFactory.class);
            EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
            SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);

            CrnkFeature feature = new CrnkFeature();
            feature.addModule(new FacetModule());
            feature.addModule(new TestModule());

            JpaModuleConfig config = new JpaModuleConfig();

            Set<ManagedType<?>> managedTypes = emFactory.getMetamodel().getManagedTypes();
            for (ManagedType<?> managedType : managedTypes) {
                Class<?> managedJavaType = managedType.getJavaType();
                if (managedJavaType.getAnnotation(Entity.class) != null && managedJavaType != CountryTranslationEntity.class) {
                    if (!config.hasRepository(managedJavaType)) {
                        config.addRepository(JpaRepositoryConfig.builder(managedJavaType).build());
                    }
                }
            }
            setupModule(config, true, em);
            JpaModule module = JpaModule.createServerModule(config, em, transactionRunner);

            feature.addModule(module);

            MetaModuleConfig metaConfig = new MetaModuleConfig();
            resourceMetaProvider = new ResourceMetaProvider();
            jpaMetaProvider = new JpaMetaProvider(emFactory);
            metaConfig.addMetaProvider(resourceMetaProvider);
            metaConfig.addMetaProvider(jpaMetaProvider);
            metaModule = MetaModule.createServerModule(metaConfig);
            feature.addModule(metaModule);
            feature.getObjectMapper().registerModule(new JavaTimeModule());

            setupFeature(feature);

            register(feature);

        }
    }

    protected void setupFeature(CrnkFeature feature) {

    }

}
