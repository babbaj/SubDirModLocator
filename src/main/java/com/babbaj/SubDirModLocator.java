package com.babbaj;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Mod("SubDirModLocator")
public class SubDirModLocator extends AbstractJarFileLocator {

    private String version;

    @Override
    public List<IModFile> scanMods() {
        List<Path> excluded = ModDirTransformerDiscoverer.allExcluded(); // want to keep the same behavior as forge, this shouldn't be used as a bypass
        final Path folder = FMLPaths.MODSDIR.get().resolve(this.version);

        return LamdbaExceptionUtils.uncheck(() ->
            Files.list(folder)
            .filter(p -> !excluded.contains(p))
            .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
            .sorted(Comparator.comparing(p -> p.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
            .map(p -> new ModFile(p, this))
            .peek(f -> this.modJars.compute(f, (mf, fs) -> this.createFileSystem(mf)))
            .collect(Collectors.toList())
        );
    }

    @Override
    public String name() {
        return "SubDirModLocator";
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
        this.version = Objects.requireNonNull((String)arguments.get("mcVersion"), "Failed to get version from initArguments");
    }
}
