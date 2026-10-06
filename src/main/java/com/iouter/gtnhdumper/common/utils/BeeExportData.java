package com.iouter.gtnhdumper.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.iouter.gtnhdumper.GTNHDumper;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IAlleleArea;
import forestry.api.genetics.IAlleleBoolean;
import forestry.api.genetics.IAlleleFloat;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IAlleleInteger;
import forestry.api.genetics.IAlleleTolerance;
import forestry.api.genetics.IChromosome;
import forestry.api.genetics.IMutation;

/** Converts Forestry's API objects to JSON data without serializing implementation internals. */
public final class BeeExportData {

    private BeeExportData() {}

    public static Map<String, Object> itemMetadata(ItemStack stack) {
        IBeeRoot root = BeeManager.beeRoot;
        if (stack == null || root == null || !root.isMember(stack)) return null;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(
            "beeType",
            root.getType(stack)
                .name());
        try {
            // An untagged bee is not evidence of the default Forest genome.
            if (!stack.hasTagCompound() || stack.getTagCompound()
                .getCompoundTag("Genome")
                .getTagList("Chromosomes", 10)
                .tagCount() == 0) {
                throw new IllegalArgumentException("Missing bee genome in item NBT");
            }
            // Forestry can repair invalid chromosomes while reading NBT; never modify the source stack.
            IBee bee = root.getMember(stack.copy());
            result.putAll(individualGenome(bee.getGenome()));
            if (bee.getMate() != null) result.put("mate", individualGenome(bee.getMate()));
        } catch (RuntimeException | LinkageError e) {
            result.clear();
            result.put(
                "beeType",
                root.getType(stack)
                    .name());
            result.put("parseError", e.toString());
            GTNHDumper.LOG.warn("Cannot extract bee metadata for " + Utils.getItemKey(stack), e);
        }
        return result;
    }

    private static Map<String, Object> individualGenome(IBeeGenome genome) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> species = new LinkedHashMap<>();
        species.put(
            "active",
            genome.getPrimary()
                .getUID());
        species.put(
            "inactive",
            genome.getSecondary()
                .getUID());
        result.put("species", species);
        Map<String, Object> genes = new LinkedHashMap<>();
        IChromosome[] chromosomes = genome.getChromosomes();
        for (EnumBeeChromosome type : EnumBeeChromosome.values()) {
            int index = type.ordinal();
            if (index >= chromosomes.length || chromosomes[index] == null) continue;
            IChromosome chromosome = chromosomes[index];
            Map<String, Object> pair = new LinkedHashMap<>();
            pair.put("active", allele(chromosome.getActiveAllele()));
            pair.put("inactive", allele(chromosome.getInactiveAllele()));
            genes.put(type.name(), pair);
        }
        result.put("genome", genes);
        return result;
    }

    public static Map<String, Object> genome(IAllele[] template) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (template == null) return result;
        for (EnumBeeChromosome chromosome : EnumBeeChromosome.values()) {
            int index = chromosome.ordinal();
            if (index >= template.length || template[index] == null) continue;
            result.put(chromosome.name(), allele(template[index]));
        }
        return result;
    }

    private static Map<String, Object> allele(IAllele allele) {
        if (allele == null) return null;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("uid", allele.getUID());
        data.put("name", allele.getName());
        data.put("dominant", allele.isDominant());
        if (allele instanceof IAlleleBoolean value) data.put("value", value.getValue());
        else if (allele instanceof IAlleleInteger value) data.put("value", value.getValue());
        else if (allele instanceof IAlleleFloat value) data.put("value", value.getValue());
        else if (allele instanceof IAlleleArea value) data.put(
            "value",
            value.getValue()
                .clone());
        else if (allele instanceof IAlleleTolerance value) data.put(
            "value",
            value.getValue()
                .name());
        else if (allele instanceof IAlleleFlowers flowers) {
            data.put(
                "flowerType",
                flowers.getProvider()
                    .getFlowerType());
            data.put(
                "description",
                flowers.getProvider()
                    .getDescription());
        }
        return data;
    }

    public static Map<String, Object> mutation(IMutation mutation) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(
            "parent1",
            mutation.getAllele0()
                .getUID());
        result.put(
            "parent2",
            mutation.getAllele1()
                .getUID());
        result.put("result", mutation.getTemplate()[EnumBeeChromosome.SPECIES.ordinal()].getUID());
        // Forestry already expresses this value as a percentage, including fractional percentages.
        result.put("baseChancePercent", mutation.getBaseChance());
        result.put("secret", mutation.isSecret());
        try {
            Collection<String> conditions = mutation.getSpecialConditions();
            result.put("requirements", conditions == null ? new ArrayList<>() : new ArrayList<>(conditions));
        } catch (RuntimeException | LinkageError e) {
            // Some addon implementations cannot describe their conditions outside a housing.
            // Keep the mutation and make the missing conditions visible in the dump.
            result.put("requirementsError", e.toString());
        }
        return result;
    }

    public static Map<String, List<Map<String, Object>>> mutationsByResult(Collection<? extends IMutation> mutations) {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (IMutation mutation : mutations) {
            Map<String, Object> data = mutation(mutation);
            String uid = (String) data.get("result");
            result.computeIfAbsent(uid, ignored -> new ArrayList<>())
                .add(data);
        }
        return result;
    }
}
