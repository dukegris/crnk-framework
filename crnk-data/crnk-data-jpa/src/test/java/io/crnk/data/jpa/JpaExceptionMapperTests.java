package io.crnk.data.jpa;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.BadRequestException;
import io.crnk.data.jpa.internal.HibernateConstraintViolationExceptionMapper;
import io.crnk.data.jpa.internal.PersistenceExceptionMapper;
import io.crnk.data.jpa.internal.PersistenceRollbackExceptionMapper;
import io.crnk.data.jpa.internal.TransactionRollbackExceptionMapper;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.PersistenceException;

public class JpaExceptionMapperTests {

    private CrnkBoot boot;

    @BeforeEach
    public void setup() {
        boot = new CrnkBoot();
        boot.addModule(JpaModule.newClientModule());
        boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
        boot.boot();
    }

    @Test
    public void testPersistenceException() {
        PersistenceException exception = new PersistenceException(new BadRequestException("test"));
        ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
        PersistenceExceptionMapper mapper = (PersistenceExceptionMapper) exceptionMapperRegistry.findMapperFor(PersistenceException.class).get();
        ErrorResponse response = mapper.toErrorResponse(exception);
        ErrorData errorData = response.getErrors().iterator().next();
        Assertions.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
        Assertions.assertEquals("test", errorData.getDetail());
    }

    @Test
    public void testConstraintException() {
        ConstraintViolationException exception = new ConstraintViolationException("message", null, "constraint");
        ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
        HibernateConstraintViolationExceptionMapper mapper = (HibernateConstraintViolationExceptionMapper) exceptionMapperRegistry.findMapperFor(ConstraintViolationException.class).get();
        ErrorResponse response = mapper.toErrorResponse(exception);
        ErrorData errorData = response.getErrors().iterator().next();
        Assertions.assertEquals(Integer.toString(HttpStatus.UNPROCESSABLE_ENTITY_422), errorData.getStatus());
        Assertions.assertEquals(exception.getConstraintName(), errorData.getCode());
        Assertions.assertEquals(exception.getMessage(), errorData.getDetail());

        Assertions.assertTrue(mapper.accepts(response));
        ConstraintViolationException deserializedException = mapper.fromErrorResponse(response);
        Assertions.assertEquals(exception.getMessage(), deserializedException.getMessage());
        Assertions.assertEquals(exception.getConstraintName(), deserializedException.getConstraintName());
    }

    @Test
    public void testPersistenceRollbackException() {
        jakarta.persistence.RollbackException exception = new jakarta.persistence.RollbackException(new BadRequestException("test"));
        ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
        PersistenceRollbackExceptionMapper mapper = (PersistenceRollbackExceptionMapper) exceptionMapperRegistry.findMapperFor(jakarta.persistence.RollbackException.class).get();
        ErrorResponse response = mapper.toErrorResponse(exception);
        ErrorData errorData = response.getErrors().iterator().next();
        Assertions.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
        Assertions.assertEquals("test", errorData.getDetail());
    }

    @Test
    public void testTransactionRollbackException() {
        jakarta.transaction.RollbackException exception = new jakarta.transaction.RollbackException() {
            public Throwable getCause() {
                return new BadRequestException("test");
            }
        };
        ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
        TransactionRollbackExceptionMapper mapper = (TransactionRollbackExceptionMapper) exceptionMapperRegistry.findMapperFor(exception.getClass()).get();
        ErrorResponse response = mapper.toErrorResponse(exception);
        ErrorData errorData = response.getErrors().iterator().next();
        Assertions.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
        Assertions.assertEquals("test", errorData.getDetail());
    }
}
