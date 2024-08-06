package com.luckyvicky.woosan.domain.member.mybatisMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMyBatisMapper {
    void updateMemberPoints(@Param("memberId") Long memberId, @Param("point") int point);
}
