package com.iouter.gtnhdumper.common.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;

import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseRecipe;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;

import codechicken.nei.recipe.IRecipeHandler;
import gregtech.api.enums.StoneType;
import gtneioreplugin.plugin.item.ItemDimensionDisplay;
import gtneioreplugin.util.DimensionHelper;
import gtneioreplugin.util.GT5OreLayerHelper;
import gtneioreplugin.util.GT5OreLayerHelper.OreLayerWrapper;

public class GTOreVeinHandlerRecipe extends BaseHandlerRecipe {

    private static final Pattern HEIGHT_RANGE = Pattern.compile("(-?\\d+)-(-?\\d+)\\*?");

    public GTOreVeinHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        List<OreLayerWrapper> veins = new ArrayList<>(
            GT5OreLayerHelper.getOreVeinsByName()
                .values());
        veins.sort(Comparator.comparing(vein -> vein.veinName));
        List<OreVeinRecipe> recipes = new ArrayList<>();
        for (OreLayerWrapper vein : veins) {
            List<String> dims = new ArrayList<>(vein.abbrDimNames);
            dims.sort(
                Comparator.comparingInt(DimensionHelper::getIndexByAbbr)
                    .thenComparing(Comparator.naturalOrder()));
            Set<StoneType> stoneTypes = new HashSet<>();
            List<ItemStack> dimensionItems = new ArrayList<>();
            for (String dim : dims) {
                DimensionHelper.Dimension dimension = DimensionHelper.getByIndex(DimensionHelper.getIndexByAbbr(dim));
                if (dimension != null) {
                    stoneTypes.addAll(dimension.stoneTypes());
                }
                ItemStack display = ItemDimensionDisplay.getItem(dim);
                if (display != null) {
                    dimensionItems.add(display);
                }
            }
            ArrayList<Object> inputItems = new ArrayList<>();
            if (!dimensionItems.isEmpty()) {
                // Dimensions are alternative locations, not ingredients required together.
                inputItems.add(RecipeUtil.getRecipeItems(dimensionItems.toArray(new ItemStack[0])));
            }
            ArrayList<Object> outputItems = new ArrayList<>();
            // The helper orders layers as primary, secondary, between and sporadic.
            for (int layer = 0; layer < vein.ores.length; layer++) {
                outputItems.add(
                    RecipeUtil.getRecipeItems(
                        vein.getVeinLayerOre(layer, stoneTypes)
                            .toArray(new ItemStack[0])));
            }
            recipes.add(new OreVeinRecipe(vein, dims, inputItems, outputItems));
        }
        return recipes;
    }

    private static class OreVeinRecipe extends BaseRecipe {

        private final String key;
        private final String localizedName;
        private final String primary;
        private final String secondary;
        private final String between;
        private final String sporadic;
        private final int size;
        private final int density;
        private final int weight;
        private final int minY;
        private final int maxY;
        private final List<String> dims;
        private final Map<String, String> dimWorldGenHeightRange;

        private OreVeinRecipe(OreLayerWrapper vein, List<String> dims, ArrayList<Object> inputItems,
            ArrayList<Object> outputItems) {
            super(inputItems, null, outputItems, null, null);
            key = vein.veinName;
            localizedName = vein.getLocalizedName();
            primary = vein.mPrimaryVeinMaterial.getInternalName();
            secondary = vein.mSecondaryMaterial.getInternalName();
            between = vein.mBetweenMaterial.getInternalName();
            sporadic = vein.mSporadicMaterial.getInternalName();
            size = vein.size;
            density = vein.density;
            weight = vein.randomWeight;
            // NEI appends '*' when a vein has dimension-specific height overrides.
            Matcher heightRange = HEIGHT_RANGE.matcher(vein.worldGenHeightRange);
            if (!heightRange.matches()) {
                throw new IllegalArgumentException("Invalid height range for " + key + ": " + vein.worldGenHeightRange);
            }
            minY = Integer.parseInt(heightRange.group(1));
            maxY = Integer.parseInt(heightRange.group(2));
            this.dims = dims;
            dimWorldGenHeightRange = new TreeMap<>(vein.dimWorldGenHeightRange);
        }
    }
}
