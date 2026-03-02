package com.lost.blog.service;

import com.lost.blog.dto.DashboardResponse;
import com.lost.blog.repository.CategoryRepository;
import com.lost.blog.repository.CommentRepository;
import com.lost.blog.repository.LikeRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.PostViewLogRepository;
import com.lost.blog.repository.ReportRepository;
import com.lost.blog.repository.TagRepository;
import com.lost.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private PostViewLogRepository postViewLogRepository;
    @Mock private TagRepository tagRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private ReportRepository reportRepository;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        when(postRepository.findTopHotPosts(10)).thenReturn(Collections.emptyList());
        when(tagRepository.findPopularTags()).thenReturn(Collections.emptyList());
        when(categoryRepository.findPopularCategories()).thenReturn(Collections.emptyList());

        dashboardService = new DashboardServiceImpl(
                userRepository,
                postRepository,
                commentRepository,
                postViewLogRepository,
                tagRepository,
                categoryRepository,
                likeRepository,
                reportRepository
        );
    }

    @Test
    void shouldBuildDailyTrendForSelectedMonth() {
        DashboardResponse response = dashboardService.getDashboardData("admin", "2026-02", "day");

        assertEquals(28, response.getUserTrend().size());
        assertEquals("02-01", response.getUserTrend().get(0).getDate());
        assertEquals("02-28", response.getUserTrend().get(27).getDate());
    }

    @Test
    void shouldBuildMonthlyTrendForSelectedAnchorMonth() {
        DashboardResponse response = dashboardService.getDashboardData("admin", "2026-02", "month");

        assertEquals(12, response.getUserTrend().size());
        assertEquals("2025-03", response.getUserTrend().get(0).getDate());
        assertEquals("2026-02", response.getUserTrend().get(11).getDate());
    }
}

