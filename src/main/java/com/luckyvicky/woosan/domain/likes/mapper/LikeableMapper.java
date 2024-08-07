package com.luckyvicky.woosan.domain.likes.mapper;

public interface LikeableMapper {
    int getLikesCount(Long targetId);
    void updateLikesCount(Long targetId, int likesCount);
}