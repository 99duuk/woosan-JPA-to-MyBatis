package com.luckyvicky.woosan.domain.member.mybatisMapper;

import com.luckyvicky.woosan.domain.member.dto.MyBoardDTO;
import com.luckyvicky.woosan.domain.member.dto.MyReplyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyPageMapper {
    List<MyBoardDTO> findMyBoards(@Param("memberId") Long memberId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    List<MyReplyDTO> findMyReplies(@Param("memberId") Long memberId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    List<MyBoardDTO> findLikedBoards(@Param("memberId") Long memberId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    long findMyBoardsTotalCount(@Param("memberId") Long memberId);
    long findLikedBoardsTotalCount(@Param("memberId") Long memberId);

    long findMyRepliesTotalCount(@Param("memberId") Long memberId);
}
