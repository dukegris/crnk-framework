package io.crnk.gen.typescript.internal;

import io.crnk.gen.typescript.TSGeneratorConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TSGeneratorConfigTest {

    @Test
    public void test() {
        TSGeneratorConfig config = new TSGeneratorConfig();

        Set<String> includes = new HashSet<>();
        config.setIncludes(includes);
        Assertions.assertSame(includes, config.getIncludes());

        Set<String> excludes = new HashSet<>();
        config.setExcludes(excludes);
        Assertions.assertSame(excludes, config.getExcludes());

        Assertions.assertFalse(config.getExpressions());
        config.setExpressions(true);
        Assertions.assertTrue(config.getExpressions());

        Assertions.assertEquals("src", config.getSourceDirectoryName());
        config.setSourceDirectoryName("testDir");
        Assertions.assertEquals("testDir", config.getSourceDirectoryName());

        Assertions.assertEquals("UNLICENSED", config.getNpm().getLicense());
        config.getNpm().setLicense("someLicense");
        Assertions.assertEquals("someLicense", config.getNpm().getLicense());

        Assertions.assertEquals(null, config.getNpm().getGitRepository());
        config.getNpm().setGitRepository("git");
        Assertions.assertEquals("git", config.getNpm().getGitRepository());

        Assertions.assertEquals(null, config.getNpm().getDescription());
        config.getNpm().setDescription("desc");
        Assertions.assertEquals("desc", config.getNpm().getDescription());

        Map packageMapping = new HashMap();
        config.getNpm().setPackageMapping(packageMapping);
        Assertions.assertSame(packageMapping, config.getNpm().getPackageMapping());

        Map peerDep = new HashMap();
        config.getNpm().setPeerDependencies(peerDep);
        Assertions.assertSame(peerDep, config.getNpm().getPeerDependencies());

        Map devDep = new HashMap();
        config.getNpm().setDevDependencies(devDep);
        Assertions.assertSame(devDep, config.getNpm().getDevDependencies());
    }
}
