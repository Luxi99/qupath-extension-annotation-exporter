package qupath.ext.template.ui;

import annotationexporter.core.FilterMode;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the export dialog.
 */
public class ExportDialogController {
    @FXML
    private CheckBox separateNucleiCheck;
    @FXML
    private CheckBox enableFilterCheck;
    @FXML
    private RadioButton ignoreRadio;
    @FXML
    private RadioButton includeRadio;
    @FXML
    private Label classNamesLabel;
    @FXML
    private TextField classNamesField;

    /**
     * Initializes the controls to their default values.
     */
    @FXML
    private void initialize() {
        enableFilterCheck.selectedProperty().addListener((obs, old, enabled) -> {
            ignoreRadio.setDisable(!enabled);
            includeRadio.setDisable(!enabled);
            classNamesLabel.setDisable(!enabled);
            classNamesField.setDisable(!enabled);
        });
    }

    /**
     * Creates a new {@code ExportConfig} based on the current state of the controls.
     * @return the {@code ExportConfig} that was created
     */
    public @NotNull ExportDialog.ExportConfig getConfig() {
        FilterMode mode;
        List<String> classes = List.of();

        if (!enableFilterCheck.isSelected()) {
            mode = FilterMode.NONE;
        } else {
            mode = ignoreRadio.isSelected() ? FilterMode.EXCLUDE : FilterMode.INCLUDE;
            classes = Arrays.stream(classNamesField.getText().split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        return new ExportDialog.ExportConfig(
                separateNucleiCheck.isSelected(),
                mode,
                classes
        );
    }
}
