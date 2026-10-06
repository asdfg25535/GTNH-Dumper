package com.iouter.gtnhdumper.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.iouter.gtnhdumper.GTNHDumper;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IChromosome;

/** Exports the displayed names of a bee item's active genes as flat item attributes. */
public final class BeeExportData {

    private BeeExportData() {}

    public static Map<String, Object> itemAttributes(ItemStack stack) {
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
            IBee bee = root.getMember(stack.copy());
            IChromosome[] chromosomes = bee.getGenome()
                .getChromosomes();
            Map<String, Object> result = new LinkedHashMap<>();
            for (EnumBeeChromosome type : EnumBeeChromosome.values()) {
                int index = type.ordinal();
                if (index >= chromosomes.length || chromosomes[index] == null) continue;
                IAllele active = chromosomes[index].getActiveAllele();
                if (active != null) result.put(type.name(), active.getName());
            }
            return result.isEmpty() ? null : result;
        } catch (RuntimeException | LinkageError e) {
            GTNHDumper.LOG.warn("Cannot extract bee attributes for " + Utils.getItemKey(stack), e);
            return null;
        }
    }
}
