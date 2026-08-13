package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.exceptions.InvalidFileTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path directory;
    private final FileStorageService storage = new FileStorageService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storage, "uploadDirectory", directory.toString());
        ReflectionTestUtils.setField(storage, "publicBaseUrl", "https://api.example.test/");
    }

    @Test
    void validImageIsDecodedAndStoredUnderServerControlledName() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        UUID userId = UUID.randomUUID();

        String url = storage.uploadFile(new MockMultipartFile(
                "file", "../../avatar.svg", "image/svg+xml", output.toByteArray()), userId.toString());

        assertThat(url).isEqualTo("https://api.example.test/users/avatar/" + userId);
        assertThat(storage.loadFile(userId).exists()).isTrue();
        assertThat(storage.getContentType(userId)).startsWith("image/");
    }

    @Test
    void nonImagePayloadIsRejectedEvenWhenMimeTypeClaimsJpeg() {
        MockMultipartFile fakeImage = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "not-an-image".getBytes());

        assertThatThrownBy(() -> storage.uploadFile(fakeImage, UUID.randomUUID().toString()))
                .isInstanceOf(InvalidFileTypeException.class);
    }
}
