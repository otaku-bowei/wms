package com.wms.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.redis.constant.RedisKeys;
import com.wms.system.domain.entity.SysCodeRule;
import com.wms.system.mapper.SysCodeRuleMapper;
import com.wms.system.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 编码规则服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeRuleServiceImpl extends ServiceImpl<SysCodeRuleMapper, SysCodeRule> implements CodeRuleService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    private final StringRedisTemplate redisTemplate;

    @Override
    public List<SysCodeRule> listRules() {
        return this.list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(Long id, String prefix, String dateFormat, Integer serialLength, String serialReset) {
        SysCodeRule rule = this.getById(id);
        if (rule == null) {
            throw new BizException(ErrorCode.CODE_RULE_NOT_FOUND);
        }
        rule.setPrefix(prefix);
        rule.setDateFormat(dateFormat);
        rule.setSerialLength(serialLength);
        rule.setSerialReset(serialReset);
        rule.setSample(buildSample(rule));
        this.updateById(rule);
    }

    @Override
    public String generateNextCode(String ruleType) {
        SysCodeRule rule = lambdaQuery().eq(SysCodeRule::getRuleType, ruleType).one();
        if (rule == null) {
            throw new BizException(ErrorCode.CODE_RULE_NOT_FOUND);
        }
        String datePart = resolveDatePart(rule);
        String key = RedisKeys.seq(ruleType, StringUtils.hasText(datePart) ? datePart : "default");

        Long seq;
        try {
            seq = redisTemplate.opsForValue().increment(key);
            if (seq != null && seq == 1) {
                Duration ttl = resolveTtl(rule);
                if (ttl != null) {
                    redisTemplate.expire(key, ttl);
                }
            }
        } catch (Exception e) {
            log.warn("Redis 生成流水号失败，回退数据库：ruleType={}", ruleType);
            seq = (long) (rule.getCurrentSeq() == null ? 1 : rule.getCurrentSeq() + 1);
        }
        if (seq == null) {
            seq = 1L;
        }
        // 持久化流水号（兜底）
        updateCurrentSeq(rule, seq.intValue(), datePart);

        int length = rule.getSerialLength() == null ? 3 : rule.getSerialLength();
        String serial = String.format("%0" + length + "d", seq);
        StringBuilder code = new StringBuilder();
        if (StringUtils.hasText(rule.getPrefix())) {
            code.append(rule.getPrefix());
        }
        if (StringUtils.hasText(datePart)) {
            code.append(datePart);
        }
        code.append(serial);
        return code.toString();
    }

    /* ==================== 私有方法 ==================== */

    private String resolveDatePart(SysCodeRule rule) {
        if (!StringUtils.hasText(rule.getDateFormat())) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return switch (rule.getDateFormat()) {
            case "yyyy" -> now.format(YEAR_FORMATTER);
            case "yyyyMM" -> now.format(MONTH_FORMATTER);
            default -> now.format(DAY_FORMATTER);
        };
    }

    private Duration resolveTtl(SysCodeRule rule) {
        if (rule.getSerialReset() == null) {
            return Duration.ofDays(1);
        }
        return switch (rule.getSerialReset()) {
            case "DAY" -> Duration.ofDays(1);
            case "MONTH" -> Duration.ofDays(31);
            case "YEAR" -> Duration.ofDays(366);
            default -> null;
        };
    }

    private void updateCurrentSeq(SysCodeRule rule, int seq, String datePart) {
        try {
            SysCodeRule update = new SysCodeRule();
            update.setId(rule.getId());
            update.setCurrentSeq(seq);
            update.setCurrentDate(datePart);
            this.updateById(update);
        } catch (Exception e) {
            log.warn("持久化流水号失败：ruleType={}", rule.getRuleType());
        }
    }

    private String buildSample(SysCodeRule rule) {
        int length = rule.getSerialLength() == null ? 3 : rule.getSerialLength();
        StringBuilder sample = new StringBuilder();
        if (StringUtils.hasText(rule.getPrefix())) {
            sample.append(rule.getPrefix());
        }
        if (StringUtils.hasText(rule.getDateFormat())) {
            sample.append(resolveDatePart(rule));
        }
        sample.append("1".repeat(Math.max(0, length)));
        return sample.toString();
    }
}
