package qupath.ext.template.ui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @FXML
    private void initialize() {
        // Abilita/disabilita i controlli di filtro in base alla checkbox
        enableFilterCheck.selectedProperty().addListener((obs, old, enabled) -> {
            ignoreRadio.setDisable(!enabled);
            includeRadio.setDisable(!enabled);
            classNamesLabel.setDisable(!enabled);
            classNamesField.setDisable(!enabled);
        });
    }

    /**
     * Costruisce la configurazione a partire dallo stato corrente dei controlli.
     */
    public ExportDialog.ExportConfig getConfig() {
        List<String> classes = List.of();

        if (enableFilterCheck.isSelected() && !classNamesField.getText().isBlank()) {
            classes = Arrays.stream(classNamesField.getText().split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        return new ExportDialog.ExportConfig(
                separateNucleiCheck.isSelected(),
                ignoreRadio.isSelected(),
                classes
        );
    }
}
