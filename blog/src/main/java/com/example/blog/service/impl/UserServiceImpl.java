package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.entity.UserLikeRecord;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserLikeRecordMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.IUserService;
import com.example.blog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserLikeRecordMapper userLikeRecordMapper;

    @Autowired
    private ArticleMapper articleMapper;
    @Override
    public String login(String username, String password) {
        // 1. 将前端传来的明文密码进行 MD5 加密
        String encryptPwd = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2. 去数据库比对用户名和加密后的密码
        User user = query().eq("username", username).eq("password", encryptPwd).one();

        if (user == null) {
            throw new RuntimeException("用户名或密码错误！");
        }

        // 3. 登录成功，生成并派发 Token
        return jwtUtil.generateToken(user.getId(), user.getRole());
    }

    @Override
    public boolean register(User user) {
        long count = query().eq("username", user.getUsername()).count();
        if (count > 0) throw new RuntimeException("用户名已被占用！");

        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setRole("USER");
        // 如果前端没传昵称，默认使用用户名
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        return save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUserWithCascades(Long userId) {
        // 1. 抹除该用户发出的所有评论
        QueryWrapper<Comment> commentQuery = new QueryWrapper<>();
        commentQuery.eq("user_id", userId);
        commentMapper.delete(commentQuery);

        // 2. 抹除该用户的所有点赞记录，并同步更新对应文章的点赞数
        // 先查询出该用户的所有点赞记录
        QueryWrapper<UserLikeRecord> likeQuery = new QueryWrapper<>();
        likeQuery.eq("user_id", userId);
        List<UserLikeRecord> likeRecords = userLikeRecordMapper.selectList(likeQuery);

        // 遍历每条点赞记录，将对应文章的点赞数减 1
        for (UserLikeRecord record : likeRecords) {
            Article article = articleMapper.selectById(record.getArticleId());
            if (article != null && article.getLikeCount() != null && article.getLikeCount() > 0) {
                article.setLikeCount(article.getLikeCount() - 1);
                articleMapper.updateById(article);
            }
        }

        // 删除该用户的所有点赞记录
        userLikeRecordMapper.delete(likeQuery);

        // 3. 最后，封禁/删除该用户本体
        this.removeById(userId);
    }
}