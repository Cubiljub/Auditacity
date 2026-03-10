package org.cubicode;

public class LanguageOption {

    private final String code;
    private final String displayName;

    public LanguageOption(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return displayName;
    }
}