package io.crnk.test.mock.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.UntypedRelationshipRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class DynamicRelationshipRepository implements UntypedRelationshipRepository<Resource, String, Resource, String> {

    private static Map<String, Resource> RESOURCES = new HashMap<>();

    private String resourceType;

    public DynamicRelationshipRepository(String resourceType) {
        this.resourceType = resourceType;
    }

    public static void clear() {
        RESOURCES.clear();
    }

    @Override
    public String getSourceResourceType() {
        return resourceType;
    }

    @Override
    public String getTargetResourceType() {
        return resourceType;
    }

    @Override
    public Class<Resource> getSourceResourceClass() {
        return Resource.class;
    }

    @Override
    public Class<Resource> getTargetResourceClass() {
        return Resource.class;
    }

    @Override
    public void setRelation(Resource source, String targetId, String fieldName) {
        Assertions.assertEquals(targetId, "12");
    }

    @Override
    public void setRelations(Resource source, Collection<String> targetIds, String fieldName) {
        String targetId = targetIds.iterator().next();
        Assertions.assertEquals(targetId, "12");
    }

    @Override
    public void addRelations(Resource source, Collection<String> targetIds, String fieldName) {
        String targetId = targetIds.iterator().next();
        Assertions.assertEquals(targetId, "12");
    }

    @Override
    public void removeRelations(Resource source, Collection<String> targetIds, String fieldName) {
        String targetId = targetIds.iterator().next();
        Assertions.assertEquals(targetId, "12");
    }

    @Override
    public Resource findOneTarget(String sourceId, String fieldName, QuerySpec querySpec) {
        return createResource();
    }


    @Override
    public ResourceList<Resource> findManyTargets(String sourceId, String fieldName, QuerySpec querySpec) {
        DefaultResourceList<Resource> list = new DefaultResourceList<>();
        list.add(createResource());
        return list;
    }


    private Resource createResource() {
        ObjectMapper mapper = new ObjectMapper();
        Resource resource = new Resource();
        resource.setId("john");
        resource.setType(resourceType);
        try {
            resource.getAttributes().put("value", mapper.readTree("\"doe\""));
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        return resource;
    }
}