package com.luckyvicky.woosan.domain.board.mapper;

import com.luckyvicky.woosan.domain.board.dto.ReplyDTO;
import com.luckyvicky.woosan.domain.likes.mapper.LikeableMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReplyMapper extends LikeableMapper {

    void insertReply(ReplyDTO.Request replyDTO);

    List<ReplyDTO.Response> findRepliesByBoardId(@Param("boardId") Long boardId,  @Param("offset") int offset, @Param("pageSize") int pageSize);
    List<ReplyDTO.Response> findRepliesByParentId(@Param("parentId") Long parentId);


    int deleteById(@Param("id") Long id);
    int deleteByParentId(@Param("parentId") Long parentId);

    void updateLikesCount(@Param("id") Long id, @Param("count") int count);

    @Select("SELECT COUNT(*) FROM reply WHERE board_id = #{boardId} AND parent_id IS NULL")
    int countRepliesByBoardId(@Param("boardId") Long boardId);

    @Select("SELECT Count(*) > 0 FROM reply WHERE id = #{id}")
    boolean existsById(@Param("id") Long id);

    @Select("SELECT writer_id FROM reply WHERE id = #{id}")
    Long findWriterIdById(@Param("id") Long id);

    @Select("SELECT board_id FROM reply WHERE id = #{id}")
    Long findBoardIdById(@Param("id") Long id);

    int getLikesCount(@Param("targetId") Long targetId);
}
