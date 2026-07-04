package com.aimedical.modules.labtest.exception;

import com.aimedical.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 检验域错误码枚举。
 */
@Getter
@AllArgsConstructor
public enum LabTestErrorCode implements ErrorCode {

    LAB_TEST_NOT_FOUND("LAB_TEST_NOT_FOUND", "检验记录不存在"),
    LAB_TEST_STATUS_INVALID("LAB_TEST_STATUS_INVALID", "检验状态不允许此操作"),
    LAB_AI_TIMEOUT("LAB_AI_TIMEOUT", "AI 检验报告服务超时"),
    LAB_AI_UNAVAILABLE("LAB_AI_UNAVAILABLE", "AI 检验报告服务不可用"),
    LAB_AI_INPUT_INVALID("LAB_AI_INPUT_INVALID", "AI 检验报告输入参数无效"),
    LAB_TEST_ITEM_EMPTY("LAB_TEST_ITEM_EMPTY", "检验明细不能为空");

    private final String code;
    private final String message;
}
