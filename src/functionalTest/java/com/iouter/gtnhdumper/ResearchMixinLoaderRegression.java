package com.iouter.gtnhdumper;

import java.util.Collections;
import java.util.jar.JarFile;

/** Executed with only the loader, JDK and UniMixins on the runtime classpath. */
public final class ResearchMixinLoaderRegression {

    private ResearchMixinLoaderRegression() {}

    public static void main(String[] args) throws Exception {
        if (ResearchMixinLoaderRegression.class.getClassLoader()
            .getResource("thaumcraft/api/research/ResearchItem.class") != null) {
            throw new AssertionError("This check requires a runtime without Thaumcraft");
        }
        ResearchMixinLoader loader = new ResearchMixinLoader();
        if (!loader.getMixins(Collections.emptySet())
            .isEmpty()) {
            throw new AssertionError("No research mixin is selected without Thaumcraft");
        }
        if (!loader.getMixins(Collections.singleton("Thaumcraft"))
            .equals(Collections.singletonList("ResearchCategoriesMixin"))) {
            throw new AssertionError("Thaumcraft selects the research hook by name");
        }
        if (loader.getClass()
            .getClassLoader()
            .getResource(loader.getMixinConfig()) == null) {
            throw new AssertionError("The declared mixin config must be packaged");
        }
        try (JarFile jar = new JarFile(args[0])) {
            String earlyConfigs = jar.getManifest()
                .getMainAttributes()
                .getValue("MixinConfigs");
            for (String config : earlyConfigs.split(",")) {
                if (jar.getJarEntry(config.trim()) == null) {
                    throw new AssertionError("Missing manifest mixin config: " + config);
                }
                if (config.trim()
                    .equals(loader.getMixinConfig())) {
                    throw new AssertionError("Research config is queued by the late loader");
                }
            }
            for (String resource : new String[] { loader.getMixinConfig(), "mixins.gtnhdumper.refmap.json",
                "com/iouter/gtnhdumper/ResearchMixinLoader.class",
                "com/iouter/gtnhdumper/mixins/ResearchCategoriesMixin.class" }) {
                if (jar.getJarEntry(resource) == null)
                    throw new AssertionError("Missing research hook resource: " + resource);
            }
        }
        System.out.println("Optional research mixin loader regression checks passed");
    }
}
