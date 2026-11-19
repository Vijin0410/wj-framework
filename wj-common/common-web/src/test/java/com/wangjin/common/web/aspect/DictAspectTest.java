package com.wangjin.common.web.aspect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wangjin.common.constant.CacheConstants;
import com.wangjin.common.redis.service.RedisService;
import com.wangjin.common.result.PageResult;
import com.wangjin.common.result.Result;
import com.wangjin.common.security.context.LoginUser;
import com.wangjin.common.security.context.TenantContext;
import com.wangjin.common.security.context.UserContext;
import com.wangjin.common.web.annotation.Dict;
import com.wangjin.common.web.model.Option;
import com.wangjin.common.web.util.DictUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DictAspectTest {

    private RedisService redis;
    private DictAspect aspect;

    @BeforeEach
    void setUp() {
        redis = mock(RedisService.class);
        aspect = new DictAspect(redis, mock(DictUtils.class));
        UserContext.set(LoginUser.builder().tenantId(1L).roles(Set.of("ROOT")).build());
        doReturn(List.of(new Option<>("1", "现金"), new Option<>("2", "微信")))
                .when(redis).getCacheList(CacheConstants.SYS_DICT_KEY + "pay_method");
        doReturn(List.of(Map.of("value", "1", "label", "已支付")))
                .when(redis).getCacheList(CacheConstants.SYS_DICT_KEY + "order_status");
    }

    @AfterEach
    void clearContext() {
        TenantContext.clear();
        UserContext.clear();
    }

    @Test
    void rootScopeUsesTenantDictionaryOverrides() throws Throwable {
        doReturn(List.of(new Option<>("1", "租户现金")))
                .when(redis).getCacheList(CacheConstants.SYS_DICT_KEY + "pay_method:77");
        try (TenantContext.Scope scope = TenantContext.use(77L)) {
            Map<?, ?> data = resultData(Result.success(new Payment(1)));
            assertEquals("租户现金", data.get("payMethod_text"));
        }
    }

    @Test
    void translatesNestedPaymentsAndRefundPaymentsFromOriginalBeans() throws Throwable {
        Detail detail = new Detail();
        detail.payments = List.of(new Payment(2));
        Refund refund = new Refund();
        refund.payments = List.of(new Payment(1));
        detail.refunds = List.of(refund);

        Map<?, ?> data = resultData(Result.success(detail));
        Map<?, ?> payment = first(data.get("payments"));
        assertEquals(2, payment.get("payMethod"));
        assertEquals("微信", payment.get("payMethod_text"));
        assertEquals("现金", first(first(data.get("refunds")).get("payments")).get("payMethod_text"));
        assertEquals(2, detail.payments.get(0).payMethod);
    }

    @Test
    void translatesNestedBeanAndInheritedFields() throws Throwable {
        Detail detail = new Detail();
        detail.status = 1;
        detail.payment = new Payment(1);
        Map<?, ?> data = resultData(Result.success(detail));
        assertEquals("已支付", data.get("status_text"));
        assertEquals("现金", ((Map<?, ?>) data.get("payment")).get("payMethod_text"));
    }

    @Test
    void translatesRecursiveTreeChildren() throws Throwable {
        Node root = new Node();
        root.payMethod = 1;
        Node child = new Node();
        child.payMethod = 2;
        root.children = List.of(child);
        Map<?, ?> data = first(((Result<?>) translate(Result.success(List.of(root)))).getData());
        assertEquals("现金", data.get("payMethod_text"));
        assertEquals("微信", first(data.get("children")).get("payMethod_text"));
    }

    @Test
    void translatesPageAndKeepsTotal() throws Throwable {
        Detail detail = new Detail();
        detail.payments = List.of(new Payment(2));
        PageResult<?> page = (PageResult<?>) translate(PageResult.success(List.of(detail), 37));
        assertEquals(37L, page.getData().getTotal());
        assertEquals("微信", first(first(page.getData().getList()).get("payments")).get("payMethod_text"));
    }

    @Test
    void supportsNullEmptyAndUnknownValuesWithoutDroppingFields() throws Throwable {
        Detail detail = new Detail();
        detail.payments = Arrays.asList(new Payment(null), null, new Payment(99));
        detail.refunds = List.of();
        Map<?, ?> data = resultData(Result.success(detail));
        List<?> payments = (List<?>) data.get("payments");
        assertEquals(2, payments.size());
        assertNull(((Map<?, ?>) payments.get(0)).get("payMethod"));
        assertEquals("-", ((Map<?, ?>) payments.get(0)).get("payMethod_text"));
        assertEquals("-", ((Map<?, ?>) payments.get(1)).get("payMethod_text"));
        assertNull(data.get("payment"));
        assertEquals(List.of(), data.get("refunds"));
    }

    @Test
    void keepsTimeFormattingAndScalarList() throws Throwable {
        Detail detail = new Detail();
        detail.paidTime = LocalDateTime.of(2026, 11, 12, 9, 8, 7);
        detail.tags = List.of("常客", "烫染");
        Map<?, ?> data = resultData(Result.success(detail));
        assertEquals("2026-11-12 09:08:07", data.get("paidTime"));
        assertEquals(detail.tags, data.get("tags"));
    }

    @Test
    void doesNotReintroduceIgnoredNestedObjects() throws Throwable {
        Detail detail = new Detail();
        detail.internalPayment = new Payment(1);
        assertFalse(resultData(Result.success(detail)).containsKey("internalPayment"));
    }

    @Test
    void tenantOverridesCommonLabelAndFallsBackToCommonValues() throws Throwable {
        UserContext.set(LoginUser.builder().tenantId(22L).build());
        doReturn(List.of(new Option<>("2", "门店微信")))
                .when(redis).getCacheList(CacheConstants.SYS_DICT_KEY + "pay_method:22");
        assertEquals("门店微信", resultData(Result.success(new Payment(2))).get("payMethod_text"));
        assertEquals("现金", resultData(Result.success(new Payment(1))).get("payMethod_text"));
    }

    @Test
    void rootOnlyReadsCommonDictionary() throws Throwable {
        assertEquals("微信", resultData(Result.success(new Payment(2))).get("payMethod_text"));
        verify(redis, never()).getCacheList(CacheConstants.SYS_DICT_KEY + "pay_method:1");
    }

    private Object translate(Object response) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(response);
        return aspect.translation(joinPoint);
    }

    private Map<?, ?> resultData(Object response) throws Throwable {
        return (Map<?, ?>) ((Result<?>) translate(response)).getData();
    }

    private Map<?, ?> first(Object value) {
        return (Map<?, ?>) ((List<?>) value).get(0);
    }

    public static class Payment {
        @Dict(dictCode = "pay_method")
        public Integer payMethod;

        public Payment(Integer payMethod) {
            this.payMethod = payMethod;
        }
    }

    public static class Base {
        @Dict(dictCode = "order_status")
        public Integer status;
    }

    public static class Detail extends Base {
        public List<Payment> payments;
        public List<Refund> refunds;
        public Payment payment;
        public LocalDateTime paidTime;
        public List<String> tags;
        @JsonIgnore
        public Payment internalPayment;
    }

    public static class Refund {
        public List<Payment> payments;
    }

    public static class Node {
        @Dict(dictCode = "pay_method")
        public Integer payMethod;
        public List<Node> children;
    }
}
