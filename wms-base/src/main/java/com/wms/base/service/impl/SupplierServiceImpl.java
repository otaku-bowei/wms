package com.wms.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.base.domain.dto.SupplierDTO;
import com.wms.base.domain.entity.BaseSupplier;
import com.wms.base.domain.query.SupplierQuery;
import com.wms.base.mapper.BaseSupplierMapper;
import com.wms.base.service.SupplierService;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 供应商服务实现
 *
 * @author WMS
 */
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<BaseSupplierMapper, BaseSupplier> implements SupplierService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSupplier(SupplierDTO dto) {
        if (existsCode(dto.getSupplierCode(), null)) {
            throw new BizException(ErrorCode.SUPPLIER_CODE_EXISTS);
        }
        BaseSupplier entity = new BaseSupplier();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSupplier(Long id, SupplierDTO dto) {
        BaseSupplier entity = this.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.SUPPLIER_NOT_FOUND);
        }
        entity.setSupplierName(dto.getSupplierName());
        entity.setContactName(dto.getContactName());
        entity.setContactPhone(dto.getContactPhone());
        entity.setAddress(dto.getAddress());
        entity.setRemark(dto.getRemark());
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        BaseSupplier entity = this.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.SUPPLIER_NOT_FOUND);
        }
        entity.setStatus(status);
        this.updateById(entity);
    }

    @Override
    public BaseSupplier getById(Long id) {
        // 调用 ServiceImpl 实现，避免使用 this 导致递归
        return super.getById(id);
    }

    @Override
    public PageResult<BaseSupplier> page(SupplierQuery query) {
        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<BaseSupplier>()
                .like(StringUtils.hasText(query.getKeyword()), BaseSupplier::getSupplierCode, query.getKeyword())
                .or().like(StringUtils.hasText(query.getKeyword()), BaseSupplier::getSupplierName, query.getKeyword())
                .eq(query.getStatus() != null, BaseSupplier::getStatus, query.getStatus())
                .orderByAsc(BaseSupplier::getId);
        IPage<BaseSupplier> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageConvert.toResult(page);
    }

    private boolean existsCode(String supplierCode, Long excludeId) {
        Long count = this.count(new LambdaQueryWrapper<BaseSupplier>()
                .eq(BaseSupplier::getSupplierCode, supplierCode)
                .ne(excludeId != null, BaseSupplier::getId, excludeId));
        return count != null && count > 0;
    }
}
