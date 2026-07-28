package com.wangjin.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.wangjin.common.enums.DataScopeEnum;
import com.wangjin.common.mybatis.annotation.DataPermission;
import com.wangjin.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限：按 LoginUser.dataScope 改写 WHERE。
 * <ul>
 *   <li>ROOT / ALL → 不加条件（本租户全量，租户仍由 TenantLine 隔离）</li>
 *   <li>DEPT / DEPT_AND_SUB / CUSTOM → dept_id IN (...)</li>
 *   <li>SELF → create_by = 当前用户</li>
 * </ul>
 * 仅处理标注了 {@link DataPermission} 的 Mapper 方法。
 */
@Slf4j
public class WjDataPermissionHandler implements DataPermissionHandler {

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        try {
            DataPermission annotation = findAnnotation(mappedStatementId);
            if (annotation == null) {
                return where;
            }
            // 未登录 / 系统任务
            if (SecurityUtils.getUserId() == null || SecurityUtils.getUserId() == 0L) {
                return where;
            }
            // 管理员：本租户全部数据
            if (SecurityUtils.isAllDataScope()) {
                return where;
            }

            Integer scope = SecurityUtils.getDataScope();
            if (scope == null) {
                scope = DataScopeEnum.SELF.getValue();
            }

            Expression scopeExpr = buildScopeExpression(annotation, scope);
            if (scopeExpr == null) {
                return where;
            }
            return where == null ? scopeExpr : new AndExpression(where, new Parenthesis(scopeExpr));
        } catch (Exception e) {
            log.error("数据权限解析失败 mappedStatementId={}: {}", mappedStatementId, e.getMessage());
            return where;
        }
    }

    private Expression buildScopeExpression(DataPermission annotation, Integer scope) {
        String alias = annotation.tableAlias();
        String deptCol = annotation.deptColumn();
        String userCol = annotation.userColumn();

        if (DataScopeEnum.SELF.getValue().equals(scope)) {
            if (userCol == null || userCol.isBlank()) {
                return null;
            }
            return eq(column(alias, userCol), SecurityUtils.getUserId());
        }

        // 部门类范围
        if (DataScopeEnum.DEPT.getValue().equals(scope)
                || DataScopeEnum.DEPT_AND_SUB.getValue().equals(scope)
                || DataScopeEnum.CUSTOM.getValue().equals(scope)) {
            if (deptCol == null || deptCol.isBlank()) {
                return null;
            }
            Set<Long> deptIds = SecurityUtils.getDataScopeDeptIds();
            if (deptIds == null || deptIds.isEmpty()) {
                // 无可见部门 → 看不到任何行（或退化为本人）
                if (userCol != null && !userCol.isBlank()) {
                    return eq(column(alias, userCol), SecurityUtils.getUserId());
                }
                return eq(column(alias, deptCol), -1L);
            }
            if (deptIds.size() == 1) {
                return eq(column(alias, deptCol), deptIds.iterator().next());
            }
            // jsqlparser 4.9：右侧用 ExpressionList 作为 Expression
            ExpressionList<LongValue> list = new ExpressionList<>(
                    deptIds.stream().map(LongValue::new).collect(Collectors.toList()));
            return new InExpression(column(alias, deptCol), list);
        }

        return null;
    }

    private static EqualsTo eq(Expression left, Long right) {
        EqualsTo eq = new EqualsTo();
        eq.setLeftExpression(left);
        eq.setRightExpression(new LongValue(right));
        return eq;
    }

    private static Column column(String alias, String col) {
        if (alias == null || alias.isBlank()) {
            return new Column(col);
        }
        return new Column(alias + "." + col);
    }

    private DataPermission findAnnotation(String mappedStatementId) {
        try {
            int idx = mappedStatementId.lastIndexOf('.');
            if (idx < 0) {
                return null;
            }
            String className = mappedStatementId.substring(0, idx);
            String methodName = mappedStatementId.substring(idx + 1);
            // 分页 count 方法名可能带 _mpCount 后缀
            if (methodName.endsWith("_mpCount")) {
                methodName = methodName.substring(0, methodName.length() - "_mpCount".length());
            }
            Class<?> mapperClass = Class.forName(className);
            DataPermission classAnn = mapperClass.getAnnotation(DataPermission.class);
            for (Method method : mapperClass.getMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }
                DataPermission methodAnn = method.getAnnotation(DataPermission.class);
                if (methodAnn != null) {
                    return methodAnn;
                }
            }
            return classAnn;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
