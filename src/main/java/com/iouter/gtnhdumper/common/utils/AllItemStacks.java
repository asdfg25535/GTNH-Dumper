package com.iouter.gtnhdumper.common.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameData;
import gregtech.api.items.CircuitComponentFakeItem;
import gregtech.common.tileentities.machines.multi.nanochip.util.CircuitComponent;

public class AllItemStacks {

    private static List<ItemStack> allItemStacks;

    private AllItemStacks() {}

    public static List<ItemStack> getAllItemStacks() {
        if (allItemStacks == null) {
            List<ItemStack> itemStacks = new ArrayList<>();
            for (Object temp : GameData.getItemRegistry()) {
                if (!(temp instanceof Item item)) continue;
                List<ItemStack> sub = new ArrayList<>();
                if (item == CircuitComponentFakeItem.INSTANCE) {
                    for (CircuitComponent component : CircuitComponent.VALUES) {
                        sub.add(component.getFakeStack(1));
                    }
                } else {
                    item.getSubItems(item, CreativeTabs.tabAllSearch, sub);
                }
                for (ItemStack itemStack : sub) {
                    if (Utils.isStackInvalid(itemStack)) {
                        continue;
                    }
                    itemStacks.add(itemStack);
                }
            }
            allItemStacks = itemStacks;
        }
        return allItemStacks;
    }
}
