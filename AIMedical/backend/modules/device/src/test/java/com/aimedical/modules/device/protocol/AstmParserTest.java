package com.aimedical.modules.device.protocol;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.modules.device.exception.DeviceErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AstmParser 单元测试。
 */
class AstmParserTest {

    private final AstmParser parser = new AstmParser();

    @Test
    @DisplayName("getProtocol 返回 ASTM")
    void getProtocol() {
        assertEquals("ASTM", parser.getProtocol());
    }

    @Test
    @DisplayName("解析单记录 H 报文应按 | 分隔字段并生成 records 数组")
    void parseSingleRecord() {
        String raw = "H|\\^&|||sender||receiver|||||host";

        String json = parser.parse(raw);

        assertTrue(json.startsWith("{\"records\":["));
        assertTrue(json.endsWith("]}"));
        assertTrue(json.contains("\"type\":\"H\""));
        assertTrue(json.contains("\"fields\":["));
        assertTrue(json.contains("\"\\\\^&\""));
        assertTrue(json.contains("\"sender\""));
        assertTrue(json.contains("\"receiver\""));
        assertTrue(json.contains("\"host\""));
    }

    @Test
    @DisplayName("解析多记录报文（H/P/O/R/L）应产生多条 records")
    void parseMultipleRecords() {
        String raw = "H|sender\nP|1|patientId|name\nO|1|orderId\nR|1|result\nL|1";

        String json = parser.parse(raw);

        assertTrue(json.contains("\"type\":\"H\""));
        assertTrue(json.contains("\"type\":\"P\""));
        assertTrue(json.contains("\"type\":\"O\""));
        assertTrue(json.contains("\"type\":\"R\""));
        assertTrue(json.contains("\"type\":\"L\""));
        assertTrue(json.contains("\"patientId\""));
        assertTrue(json.contains("\"orderId\""));
        assertTrue(json.contains("\"result\""));
    }

    @Test
    @DisplayName("解析含空字段的报文应保留空字符串")
    void parseEmptyField() {
        String raw = "H||sender||";

        String json = parser.parse(raw);

        assertTrue(json.contains("\"\",\"sender\",\"\",\"\""));
    }

    @Test
    @DisplayName("解析含需要转义字符的报文应正确转义")
    void parseEscapedChars() {
        String raw = "H|a\"b\\c";

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
