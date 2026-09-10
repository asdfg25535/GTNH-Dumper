package com.iouter.gtnhdumper.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.StoneType;
import gtneioreplugin.plugin.item.ItemDimensionDisplay;
import gtneioreplugin.util.DimensionHelper;

public final class WorldgenRecipeUtil {

    private WorldgenRecipeUtil() {}

    public static List<String> sortDimensions(Collection<String> dimensions) {
        List<String> dims = new ArrayList<>(dimensions);
        dims.sort(
            Comparator.comparingInt(DimensionHelper::getIndexByAbbr)
                .thenComparing(Comparator.naturalOrder()));
        return dims;
    }

    public static Set<StoneType> getStoneTypes(Collection<String> dimensions) {
        Set<StoneType> stoneTypes = new LinkedHashSet<>();
        for (String dim : dimensions) {
            DimensionHelper.Dimension dimension = DimensionHelper.getByIndex(DimensionHelper.getIndexByAbbr(dim));
            if (dimension != null) {
                stoneTypes.addAll(dimension.stoneTypes());
            }
        }
        return stoneTypes;
    }

    public static ArrayList<Object> getDimensionInputs(Collection<String> dimensions) {
        List<ItemStack> items = new ArrayList<>();
        for (String dim : dimensions) {
            items.add(ItemDimensionDisplay.getItem(dim));
        }
        ArrayList<Object> inputs = new ArrayList<>();
        // Dimensions are alternative locations, not ingredients required together.
        addItemAlternatives(inputs, items);
        return inputs;
    }

    public static Object getItemAlternatives(Collection<ItemStack> items) {
        if (items == null) return null;
        ItemStack[] stacks = items.stream()
            .filter(Objects::nonNull)
            .toArray(ItemStack[]::new);
        return stacks.length == 0 ? null : RecipeUtil.getRecipeItems(stacks);
    }

    public static void addItemAlternatives(ArrayList<Object> slots, Collection<ItemStack> items) {
        Object alternatives = getItemAlternatives(items);
        if (alternatives != null) {
            slots.add(alternatives);
        }
    }
}
