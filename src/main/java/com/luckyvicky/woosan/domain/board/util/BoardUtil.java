package com.luckyvicky.woosan.domain.board.util;

import com.luckyvicky.woosan.domain.board.exception.BoardException;
import com.luckyvicky.woosan.domain.board.mapper.BoardMapper;
import com.luckyvicky.woosan.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BoardUtil {
    private final BoardMapper boardMapper;
    /**
     * 게시물 조회수 증가
     */
    public void increaseViewCount(Long id) {
        if(!boardMapper.existsById(id)){
            throw new BoardException(ErrorCode.BOARD_NOT_FOUND);
        };
        boardMapper.addViewCount(id);
    }

}
