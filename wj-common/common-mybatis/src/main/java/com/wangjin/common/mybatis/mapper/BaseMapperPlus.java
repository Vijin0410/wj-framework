package com.wangjin.common.mybatis.mapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Mapper 扩展：批量操作 + 直接查 VO。
 *
 * @param <T> 实体
 * @param <V> VO
 */
@SuppressWarnings("unchecked")
public interface BaseMapperPlus<T, V> extends BaseMapper<T> {

    default Class<V> currentVoClass() {
        return (Class<V>) ReflectionKit.getSuperClassGenericType(this.getClass(), BaseMapperPlus.class, 1);
    }

    default Class<T> currentModelClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(this.getClass(), BaseMapperPlus.class, 0);
    }

    default List<T> selectList() {
        return this.selectList(new QueryWrapper<>());
    }

    default boolean insertBatch(Collection<T> entityList) {
        return Db.saveBatch(entityList);
    }

    default boolean insertBatch(Collection<T> entityList, int batchSize) {
        return Db.saveBatch(entityList, batchSize);
    }

    default boolean updateBatchById(Collection<T> entityList) {
        return Db.updateBatchById(entityList);
    }

    default boolean updateBatchById(Collection<T> entityList, int batchSize) {
        return Db.updateBatchById(entityList, batchSize);
    }

    default boolean insertOrUpdate(T entity) {
        return Db.saveOrUpdate(entity);
    }

    default boolean insertOrUpdateBatch(Collection<T> entityList) {
        return Db.saveOrUpdateBatch(entityList);
    }

    default V selectVoById(Serializable id) {
        return selectVoById(id, currentVoClass());
    }

    default <C> C selectVoById(Serializable id, Class<C> voClass) {
        T obj = this.selectById(id);
        return ObjectUtil.isNull(obj) ? null : BeanUtil.copyProperties(obj, voClass);
    }

    default List<V> selectVoList(Wrapper<T> wrapper) {
        return selectVoList(wrapper, currentVoClass());
    }

    default <C> List<C> selectVoList(Wrapper<T> wrapper, Class<C> voClass) {
        List<T> list = this.selectList(wrapper);
        return CollUtil.isEmpty(list) ? CollUtil.newArrayList() : BeanUtil.copyToList(list, voClass);
    }

    default V selectVoOne(Wrapper<T> wrapper) {
        T obj = this.selectOne(wrapper);
        return ObjectUtil.isNull(obj) ? null : BeanUtil.copyProperties(obj, currentVoClass());
    }

    default List<V> selectVoByMap(Map<String, Object> map) {
        List<T> list = this.selectByMap(map);
        return CollUtil.isEmpty(list) ? CollUtil.newArrayList() : BeanUtil.copyToList(list, currentVoClass());
    }

    default <C> IPage<C> selectVoPage(IPage<T> page, Wrapper<T> wrapper, Class<C> voClass) {
        IPage<T> pageData = this.selectPage(page, wrapper);
        IPage<C> voPage = new Page<>(pageData.getCurrent(), pageData.getSize(), pageData.getTotal());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return voPage;
        }
        voPage.setRecords(BeanUtil.copyToList(pageData.getRecords(), voClass));
        return voPage;
    }

    default IPage<V> selectVoPage(IPage<T> page, Wrapper<T> wrapper) {
        return selectVoPage(page, wrapper, currentVoClass());
    }
}
