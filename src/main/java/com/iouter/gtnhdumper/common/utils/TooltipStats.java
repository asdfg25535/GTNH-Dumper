package com.iouter.gtnhdumper.common.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iouter.gtnhdumper.GTNHDumper;

import cpw.mods.fml.common.Loader;

/** Exports the attributes that Chromatic Tooltips adds outside ItemStack.getTooltip(). */
public final class TooltipStats {

    private TooltipStats() {}

    public static List<JsonObject> get(ItemStack stack) {
        if (!Loader.isModLoaded("chromatictooltips")) {
            return Collections.emptyList();
        }
        try {
            return Chromatic.get(stack);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            GTNHDumper.LOG.warn("Could not export Chromatic Tooltips attributes for {}", stack, e);
            return Collections.emptyList();
        }
    }

    /** Uses the optional mod's public API without requiring its classes on the classpath. */
    private static final class Chromatic {

        private static final Gson GSON = new Gson();
        private static final String PACKAGE = "com.slprime.chromatictooltips.";
        private static final Method CREATE_TARGET;
        private static final Method GET_STATS;
        private static final Class<?> STATS_TYPE;

        static {
            try {
                Class<?> target = Class.forName(PACKAGE + "api.TooltipTarget");
                CREATE_TARGET = target.getMethod("ofItem", ItemStack.class);
                GET_STATS = Class.forName(PACKAGE + "enricher.ItemStatsEnricher")
                    .getMethod("getAttributeModifiers", target);
                STATS_TYPE = Class.forName(PACKAGE + "api.ItemStats");
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private static List<JsonObject> get(ItemStack stack) throws ReflectiveOperationException {
            List<JsonObject> result = new ArrayList<>();
            Object target = CREATE_TARGET.invoke(null, stack);
            for (Object stat : (List<?>) GET_STATS.invoke(null, target)) {
                // Serialize only the base API fields, not arbitrary fields from mod-specific subclasses.
                JsonObject data = GSON.toJsonTree(stat, STATS_TYPE)
                    .getAsJsonObject();
                if (data.has("icon")) {
                    String icon = data.get("icon")
                        .getAsString();
                    if (!icon.contains(":")) {
                        data.addProperty("icon", "chromatictooltips:" + icon);
                    }
                }
                result.add(data);
            }
            return result;
        }
    }
}
