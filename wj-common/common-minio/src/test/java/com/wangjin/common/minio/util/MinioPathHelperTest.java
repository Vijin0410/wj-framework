package com.wangjin.common.minio.util;

import com.wangjin.common.exception.BizException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class MinioPathHelperTest {

    @Test
    void buildObjectKey_withTenant() {
        String key = MinioPathHelper.buildObjectKey(1L, "logo", "a.PNG", true);
        Assertions.assertTrue(key.startsWith("1/logo/"));
        Assertions.assertTrue(key.endsWith(".png"));
        Assertions.assertFalse(key.contains(".."));
    }

    @Test
    void buildObjectKey_withoutTenant() {
        String key = MinioPathHelper.buildObjectKey(null, "avatar", "x.jpg", true);
        Assertions.assertTrue(key.startsWith("avatar/"));
        Assertions.assertTrue(key.endsWith(".jpg"));
    }

    @Test
    void assertAllowed_rejectsUnknown() {
        Assertions.assertThrows(BizException.class,
                () -> MinioPathHelper.assertAllowed("exe", List.of("png", "jpg")));
    }

    @Test
    void sanitizeSegment_rejectsTraversal() {
        Assertions.assertThrows(BizException.class,
                () -> MinioPathHelper.sanitizeSegment("../etc"));
    }
}
