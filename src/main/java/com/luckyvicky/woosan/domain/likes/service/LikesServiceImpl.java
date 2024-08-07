package com.luckyvicky.woosan.domain.likes.service;

import com.luckyvicky.woosan.domain.board.mapper.BoardMapper;
import com.luckyvicky.woosan.domain.board.mapper.ReplyMapper;
import com.luckyvicky.woosan.domain.likes.entity.Likes;
import com.luckyvicky.woosan.domain.likes.exception.LikeException;
import com.luckyvicky.woosan.domain.likes.mapper.LikesMapper;
import com.luckyvicky.woosan.domain.member.mybatisMapper.MemberMyBatisMapper;
import com.luckyvicky.woosan.global.exception.ErrorCode;
import com.luckyvicky.woosan.global.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.luckyvicky.woosan.global.util.Constants.TYPE_BOARD;
import static com.luckyvicky.woosan.global.util.Constants.TYPE_REPLY;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class LikesServiceImpl implements LikesService {

    private final ValidationHelper validationHelper;
    private final LikesMapper likesMapper;
    private final MemberMyBatisMapper memberMyBatisMapper;
    private final BoardMapper boardMapper;
    private final ReplyMapper replyMapper;


    /**
     * 추천 버튼 토글
     * - 추천이 되어 있는 경우: 추천 취소 후 포인트 차감
     * - 추천이 되어 있지 않은 경우: 추천 추가 후 포인트 증가
     */
    @Override
    @Transactional
    public void toggleLike(Long memberId, String type, Long targetId) {
        validationHelper.validateLikeInput(memberId, type, targetId); // 입력값 검증
        Optional<Likes> existingLike = likesMapper.findByMemberIdAndTypeAndTargetId(memberId, type, targetId); //

        if (existingLike.isPresent()) {
            removeLike(existingLike.get(), type, targetId, memberId);
        } else {
            insertLike(type, targetId, memberId);
        }
    }

    /**
     * 이미 추천이 되어 있는 경우, 추천 취소
     */
    public void removeLike(Likes existingLike, String type, Long targetId, Long memberId) {
        likesMapper.deleteLike(existingLike.getId()); // 추천 정보 삭제
        memberMyBatisMapper.updateMemberPoints(memberId, -5); // 포인트 차감
        updateLikeCount(type, targetId, -1); // likesCount 감소
    }

    /**
     * 추천이 되어있지 않은 경우, 추천 추가
     */
    private void insertLike(String type, Long targetId, Long memberId) {
        validationHelper.memberExist(memberId); // 회원 존재 여부 확인
        memberMyBatisMapper.updateMemberPoints(memberId, 5); // 포인트 지급
        likesMapper.insertLike(memberId, type, targetId); // 추천 정보 추가
        updateLikeCount(type, targetId, 1); // likesCount 증가
    }


    /**
     * 좋아요 수를 업데이트
     */
    private void updateLikeCount(String type, Long targetId, int likesCount) {
        if (TYPE_BOARD.equals(type)) {
            boardMapper.updateLikesCount(targetId, likesCount);
        } else if (TYPE_REPLY.equals(type)) {
            replyMapper.updateLikesCount(targetId, likesCount);
        } else {
            throw new LikeException(ErrorCode.INVALID_TYPE); // 유효하지 않은 타입 처리
        }

    }


    /**
     * 추천 여부 확인
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isLiked(Long memberId, String type, Long targetId) {
        validationHelper.validateLikeInput(memberId, type, targetId); // 입력값 검증
        return likesMapper.existsByMemberIdAndTypeAndTargetId(memberId, type, targetId); // 추천 여부 확인
    }


}