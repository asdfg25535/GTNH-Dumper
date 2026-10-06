package com.iouter.gtnhdumper.common.recipe.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.item.ItemStack;

import com.iouter.gtnhdumper.common.utils.Utils;

public class RecipeItem {

    public static final Map<String, String[]> oreDictMap = Utils.getOreDict();

    public String key;
    public Long amount;
    public Object chance;
    public String nbt;
    public Map<String, Object> metadata;
    public List<String> tooltip;

    public RecipeItem(String key, long amount) {
        this.key = key;
        if (amount != 1) {
            this.amount = amount;
        } else {
            this.amount = null;
        }
    }

    public RecipeItem(ItemStack stack) {
        if (stack != null) {
            this.key = Utils.getItemKey(stack);
            int amount = stack.stackSize;
            if (amount != 1) {
                this.amount = (long) amount;
            } else {
                this.amount = null;
            }
        } else {
            this.key = null;
            this.amount = null;
        }
        this.withNBT(stack);
    }

    public RecipeItem withChance(int chance) {
        if (chance != 10000) {
            this.chance = chance;
        }
        return this;

    }

    public RecipeItem withNBT(String nbt) {
        // Metadata from a previous stack must not describe a replacement NBT string.
        if (!Objects.equals(this.nbt, nbt)) this.metadata = null;
        this.nbt = nbt;
        return this;
    }

    public RecipeItem withNBT(ItemStack stack) {
        this.nbt = Utils.getItemNBT(stack);
        this.metadata = Utils.getItemMetadata(stack);
        return this;
    }

    public RecipeItem withAmount(long amount) {
        this.amount = amount;
        return this;
    }

    public RecipeItem withTooltip(List<String> tooltips) {
        if (this.tooltip == null) {
            this.tooltip = new ArrayList<>();
        }
        this.tooltip.addAll(tooltips);
        return this;
    }

    public RecipeItem withTooltip(String... tooltips) {
        return withTooltip(Arrays.asList(tooltips));
    }
}
