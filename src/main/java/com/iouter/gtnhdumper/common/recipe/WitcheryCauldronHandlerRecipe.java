package com.iouter.gtnhdumper.common.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.emoniph.witchery.brewing.WitcheryBrewRegistry;
import com.emoniph.witchery.brewing.action.BrewActionRitualRecipe;
import com.emoniph.witchery.integration.NEICauldronRecipeHandler;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;

import codechicken.nei.recipe.IRecipeHandler;

public class WitcheryCauldronHandlerRecipe extends BaseHandlerRecipe {

    public WitcheryCauldronHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        if (!(handler instanceof NEICauldronRecipeHandler)) {
            return null;
        }

        List<CauldronRecipe> recipes = new ArrayList<>();
        for (BrewActionRitualRecipe ritual : WitcheryBrewRegistry.INSTANCE.getRecipes()) {
            for (BrewActionRitualRecipe.Recipe recipe : ritual.getExpandedRecipes()) {
                ArrayList<Object> inputItems = new ArrayList<>();
                Arrays.stream(recipe.ingredients)
                    .filter(input -> input != null)
                    .map(RecipeItem::new)
                    .forEach(inputItems::add);

                ArrayList<Object> outputItems = new ArrayList<>();
                outputItems.add(new RecipeItem(recipe.result));

                int altarPower = Arrays.stream(recipe.ingredients)
                    .filter(input -> input != null)
                    .mapToInt(WitcheryBrewRegistry.INSTANCE::getAltarPower)
                    .sum();
                recipes.add(new CauldronRecipe(inputItems, outputItems, altarPower));
            }
        }
        return recipes;
    }

    private static class CauldronRecipe extends BaseRecipe {

        private final int altarPower;

        private CauldronRecipe(ArrayList<Object> inputItems, ArrayList<Object> outputItems, int altarPower) {
            super(inputItems, null, outputItems, null, null);
            this.altarPower = altarPower;
        }
    }
}
