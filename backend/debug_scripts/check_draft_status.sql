-- SQL脚本：检查数据库中文章的草稿状态
-- 用于调试草稿箱功能

-- 1. 查询所有文章的草稿状态
SELECT 
    id,
    title,
    is_draft,
    CASE 
        WHEN is_draft = true THEN '草稿'
        WHEN is_draft = false THEN '已发布'
        ELSE '未知状态'
    END as status_text,
    user_id,
    created_at,
    published_at
FROM posts
ORDER BY created_at DESC;

-- 2. 按用户统计草稿和已发布文章数量
SELECT 
    u.username,
    COUNT(CASE WHEN p.is_draft = true THEN 1 END) as draft_count,
    COUNT(CASE WHEN p.is_draft = false THEN 1 END) as published_count,
    COUNT(*) as total_count
FROM users u
LEFT JOIN posts p ON u.id = p.user_id
GROUP BY u.id, u.username
ORDER BY u.username;

-- 3. 检查is_draft字段的数据类型（可能的问题：字符串而非布尔值）
SELECT 
    id,
    title,
    is_draft,
    TYPEOF(is_draft) as draft_type,
    LENGTH(is_draft) as draft_length
FROM posts
LIMIT 10;

-- 4. 检查所有草稿文章
SELECT 
    p.id,
    p.title,
    u.username,
    p.is_draft,
    p.created_at,
    p.updated_at
FROM posts p
JOIN users u ON p.user_id = u.id
WHERE p.is_draft = true
ORDER BY p.created_at DESC;

-- 5. 检查所有已发布文章
SELECT 
    p.id,
    p.title,
    u.username,
    p.is_draft,
    p.published_at
FROM posts p
JOIN users u ON p.user_id = u.id
WHERE p.is_draft = false
ORDER BY p.published_at DESC;
