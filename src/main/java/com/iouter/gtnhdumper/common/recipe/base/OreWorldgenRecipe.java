package com.iouter.gtnhdumper.common.recipe.base;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class OreWorldgenRecipe extends BaseRecipe {

    private static final Pattern HEIGHT_RANGE = Pattern.compile("(-?\\d+)-(-?\\d+)\\*?");

    private final String key;
    private final int minY;
    private final int maxY;
    private final List<String> dims;

    protected OreWorldgenRecipe(String key, String worldGenHeightRange, List<String> dims, ArrayList<Object> inputItems,
        ArrayList<Object> outputItems, ArrayList<Object> otherItems) {
        super(inputItems, null, outputItems, null, otherItems);
        this.key = key;
        this.dims = new ArrayList<>(dims);
        // NEI appends '*' when a vein has dimension-specific height overrides.
        Matcher heightRange = HEIGHT_RANGE.matcher(worldGenHeightRange);
        if (!heightRange.matches()) {
            throw new IllegalArgumentException("Invalid height range for " + key + ": " + worldGenHeightRange);
        }
        minY = Integer.parseInt(heightRange.group(1));
        maxY = Integer.parseInt(heightRange.group(2));
    }
}
