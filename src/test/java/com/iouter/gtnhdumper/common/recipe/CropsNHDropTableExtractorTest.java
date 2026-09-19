package com.iouter.gtnhdumper.common.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class CropsNHDropTableExtractorTest {

    @Test
    void extractsOriginalTenThousandthsChanceFromCropDefinition() {
        Map<String, Integer> dropTable = new LinkedHashMap<>();
        dropTable.put("argentia", 5000);

        Optional<Map<?, ?>> extracted = CropsNHDropTableExtractor.extract(new CachedCropRecipe(dropTable));

        assertSame(dropTable, extracted.get());
        assertEquals(
            5000,
            extracted.get()
                .get("argentia"));
    }

    @Test
    void rejectsObjectsWithoutCropCardMetadata() {
        assertFalse(
            CropsNHDropTableExtractor.extract(new Object())
                .isPresent());
    }

    private static final class CachedCropRecipe {

        @SuppressWarnings("unused")
        private final CropCard cropCard;

        private CachedCropRecipe(Map<String, Integer> dropTable) {
            this.cropCard = new CropCard(dropTable);
        }
    }

    private static final class CropCard {

        private final Map<String, Integer> dropTable;

        private CropCard(Map<String, Integer> dropTable) {
            this.dropTable = dropTable;
        }

        public Map<String, Integer> getDropTable() {
            return dropTable;
        }
    }
}
