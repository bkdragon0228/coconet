package com.coconet.articleservice.repository;

import com.coconet.articleservice.entity.MemberEntity;
import com.coconet.articleservice.entity.TechStackEntity;

import java.util.List;

public interface TechStackRepositoryCustom {
    List<TechStackEntity> getMemberStacks(MemberEntity member);
}