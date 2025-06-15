import javax.swing.*;
import java.awt.*;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

/**
 * Main class that serves as the entry point for the application.
 */
public class Main {
    /**
     * The main method that starts the BackgroundSnap application.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        new BackgroundSnap().start();
    }
}

/**
 * A class that periodically takes screenshots and webcam photos,
 * displaying them side by side on the screen.
 */
class BackgroundSnap {
    private final Random random = new Random();
    private final int minDelay = 5;
    private final int maxDelay = 10;
    private final int displayTime = 3;
    private final OpenCVFrameGrabber camera;
    private final Java2DFrameConverter converter;
    private BufferedImage dwarfImage;
    private final Dimension screenSize;
    private final int targetWidth;
    private final int targetHeight;

    /**
     * Constructor initializes the camera, screen dimensions, and loads the fallback dwarf image.
     * Sets up the webcam capture and configures initial display parameters.
     */
    public BackgroundSnap() {
        camera = new OpenCVFrameGrabber(0);
        converter = new Java2DFrameConverter();
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        targetWidth = screenSize.width / 2;  // Half the screen width for each image
        targetHeight = screenSize.height;
        try {
            camera.start();
        } catch (Exception e) {
            System.err.println("Failed to start camera: " + e.getMessage());
        }
        loadDwarfImage();
    }

    /**
     * Loads the dwarf image from resources to be used as a fallback when webcam fails.
     * The image is scaled to fit the target dimensions.
     */
    private void loadDwarfImage() {
        try {
            InputStream is = getClass().getResourceAsStream("/dwarf.jpg");
            if (is != null) {
                dwarfImage = ImageIO.read(is);
                dwarfImage = scaleImage(dwarfImage);
                is.close();
            }
        } catch (Exception e) {
            System.err.println("Failed to load dwarf image: " + e.getMessage());
        }
    }

    /**
     * Scales an image to fit within the target dimensions while maintaining aspect ratio.
     * @param original The original BufferedImage to scale
     * @return A new BufferedImage scaled to fit the target dimensions, or null if input is null
     */
    private BufferedImage scaleImage(BufferedImage original) {
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

    /**
     * Starts the main application loop that periodically captures and displays images.
     * The loop runs indefinitely, waiting for random intervals between captures.
     */
    public void start() {
        while (true) {
            try {
                int delay = minDelay + random.nextInt(maxDelay - minDelay + 1);
                Thread.sleep(delay * 1000L);
                
                // Take and scale screenshot first
                BufferedImage screenshot = takeScreenshot();
                if (screenshot != null) {
                    screenshot = scaleImage(screenshot);
                }
                
                // Take webcam photo last, right before showing images
                BufferedImage photo = takeWebcamPhoto();
                if (photo != null) {
                    photo = scaleImage(photo);
                }
                
                showImages(screenshot, photo);
            } catch (Exception e) {
                System.err.println("Error in main loop: " + e.getMessage());
            }
        }
    }

    /**
     * Takes a screenshot of the entire screen.
     * @return A BufferedImage containing the screenshot
     * @throws Exception if the screenshot capture fails
     */
    private BufferedImage takeScreenshot() throws Exception {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(screenSize);
        return robot.createScreenCapture(screenRect);
    }
    

    /**
     * Captures an image from the webcam. If the webcam capture fails,
     * returns the fallback dwarf image.
     * @return A BufferedImage from the webcam or the dwarf image if capture fails
     */
    private BufferedImage takeWebcamPhoto() {
        try {
            Frame frame = camera.grab();
            if (frame != null) {
                return converter.convert(frame);
            }
        } catch (Exception e) {
            System.err.println("Webcam error: " + e.getMessage());
        }
        return dwarfImage; // Return dwarf image as fallback
    }

    /**
     * Displays two images side by side in a borderless window for a fixed duration.
     * The window is always on top and automatically closes after the display time.
     * @param img1 The first image to display (usually the screenshot)
     * @param img2 The second image to display (usually the webcam photo)
     */
    private void showImages(BufferedImage img1, BufferedImage img2) {
        JFrame frame = new JFrame("Snap!");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setLayout(new GridLayout(1, 2));
        
        if (img1 != null) frame.add(new JLabel(new ImageIcon(img1)));
        if (img2 != null) frame.add(new JLabel(new ImageIcon(img2 != null ? img2 : dwarfImage)));
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Timer(displayTime * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
            }
        }).start();
    }
}
