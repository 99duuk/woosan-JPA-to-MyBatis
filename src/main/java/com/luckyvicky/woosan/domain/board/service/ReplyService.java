package com.luckyvicky.woosan.domain.board.service;

import com.luckyvicky.woosan.domain.board.dto.RemoveDTO;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import com.luckyvicky.woosan.domain.board.dto.ReplyDTO;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public interface ReplyService {

    void createReply(ReplyDTO.Request replyDTO);

    @Transactional(readOnly = true)
    PageResponseDTO<ReplyDTO.Response> getReplies(Long boardId, PageRequestDTO pageRequestDTO);

    @Transactional
    void deleteReply(RemoveDTO removeDTO);
}
