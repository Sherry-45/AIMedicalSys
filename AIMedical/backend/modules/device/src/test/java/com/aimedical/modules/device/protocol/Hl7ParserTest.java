package com.aimedical.modules.device.protocol;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.modules.device.exception.DeviceErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hl7Parser 单元测试。
 */
class Hl7ParserTest {

    private final Hl7Parser parser = new Hl7Parser();

    @Test
    @DisplayName("getProtocol 返回 HL7")
    void getProtocol() {
        assertEquals("HL7", parser.getProtocol());
    }

    @Test
    @DisplayName("解析单段 MSH 报文应按 | 分隔字段")
    void parseSingleSegment() {
        String raw = "MSH|^~\\&|appid|sender|receiver";

        String json = parser.parse(raw);

        assertTrue(json.contains("\"name\":\"MSH\""));
        assertTrue(json.contains("\"fields\":["));
        assertTrue(json.contains("\"^~\\\\&\""));
        assertTrue(json.contains("\"appid\""));
        assertTrue(json.contains("\"sender\""));
        assertTrue(json.contains("\"receiver\""));
        assertTrue(json.startsWith("{\"segments\":["));
        assertTrue(json.endsWith("]}"));
    }

    @Test
    @DisplayName("解析多段报文（换行分隔）应产生多个 segment")
    void parseMultipleSegments() {
        String raw = "MSH|appid|sender\nPID|1|patientId\nOBX|1|result";

        String json = parser.parse(raw);

        assertTrue(json.contains("\"name\":\"MSH\""));
        assertTrue(json.contains("\"name\":\"PID\""));
        assertTrue(json.contains("\"name\":\"OBX\""));
        // PID 段字段
        assertTrue(json.contains("\"1\""));
        assertTrue(json.contains("\"patientId\""));
    }

    @Test
    @DisplayName("解析含空字段的报文应保留空字符串")
    void parseEmptyField() {
        String raw = "MSH||appid|sender";

        String json = parser.parse(raw);

        assertTrue(json.contains("\"\",\"appid\""));
    }

    @Test
    @DisplayName("解析含需要转义字符的报文应正确转义")
    void parseEscapedChars() {
        String raw = "MSH|a\"b\\c";

        String json = parser.parse(raw);

        assertTrue(json.contains("a\\\"b\\\\c"));
    }

    @Test
    @DisplayName("解析 null 报文应抛 MESSAGE_PARSE_FAILED")
    void parseNull() {
        BusinessException ex = assertThrows(BusinessException.class, () -> parser.parse(null));
        assertEquals(DeviceErrorCode.MESSAGE_PARSE_FAILED.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("解析空字符串报文应抛 MESSAGE_PARSE_FAILED")
    void parseEmpty() {
        BusinessException ex = assertThrows(BusinessException.class, () -> parser.parse(""));
        assertEquals(DeviceErrorCode.MESSAGE_PARSE_FAILED.getCode(), ex.getErrorCode().getCode());
    }
}
