import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.BufferedInputStream;
import javax.imageio.ImageIO;
import javazoom.jl.player.Player;
import java.util.Random;

/**
 * A class that periodically takes screenshots and webcam photos,
 * displaying them side by side on the screen.
 */
public class BackgroundSnap {
    private final Random random = new Random();
    private final int minDelay = 5;
    private final int maxDelay = 10;
    private final int displayTime = 3;
    private final CameraHandler cameraHandler;
    private final ImageProcessor imageProcessor;
    private BufferedImage dwarfImage;
    private final Dimension screenSize;
    private final int targetWidth;
    private final int targetHeight;

    /**
     * Constructor initializes the camera, screen dimensions, and loads the fallback dwarf image.
     * Sets up the webcam capture and configures initial display parameters.
     */
    public BackgroundSnap() {
        cameraHandler = new CameraHandler();
        imageProcessor = new ImageProcessor();
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        targetWidth = screenSize.width / 2;  // Half the screen width for each image
        targetHeight = screenSize.height;

        try {
            cameraHandler.start();
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
                dwarfImage = imageProcessor.scaleImage(dwarfImage, targetWidth, targetHeight);
                is.close();
            }
        } catch (Exception e) {
            System.err.println("Failed to load dwarf image: " + e.getMessage());
        }
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
                    screenshot = imageProcessor.scaleImage(screenshot, targetWidth, targetHeight);
                }

                // Take webcam photo last, right before showing images
                BufferedImage photo = takeWebcamPhoto();
                if (photo != null) {
                    photo = imageProcessor.scaleImage(photo, targetWidth, targetHeight);
                }

                playSound();
                showImages(screenshot, photo);
            } catch (Exception e) {
                System.err.println("Error in main loop: " + e.getMessage());
            }
        }
    }

    /**
     * Plays the wolf howling sound effect.
     */
    private void playSound() {
        new Thread(() -> {
            try {
                InputStream is = getClass().getResourceAsStream("/sounds/wolf-howl.mp3");
                if (is != null) {
                    BufferedInputStream bis = new BufferedInputStream(is);
                    Player player = new Player(bis);
                    player.play();
                } else {
                    System.err.println("Sound file not found!");
                }
            } catch (Exception e) {
                System.err.println("Error playing sound: " + e.getMessage());
            }
        }).start();
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
            BufferedImage image = cameraHandler.capture();
            if (image != null) {
                return image;
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
        SwingUtilities.invokeLater(() -> {
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
        });
    }
}
