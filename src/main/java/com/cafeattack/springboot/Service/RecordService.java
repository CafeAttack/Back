package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Dto.response.RecordDateOrderPageResponseDTO;
import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Repository.MemberRepository;
import com.cafeattack.springboot.Repository.RecordRepository;
import com.cafeattack.springboot.common.BaseResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ResponseEntity getDateorderPage(int memberId) {
        Member member = memberRepository.findById(memberId).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        List<Cafe> cafeList = recordRepository.findAllCafeByMember(member);
        List<RecordDateOrderPageResponseDTO> cafes = new ArrayList<>();
        for(Cafe cafe : cafeList) {
            int visited = recordRepository.getVisitCount(member, cafe.getCafeId());
            Date latest = recordRepository.getLatestDate(member, cafe);

            RecordDateOrderPageResponseDTO tempDto = RecordDateOrderPageResponseDTO.builder()
                    .cafename(cafe.getCafeName())
                    .visitcount(visited)
                    .latestvisit(latest).build();
            cafes.add(tempDto);
        }

        return ResponseEntity.status(200).body(new BaseResponse(200, "기록장이 열렸습니다.", cafes));
    }
}
