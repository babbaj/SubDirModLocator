package com.babbaj;

import cpw.mods.modlauncher.ArgumentHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Troll implements ITransformationService {

    public Troll() {
        // If this fails then we were probably loaded by the default classloader
        // which should only happen in debug when we in the classpath
        final URLClassLoader loader = (URLClassLoader) getClass().getClassLoader();
        try {
            final Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrl.setAccessible(true);
            // TODO: get game dir correctly
            final Path mods = Paths.get("mods/" + getGameVersion());
            if (Files.exists(mods)) {
                ModDirTransformerDiscoverer.getExtraLocators().add(getOurPath());
                Files.list(mods)
                    .forEach(p -> {
                        try {
                            System.out.println("Adding mod jar: " + p);

                            addUrl.invoke(loader, p.toUri().toURL());
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    // Not sure if its okay for this to not be a jar
    private static Path getOurPath() {
        try {
            return Paths.get(Troll.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getGameVersion() {
        ArgumentHandler argHandler = Objects.requireNonNull(getArgumentHandler());
        String[] args = Objects.requireNonNull(getArgs(argHandler));
        final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("fml.mcVersion").withRequiredArg();
        final OptionSet optionSet = parser.parse(args);

        return Objects.requireNonNull((String) optionSet.valueOf("fml.mcVersion"), "Failed to get the mc version from argument \"fml.mcVersion\"");
    }

    private static String[] getArgs(ArgumentHandler argHandler) {
        try {
            Field f = ArgumentHandler.class.getDeclaredField("args");
            f.setAccessible(true);
            return (String[]) f.get(argHandler);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static ArgumentHandler getArgumentHandler() {
        try {
            Field f = Launcher.class.getDeclaredField("argumentHandler");
            f.setAccessible(true);
            return (ArgumentHandler) f.get(Launcher.INSTANCE);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nonnull
    @Override
    public String name() {
        return "TrollTransformationService";
    }

    @Override
    public void initialize(IEnvironment environment) { }

    @Override
    public void beginScanning(IEnvironment environment) { }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException { }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return Collections.emptyList();
    }
}
