package com.aimedical.modules.ai.api.dto.discussion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscussionTranscriptTest {

    @Test
    void shouldDefaultToNullViaNoArgConstructor() {
        DiscussionTranscript t = new DiscussionTranscript();
        assertNull(t.getSpeakerRole());
        assertNull(t.getSpeakerName());
        assertNull(t.getTimestamp());
        assertNull(t.getContent());
    }

    @Test
    void shouldSetAndGetAllFields() {
        DiscussionTranscript t = new DiscussionTranscript();
        t.setSpeakerRole("doctor");
        t.setSpeakerName("张医生");
        t.setTimestamp("2026-07-02T10:00:00");
        t.setContent("患者血压偏高");

        assertEquals("doctor", t.getSpeakerRole());
        assertEquals("张医生", t.getSpeakerName());
        assertEquals("2026-07-02T10:00:00", t.getTimestamp());
        assertEquals("患者血压偏高", t.getContent());
    }

    @Test
    void shouldAllowUpdatingFields() {
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("initial");
        t.setContent("updated");
        assertEquals("updated", t.getContent());
    }
}
