package com.aimedical.modules.ai.api.dto.discussion;

import java.util.List;

public class DiscussionConclusionRequest {

    private List<DiscussionTranscript> transcripts;

    public DiscussionConclusionRequest() {
    }

    public List<DiscussionTranscript> getTranscripts() { return transcripts; }
    public void setTranscripts(List<DiscussionTranscript> transcripts) { this.transcripts = transcripts; }
}
