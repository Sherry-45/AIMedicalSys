package com.aimedical.modules.ai.api.dto.discussion;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscussionConclusionRequestTest {

    @Test
    void shouldDefaultToNullTranscripts() {
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        assertNull(request.getTranscripts());
    }

    @Test
    void shouldSetAndGetTranscripts() {
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        DiscussionTranscript t1 = new DiscussionTranscript();
        t1.setSpeakerRole("doctor");
        t1.setContent("血压偏高");
        DiscussionTranscript t2 = new DiscussionTranscript();
        t2.setSpeakerRole("patient");
        t2.setContent("我最近头晕");

        List<DiscussionTranscript> transcripts = new ArrayList<>();
        transcripts.add(t1);
        transcripts.add(t2);
        request.setTranscripts(transcripts);

        assertEquals(2, request.getTranscripts().size());
        assertEquals("doctor", request.getTranscripts().get(0).getSpeakerRole());
        assertEquals("血压偏高", request.getTranscripts().get(0).getContent());
        assertEquals("patient", request.getTranscripts().get(1).getSpeakerRole());
    }

    @Test
    void shouldAllowEmptyTranscriptsList() {
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        request.setTranscripts(new ArrayList<>());
        assertNotNull(request.getTranscripts());
        assertTrue(request.getTranscripts().isEmpty());
    }

    @Test
    void shouldAllowReplacingTranscripts() {
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        request.setTranscripts(List.of(new DiscussionTranscript()));
        assertEquals(1, request.getTranscripts().size());
        request.setTranscripts(new ArrayList<>());
        assertTrue(request.getTranscripts().isEmpty());
    }
}
