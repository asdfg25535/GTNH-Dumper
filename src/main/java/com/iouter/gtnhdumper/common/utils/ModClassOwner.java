package com.iouter.gtnhdumper.common.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/** Resolves implementation classes back to the mod package that supplied them. */
public final class ModClassOwner {

    private static final String AMBIGUOUS = "";
    private static Map<String, String> packageOwners;
    private static List<PackagePrefixOwner> modPackagePrefixes;

    private ModClassOwner() {}

    public static String find(Class<?> type) {
        if (type == null || type.getPackage() == null) return null;
        String packageName = type.getPackage()
            .getName();
        initializeOwners();
        String owner = findByModPackagePrefix(packageName);
        if (owner != null) return owner;
        owner = packageOwners.get(packageName);
        return owner == null || owner.equals(AMBIGUOUS) ? null : owner;
    }

    private static void initializeOwners() {
        if (packageOwners == null) {
            Map<String, String> owners = new HashMap<>();
            List<PackagePrefixOwner> prefixes = new LinkedList<>();
            for (ModContainer mod : Loader.instance()
                .getModList()) {
                Object modObject = mod.getMod();
                if (modObject != null && modObject.getClass()
                    .getPackage() != null) {
                    prefixes.add(
                        new PackagePrefixOwner(
                            modObject.getClass()
                                .getPackage()
                                .getName(),
                            mod.getModId()));
                }
                for (String packageName : mod.getOwnedPackages()) {
                    String existing = owners.get(packageName);
                    if (existing == null || existing.equalsIgnoreCase(mod.getModId())) {
                        owners.put(packageName, mod.getModId());
                    } else {
                        // A package shared by multiple ModContainers cannot identify one owner reliably.
                        owners.put(packageName, AMBIGUOUS);
                    }
                }
            }
            modPackagePrefixes = prefixes;
            packageOwners = owners;
        }
    }

    private static String findByModPackagePrefix(String packageName) {
        String owner = null;
        int longestPrefix = -1;
        boolean ambiguous = false;
        for (PackagePrefixOwner candidate : modPackagePrefixes) {
            if (!packageName.equals(candidate.packagePrefix)
                && !packageName.startsWith(candidate.packagePrefix + ".")) {
                continue;
            }
            int length = candidate.packagePrefix.length();
            if (length > longestPrefix) {
                owner = candidate.modId;
                longestPrefix = length;
                ambiguous = false;
            } else if (length == longestPrefix && !candidate.modId.equalsIgnoreCase(owner)) {
                ambiguous = true;
            }
        }
        return ambiguous ? null : owner;
    }

    private static final class PackagePrefixOwner {

        private final String packagePrefix;
        private final String modId;

        private PackagePrefixOwner(String packagePrefix, String modId) {
            this.packagePrefix = packagePrefix;
            this.modId = modId;
        }
    }
}
