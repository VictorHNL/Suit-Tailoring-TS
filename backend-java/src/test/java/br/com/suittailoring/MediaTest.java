package br.com.suittailoring;
import br.com.suittailoring.media.MediaController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
class MediaTest {
    @TempDir Path directory;
    @Test void rejectsForgedImage(){
        var controller=new MediaController(directory.toString());
        assertThrows(IllegalArgumentException.class,()->controller.upload(new MockMultipartFile("file","x.jpg","image/jpeg","not an image".getBytes())));
    }
    @Test void storesCanonicalImageWithGeneratedName() throws Exception{
        var buffer=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(600,800,BufferedImage.TYPE_INT_RGB),"png",buffer);
        var controller=new MediaController(directory.toString());
        var output=controller.upload(new MockMultipartFile("file","../../evil.png","image/png",buffer.toByteArray()));
        String url=(String)output.get("url");
        assertTrue(url.matches("/api/media/[a-f0-9-]{36}\\.png"));
        assertEquals(200,controller.get(url.substring("/api/media/".length())).getStatusCode().value());
    }
}
