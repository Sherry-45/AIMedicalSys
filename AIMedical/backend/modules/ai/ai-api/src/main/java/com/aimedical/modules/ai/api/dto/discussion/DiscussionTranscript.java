package com.aimedical.modules.ai.api.dto.discussion;

public class DiscussionTranscript {
    private String speakerRole;
    private String speakerName;
    private String timestamp;
    private String content;

    public DiscussionTranscript() {}

    public String getSpeakerRole() { return speakerRole; }
    public void setSpeakerRole(String speakerRole) { this.speakerRole = speakerRole; }
    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
