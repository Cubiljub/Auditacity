package org.cubicode;

import java.util.Locale;
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
}