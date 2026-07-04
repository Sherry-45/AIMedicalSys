package com.aimedical.modules.window.converter;

import com.aimedical.modules.window.dto.OfflineRegistrationResponse;
import com.aimedical.modules.window.dto.PaymentItemResponse;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.entity.OfflineRegistrationEntity;
import com.aimedical.modules.window.entity.PaymentItemEntity;
import com.aimedical.modules.window.entity.PaymentRecordEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WindowConverterTest {

    private WindowConverter converter;

    @BeforeEach
    void setUp() {
        converter = new WindowConverter();
    }

    // ==================== toRegistrationResponse ====================

    @Test
    void toRegistrationResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toRegistrationResponse(null));
    }

    @Test
    void toRegistrationResponseShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 6, 1, 10, 0);
        OfflineRegistrationEntity entity = new OfflineRegistrationEntity();
        entity.setId(1L);
        entity.setRegistrationNo("OFR20240601001");
        entity.setPatientId(100L);
        entity.setPatientName("张三");
        entity.setPatientPhone("13800000000");
        entity.setIdCard("110101199001011234");
        entity.setDoctorId(200L);
        entity.setDoctorName("李医生");
        entity.setDepartment("内科");
        entity.setRegistrationType("OUTPATIENT");
        entity.setStatus("ACTIVE");
        entity.setRegistrationFee(new BigDecimal("50.00"));
        entity.setOperatorId(300L);
        entity.setOperatorName("操作员");
        entity.setCancelReason("取消原因");
        entity.setCancelTime(now);
        entity.setRemark("备注");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        OfflineRegistrationResponse resp = converter.toRegistrationResponse(entity);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("OFR20240601001", resp.getRegistrationNo());
        assertEquals(100L, resp.getPatientId());
        assertEquals("张三", resp.getPatientName());
        assertEquals("13800000000", resp.getPatientPhone());
        assertEquals("110101199001011234", resp.getIdCard());
        assertEquals(200L, resp.getDoctorId());
        assertEquals("李医生", resp.getDoctorName());
        assertEquals("内科", resp.getDepartment());
        assertEquals("OUTPATIENT", resp.getRegistrationType());
        assertEquals("ACTIVE", resp.getStatus());
        assertEquals(new BigDecimal("50.00"), resp.getRegistrationFee());
        assertEquals(300L, resp.getOperatorId());
        assertEquals("操作员", resp.getOperatorName());
        assertEquals("取消原因", resp.getCancelReason());
        assertEquals(now, resp.getCancelTime());
        assertEquals("备注", resp.getRemark());
        assertEquals(now, resp.getCreatedAt());
        assertEquals(now, resp.getUpdatedAt());
    }

    @Test
    void toRegistrationResponseShouldHandleNullFields() {
        OfflineRegistrationEntity entity = new OfflineRegistrationEntity();
        entity.setId(1L);

        OfflineRegistrationResponse resp = converter.toRegistrationResponse(entity);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertNull(resp.getRegistrationNo());
        assertNull(resp.getPatientName());
        assertNull(resp.getCancelReason());
    }

    // ==================== toPaymentResponse ====================

    @Test
    void toPaymentResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toPaymentResponse(null, Collections.emptyList()));
    }

    @Test
    void toPaymentResponseShouldMapAllFieldsAndItems() {
        LocalDateTime now = LocalDateTime.of(2024, 6, 1, 10, 0);
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setId(1L);
        entity.setPaymentNo("PAY20240601001");
        entity.setPatientId(100L);
        entity.setPatientName("张三");
        entity.setSourceId(500L);
        entity.setSourceType("REGISTRATION");
        entity.setSourceNo("OFR20240601001");
        entity.setTotalAmount(new BigDecimal("100.00"));
        entity.setPaidAmount(new BigDecimal("100.00"));
        entity.setRefundAmount(BigDecimal.ZERO);
        entity.setStatus("PAID");
        entity.setPaymentMethod("CASH");
        entity.setPayerName("张三");
        entity.setOperatorId(300L);
        entity.setOperatorName("操作员");
        entity.setPaidAt(now);
        entity.setRefundedAt(null);
        entity.setReconciledAt(now);
        entity.setRefundReason(null);
        entity.setReconcileBatchNo("RCN001");
        entity.setRemark("备注");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        PaymentItemEntity item = new PaymentItemEntity();
        item.setId(10L);
        item.setPaymentId(1L);
        item.setItemType("REGISTRATION_FEE");
        item.setItemName("挂号费");
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setAmount(new BigDecimal("100.00"));
        item.setRemark("item备注");

        PaymentRecordResponse resp = converter.toPaymentResponse(entity, List.of(item));

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("PAY20240601001", resp.getPaymentNo());
        assertEquals(100L, resp.getPatientId());
        assertEquals("张三", resp.getPatientName());
        assertEquals(500L, resp.getSourceId());
        assertEquals("REGISTRATION", resp.getSourceType());
        assertEquals("OFR20240601001", resp.getSourceNo());
        assertEquals(new BigDecimal("100.00"), resp.getTotalAmount());
        assertEquals(new BigDecimal("100.00"), resp.getPaidAmount());
        assertEquals(BigDecimal.ZERO, resp.getRefundAmount());
        assertEquals("PAID", resp.getStatus());
        assertEquals("CASH", resp.getPaymentMethod());
        assertEquals("张三", resp.getPayerName());
        assertEquals(300L, resp.getOperatorId());
        assertEquals("操作员", resp.getOperatorName());
        assertEquals(now, resp.getPaidAt());
        assertNull(resp.getRefundedAt());
        assertEquals(now, resp.getReconciledAt());
        assertNull(resp.getRefundReason());
        assertEquals("RCN001", resp.getReconcileBatchNo());
        assertEquals("备注", resp.getRemark());
        assertEquals(now, resp.getCreatedAt());
        assertEquals(now, resp.getUpdatedAt());
        assertNotNull(resp.getItems());
        assertEquals(1, resp.getItems().size());
        assertEquals(10L, resp.getItems().get(0).getId());
    }

    @Test
    void toPaymentResponseShouldReturnEmptyItemsWhenItemsNull() {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setId(1L);

        PaymentRecordResponse resp = converter.toPaymentResponse(entity, null);

        assertNotNull(resp);
        assertNotNull(resp.getItems());
        assertTrue(resp.getItems().isEmpty());
    }

    @Test
    void toPaymentResponseShouldMapNullItemToNullEntry() {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setId(1L);
        PaymentItemEntity item = new PaymentItemEntity();
        item.setId(10L);
        item.setPaymentId(1L);

        // List.of 不允许 null 元素，使用 Arrays.asList 以包含 null
        PaymentRecordResponse resp = converter.toPaymentResponse(entity, java.util.Arrays.asList(null, item));

        assertNotNull(resp.getItems());
        // toItemResponse(null) 返回 null，map 不做过滤，列表保持两个元素
        assertEquals(2, resp.getItems().size());
        assertNull(resp.getItems().get(0));
        assertNotNull(resp.getItems().get(1));
    }

    // ==================== toItemResponse ====================

    @Test
    void toItemResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toItemResponse(null));
    }

    @Test
    void toItemResponseShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 6, 1, 10, 0);
        PaymentItemEntity entity = new PaymentItemEntity();
        entity.setId(10L);
        entity.setPaymentId(1L);
        entity.setItemType("DRUG_FEE");
        entity.setItemName("阿莫西林");
        entity.setQuantity(new BigDecimal("2"));
        entity.setUnitPrice(new BigDecimal("15.50"));
        entity.setAmount(new BigDecimal("31.00"));
        entity.setRemark("明细备注");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        PaymentItemResponse resp = converter.toItemResponse(entity);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals(1L, resp.getPaymentId());
        assertEquals("DRUG_FEE", resp.getItemType());
        assertEquals("阿莫西林", resp.getItemName());
        assertEquals(new BigDecimal("2"), resp.getQuantity());
        assertEquals(new BigDecimal("15.50"), resp.getUnitPrice());
        assertEquals(new BigDecimal("31.00"), resp.getAmount());
        assertEquals("明细备注", resp.getRemark());
        assertEquals(now, resp.getCreatedAt());
        assertEquals(now, resp.getUpdatedAt());
    }

    @Test
    void toItemResponseShouldHandleNullFields() {
        PaymentItemEntity entity = new PaymentItemEntity();
        entity.setId(1L);

        PaymentItemResponse resp = converter.toItemResponse(entity);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertNull(resp.getItemType());
        assertNull(resp.getItemName());
        assertNull(resp.getUnitPrice());
    }

    // ==================== toPaymentResponses (batch) ====================

    @Test
    void toPaymentResponsesShouldGroupItemsByPaymentId() {
        PaymentRecordEntity e1 = new PaymentRecordEntity();
        e1.setId(1L);
        e1.setPaymentNo("PAY001");
        PaymentRecordEntity e2 = new PaymentRecordEntity();
        e2.setId(2L);
        e2.setPaymentNo("PAY002");

        PaymentItemEntity item1 = new PaymentItemEntity();
        item1.setId(10L);
        item1.setPaymentId(1L);
        item1.setItemName("item1");
        PaymentItemEntity item2 = new PaymentItemEntity();
        item2.setId(11L);
        item2.setPaymentId(1L);
        item2.setItemName("item2");
        PaymentItemEntity item3 = new PaymentItemEntity();
        item3.setId(20L);
        item3.setPaymentId(2L);
        item3.setItemName("item3");

        List<PaymentRecordResponse> responses = converter.toPaymentResponses(
                List.of(e1, e2), List.of(item1, item2, item3));

        assertEquals(2, responses.size());
        assertEquals("PAY001", responses.get(0).getPaymentNo());
        assertEquals(2, responses.get(0).getItems().size());
        assertEquals("PAY002", responses.get(1).getPaymentNo());
        assertEquals(1, responses.get(1).getItems().size());
    }

    @Test
    void toPaymentResponsesShouldReturnEmptyItemsWhenAllItemsNull() {
        PaymentRecordEntity e1 = new PaymentRecordEntity();
        e1.setId(1L);
        e1.setPaymentNo("PAY001");

        List<PaymentRecordResponse> responses = converter.toPaymentResponses(List.of(e1), null);

        assertEquals(1, responses.size());
        assertNotNull(responses.get(0).getItems());
        assertTrue(responses.get(0).getItems().isEmpty());
    }

    @Test
    void toPaymentResponsesShouldHandleEntityWithoutMatchingItems() {
        PaymentRecordEntity e1 = new PaymentRecordEntity();
        e1.setId(1L);
        e1.setPaymentNo("PAY001");

        PaymentItemEntity item = new PaymentItemEntity();
        item.setId(10L);
        item.setPaymentId(999L); // 属于其他缴费记录

        List<PaymentRecordResponse> responses = converter.toPaymentResponses(List.of(e1), List.of(item));

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getItems().isEmpty());
    }

    @Test
    void toPaymentResponsesShouldReturnEmptyListForEmptyEntities() {
        List<PaymentRecordResponse> responses = converter.toPaymentResponses(Collections.emptyList(), null);
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}
