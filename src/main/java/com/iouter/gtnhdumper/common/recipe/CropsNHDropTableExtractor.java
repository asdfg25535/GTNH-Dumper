package com.iouter.gtnhdumper.common.recipe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

final class CropsNHDropTableExtractor {

    private CropsNHDropTableExtractor() {}

    static Optional<Map<?, ?>> extract(Object cachedRecipe) {
        if (cachedRecipe == null) return Optional.empty();

        try {
            Field cropCardField = cachedRecipe.getClass()
                .getDeclaredField("cropCard");
            cropCardField.setAccessible(true);
            Object cropCard = cropCardField.get(cachedRecipe);
            if (cropCard == null) return Optional.empty();

            Method getDropTable = cropCard.getClass()
                .getMethod("getDropTable");
            getDropTable.setAccessible(true);
            Object dropTable = getDropTable.invoke(cropCard);
            if (!(dropTable instanceof Map)) return Optional.empty();

            return Optional.of((Map<?, ?>) dropTable);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException
            | SecurityException ignored) {
            return Optional.empty();
        }
    }
}
