package qupath.ext.template.ui;

import annotationexporter.core.FilterMode;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.QuPathGUI;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Utility class to show the export dialog.
 */
public class ExportDialog {
    private static final Logger logger = LoggerFactory.getLogger(ExportDialog.class);

    /**
     * Support record with configurations for the export.
     */
    public record ExportConfig(
            boolean separateNuclei,
            FilterMode filterMode,
            List<String> classNames
    ) {}

    /**
     * Loads the dialog window, shows it, and returns the configuration chosen by the user
     * or empty if the user canceled the operation.
     * @param qupath the {@code QuPathGUI} instance
     * @return the configuration chosen by the user, as an {@code Optional<ExportConfig>}
     */
    public static Optional<ExportConfig> show(@NotNull QuPathGUI qupath) {
        try {
            var url = ExportDialog.class.getResource("export_dialog.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            loader.setClassLoader(ExportDialogController.class.getClassLoader());
            Parent root = loader.load();
            ExportDialogController controller = loader.getController();

            Dialog<ExportConfig> dialog = new Dialog<>();
            dialog.setTitle("Export Annotation Masks");
            dialog.setHeaderText("Configure annotation mask export options");
            dialog.initOwner(qupath.getStage());
            dialog.getDialogPane().setContent(root);

            ButtonType exportBtn = new ButtonType("Export", ButtonType.OK.getButtonData());
            dialog.getDialogPane().getButtonTypes().addAll(exportBtn, ButtonType.CANCEL);

            dialog.setResultConverter(btn ->
                    btn == exportBtn ? controller.getConfig() : null
            );

            return dialog.showAndWait();

        } catch (IOException e) {
            logger.error("Impossibile caricare export_dialog.fxml: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
