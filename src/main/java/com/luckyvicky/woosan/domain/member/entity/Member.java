package com.luckyvicky.woosan.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@DynamicUpdate
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    // 현재 포인트
    @ColumnDefault("0")
    private int point;

    // 다음 레벨까지 필요한 포인트
    @ColumnDefault("100")
    private int nextPoint;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    @Enumerated(EnumType.STRING)
    private MemberType.Level level;

    @ColumnDefault("true")
    private Boolean isActive;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    // level 필드를 제외한 생성자(ADMIN, GUEST 사용)
    public Member(String email, String nickname, String password, MemberType memberType, Boolean isActive, SocialType socialType) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.memberType = memberType;
        this.isActive = isActive;
        this.socialType = socialType;
    }

    // 게시물, 댓글 작성 시 포인트 지급
    public void addPoint(int points){
        if(this.memberType == MemberType.USER) {
            this.point += points;
            updateLevel();
        }
    }

    // 현재 레벨에 따라 다음 레벨까지 가기 위해 필요한 포인트(nextPoint) 계산
    private int calculateNextPoint(MemberType.Level level) {
        switch (level) {
            case LEVEL_1:
                return 100;
            case LEVEL_2:
                return 200;
            case LEVEL_3:
                return 300;
            case LEVEL_4:
                return 400;
            case LEVEL_5:
                return 0; // LEVEL5가 최고 레벨이라면 필요한 포인트는 0
            default:
                return 0;
        }
    }

    // 현재 보유 포인트 + 지급된 포인트에 따라 레벨과 현재 포인트 변경
    public void updateLevel() {

        while (this.point >= this.nextPoint && this.level != MemberType.Level.LEVEL_5) {
            this.point -= this.nextPoint;
            this.level = MemberType.Level.values()[this.level.ordinal() + 1];   // 다음 레벨로
            this.nextPoint = calculateNextPoint(this.level);
        }

        // 최고 레벨에 도달했을 때 nextPoint를 0으로 설정
        if (this.level == MemberType.Level.LEVEL_5) {
            this.nextPoint = 0;
        }
    }

    // 비밀번호 변경
    public void changePassword(String password) {
        this.password = password;
    }

    //닉네임 변경
    public void setNickname(String nickname) { this.nickname = nickname; }

    public Member changeIsActive() {
        this.isActive = false;
        return this;
    }
}
