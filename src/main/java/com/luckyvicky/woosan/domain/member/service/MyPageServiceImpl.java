package com.luckyvicky.woosan.domain.member.service;

import com.luckyvicky.woosan.domain.board.dto.BoardDTO;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.board.repository.jpa.ReplyRepository;
import com.luckyvicky.woosan.domain.member.mybatisMapper.MyPageMapper;
import com.luckyvicky.woosan.global.annotation.SlaveDBRequest;
import com.luckyvicky.woosan.global.util.CommonUtils;
import com.luckyvicky.woosan.global.util.ValidationHelper;
import com.luckyvicky.woosan.domain.member.dto.MyBoardDTO;
import com.luckyvicky.woosan.domain.member.dto.MyPageDTO;
import com.luckyvicky.woosan.domain.member.dto.MyReplyDTO;
import com.luckyvicky.woosan.domain.member.entity.Member;
import com.luckyvicky.woosan.domain.member.repository.jpa.MemberRepository;
import com.luckyvicky.woosan.domain.messages.dto.MessageDTO;
import com.luckyvicky.woosan.domain.messages.entity.Message;
import com.luckyvicky.woosan.domain.messages.exception.MessageException;
import com.luckyvicky.woosan.domain.messages.mapper.MessageMapper;
import com.luckyvicky.woosan.domain.messages.repository.MessageRepository;
import com.luckyvicky.woosan.global.exception.ErrorCode;
import com.luckyvicky.woosan.global.exception.MemberException;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class MyPageServiceImpl implements MyPageService {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final MemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ModelMapper modelMapper;
    private final ValidationHelper validationHelper;
    private final CommonUtils commonUtils;
    private final MyPageMapper myPageMapper;

    @Override
    @Transactional
    public PageResponseDTO<MyBoardDTO> getMyBoard(MyPageDTO myPageDTO) {
        Long memberId = getMemberId(myPageDTO);
        PageRequestDTO pageRequestDTO = getPageRequestDTO(myPageDTO);
        pageRequestDTO.validate();

        int offset = (pageRequestDTO.getPage() - 1) * pageRequestDTO.getSize();
        int pageSize = pageRequestDTO.getSize();

        List<MyBoardDTO> myBoardDTOs = myPageMapper.findMyBoards(memberId, offset, pageSize);

        long totalCount = myPageMapper.findMyBoardsTotalCount(memberId);

        return PageResponseDTO.<MyBoardDTO>withAll()
                .dtoList(myBoardDTOs)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(totalCount)
                .build();
    }

    @SlaveDBRequest
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<MyReplyDTO> getMyReply(MyPageDTO myPageDTO) {
        Long memberId = getMemberId(myPageDTO);
        PageRequestDTO pageRequestDTO = getPageRequestDTO(myPageDTO);

        pageRequestDTO.validate();

        int offset = (pageRequestDTO.getPage() - 1) * pageRequestDTO.getSize();
        int pageSize = pageRequestDTO.getSize();

        List<MyReplyDTO> myReplyDTOs = myPageMapper.findMyReplies(memberId, offset, pageSize);

        long totalCount = myPageMapper.findMyRepliesTotalCount(memberId);

        return PageResponseDTO.<MyReplyDTO>withAll()
                .dtoList(myReplyDTOs)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(totalCount)
                .build();
    }

    @SlaveDBRequest
    @Override
    public PageResponseDTO<MyBoardDTO> myLikeBoardList(MyPageDTO myPageDTO) {
        Long memberId = getMemberId(myPageDTO);
        PageRequestDTO pageRequestDTO = getPageRequestDTO(myPageDTO);
        pageRequestDTO.validate();

        int offset = (pageRequestDTO.getPage() - 1) * pageRequestDTO.getSize();
        int pageSize = pageRequestDTO.getSize();

        List<MyBoardDTO> myBoardDTOs = myPageMapper.findLikedBoards(memberId, offset, pageSize);

        long totalCount = myPageMapper.findLikedBoardsTotalCount(memberId);

        return PageResponseDTO.<MyBoardDTO>withAll()
                .dtoList(myBoardDTOs)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(totalCount)
                .build();
    }


    @SlaveDBRequest
    @Override
    public PageResponseDTO<MessageDTO> mySendMessages(MyPageDTO myPageDTO) {
        Long memberId = getMemberId(myPageDTO);
        PageRequestDTO pageRequestDTO = getPageRequestDTO(myPageDTO);
        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(), Sort.by("id").descending());

        Member sender = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        Page<Message> result = messageRepository.findBySenderAndDelBySender(sender, pageable, false);

        List<MessageDTO> dtoList = result.getContent().stream()
                .map(message -> modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());

        return createPageResponseDTO(dtoList, pageRequestDTO, result.getTotalElements());
    }

    @SlaveDBRequest
    @Override
    public PageResponseDTO<MessageDTO> myReceiveMessages(MyPageDTO myPageDTO) {
        Long memberId = getMemberId(myPageDTO);
        PageRequestDTO pageRequestDTO = getPageRequestDTO(myPageDTO);
        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(), Sort.by("id").descending());

        Member receiver = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        Page<Message> result = messageRepository.findByReceiverAndDelByReceiver(receiver, pageable, false);

        List<MessageDTO> dtoList = result.getContent().stream()
                .map(message -> modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());

        return createPageResponseDTO(dtoList, pageRequestDTO, result.getTotalElements());
    }

    @Override
    public String removeSendMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageException(ErrorCode.MESSAGE_NOT_FOUND));

        if (message.getDelBySender() == true) {
            throw new MessageException(ErrorCode.MESSAGE_ALREADY_DELETED);
        }

        message.changeIsDelBySender();
        messageRepository.save(message);

        return "보낸 메시지 삭제 완료";
    }

    @Override
    public String removeReceiveMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageException(ErrorCode.MESSAGE_NOT_FOUND));

        if (message.getDelByReceiver() == true) {
            throw new MessageException(ErrorCode.MESSAGE_ALREADY_DELETED);
        }

        message.changeIsDelByReceiver();
        messageRepository.save(message);

        return "받은 메시지 삭제 완료";
    }

    @SlaveDBRequest
    @Override
    public MessageDTO getMyMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageException(ErrorCode.MESSAGE_NOT_FOUND));

        MessageDTO messageDTO = messageMapper.messageToMessageDTO(message);

        Member receiver = memberRepository.findById(message.getReceiver().getId())
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        Member sender = memberRepository.findById(message.getSender().getId())
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        messageDTO.setReceiverNickname(receiver.getNickname());
        messageDTO.setSenderNickname(sender.getNickname());

        return messageDTO;
    }

    // 공통 메소드 ================================================================================

    //memberId 추출 메소드
    private Long getMemberId(MyPageDTO myPageDTO) {
        return myPageDTO.getMemberId();
    }

    //pageRequestDTO 추출 메소드
    private PageRequestDTO getPageRequestDTO(MyPageDTO myPageDTO) {
        return myPageDTO.getPageRequestDTO() != null ? myPageDTO.getPageRequestDTO() : new PageRequestDTO();
    }

    //페이지네이션 응답 반환 메소드
    private <T> PageResponseDTO<T> createPageResponseDTO(List<T> dtoList, PageRequestDTO pageRequestDTO, long totalCount) {
        return PageResponseDTO.<T>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(totalCount)
                .build();
    }
}