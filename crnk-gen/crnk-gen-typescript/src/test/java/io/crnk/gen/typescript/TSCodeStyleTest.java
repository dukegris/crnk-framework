package io.crnk.gen.typescript;

import io.crnk.gen.typescript.model.writer.TSCodeStyle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TSCodeStyleTest {

    @Test
    public void test() {
        TSCodeStyle style = new TSCodeStyle();
        Assertions.assertEquals("\t", style.getIndentation());
        Assertions.assertEquals("\n", style.getLineSeparator());
        style.setIndentation("a");
        style.setLineSeparator("b");
        Assertions.assertEquals("a", style.getIndentation());
        Assertions.assertEquals("b", style.getLineSeparator());
    }
}
