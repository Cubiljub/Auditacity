package org.cubicode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

public class SettingsDialog extends JDialog {

    private DefaultListModel<FieldDefinition> fieldListModel;
    private JList<FieldDefinition> fieldList;

    private boolean saved = false;

    private final LanguageManager languageManager;
    private final ThemeMode currentTheme;
    private final ProfileService profileService = new ProfileService();

    private JComboBox<LanguageOption> languageComboBox;
    private JComboBox<ThemeOption> themeComboBox;
    private JComboBox<String> profileComboBox;

    private String initialProfileName;

    public SettingsDialog(JFrame parent,
                          List<FieldDefinition> existingFields,
                          LanguageManager languageManager,
                          ThemeMode currentTheme,
                          String initialProfileName) {
        super(parent, languageManager.get("dialog.settings"), true);
        this.languageManager = languageManager;
        this.currentTheme = currentTheme;
        this.initialProfileName = initialProfileName == null ? "" : initialProfileName;
        initUI();
        loadFields(existingFields);
        refreshProfileList();
    }

    private void initUI() {
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(languageManager.get("tab.appearance"), createAppearancePanel());
        tabs.addTab(languageManager.get("tab.fields"), createFieldsPanel());
        tabs.addTab(languageManager.get("tab.language"), createLanguagePanel());

        add(tabs, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private void loadFields(List<FieldDefinition> existingFields) {
        for (FieldDefinition field : existingFields) {
            fieldListModel.addElement(new FieldDefinition(
                    field.getId(),
                    field.getName(),
                    field.getType(),
                    new ArrayList<String>(field.getOptions())
            ));
        }
    }

    private JPanel createAppearancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel themeLabel = new JLabel(languageManager.get("label.theme"));

        themeComboBox = new JComboBox<ThemeOption>(new ThemeOption[]{
                new ThemeOption(ThemeMode.LIGHT, languageManager.get("theme.light")),
                new ThemeOption(ThemeMode.DARK, languageManager.get("theme.dark")),
                new ThemeOption(ThemeMode.SYSTEM, languageManager.get("theme.system"))
        });

        selectCurrentTheme();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(themeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(themeComboBox, gbc);

        return panel;
    }

    private void selectCurrentTheme() {
        for (int i = 0; i < themeComboBox.getItemCount(); i++) {
            ThemeOption option = themeComboBox.getItemAt(i);
            if (option.getThemeMode() == currentTheme) {
                themeComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private JPanel createFieldsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        fieldListModel = new DefaultListModel<FieldDefinition>();
        fieldList = new JList<FieldDefinition>(fieldListModel);

        configureFieldListDragAndDrop();

        JScrollPane scrollPane = new JScrollPane(fieldList);

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.add(createProfileBar(), BorderLayout.NORTH);
        topPanel.add(createFieldButtonBar(), BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void configureFieldListDragAndDrop() {
        fieldList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fieldList.setDragEnabled(true);
        fieldList.setDropMode(DropMode.INSERT);
        fieldList.setTransferHandler(new FieldListTransferHandler());
    }

    private JPanel createProfileBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JLabel profileLabel = new JLabel("Profile:");

        profileComboBox = new JComboBox<String>();
        profileComboBox.setEditable(true);
        profileComboBox.setPreferredSize(new Dimension(220, 26));

        JButton loadButton = new JButton("Load");
        JButton saveProfileButton = new JButton("Save Profile");
        JButton deleteProfileButton = new JButton("Delete Profile");

        loadButton.addActionListener(e -> loadSelectedProfile());
        saveProfileButton.addActionListener(e -> saveCurrentProfile());
        deleteProfileButton.addActionListener(e -> deleteSelectedProfileFromDisk());

        panel.add(profileLabel);
        panel.add(profileComboBox);
        panel.add(loadButton);
        panel.add(saveProfileButton);
        panel.add(deleteProfileButton);

        return panel;
    }

    private JPanel createFieldButtonBar() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton addButton = new JButton(languageManager.get("button.add"));
        JButton editButton = new JButton(languageManager.get("button.edit"));
        JButton removeButton = new JButton(languageManager.get("button.remove"));

        addButton.addActionListener(e -> openAddFieldDialog());
        editButton.addActionListener(e -> openEditFieldDialog());
        removeButton.addActionListener(e -> removeSelectedField());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);

        return buttonPanel;
    }

    private void refreshProfileList() {
        if (profileComboBox == null) {
            return;
        }

        String currentValue = getCurrentProfileName();

        profileComboBox.removeAllItems();

        List<String> profiles = profileService.listProfileNames();
        for (String profile : profiles) {
            profileComboBox.addItem(profile);
        }

        if (!currentValue.trim().isEmpty()) {
            profileComboBox.setSelectedItem(currentValue);
        } else if (!initialProfileName.trim().isEmpty()) {
            profileComboBox.setSelectedItem(initialProfileName);
        }
    }

    private String getCurrentProfileName() {
        Object editorValue = profileComboBox.getEditor().getItem();
        return editorValue == null ? "" : editorValue.toString().trim();
    }

    private void saveCurrentProfile() {
        String profileName = getCurrentProfileName();

        if (profileName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Profile name must not be empty.",
                    languageManager.get("dialog.error"),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        profileService.saveProfile(profileName, getFieldDefinitions());
        refreshProfileList();

        JOptionPane.showMessageDialog(
                this,
                "Profile saved successfully.",
                languageManager.get("dialog.success"),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void loadSelectedProfile() {
        String profileName = getCurrentProfileName();

        if (profileName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select or enter a profile name first.",
                    languageManager.get("dialog.information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        List<FieldDefinition> loadedFields = profileService.loadProfile(profileName);

        fieldListModel.clear();
        for (FieldDefinition field : loadedFields) {
            fieldListModel.addElement(field);
        }
    }

    private void deleteSelectedProfileFromDisk() {
        String profileName = getCurrentProfileName();

        if (profileName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select or enter a profile name first.",
                    languageManager.get("dialog.information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you really want to delete this profile?",
                "Confirm profile deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            profileService.deleteProfile(profileName);
            refreshProfileList();
        }
    }

    private void openAddFieldDialog() {
        FieldEditorDialog dialog = new FieldEditorDialog((JFrame) getParent(), languageManager);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            fieldListModel.addElement(dialog.getFieldDefinition());
        }
    }

    private void openEditFieldDialog() {
        int selectedIndex = fieldList.getSelectedIndex();

        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.selectFieldFirst"),
                    languageManager.get("dialog.information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        FieldDefinition selectedField = fieldListModel.get(selectedIndex);

        FieldEditorDialog dialog = new FieldEditorDialog((JFrame) getParent(), selectedField, languageManager);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            fieldListModel.set(selectedIndex, dialog.getFieldDefinition());
        }
    }

    private void removeSelectedField() {
        int selectedIndex = fieldList.getSelectedIndex();

        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.selectFieldFirst"),
                    languageManager.get("dialog.information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        fieldListModel.remove(selectedIndex);
    }

    private JPanel createLanguagePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel languageLabel = new JLabel(languageManager.get("label.language"));

        languageComboBox = new JComboBox<LanguageOption>(new LanguageOption[]{
                new LanguageOption("de", languageManager.get("language.german")),
                new LanguageOption("en", languageManager.get("language.english")),
                new LanguageOption("sr", languageManager.get("language.serbian"))
        });

        selectCurrentLanguage();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(languageLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(languageComboBox, gbc);

        return panel;
    }

    private void selectCurrentLanguage() {
        String currentLanguage = languageManager.getCurrentLanguageCode();

        for (int i = 0; i < languageComboBox.getItemCount(); i++) {
            LanguageOption option = languageComboBox.getItemAt(i);
            if (option.getCode().equals(currentLanguage)) {
                languageComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton saveButton = new JButton(languageManager.get("button.save"));
        JButton cancelButton = new JButton(languageManager.get("button.cancel"));

        saveButton.addActionListener(e -> saveSettings());
        cancelButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        return panel;
    }

    private void saveSettings() {
        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public List<FieldDefinition> getFieldDefinitions() {
        List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

        for (int i = 0; i < fieldListModel.size(); i++) {
            fields.add(fieldListModel.get(i));
        }

        return fields;
    }

    public String getSelectedLanguageCode() {
        LanguageOption selected = (LanguageOption) languageComboBox.getSelectedItem();
        return selected == null ? "en" : selected.getCode();
    }

    public ThemeMode getSelectedThemeMode() {
        ThemeOption selected = (ThemeOption) themeComboBox.getSelectedItem();
        return selected == null ? ThemeMode.SYSTEM : selected.getThemeMode();
    }

    public String getSelectedProfileName() {
        return getCurrentProfileName();
    }

    private static class LanguageOption {
        private final String code;
        private final String displayName;

        public LanguageOption(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static class ThemeOption {
        private final ThemeMode themeMode;
        private final String displayName;

        public ThemeOption(ThemeMode themeMode, String displayName) {
            this.themeMode = themeMode;
            this.displayName = displayName;
        }

        public ThemeMode getThemeMode() {
            return themeMode;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private class FieldListTransferHandler extends TransferHandler {

        private final DataFlavor indexFlavor = new DataFlavor(Integer.class, "Integer Row Index");
        private int draggedIndex = -1;

        @Override
        protected Transferable createTransferable(JComponent c) {
            draggedIndex = fieldList.getSelectedIndex();

            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{indexFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return indexFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) {
                    return draggedIndex;
                }
            };
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop() && support.isDataFlavorSupported(indexFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
                int targetIndex = dropLocation.getIndex();

                Integer sourceIndex = (Integer) support.getTransferable().getTransferData(indexFlavor);

                if (sourceIndex == null || sourceIndex < 0 || sourceIndex >= fieldListModel.size()) {
                    return false;
                }

                FieldDefinition movedItem = fieldListModel.get(sourceIndex);

                if (sourceIndex < targetIndex) {
                    targetIndex--;
                }

                fieldListModel.remove(sourceIndex);
                fieldListModel.add(targetIndex, movedItem);
                fieldList.setSelectedIndex(targetIndex);

                return true;

            } catch (Exception ex) {
                return false;
            }
        }
    }
}