package com.babbaj;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class SubDirModLocator extends AbstractJarFileLocator {

    private String version;

    @Override
    public List<IModFile> scanMods() {
        List<Path> excluded = ModDirTransformerDiscoverer.allExcluded(); // want to keep the same behavior as forge, this shouldn't be used as a bypass
        final Path folder = FMLPaths.MODSDIR.get().resolve(this.version);

        if (Files.exists(folder)) {
            return LamdbaExceptionUtils.uncheck(() -> {
                final List<IModFile> mods = Files.list(folder)
                    .filter(p -> !excluded.contains(p))
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .map(p -> new ModFile(p, this))
                    .peek(f -> this.modJars.compute(f, (mf, fs) -> this.createFileSystem(mf)))
                    .collect(Collectors.toList());
                // TODO: there might be other services other than ITransformationService that currently dont work
                /*{
                    final List<IModFile> badMods = mods.stream()
                        .filter(mf -> hasTransformerService(mf.getFilePath()))
                        .collect(Collectors.toList());
                    if (!badMods.isEmpty()) {
                        throw new IllegalStateException("Mods with ITransformationServices can not be loaded properly: " + badMods);
                    }
                }*/

                return mods;
            });
        } else {
            return Collections.emptyList();
        }
    }

    private static boolean hasTransformerService(Path jar) {
        return LamdbaExceptionUtils.uncheck(() -> {
            ZipFile zf = new ZipFile(new File(jar.toUri()));
            return zf.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService") != null;
        });

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
