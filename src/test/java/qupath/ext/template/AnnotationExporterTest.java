package qupath.ext.template;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.PathObjects;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.regions.ImagePlane;
import qupath.lib.roi.ROIs;
import qupath.lib.roi.RoiTools;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnnotationExporterTest {
    private static final int W = 100;
    private static final int H = 100;

    /**
     * Crea annotazione con etichetta e forma, ma senza figli
     * */
    static PathObject createAnnotation(String className, Shape shape) {
        return createAnnotationWithChildren(className, shape, List.of());
    }

    /**
     * Crea annotazione con etichetta, forma e figli
     * */
    static PathObject createAnnotationWithChildren(String className, Shape shape, List<PathObject> children) {
        var pathClass = PathClass.fromString(className);
        var roi = RoiTools.getShapeROI(shape, ImagePlane.getDefaultPlane(), 1);
        var annotation = PathObjects.createAnnotationObject(roi);

        annotation.setPathClass(pathClass);
        annotation.addChildObjects(children);

        return annotation;
    }

    /**
     * Conta quanti pixel con un determinato valore siano presenti nell'immagine
     * */
    static int countPixelsWithValue(BufferedImage img, int value) {
        var raster = img.getRaster();
        int count = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (raster.getSample(x, y, 0) == value) count++;
            }
        }

        return count;
    }


    /**
     * Data una lista di annotazioni, verifica che il metodo di sorting le posizioni
     * in fondo a tale lista
     * */
    @Test
    @DisplayName("I nuclei devono essere messi in fondo alla lista")
    void testNucleiInFondo() {
        Shape shape = new Rectangle(0, 0, 10, 10);
        var annotations = List.of(
            createAnnotation("red blood cell", shape),
                createAnnotation("nucleo", shape),
                createAnnotation("necrosis", shape),
                createAnnotation("positive", shape),
                createAnnotation("negative", shape),
                createAnnotation("suamous epithelial cells", shape),
                createAnnotation("echinocytes", shape),
                createAnnotation("nucleo", shape),
                createAnnotation("lymphocyte", shape),
                createAnnotation("neutrofilo", shape),
                createAnnotation("others", shape)
                );

        var sorted = AnnotationExporter.sortAnnotations(annotations);
        assertEquals("nucleo", sorted.getLast().getPathClass().getName());
        assertEquals("nucleo", sorted.get(sorted.size()-2).getPathClass().getName());
        sorted.subList(0,9).forEach(e -> assertNotEquals("nucleo", e.getPathClass().getName()));
    }

    /**
     * Se in lista non sono presenti nuclei, l'ordine della stessa non cambia se passata
     * al metodo di ordinamento
     * */
    @Test
    @DisplayName("Lista senza nuclei non deve cambiare ordine relativo")
    void testOrdineSenzaNuclei() {
        var shape = new Rectangle(0, 0, 10, 10);
        var sorted = AnnotationExporter.sortAnnotations(List.of(
                createAnnotation("red blood cell", shape),
                createAnnotation("lymphocyte", shape),
                createAnnotation("positive", shape)
                )
        );

        assertEquals(List.of("red blood cell", "lymphocyte", "positive"),
                sorted.stream()
                        .map(e -> e.getPathClass() != null ? e.getPathClass().getName() : "Unclassified")
                        .collect(Collectors.toList()));
    }

    /**
     * Verifica che una lista vuota passata al metodo di ordinamento restituisca sempre una lista vuota
     * */
    @Test
    @DisplayName("Lista vuota deve restituire lista vuota")
    void testOrdinamentoListaVuota() {
        assertTrue(AnnotationExporter.sortAnnotations(List.of()).isEmpty());
    }

    /**
     * Verifica che una lista di soli nuclei non cambi a seguito di un ordinamento
     * */
    @Test
    @DisplayName("Lista con soli nuclei deve rimanere invariata")
    void testOrdinamentoSoloNuclei() {
        var shape = new Rectangle(0, 0, 10, 10);
        var sorted = AnnotationExporter.sortAnnotations(List.of(
                createAnnotation("nucleo", shape),
                createAnnotation("nucleo", shape)
                )
        );

        assertEquals(2, sorted.size());
        sorted.forEach(e -> assertEquals("nucleo", e.getPathClass().getName()));
    }


    /**
     * Verifica che sottraendo l'area dei figli ad una forma che non ha figli,
     * la sua area rimanga invariata
     * */
    @Test
    @DisplayName("Senza figli l'area deve essere uguale alla forma originale")
    void testSottrazioneSenzaFigli() {
        var parent = new Rectangle(10, 10, 40, 40);
        var area = AnnotationExporter.subtractChildren(parent, List.of());
        var differenza = new Area(area);
        differenza.subtract(new Area(parent));
        assertTrue(differenza.isEmpty());
    }

    /**
     * Verifica che l'area di un'annotazione figlia venga effettivamente sottratta da quella
     * del padre contando i pixel corrispondenti all'area sottratta.
     */
    @Test
    @DisplayName("Il figlio deve essere sottratto dall'area padre")
    void testSottrazioneFiglio() {
        var parent = new Rectangle(10, 10, 40, 40);
        var child  = new Rectangle(20, 20, 10, 10);
        var area = AnnotationExporter.subtractChildren(parent, List.of(child));

        var img = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_BINARY);
        var g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fill(area);
        g.dispose();

        // L'immagine creata ha pixel di 1 bit e si sta considerando solo 1 canale
        int pixelBianchi = countPixelsWithValue(img, 1);

        assertEquals(1500, pixelBianchi);
    }


    /**
     * Verifica che l'area del figlio sottratta a quella del padre crei un "buco" nel padre.
     * Controlla verificando che il valore del pixel corrispondente al centroide del figlio nell'immagine
     * formata con l'area risultante sia 0 (sfondo nero).
     */
    @Test
    @DisplayName("Il figlio deve creare un buco nell'area padre")
    void testBucoNelCitoplasma() {
        var parent = new Rectangle(10, 10, 40, 40);
        var child  = new Rectangle(20, 20, 10, 10);
        var area = AnnotationExporter.subtractChildren(parent, List.of(child));

        var img = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_BINARY);
        var g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fill(area);
        g.dispose();

        var raster = img.getRaster();
        assertEquals(0, raster.getSample(25, 25, 0), "Il centro del nucleo deve essere un buco");
        assertTrue(raster.getSample(12, 12, 0) > 0, "Un punto nel citoplasma deve essere bianco");
    }

    /**
     * Verifica che il metodo paintLabel scriva correttamente l'etichetta passata come parametro nell'immagine finale
     */
    @Test
    @DisplayName("paintLabel deve scrivere il label corretto nei pixel")
    void testPaintLabel() {
        var img = new BufferedImage(W, H, BufferedImage.TYPE_USHORT_GRAY);
        AnnotationExporter.paintLabel(img.getRaster(), new Area(new Rectangle(10, 10, 20, 20)), 42, W, H);
        assertEquals(42, img.getRaster().getSample(20, 20, 0));
    }

    /**
     * Verifica che i pixel al di fuori dell'area disegnata rimangano a 0
     */
    @Test
    @DisplayName("I pixel fuori dall'area devono rimanere a 0")
    void testPixelEsterniRimangono0() {
        var img = new BufferedImage(W, H, BufferedImage.TYPE_USHORT_GRAY);
        AnnotationExporter.paintLabel(img.getRaster(), new Area(new Rectangle(10, 10, 20, 20)), 1, W, H);
        assertEquals(0, img.getRaster().getSample(0, 0, 0));
        assertEquals(0, img.getRaster().getSample(99, 99, 0));
    }


    /**
     * Verifica che due etichette diverse non interferiscano tra loro nell'immagine finale
     */
    @Test
    @DisplayName("Due label diversi non devono sovrascriversi")
    void testDueLabel() {
        var img = new BufferedImage(W, H, BufferedImage.TYPE_USHORT_GRAY);
        var raster = img.getRaster();
        AnnotationExporter.paintLabel(raster, new Area(new Rectangle(5,  5,  20, 20)), 1, W, H);
        AnnotationExporter.paintLabel(raster, new Area(new Rectangle(60, 60, 20, 20)), 2, W, H);
        assertEquals(1, raster.getSample(15, 15, 0));
        assertEquals(2, raster.getSample(70, 70, 0));
        assertEquals(0, raster.getSample(40, 40, 0));
    }


    /**
     * Verifica che il metodo createTableRecord restituisca la corretta stringa formattata in TSV,
     * data un'annotazione
     */
    @Test
    @DisplayName("createTableRecord deve restituire una stringa TSV corretta")
    void testCreateTableRecord() {
        var ann = createAnnotation("red blood cell", new Rectangle(10, 10, 20, 20));
        var parts = AnnotationExporter.createTableRecord(ann, 3).split("\t");
        assertEquals(4, parts.length);
        assertEquals("3", parts[0]);
        assertEquals("20.0", parts[1]);
        assertEquals("20.0", parts[2]);
        assertEquals("red blood cell", parts[3]);
    }

    /**
     * Verifica che se manca l'etichetta ad un'annotazione, createTableRecord usa Unclassified come etichetta
     */
    @Test
    @DisplayName("createTableRecord con classe null deve usare 'Unclassified'")
    void testCreateTableRecordSenzaClasse() {
        var roi = ROIs.createRectangleROI(0, 0, 10, 10);

        var ann = PathObjects.createAnnotationObject(roi);

        assertTrue(AnnotationExporter.createTableRecord(ann, 1).endsWith("Unclassified"));
    }


    /**
     * Verifica che il metodo buildMaskAndTable crei un'immagine con label incrementali che partano da 1 per la
     * prima annotazione
     */
    @Test
    @DisplayName("buildMaskAndTable deve assegnare label progressivi da 1")
    void testLabelProgressivi() {
        var annotations = List.of(
                createAnnotation("red blood cell",       new Rectangle(5,  5,  20, 20)),
                createAnnotation("lymphocyte", new Rectangle(40, 40, 20, 20))
                );

        Object o = AnnotationExporter.buildMaskAndTable(annotations, W, H, false).get("image");
        BufferedImage img = (BufferedImage) o;
        Raster raster = img.getRaster();
        assertEquals(1, raster.getSample(15, 15, 0));
        assertEquals(2, raster.getSample(50, 50, 0));
        assertEquals(0, raster.getSample(0,  0,  0));
    }

    @Disabled
    @Test
    @DisplayName("buildMaskAndTable deve mettere i nuclei in fondo")
    void testNucleiInFondoNellaMaschera() {
        List<PathObject> annotations = List.of(
                        createAnnotation("nucleo", new Rectangle(5,  5,  20, 20)),
                        createAnnotation("red blood cell",    new Rectangle(40, 40, 20, 20))
        );
        var result = AnnotationExporter.buildMaskAndTable(annotations, W, H, false);

        List<String> table = (List<String>) result.get("table");;

        assertTrue(table.getFirst().contains("red blood cell"));
        assertTrue(table.getLast().contains("nucleo"));
    }

    /**
     * Verifica che da una lista vuota di annotazioni buildMaskAndTable crei una maschera con pixel tutti a 0
     */
    @Test
    @DisplayName("buildMaskAndTable con lista vuota deve restituire maschera tutta a 0")
    void testMascheraVuota() {
        var result = AnnotationExporter.buildMaskAndTable(List.of(), W, H, false);

        BufferedImage image = (BufferedImage) result.get("image");
        List<String> table = (List<String>) result.get("table");;

        assertEquals(W * H, countPixelsWithValue(image, 0));
        assertTrue(table.isEmpty());
    }

    /**
     * Verifica che nell'immagine la label del nucleo sia diversa da quella del genitore
     */
    @Test
    @DisplayName("buildMaskAndTable con nucleo dentro citoplasma deve creare il buco")
    void testBucoIntegrazione() {
        var nucleus = createAnnotation("nucleo", new Rectangle(25, 25, 10, 10));
        var cytoplasmAnn = createAnnotationWithChildren(
                "positive",
                new Rectangle(10, 10, 50, 50),
                List.of(nucleus)
        );
        var annotations = List.of(cytoplasmAnn, nucleus);

        var result = AnnotationExporter.buildMaskAndTable(annotations, W, H, true);
        var image = (BufferedImage) result.get("image");
        var raster = image.getRaster();
        assertNotEquals(1, raster.getSample(30, 30, 0),
                "Il centro del nucleo non deve avere il label del citoplasma");
    }

    /**
     * Verifica che lo script lanciato con parametro separateNuclei = false, restituisca una label image
     * con i soli citoplasmi delle cellule annotate, scartando le annotazioni di tipo nucleo.
     */
    @Test
    @DisplayName("Con separateNuclei=false i nuclei non devono apparire nella tabella")
    void testSeparateNucleiFalse() {
        var nucleus = createAnnotation("nucleo", new Rectangle(25, 25, 10, 10));
        var cyto = createAnnotationWithChildren(
                "cellula cancerosa",
                new Rectangle(10, 10, 50, 50),
                List.of(nucleus)
        );
        var annotations = List.of(cyto, nucleus);


        var result = AnnotationExporter.buildMaskAndTable(annotations, W, H, false);
        var table = (List<String>) result.get("table");
        var image = (BufferedImage) result.get("image");

        // La tabella deve avere solo 1 riga — il citoplasma, non il nucleo
        assertEquals(1, table.size(), "Con separateNuclei=false i nuclei non devono apparire nella tabella");
        // Il centro del nucleo deve avere il label del citoplasma (nessun buco)
        assertEquals(1, image.getRaster().getSample(30, 30, 0), "Con separateNuclei=false l'area del nucleo deve far parte del citoplasma");
    }

    /**
     * Verifica che lo script lanciato con parametro separateNuclei = true, restituisca una label image
     * con anche i nuclei annotati sovra impressi alle cellule genitrici
     */
    @Test
    @DisplayName("Con separateNuclei=true i nuclei devono apparire nella tabella")
    void testSeparateNucleiTrueMantieneNuclei() {
        var nucleus = createAnnotation("nucleo", new Rectangle(25, 25, 10, 10));
        var cyto = createAnnotationWithChildren(
                "cellula cancerosa",
                new Rectangle(10, 10, 50, 50),
                List.of(nucleus)
        );
        var annotations = List.of(cyto, nucleus);

        var result = AnnotationExporter.buildMaskAndTable(annotations, W, H, true);
        var table = (List<String>) result.get("table");
        var image = (BufferedImage) result.get("image");
        // La tabella deve avere 2 righe — citoplasma e nucleo
        assertEquals(2, table.size(),
                "Con separateNuclei=true i nuclei devono apparire nella tabella");

        // Il centro del nucleo non deve avere il label del citoplasma (buco presente)
        assertNotEquals(1, image.getRaster().getSample(30, 30, 0),
                "Con separateNuclei=true ci deve essere il buco del nucleo");
    }

    /**
     * Verifica che il metodo getImageName restituisca il nome dell'immagine senza
     * l'estensione.
     */
    /*
    @Test
    void testGetImageNameNoImage() {
        assertEquals(AnnotationExporter.getImageName(), "Unnamed");
    }

    @Test
    void testIgnoreClass() {
        Shape shape = new Rectangle()

        var a1 = createAnnotation("red blood cell", shape);
        var a2 = createAnnotation("nucleo", shape);
        var a3 = createAnnotation("necrosis", shape);
        var a4 = createAnnotation("positive", shape);
        var annotations = List.of(a1, a2, a3, a4);

        var classNames = List.of("red blood cell", "necrosis");

        AnnotationExporter.ignoreClass(annotations, classNames);

        assertTrue(annotations.size() == 2);
        assertTrue(annotations.contains(a2));
        assertTrue(annotations.contains(a4));
    }

    @Test
    void testIgnoreClassNotPresent() {
        Shape shape = new Rectangle();

        var a1 = createAnnotation("red blood cell", shape);
        var a2 = createAnnotation("nucleo", shape);
        var a3 = createAnnotation("necrosis", shape);
        var a4 = createAnnotation("positive", shape);
        var annotations = List.of(a1, a2, a3, a4);

        var classNames = List.of("lymphocyte", "negative");

        AnnotationExporter.ignoreClass(annotations, classNames);

        assertTrue(annotations.size() == 4);
        assertTrue(annotations.contains(a1));
        assertTrue(annotations.contains(a2));
        assertTrue(annotations.contains(a3));
        assertTrue(annotations.contains(a4));
    }

    @Test
    void testIncludeClass() {
        Shape shape = new Rectangle()

        var a1 = createAnnotation("red blood cell", shape);
        var a2 = createAnnotation("nucleo", shape);
        var a3 = createAnnotation("necrosis", shape);
        var a4 = createAnnotation("positive", shape);
        var annotations = List.of(a1, a2, a3, a4);

        var classNames = List.of("red blood cell", "necrosis");

        AnnotationExporter.includeClass(annotations, classNames);

        assertTrue(annotations.size() == 2);
        assertTrue(annotations.contains(a1));
        assertTrue(annotations.contains(a3));
    }

    @Test
    void testIncludeClassNoElements() {
        Shape shape = new Rectangle()

        var a1 = createAnnotation("red blood cell", shape);
        var a2 = createAnnotation("nucleo", shape);
        var a3 = createAnnotation("necrosis", shape);
        var a4 = createAnnotation("positive", shape);
        var annotations = List.of(a1, a2, a3, a4)

        var classNames = List.of();

        AnnotationExporter.includeClass(annotations, classNames)

        assertTrue(annotations.isEmpty());
    }
    */
}
