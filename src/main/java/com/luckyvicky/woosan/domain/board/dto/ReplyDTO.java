package com.luckyvicky.woosan.domain.board.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ReplyDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long boardId;  // 게시글 고유번호
        private String content;  // 내용
        private Long writerId;  // 작성자 고유번호
        private Long parentId;  // 부모 댓글의 고유번호
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long id;  // 댓글 고유번호
        private Long boardId;  // 게시글 고유번호
        private String content;  // 내용
        private int likesCount; // 추천수
        private Long writerId;  // 작성자 고유번호
        private String nickname;    //작성자 닉네임
        private List<String> writerProfile;
        private LocalDateTime regDate;  // 작성 날짜
        private Long parentId;  // 부모 댓글의 고유번호
        private List<Response> children;  // 자식 댓글 리스트
    }
}
