package org.cubicode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PreferencesService {

    private final ObjectMapper objectMapper;
    private final Path preferencesFile;

    public PreferencesService() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        preferencesFile = Paths.get(
                System.getenv("LOCALAPPDATA"),
                "Auditacity",
                "preferences.json"
        );
    }

    public AppPreferences loadPreferences() {
        try {
            Files.createDirectories(preferencesFile.getParent());

            if (!Files.exists(preferencesFile)) {
                return new AppPreferences();
            }

            return objectMapper.readValue(preferencesFile.toFile(), AppPreferences.class);

        } catch (Exception ex) {
            return new AppPreferences();
        }
    }

    public void savePreferences(AppPreferences preferences) {
        try {
            Files.createDirectories(preferencesFile.getParent());
            objectMapper.writeValue(preferencesFile.toFile(), preferences);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save preferences.", ex);
        }
    }
}