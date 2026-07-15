package qupath.ext.template.ui;

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

public class ExportDialog {
    private static final Logger logger = LoggerFactory.getLogger(ExportDialog.class);

    /**
     * Parametri di configurazione raccolti dal dialog.
     */
    public record ExportConfig(
            boolean separateNuclei,
            boolean ignoreClasses,
            List<String> classNames
    ) {}

    /**
     * Carica il dialog da FXML, lo mostra e restituisce la configurazione scelta,
     * o empty() se l'utente ha annullato.
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
            dialog.setHeaderText("Configura l'esportazione delle annotazioni");
            dialog.initOwner(qupath.getStage());
            dialog.getDialogPane().setContent(root);

            ButtonType exportBtn = new ButtonType("Esporta", ButtonType.OK.getButtonData());
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
