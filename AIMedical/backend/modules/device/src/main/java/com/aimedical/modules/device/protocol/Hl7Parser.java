package com.aimedical.modules.device.protocol;

import com.aimedical.modules.device.exception.DeviceErrorCode;
import com.aimedical.common.exception.BusinessException;
import com.aimedical.modules.device.util.JsonEscapeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * HL7 协议解析器。
 * 将 MSH|...|PID|...|OBX|... 等段按换行符分隔，每段按 {@code |} 分隔字段，构建 JSON：
 * {@code {"segments":[{"name":"MSH","fields":["...","...",...]},...]}}.
 * 解析失败抛 {@link DeviceErrorCode#MESSAGE_PARSE_FAILED}.
 */
@Component
public class Hl7Parser implements ProtocolParser {

    @Override
    public String getProtocol() {
        return "HL7";
    }

    @Override
    public String parse(String rawContent) {
        if (rawContent == null || rawContent.isEmpty()) {
            throw new BusinessException(DeviceErrorCode.MESSAGE_PARSE_FAILED);
        }
        try {
            String[] lines = rawContent.split("\\r?\\n|\\r");
            List<String> segmentJsonList = new ArrayList<>();
            for (String line : lines) {
                if (line == null || line.isEmpty()) {
                    continue;
                }
                // split with -1 保留尾部空字符串，length 至少为 1，无需 length==0 判断
                String[] parts = line.split("\\|", -1);
                String name = parts[0];
                StringBuilder fieldsJson = new StringBuilder("[");
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) {
                        fieldsJson.append(',');
                    }
                    fieldsJson.append('"').append(JsonEscapeUtil.escape(parts[i])).append('"');
                }
                fieldsJson.append(']');
                segmentJsonList.add("{\"name\":\"" + JsonEscapeUtil.escape(name)
                        + "\",\"fields\":" + fieldsJson + '}');
            }
            return "{\"segments\":[" + String.join(",", segmentJsonList) + "]}";
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(DeviceErrorCode.MESSAGE_PARSE_FAILED, e);
        }
    }
}
