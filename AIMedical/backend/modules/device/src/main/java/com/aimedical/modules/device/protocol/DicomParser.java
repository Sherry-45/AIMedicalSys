package com.aimedical.modules.device.protocol;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.modules.device.exception.DeviceErrorCode;
import com.aimedical.modules.device.util.JsonEscapeUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DICOM 协议解析器。
 * 将形如 {@code tag=value} 的每行报文解析为 JSON：
 * {@code {"tags":{"00080018":"1.2.840...","00100010":"PatientName",...}}}.
 * <p>
 * 跳过空行与以 {@code #} 开头的注释行（DICOM dump 文件常见）。
 * <p>
 * 解析失败抛 {@link DeviceErrorCode#MESSAGE_PARSE_FAILED}.
 */
@Component
public class DicomParser implements ProtocolParser {

    @Override
    public String getProtocol() {
        return "DICOM";
    }

    @Override
    public String parse(String rawContent) {
        if (rawContent == null || rawContent.isEmpty()) {
            throw new BusinessException(DeviceErrorCode.MESSAGE_PARSE_FAILED);
        }
        try {
            String[] lines = rawContent.split("\\r?\\n|\\r");
            Map<String, String> tags = new LinkedHashMap<>();
            for (String line : lines) {
                // 跳过空行（含纯空白）与注释行（# 开头）
                String trimmed = line == null ? "" : line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx < 0) {
                    throw new BusinessException(DeviceErrorCode.MESSAGE_PARSE_FAILED);
                }
                String key = line.substring(0, idx).trim();
                // F3: value 需 trim，避免行尾换行符/空白残留导致数据不一致
                String value = line.substring(idx + 1).trim();
                if (key.isEmpty()) {
                    throw new BusinessException(DeviceErrorCode.MESSAGE_PARSE_FAILED);
                }
                tags.put(key, value);
            }
            StringBuilder sb = new StringBuilder("{\"tags\":{");
            boolean first = true;
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('"').append(JsonEscapeUtil.escape(entry.getKey())).append("\":\"")
                        .append(JsonEscapeUtil.escape(entry.getValue())).append('"');
            }
            sb.append("}}");
            return sb.toString();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(DeviceErrorCode.MESSAGE_PARSE_FAILED, e);
        }
    }
}
