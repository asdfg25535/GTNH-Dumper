package com.iouter.gtnhdumper.common.utils;

import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.logging.log4j.ThreadContext;

import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;

/** Original registration source for each research object, including entries in shared categories. */
public final class ResearchSources {

    private static final Map<ResearchItem, String> SOURCES = new IdentityHashMap<>();

    private ResearchSources() {}

    public static void capture(ResearchItem research) {
        ResearchCategoryList category = ResearchCategories.getResearchList(research.category);
        if (category == null || category.research.get(research.key) != research || SOURCES.containsKey(research))
            return;
        // FML sets this context around every mod lifecycle event, including loadComplete.
        // activeModContainer() guesses from the call stack when no event is active; missing context stays unknown.
        SOURCES.put(research, ThreadContext.get("mod"));
    }

    public static String sourceOf(ResearchItem research) {
        return SOURCES.get(research);
    }
}
