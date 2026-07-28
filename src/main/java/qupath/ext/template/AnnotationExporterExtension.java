package qupath.ext.template;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.prefs.PathPrefs;

import java.util.ResourceBundle;


/**
 * Main class for the extension. This class is instantiated by QuPath when the extension is loaded.
 */
public class AnnotationExporterExtension implements QuPathExtension {
	// TODO: add and modify strings to this resource bundle as needed
	/**
	 * A resource bundle containing all the text used by the extension. This may be useful for translation to other languages.
	 * Note that this is optional and you can define the text within the code and FXML files that you use.
	 */
	private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.template.ui.strings");
	private static final Logger logger = LoggerFactory.getLogger(AnnotationExporterExtension.class);

	/**
	 * Display name for the extension
	 */
	private static final String EXTENSION_NAME = resources.getString("name");

	/**
	 * Short description, used under 'Extensions > Installed extensions'
	 */
	private static final String EXTENSION_DESCRIPTION = resources.getString("description");

	/**
	 * QuPath version that the extension is designed to work with.
	 * This allows QuPath to inform the user if it seems incompatible.
	 */
	private static final Version EXTENSION_QUPATH_VERSION = Version.parse("v0.7.0");

	/**
	 * Flag whether the extension is already installed
	 */
	private boolean isInstalled = false;

	/**
	 * Create a stage for the extension to display
	 */
	private Stage stage;

	/**
	 * Install the extension. This is called by QuPath when the extension is loaded.
	 * @param qupath the {@code QuPathGUI} instance
	 */
	@Override
	public void installExtension(@NotNull QuPathGUI qupath) {
		if (isInstalled) {
			logger.debug("{} is already installed", getName());
			return;
		}

		isInstalled = true;
		var action = new Action("Export Annotation Masks...", e -> new ExportCommand(qupath).run());
		var menuItem = ActionUtils.createMenuItem(action);
		var menu = qupath.getMenu("Extensions>Annotation Exporter", true);
		menu.getItems().add(menuItem);
	}

	/**
	 * Get the name of the extension.
	 * @return the name of the extension
	 */
	@Override
	public String getName() {
		return EXTENSION_NAME;
	}

	/**
	 * Get the description of the extension.
	 * @return the description of the extension
	 */
	@Override
	public String getDescription() {
		return EXTENSION_DESCRIPTION;
	}

	/**
	 * Get the version of QuPath that the extension is designed to work with.
	 * @return the version of QuPath that the extension is designed to work with
	 */
	@Override
	public Version getQuPathVersion() {
		return EXTENSION_QUPATH_VERSION;
	}
}
