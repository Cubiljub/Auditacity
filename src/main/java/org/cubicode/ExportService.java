package org.cubicode;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ExportService {

    public FileNameExtensionFilter createExcelFilter() {
        return new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx");
    }

    public FileNameExtensionFilter createCsvFilter() {
        return new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
    }

    public File ensureCorrectExtension(File file, FileFilter selectedFilter) {
        String fileName = file.getName().toLowerCase();

        if (selectedFilter instanceof FileNameExtensionFilter) {
            FileNameExtensionFilter filter = (FileNameExtensionFilter) selectedFilter;
            String[] extensions = filter.getExtensions();

            if (extensions.length > 0) {
                String expectedExtension = "." + extensions[0].toLowerCase();
                if (!fileName.endsWith(expectedExtension)) {
                    return new File(file.getParent(), file.getName() + expectedExtension);
                }
            }
        }

        return file;
    }

    public void exportToExcel(File file, List<FieldDefinition> fields, List<EntryRecord> entries) throws IOException {
        exportToExcel(file, fields, entries, false);
    }

    public void exportToExcelDb(File file, List<FieldDefinition> fields, List<EntryRecord> entries) throws IOException {
        exportToExcel(file, fields, entries, true);
    }

    public void exportToCsv(File file, List<FieldDefinition> fields, List<EntryRecord> entries) throws IOException {
        exportToCsv(file, fields, entries, false);
    }

    public void exportToCsvDb(File file, List<FieldDefinition> fields, List<EntryRecord> entries) throws IOException {
        exportToCsv(file, fields, entries, true);
    }

    private void exportToExcel(File file,
                               List<FieldDefinition> fields,
                               List<EntryRecord> entries,
                               boolean databaseMode) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("Entries");

            Row headerRow = sheet.createRow(0);

            int startColumn = 0;

            if (!databaseMode) {
                headerRow.createCell(0).setCellValue("Nr.");
                startColumn = 1;
            }

            for (int i = 0; i < fields.size(); i++) {
                String headerName = databaseMode
                        ? normalizeColumnName(fields.get(i).getName())
                        : fields.get(i).getName();

                headerRow.createCell(i + startColumn).setCellValue(headerName);
            }

            for (int rowIndex = 0; rowIndex < entries.size(); rowIndex++) {
                EntryRecord entry = entries.get(rowIndex);
                Row row = sheet.createRow(rowIndex + 1);

                if (!databaseMode) {
                    row.createCell(0).setCellValue(rowIndex + 1);
                }

                for (int colIndex = 0; colIndex < fields.size(); colIndex++) {
                    FieldDefinition field = fields.get(colIndex);
                    row.createCell(colIndex + startColumn).setCellValue(entry.getValue(field));
                }
            }

            int totalColumns = fields.size() + (databaseMode ? 0 : 1);
            for (int i = 0; i < totalColumns; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    private void exportToCsv(File file,
                             List<FieldDefinition> fields,
                             List<EntryRecord> entries,
                             boolean databaseMode) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            if (!databaseMode) {
                writer.write("Nr.");
                for (FieldDefinition field : fields) {
                    writer.write(",");
                    writer.write(escapeCsv(field.getName()));
                }
            } else {
                for (int i = 0; i < fields.size(); i++) {
                    if (i > 0) {
                        writer.write(",");
                    }
                    writer.write(escapeCsv(normalizeColumnName(fields.get(i).getName())));
                }
            }

            writer.newLine();

            for (int rowIndex = 0; rowIndex < entries.size(); rowIndex++) {
                EntryRecord entry = entries.get(rowIndex);

                if (!databaseMode) {
                    writer.write(String.valueOf(rowIndex + 1));

                    for (FieldDefinition field : fields) {
                        writer.write(",");
                        writer.write(escapeCsv(entry.getValue(field)));
                    }
                } else {
                    for (int i = 0; i < fields.size(); i++) {
                        if (i > 0) {
                            writer.write(",");
                        }
                        writer.write(escapeCsv(entry.getValue(fields.get(i))));
                    }
                }

                writer.newLine();
            }
        }
    }

    public ImportData importFromCsv(File file) throws IOException {
        List<List<String>> rows = new ArrayList<List<String>>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                rows.add(parseCsvLine(line));
            }
        }

        return buildImportData(rows);
    }

    public ImportData importFromExcel(File file) throws IOException {
        List<List<String>> rows = new ArrayList<List<String>>();

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                List<String> values = new ArrayList<String>();

                int lastCell = row.getLastCellNum();
                if (lastCell < 0) {
                    rows.add(values);
                    continue;
                }

                for (int i = 0; i < lastCell; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    values.add(cell == null ? "" : formatter.formatCellValue(cell));
                }

                rows.add(values);
            }
        }

        return buildImportData(rows);
    }

    private ImportData buildImportData(List<List<String>> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("The selected file is empty.");
        }

        List<String> header = rows.get(0);
        if (header.isEmpty()) {
            throw new IllegalArgumentException("The file has no header row.");
        }

        List<Integer> includedColumnIndexes = new ArrayList<Integer>();
        List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

        for (int i = 0; i < header.size(); i++) {
            String rawName = safeTrim(header.get(i));

            if (rawName.isEmpty()) {
                continue;
            }

            if (isNumberingColumn(rawName)) {
                continue;
            }

            includedColumnIndexes.add(i);
            fields.add(new FieldDefinition(rawName, FieldType.TEXT, new ArrayList<String>()));
        }

        if (fields.isEmpty()) {
            throw new IllegalArgumentException("No usable columns were found in the header row.");
        }

        List<EntryRecord> entries = new ArrayList<EntryRecord>();

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<String> rowValues = rows.get(rowIndex);
            EntryRecord entry = new EntryRecord();

            boolean hasAnyValue = false;

            for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
                int sourceColumnIndex = includedColumnIndexes.get(fieldIndex);

                String value = "";
                if (sourceColumnIndex < rowValues.size()) {
                    value = safeTrim(rowValues.get(sourceColumnIndex));
                }

                if (!value.isEmpty()) {
                    hasAnyValue = true;
                }

                entry.putValue(fields.get(fieldIndex), value);
            }

            if (hasAnyValue) {
                entries.add(entry);
            }
        }

        return new ImportData(fields, entries);
    }

    private boolean isNumberingColumn(String name) {
        String normalized = name.trim().toLowerCase();
        return normalized.equals("nr")
                || normalized.equals("nr.")
                || normalized.equals("no")
                || normalized.equals("no.")
                || normalized.equals("#");
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuotes =
                value.contains(",") ||
                        value.contains("\"") ||
                        value.contains("\n") ||
                        value.contains("\r");

        if (!needsQuotes) {
            return value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<String>();
        StringBuilder current = new StringBuilder();

        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        result.add(current.toString());
        return result;
    }

    private String normalizeColumnName(String input) {
        if (input == null) {
            return "";
        }

        String result = input.trim().toLowerCase();
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{M}", "");
        result = result.replaceAll("[^a-z0-9]+", "_");
        result = result.replaceAll("^_+|_+$", "");

        if (result.isEmpty()) {
            result = "column";
        }

        return result;
    }
}