package org.cubicode;

import java.util.ArrayList;
import java.util.List;

public class FieldProfile {

    private String name;
    private List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

    public FieldProfile() {
    }

    public FieldProfile(String name, List<FieldDefinition> fields) {
        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFields(List<FieldDefinition> fields) {
        this.fields = fields;
    }
}