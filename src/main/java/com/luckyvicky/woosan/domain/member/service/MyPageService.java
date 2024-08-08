package com.luckyvicky.woosan.domain.member.service;


import com.luckyvicky.woosan.domain.board.dto.BoardDTO;
import com.luckyvicky.woosan.domain.member.dto.MyReplyDTO;
import com.luckyvicky.woosan.domain.member.dto.MyBoardDTO;
import com.luckyvicky.woosan.domain.member.dto.MyPageDTO;
import com.luckyvicky.woosan.domain.messages.dto.MessageDTO;
import com.luckyvicky.woosan.domain.messages.entity.Message;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import org.springframework.transaction.annotation.Transactional;


public interface MyPageService {

    @Transactional(readOnly = true)
    PageResponseDTO<MyBoardDTO> myLikeBoardList(MyPageDTO myPageDTO);

    @Transactional(readOnly = true)
    PageResponseDTO<MyReplyDTO> getMyReply(MyPageDTO myPageDTO);

    @Transactional(readOnly = true)
    PageResponseDTO<MyBoardDTO> getMyBoard(MyPageDTO myPageDTO);

    @Transactional(readOnly = true)
    PageResponseDTO<MessageDTO> mySendMessages(MyPageDTO myPageDTO);

    @Transactional(readOnly = true)
    PageResponseDTO<MessageDTO> myReceiveMessages(MyPageDTO myPageDTO);

    @Transactional
    String removeSendMessage(Long id);

    @Transactional
    String removeReceiveMessage(Long id);

    @Transactional(readOnly = true)
    MessageDTO getMyMessage(Long id);
}
