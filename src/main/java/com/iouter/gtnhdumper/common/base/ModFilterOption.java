package com.iouter.gtnhdumper.common.base;

import codechicken.nei.config.OptionList;
import codechicken.nei.config.OptionTextField;

/** Uses NEI's normal global/world configuration and persists changes immediately. */
public class ModFilterOption extends OptionTextField {

    public static final ModFilterOption INSTANCE = new ModFilterOption();

    private ModFilterOption() {
        super("tools.dump.gtnhdumper.modFilter");
    }

    @Override
    public void onAdded(OptionList list) {
        super.onAdded(list);
        globalConfigSet().config.getTag(configName())
            .setDefaultValue("");
    }

    public String value() {
        // Follow the same global/world selector as the dump buttons.
        if (parent == null) return "";
        return (getSlot() == null ? activeTag() : renderTag()).getValue();
    }
}
