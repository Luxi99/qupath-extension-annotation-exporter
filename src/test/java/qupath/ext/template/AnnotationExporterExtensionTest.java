package qupath.ext.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AnnotationExporterExtensionTest {

    private AnnotationExporterExtension SUT;

    @BeforeEach
    public void setup() {
        SUT = new AnnotationExporterExtension();
    }

    @Test
    void testGetNameNotBlank() {
        assertThat(SUT.getName(), not(blankOrNullString()));
    }

    @Test
    void testGetDescriptionNotBlank() {
        assertThat(SUT.getDescription(), not(blankOrNullString()));
    }

    @Test
    void testQupathVersionParse() {
        assertThat(SUT.getQuPathVersion(), is(notNullValue()));
    }
}