package org.cubicode;

public class AppPreferences {

    private String languageCode = "en";
    private ThemeMode themeMode = ThemeMode.SYSTEM;
    private String lastProfileName = "";

    public AppPreferences() {
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public ThemeMode getThemeMode() {
        return themeMode;
    }

    public String getLastProfileName() {
        return lastProfileName;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
    }

    public void setLastProfileName(String lastProfileName) {
        this.lastProfileName = lastProfileName;
    }
}