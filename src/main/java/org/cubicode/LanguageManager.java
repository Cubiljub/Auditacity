package org.cubicode;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LanguageManager {

    private Locale currentLocale;
    private ResourceBundle bundle;

    public LanguageManager() {
        setLanguage("en");
    }

    public void setLanguage(String languageCode) {
        currentLocale = new Locale(languageCode);
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public String get(String key) {
        return bundle.getString(key);
    }

    public String getCurrentLanguageCode() {
        return currentLocale.getLanguage();
    }

    public List<LanguageOption> getAvailableLanguages() {
        List<String> codes = findAvailableLanguageCodes();
        List<LanguageOption> result = new ArrayList<LanguageOption>();

        for (String code : codes) {
            try {
                ResourceBundle langBundle = ResourceBundle.getBundle("messages", new Locale(code));
                String displayName = langBundle.getString("language.self");
                result.add(new LanguageOption(code, displayName));
            } catch (Exception ignored) {
            }
        }

        result.sort(new Comparator<LanguageOption>() {
            @Override
            public int compare(LanguageOption a, LanguageOption b) {
                return a.toString().compareToIgnoreCase(b.toString());
            }
        });

        return result;
    }

    private List<String> findAvailableLanguageCodes() {
        Set<String> codes = new LinkedHashSet<String>();

        try {
            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();

            if (location == null) {
                return getFallbackLanguages();
            }

            File source = new File(location.toURI());

            if (source.isFile() && source.getName().endsWith(".jar")) {
                readLanguagesFromJar(source, codes);
            } else if (source.isDirectory()) {
                readLanguagesFromDirectory(source, codes);
            }

        } catch (Exception ignored) {
        }

        if (codes.isEmpty()) {
            return getFallbackLanguages();
        }

        return new ArrayList<String>(codes);
    }

    private void readLanguagesFromJar(File jarFile, Set<String> codes) {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith("messages_") && name.endsWith(".properties")) {
                    String code = extractLanguageCode(name);
                    if (!code.isEmpty()) {
                        codes.add(code);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void readLanguagesFromDirectory(File directory, Set<String> codes) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        scanDirectory(directory, codes);
    }

    private void scanDirectory(File directory, Set<String> codes) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, codes);
            } else {
                String name = file.getName();
                if (name.startsWith("messages_") && name.endsWith(".properties")) {
                    String code = extractLanguageCode(name);
                    if (!code.isEmpty()) {
                        codes.add(code);
                    }
                }
            }
        }
    }

    private String extractLanguageCode(String fileName) {
        String name = fileName;

        int slashIndex = name.lastIndexOf('/');
        if (slashIndex >= 0) {
            name = name.substring(slashIndex + 1);
        }

        if (!name.startsWith("messages_") || !name.endsWith(".properties")) {
            return "";
        }

        String code = name.substring("messages_".length(), name.length() - ".properties".length()).trim();

        if (code.isEmpty()) {
            return "";
        }

        return code;
    }

    private List<String> getFallbackLanguages() {
        List<String> fallback = new ArrayList<String>();
        fallback.add("de");
        fallback.add("en");
        fallback.add("sr");
        return fallback;
    }
}