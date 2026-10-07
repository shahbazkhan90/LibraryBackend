package com.project.librarybackend.repository;

import com.project.librarybackend.model.BorrowingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord,Integer> {
    Optional<BorrowingRecord> findFirstByBookRelation_BookIdAndMemberRelation_MemberIdAndReturnDateIsNull(Integer bookId, Integer memberId);

    List<BorrowingRecord>  findByMemberRelation_MemberId(Integer memberId);
}
