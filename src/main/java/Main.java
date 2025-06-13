import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.imageio.ImageIO;
import org.json.JSONObject;
import org.json.JSONArray;

public class Main {
    public static void main(String[] args) {
        new BackgroundSnap().start();
    }
}

class BackgroundSnap {
    private final Random random = new Random();
    private final int minDelay = 5;
    private final int maxDelay = 10;
    private final int displayTime = 3;
    private BufferedImage dwarfImage;

    public BackgroundSnap() {
        loadDwarfImage();
    }

    private void loadDwarfImage() {
        try {
            InputStream is = getClass().getResourceAsStream("/dwarf.jpg");
            if (is != null) {
                dwarfImage = ImageIO.read(is);
            }
        } catch (Exception e) {
            System.err.println("Failed to load dwarf image: " + e.getMessage());
        }
    }

    public void start() {
        while (true) {
            try {
                int delay = minDelay + random.nextInt(maxDelay - minDelay + 1);
                Thread.sleep(delay * 1000L);
                BufferedImage screenshot = takeScreenshot();
                BufferedImage randomImage = fetchRandomImage();
                showImages(screenshot, randomImage);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private BufferedImage takeScreenshot() throws Exception {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(screenRect);
    }

    private BufferedImage fetchRandomImage() {
        try {
            // First get a random image URL from Picsum
            URL url = new URL("https://picsum.photos/800/600");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            String finalUrl = conn.getURL().toString();
            
            // Now download and create the image
            return ImageIO.read(new URL(finalUrl));
        } catch (Exception e) {
            System.err.println("Failed to fetch random image: " + e.getMessage());
            return dwarfImage; // Return dwarf image as fallback
        }
    }

    private void showImages(BufferedImage img1, BufferedImage img2) {
        JFrame frame = new JFrame("Snap!");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setLayout(new GridLayout(1, 2));
        frame.add(new JLabel(new ImageIcon(img1)));
        frame.add(new JLabel(new ImageIcon(img2 != null ? img2 : dwarfImage)));
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
