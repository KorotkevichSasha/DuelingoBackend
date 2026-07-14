package by.gsu.duelingobackend.service.audio.impl;

import by.gsu.duelingobackend.service.audio.SpeechToTextService;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class GoogleSpeechToTextService implements SpeechToTextService {

    @Override
    public String recognize(MultipartFile audioFile) {
        String fileName = audioFile.getOriginalFilename();

        try (SpeechClient speechClient = SpeechClient.create()) {

            ByteString audioBytes = ByteString.readFrom(audioFile.getInputStream());
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setLanguageCode("en-US")
                    .build();

            var results = speechClient.recognize(config, audio).getResultsList();

            if (results.isEmpty()) {
                log.info("No speech recognition results for file: {}", fileName);
                return "";
            }

            var firstResult = results.get(0);
            if (firstResult.getAlternativesList().isEmpty()) {
                log.warn("Speech result contains no alternatives for file: {}", fileName);
                return "";
            }

            String transcript = firstResult.getAlternativesList().get(0).getTranscript();
            log.info("Successfully processed speech recognition for file: {}", fileName);
            return transcript;

        } catch (IOException e) {
            log.error("IO error processing file {}: {}", fileName, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error processing file {}: {}", fileName, e.getMessage(), e);
        }

        return "";
    }
}