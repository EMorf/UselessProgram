import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Utility class for image processing operations.
 */
public class ImageProcessor {

    /**
     * Scales an image to fit within the target dimensions while maintaining aspect ratio.
     * @param original The original BufferedImage to scale
     * @param targetWidth The target width
     * @param targetHeight The target height
     * @return A new BufferedImage scaled to fit the target dimensions, or null if input is null
     */
    public BufferedImage scaleImage(BufferedImage original, int targetWidth, int targetHeight) {
        if (original == null) return null;

        double scaleFactor = Math.min(
            (double) targetWidth / original.getWidth(),
            (double) targetHeight / original.getHeight()
        );

        int newWidth = (int) (original.getWidth() * scaleFactor);
        int newHeight = (int) (original.getHeight() * scaleFactor);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }
}
