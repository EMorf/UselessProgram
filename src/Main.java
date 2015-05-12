// Για να λειτουργήσει η κάμερα, κατέβασε το OpenCV jar από:
// https://opencv.org/releases/
// και τοποθέτησέ το στον φάκελο libs/ του project σου.
// Πρόσθεσε το jar στο classpath κατά το compile/run.
//
// Ενδεικτική εντολή compile:
// javac -cp .:libs/opencv-xxx.jar src/Main.java
//
// Ενδεικτική εντολή run:
// java -cp .:libs/opencv-xxx.jar src.Main

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

public class Main {
    public static void main(String[] args) {
        new BackgroundSnap().start();
    }
}

class BackgroundSnap {
    private final Random random = new Random();
    private final int minDelay = 10; // seconds
    private final int maxDelay = 30; // seconds
    private final int displayTime = 3; // seconds

    public void start() {
        while (true) {
            try {
                int delay = minDelay + random.nextInt(maxDelay - minDelay + 1);
                Thread.sleep(delay * 1000L);
                BufferedImage screenshot = takeScreenshot();
                BufferedImage webcam = takeWebcamPhoto();
                showImages(screenshot, webcam);
            } catch (Exception e) {
                e.printStackTrace();
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
            try {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            } catch (UnsatisfiedLinkError e) {
                System.err.println("Failed to load OpenCV native library. Please ensure it is correctly set up.");
                return null;
            }
            VideoCapture camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                System.err.println("Webcam not found!");
                return null;
            }
            Mat frame = new Mat();
            // Δώσε λίγο χρόνο στην κάμερα να "ξυπνήσει"
            Thread.sleep(500);
            camera.read(frame);
            camera.release();
            if (frame.empty()) {
                System.err.println("No frame captured from webcam!");
                return null;
            }
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, mob);
            byte[] byteArray = mob.toArray();
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            e.printStackTrace();
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
            frame.add(new JLabel(new JLabel("No webcam image")));
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
