import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * Main class that serves as the entry point for the application.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * The main method that starts the BackgroundSnap application.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        setupLogging();
        logger.info("Starting application...");
        try {
            new BackgroundSnap().start();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Fatal error in application loop", t);
            // Try to show error dialog safely
            try {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, "Fatal error: " + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                );
            } catch (Throwable dialogError) {
                logger.log(Level.SEVERE, "Failed to show error dialog", dialogError);
            }
        }
    }

    private static void setupLogging() {
        try {
            // Log to a file
            FileHandler fileHandler = new FileHandler("useless_program.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            Logger rootLogger = Logger.getLogger("");
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.INFO);

            // Redirect stderr to a file to capture native crashes or uncaught exceptions printed to stderr
            System.setErr(new PrintStream(new FileOutputStream("useless_program_err.log", true)));
        } catch (IOException e) {
            e.printStackTrace(); // Can't do much else if logging fails
        }
    }
}

/**
 * A class that periodically takes screenshots and webcam photos,
 * displaying them side by side on the screen.
 */
class BackgroundSnap {
    private static final Logger logger = Logger.getLogger(BackgroundSnap.class.getName());
    private final SecureRandom random = new SecureRandom();
    private final int minDelay = 5;
    private final int maxDelay = 10;
    private final int displayTime = 3;
    private OpenCVFrameGrabber camera;
    private final Java2DFrameConverter converter;
    private BufferedImage dwarfImage;
    private Dimension screenSize;
    private int targetWidth;
    private int targetHeight;
    private volatile Robot robot;
    private Rectangle screenRect;

    /**
     * Constructor initializes the camera, screen dimensions, and loads the fallback dwarf image.
     * Sets up the webcam capture and configures initial display parameters.
     */
    public BackgroundSnap() {
        converter = new Java2DFrameConverter();

        try {
            if (!GraphicsEnvironment.isHeadless()) {
                 screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            } else {
                 screenSize = new Dimension(1920, 1080); // Fallback for headless environments (like CI)
                 logger.warning("Headless environment detected, using default screen size.");
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Failed to get screen size", t);
            screenSize = new Dimension(800, 600);
        }

        screenRect = new Rectangle(screenSize);
        targetWidth = screenSize.width / 2;
        targetHeight = screenSize.height;

        try {
            camera = new OpenCVFrameGrabber(0);
            camera.start();
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Failed to start camera: " + e.getMessage(), e);
            camera = null;
        }

        try {
            robot = new Robot();
        } catch (AWTException e) {
            logger.log(Level.WARNING, "Failed to initialize Robot: " + e.getMessage(), e);
        } catch (Throwable t) {
             logger.log(Level.SEVERE, "Unexpected error initializing Robot", t);
        }

        loadDwarfImage();
    }

    /**
     * Loads the dwarf image from resources to be used as a fallback when webcam fails.
     * The image is scaled to fit the target dimensions.
     */
    private void loadDwarfImage() {
        try (InputStream is = getClass().getResourceAsStream("/dwarf.jpg")) {
            if (is != null) {
                dwarfImage = ImageIO.read(is);
                dwarfImage = scaleImage(dwarfImage);
            } else {
                logger.warning("Dwarf image resource not found.");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load dwarf image: " + e.getMessage(), e);
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
        logger.info("Entering main loop...");
        while (true) {
            try {
                int delay = minDelay + random.nextInt(maxDelay - minDelay + 1);
                logger.info("Waiting for " + delay + " seconds.");
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
                
                final BufferedImage finalScreenshot = screenshot;
                final BufferedImage finalPhoto = photo;

                SwingUtilities.invokeLater(() -> showImages(finalScreenshot, finalPhoto));

            } catch (InterruptedException ie) {
                 logger.info("Main loop interrupted. Exiting.");
                 Thread.currentThread().interrupt();
                 break;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Error in main loop: " + e.getMessage(), e);
                // Optional: sleep a bit to avoid tight loop on persistent error
                try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
            }
        }
    }

    /**
     * Takes a screenshot of the entire screen.
     * @return A BufferedImage containing the screenshot
     * @throws Exception if the screenshot capture fails
     */
    private BufferedImage takeScreenshot() {
        try {
            if (robot == null) {
                robot = new Robot();
            }
            return robot.createScreenCapture(screenRect);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Failed to take screenshot: " + t.getMessage(), t);
            return null;
        }
    }
    

    /**
     * Captures an image from the webcam. If the webcam capture fails,
     * returns the fallback dwarf image.
     * @return A BufferedImage from the webcam or the dwarf image if capture fails
     */
    private BufferedImage takeWebcamPhoto() {
        if (camera == null) return dwarfImage;

        try {
            Frame frame = camera.grab();
            if (frame != null) {
                return converter.convert(frame);
            }
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Webcam error: " + e.getMessage(), e);
            // Try to restart camera on error? Maybe later.
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
        try {
            JFrame frame = new JFrame("Snap!");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setAlwaysOnTop(true);
            frame.setUndecorated(true);
            frame.setLayout(new GridLayout(1, 2));

            if (img1 != null) frame.add(new JLabel(new ImageIcon(img1)));
            if (img2 != null) frame.add(new JLabel(new ImageIcon(img2)));

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Timer(displayTime * 1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            }).start();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error displaying images: " + t.getMessage(), t);
        }
    }
}
