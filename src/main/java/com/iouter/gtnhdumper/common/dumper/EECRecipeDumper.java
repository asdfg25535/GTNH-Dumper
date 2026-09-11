package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import net.minecraft.util.ChatComponentTranslation;

import com.iouter.gtnhdumper.GTNHDumper;

import codechicken.nei.NEIClientUtils;
import codechicken.nei.config.DataDumper;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/** Exports the final recipe map used by KubaTech's Extreme Entity Crusher. */
public class EECRecipeDumper extends DataDumper {

    private static final String EXPORTER_VERSION = "1.0.1";
    private static final Path OUTPUT_DIRECTORY = new File("dumps/eec").toPath();
    private static final Path OUTPUT_FILE = OUTPUT_DIRECTORY.resolve("eec-runtime.json");
    private static final Path TEMPORARY_FILE = OUTPUT_DIRECTORY.resolve("eec-runtime.json.tmp");
    private static final Path STATUS_FILE = OUTPUT_DIRECTORY.resolve("eec-status.txt");
    private static final Path ERROR_FILE = OUTPUT_DIRECTORY.resolve("eec-error.txt");

    private static String stage = "starting";

    public EECRecipeDumper() {
        super("tools.dump.gtnhdumper.eecrecipe");
    }

    @Override
    public String[] header() {
        return null;
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        return null;
    }

    @Override
    public void dumpFile() {
        try {
            stage = "starting";
            Files.createDirectories(OUTPUT_DIRECTORY);
            writeStatus("Exporting recipes...");
            Map<String, Object> result = export();
            Files.write(
                TEMPORARY_FILE,
                GTNHDumper.GSON.toJson(result)
                    .getBytes(StandardCharsets.UTF_8));
            Files.move(TEMPORARY_FILE, OUTPUT_FILE, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(ERROR_FILE);
            writeStatus(
                "SUCCESS: " + new Date()
                    + "\nRecipes: "
                    + ((List<?>) result.get("mobs")).size()
                    + "\nFile: eec-runtime.json");
            GTNHDumper.info("EEC recipes written to " + OUTPUT_FILE.toAbsolutePath());
            NEIClientUtils.printChatMessage(dumpMessage(OUTPUT_FILE.toFile()));
        } catch (Throwable error) {
            GTNHDumper.LOG.error("EEC recipe export failed; an existing JSON file was left untouched", error);
            writeFailure(error);
            NEIClientUtils.printChatMessage(
                new ChatComponentTranslation(
                    "nei.options.tools.dump.gtnhdumper.eecrecipe.failed",
                    ERROR_FILE.toString()));
        } finally {
            try {
                Files.deleteIfExists(TEMPORARY_FILE);
            } catch (IOException error) {
                GTNHDumper.LOG.warn("Could not remove temporary EEC export file", error);
            }
        }
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation("nei.options.tools.dump.gtnhdumper.eecrecipe.dumped", file.getPath());
    }

    @Override
    public int modeCount() {
        return 1;
    }

    static Map<String, Object> export() throws Exception {
        Map<String, Object> result = map(
            "schema",
            "gtnh-eec-v1",
            "generatedAt",
            new Date().toString(),
            "source",
            "kubatech.loaders.MobHandlerLoader.recipeMap",
            "targetVersion",
            "2.9.0-beta-3",
            "exporterVersion",
            EXPORTER_VERSION);

        stage = "reading EEC recipeMap";
        Map<?, ?> eecRecipes = (Map<?, ?>) field(Class.forName("kubatech.loaders.MobHandlerLoader"), "recipeMap");
        if (eecRecipes.isEmpty()) {
            throw new IllegalStateException("EEC recipe map is empty; no data file was produced.");
        }

        stage = "reading Mobs Info api.MobRecipe.MobNameToRecipeMap";
        Map<?, ?> allMobRecipes = (Map<?, ?>) field(
            Class.forName("com.kuba6000.mobsinfo.api.MobRecipe"),
            "MobNameToRecipeMap");

        Map<String, Object> versions = new TreeMap<>();
        for (ModContainer mod : Loader.instance()
            .getActiveModList()) {
            versions.put(mod.getModId(), mod.getVersion());
        }
        result.put("mods", versions);
        result.put(
            "playerOnlyModifier",
            field(Class.forName("kubatech.config.Config$MobHandler"), "playerOnlyDropsModifier"));

        TreeSet<String> ids = new TreeSet<>();
        for (Object key : allMobRecipes.keySet()) {
            ids.add(String.valueOf(key));
        }
        for (Object key : eecRecipes.keySet()) {
            ids.add(String.valueOf(key));
        }

        List<Object> mobs = new ArrayList<>();
        for (String id : ids) {
            stage = "exporting mob " + id;
            Object eecRecipe = eecRecipes.get(id);
            Object recipe = eecRecipe == null ? allMobRecipes.get(id) : field(eecRecipe, "recipe");
            Object entity = field(recipe, "entity");
            Map<String, Object> mob = map(
                "id",
                id,
                "name",
                tryInvoke(entity, new String[] { "getCommandSenderName", "func_70005_c_" }, id),
                "usable",
                eecRecipe != null,
                "health",
                field(recipe, "maxEntityHealth"),
                "peacefulAllowed",
                field(recipe, "isPeacefulAllowed"),
                "alwaysinfernal",
                field(recipe, "alwaysinfernal"),
                "infernalityAllowed",
                field(recipe, "infernalityAllowed"),
                "maxDamageChance",
                field(recipe, "mMaxDamageChance"));
            Object aspects = invoke(
                Class.forName("thaumcraft.common.lib.research.ScanManager"),
                "generateEntityAspects",
                entity);
            mob.put("aspects", aspectList(aspects));

            List<Object> drops = new ArrayList<>();
            Object dropSource = eecRecipe == null ? recipe : eecRecipe;
            for (Object drop : (List<?>) field(dropSource, "mOutputs")) {
                stage = "exporting mob " + id + ", drop " + drops.size();
                Object stack = field(drop, "stack");
                Map<String, Object> record = stack(stack);
                Object damages = field(drop, "damages");
                record.put("chance", field(drop, "chance"));
                record.put("lootable", field(drop, "lootable"));
                record.put("playerOnly", field(drop, "playerOnly"));
                record.put("enchantable", field(drop, "enchantable"));
                record.put("damages", damages);
                record.put("maxDamageChance", field(recipe, "mMaxDamageChance"));
                List<Object> damageOrder = new ArrayList<>();
                if (damages != null) {
                    for (Map.Entry<?, ?> damage : ((Map<?, ?>) damages).entrySet()) {
                        damageOrder.add(Arrays.asList(damage.getKey(), damage.getValue()));
                    }
                }
                record.put("damageOrder", damageOrder);
                record.put("type", String.valueOf(field(drop, "type")));
                record.put(
                    "damaged",
                    Boolean.TRUE.equals(tryInvoke(stack, new String[] { "isItemDamaged", "func_77951_h" }, false)));
                record.put(
                    "damageable",
                    Boolean.TRUE
                        .equals(tryInvoke(stack, new String[] { "isItemStackDamageable", "func_77984_f" }, false)));
                List<Object> modifiers = new ArrayList<>();
                for (Object modifier : (List<?>) field(drop, "chanceModifiers")) {
                    modifiers.add(modifier(modifier));
                }
                record.put("modifiers", modifiers);
                record.put("additionalInfo", field(drop, "additionalInfo"));
                drops.add(record);
            }
            mob.put("drops", drops);
            mobs.add(mob);
        }
        result.put("mobs", mobs);

        stage = "exporting enchantment registry";
        List<Object> enchantments = new ArrayList<>();
        Object enchantmentArray = field(
            Class.forName("net.minecraft.enchantment.Enchantment"),
            "enchantmentsList",
            "field_77331_b");
        for (int index = 0; index < Array.getLength(enchantmentArray); index++) {
            Object enchantment = Array.get(enchantmentArray, index);
            if (enchantment != null) {
                enchantments.add(enchantment(enchantment));
            }
        }
        result.put("enchantments", enchantments);
        return result;
    }

    private static Map<String, Object> modifier(Object modifier) throws Exception {
        String type = modifier.getClass()
            .getSimpleName();
        Map<String, Object> result = map(
            "type",
            type,
            "class",
            modifier.getClass()
                .getName(),
            "description",
            tryInvoke(modifier, new String[] { "getDescription" }, type));
        if (type.equals("NormalChance") || type.equals("BaseChance")) {
            result.put("chance", field(modifier, "chance"));
        } else if (type.equals("DropsOnlyWithEnchant")) {
            Object data = field(modifier, "enchantmentData");
            Object enchantment = field(data, "enchantmentobj", "field_76302_b");
            result.put("enchantmentId", field(enchantment, "effectId", "field_77352_x"));
            result.put("level", field(data, "enchantmentLevel", "field_76303_c"));
        } else if (type.equals("EachLevelOfGives")) {
            result.put("enchantmentId", field(field(modifier, "enchantment"), "effectId", "field_77352_x"));
            result.put("change", field(modifier, "change"));
        } else if (type.equals("DropsOnlyInDimension")) {
            result.put("dimension", field(modifier, "dimension"));
        } else if (type.equals("OrBiome")) {
            result.put("biomeId", field(field(modifier, "biome"), "biomeID", "field_76756_M"));
            result.put("newChance", field(modifier, "newChance"));
        } else if (type.equals("DropsOnlyUsing") || type.equals("OrUsing")) {
            Map<String, Object> weapon = stack(field(modifier, "weapon"));
            result.put("weapon", weapon.get("item"));
            result.put("weaponName", weapon.get("name"));
            if (type.equals("OrUsing")) {
                result.put("newChance", field(modifier, "newChance"));
            }
        }
        // Unknown conditions retain their runtime type and description instead of becoming unconditional.
        return result;
    }

    private static Map<String, Object> enchantment(Object enchantment) throws Exception {
        Object key = invokeAny(enchantment, new String[] { "getName", "func_77320_a" });
        Object name = tryInvoke(
            Class.forName("net.minecraft.util.StatCollector"),
            new String[] { "translateToLocal", "func_74838_a" },
            key,
            key);
        return map(
            "id",
            field(enchantment, "effectId", "field_77352_x"),
            "key",
            key,
            "name",
            name,
            "maxLevel",
            invokeAny(enchantment, new String[] { "getMaxLevel", "func_77325_b" }));
    }

    private static Map<String, Object> stack(Object stack) throws Exception {
        Object item = invokeAny(stack, new String[] { "getItem", "func_77973_b" });
        Object registry = field(Class.forName("net.minecraft.item.Item"), "itemRegistry", "field_150901_e");
        String id = String.valueOf(invokeAny(registry, new String[] { "getNameForObject", "func_148750_c" }, item));
        Object metadata = invokeAny(stack, new String[] { "getItemDamage", "func_77960_j" });
        Object tag = invokeAny(stack, new String[] { "getTagCompound", "func_77978_p" });
        String nbt = tag == null ? "" : tag.toString();
        Object name = invokeAny(stack, new String[] { "getDisplayName", "func_82833_r" });
        Map<String, Object> result = map(
            "key",
            id + "@" + metadata + "|" + nbt,
            "item",
            id,
            "meta",
            metadata,
            "nbt",
            nbt,
            "name",
            name,
            "size",
            field(stack, "stackSize", "field_77994_a"));
        if (item.getClass()
            .getName()
            .equals("thaumcraft.common.items.ItemCrystalEssence")) {
            List<Object> aspects = aspectList(invoke(item, "getAspects", stack));
            result.put("crystalAspects", aspects);
            if (aspects.size() == 1) {
                result.put("aspect", ((Map<?, ?>) aspects.get(0)).get("tag"));
            }
        }
        return result;
    }

    private static List<Object> aspectList(Object aspectList) throws Exception {
        List<Object> result = new ArrayList<>();
        if (aspectList == null) {
            return result;
        }
        Object aspects = invoke(aspectList, "getAspects");
        for (int index = 0; index < Array.getLength(aspects); index++) {
            Object aspect = Array.get(aspects, index);
            if (aspect != null) {
                result.add(
                    map(
                        "tag",
                        invoke(aspect, "getTag"),
                        "name",
                        invoke(aspect, "getName"),
                        "amount",
                        invoke(aspectList, "getAmount", aspect),
                        "index",
                        index));
            }
        }
        return result;
    }

    private static Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            result.put(String.valueOf(pairs[index]), pairs[index + 1]);
        }
        return result;
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

    private static Object invoke(Object target, String name, Object... arguments) throws Exception {
        return invokeAny(target, new String[] { name }, arguments);
    }

    private static Object tryInvoke(Object target, String[] names, Object fallback, Object... arguments) {
        try {
            return invokeAny(target, names, arguments);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static Object invokeAny(Object target, String[] names, Object... arguments) throws Exception {
        Class<?> type = target instanceof Class ? (Class<?>) target : target.getClass();
        for (String name : names) {
            for (Method method : type.getMethods()) {
                if (method.getName()
                    .equals(name) && method.getParameterTypes().length == arguments.length
                    && parametersMatch(method.getParameterTypes(), arguments)) {
                    return method.invoke(target instanceof Class ? null : target, arguments);
                }
            }
        }
        throw new NoSuchMethodException(type.getName() + " " + Arrays.toString(names));
    }

    private static boolean parametersMatch(Class<?>[] parameters, Object[] arguments) {
        for (int index = 0; index < arguments.length; index++) {
            if (arguments[index] != null && !parameters[index].isPrimitive()
                && !parameters[index].isInstance(arguments[index])) {
                return false;
            }
        }
        return true;
    }

    private static void writeStatus(String message) throws IOException {
        String status = "EEC Recipe Export " + EXPORTER_VERSION + "\n" + message + "\n";
        Files.write(STATUS_FILE, status.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeFailure(Throwable error) {
        try {
            Files.createDirectories(OUTPUT_DIRECTORY);
            StringWriter trace = new StringWriter();
            error.printStackTrace(new PrintWriter(trace));
            String message = "FAILED: " + new Date()
                + "\nStage: "
                + stage
                + "\nAn existing JSON file, if any, is from an earlier successful run.\n\n"
                + trace;
            String diagnostic = "EEC Recipe Export " + EXPORTER_VERSION + "\n" + message;
            byte[] bytes = diagnostic.getBytes(StandardCharsets.UTF_8);
            Files.write(ERROR_FILE, bytes);
            Files.write(STATUS_FILE, bytes);
        } catch (IOException diagnosticError) {
            GTNHDumper.LOG.error("Could not persist EEC export diagnostics", diagnosticError);
        }
    }
}
