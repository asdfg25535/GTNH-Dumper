package com.iouter.gtnhdumper.common.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import codechicken.nei.PositionedStack;

class CropsNHDropTableExtractorTest {

    @Test
    void extractsOriginalTenThousandthsChanceFromCropDefinition() {
        ItemStack seed = new ItemStack(new Item());
        ItemStack cropDrop = new ItemStack(new Item());
        Map<ItemStack, Integer> dropTable = new LinkedHashMap<>();
        dropTable.put(cropDrop, 5000);

        Optional<Map<ItemStack, Integer>> extracted = CropsNHDropTableExtractor
            .extract(Collections.singletonList(new PositionedStack(seed, 0, 0)), actualSeed -> {
                assertSame(seed.getItem(), actualSeed.getItem());
                return dropTable;
            });

        assertSame(dropTable, extracted.get());
        assertEquals(
            5000,
            extracted.get()
                .get(cropDrop));
    }

    @Test
    void rejectsRecipesWithoutSeedIngredient() {
        assertFalse(
            CropsNHDropTableExtractor.extract(Collections.emptyList(), ignored -> Collections.emptyMap())
                .isPresent());
    }
}
