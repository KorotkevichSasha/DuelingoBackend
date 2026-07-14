package by.gsu.duelingobackend.service.audio;

import org.springframework.web.multipart.MultipartFile;

public interface SpeechToTextService {

    String recognize(MultipartFile audioFile);
}
