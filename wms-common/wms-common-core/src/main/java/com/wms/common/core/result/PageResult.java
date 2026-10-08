package com.wms.common.core.result;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页结果
 *
 * @author WMS
 */
@Data
@Accessors(chain = true)
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> list;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long pageNum;

    /** 每页条数 */
    private long pageSize;

    /** 总页数 */
    private long pages;

    public static <T> PageResult<T> of(List<T> list, long total, long pageNum, long pageSize) {
        long pages = pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize;
        PageResult<T> result = new PageResult<>();
        result.setList(list == null ? Collections.emptyList() : list)
                .setTotal(total)
                .setPageNum(pageNum)
                .setPageSize(pageSize)
                .setPages(pages);
        return result;
    }

    public static <T> PageResult<T> empty() {
        return of(Collections.emptyList(), 0, 1, 20);
    }
}
