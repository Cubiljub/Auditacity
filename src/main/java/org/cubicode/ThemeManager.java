package org.cubicode;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ThemeManager {

    private ThemeMode currentTheme = ThemeMode.SYSTEM;

    public void applyTheme(ThemeMode themeMode) {
        currentTheme = themeMode;

        try {
            switch (themeMode) {
                case LIGHT:
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;

                case DARK:
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;

                case SYSTEM:
                default:
                    if (isSystemDarkMode()) {
                        UIManager.setLookAndFeel(new FlatDarkLaf());
                    } else {
                        UIManager.setLookAndFeel(new FlatLightLaf());
                    }
                    break;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to apply theme.", ex);
        }
    }

    public ThemeMode getCurrentTheme() {
        return currentTheme;
    }

    private boolean isSystemDarkMode() {
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("win")) {
            return isWindowsDarkMode();
        }

        if (os.contains("mac")) {
            return isMacDarkMode();
        }

        return false;
    }

    private boolean isWindowsDarkMode() {
        try {
            Process process = new ProcessBuilder(
                    "reg",
                    "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v",
                    "AppsUseLightTheme"
            ).start();

            String output = readProcessOutput(process);
            process.waitFor();

            if (output.contains("0x0")) {
                return true;
            }

            if (output.contains("0x1")) {
                return false;
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    private boolean isMacDarkMode() {
        try {
            Process process = new ProcessBuilder(
                    "defaults",
                    "read",
                    "-g",
                    "AppleInterfaceStyle"
            ).start();

            String output = readProcessOutput(process);
            process.waitFor();

            return output.toLowerCase().contains("dark");

        } catch (Exception ignored) {
        }

        return false;
    }

    private String readProcessOutput(Process process) throws Exception {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }

        return builder.toString();
    }
}