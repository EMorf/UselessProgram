import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import java.awt.image.BufferedImage;

/**
 * Handles webcam operations using JavaCV.
 */
public class CameraHandler {
    private final OpenCVFrameGrabber camera;
    private final Java2DFrameConverter converter;

    public CameraHandler() {
        camera = new OpenCVFrameGrabber(0);
        converter = new Java2DFrameConverter();
    }

    public void start() throws Exception {
        camera.start();
    }

    public BufferedImage capture() throws Exception {
        Frame frame = camera.grab();
        if (frame != null) {
            return converter.convert(frame);
        }
        return null;
    }

    public void stop() throws Exception {
        camera.stop();
    }
}
