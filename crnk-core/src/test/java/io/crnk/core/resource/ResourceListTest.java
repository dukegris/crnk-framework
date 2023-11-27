package io.crnk.core.resource;

import io.crnk.core.engine.internal.utils.CastableInformation;
import io.crnk.core.mock.models.Task;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.MetaInformation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class ResourceListTest {

    @Test
    public void defaultConstructor() {
        DefaultResourceList<Task> list = new DefaultResourceList<Task>();
        Assertions.assertNull(list.getMeta());
        Assertions.assertNull(list.getLinks());
        Assertions.assertNull(list.getMeta(TestMeta.class));
        Assertions.assertNull(list.getLinks(TestLinks.class));
    }

    @Test
    public void setters() {
        ResourceListBase<Task, TestMeta, TestLinks> list = new ResourceListBase<Task, TestMeta, TestLinks>() {

        };
        list.setLinks(new TestLinks());
        list.setMeta(new TestMeta());
        Assertions.assertNotNull(list.getMeta());
        Assertions.assertNotNull(list.getLinks());
    }

    @Test
    public void defaultInformationConstructor() {
        DefaultResourceList<Task> list = new DefaultResourceList<Task>(new TestMeta(), new TestLinks());
        Assertions.assertNotNull(list.getMeta());
        Assertions.assertNotNull(list.getLinks());
    }

    @Test
    public void testListConstructor() {
        LinkedList<Task> linkedList = new LinkedList<Task>();
        DefaultResourceList<Task> list = new DefaultResourceList<Task>(linkedList, new TestMeta(), new TestLinks());
        Assertions.assertNotNull(list.getMeta());
        Assertions.assertNotNull(list.getLinks());
        list.add(new Task());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(1, linkedList.size());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void casting() {
        DefaultResourceList<Task> list = new DefaultResourceList<Task>(new TestMeta(), new TestLinks());

        TestMeta testMeta = list.getMeta(TestMeta.class);
        Assertions.assertNotNull(testMeta);
        OtherMeta otherMeta = list.getMeta(OtherMeta.class);
        Assertions.assertNotNull(otherMeta);

        TestLinks testLinks = list.getLinks(TestLinks.class);
        Assertions.assertNotNull(testLinks);
        OtherLinks otherLinks = list.getLinks(OtherLinks.class);
        Assertions.assertNotNull(otherLinks);
    }

    class TestLinks implements LinksInformation, CastableInformation<LinksInformation> {

        public String name = "value";

        @SuppressWarnings("unchecked")
        @Override
        public <L extends LinksInformation> L as(Class<L> linksClass) {
            return (L) new OtherLinks();
        }
    }

    class TestMeta implements MetaInformation, CastableInformation<MetaInformation> {

        public String name = "value";

        @SuppressWarnings("unchecked")
        @Override
        public <L extends MetaInformation> L as(Class<L> linksClass) {
            return (L) new OtherMeta();
        }

    }

    class OtherLinks implements LinksInformation {

        public String name = "value";
    }

    class OtherMeta implements MetaInformation {

        public String name = "value";

    }

}
