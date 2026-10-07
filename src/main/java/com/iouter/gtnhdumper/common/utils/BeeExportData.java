package com.iouter.gtnhdumper.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.iouter.gtnhdumper.GTNHDumper;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IChromosome;

/** Exports the active genes and stable identity of a bee item. */
public final class BeeExportData {

    private BeeExportData() {}

    public static ExportData extract(ItemStack stack) {
        IBeeRoot root = BeeManager.beeRoot;
        if (stack == null || root == null || !root.isMember(stack)) return null;
        try {
            // An untagged bee is not evidence of the default Forest genome.
            if (!stack.hasTagCompound() || stack.getTagCompound()
                .getCompoundTag("Genome")
                .getTagList("Chromosomes", 10)
                .tagCount() == 0) {
                throw new IllegalArgumentException("Missing bee genome in item NBT");
            }
            // Forestry can repair NBT while reading; preserve the original item and its NBT.
            ItemStack copy = stack.copy();
            IBee bee = root.getMember(copy);
            IChromosome[] chromosomes = bee.getGenome()
                .getChromosomes();
            Map<String, Object> attributes = new LinkedHashMap<>();
            for (EnumBeeChromosome type : EnumBeeChromosome.values()) {
                int index = type.ordinal();
                if (index >= chromosomes.length || chromosomes[index] == null) continue;
                IAllele active = chromosomes[index].getActiveAllele();
                if (active != null) attributes.put(type.name(), active.getName());
            }

            IAllele species = bee.getGenome()
                .getPrimary();
            EnumBeeType beeType = root.getType(copy);
            if (species == null || species.getUID() == null
                || species.getUID()
                    .isEmpty()
                || beeType == null
                || beeType == EnumBeeType.NONE) {
                throw new IllegalArgumentException("Incomplete bee identity");
            }

            Map<String, Object> identity = new LinkedHashMap<>();
            identity.put("speciesUid", species.getUID());
            identity.put("type", beeType.name());
            return new ExportData(attributes.isEmpty() ? null : attributes, identity);
        } catch (RuntimeException | LinkageError e) {
            GTNHDumper.LOG.warn("Cannot extract bee export data for " + Utils.getItemKey(stack), e);
            return null;
        }
    }

    public static final class ExportData {

        public final Map<String, Object> attributes;
        public final Map<String, Object> identity;

        private ExportData(Map<String, Object> attributes, Map<String, Object> identity) {
            this.attributes = attributes;
            this.identity = identity;
        }
    }
}
