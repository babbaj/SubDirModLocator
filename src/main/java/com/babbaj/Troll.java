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
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
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
            final Path mods = Paths.get("mods/" + getGameVersion());
            if (Files.exists(mods)) {
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
            ModDirTransformerDiscoverer.getExtraLocators().add(getOurPath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public static Path getOurPath() {
        final String thisPath = Troll.class.getName().replace('.', '/') + ".class";
        final URL url = Troll.class.getClassLoader().getResource(thisPath);

        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
                return new File(((JarURLConnection) connection).getJarFileURL().toURI()).toPath();
            } else {
                throw new IllegalStateException("URLConnection is not a JarURLConnection");
            }
        } catch (Exception ex) {
            // we aren't a jar or something lol
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
