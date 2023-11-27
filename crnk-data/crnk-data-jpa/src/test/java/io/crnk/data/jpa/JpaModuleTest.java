package io.crnk.data.jpa;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.data.jpa.model.TestEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.persistence.EntityManager;

public class JpaModuleTest {


    @Test	
    public void cannotPerformDuplicateRegistration() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
        JpaModuleConfig config = new JpaModuleConfig();
        config.addRepository(JpaRepositoryConfig.create(TestEntity.class));
        config.addRepository(JpaRepositoryConfig.create(TestEntity.class));
		});
    }

    @Test
    public void checkInvalidConfig() {
        JpaModuleConfig config = new JpaModuleConfig();
        config.addRepository(JpaRepositoryConfig.create(String.class));

        CrnkBoot boot = new CrnkBoot();
        EntityManager em = Mockito.mock(EntityManager.class);
        TransactionRunner transactionManager = Mockito.mock(TransactionRunner.class);
        boot.addModule(JpaModule.createServerModule(config, em, transactionManager));
        try {
            boot.boot();
        } catch (IllegalStateException e) {
            // ok
            Assertions.assertEquals(e.getMessage(),
                    "failed to gather entity informations from class java.lang.String, make sure it is probably annotated with "
                            + "JPA annotations");
        }
    }
}
