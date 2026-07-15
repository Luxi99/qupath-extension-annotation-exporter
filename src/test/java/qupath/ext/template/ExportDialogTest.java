package qupath.ext.template;

import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import qupath.ext.template.ui.ExportDialog;
import qupath.ext.template.ui.ExportDialogController;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ExportDialogTest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @Test
    void fxmlLoadsWithoutErrors() throws Exception {
        var url = ExportDialog.class.getResource("export_dialog.fxml");
        assertThat(url, is(notNullValue()));

        FXMLLoader loader = new FXMLLoader(url);
        var root = loader.load();

        assertThat(root, is(notNullValue()));
        assertThat(loader.getController(), is(instanceOf(ExportDialogController.class)));
    }
}
