package qupath.ext.template;

import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qupath.ext.template.ui.ExportDialogController;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ExportDialogControllerTest {

    private ExportDialogController controller;

    @BeforeAll
    static void initJavaFx() throws InterruptedException {
        // Inizializza il toolkit JavaFX una sola volta per tutta la classe di test
        CountDownLatch latch = new CountDownLatch(1);
        new JFXPanel(); // side effect: avvia il thread JavaFX
        latch.countDown();
    }

    @BeforeEach
    void loadFxml() throws Exception {
        var url = ExportDialogController.class.getResource("export_dialog.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        controller = loader.getController();
    }

    @Test
    void defaultHasSeparateNucleiTrueNoFilter() {
        var config = controller.getConfig();

        assertThat(config.separateNuclei(), is(true));
        assertThat(config.classNames(), is(empty()));
    }

    @Test
    void filterDisabledIgnoresClassNamesFieldContent() throws Exception {
        setTextField("classNamesField", "fondo, artefatto");
        // enableFilterCheck resta false di default

        var config = controller.getConfig();

        assertThat(config.classNames(), is(empty()));
    }

    @Test
    void filterEnabledParsesClassNamesCorrectly() throws Exception {
        setCheckBox("enableFilterCheck", true);
        setTextField("classNamesField", "Fondo, Artefatto , nucleo");

        var config = controller.getConfig();

        assertThat(config.classNames(), containsInAnyOrder("fondo", "artefatto", "nucleo"));
    }

    @Test
    void filterEnabledEmptyFieldProducesEmptyList() throws Exception {
        setCheckBox("enableFilterCheck", true);
        setTextField("classNamesField", "");

        var config = controller.getConfig();

        assertThat(config.classNames(), is(empty()));
    }

    @Test
    void ignoreRadioSelectedByDefaultSetsIgnoreClassesTrue() {
        var config = controller.getConfig();

        assertThat(config.ignoreClasses(), is(true));
    }

    // --- Helpers per accedere ai campi privati @FXML tramite reflection ---

    private void setTextField(String fieldName, String value) throws Exception {
        var field = ExportDialogController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        javafx.scene.control.TextField tf = (javafx.scene.control.TextField) field.get(controller);
        tf.setText(value);
    }

    private void setCheckBox(String fieldName, boolean value) throws Exception {
        var field = ExportDialogController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        javafx.scene.control.CheckBox cb = (javafx.scene.control.CheckBox) field.get(controller);
        cb.setSelected(value);
    }
}
