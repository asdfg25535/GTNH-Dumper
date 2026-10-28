package com.iouter.gtnhdumper.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.ThreadContext;

import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;

/** Use Thaumcraft's real registration rules with synthetic mod IDs; no addons or client are needed. */
public final class ResearchSourcesRegression {

    private ResearchSourcesRegression() {}

    public static void main(String[] args) {
        // Rejected coordinate collisions use FML's logger, whose side is normally set by LaunchWrapper.
        ReflectionHelper.setPrivateValue(FMLRelaunchLog.class, null, Side.CLIENT, "side");
        ResearchCategories.registerCategory("SHARED", null, null);
        ResearchCategories.registerCategory("OTHER", null, null);
        ResearchItem base = new ResearchItem("BASE", "SHARED");
        ResearchItem addon = new ResearchItem("ADDON", "SHARED");
        register(base, "base");
        register(addon, "addon");
        check("base".equals(ResearchSources.sourceOf(base)), "Base registration keeps its source");
        check("addon".equals(ResearchSources.sourceOf(addon)), "Shared categories allow different owners");

        ResearchItem duplicate = new ResearchItem("BASE", "SHARED");
        register(duplicate, "addon");
        register(base, "addon");
        check(ResearchSources.sourceOf(duplicate) == null, "Rejected duplicate is not recorded");
        check("base".equals(ResearchSources.sourceOf(base)), "Repeated object registration preserves the first source");
        ResearchItem otherCategory = new ResearchItem("BASE", "OTHER");
        register(otherCategory, "addon");
        check(
            "addon".equals(ResearchSources.sourceOf(otherCategory)),
            "Identical keys in distinct categories stay separate");

        ResearchItem missingCategory = new ResearchItem("MISSING", "ABSENT");
        register(missingCategory, "addon");
        check(ResearchSources.sourceOf(missingCategory) == null, "Missing category rejects registration");
        ResearchItem positioned = new ResearchItem(
            "POSITIONED",
            "SHARED",
            new AspectList(),
            1,
            1,
            1,
            (ResourceLocation) null);
        ResearchItem overlap = new ResearchItem(
            "OVERLAP",
            "SHARED",
            new AspectList(),
            1,
            1,
            1,
            (ResourceLocation) null);
        register(positioned, "base");
        register(overlap, "addon");
        check(ResearchSources.sourceOf(overlap) == null, "Rejected coordinate collision is not recorded");

        ResearchItem unknown = new ResearchItem("UNKNOWN", "SHARED");
        register(unknown, null);
        register(unknown, "addon");
        check(
            ResearchSources.sourceOf(unknown) == null,
            "Missing lifecycle context stays unknown after a repeated call");
        Map<String, String> mods = new LinkedHashMap<>();
        mods.put("base", "Base");
        mods.put("addon", "Addon");
        ModFilter selected = new ModFilter("addon", mods);
        check(selected.matchesMod(ResearchSources.sourceOf(addon)), "Selected registrations are included");
        check(!selected.matchesMod(ResearchSources.sourceOf(base)), "Shared category does not include other mods");
        check(!selected.matchesMod(ResearchSources.sourceOf(unknown)), "Filtered export excludes unknown sources");
        check(
            new ModFilter("", mods).matchesMod(ResearchSources.sourceOf(unknown)),
            "Full export retains unknown research");
        System.out.println("Research registration source regression checks passed");
    }

    private static void register(ResearchItem research, String modId) {
        if (modId == null) ThreadContext.remove("mod");
        else ThreadContext.put("mod", modId);
        try {
            ResearchCategories.addResearch(research);
            // The late mixin invokes this after every addResearch return.
            ResearchSources.capture(research);
        } finally {
            ThreadContext.remove("mod");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
