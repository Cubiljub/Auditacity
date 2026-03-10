package org.cubicode;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntryRecord {

    private final Map<FieldDefinition, String> values = new LinkedHashMap<>();

    public void putValue(FieldDefinition field, String value) {
        values.put(field, value);
    }

    public String getValue(FieldDefinition field) {
        return values.getOrDefault(field, "");
    }

    public Map<FieldDefinition, String> getValues() {
        return values;
    }
}