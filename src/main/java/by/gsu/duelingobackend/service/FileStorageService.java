package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.exceptions.InvalidFileTypeException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import java.util.Iterator;
import java.util.UUID;

import static by.gsu.duelingobackend.util.Constants.INVALID_IMAGE_TYPE_ERR_MSG;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/avatars/";

    public String uploadFile(MultipartFile file, String filename) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException(String.format(INVALID_IMAGE_TYPE_ERR_MSG, contentType));
        }
        String extension = contentType.split("/")[1];
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(filename + "." + extension);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        String serverUrl = "http://localhost:8082";
        return serverUrl + "/users/avatar/" + filename + "." + extension;
    }

    public Resource loadFile(UUID userId) throws IOException {
        Path directory = Paths.get(UPLOAD_DIR);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, userId + ".*")) {
            Iterator<Path> iterator = stream.iterator();
            if (!iterator.hasNext()) {
                throw new FileNotFoundException("Avatar not found for user: " + userId);
            }

            Path filePath = iterator.next();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IOException("Could not read file: " + filePath);
            }

            return resource;
        }
    }

    public String getContentType(UUID userId) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(UPLOAD_DIR), userId + ".*")) {
            Path filePath = stream.iterator().next();
            return Files.probeContentType(filePath);
        }
    }

    public String calculateETag(UUID userId) throws IOException {
        Resource resource = loadFile(userId);
        try (InputStream is = resource.getInputStream()) {
            String md5Hash = DigestUtils.md5Hex(is);
            return "\"" + md5Hash + "\"";
        }
    }
}
