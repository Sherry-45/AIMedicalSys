package com.aimedical.modules.device.util;

/**
 * JSON 字符串转义工具类。
 * 供协议解析器（HL7/DICOM/ASTM 等）手工拼接 JSON 时统一调用，避免重复实现。
 */
public final class JsonEscapeUtil {

    private JsonEscapeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 将字符串按 JSON 规范转义。{@code null} 返回空串。
     *
     * @param value 原始字符串
     * @return 转义后的字符串
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
