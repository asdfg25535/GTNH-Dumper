package com.iouter.gtnhdumper.common.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

/** Text representation shared by the ordinary and modifier-key tooltip exports. */
final class TooltipText {

    private static final Pattern FORMATTING = Pattern.compile("(?i)§[0-9A-FK-OR]");

    // Vanilla translations can put the signed amount after the name (e.g. Chinese),
    // while Chromatic Tooltips puts it first. Normalize just this attribute notation.
    private static final Pattern LEADING_AMOUNT = Pattern.compile("^([+-][0-9][0-9.,]*%?)\\s+(.+)$");

    private TooltipText() {}

    static String format(List<String> lines, List<JsonObject> stats) {
        List<String> result = new ArrayList<>(lines);
        Set<String> existing = new HashSet<>();
        for (String line : lines) {
            existing.add(plainText(line));
        }
        for (JsonObject stat : stats) {
            if (!stat.has("textLine") || stat.get("textLine")
                .isJsonNull()) continue;
            String text = stat.get("textLine")
                .getAsString();
            String plain = plainText(text);
            if (!plain.isEmpty() && existing.add(plain)) {
                result.add(text);
            }
        }
        return String.join("<br>", result);
    }

    private static String plainText(String line) {
        String plain = FORMATTING.matcher(String.valueOf(line))
            .replaceAll("")
            .trim();
        return LEADING_AMOUNT.matcher(plain)
            .replaceFirst("$2 $1");
    }
}
