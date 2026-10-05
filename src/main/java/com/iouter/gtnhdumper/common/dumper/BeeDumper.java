package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.utils.BeeExportData;
import com.iouter.gtnhdumper.common.utils.Utils;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IAlleleBeeSpeciesCustom;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.apiculture.IJubilanceProvider;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IClassification;

public class BeeDumper extends WikiDumper {

    public BeeDumper() {
        super("tools.dump.gtnhdumper.bee");
    }

    @Override
    public int getKeyIndex() {
        return 0;
    }

    @Override
    public String getKeyStr() {
        return "bees";
    }

    @Override
    public String[] header() {
        return new String[] { "uid", "originalName", "localizedName", "binomial", "authority", "branch", "dominant",
            "secret", "counted", "blacklisted", "temperature", "humidity", "nocturnal", "hasEffect", "complexity",
            "hasTemplate", "genome", "items", "products", "specialties", "jubilance", "mutations" };
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        IBeeRoot root = BeeManager.beeRoot;
        if (root == null || AlleleManager.alleleRegistry == null) {
            throw new IllegalStateException("Forestry apiculture is not available");
        }

        Map<String, IAlleleBeeSpecies> speciesByUid = new TreeMap<>();
        for (IAllele allele : AlleleManager.alleleRegistry.getRegisteredAlleles()
            .values()) {
            if (allele instanceof IAlleleBeeSpecies species) speciesByUid.put(species.getUID(), species);
        }
        Map<String, String> englishNames = new LinkedHashMap<>();
        Utils.getEnglishTranslation(() -> {
            for (IAlleleBeeSpecies species : speciesByUid.values()) {
                englishNames.put(species.getUID(), species.getName());
            }
        });

        Map<String, List<Map<String, Object>>> mutations = BeeExportData.mutationsByResult(root.getMutations(false));
        List<Object[]> rows = new ArrayList<>();
        for (IAlleleBeeSpecies species : speciesByUid.values()) {
            String uid = species.getUID();
            IAllele[] template = root.getTemplate(uid);
            rows.add(
                new Object[] { uid, englishNames.get(uid), species.getName(), species.getBinomial(),
                    species.getAuthority(), branch(species.getBranch()), species.isDominant(), species.isSecret(),
                    species.isCounted(), AlleleManager.alleleRegistry.isBlacklisted(uid), species.getTemperature()
                        .name(),
                    species.getHumidity()
                        .name(),
                    species.isNocturnal(), species.hasEffect(), species.getComplexity(), template != null,
                    BeeExportData.genome(template), beeItems(root, template), products(species.getProductChances()),
                    products(species.getSpecialtyChances()), jubilance(species),
                    mutations.getOrDefault(uid, Collections.emptyList()) });
        }
        return rows;
    }

    @Override
    public void dumpTo(File file) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        String[] columns = header();
        for (Object[] row : dumpObject(getMode())) {
            Map<String, Object> data = new LinkedHashMap<>();
            for (int i = 0; i < columns.length; i++) {
                data.put(columns[i], row[i]);
            }
            list.add(data);
            map.put((String) row[getKeyIndex()], data);
        }
        Object document = getMode() == 0 ? Collections.singletonMap(getKeyStr(), list) : map;
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            GTNHDumper.GSON.toJson(document, writer);
        }
    }

    private static Map<String, Object> branch(IClassification branch) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (branch != null) {
            result.put("uid", branch.getUID());
            result.put("name", branch.getName());
            result.put("scientific", branch.getScientific());
        }
        return result;
    }

    private static Map<String, Object> jubilance(IAlleleBeeSpecies species) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (species instanceof IAlleleBeeSpeciesCustom custom) {
            IJubilanceProvider provider = custom.getJubilanceProvider();
            if (provider != null) {
                result.put(
                    "provider",
                    provider.getClass()
                        .getName());
                try {
                    result.put("description", provider.getDescription());
                } catch (RuntimeException | LinkageError e) {
                    result.put("descriptionError", e.toString());
                    GTNHDumper.LOG.warn("Cannot describe jubilance for " + species.getUID(), e);
                }
            }
        }
        return result;
    }

    private static Map<String, RecipeItem> beeItems(IBeeRoot root, IAllele[] template) {
        Map<String, RecipeItem> result = new LinkedHashMap<>();
        if (template == null) return result;
        IBee bee = root.templateAsIndividual(template);
        bee.analyze();
        for (EnumBeeType type : new EnumBeeType[] { EnumBeeType.DRONE, EnumBeeType.PRINCESS, EnumBeeType.QUEEN }) {
            ItemStack stack = root.getMemberStack(bee, type.ordinal());
            if (stack != null) result.put(type.name(), new RecipeItem(stack));
        }
        return result;
    }

    private static List<Map<String, Object>> products(Map<ItemStack, Float> products) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<ItemStack, Float> entry : products.entrySet()) {
            Map<String, Object> product = new LinkedHashMap<>();
            product.put("item", new RecipeItem(entry.getKey()));
            // Keep the raw per-cycle probability; do not fold speed or housing modifiers into it.
            product.put("baseChance", entry.getValue());
            result.add(product);
        }
        result.sort(Comparator.comparing(product -> {
            RecipeItem item = (RecipeItem) product.get("item");
            return item.key + ":" + item.nbt + ":" + item.amount + ":" + product.get("baseChance");
        }));
        return result;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation("nei.options.tools.dump.gtnhdumper.bee.dumped", "dumps/" + file.getName());
    }
}
