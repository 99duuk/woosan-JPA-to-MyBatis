package com.luckyvicky.woosan.domain.board.mapper;

import com.luckyvicky.woosan.domain.board.dto.BoardDTO;
import com.luckyvicky.woosan.domain.board.dto.BoardListDTO;
import com.luckyvicky.woosan.domain.board.dto.UpdateBoardDTO;
import com.luckyvicky.woosan.domain.likes.mapper.LikeableMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BoardMapper extends LikeableMapper {

    Long insertBoard(BoardDTO boardDTO);


    List<BoardListDTO> findAllByCategoryName(@Param("categoryName") String categoryName, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countBoardsByCategory(@Param("categoryName") String categoryName);

    BoardListDTO findFirstNotice(@Param("categoryName") String categoryName);
    List<BoardListDTO> findTop3OrderByViewsDesc();

    BoardDTO findById(@Param("id") Long id);

    void updateBoard(BoardDTO boardDTO);
    UpdateBoardDTO findByIdForUpdate(@Param("id") Long id);

    void deleteBoard(@Param("id") Long id);

    void addViewCount(@Param("id") Long id);

    void updateReplyCount(@Param("boardId") Long boardId, @Param("count") int count);
    void updateLikesCount(@Param("boardId") Long boardId, @Param("count") int count);

    int getLikesCount(Long targetId);


    BoardDTO findNoticeById(@Param("id")Long id, @Param("categoryName") String categoryName);


    @Select("SELECT COUNT(*) > 0 FROM board WHERE id = #{boardId}")
    boolean existsById(@Param("boardId") Long boardId);

    @Select("SELECT writer_id FROM board WHERE id = #{id}")
    Long findWriterIdById(@Param("id") Long id);

    List<BoardListDTO> findTop5Notices(@Param("categoryName") String categoryName);

    List<BoardListDTO> findTop10OrderByLikesCountDesc();

    boolean findIsDeleted(@Param("boardId") Long boardId);
}
