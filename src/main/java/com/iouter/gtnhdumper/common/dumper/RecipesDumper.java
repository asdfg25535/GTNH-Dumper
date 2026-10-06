package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.bdew.neiaddons.forestry.BaseBreedingRecipeHandler;
import net.bdew.neiaddons.forestry.BaseProduceRecipeHandler;
import net.minecraft.util.ChatComponentTranslation;

import com.emoniph.witchery.integration.NEICauldronRecipeHandler;
import com.google.common.base.Objects;
import com.google.gson.JsonObject;
import com.gtnewhorizon.cropsnh.compatibility.NEI.NEICropsNHCropHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.AlchemyRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.AspectCombinationHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.InfusionRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapedArcaneRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapelessArcaneRecipeHandler;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.base.FilteredDataDumper;
import com.iouter.gtnhdumper.common.recipe.AvaExtremeShapedHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.CarpenterHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.CropsNHHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.ForestryHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GTDefaultHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GTOreVeinHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GTSmallOreVeinHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GTUndergroundFluidHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GasSiphonHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GeneralHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.MobHandlerInfernalRecipe;
import com.iouter.gtnhdumper.common.recipe.MobHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.ShapedCraftingHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.SpacePumpModuleHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.TCHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.VendingMachineHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.WitcheryCauldronHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.utils.ModFilter;
import com.iouter.gtnhdumper.common.utils.Utils;
import com.kuba6000.mobsinfo.nei.MobHandler;
import com.kuba6000.mobsinfo.nei.MobHandlerInfernal;

import codechicken.nei.NEIClientUtils;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import forestry.factory.recipes.nei.NEIHandlerCarpenter;
import fox.spiteful.avaritia.compat.nei.ExtremeShapedRecipeHandler;
import gregtech.nei.GTNEIDefaultHandler;
import gtneioreplugin.plugin.gregtech5.PluginGT5SmallOreStat;
import gtneioreplugin.plugin.gregtech5.PluginGT5UndergroundFluid;
import gtneioreplugin.plugin.gregtech5.PluginGT5VeinStat;
import gtnhintergalactic.nei.GasSiphonRecipeHandler;
import gtnhintergalactic.nei.SpacePumpModuleRecipeHandler;

public class RecipesDumper extends FilteredDataDumper {

    public RecipesDumper() {
        super("tools.dump.gtnhdumper.recipe");
    }

    private static BaseHandlerRecipe dumpRecipes(IRecipeHandler recipeHandler) {
        final String clazz = Utils.getAfterLastDot(recipeHandler.getHandlerId())
            .replace(".", "_");
        if (CommonProxy.isGTLoaded) {
            if (recipeHandler instanceof PluginGT5VeinStat) {
                return new GTOreVeinHandlerRecipe(recipeHandler);
            }
            if (recipeHandler instanceof PluginGT5SmallOreStat) {
                return new GTSmallOreVeinHandlerRecipe(recipeHandler);
            }
            if (recipeHandler instanceof PluginGT5UndergroundFluid) {
                return new GTUndergroundFluidHandlerRecipe(recipeHandler);
            }
            if (recipeHandler instanceof GasSiphonRecipeHandler) {
                return new GasSiphonHandlerRecipe(recipeHandler);
            }
            if (recipeHandler instanceof SpacePumpModuleRecipeHandler) {
                return new SpacePumpModuleHandlerRecipe(recipeHandler);
            }
            if (recipeHandler instanceof GTNEIDefaultHandler) {
                return new GTDefaultHandlerRecipe((GTNEIDefaultHandler) recipeHandler);
            }
        }
        if (CommonProxy.isTCLoaded) {
            if (recipeHandler instanceof AspectCombinationHandler || recipeHandler instanceof ShapedArcaneRecipeHandler
                || recipeHandler instanceof ShapelessArcaneRecipeHandler
                || recipeHandler instanceof AlchemyRecipeHandler
                || recipeHandler instanceof InfusionRecipeHandler) {
                return new TCHandlerRecipe(recipeHandler);
            }
        }
        if (CommonProxy.isAvaritiaLoaded) {
            if (recipeHandler instanceof ExtremeShapedRecipeHandler) {
                return new AvaExtremeShapedHandlerRecipe(recipeHandler);
            }
        }
        if (CommonProxy.isFRLoaded) {
            if (recipeHandler instanceof NEIHandlerCarpenter) {
                return new CarpenterHandlerRecipe(recipeHandler);
            }
            if (CommonProxy.isNEIAddonLoaded) {
                if (recipeHandler instanceof BaseBreedingRecipeHandler
                    || recipeHandler instanceof BaseProduceRecipeHandler) {
                    return new ForestryHandlerRecipe(recipeHandler);
                }
            }
        }
        if (CommonProxy.isMobsInfoLoaded) {
            if (recipeHandler instanceof MobHandler) {
                return new MobHandlerRecipe((MobHandler) recipeHandler);
            }
            if (recipeHandler instanceof MobHandlerInfernal) {
                return new MobHandlerInfernalRecipe((MobHandlerInfernal) recipeHandler);
            }
        }
        if (CommonProxy.isWitcheryLoaded && recipeHandler instanceof NEICauldronRecipeHandler) {
            return new WitcheryCauldronHandlerRecipe(recipeHandler);
        }
        if (CommonProxy.isCropsNHLoaded && recipeHandler instanceof NEICropsNHCropHandler cropsNHHandler) {
            return new CropsNHHandlerRecipe(cropsNHHandler);
        }
        if (recipeHandler instanceof TemplateRecipeHandler templateHandler
            && "vendingmachine".equals(templateHandler.getOverlayIdentifier())) {
            return new VendingMachineHandlerRecipe(recipeHandler);
        }
        if (clazz.contains("Shaped") && !clazz.equals("RecipeHandlerRollingMachineShaped")) {
            return new ShapedCraftingHandlerRecipe(recipeHandler);
        }
        return new GeneralHandlerRecipe(recipeHandler);
    }

    @Override
    public String[] header() {
        return new String[] { "Recipe json" };
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        List<String[]> recipesList = new ArrayList<>();
        ModFilter filter = ModFilter.current();
        Map<String, String> skipped = new LinkedHashMap<>();
        for (IRecipeHandler handler : GuiUsageRecipe.usagehandlers) {
            final String name = handler.getRecipeName();
            if (filter.isActive() && !(CommonProxy.isGTLoaded && handler instanceof GTNEIDefaultHandler)) {
                skipped.put(handler.getHandlerId(), "Original registering mod is not recorded by this handler");
                continue;
            }
            final String handlerName = handler.getHandlerId();
            final String handlerId = Objects
                .firstNonNull(handler instanceof TemplateRecipeHandler ? handler.getOverlayIdentifier() : null, "null");
            HandlerInfo info = GuiRecipeTab.getHandlerInfo(handlerName, handlerId);
            String modID = info != null ? info.getModId() : "Unknown";
            String id = Utils.getAfterLastDot(handlerId);
            if (java.util.Objects.equals(id, "name") && handlerId != null) {
                id = Utils.getAfterLastDot(handlerId.substring(0, handlerId.length() - ".name".length()));
            }
            String clazz = Utils.getAfterLastDot(handlerName);
            String fileName = "recipes/" + modID + "/" + clazz + "_" + id + ".json";
            fileName = Utils.replacePathIllegalChars(fileName);
            File file = filter.outputFile(fileName);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                JsonObject data = dumpRecipes(handler).build();
                if (data.has("sourceFilterUnsupported")) {
                    skipped.put(handlerName, "Custom recipe storage does not retain original owners");
                } else if (data.has("recipesWithoutSource") && data.get("recipesWithoutSource")
                    .getAsLong() > 0) {
                        skipped.put(
                            handlerName,
                            data.get("recipesWithoutSource")
                                .getAsLong()
                                + " recipes have no original owner; enable GregTech NEIRecipeOwner and restart before dumping");
                    }
                GTNHDumper.GSON.toJson(data, writer);
                recipesList.add(new String[] { name });
                GTNHDumper.info("已写入：" + file.getAbsolutePath());
            } catch (IOException e) {
                file.deleteOnExit();
                GTNHDumper.LOG.error(e);
            } catch (Exception e) {
                GTNHDumper.info("导出" + name + "时发生错误：" + e.getLocalizedMessage());
                file.deleteOnExit();
                GTNHDumper.LOG.error(e);
            }
        }
        if (!filter.isActive() && CommonProxy.isKubaTechLoaded) try {
            EECRecipeDumper.dump();
            recipesList.add(new String[] { EECRecipeDumper.RECIPE_NAME });
        } catch (Exception e) {
            GTNHDumper.info("导出" + EECRecipeDumper.RECIPE_NAME + "时发生错误：" + e.getLocalizedMessage());
            GTNHDumper.LOG.error(e);
        }
        if (filter.isActive()) {
            if (CommonProxy.isKubaTechLoaded) {
                skipped.put("kubatech.eec", "Original registering mod is not recorded by this custom recipe storage");
            }
            File report = filter.outputFile("recipes_filter_report.json");
            try (FileWriter writer = new FileWriter(report)) {
                GTNHDumper.GSON.toJson(skipped, writer);
            } catch (IOException e) {
                GTNHDumper.LOG.error("Could not write recipe source filter report", e);
            }
            if (!skipped.isEmpty()) {
                NEIClientUtils.printChatMessage(
                    new ChatComponentTranslation(
                        "nei.options.tools.dump.gtnhdumper.modFilter.skipped",
                        skipped.size(),
                        report.getPath()));
            }
        }
        return recipesList;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation("nei.options.tools.dump.gtnhdumper.recipes.dumped", file.getPath());
    }

    @Override
    public int modeCount() {
        return 1;
    }
}
