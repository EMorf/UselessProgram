import org.junit.Test;
import java.awt.image.BufferedImage;
import static org.junit.Assert.*;

public class ImageProcessorTest {

    @Test
    public void testScaleImage() {
        ImageProcessor processor = new ImageProcessor();
        BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Target is smaller
        BufferedImage scaled = processor.scaleImage(original, 50, 50);
        assertNotNull(scaled);
        assertEquals(50, scaled.getWidth());
        assertEquals(50, scaled.getHeight());

        // Target is larger (should scale up)
        scaled = processor.scaleImage(original, 200, 200);
        assertNotNull(scaled);
        assertEquals(200, scaled.getWidth());
        assertEquals(200, scaled.getHeight());

        // Aspect ratio check: 100x50 original, target 50x50 -> should be 50x25
        original = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        scaled = processor.scaleImage(original, 50, 50);
        assertNotNull(scaled);
        assertEquals(50, scaled.getWidth());
        assertEquals(25, scaled.getHeight());

        // Aspect ratio check: 50x100 original, target 50x50 -> should be 25x50
        original = new BufferedImage(50, 100, BufferedImage.TYPE_INT_RGB);
        scaled = processor.scaleImage(original, 50, 50);
        assertNotNull(scaled);
        assertEquals(25, scaled.getWidth());
        assertEquals(50, scaled.getHeight());
    }

    @Test
    public void testScaleImageNull() {
        ImageProcessor processor = new ImageProcessor();
        assertNull(processor.scaleImage(null, 100, 100));
    }
}
