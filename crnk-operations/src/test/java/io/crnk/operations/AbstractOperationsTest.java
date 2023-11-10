package io.crnk.operations;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.crnk.client.CrnkClient;
import io.crnk.client.action.JerseyActionStubFactory;
import io.crnk.client.http.HttpAdapterProvider;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListenerBase;
import io.crnk.client.http.okhttp.OkHttpAdapterProvider;
import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.meta.JpaMetaProvider;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.JpaQueryFactoryContext;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.MetaPartition;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.operations.model.MovieEntity;
import io.crnk.operations.model.PersonEntity;
import io.crnk.operations.server.OperationsModule;
import io.crnk.operations.server.TransactionOperationFilter;
import io.crnk.rs.CrnkFeature;
import io.crnk.spring.internal.SpringServiceDiscovery;
import io.crnk.spring.jpa.SpringTransactionRunner;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.models.Task;
import io.crnk.validation.ValidationModule;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import okhttp3.OkHttpClient.Builder;

public abstract class AbstractOperationsTest extends JerseyTestBase {

	protected CrnkClient client;

	protected AnnotationConfigApplicationContext context;

	protected OperationsModule operationsModule;

	public static void setNetworkTimeout(CrnkClient client, final int timeout, final TimeUnit timeUnit) {
		// RCS Blindar el tipo de adaptador
		List<HttpAdapterProvider> httpAdapterProviders = client.getHttpAdapterProviders();
		OkHttpAdapter httpAdapter = null;
		for (HttpAdapterProvider httpAdapterProvider : httpAdapterProviders) {
			if (httpAdapterProvider.isAvailable() && httpAdapterProvider instanceof OkHttpAdapterProvider) {
				httpAdapter = (OkHttpAdapter) httpAdapterProvider.newInstance();
				break;
			}
		}		
		// RCS Blindar el tipo de adaptador
		if (httpAdapter == null) {
			httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
		}
		// OkHttpAdapter httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
		httpAdapter.addListener(new OkHttpAdapterListenerBase() {
			@Override
			public void onBuild(Builder builder) {
				builder.readTimeout(timeout, timeUnit);
			}
		});
	}

	public static void clear(EntityManager em) {
		clear(em, JpaCriteriaQueryFactory.newInstance());
	}

	public static void clear(final EntityManager em, JpaQueryFactory factory) {
		factory.initalize(new JpaQueryFactoryContext() {
			@Override
			public MetaPartition getMetaPartition() {
				MetaLookupImpl metaLookup = new MetaLookupImpl();
				JpaMetaProvider metaProvider = new JpaMetaProvider(em.getEntityManagerFactory());
				metaLookup.addProvider(metaProvider);
				metaLookup.initialize();
				return metaProvider.getPartition();
			}

			@Override
			public EntityManager getEntityManager() {
				return em;
			}
		});
		clear(em, factory.query(MovieEntity.class).buildExecutor().getResultList());
		clear(em, factory.query(PersonEntity.class).buildExecutor().getResultList());
		em.flush();
		em.clear();
	}

	private static void clear(EntityManager em, List<?> list) {
		for (Object obj : list) {
			em.remove(obj);
		}
	}

	@Before
	public void setup() {
		clear();
		client = new CrnkClient(getBaseUri().toString());
		client.setActionStubFactory(JerseyActionStubFactory.newInstance());
		client.getHttpAdapter().setReceiveTimeout(10000000, TimeUnit.MILLISECONDS);

		MetaModuleConfig config = new MetaModuleConfig();
		config.addMetaProvider(new ResourceMetaProvider());
		MetaModule metaModule = MetaModule.createServerModule(config);
		client.addModule(metaModule);

		JpaModule module = JpaModule.newClientModule();
		setupModule(module, false);
		client.addModule(module);

		setNetworkTimeout(client, 10000, TimeUnit.SECONDS);
	}

	protected MovieEntity newMovie(String title) {
		MovieEntity movie = new MovieEntity();
		movie.setId(UUID.randomUUID());
		movie.setImdbId(title);
		movie.setTitle(title);
		return movie;
	}

	protected PersonEntity newPerson(String name) {
		PersonEntity person = new PersonEntity();
		person.setId(UUID.randomUUID());
		person.setName(name);
		return person;
	}


	protected Task newTask(String name) {
		Task person = new Task();
		person.setId(UUID.randomUUID().getLeastSignificantBits());
		person.setName(name);
		return person;
	}

	protected void setupModule(JpaModule module, boolean server) {
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();

		clear();

		if (context != null) {
			// RCS deprecated
			// context.destroy();
			context.close();
		}
	}

	protected void clear() {
		SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);
		transactionRunner.doInTransaction(() -> {
			EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
			clear(em);
			return null;
		});
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private class TestApplication extends ResourceConfig {


		public TestApplication() {
			Assert.assertNull(context);

			context = new AnnotationConfigApplicationContext(io.crnk.operations.OperationsTestConfig.class);
			context.start();
			EntityManagerFactory emFactory = context.getBean(EntityManagerFactory.class);
			EntityManager em = context.getBean(io.crnk.operations.EntityManagerProducer.class).getEntityManager();
			SpringServiceDiscovery serviceDiscovery = context.getBean(SpringServiceDiscovery.class);
			SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);

			CrnkFeature feature = new CrnkFeature();
			feature.getBoot().setServiceDiscovery(serviceDiscovery);

			JpaModuleConfig config = new JpaModuleConfig();
			Set<ManagedType<?>> managedTypes = emFactory.getMetamodel().getManagedTypes();
			for (ManagedType<?> managedType : managedTypes) {
				Class<?> managedJavaType = managedType.getJavaType();
				if (managedJavaType.getAnnotation(Entity.class) != null) {
					if (!config.hasRepository(managedJavaType)) {
						config.addRepository(JpaRepositoryConfig.builder(managedJavaType).build());
					}
				}
			}

			JpaModule jpaModule = JpaModule.createServerModule(config, em, transactionRunner);
			setupModule(jpaModule, true);

			operationsModule = OperationsModule.create();

			// tag::transaction[]
            if (isTransactional()) {
				operationsModule.addFilter(new TransactionOperationFilter());
			}
			// end::transaction[]
			feature.addModule(jpaModule);
			feature.addModule(operationsModule);
			feature.addModule(ValidationModule.create());
			setupServer(feature);
			register(feature);
		}
	}

    protected boolean isTransactional() {
        return true;
    }

	protected void setupServer(CrnkFeature feature) {
		// noting to do, override if necessary
	}

}
