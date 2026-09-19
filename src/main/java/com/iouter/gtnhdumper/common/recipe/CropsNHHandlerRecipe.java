package com.iouter.gtnhdumper.common.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.gtnewhorizon.cropsnh.api.ICropCard;
import com.gtnewhorizon.cropsnh.compatibility.NEI.NEICropsNHCropHandler;
import com.gtnewhorizon.cropsnh.farming.registries.CropRegistry;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseRecipe;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;
import com.iouter.gtnhdumper.common.utils.Utils;

import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class CropsNHHandlerRecipe extends BaseHandlerRecipe {

    public CropsNHHandlerRecipe(NEICropsNHCropHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        List<BaseRecipe> recipes = new ArrayList<>();
        if (!(handler instanceof NEICropsNHCropHandler recipeHandler)) {
            return null;
        }
        recipeHandler.loadCraftingRecipes(recipeHandler.getOverlayIdentifier(), (Object) null);
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
        ItemStack seed = recipe.getIngredients()
            .get(0).items[0];
        ICropCard cropCard = CropRegistry.instance.get(seed);
        for (Map.Entry<ItemStack, Integer> drop : cropCard.getDropTable()
            .entrySet()) {
            items.add(RecipeUtil.getRecipeItems(new ItemStack[] { drop.getKey() }, drop.getValue()));
        }
        return items;
    }
}
