package com.luckyvicky.woosan.domain.likes.mapper;

import com.luckyvicky.woosan.domain.likes.entity.Likes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface LikesMapper {

    boolean existsByMemberIdAndTypeAndTargetId(@Param("memberId")Long memberId, @Param("type") String type, @Param("targetId") Long targetId);

    Optional<Likes> findByMemberIdAndTypeAndTargetId(@Param("memberId") Long memberId, @Param("type") String type, @Param("targetId") Long targetId);

    void insertLike(Long memberId, String type, Long targetId);

    void deleteLike(Long id);
}
