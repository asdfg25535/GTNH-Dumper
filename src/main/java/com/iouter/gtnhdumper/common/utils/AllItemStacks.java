package com.iouter.gtnhdumper.common.utils;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameData;
import gregtech.api.items.CircuitComponentFakeItem;
import gregtech.common.tileentities.machines.multi.nanochip.util.CircuitComponent;

public class AllItemStacks {

    private static List<ItemStack> allItemStacks;
    // getSubItems may return stacks backed by a different, unregistered Item. Keep the
    // registered source that produced each exact stack so filtered exports retain its owner.
    private static Map<ItemStack, String> registryNames;

    private AllItemStacks() {}

    public static List<ItemStack> getAllItemStacks() {
        if (allItemStacks == null) {
            List<ItemStack> itemStacks = new ArrayList<>();
            Map<ItemStack, String> itemRegistryNames = new IdentityHashMap<>();
            for (Object temp : GameData.getItemRegistry()) {
                if (!(temp instanceof Item item)) continue;
                String registryName = GameData.getItemRegistry()
                    .getNameForObject(item);
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
                    itemRegistryNames.put(itemStack, registryName);
                }
            }
            registryNames = itemRegistryNames;
            allItemStacks = itemStacks;
        }
        return allItemStacks;
    }

    public static List<ItemStack> getFilteredItemStacks() {
        ModFilter filter = ModFilter.current();
        if (!filter.isActive()) return getAllItemStacks();
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : getAllItemStacks()) {
            if (filter.matchesKey(registryNames.get(stack))) result.add(stack);
        }
        return result;
    }
}
