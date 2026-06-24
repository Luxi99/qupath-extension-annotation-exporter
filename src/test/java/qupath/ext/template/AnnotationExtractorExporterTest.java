package qupath.ext.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnnotationExtractorExporterTest {

    private AnnotationExporterExtension SUT;

    @BeforeEach
    public void setup() {
        SUT = new AnnotationExporterExtension();
    }

    @Test
    void testGetName() {
        assertEquals("Manual Annotation Exporter", SUT.getName());
    }
}