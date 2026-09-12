package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;

/** Adds KubaTech's final Extreme Entity Crusher recipe map to the normal recipe dump. */
public final class EECRecipeDumper {

    public static final String RECIPE_NAME = "Extreme Entity Crusher";
    private static final File OUTPUT_FILE = new File("dumps/recipes/kubatech/ExtremeEntityCrusher.json");

    private EECRecipeDumper() {}

    public static void dump() throws Exception {
        File parent = OUTPUT_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(OUTPUT_FILE)) {
            GTNHDumper.GSON.toJson(build(), writer);
        }
        GTNHDumper.info("已写入：" + OUTPUT_FILE.getAbsolutePath());
    }

    static JsonObject build() throws Exception {
        Map<?, ?> eecRecipes = (Map<?, ?>) field(Class.forName("kubatech.loaders.MobHandlerLoader"), "recipeMap");
        List<EECRecipe> recipes = new ArrayList<>();
        Map<String, Object> sortedRecipes = new TreeMap<>();
        for (Map.Entry<?, ?> entry : eecRecipes.entrySet()) {
            sortedRecipes.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        for (Object eecRecipe : sortedRecipes.values()) {
            Object mobRecipe = field(eecRecipe, "recipe");
            ItemStack spawnEgg = getSpawnEgg((Entity) field(mobRecipe, "entity"));
            if (spawnEgg == null) {
                continue;
            }

            ArrayList<Object> inputItems = new ArrayList<>();
            inputItems.add(new RecipeItem(spawnEgg));
            ArrayList<Object> outputItems = new ArrayList<>();
            for (Object drop : (List<?>) field(eecRecipe, "mOutputs")) {
                ItemStack stack = ((ItemStack) field(drop, "stack")).copy();
                Object damages = field(drop, "damages");
                if (stack.isItemStackDamageable() && damages instanceof Map && ((Map<?, ?>) damages).size() > 1) {
                    stack.setItemDamage(0);
                }
                int chance = ((Number) field(drop, "chance")).intValue();
                outputItems.add(new RecipeItem(stack).withChance(chance));
            }
            recipes.add(
                new EECRecipe(
                    inputItems,
                    outputItems,
                    ((Number) field(eecRecipe, "mEUt")).intValue(),
                    ((Number) field(eecRecipe, "mDuration")).intValue(),
                    ((Number) field(mobRecipe, "maxEntityHealth")).floatValue(),
                    getInfernalStatus(
                        (Boolean) field(mobRecipe, "alwaysinfernal"),
                        (Boolean) field(mobRecipe, "infernalityAllowed"))));
        }

        JsonObject result = new JsonObject();
        result.addProperty("name", RECIPE_NAME);
        result.add("catalysts", new JsonArray());
        result.addProperty("identifier", "kubatech.eec");
        result.addProperty("source", "kubatech.loaders.MobHandlerLoader.recipeMap");
        result.addProperty("markedItem", "null");
        result.add("recipes", GTNHDumper.GSON.toJsonTree(recipes));
        return result;
    }

    private static ItemStack getSpawnEgg(Entity entity) {
        int entityId = EntityList.getEntityID(entity);
        if (entityId <= 0) {
            return null;
        }
        return new ItemStack(Items.spawn_egg, 1, entityId);
    }

    private static String getInfernalStatus(boolean alwaysInfernal, boolean infernalityAllowed) {
        if (alwaysInfernal) {
            return "always";
        }
        return infernalityAllowed ? "allowed" : "never";
    }

    private static Object field(Object target, String... names) throws Exception {
        Class<?> start = target instanceof Class ? (Class<?>) target : target.getClass();
        for (String name : names) {
            for (Class<?> current = start; current != null; current = current.getSuperclass()) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    return field.get(target instanceof Class ? null : target);
                } catch (NoSuchFieldException ignored) {}
            }
        }
        throw new NoSuchFieldException(start.getName() + " " + Arrays.toString(names));
    }

    private static class EECRecipe {

        private final ArrayList<Object> inputItems;
        private final ArrayList<RecipeFluid> inputFluids = new ArrayList<>();
        private final ArrayList<Object> outputItems;
        private final ArrayList<RecipeFluid> outputFluids = new ArrayList<>();
        private final ArrayList<Object> otherItems = new ArrayList<>();
        private final int eut;
        private final int duration;
        private final float health;
        private final String infernalstatus;

        private EECRecipe(ArrayList<Object> inputItems, ArrayList<Object> outputItems, int eut, int duration,
            float health, String infernalstatus) {
            this.inputItems = inputItems;
            this.outputItems = outputItems;
            this.eut = eut;
            this.duration = duration;
            this.health = health;
            this.infernalstatus = infernalstatus;
        }
    }
}
