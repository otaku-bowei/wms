package com.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.inventory.domain.entity.InvStock;
import com.wms.inventory.domain.entity.InvWarningRecord;
import com.wms.inventory.domain.entity.InvWarningRule;
import com.wms.inventory.mapper.InvStockMapper;
import com.wms.inventory.mapper.InvWarningRecordMapper;
import com.wms.inventory.mapper.InvWarningRuleMapper;
import com.wms.inventory.service.WarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存预警服务实现：扫描库存生成低库存/高库存/临期预警。
 */
@Service
@RequiredArgsConstructor
public class WarningServiceImpl extends ServiceImpl<InvWarningRuleMapper, InvWarningRule> implements WarningService {

    private final InvStockMapper stockMapper;
    private final InvWarningRecordMapper warningRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(InvWarningRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setDeleted(0);
        baseMapper.insert(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(Long id, InvWarningRule rule) {
        rule.setId(id);
        rule.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(rule);
    }

    @Override
    public PageResult<InvWarningRule> pageRules(long pageNum, long pageSize) {
        IPage<InvWarningRule> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, new LambdaQueryWrapper<InvWarningRule>().eq(InvWarningRule::getDeleted, 0));
        return PageConvert.toResult(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndGenerate() {
        List<InvWarningRule> rules = baseMapper.selectList(
                new LambdaQueryWrapper<InvWarningRule>().eq(InvWarningRule::getDeleted, 0).eq(InvWarningRule::getEnabled, 1));
        int count = 0;
        for (InvWarningRule rule : rules) {
            List<InvStock> stocks = stockMapper.selectList(new LambdaQueryWrapper<InvStock>()
                    .eq(InvStock::getWarehouseId, rule.getWarehouseId())
                    .eq(rule.getSkuId() != null, InvStock::getSkuId, rule.getSkuId())
                    .eq(InvStock::getDeleted, 0));
            for (InvStock stock : stocks) {
                int qty = stock.getAvailableQty() == null ? 0 : stock.getAvailableQty();
                if (matchLow(rule, qty) || matchHigh(rule, qty) || matchExpire(rule, stock)) {
                    InvWarningRecord record = new InvWarningRecord();
                    record.setRuleId(rule.getId());
                    record.setSkuId(stock.getSkuId());
                    record.setWarehouseId(stock.getWarehouseId());
                    record.setWarningType(rule.getWarningType());
                    record.setCurrentQty(qty);
                    record.setThresholdValue(rule.getMinQty());
                    record.setStatus("UNHANDLED");
                    record.setTriggerTime(LocalDateTime.now());
                    record.setCreateTime(LocalDateTime.now());
                    record.setUpdateTime(LocalDateTime.now());
                    record.setDeleted(0);
                    warningRecordMapper.insert(record);
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public List<InvWarningRecord> listRecords() {
        return warningRecordMapper.selectList(new LambdaQueryWrapper<InvWarningRecord>()
                .eq(InvWarningRecord::getDeleted, 0).orderByDesc(InvWarningRecord::getTriggerTime));
    }

    private boolean matchLow(InvWarningRule rule, int qty) {
        return "LOW".equals(rule.getWarningType()) && rule.getMinQty() != null && qty < rule.getMinQty();
    }

    private boolean matchHigh(InvWarningRule rule, int qty) {
        return "HIGH".equals(rule.getWarningType()) && rule.getMaxQty() != null && qty > rule.getMaxQty();
    }

    private boolean matchExpire(InvWarningRule rule, InvStock stock) {
        if (!"EXPIRE".equals(rule.getWarningType()) || rule.getExpireDays() == null || stock.getExpiryDate() == null) {
            return false;
        }
        LocalDate threshold = LocalDate.now().plusDays(rule.getExpireDays());
        return !stock.getExpiryDate().isAfter(threshold);
    }
}
