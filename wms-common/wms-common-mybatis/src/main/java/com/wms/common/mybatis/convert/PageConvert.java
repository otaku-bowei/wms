package com.wms.common.mybatis.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.core.result.PageResult;

/**
 * MyBatis-Plus 分页结果转换器
 *
 * @author WMS
 */
public final class PageConvert {

    private PageConvert() {
    }

    /**
     * 将 MyBatis-Plus 分页对象转换为统一分页结果
     *
     * @param page 分页对象
     * @param <T>  数据类型
     * @return 统一分页结果
     */
    public static <T> PageResult<T> toResult(IPage<T> page) {
        if (page == null) {
            return PageResult.empty();
        }
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
