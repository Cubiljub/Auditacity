package org.cubicode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FieldEditorDialog extends JDialog {

    private JTextField fieldNameField;
    private JComboBox<FieldTypeOption> fieldTypeComboBox;
    private JTextArea optionsArea;
    private JScrollPane optionsScrollPane;
    private JLabel optionsLabel;
    private String editingFieldId;

    private boolean saved = false;
    private FieldDefinition fieldDefinition;

    private final LanguageManager languageManager;

    public FieldEditorDialog(JFrame parent, LanguageManager languageManager) {
        super(parent, languageManager.get("dialog.addField"), true);
        this.languageManager = languageManager;
        this.editingFieldId = null;
        initUI();
    }

    public FieldEditorDialog(JFrame parent, FieldDefinition existingField, LanguageManager languageManager) {
        super(parent, languageManager.get("dialog.editField"), true);
        this.languageManager = languageManager;
        this.editingFieldId = existingField.getId();
        initUI();
        loadField(existingField);
    }

    private void initUI() {
        setSize(450, 320);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel fieldNameLabel = new JLabel(languageManager.get("label.fieldName"));
        fieldNameField = new JTextField();

        JLabel fieldTypeLabel = new JLabel(languageManager.get("label.fieldType"));
        fieldTypeComboBox = new JComboBox<FieldTypeOption>(new FieldTypeOption[]{
                new FieldTypeOption(FieldType.TEXT, languageManager.get("fieldType.text")),
                new FieldTypeOption(FieldType.SELECTION, languageManager.get("fieldType.selection"))
        });

        optionsLabel = new JLabel(languageManager.get("label.options"));
        optionsArea = new JTextArea(6, 20);
        optionsScrollPane = new JScrollPane(optionsArea);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(fieldNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(fieldNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(fieldTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(fieldTypeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(optionsLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(optionsScrollPane, gbc);

        fieldTypeComboBox.addActionListener(e -> updateOptionsVisibility());
        updateOptionsVisibility();

        add(formPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private void loadField(FieldDefinition existingField) {
        fieldNameField.setText(existingField.getName());
        selectFieldType(existingField.getType());

        if (existingField.getOptions() != null && !existingField.getOptions().isEmpty()) {
            optionsArea.setText(String.join("\n", existingField.getOptions()));
        }

        updateOptionsVisibility();
    }

    private void selectFieldType(FieldType type) {
        for (int i = 0; i < fieldTypeComboBox.getItemCount(); i++) {
            FieldTypeOption option = fieldTypeComboBox.getItemAt(i);
            if (option.getType() == type) {
                fieldTypeComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void updateOptionsVisibility() {
        FieldType selectedType = getSelectedFieldType();
        boolean isSelection = selectedType == FieldType.SELECTION;

        optionsLabel.setVisible(isSelection);
        optionsScrollPane.setVisible(isSelection);

        revalidate();
        repaint();
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton saveButton = new JButton(languageManager.get("button.save"));
        JButton cancelButton = new JButton(languageManager.get("button.cancel"));

        saveButton.addActionListener(e -> saveField());
        cancelButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        return panel;
    }

    private void saveField() {
        String fieldName = fieldNameField.getText().trim();
        FieldType fieldType = getSelectedFieldType();

        if (fieldName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    languageManager.get("message.fieldNameEmpty"),
                    languageManager.get("dialog.error"),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        List<String> options = new ArrayList<String>();

        if (fieldType == FieldType.SELECTION) {
            String[] lines = optionsArea.getText().split("\\R");

            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    options.add(trimmed);
                }
            }

            if (options.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        languageManager.get("message.selectionNeedsOptions"),
                        languageManager.get("dialog.error"),
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        fieldDefinition = new FieldDefinition(editingFieldId, fieldName, fieldType, options);
        saved = true;
        dispose();
    }

    private FieldType getSelectedFieldType() {
        FieldTypeOption selected = (FieldTypeOption) fieldTypeComboBox.getSelectedItem();
        return selected == null ? FieldType.TEXT : selected.getType();
    }

    public boolean isSaved() {
        return saved;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    private static class FieldTypeOption {
        private final FieldType type;
        private final String displayName;

        public FieldTypeOption(FieldType type, String displayName) {
            this.type = type;
            this.displayName = displayName;
        }

        public FieldType getType() {
            return type;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}