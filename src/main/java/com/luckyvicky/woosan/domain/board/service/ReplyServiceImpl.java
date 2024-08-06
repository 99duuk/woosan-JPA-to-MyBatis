package com.luckyvicky.woosan.domain.board.service;

import com.luckyvicky.woosan.domain.board.dto.RemoveDTO;
import com.luckyvicky.woosan.domain.board.dto.ReplyDTO;
import com.luckyvicky.woosan.domain.board.mapper.BoardMapper;
import com.luckyvicky.woosan.domain.board.mapper.ReplyMapper;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.board.repository.jpa.ReplyRepository;
import com.luckyvicky.woosan.domain.fileImg.service.FileImgService;
import com.luckyvicky.woosan.domain.member.mapper.MemberMapper;
import com.luckyvicky.woosan.domain.member.mybatisMapper.MemberMyBatisMapper;
import com.luckyvicky.woosan.domain.member.repository.jpa.MemberRepository;
import com.luckyvicky.woosan.global.util.CommonUtils;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import com.luckyvicky.woosan.global.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService {

    private final ReplyRepository replyRepository;
    private final ReplyMapper replyMapper;
    private final MemberMapper memberMapper;

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final FileImgService fileImgService;
    private final ValidationHelper validationHelper;
    private final CommonUtils commonUtils;
    private final MemberMyBatisMapper memberMyBatisMapper;
    private final BoardMapper boardMapper;
    private final ModelMapper modelMapper;


    /**
     * 댓글 작성
     */
    @Override
    @Transactional
    public void createReply(ReplyDTO.Request replyDTO) {
        validationHelper.replyInput(replyDTO); //필수 입력값 검증

        memberMyBatisMapper.updateMemberPoints(replyDTO.getWriterId(), 1); // 작성자 포인트 증가
        boardMapper.updateReplyCount(replyDTO.getBoardId(), 1);  // 게시글의 댓글 수 증가
        replyMapper.insertReply(replyDTO); // 댓글(답글) 추가
    }


    /**
     * 댓글 조회
     */
//    @SlaveDBRequest
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ReplyDTO.Response> getReplies(Long boardId, PageRequestDTO pageRequestDTO) {
        validationHelper.boardExist(boardId); // 게시물 존재 여부 확인

        Pageable pageable = commonUtils.createPageable(pageRequestDTO);
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();
        List<ReplyDTO.Response> parentReplies = replyMapper.findRepliesByBoardId(boardId, offset, pageSize); // 부모 댓글 조회

        // 부모 댓글 각각에 대해 자식 댓글을 포함한 ReplyDTO.Response 객체로 변환
        List<ReplyDTO.Response> replyDTOs = parentReplies.stream()
                .map(this::convertToReplyDTOWithChildren)
                .collect(Collectors.toList());

        // 총 댓글 수 조회
        int totalCount = replyMapper.countRepliesByBoardId(boardId);

        // PageResponseDTO를 생성하여 반환 (페이지 정보 포함)
        return new PageResponseDTO<>(replyDTOs, pageRequestDTO, totalCount);
    }

    // 주어진 부모 댓글에 대해 자식 댓글을 포함한 ReplyDTO.Response 객체로 변환
    private ReplyDTO.Response convertToReplyDTOWithChildren(ReplyDTO.Response replyDTO) {
        ReplyDTO.Response responseDTO = modelMapper.map(replyDTO, ReplyDTO.Response.class); // replyDTO를 ResponseDTO로 변환

        // 작성자의 프로필 이미지 설정
        responseDTO.setWriterProfile(fileImgService.findFiles("member", replyDTO.getWriterId()));

        // 주어진 부모 댓글 ID에 대한 자식 댓글 조회
        List<ReplyDTO.Response> childReplies = replyMapper.findRepliesByParentId(replyDTO.getId());
        List<ReplyDTO.Response> childReplyDTOs = childReplies.stream()
                .map(this::convertToReplyDTOWithChildren)
                .collect(Collectors.toList());

        // 자식 댓글 리스트를 부모 댓글의 children 필드에 설정
        responseDTO.setChildren(childReplyDTOs);
        return responseDTO;
    }



    /**
     * 댓글 삭제
     */
    @Override
    @Transactional
    public void deleteReply(RemoveDTO removeDTO) {
        validationHelper.replyExist(removeDTO.getId()); // 댓글 존재 여부 확인

        Long writerId = replyMapper.findWriterIdById(removeDTO.getId());
        validationHelper.checkReplyOwnership(writerId, removeDTO); // 작성자 검증

        Long boardId = replyMapper.findBoardIdById(removeDTO.getId()); // removeDTO에 boardId가 없기 때문에 조회 (FE)


        int childReplyCount = replyMapper.deleteByParentId(removeDTO.getId());  // 자식 댓글 삭제

        int parentReplyCount = replyMapper.deleteById(removeDTO.getId()); // 부모 댓글 삭제


        int totalDeleted = childReplyCount + parentReplyCount;
        boardMapper.updateReplyCount(boardId, -totalDeleted); // 게시글 댓글 수 감소
    }


}
