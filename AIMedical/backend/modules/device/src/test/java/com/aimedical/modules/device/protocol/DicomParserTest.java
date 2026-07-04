package com.aimedical.modules.device.protocol;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.modules.device.exception.DeviceErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DicomParser 单元测试。
 */
class DicomParserTest {

    private final DicomParser parser = new DicomParser();

    @Test
    @DisplayName("getProtocol 返回 DICOM")
    void getProtocol() {
        assertEquals("DICOM", parser.getProtocol());
    }

    @Test
    @DisplayName("解析 tag=value 格式报文应生成 tags 对象")
    void parseTagValue() {
        String raw = "00080018=1.2.840.113619\n00100010=PatientName";

        String json = parser.parse(raw);

        assertTrue(json.startsWith("{\"tags\":{"));
        assertTrue(json.contains("\"00080018\":\"1.2.840.113619\""));
        assertTrue(json.contains("\"00100010\":\"PatientName\""));
        assertTrue(json.endsWith("}}"));
    }

    @Test
    @DisplayName("解析含等号的值应仅按首个等号分割")
    void parseValueWithEquals() {
        String raw = "00080018=a=b=c";

        String json = parser.parse(raw);

        assertTrue(json.contains("\"00080018\":\"a=b=c\""));
    }

    @Test
    @DisplayName("解析含需要转义字符的报文应正确转义")
    void parseEscapedChars() {
        String raw = "00100010=name\"with\\slash";

        String json = parser.parse(raw);

        assertTrue(json.contains("name\\\"with\\\\slash"));
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

    @Test
    @DisplayName("解析缺少等号的行应抛 MESSAGE_PARSE_FAILED")
    void parseNoEquals() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> parser.parse("00080018value"));
        assertEquals(DeviceErrorCode.MESSAGE_PARSE_FAILED.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("解析应跳过空行与 # 开头的注释行")
    void parseSkipCommentAndBlankLines() {
        String raw = "# DICOM dump\n00080018=1.2.840\n\n# patient name\n00100010=PatientName\n";

        String json = parser.parse(raw);

        assertTrue(json.contains("\"00080018\":\"1.2.840\""));
        assertTrue(json.contains("\"00100010\":\"PatientName\""));
        // 注释行不应进入 tags
        assertTrue(!json.contains("DICOM dump"));
        assertTrue(!json.contains("patient name"));
    }

    @Test
    @DisplayName("仅含注释和空行的报文应解析为空 tags 对象")
    void parseOnlyCommentsAndBlanks() {
        String raw = "# comment\n\n   \n# another";

        String json = parser.parse(raw);

        assertEquals("{\"tags\":{}}", json);
    }
}
