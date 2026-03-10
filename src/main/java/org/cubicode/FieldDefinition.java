package org.cubicode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FieldDefinition {

    private String id;
    private String name;
    private FieldType type;
    private List<String> options = new ArrayList<String>();

    public FieldDefinition() {
        this.id = UUID.randomUUID().toString();
    }

    public FieldDefinition(String name, FieldType type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
    }

    public FieldDefinition(String name, FieldType type, List<String> options) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.options = options;
    }

    public FieldDefinition(String id, String name, FieldType type, List<String> options) {
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.type = type;
        this.options = options == null ? new ArrayList<String>() : options;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FieldType getType() {
        return type;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return name;
    }
}