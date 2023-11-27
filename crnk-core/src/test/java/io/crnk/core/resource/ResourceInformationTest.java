package io.crnk.core.resource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.AnyResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceInformationTest {

    private ResourceInformation sut;

    private TypeParser typeParser = new TypeParser();


    private ResourceFieldImpl valueField;

    @BeforeEach
    public void setup() {
        ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
        ResourceFieldAccessor valueIdAccessor = Mockito.mock(ResourceFieldAccessor.class);
        valueField = new ResourceFieldImpl("value", "value", ResourceFieldType.RELATIONSHIP, String.class,
                String.class, "projects", null, SerializeType.LAZY, JsonIncludeStrategy.DEFAULT, LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
                new ResourceFieldAccess(true, true, true, true, true, true),
                "valueId", String.class, valueIdAccessor, RelationshipRepositoryBehavior.DEFAULT, null);
        sut = new ResourceInformation(typeParser, Task.class, "tasks", null, Arrays.asList(idField, valueField),
                OffsetLimitPagingSpec.class);
    }

    @Test
    public void onRelationshipFieldSearchShouldReturnExistingField() {
        ResourceField result = sut.findRelationshipFieldByName("value");
        assertThat(result.getUnderlyingName()).isEqualTo("value");
    }

    @Test
    public void toIdString() {
        Assertions.assertNull(sut.toIdString(null));
        Assertions.assertEquals("13", sut.toIdString(13));
    }

    @Test
    public void toResourceIdentifier() {
        Resource resource = new Resource();
        resource.setId("13");
        resource.setType("tasks");

        Task task = new Task();
        task.setId(13L);

        ResourceIdentifier expected = new ResourceIdentifier("13", "tasks");
        Assertions.assertSame(expected, sut.toResourceIdentifier(expected));
        Assertions.assertNull(sut.toResourceIdentifier(null));
        Assertions.assertEquals(expected, sut.toResourceIdentifier("13"));
        Assertions.assertEquals(expected, sut.toResourceIdentifier(13L));
        Assertions.assertEquals(expected, sut.toResourceIdentifier(task));
        Assertions.assertEquals(expected, sut.toResourceIdentifier(resource));
        Assertions.assertNotSame(resource, sut.toResourceIdentifier(resource));
    }

    @Test
    public void getFields() {
        Assertions.assertEquals(2, sut.getFields().size());
    }

    @Test
    public void checkIdAccess() {
        Task task = new Task();
        sut.setId(task, 13L);
        Assertions.assertEquals(13L, task.getId().longValue());
        Assertions.assertEquals(13L, sut.getId(task));
    }


    @Test
    public void checkGetAccessor() {
        Assertions.assertSame(valueField.getIdAccessor(), sut.getAccessor("valueId"));
        Assertions.assertSame(valueField.getAccessor(), sut.getAccessor("value"));
    }

    @Test
    public void checkAnyAccessor() {
        ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
        sut = new ResourceInformation(typeParser, ResourceWithAny.class, "tasks", null, Arrays.asList(idField),
                OffsetLimitPagingSpec.class);

        ResourceWithAny resource = new ResourceWithAny();

        AnyResourceFieldAccessor accessor = sut.getAnyFieldAccessor();

        Map<String, Object> map = accessor.getValues(resource);
        Assertions.assertNull(map.get("test"));
        accessor.setValue(resource, "test", "testValue");
        Assertions.assertEquals("testValue", map.get("test"));
        Assertions.assertEquals("testValue", resource.getProperties().get("test"));
    }

    @Test
    public void attributeCannotBeNamedLinks() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			new ResourceFieldImpl("links", "id", ResourceFieldType.ATTRIBUTE, Long.class, Long.class, null);
		});
    }

    @Test
    public void attributeCannotBeNamedMeta() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			new ResourceFieldImpl("meta", "id", ResourceFieldType.ATTRIBUTE, Long.class, Long.class, null);
		});
    }

    @Test
    public void checkGetAnyWithInvalidKey() {
        ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
        sut = new ResourceInformation(typeParser, ResourceWithAny.class, "tasks", null, Arrays.asList(idField),
                OffsetLimitPagingSpec.class);

        ResourceWithAny resource = new ResourceWithAny();

        AnyResourceFieldAccessor accessor = sut.getAnyFieldAccessor();
        Object o = accessor.getValues(resource);
        Map<String, Object> map = accessor.getValues(resource);
        Assertions.assertNull(map.get("notAllowed"));
    }

    @Test
    public void checkSetAnyWithInvalidKey() {
		Assertions.assertThrows(ResourceException.class, () -> {
	        ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
	        sut = new ResourceInformation(typeParser, ResourceWithAny.class, "tasks", null, Arrays.asList(idField),
	                OffsetLimitPagingSpec.class);

	        ResourceWithAny resource = new ResourceWithAny();

	        AnyResourceFieldAccessor accessor = sut.getAnyFieldAccessor();
	        accessor.setValue(resource, "notAllowed", "testValue");
		});
    }

    @Test
    public void checkAnyAccessorWithoutSetterThrowsException() {
		Assertions.assertThrows(InvalidResourceException.class, () -> {
	        ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
	        new ResourceInformation(typeParser, ResourceWithAnyWithoutSetter.class, "tasks", null, Arrays.asList(idField),
	        		OffsetLimitPagingSpec.class);
		});
    }

    @JsonApiResource(type = "test")
    public static class ResourceWithAny {

        @JsonApiId
        private String id;

        private Map<String, String> properties = new HashMap<>();

        @JsonAnyGetter
        public Map getProperties() {
            return properties;
        }

        @JsonAnySetter
        public void setProperty(String key, String value) {
            if ("notAllowed".equals(key)) {
                throw new IllegalStateException();
            }
            this.properties.put(key, value);
        }
    }


    @JsonApiResource(type = "test")
    class ResourceWithAnyWithoutSetter {

        @JsonApiId
        private String id;

        @JsonAnyGetter
        public Map<String, String> getProperties() {
            return null;
        }
    }


}
