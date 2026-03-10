package org.cubicode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

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
        List<LanguageOption> result = new ArrayList<LanguageOption>();

        for (String code : loadLanguageCodes()) {
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

    private List<String> loadLanguageCodes() {
        List<String> codes = new ArrayList<String>();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("languages.properties")) {
            if (input == null) {
                return getFallbackLanguages();
            }

            Properties properties = new Properties();
            properties.load(input);

            String raw = properties.getProperty("languages", "").trim();
            if (raw.isEmpty()) {
                return getFallbackLanguages();
            }

            String[] parts = raw.split(",");
            for (String part : parts) {
                String code = part.trim();
                if (!code.isEmpty()) {
                    codes.add(code);
                }
            }

        } catch (Exception ignored) {
            return getFallbackLanguages();
        }

        if (codes.isEmpty()) {
            return getFallbackLanguages();
        }

        return codes;
    }

    private List<String> getFallbackLanguages() {
        List<String> fallback = new ArrayList<String>();
        fallback.add("en");
        fallback.add("de");
        fallback.add("sr");
        return fallback;
    }
}