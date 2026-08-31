package com.github.epsilon.gui.lib;

/**
 * 上游 Lumin Graphics SelectionRange 的内建等价 shim。
 *
 * <p>文本输入选区（起止偏移，字符单位）。内建 InputNode 暂未消费选区，
 * 先以纯数据承载保持 API 形状。</p>
 */
public record SelectionRangeShim(int start, int end) {

    public SelectionRangeShim {
        if (end < start) {
            int t = start;
            start = end;
            end = t;
        }
    }

    public int length() {
        return end - start;
    }
}
