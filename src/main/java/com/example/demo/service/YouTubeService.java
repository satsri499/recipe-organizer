package com.example.demo.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    // -------------------------------------------------------
    // Fetch video details from YouTube API
    // -------------------------------------------------------
    public VideoDetails fetchVideoDetails(String youtubeUrl) throws Exception {
        System.out.println("YouTube API Key starts with: " +
                (apiKey != null ? apiKey.substring(0, 10) + "..." : "NULL"));
        // 1. Extract video ID from URL
        String videoId = extractVideoId(youtubeUrl);
        if (videoId == null) {
            throw new RuntimeException(
                    "Invalid YouTube URL. Please use a valid YouTube link.");
        }

        System.out.println("Fetching YouTube video ID: " + videoId);

        // 2. Call YouTube Data API
        YouTube youtube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                request -> {}
        ).setApplicationName("recipe-organizer").build();

        VideoListResponse response = youtube.videos()
                .list(Arrays.asList("snippet"))
                .setId(Arrays.asList(videoId))
                .setKey(apiKey)
                .execute();

        // 3. Check if video exists
        if (response.getItems() == null || response.getItems().isEmpty()) {
            throw new RuntimeException(
                    "Video not found. Please check the YouTube URL.");
        }

        Video video = response.getItems().get(0);
        String title = video.getSnippet().getTitle();
        String description = video.getSnippet().getDescription();
        String channelName = video.getSnippet().getChannelTitle();

        System.out.println("Title: " + title);
        System.out.println("Channel: " + channelName);
        System.out.println("Description length: " +
                (description != null ? description.length() : 0) + " chars");

        return new VideoDetails(videoId, title, description, channelName);
    }

    // -------------------------------------------------------
    // Check if video description contains a recipe
    // -------------------------------------------------------
    public boolean containsRecipe(String description) {
        if (description == null || description.trim().isEmpty()) {
            return false;
        }

        String lower = description.toLowerCase();

        boolean hasIngredients = lower.contains("ingredient") ||
                lower.contains("you will need") ||
                lower.contains("you'll need") ||
                lower.contains("what you need") ||
                lower.contains("serves") ||
                lower.contains("makes ");

        boolean hasInstructions = lower.contains("instruction") ||
                lower.contains("direction") ||
                lower.contains("method") ||
                lower.contains("step 1") ||
                lower.contains("how to make") ||
                lower.contains("preheat") ||
                lower.contains("combine") ||
                lower.contains("mix") ||
                lower.contains("bake") ||
                lower.contains("cook");

        boolean hasMeasurements = lower.contains(" cup") ||
                lower.contains(" tbsp") ||
                lower.contains(" tsp") ||
                lower.contains(" gram") ||
                lower.contains(" ml") ||
                lower.contains(" oz") ||
                lower.contains("g ") ||
                lower.contains("kg");

        int signals = (hasIngredients ? 1 : 0) +
                (hasInstructions ? 1 : 0) +
                (hasMeasurements ? 1 : 0);

        System.out.println("Recipe signals: " + signals + "/3" +
                " (ingredients=" + hasIngredients +
                ", instructions=" + hasInstructions +
                ", measurements=" + hasMeasurements + ")");

        return signals >= 2;
    }

    // -------------------------------------------------------
    // Extract video ID from various YouTube URL formats
    // -------------------------------------------------------
    private String extractVideoId(String url) {
        // Handles:
        // https://www.youtube.com/watch?v=VIDEO_ID
        // https://youtu.be/VIDEO_ID
        // https://www.youtube.com/shorts/VIDEO_ID
        Pattern pattern = Pattern.compile(
                "(?:youtube\\.com/(?:watch\\?v=|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    // -------------------------------------------------------
    // Simple class to hold video details
    // -------------------------------------------------------
    public static class VideoDetails {
        public final String videoId;
        public final String title;
        public final String description;
        public final String channelName;

        public VideoDetails(String videoId, String title,
                            String description, String channelName) {
            this.videoId = videoId;
            this.title = title;
            this.description = description;
            this.channelName = channelName;
        }
    }
}