package com.aimedical.modules.device.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JsonEscapeUtil 单元测试。
 */
class JsonEscapeUtilTest {

    @Test
    @DisplayName("escape(null) 返回空串")
    void escapeNull() {
        assertEquals("", JsonEscapeUtil.escape(null));
    }

    @Test
    @DisplayName("escape(\"\") 返回空串")
    void escapeEmpty() {
        assertEquals("", JsonEscapeUtil.escape(""));
    }

    @Test
    @DisplayName("escape 普通字符串应原样返回")
    void escapePlain() {
        assertEquals("hello world", JsonEscapeUtil.escape("hello world"));
    }

    @Test
    @DisplayName("escape 应转义双引号与反斜杠")
    void escapeQuotesAndBackslash() {
        assertEquals("a\\\"b\\\\c", JsonEscapeUtil.escape("a\"b\\c"));
    }

    @Test
    @DisplayName("escape 应转义控制字符 \\b \\f \\n \\r \\t")
    void escapeControlChars() {
        assertEquals("\\b\\f\\n\\r\\t", JsonEscapeUtil.escape("\b\f\n\r\t"));
    }

    @Test
    @DisplayName("escape 应将 0x20 以下其他控制字符转义为 \\uXXXX")
    void escapeOtherControlChars() {
        assertEquals("\\u0001\\u001f", JsonEscapeUtil.escape("\u0001\u001f"));
    }

    @Test
    @DisplayName("escape 不应修改中文字符")
    void escapeChineseChars() {
        assertEquals("患者信息", JsonEscapeUtil.escape("患者信息"));
    }
}
