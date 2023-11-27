package io.crnk.gen.typescript;

import io.crnk.gen.typescript.internal.TypescriptUtils;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSModule;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TypescriptUtilsTest {

    @Test
    public void checkHasPrivateConstructor() {
        ClassTestUtils.assertPrivateConstructor(TypescriptUtils.class);
    }

    @Test
    public void getNestedInterfaesCreatesNewInterface() {
        TSModule module = new TSModule();
        module.setName("TestModule");

        TSClassType classType = new TSClassType();
        classType.setParent(module);
        classType.setName("TestClass");
        module.getElements().add(classType);

        TSInterfaceType testInterface = TypescriptUtils.getNestedInterface(classType, "TestInterface", true);
        Assertions.assertEquals("TestInterface", testInterface.getName());
        Assertions.assertTrue(testInterface.getParent() instanceof TSModule);
        Assertions.assertEquals(module, testInterface.getParent().getParent());
        Assertions.assertEquals("TestClass", ((TSModule) testInterface.getParent()).getName());

        Assertions.assertEquals(2, module.getElements().size());
    }

    @Test
    public void getNestedInterfacesReturnsNullIfDoesNotExistsAndNonCreateRequested() {
        TSModule module = new TSModule();
        module.setName("TestModule");

        TSClassType classType = new TSClassType();
        classType.setParent(module);
        classType.setName("TestClass");
        module.getElements().add(classType);

        TSInterfaceType testInterface = TypescriptUtils.getNestedInterface(classType, "TestInterface", false);
        Assertions.assertNull(testInterface);
    }

    @Test
    public void getNestedInterfacesReturnsNullIfNoParent() {
        TSClassType classType = new TSClassType();
        classType.setName("TestClass");

        TSInterfaceType testInterface = TypescriptUtils.getNestedInterface(classType, "TestInterface", false);
        Assertions.assertNull(testInterface);
    }

    @Test
    public void getNestedInterfacesThrowsExceptionOnCreateIfNoParent() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
            TSClassType classType = new TSClassType();
            classType.setName("TestClass");

            TypescriptUtils.getNestedInterface(classType, "TestInterface", true);
        });
    }
}
