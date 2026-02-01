package com.lost.blog.service;

import com.lost.blog.dto.HotAuthorResponse;
import java.util.List;

/**
 * 热门作者服务接口
 * 提供热门作者查询功能
 */
public interface HotAuthorService {

    /**
     * 获取热门作者列表
     * 根据加权对数混合模型计算热度值：
     * H = W_a * A + W_f * F + (W_l * L + W_c * C) + W_v * log10(V + 1)
     * 
     * @param limit 返回的作者数量（默认30）
     * @return 热门作者列表
     */
    List<HotAuthorResponse> getHotAuthors(int limit);
}
