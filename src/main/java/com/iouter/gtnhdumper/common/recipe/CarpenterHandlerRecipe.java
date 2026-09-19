package com.iouter.gtnhdumper.common.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;

import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;
import com.iouter.gtnhdumper.common.utils.Utils;

import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import forestry.factory.recipes.nei.NEIHandlerCarpenter;

public class CarpenterHandlerRecipe extends BaseHandlerRecipe {

    public CarpenterHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        if (!(handler instanceof NEIHandlerCarpenter carpenter)) {
            return null;
        }

        carpenter.loadCraftingRecipes(carpenter.getRecipeID(), (Object) null);
        List<BaseRecipe> recipes = new ArrayList<>();
        for (TemplateRecipeHandler.CachedRecipe cached : carpenter.arecipes) {
            if (!(cached instanceof NEIHandlerCarpenter.CachedCarpenterRecipe recipe)) {
                continue;
            }

            // Forestry keeps this input in a separate tank, outside getIngredients().
            ArrayList<RecipeFluid> inputFluids = null;
            if (recipe.tank != null && recipe.tank.tank != null) {
                FluidStack fluid = recipe.tank.tank.getFluid();
                if (fluid != null && fluid.getFluid() != null) {
                    inputFluids = new ArrayList<>();
                    inputFluids.add(new RecipeFluid(fluid).withNBT(fluid));
                }
            }
            recipes.add(
                new BaseRecipe(
                    Utils.getRecipeItems(recipe.getIngredients()),
                    inputFluids,
                    Utils.getRecipeItems(recipe.getResult()),
                    null,
                    null));
        }
        return recipes;
    }
}
