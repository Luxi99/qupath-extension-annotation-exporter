package qupath.ext.template;

import annotationexporter.core.AnnotationExporter;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.template.ui.ExportDialog;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.projects.Project;
import qupath.lib.projects.ProjectImageEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Command to export annotations. Contains the main logic for the export.
 */
public class ExportCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ExportCommand.class);
    private final QuPathGUI qupath;

    public ExportCommand(@NotNull QuPathGUI qupath) {
        this.qupath = qupath;
    }

    /**
     * Checks if a project is open, shows the export dialog, then runs the export in a background thread.
     */
    @Override
    public void run() {
        Project<?> project = qupath.getProject();
        if (project == null) {
            Dialogs.showErrorMessage(
                    "No project open",
                    "Open a QuPath project before running the export."
            );
            return;
        }

        Optional<ExportDialog.ExportConfig> configOpt = ExportDialog.show(qupath);
        if (configOpt.isEmpty()) return; // utente ha annullato

        var config = configOpt.get();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                runExport(project, config);
                return null;
            }
        };

        qupath.getThreadPoolManager().getSingleThreadExecutor(this).submit(task);    }

    /**
     * Runs the actual export for each image in the project.
     * @param project the project to export annotations from
     * @param config configuration for the export
     */
    private void runExport(@NotNull Project<?> project, @NotNull ExportDialog.ExportConfig config) {
        var entries = project.getImageList();
        int total = entries.size();
        int exported = 0;
        int skipped = 0;

        logger.info("[START] Esportazione su {} immagini", total);

        for (ProjectImageEntry<?> entry : entries) {
            try {
                boolean done = exportEntry(entry, config, project);
                if (done) exported++;
                else skipped++;
            } catch (Exception e) {
                logger.error("Errore nell'immagine {}: {}", entry.getImageName(), e.getMessage(), e);
            }
        }

        int finalExported = exported;
        int finalSkipped = skipped;

        // Torna sul thread JavaFX per mostrare il messaggio finale
        javafx.application.Platform.runLater(() ->
                Dialogs.showInfoNotification(
                        "Export completed",
                        String.format("Exported: %d images\nSkipped (no annotations): %d",
                                finalExported, finalSkipped)
                )
        );

        logger.info("[END] Exported: {}, Skipped: {}", finalExported, finalSkipped);
    }

    /**
     * Exports annotations from a single image.
     * @param entry the image to export annotations from
     * @param config configuration for the export
     * @param project the project to export annotations from
     * @return true if annotations were exported, false otherwise
     * @throws IOException if an error occurs while reading the image data
     */
    private boolean exportEntry(@NotNull ProjectImageEntry<?> entry, @NotNull ExportDialog.ExportConfig config, @NotNull Project<?> project) throws IOException {
        ImageData<?> imageData = entry.readImageData();
        PathObjectHierarchy hierarchy = imageData.getHierarchy();
        hierarchy.resolveHierarchy();

        List<PathObject> annotations = new ArrayList<>(hierarchy.getAnnotationObjects());

        Predicate<PathObject> predicate = AnnotationExporter.getClassnamesPredicate(config.filterMode(), config.classNames());
        List<PathObject> filtered = AnnotationExporter.filter(annotations, predicate);

        if (filtered.isEmpty()) return false;

        int w = imageData.getServer().getWidth();
        int h = imageData.getServer().getHeight();

        AnnotationExporter.LabelResult result = AnnotationExporter.buildLabelMask(filtered, w, h, config.separateNuclei());

        String baseName = entry.getImageName().replaceFirst("\\.[^.]+$", "");
        Path outputDir = Path.of(project.getPath().getParent().toUri()).resolve("exports");
        AnnotationExporter.writeImage(result.image(), "TIFF", outputDir.resolve(baseName + "_mask.tif"));
        AnnotationExporter.writeTable(result.tableRows(), "LabelID\tCentroidX\tCentroidY\tClass",
                outputDir.resolve(baseName + "_table.tsv"));

        return true;
    }
}
