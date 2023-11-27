package io.crnk.core.engine.internal.document.mapper.lookup;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.engine.internal.document.mapper.IncludeLookupUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class IncludeLookupUtilTest extends AbstractDocumentMapperTest {


    @Test
    public void checkLegacyDefaultLookupIncludeBehavior() {
        Assertions.assertEquals(LookupIncludeBehavior.DEFAULT, IncludeLookupUtil.getGlobalLookupIncludeBehavior(null));

        PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
        Assertions.assertEquals(LookupIncludeBehavior.DEFAULT, IncludeLookupUtil.getGlobalLookupIncludeBehavior
                (propertiesProvider));
    }

    @Test
    public void checkDefaultLookupIncludeBehavior() {
        Assertions.assertEquals(LookupIncludeBehavior.DEFAULT, IncludeLookupUtil.getGlobalLookupIncludeBehavior(null));

        PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
        Mockito.when(propertiesProvider.getProperty(CrnkProperties.DEFAULT_LOOKUP_BEHAVIOR))
                .thenReturn(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS.toString());
        Assertions.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
                IncludeLookupUtil.getGlobalLookupIncludeBehavior(propertiesProvider));
    }

}
