package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.core.result.PageResult;
import com.wms.common.redis.constant.RedisKeys;
import com.wms.system.domain.entity.SysConfig;
import com.wms.system.mapper.SysConfigMapper;
import com.wms.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 系统参数服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ConfigService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(60);

    private final StringRedisTemplate redisTemplate;

    @Override
    public List<SysConfig> listParams(String paramGroup) {
        return this.list(new LambdaQueryWrapper<SysConfig>()
                .eq(StringUtils.hasText(paramGroup), SysConfig::getParamGroup, paramGroup)
                .orderByAsc(SysConfig::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            SysConfig config = this.getOne(Wrappers.<SysConfig>lambdaQuery()
                    .eq(SysConfig::getParamKey, entry.getKey()));
            if (config == null) {
                log.warn("系统参数不存在，已跳过：paramKey={}", entry.getKey());
                continue;
            }
            SysConfig update = new SysConfig();
            update.setId(config.getId());
            update.setParamValue(entry.getValue());
            this.updateById(update);
            clearCache(entry.getKey());
        }
    }

    @Override
    public String getParamValue(String paramKey) {
        try {
            String cached = redisTemplate.opsForValue().get(RedisKeys.config(paramKey));
            if (StringUtils.hasText(cached)) {
                return cached;
            }
        } catch (Exception e) {
            log.debug("读取参数缓存失败：paramKey={}", paramKey);
        }
        SysConfig config = this.getOne(Wrappers.<SysConfig>lambdaQuery()
                .eq(SysConfig::getParamKey, paramKey));
        String value = config == null ? null : config.getParamValue();
        if (value != null) {
            try {
                redisTemplate.opsForValue().set(RedisKeys.config(paramKey), value, CACHE_TTL);
            } catch (Exception e) {
                log.debug("写入参数缓存失败：paramKey={}", paramKey);
            }
        }
        return value;
    }

    private void clearCache(String paramKey) {
        try {
            redisTemplate.delete(RedisKeys.config(paramKey));
        } catch (Exception e) {
            log.warn("清理参数缓存失败：paramKey={}", paramKey);
        }
    }
}
