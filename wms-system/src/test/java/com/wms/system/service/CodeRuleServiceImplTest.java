package com.wms.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.common.core.exception.BizException;
import com.wms.system.domain.entity.SysCodeRule;
import com.wms.system.mapper.SysCodeRuleMapper;
import com.wms.system.service.impl.CodeRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 编码规则服务单元测试
 *
 * <p>覆盖测试点：TP-R1-1.7.2-02 ~ TP-R1-1.7.2-05
 *
 * @author WMS
 */
@ExtendWith(MockitoExtension.class)
class CodeRuleServiceImplTest {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String TODAY = LocalDate.now().format(DAY);

    @Mock
    private SysCodeRuleMapper codeRuleMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CodeRuleServiceImpl codeRuleService;

    @BeforeEach
    void setUp() {
        // 手动构造以显式注入 ServiceImpl 的 baseMapper（Mockito 构造器注入不会填充父类 protected 字段）
        codeRuleService = new CodeRuleServiceImpl(redisTemplate);
        ReflectionTestUtils.setField(codeRuleService, "baseMapper", codeRuleMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("生成编码：前缀 + 日期 + 三位流水")
    void generateNextCode_shouldBuildCode() {
        when(codeRuleMapper.selectOne(any(Wrapper.class), anyBoolean()))
                .thenReturn(buildRule("INBOUND", "IN", "yyyyMMdd", 3, "DAY", 0));
        when(valueOperations.increment(anyString())).thenReturn(1L);

        String code = codeRuleService.generateNextCode("INBOUND");

        assertThat(code).isEqualTo("IN" + TODAY + "001");
        verify(redisTemplate).expire(anyString(), eq(Duration.ofDays(1)));
    }

    @Test
    @DisplayName("生成编码：流水号递增并补零到指定位数")
    void generateNextCode_shouldPadSerial() {
        when(codeRuleMapper.selectOne(any(Wrapper.class), anyBoolean()))
                .thenReturn(buildRule("OUTBOUND", "OUT", "yyyyMMdd", 4, "DAY", 0));
        when(valueOperations.increment(anyString())).thenReturn(12L);

        String code = codeRuleService.generateNextCode("OUTBOUND");

        assertThat(code).isEqualTo("OUT" + TODAY + "0012");
    }

    @Test
    @DisplayName("生成编码：无日期格式时仅拼接前缀与流水")
    void generateNextCode_shouldSkipDate_whenDateFormatEmpty() {
        when(codeRuleMapper.selectOne(any(Wrapper.class), anyBoolean()))
                .thenReturn(buildRule("SKU", "SP", null, 3, "NEVER", 0));
        when(valueOperations.increment(anyString())).thenReturn(2L);

        String code = codeRuleService.generateNextCode("SKU");

        assertThat(code).isEqualTo("SP002");
    }

    @Test
    @DisplayName("生成编码：Redis 不可用时回退数据库流水号")
    void generateNextCode_shouldFallback_whenRedisFailed() {
        when(codeRuleMapper.selectOne(any(Wrapper.class), anyBoolean()))
                .thenReturn(buildRule("INBOUND", "IN", "yyyyMMdd", 3, "DAY", 5));
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("redis down"));

        String code = codeRuleService.generateNextCode("INBOUND");

        assertThat(code).isEqualTo("IN" + TODAY + "006");
    }

    @Test
    @DisplayName("生成编码：规则不存在时抛出 70001")
    void generateNextCode_shouldThrow_whenRuleNotFound() {
        when(codeRuleMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> codeRuleService.generateNextCode("UNKNOWN"));

        assertThat(ex.getCode()).isEqualTo(70001);
    }

    @Test
    @DisplayName("修改编码规则：更新字段并重算示例")
    void updateRule_shouldUpdateAndRebuildSample() {
        SysCodeRule rule = buildRule("INBOUND", "IN", "yyyyMMdd", 3, "DAY", 0);
        when(codeRuleMapper.selectById(1L)).thenReturn(rule);

        codeRuleService.updateRule(1L, "IN", "yyyyMMdd", 4, "DAY");

        verify(codeRuleMapper).updateById(any(SysCodeRule.class));
        assertThat(rule.getSerialLength()).isEqualTo(4);
        assertThat(rule.getSample()).startsWith("IN" + TODAY);
    }

    @Test
    @DisplayName("修改编码规则：规则不存在时抛出 70001")
    void updateRule_shouldThrow_whenRuleNotFound() {
        when(codeRuleMapper.selectById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> codeRuleService.updateRule(1L, "IN", "yyyyMMdd", 3, "DAY"));

        assertThat(ex.getCode()).isEqualTo(70001);
    }

    @Test
    @DisplayName("查询编码规则列表")
    void listRules_shouldReturnList() {
        when(codeRuleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(new SysCodeRule()));

        assertThat(codeRuleService.listRules()).hasSize(1);
    }

    private SysCodeRule buildRule(String ruleType, String prefix, String dateFormat,
                                  Integer serialLength, String serialReset, Integer currentSeq) {
        SysCodeRule rule = new SysCodeRule();
        rule.setId(1L);
        rule.setRuleType(ruleType);
        rule.setRuleName("测试规则");
        rule.setPrefix(prefix);
        rule.setDateFormat(dateFormat);
        rule.setSerialLength(serialLength);
        rule.setSerialReset(serialReset);
        rule.setCurrentSeq(currentSeq);
        rule.setStatus(1);
        return rule;
    }
}
