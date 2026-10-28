package com.iouter.gtnhdumper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

/** Queue the research hook after mod discovery, while Thaumcraft remains an optional dependency. */
@LateMixin
public final class ResearchMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.gtnhdumper.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return loadedMods.contains("Thaumcraft") ? Collections.singletonList("ResearchCategoriesMixin")
            : Collections.emptyList();
    }
}
