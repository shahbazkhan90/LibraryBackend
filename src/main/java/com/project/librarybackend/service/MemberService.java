package com.project.librarybackend.service;

import com.project.librarybackend.model.Member;
import com.project.librarybackend.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member addMember(Member member) {
        member.setMembershipDate(LocalDateTime.now());
        return memberRepository.save(member);
    }
}
