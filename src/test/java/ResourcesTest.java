import org.junit.Test;
import java.io.InputStream;
import static org.junit.Assert.*;

public class ResourcesTest {

    @Test
    public void testSoundFileExists() {
        InputStream is = getClass().getResourceAsStream("/sounds/wolf-howl.mp3");
        assertNotNull("Wolf howl sound file should exist", is);
        try {
            is.close();
        } catch (Exception e) {}
    }

    @Test
    public void testDwarfImageExists() {
        InputStream is = getClass().getResourceAsStream("/dwarf.jpg");
        assertNotNull("Dwarf fallback image should exist", is);
        try {
             is.close();
        } catch (Exception e) {}
    }
}
