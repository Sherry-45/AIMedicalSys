package com.aimedical.modules.device.protocol;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.modules.device.exception.DeviceErrorCode;
import com.aimedical.modules.device.util.JsonEscapeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ASTM E1381/E1394 协议解析器。
 * <p>
 * 解析以记录类型字符开头、字段以 {@code |} 分隔的报文，例如：
 * <pre>
 * H|\^&|||sender||receiver|||||host
 * P|1||patientId||name||age|sex
 * O|1|orderId||^test|||N
 * R|1|^^result|value|unit|normal|N
 * L|1|N
 * </pre>
 * 每行一条记录，第一个字段为记录类型（H/P/O/R/C/Q/L 等）。
 * <p>
 * 输出 JSON 结构：
 * {@code {"records":[{"type":"H","fields":["...","..."]},...]}}.
 * <p>
 * 解析失败抛 {@link DeviceErrorCode#MESSAGE_PARSE_FAILED}。
 */
@Component
public class AstmParser implements ProtocolParser {

    @Override
    public String getProtocol() {
        return "ASTM";
    }

    @Override
    public String parse(String rawContent) {
        if (rawContent == null || rawContent.isEmpty()) {
            throw new BusinessException(DeviceErrorCode.MESSAGE_PARSE_FAILED);
        }
        try {
            String[] lines = rawContent.split("\\r?\\n|\\r");
            List<String> recordJsonList = new ArrayList<>();
            for (String line : lines) {
                if (line == null || line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                String type = parts[0];
                StringBuilder fieldsJson = new StringBuilder("[");
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) {
                        fieldsJson.append(',');
                    }
                    fieldsJson.append('"').append(JsonEscapeUtil.escape(parts[i])).append('"');
                }
                fieldsJson.append(']');
                recordJsonList.add("{\"type\":\"" + JsonEscapeUtil.escape(type)
                        + "\",\"fields\":" + fieldsJson + '}');
            }
            return "{\"records\":[" + String.join(",", recordJsonList) + "]}";
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(DeviceErrorCode.MESSAGE_PARSE_FAILED, e);
        }
    }
}
