package com.example.demo.dto;

public class ExtractionRequest {

    private String sourceType;  // IMAGE, YOUTUBE, INSTAGRAM, URL
    private String sourceUrl;   // URL for YouTube/website

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}