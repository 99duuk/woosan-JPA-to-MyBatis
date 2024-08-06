package com.luckyvicky.woosan.global.util;


import com.luckyvicky.woosan.domain.board.dto.BoardDTO;
import com.luckyvicky.woosan.domain.board.dto.RemoveDTO;
import com.luckyvicky.woosan.domain.board.dto.ReplyDTO;
import com.luckyvicky.woosan.domain.board.entity.Board;
import com.luckyvicky.woosan.domain.board.entity.Reply;
import com.luckyvicky.woosan.domain.board.exception.BoardException;
import com.luckyvicky.woosan.domain.board.exception.ReplyException;
import com.luckyvicky.woosan.domain.board.mapper.BoardMapper;
import com.luckyvicky.woosan.domain.board.mapper.ReplyMapper;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.board.repository.jpa.ReplyRepository;
import com.luckyvicky.woosan.domain.likes.exception.LikeException;
import com.luckyvicky.woosan.domain.member.entity.Member;
import com.luckyvicky.woosan.domain.member.entity.MemberType;
import com.luckyvicky.woosan.domain.member.repository.jpa.MemberRepository;
import com.luckyvicky.woosan.global.exception.ErrorCode;
import com.luckyvicky.woosan.global.exception.MemberException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import static com.luckyvicky.woosan.global.util.Constants.*;

@Component
@AllArgsConstructor
public class ValidationHelper {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final MemberRepository memberRepository;
    private final ReplyMapper replyMapper;
    private final BoardMapper boardMapper;

    /**
     * BoardDTO 입력값 검증
     */
    public void boardInput(BoardDTO boardDTO) {
        validateBoardTitle(boardDTO.getTitle());
        validateBoardContent(boardDTO.getContent());
        validateBoardCategory(boardDTO.getCategoryName());
    }

    /**
     * 공지사항 입력값 검증
     */
    public void noticeInput(BoardDTO boardDTO) {
        validateBoardTitle(boardDTO.getTitle());
        validateBoardContent(boardDTO.getContent());
        validateNoticeCategory(boardDTO.getCategoryName());
    }

    private void validateBoardTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BoardException(ErrorCode.NULL_OR_BLANK);
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new BoardException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateBoardContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BoardException(ErrorCode.NULL_OR_BLANK);
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BoardException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateBoardCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new BoardException(ErrorCode.NULL_OR_BLANK);
        }
        if (!VALID_CATEGORIES.contains(categoryName)) {
            throw new BoardException(ErrorCode.INVALID_TYPE); // 유효하지 않은 카테고리일 경우
        }
    }

    private void validateNoticeCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new BoardException(ErrorCode.NULL_OR_BLANK);
        }
        if (!VALID_NOTICE.contains(categoryName)) {
            throw new BoardException(ErrorCode.INVALID_TYPE); // 공지사항이 아닌 카테고리일 경우
        }
    }


    /**
     * ReplyDTO 입력값 검증
     */
    public void replyInput(ReplyDTO.Request replyDTO) {
        if ((replyDTO.getBoardId() == null) || (replyDTO.getWriterId() == null) || replyDTO.getContent().isBlank()) {
            throw new ReplyException(ErrorCode.NULL_OR_BLANK);
        }
        if (replyDTO.getContent().length() > MAX_REPLY_CONTENT_LENGTH) {
            throw new ReplyException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }


    /**
     * 사용자, 타입, 타겟 검증
     */
    public void validateLikeInput(Long memberId, String type, Long targetId) {
        if (type == null || type.isEmpty() || (!type.equals("댓글") && !type.equals("게시물"))) {
            throw new LikeException(ErrorCode.INVALID_TYPE);
        }
        if (memberId == null || memberId < 0) {
            throw new MemberException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (targetId == null || targetId < 0) {
            throw new LikeException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }


    /**
     * Board 존재 여부 검증
     */
    public void boardExist(Long boardId) {
        if (!boardMapper.existsById(boardId)) {
            throw new BoardException(ErrorCode.BOARD_NOT_FOUND);
        }
    }

    /**
     * Writer 존재 여부 검증
     */
    public void writerExist(Long writerId) {
        if (writerId == null) {
            throw new MemberException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }


    /**
     * Reply 존재 여부 검증
     */
    public void replyExist(Long id) {
        if (!replyMapper.existsById(id)) {
            throw new ReplyException(ErrorCode.REPLY_NOT_FOUND);
        }
    }


    /**
     * 부모 댓글 존재 여부 검증
     */
    public void parentId(Long parentId) {
        if (!replyRepository.existsById(parentId)) {
            throw new ReplyException(ErrorCode.PARENT_REPLY_NOT_FOUND);
        }
    }


    /**
     * 게시물 소유자 검증
     * 작성자 일치 여부 확인
     */
    public void checkBoardOwnership(Long boardId, Long writerId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));
        if (!board.getWriter().getId().equals(writerId)) {
            throw new BoardException(ErrorCode.ACCESS_DENIED);
        }
    }


    /**
     * Reply 소유자 검증
     */
    public void checkReplyOwnership(Long writerId, RemoveDTO removeDTO) {
        if (!writerId.equals(removeDTO.getWriterId())) {
            throw new ReplyException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 게시물 삭제 여부 확인
     */
    public void checkBoardNotDeleted(Board board) {
        if (board.isDeleted()) {
            throw new BoardException(ErrorCode.BOARD_ALREADY_DELETED);
        }
    }

    /**
     * 관리자 여부 확인
     */
    public void checkAdmin(Long memberId) {
        if (memberId != 1) {
            throw new MemberException(ErrorCode.ACCESS_DENIED);
        }
    }


    /**
     * 게시물 조회
     */
    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));
    }

    /**
     * 작성자 조회
     */
    public Member findWriter(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 작성자 조회 (포인트 추가)
     */
    public Member findWriterAndAddPoints(Long writerId, int point) {
        Member writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
        writer.addPoint(point);
        return writer;
    }

    /**
     * 댓글 조회
     */
    public Reply findReply(Long replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(() -> new ReplyException(ErrorCode.REPLY_NOT_FOUND));
    }

    /**
     * 관리자 여부 검증 및 조회
     */
    public Member checkAndFindAdmin(Long writerId) {
        Member member = findWriter(writerId);

        if (member.getMemberType() != MemberType.ADMIN) {
            throw new MemberException(ErrorCode.ACCESS_DENIED);
        }
        return member;
    }
}