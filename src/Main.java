/*
 * Χρησιμοποιείται πλέον το sarxos/webcam-capture για τη λήψη φωτογραφίας από την κάμερα.
 * Κατέβασε το jar από https://github.com/sarxos/webcam-capture/releases ή μέσω Maven Central.
 * Τοποθέτησέ το στον φάκελο libs/ του project σου.
 *
 * Ενδεικτική εντολή compile:
 * javac -cp "libs/*" src/Main.java
 *
 * Ενδεικτική εντολή run:
 * java -cp "libs/*:src" Main
 *
 * Για logging, κατέβασε και τα logback-classic-1.2.13.jar και logback-core-1.2.13.jar στο libs/.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) {
        new BackgroundSnap().start();
    }
}

class BackgroundSnap {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundSnap.class);
    private final Random random = new Random();
    private final int minDelay = 10; // seconds
    private final int maxDelay = 30; // seconds
    private final int displayTime = 3; // seconds

    public void start() {
        while (true) {
            try {
                int delay = minDelay + random.nextInt(maxDelay - minDelay + 1);
                logger.info("Sleeping for {} seconds before next capture", delay);
                Thread.sleep(delay * 1000L);
                BufferedImage screenshot = takeScreenshot();
                BufferedImage webcam = takeWebcamPhoto();
                showImages(screenshot, webcam);
            } catch (Exception e) {
                logger.error("Exception in main loop", e);
            }
        }
    }

    private BufferedImage takeScreenshot() throws Exception {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(screenRect);
    }

    private BufferedImage takeWebcamPhoto() {
        try {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                logger.error("Webcam not found!");
                return null;
            }
            if (!webcam.isOpen()) {
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.open();
            }
            BufferedImage image = webcam.getImage();
            webcam.close();
            if (image == null) {
                logger.error("No frame captured from webcam!");
                return null;
            }
            logger.info("Webcam image captured successfully");
            return image;
        } catch (Exception e) {
            logger.error("Exception while capturing webcam image", e);
            return null;
        }
    }

    private void showImages(BufferedImage img1, BufferedImage img2) {
        JFrame frame = new JFrame("Snap!");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setLayout(new GridLayout(1, 2));
        frame.add(new JLabel(new ImageIcon(img1)));
        if (img2 != null) {
            frame.add(new JLabel(new ImageIcon(img2)));
        } else {
            frame.add(new JLabel("No webcam image"));
        }
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
