package com.luckyvicky.woosan.domain.board.mapper;

import com.luckyvicky.woosan.domain.board.entity.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BoardMapper {

    void updateReplyCount(@Param("boardId") Long boardId, @Param("count") int count);



    @Select("SELECT COUNT(*) > 0 FROM board WHERE id = #{boardId}")
    boolean existsById(@Param("boardId") Long boardId);

    @Select("SELECT * FROM board WHERE id = #{boardId}")
    Board findBoardById(@Param("boardId") Long boardId);
}
