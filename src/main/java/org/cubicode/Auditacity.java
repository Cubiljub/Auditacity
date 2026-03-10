package org.cubicode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Auditacity extends JFrame {

    private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
    private final Map<FieldDefinition, JComponent> inputComponents = new LinkedHashMap<FieldDefinition, JComponent>();
    private final ExportService exportService = new ExportService();
    private final LanguageManager languageManager = new LanguageManager();
    private final ThemeManager themeManager = new ThemeManager();
    private final PreferencesService preferencesService = new PreferencesService();
    private final ProfileService profileService = new ProfileService();

    private AppPreferences preferences;
    private ThemeMode currentTheme = ThemeMode.SYSTEM;
    private String currentProfileName = "";
    private JPanel formPanel;
    private JTable table;
    private DynamicTableModel tableModel;
    private TableRowSorter<DynamicTableModel> tableSorter;
    private JTextField searchField;

    public Auditacity() {
        loadStartupPreferences();
        initUI();
        loadStartupProfile();
    }

    private void loadStartupPreferences() {
        preferences = preferencesService.loadPreferences();

        languageManager.setLanguage(preferences.getLanguageCode());

        currentTheme = preferences.getThemeMode();
        themeManager.applyTheme(currentTheme);

        currentProfileName = preferences.getLastProfileName() == null
                ? ""
                : preferences.getLastProfileName();
    }

    private void loadStartupProfile() {
        if (currentProfileName == null || currentProfileName.trim().isEmpty()) {
            return;
        }

        List<FieldDefinition> loadedFields = profileService.loadProfile(currentProfileName);

        if (!loadedFields.isEmpty()) {
            fields.clear();
            fields.addAll(loadedFields);

            if (tableModel != null) {
                tableModel.setFields(fields);
                configureTableSorting();
                configureNumberColumn();
            }

            rebuildForm();
        }
    }

    private void savePreferences() {
        preferences.setLanguageCode(languageManager.getCurrentLanguageCode());
        preferences.setThemeMode(currentTheme);
        preferences.setLastProfileName(currentProfileName == null ? "" : currentProfileName);

        preferencesService.savePreferences(preferences);
    }

    private List<EntryRecord> remapEntries(List<FieldDefinition> oldFields,
                                           List<FieldDefinition> newFields,
                                           List<EntryRecord> oldEntries) {
        List<EntryRecord> remappedEntries = new ArrayList<EntryRecord>();

        for (EntryRecord oldEntry : oldEntries) {
            EntryRecord newEntry = new EntryRecord();

            for (FieldDefinition newField : newFields) {
                String value = "";

                for (FieldDefinition oldField : oldFields) {
                    if (oldField.getId().equals(newField.getId())) {
                        value = oldEntry.getValue(oldField);
                        break;
                    }
                }

                newEntry.putValue(newField, value);
            }

            remappedEntries.add(newEntry);
        }

        return remappedEntries;
    }

    private void initUI() {
        setTitle(languageManager.get("app.title"));
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);
        add(createMainTabs(), BorderLayout.CENTER);

        rebuildForm();
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEtchedBorder());

        JButton settingsButton = new JButton(languageManager.get("button.settings"));
        settingsButton.setMargin(new Insets(1, 1, 1, 1));
        settingsButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        settingsButton.addActionListener(e -> openSettingsDialog());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        leftPanel.add(settingsButton);

        topBar.add(leftPanel, BorderLayout.WEST);

        return topBar;
    }

    private JTabbedPane createMainTabs() {
        JTabbedPane tabs = new JTabbedPane();

        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.setBorder(null);

        tableModel = new DynamicTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        configureTableSorting();
        configureNumberColumn();
        installTableDoubleClickEdit();

        JPanel tablePanel = new JPanel(new BorderLayout());

        JScrollPane tableScrollPane = new JScrollPane(table);
        tablePanel.add(createTableTopBar(), BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        tablePanel.add(createTableButtonBar(), BorderLayout.SOUTH);

        tabs.addTab(languageManager.get("tab.form"), formScrollPane);
        tabs.addTab(languageManager.get("tab.table"), tablePanel);

        return tabs;
    }

    private void configureTableSorting() {
        tableSorter = new TableRowSorter<DynamicTableModel>(tableModel);

        tableSorter.setComparator(0, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Integer.compare(a, b);
            }
        });

        Comparator<String> customTextComparator = new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                String left = a == null ? "" : a.trim();
                String right = b == null ? "" : b.trim();

                boolean leftIsNumber = isPureNumber(left);
                boolean rightIsNumber = isPureNumber(right);

                if (leftIsNumber && rightIsNumber) {
                    try {
                        long leftNumber = Long.parseLong(left);
                        long rightNumber = Long.parseLong(right);
                        return Long.compare(leftNumber, rightNumber);
                    } catch (NumberFormatException ignored) {
                    }
                }

                if (leftIsNumber && !rightIsNumber) {
                    return -1;
                }

                if (!leftIsNumber && rightIsNumber) {
                    return 1;
                }

                int ignoreCaseResult = left.compareToIgnoreCase(right);
                if (ignoreCaseResult != 0) {
                    return ignoreCaseResult;
                }

                return left.compareTo(right);
            }
        };

        for (int i = 1; i < tableModel.getColumnCount(); i++) {
            tableSorter.setComparator(i, customTextComparator);
        }

        table.setRowSorter(tableSorter);
    }

    private void applyTableFilter() {
        if (tableSorter == null || searchField == null) {
            return;
        }

        String text = searchField.getText();

        if (text == null || text.trim().isEmpty()) {
            tableSorter.setRowFilter(null);
            return;
        }

        final String searchText = text.trim().toLowerCase();

        tableSorter.setRowFilter(new RowFilter<DynamicTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DynamicTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object value = entry.getValue(i);
                    if (value != null && value.toString().toLowerCase().contains(searchText)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private JPanel createTableTopBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel searchLabel = new JLabel(languageManager.get("label.search"));
        searchField = new JTextField();

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyTableFilter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyTableFilter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyTableFilter();
            }
        });

        panel.add(searchLabel, BorderLayout.WEST);
        panel.add(searchField, BorderLayout.CENTER);

        return panel;
    }

    private void configureNumberColumn() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);

        table.getColumnModel().getColumn(0).setCellRenderer(renderer);
    }

    private boolean isPureNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private void installTableDoubleClickEdit() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    editSelectedEntry();
                }
            }
        });
    }

    private JPanel createTableButtonBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton importButton = new JButton(languageManager.get("button.import"));
        JButton exportButton = new JButton(languageManager.get("button.export"));
        JButton editButton = new JButton(languageManager.get("button.edit"));
        JButton deleteButton = new JButton(languageManager.get("button.deleteSelected"));

        importButton.addActionListener(e -> importEntries());
        exportButton.addActionListener(e -> exportEntries());
        editButton.addActionListener(e -> editSelectedEntry());
        deleteButton.addActionListener(e -> deleteSelectedEntry());

        panel.add(importButton);
        panel.add(exportButton);
        panel.add(editButton);
        panel.add(deleteButton);

        return panel;
    }

    private void importEntries() {
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter excelFilter = exportService.createExcelFilter();
        FileNameExtensionFilter csvFilter = exportService.createCsvFilter();

        fileChooser.addChoosableFileFilter(excelFilter);
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setFileFilter(excelFilter);

        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        try {
            ImportData importData;

            String fileName = selectedFile.getName().toLowerCase();

            if (fileName.endsWith(".xlsx")) {
                importData = exportService.importFromExcel(selectedFile);
            } else if (fileName.endsWith(".csv")) {
                importData = exportService.importFromCsv(selectedFile);
            } else {
                throw new IllegalArgumentException(languageManager.get("message.onlyExportFormats"));
            }

            String suggestedProfileName = removeFileExtension(selectedFile.getName());

            String profileName = (String) JOptionPane.showInputDialog(
                    this,
                    languageManager.get("message.enterProfileName"),
                    languageManager.get("dialog.import"),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    suggestedProfileName
            );

            if (profileName == null) {
                return;
            }

            profileName = profileName.trim();
            if (profileName.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        languageManager.get("message.profileNameEmpty"),
                        languageManager.get("dialog.error"),
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            fields.clear();
            fields.addAll(importData.getFields());

            tableModel.setFields(fields);
            tableModel.setEntries(importData.getEntries());

            configureTableSorting();
            configureNumberColumn();
            rebuildForm();

            currentProfileName = profileName;
            profileService.saveProfile(profileName, fields);
            savePreferences();

            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.importSuccess"),
                    languageManager.get("dialog.success"),
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.importFailed") + " " + ex.getMessage(),
                    languageManager.get("dialog.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

    private void exportEntries() {
        if (fields.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.noFieldsDefined"),
                    languageManager.get("dialog.information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (tableModel.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.noEntriesToExport"),
                    languageManager.get("dialog.information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        Object[] exportOptions = {
                languageManager.get("button.normalExport"),
                languageManager.get("button.databaseExport"),
                languageManager.get("button.cancel")
        };

        int exportMode = JOptionPane.showOptionDialog(
                this,
                languageManager.get("message.chooseExportType"),
                languageManager.get("dialog.export"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                exportOptions,
                exportOptions[0]
        );

        if (exportMode == 2 || exportMode == JOptionPane.CLOSED_OPTION) {
            return;
        }

        boolean databaseMode = exportMode == 1;

        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter excelFilter = exportService.createExcelFilter();
        FileNameExtensionFilter csvFilter = exportService.createCsvFilter();

        fileChooser.addChoosableFileFilter(excelFilter);
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setFileFilter(excelFilter);

        String today = new SimpleDateFormat("dd_MM_yyyy").format(new Date());
        fileChooser.setSelectedFile(new File("Auditacity_" + today));

        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = exportService.ensureCorrectExtension(
                fileChooser.getSelectedFile(),
                fileChooser.getFileFilter()
        );

        try {
            String fileName = selectedFile.getName().toLowerCase();

            if (fileName.endsWith(".xlsx")) {
                if (databaseMode) {
                    exportService.exportToExcelDb(selectedFile, fields, tableModel.getEntries());
                } else {
                    exportService.exportToExcel(selectedFile, fields, tableModel.getEntries());
                }
            } else if (fileName.endsWith(".csv")) {
                if (databaseMode) {
                    exportService.exportToCsvDb(selectedFile, fields, tableModel.getEntries());
                } else {
                    exportService.exportToCsv(selectedFile, fields, tableModel.getEntries());
                }
            } else {
                throw new IllegalArgumentException(languageManager.get("message.onlyExportFormats"));
            }

            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.exportSuccess"),
                    languageManager.get("dialog.success"),
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.exportFailed") + " " + ex.getMessage(),
                    languageManager.get("dialog.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void editSelectedEntry() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.selectEntryFirst"),
                    languageManager.get("dialog.information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        EntryRecord existingEntry = tableModel.getEntryAt(modelRow);

        if (existingEntry == null) {
            return;
        }

        Map<FieldDefinition, JComponent> editorInputs = new LinkedHashMap<FieldDefinition, JComponent>();

        JPanel editorPanel = new JPanel(new GridBagLayout());
        editorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;

        for (FieldDefinition field : fields) {
            JLabel label = new JLabel(field.getName() + ":");
            JComponent input = createEditorInputComponent(field, existingEntry.getValue(field));

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.4;
            editorPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.6;
            editorPanel.add(input, gbc);

            editorInputs.put(field, input);
            row++;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                editorPanel,
                languageManager.get("button.edit"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            EntryRecord updatedEntry = new EntryRecord();

            for (FieldDefinition field : fields) {
                JComponent component = editorInputs.get(field);
                String value = readComponentValue(component);
                updatedEntry.putValue(field, value);
            }

            tableModel.updateEntry(modelRow, updatedEntry);
        }
    }

    private JComponent createEditorInputComponent(FieldDefinition field, String currentValue) {
        if (field.getType() == FieldType.TEXT) {
            JTextField textField = new JTextField();
            textField.setText(currentValue == null ? "" : currentValue);
            return textField;
        }

        if (field.getType() == FieldType.SELECTION) {
            JComboBox<String> comboBox = new JComboBox<String>(field.getOptions().toArray(new String[0]));
            comboBox.setSelectedItem(currentValue);
            return comboBox;
        }

        JTextField textField = new JTextField();
        textField.setText(currentValue == null ? "" : currentValue);
        return textField;
    }

    private void deleteSelectedEntry() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.selectEntryFirst"),
                    languageManager.get("dialog.information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                languageManager.get("message.confirmDeleteEntry"),
                languageManager.get("dialog.confirmDeletion"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeEntry(modelRow);
        }
    }

    private void openSettingsDialog() {
        List<FieldDefinition> oldFields = new ArrayList<FieldDefinition>(fields);
        List<EntryRecord> oldEntries = tableModel.getEntries();

        SettingsDialog dialog = new SettingsDialog(
                this,
                fields,
                languageManager,
                currentTheme,
                currentProfileName
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            List<FieldDefinition> newFields = dialog.getFieldDefinitions();
            List<EntryRecord> remappedEntries = remapEntries(oldFields, newFields, oldEntries);

            fields.clear();
            fields.addAll(newFields);

            String selectedLanguage = dialog.getSelectedLanguageCode();
            languageManager.setLanguage(selectedLanguage);

            currentTheme = dialog.getSelectedThemeMode();
            themeManager.applyTheme(currentTheme);

            currentProfileName = dialog.getSelectedProfileName();

            getContentPane().removeAll();
            add(createTopBar(), BorderLayout.NORTH);
            add(createMainTabs(), BorderLayout.CENTER);

            tableModel.setFields(fields);
            tableModel.setEntries(remappedEntries);

            configureTableSorting();
            configureNumberColumn();
            rebuildForm();

            setTitle(languageManager.get("app.title"));

            savePreferences();

            SwingUtilities.updateComponentTreeUI(this);
            revalidate();
            repaint();
        }
    }

    private void addFieldDirectly() {
        FieldEditorDialog dialog = new FieldEditorDialog(this, languageManager);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            fields.add(dialog.getFieldDefinition());
            tableModel.setFields(fields);
            configureTableSorting();
            configureNumberColumn();
            rebuildForm();
            savePreferences();
        }
    }

    private void rebuildForm() {
        formPanel.removeAll();
        inputComponents.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;

        for (FieldDefinition field : fields) {
            JLabel label = new JLabel(field.getName() + ":");
            label.setHorizontalAlignment(SwingConstants.LEFT);

            JComponent input = createInputComponent(field);

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            gbc.weightx = 0.5;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.5;
            formPanel.add(input, gbc);

            inputComponents.put(field, input);
            row++;
        }

        if (!fields.isEmpty()) {
            JButton saveEntryButton = new JButton(languageManager.get("button.saveEntry"));
            saveEntryButton.addActionListener(e -> saveEntry());

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(saveEntryButton, gbc);

            row++;
        }

        JButton addButton = createPlusButton();

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(addButton, gbc);

        row++;

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(spacer, gbc);

        formPanel.revalidate();
        formPanel.repaint();
    }

    private void saveEntry() {
        EntryRecord entry = new EntryRecord();

        for (FieldDefinition field : fields) {
            JComponent component = inputComponents.get(field);
            String value = readComponentValue(component);
            entry.putValue(field, value);
        }

        tableModel.addEntry(entry);
        clearFormInputs();
    }

    private String readComponentValue(JComponent component) {
        if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            return textField.getText().trim();
        }

        if (component instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) component;
            Object selected = comboBox.getSelectedItem();
            return selected == null ? "" : selected.toString();
        }

        return "";
    }

    private void clearFormInputs() {
        for (JComponent component : inputComponents.values()) {
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                textField.setText("");
            } else if (component instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) component;
                if (comboBox.getItemCount() > 0) {
                    comboBox.setSelectedIndex(0);
                }
            }
        }
    }

    private JButton createPlusButton() {
        JButton button = new JButton("+");

        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText(languageManager.get("tooltip.addField"));

        button.addActionListener(e -> addFieldDirectly());

        return button;
    }

    private JComponent createInputComponent(FieldDefinition field) {
        if (field.getType() == FieldType.TEXT) {
            return new JTextField();
        }

        if (field.getType() == FieldType.SELECTION) {
            return new JComboBox<String>(field.getOptions().toArray(new String[0]));
        }

        return new JTextField();
    }

    public static void main(String[] args) {
        ThemeManager startupThemeManager = new ThemeManager();
        PreferencesService startupPreferencesService = new PreferencesService();
        AppPreferences startupPreferences = startupPreferencesService.loadPreferences();

        startupThemeManager.applyTheme(startupPreferences.getThemeMode());

        SwingUtilities.invokeLater(() -> {
            Auditacity app = new Auditacity();
            SwingUtilities.updateComponentTreeUI(app);
            app.setVisible(true);
        });
    }
}