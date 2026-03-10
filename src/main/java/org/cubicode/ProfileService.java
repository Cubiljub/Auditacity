package org.cubicode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileService {

    private final ObjectMapper objectMapper;
    private final Path profilesDirectory;

    public ProfileService() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        profilesDirectory = Paths.get(
                System.getenv("LOCALAPPDATA"),
                "Auditacity",
                "profiles"
        );
    }

    public List<String> listProfileNames() {
        try {
            Files.createDirectories(profilesDirectory);

            List<String> names = new ArrayList<String>();
            File[] files = profilesDirectory.toFile().listFiles();

            if (files == null) {
                return names;
            }

            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    FieldProfile profile = objectMapper.readValue(file, FieldProfile.class);
                    if (profile.getName() != null && !profile.getName().trim().isEmpty()) {
                        names.add(profile.getName());
                    }
                }
            }

            Collections.sort(names);
            return names;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to list profiles.", ex);
        }
    }

    public void saveProfile(String profileName, List<FieldDefinition> fields) {
        try {
            Files.createDirectories(profilesDirectory);

            List<FieldDefinition> copy = new ArrayList<FieldDefinition>();
            for (FieldDefinition field : fields) {
                copy.add(new FieldDefinition(
                        field.getId(),
                        field.getName(),
                        field.getType(),
                        new ArrayList<String>(field.getOptions())
                ));
            }

            FieldProfile profile = new FieldProfile(profileName, copy);
            objectMapper.writeValue(getProfileFile(profileName).toFile(), profile);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to save profile.", ex);
        }
    }

    public List<FieldDefinition> loadProfile(String profileName) {
        try {
            File file = getProfileFile(profileName).toFile();

            if (!file.exists()) {
                return new ArrayList<FieldDefinition>();
            }

            FieldProfile profile = objectMapper.readValue(file, FieldProfile.class);
            List<FieldDefinition> result = new ArrayList<FieldDefinition>();

            for (FieldDefinition field : profile.getFields()) {
                result.add(new FieldDefinition(
                        field.getId(),
                        field.getName(),
                        field.getType(),
                        new ArrayList<String>(field.getOptions())
                ));
            }

            return result;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to load profile.", ex);
        }
    }

    public void deleteProfile(String profileName) {
        try {
            Files.deleteIfExists(getProfileFile(profileName));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete profile.", ex);
        }
    }

    private Path getProfileFile(String profileName) {
        String safeName = profileName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        return profilesDirectory.resolve(safeName + ".json");
    }
}