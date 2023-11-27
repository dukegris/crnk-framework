package io.crnk.gen.openapi.internal;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class OASMergeUtilTest {
  @Test
  public void testMergeOperations() {
    Operation thatOperation = new Operation();

    Operation thisOperation = new Operation();
    thisOperation.setOperationId("new id");
    thisOperation.setSummary("new summary");
    thisOperation.setDescription("new description");

    Map<String, Object> extensions = new HashMap<>();
    extensions.put("new schema", new Schema());
    thisOperation.setExtensions(extensions);

    Assertions.assertSame(thisOperation, OASMergeUtil.mergeOperations(thisOperation, null));

    Operation afterMerge = OASMergeUtil.mergeOperations(thatOperation, thisOperation);
    Assertions.assertEquals("new id", afterMerge.getOperationId());
    Assertions.assertEquals("new summary", afterMerge.getSummary());
    Assertions.assertEquals("new description", afterMerge.getDescription());
    Assertions.assertSame(extensions, afterMerge.getExtensions());

    thatOperation.setOperationId("existing id");
    thatOperation.setSummary("existing summary");
    thatOperation.setDescription("existing description");

    Map<String, Object> existingExtensions = new HashMap<>();
    extensions.put("existing schema", new Schema());
    thisOperation.setExtensions(extensions);

    afterMerge = OASMergeUtil.mergeOperations(thisOperation, thatOperation);
    Assertions.assertEquals("existing id", afterMerge.getOperationId());
    Assertions.assertEquals("existing summary", afterMerge.getSummary());
    Assertions.assertEquals("existing description", afterMerge.getDescription());
    Assertions.assertSame(extensions, afterMerge.getExtensions());
  }
}
