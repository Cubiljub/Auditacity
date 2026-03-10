package org.cubicode;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class DynamicTableModel extends AbstractTableModel {

    private List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
    private final List<EntryRecord> entries = new ArrayList<EntryRecord>();

    public void setFields(List<FieldDefinition> fields) {
        this.fields = new ArrayList<FieldDefinition>(fields);
        fireTableStructureChanged();
    }

    public void setEntries(List<EntryRecord> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        fireTableDataChanged();
    }

    public void addEntry(EntryRecord entry) {
        entries.add(entry);
        fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
    }

    public void updateEntry(int rowIndex, EntryRecord updatedEntry) {
        if (rowIndex < 0 || rowIndex >= entries.size()) {
            return;
        }

        entries.set(rowIndex, updatedEntry);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public EntryRecord getEntryAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= entries.size()) {
            return null;
        }

        return entries.get(rowIndex);
    }

    public void removeEntry(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= entries.size()) {
            return;
        }

        entries.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void clearEntries() {
        entries.clear();
        fireTableDataChanged();
    }

    public List<EntryRecord> getEntries() {
        return new ArrayList<EntryRecord>(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return fields.size() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Nr.";
        }

        return fields.get(column - 1).getName();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return rowIndex + 1;
        }

        EntryRecord entry = entries.get(rowIndex);
        FieldDefinition field = fields.get(columnIndex - 1);
        return entry.getValue(field);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Integer.class;
        }

        return String.class;
    }
}