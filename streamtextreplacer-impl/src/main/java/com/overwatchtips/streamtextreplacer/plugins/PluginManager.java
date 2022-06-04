// This file is part of the StreamTextReplacer project.
// Copyright (C) 2022 Fernando Pettinelli

// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// at your option any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package com.overwatchtips.streamtextreplacer.plugins;

import com.overwatchtips.streamtextreplacer.StreamTextReplacer;
import com.overwatchtips.streamtextreplacer.api.ReplacerPlugin;
import com.overwatchtips.streamtextreplacer.api.commands.ConsoleCommand;
import com.overwatchtips.streamtextreplacer.records.OBSSettings;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PluginManager {

    private final StreamTextReplacer main;
    private final Map<String, ReplacerPlugin> loadedPlugins = new HashMap<>();
    private final Map<ReplacerPlugin, Long> intervalTime = new HashMap<>();
    private final Map<String, String> cachedPlaceholders = new HashMap<>();
    public PluginManager(StreamTextReplacer main) {
        this.main = main;
        loadPlugins();
    }

    public void refreshPlaceholders(boolean bypassCache) {
        OBSSettings obsSettings = main.getObsConfig().getObsSettings();
        Set<ReplacerPlugin> usedPlugins = new HashSet<>();

        obsSettings.customSources().forEach(obsSource -> {
            Map<String, Object> settings = new HashMap<>();
            String text = obsSource.text();

            for (String placeholder : substringsBetween(text, '%')) {
                String cached = cachedPlaceholders.get(placeholder);
                String placeholderWithoutCharacters = placeholder.substring(1, placeholder.length() - 1);
                String[] split = placeholderWithoutCharacters.split("_");
                if (split.length <= 1) {
                    continue;
                }

                ReplacerPlugin plugin = main.getPluginManager().getPluginByName(split[0]);
                if (plugin == null) {
                    continue;
                }

                if (bypassCache || !isInCooldown(plugin) || cached == null) {
                    String args = String.join("_", Arrays.copyOfRange(split, 1, split.length));
                    String parsed = plugin.onRequest(args);

                    if (parsed == null) {
                        if (cached != null) {
                            text = text.replace(placeholder, cached);
                            StreamTextReplacer.getLogger().warn(placeholder + " was not resolved, so a cached version was used instead.");
                        }
                        continue;
                    }

                    cachedPlaceholders.put(placeholder, parsed);
                    usedPlugins.add(plugin);

                    text = text.replace(placeholder, parsed);
                }else{
                    text = text.replace(placeholder, cached);
                }
            }

            settings.put("text", text);

            main.getController().setSourceSettings(obsSource.sourceName(), settings, responseBase ->
                    StreamTextReplacer.getLogger().debug("Changing settings of source {} to {}, returned {}", obsSource.sourceName(), obsSource.text(), responseBase.getStatus()));
        });
        usedPlugins.forEach(plugin -> intervalTime.put(plugin, Instant.now().getEpochSecond() + plugin.getRefreshTime()));
    }

    // adapted from Apache Commons Lang 3
    private List<String> substringsBetween(String text, char separator) {
        List<String> list = new ArrayList<>();
        int position = 0;
        while (position < text.length()) {
            int start = text.indexOf(separator, position);
            if (start < 0) {
                break;
            }

            start += 1;
            int end = text.indexOf(separator, start);
            if (end < 0) {
                break;
            }

            list.add(text.substring(start - 1, end + 1));
            position = end + 1;
        }

        return list;
    }

    private boolean isInCooldown(ReplacerPlugin plugin) {
        return intervalTime.getOrDefault(plugin, 0L) > Instant.now().getEpochSecond();
    }

    public ReplacerPlugin getPluginByName(String name) {
        return loadedPlugins.get(name);
    }

    public Collection<ReplacerPlugin> getLoadedPlugins() {
        return loadedPlugins.values();
    }

    private void loadPlugins() {
        File pluginsFolder = new File("plugins");
        if (!pluginsFolder.exists() || !pluginsFolder.isDirectory()) {
            pluginsFolder.mkdir();
        }

        List<Class<?>> subs = getClasses(pluginsFolder);
        for (Class<?> clazz : subs) {
            try {
                ReplacerPlugin plugin = (ReplacerPlugin)clazz.getConstructor(Logger.class).newInstance(StreamTextReplacer.getLogger());
                StreamTextReplacer.getLogger().info("Enabling {}, version {} by {}", plugin.getName(), plugin.getVersion(), plugin.getAuthor());
                plugin.setDataFolder(new File(pluginsFolder, plugin.getName()));

                for (Map.Entry<String, ConsoleCommand> entry : plugin.getCommandsToRegister().entrySet()) {
                    String commandName = entry.getKey();
                    ConsoleCommand command = entry.getValue();

                    if (main.getCommandManager().isCommandRegistered(commandName)) {
                        StreamTextReplacer.getLogger().warn("{} is already registered as a command!", commandName);
                        continue;
                    }

                    main.getCommandManager().registerCommand(commandName, command);
                }

                plugin.onEnable();

                loadedPlugins.put(plugin.getIdentifier(), plugin);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Class<?>> getClasses(File pluginFolder) {
        List<Class<?>> list = new ArrayList<>();

        FilenameFilter fileNameFilter = (dir, name) -> name.endsWith(".jar");

        File[] jars = pluginFolder.listFiles(fileNameFilter);
        if (jars == null) {
            return list;
        }

        for (File file : jars) {
            try {
                list = gather(file.toURI().toURL(), list);
            } catch(Throwable t){
                StreamTextReplacer.getLogger().error("Plugin {} failed to load.", file.getName());
            }
        }

        return list;
    }

    private List<Class<?>> gather(URL jar, List<Class<?>> list) {
        if (list == null) {
            list = new ArrayList<>();
        }

        ClassLoader classLoader = ReplacerPlugin.class.getClassLoader();

        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar}, classLoader);
             final JarInputStream jarInputStream = new JarInputStream(jar.openStream())) {

            while (true) {
                JarEntry jarInputStreamNextJarEntry = jarInputStream.getNextJarEntry();
                if (jarInputStreamNextJarEntry == null) {
                    break;
                }

                String name = jarInputStreamNextJarEntry.getName();
                if (name.isEmpty()) {
                    continue;
                }

                if (name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    String cname = name.substring(0, name.lastIndexOf(".class"));

                    Class<?> c = cl.loadClass(cname);
                    if (ReplacerPlugin.class.isAssignableFrom(c)) {
                        list.add(c);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return list;
    }
}
