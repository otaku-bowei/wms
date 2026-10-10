package com.wms.common.core.query;

import org.springframework.util.CollectionUtils;

import java.util.*;

public class DeviceEventBuffer {

    private final Deque<String> st;
    private final long firstSeq;
    private long nowIndex;

    DeviceEventBuffer(long firstSeq) {
        this.firstSeq = firstSeq;
        this.nowIndex = Long.MAX_VALUE;
        this.st = new ArrayDeque<>(16);
    }

    // 返回本次可以连续处理的数据，可能为空，也可能一次多条
    List<String> receive(long seq, String payload) {
        if (seq == this.firstSeq) {
            int len = this.st.size();
            List<String> ans = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                ans.add(this.st.pollLast());
            }
            return ans;
        } else {
            if (this.nowIndex > seq) {
                this.nowIndex = seq;
                this.st.addLast(payload);
            }
            return new ArrayList<>();
        }
    }
    //约定：起始序号是 1。先收到 3、2 都返回空列表，再收到 1 时返回这三条，顺序为 1、2、3。序号小于当前游标的迟到数据丢弃。相同序号只保留第一次。单次调用的平均耗时与本次连续输出的条数成正比，不要每次对全部缓存排序。先按单线程写。
}

