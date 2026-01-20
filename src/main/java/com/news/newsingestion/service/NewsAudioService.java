package com.news.newsingestion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class NewsAudioService {

    private static final Logger log = LoggerFactory.getLogger(NewsAudioService.class);
    private final OpenAiAudioSpeechModel speechModel;

    public NewsAudioService(OpenAiAudioSpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    public byte[] generateNewsAudio(String transcript) {
        log.info("Generating audio for transcript of length: {}", transcript.length());

        // Configure the voice and format
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .voice(OpenAiAudioApi.SpeechRequest.Voice.ONYX) // Professional 'Onyx' voice for news
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .build();

        TextToSpeechPrompt speechPrompt = new TextToSpeechPrompt(transcript, speechOptions);
        TextToSpeechResponse response = speechModel.call(speechPrompt);

        // The output is a byte array (the MP3 data)
        return response.getResult().getOutput();
    }

    public void saveAudioToFile(byte[] audioData, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(audioData);
            log.info("Audio saved successfully to: {}", fileName);
        } catch (IOException e) {
            log.error("Failed to save audio file", e);
            throw new RuntimeException("Audio storage failed", e);
        }
    }
}
