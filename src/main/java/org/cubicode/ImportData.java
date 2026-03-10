package org.cubicode;

import java.util.ArrayList;
import java.util.List;

public class ImportData {

    private final List<FieldDefinition> fields;
    private final List<EntryRecord> entries;

    public ImportData(List<FieldDefinition> fields, List<EntryRecord> entries) {
        this.fields = new ArrayList<FieldDefinition>(fields);
        this.entries = new ArrayList<EntryRecord>(entries);
    }

    public List<FieldDefinition> getFields() {
        return new ArrayList<FieldDefinition>(fields);
    }

    public List<EntryRecord> getEntries() {
        return new ArrayList<EntryRecord>(entries);
    }
}