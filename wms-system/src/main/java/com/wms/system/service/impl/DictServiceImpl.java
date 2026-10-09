package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.common.redis.constant.RedisKeys;
import com.wms.system.domain.entity.SysDict;
import com.wms.system.domain.entity.SysDictItem;
import com.wms.system.mapper.SysDictItemMapper;
import com.wms.system.mapper.SysDictMapper;
import com.wms.system.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * 数据字典服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements DictService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(60);

    private final SysDictItemMapper dictItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<SysDict> listDictTypes(String dictType) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
                .eq(StringUtils.hasText(dictType), SysDict::getDictType, dictType)
                .orderByAsc(SysDict::getId);
        return this.list(wrapper);
    }

    @Override
    public List<SysDictItem> listDictItems(String dictType) {
        if (!StringUtils.hasText(dictType)) {
            return dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>().orderByAsc(SysDictItem::getSort));
        }
        String cacheKey = RedisKeys.dict(dictType);
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cached)) {
                SysDictItem[] items = objectMapper.readValue(cached, SysDictItem[].class);
                return List.of(items);
            }
        } catch (Exception e) {
            log.debug("读取字典缓存失败：dictType={}", dictType);
        }
        List<SysDictItem> items = dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictType, dictType)
                .eq(SysDictItem::getStatus, 1)
                .orderByAsc(SysDictItem::getSort));
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(items), CACHE_TTL);
        } catch (Exception e) {
            log.debug("写入字典缓存失败：dictType={}", dictType);
        }
        return items;
    }

    @Override
    public void refreshCache() {
        List<SysDict> dictTypes = this.list();
        for (SysDict dict : dictTypes) {
            try {
                redisTemplate.delete(RedisKeys.dict(dict.getDictType()));
            } catch (Exception e) {
                log.warn("清理字典缓存失败：dictType={}", dict.getDictType());
            }
        }
        log.info("字典缓存已刷新，共 {} 个字典类型", dictTypes.size());
    }
}
