package com.luckyvicky.woosan.domain.board.service;

import com.luckyvicky.woosan.domain.board.dto.RemoveDTO;
import com.luckyvicky.woosan.global.annotation.SlaveDBRequest;
import com.luckyvicky.woosan.global.util.ValidationHelper;
import com.luckyvicky.woosan.domain.fileImg.service.FileImgService;
import com.luckyvicky.woosan.global.util.CommonUtils;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import com.luckyvicky.woosan.domain.board.dto.ReplyDTO;
import com.luckyvicky.woosan.domain.board.entity.Board;
import com.luckyvicky.woosan.domain.board.entity.Reply;
import com.luckyvicky.woosan.domain.board.projection.IReply;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.board.repository.jpa.ReplyRepository;
import com.luckyvicky.woosan.domain.member.entity.Member;
import com.luckyvicky.woosan.domain.member.repository.jpa.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final FileImgService fileImgService;
    private final ValidationHelper validationHelper;
    private final CommonUtils commonUtils;


    /**
     * 댓글 작성
     */
    @Override
    @Transactional
    public void createReply(ReplyDTO replyDTO) {
        validationHelper.replyInput(replyDTO); //필수 입력값 검증

        // 게시글과 작성자 조회
        Board board = validationHelper.findBoard(replyDTO.getBoardId());
        Member writer = validationHelper.findWriterAndAddPoints(replyDTO.getWriterId(), 1);

        Reply reply = buildReply(replyDTO, board, writer);


        memberRepository.save(writer);
        board.changeReplyCount(+1);
        boardRepository.save(board);
        replyRepository.save(reply);
    }


    /**
     * 댓글 조회
     */
    @SlaveDBRequest
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ReplyDTO> getReplies(Long boardId, PageRequestDTO pageRequestDTO) {
        validationHelper.boardExist(boardId);

        Pageable pageable = commonUtils.createPageable(pageRequestDTO);
        Page<IReply> parentReplies = replyRepository.findByBoardIdAndParentIdIsNull(boardId, pageable);

        List<ReplyDTO> ReplyDTOs = parentReplies.getContent().stream()
                .map(this::convertToReplyDTOWithChildren)
                .collect(Collectors.toList());

        return commonUtils.createPageResponseDTO(pageRequestDTO, ReplyDTOs, parentReplies.getTotalElements());
    }

    private ReplyDTO convertToReplyDTOWithChildren(IReply iReply) {
        ReplyDTO replyDTO = commonUtils.mapObject(iReply, ReplyDTO.class);
        replyDTO.setWriterProfile(fileImgService.findFiles("member", iReply.getWriter().getId()));

        List<ReplyDTO> childReplyDTOs = replyRepository.findByParentId(iReply.getId()).stream()
                .map(childReply -> {
                    ReplyDTO childReplyDTO = commonUtils.mapObject(childReply, ReplyDTO.class);
                    childReplyDTO.setWriterProfile(fileImgService.findFiles("member", childReply.getWriter().getId()));
                    return childReplyDTO;
                })
                .collect(Collectors.toList());

        replyDTO.setChildren(childReplyDTOs);
        return replyDTO;
    }

    /**
     * 댓글 삭제
     */
    @Override
    @Transactional
    public void deleteReply(RemoveDTO removeDTO) {
        validationHelper.replyExist(removeDTO.getId());

        Reply reply = validationHelper.findReply(removeDTO.getId()); // 부모 댓글 조회
        validationHelper.checkReplyOwnership(reply, removeDTO.getWriterId()); // 작성자 검증

        int childReplyCount = deleteChildReplies(reply.getId()); // 자식 댓글 수 계산 및 삭제

        Board board = reply.getBoard(); 
        board.changeReplyCount(-(childReplyCount + 1));  // 자식 댓글 수와 부모 댓글을 포함한 삭제
        boardRepository.save(board);

        replyRepository.delete(reply);
    }


    private int deleteChildReplies(Long parentId) {
        List<IReply> childReplies = replyRepository.findByParentId(parentId);
        int count = 0;

        for (IReply child : childReplies) {
            count += deleteChildReplies(child.getId()) + 1;
            replyRepository.deleteById(child.getId());
        }

        return count;
    }



    private Reply buildReply(ReplyDTO replyDTO, Board board, Member writer) {
        Reply.ReplyBuilder replyBuilder = Reply.builder()
                .board(board)
                .writer(writer)
                .content(replyDTO.getContent());

        // 부모 댓글 있는 경우
        if (replyDTO.getParentId() != null) {
            validationHelper.parentId(replyDTO.getParentId());
            replyBuilder.parentId(replyDTO.getParentId());
        }

        return replyBuilder.build();
    }


}
