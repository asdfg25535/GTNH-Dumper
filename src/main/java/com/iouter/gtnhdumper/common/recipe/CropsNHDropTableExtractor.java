package com.iouter.gtnhdumper.common.recipe;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.item.ItemStack;

import com.gtnewhorizon.cropsnh.api.ICropCard;
import com.gtnewhorizon.cropsnh.farming.registries.CropRegistry;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

final class CropsNHDropTableExtractor {

    private CropsNHDropTableExtractor() {}

    static Optional<Map<ItemStack, Integer>> extract(TemplateRecipeHandler.CachedRecipe cachedRecipe) {
        if (cachedRecipe == null) return Optional.empty();

        return extract(cachedRecipe.getIngredients(), seed -> {
            ICropCard cropCard = CropRegistry.instance.get(seed);
            return cropCard == null ? null : cropCard.getDropTable();
        });
    }

    static Optional<Map<ItemStack, Integer>> extract(List<PositionedStack> ingredients,
        Function<ItemStack, Map<ItemStack, Integer>> dropTableLookup) {
        if (ingredients == null || ingredients.isEmpty()) return Optional.empty();

        PositionedStack seedIngredient = ingredients.get(0);
        if (seedIngredient == null || seedIngredient.items == null
            || seedIngredient.items.length == 0
            || seedIngredient.items[0] == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(dropTableLookup.apply(seedIngredient.items[0]));
    }
}
