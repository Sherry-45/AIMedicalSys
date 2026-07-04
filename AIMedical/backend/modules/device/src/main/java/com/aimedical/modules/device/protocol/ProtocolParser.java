package com.aimedical.modules.device.protocol;

/**
 * 协议解析器接口。每种支持的设备协议对应一个实现，按 {@link #getProtocol()} 建索引。
 */
public interface ProtocolParser {

    /**
     * 返回该解析器支持的协议名（与 {@code DeviceProtocol.code} 对应）。
     *
     * @return 协议名
     */
    String getProtocol();

    /**
     * 解析原始报文为 JSON 字符串。
     *
     * @param rawContent 原始报文
     * @return 解析后的 JSON 字符串
     */
    String parse(String rawContent);
}
