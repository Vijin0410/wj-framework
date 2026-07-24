package com.wangjin.common.security.util;

import cn.hutool.core.util.StrUtil;
import com.wangjin.common.exception.BizException;
import com.wangjin.common.result.ResultCode;

/**
 * 密码复杂度校验。
 */
public final class PwdCheckUtil {

    public static final String SPECIAL_CHAR = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    private PwdCheckUtil() {
    }

    public static boolean checkPasswordLength(String password, int min, int max) {
        if (StrUtil.isBlank(password)) {
            return false;
        }
        int len = password.length();
        return len >= min && len <= max;
    }

    public static boolean checkContainDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }

    public static boolean checkContainCase(String password) {
        return password.chars().anyMatch(Character::isLetter);
    }

    public static boolean checkContainSpecialChar(String password) {
        return password.chars().anyMatch(c -> SPECIAL_CHAR.indexOf(c) >= 0);
    }

    /**
     * 强密码：>=8 位，含字母、数字、特殊字符。
     */
    public static void checkStrongPwd(String pwd) {
        if (StrUtil.isBlank(pwd)
                || !checkPasswordLength(pwd, 8, 64)
                || !checkContainCase(pwd)
                || !checkContainDigit(pwd)
                || !checkContainSpecialChar(pwd)) {
            throw new BizException(ResultCode.PARAM_ERROR, "密码必须包含字母、数字、特殊符号且不少于8位");
        }
    }
}
