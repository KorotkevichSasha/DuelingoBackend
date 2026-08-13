package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.exceptions.InvalidFileTypeException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.Iterator;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;

import static by.gsu.duelingobackend.util.Constants.INVALID_IMAGE_TYPE_ERR_MSG;

@Service
public class FileStorageService {

    private static final long MAX_IMAGE_PIXELS = 16_000_000L;
    private static final int MAX_IMAGE_DIMENSION = 4096;

    @Value("${app.storage.avatar-directory:uploads/avatars}")
    private String uploadDirectory;

    @Value("${app.public-base-url:http://localhost:8082}")
    private String publicBaseUrl;

    public String uploadFile(MultipartFile file, String filename) throws IOException {
        UUID.fromString(filename);
        if (file.isEmpty()) {
            throw new InvalidFileTypeException(String.format(INVALID_IMAGE_TYPE_ERR_MSG, "empty file"));
        }

        BufferedImage image;
        String format;
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(file.getInputStream())) {
            if (imageInput == null) {
                throw new InvalidFileTypeException(String.format(INVALID_IMAGE_TYPE_ERR_MSG, file.getContentType()));
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new InvalidFileTypeException(String.format(INVALID_IMAGE_TYPE_ERR_MSG, file.getContentType()));
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new InvalidFileTypeException("Avatar dimensions are not allowed");
                }
                format = reader.getFormatName().toLowerCase();
                if (!format.equals("jpeg") && !format.equals("jpg") && !format.equals("png")) {
                    throw new InvalidFileTypeException(String.format(INVALID_IMAGE_TYPE_ERR_MSG, format));
                }
                image = reader.read(0);
                if (!passesSafetyScreen(image)) {
                    throw new InvalidFileTypeException("Avatar was rejected by the safety filter");
                }
            } finally {
                reader.dispose();
            }
        }

        String extension = format.equals("png") ? "png" : "jpg";
        String outputFormat = extension.equals("png") ? "png" : "jpg";
        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        deleteFilesForUser(uploadPath, UUID.fromString(filename));

        Path filePath = uploadPath.resolve(filename + "." + extension).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new IOException("Invalid avatar path");
        }

        Path temporaryFile = Files.createTempFile(uploadPath, filename + "-", ".tmp");
        try {
            if (!ImageIO.write(image, outputFormat, temporaryFile.toFile())) {
                throw new IOException("Could not encode avatar image");
            }
            try {
                Files.move(temporaryFile, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryFile, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }

        return publicBaseUrl.replaceAll("/+$", "") + "/users/avatar/" + filename;
    }

    public Resource loadFile(UUID userId) throws IOException {
        Path filePath = findAvatar(userId);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Could not read avatar");
        }

        return resource;
    }

    public String getContentType(UUID userId) throws IOException {
        return Files.probeContentType(findAvatar(userId));
    }

    public String calculateETag(UUID userId) throws IOException {
        Resource resource = loadFile(userId);
        try (InputStream is = resource.getInputStream()) {
            String md5Hash = DigestUtils.md5Hex(is);
            return "\"" + md5Hash + "\"";
        }
    }

    /** Conservative first-line moderation for predominantly exposed-skin imagery.
     *  It complements, but does not replace, review/reporting in a production UGC policy. */
    private boolean passesSafetyScreen(BufferedImage image) {
        int step = Math.max(1, Math.min(image.getWidth(), image.getHeight()) / 220);
        long sampled = 0;
        long skinLike = 0;
        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                boolean skin = r > 95 && g > 40 && b > 20 && max - min > 15
                        && Math.abs(r - g) > 15 && r > g && r > b;
                if (skin) skinLike++;
                sampled++;
            }
        }
        return sampled == 0 || (double) skinLike / sampled < 0.72d;
    }

    public void deleteAvatar(UUID userId) throws IOException {
        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        if (Files.exists(uploadPath)) {
            deleteFilesForUser(uploadPath, userId);
        }
    }

    private Path findAvatar(UUID userId) throws IOException {
        Path directory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        if (!Files.isDirectory(directory)) {
            throw new FileNotFoundException("Avatar not found");
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, userId + ".*")) {
            Iterator<Path> iterator = stream.iterator();
            if (!iterator.hasNext()) {
                throw new FileNotFoundException("Avatar not found");
            }
            return iterator.next();
        }
    }

    private void deleteFilesForUser(Path directory, UUID userId) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, userId + ".*")) {
            for (Path path : stream) {
                Files.deleteIfExists(path);
            }
        }
    }
}
