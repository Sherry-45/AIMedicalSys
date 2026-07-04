package com.aimedical.modules.examination.exception;

import com.aimedical.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExaminationErrorCode implements ErrorCode {

    EXAMINATION_NOT_FOUND("EXAMINATION_NOT_FOUND", "检查记录不存在"),
    EXAMINATION_STATUS_INVALID("EXAMINATION_STATUS_INVALID", "检查状态不允许此操作"),
    EXAMINATION_ITEM_EMPTY("EXAMINATION_ITEM_EMPTY", "检查明细不能为空"),
    SCHEDULED_TIME_INVALID("SCHEDULED_TIME_INVALID", "预约时间必须晚于当前时间"),

    // 3.4.5 AI 检查报告错误码（REQ-108：EXAM_AI_ 前缀）
    EXAM_AI_TIMEOUT("EXAM_AI_TIMEOUT", "AI检查报告生成超时"),
    EXAM_AI_UNAVAILABLE("EXAM_AI_UNAVAILABLE", "AI检查报告服务暂不可用"),
    EXAM_AI_INPUT_INVALID("EXAM_AI_INPUT_INVALID", "AI检查报告输入参数无效"),
    EXAM_AI_INTERNAL_IMG_FAIL("EXAM_AI_INTERNAL_IMG_FAIL", "AI检查报告内部影像分析失败"),

    // 3.4.7 AI 影像分析错误码（REQ-108：IMG_AI_ 前缀）
    IMG_AI_TIMEOUT("IMG_AI_TIMEOUT", "AI影像分析超时"),
    IMG_AI_UNAVAILABLE("IMG_AI_UNAVAILABLE", "AI影像分析服务暂不可用"),
    IMG_AI_MODEL_FORBIDDEN("IMG_AI_MODEL_FORBIDDEN", "当前岗位无权使用该影像分析模型"),
    IMG_AI_INPUT_INVALID("IMG_AI_INPUT_INVALID", "AI影像分析输入参数无效"),

    // 3.4.11 AI 执行顺序推荐错误码（REQ-108：EXEC_ORDER_AI_ 前缀）
    EXEC_ORDER_AI_TIMEOUT("EXEC_ORDER_AI_TIMEOUT", "AI执行顺序推荐超时"),
    EXEC_ORDER_AI_UNAVAILABLE("EXEC_ORDER_AI_UNAVAILABLE", "AI执行顺序推荐服务暂不可用"),
    EXEC_ORDER_AI_INPUT_INVALID("EXEC_ORDER_AI_INPUT_INVALID", "AI执行顺序推荐输入参数无效");

    private final String code;
    private final String message;
}
