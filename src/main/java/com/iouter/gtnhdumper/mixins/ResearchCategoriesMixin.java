package com.iouter.gtnhdumper.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.iouter.gtnhdumper.common.utils.ResearchSources;

import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;

@Mixin(value = ResearchCategories.class, remap = false)
public abstract class ResearchCategoriesMixin {

    /** The registry check in capture distinguishes successful inserts from rejected registrations. */
    @Inject(method = "addResearch", at = @At("RETURN"), require = 1)
    private static void gtnhdumper$recordResearchSource(ResearchItem research, CallbackInfo callback) {
        ResearchSources.capture(research);
    }
}
