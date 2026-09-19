package com.iouter.gtnhdumper.common.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.item.ItemStack;

import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseRecipe;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;
import com.iouter.gtnhdumper.common.utils.Utils;

import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class CropsNHHandlerRecipe extends BaseHandlerRecipe {

    private static final String CROPS_HANDLER_CLASS = "com.gtnewhorizon.cropsnh.compatibility.NEI.NEICropsNHCropHandler";

    public CropsNHHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    public static boolean supports(IRecipeHandler handler) {
        return CROPS_HANDLER_CLASS.equals(
            handler.getClass()
                .getName());
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        List<BaseRecipe> recipes = new ArrayList<>();
        if (!(handler instanceof TemplateRecipeHandler recipeHandler)) {
            return null;
        }
        try {
            recipeHandler.loadCraftingRecipes(recipeHandler.getOverlayIdentifier(), (Object) null);
        } catch (Exception ignored) {
            return null;
        }
        for (TemplateRecipeHandler.CachedRecipe recipe : recipeHandler.arecipes) {
            ArrayList<Object> otherItems = getRecipeItems(recipe);
            if (otherItems.isEmpty()) otherItems = null;
            recipes.add(
                new BaseRecipe(
                    Utils.getRecipeItems(recipe.getIngredients()),
                    null,
                    Utils.getRecipeItems(recipe.getResult()),
                    null,
                    otherItems));
        }
        return recipes;
    }

    private static ArrayList<Object> getRecipeItems(TemplateRecipeHandler.CachedRecipe recipe) {
        ArrayList<Object> items = new ArrayList<>();
        Optional<Map<ItemStack, Integer>> dropTable = CropsNHDropTableExtractor.extract(recipe);
        if (!dropTable.isPresent()) return Utils.getRecipeItems(recipe.getOtherStacks());

        List<Map.Entry<ItemStack, Integer>> drops = new ArrayList<>(
            dropTable.get()
                .entrySet());
        drops.sort((left, right) -> Integer.compare(right.getValue(), left.getValue()));
        for (Map.Entry<ItemStack, Integer> drop : drops) {
            items.add(RecipeUtil.getRecipeItems(new ItemStack[] { drop.getKey() }, drop.getValue()));
        }
        return items;
    }
}
