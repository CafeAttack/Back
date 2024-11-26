package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Dto.response.CafeRecordPageResponseDTO;
import com.cafeattack.springboot.Domain.Dto.response.CafeRecordsDTO;
import com.cafeattack.springboot.Domain.Dto.response.RecordOrderPageResponseDTO;
import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Domain.Entity.Records;
import com.cafeattack.springboot.Repository.*;
import com.cafeattack.springboot.common.BaseResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final CafeRepository cafeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final GroupCafePKRepository groupCafePKRepository;

    @Transactional
    public ResponseEntity getDateorderPage(int memberId) {
        Member member = memberRepository.findById(memberId).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        List<RecordOrderPageResponseDTO> cafes = getPageDTO(member);
        cafes.sort((dto1, dto2) -> dto2.getLatestvisit().compareTo(dto1.getLatestvisit()));

        return ResponseEntity.status(200).body(new BaseResponse(200, "기록장이 열렸습니다.", cafes));
    }

    @Transactional
    public ResponseEntity getNameorderPage(int memberId) {
        Member member = memberRepository.findById(memberId).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        List<RecordOrderPageResponseDTO> cafes = getPageDTO(member);
        cafes.sort((dto1, dto2) -> dto2.getCafename().compareTo(dto1.getCafename()));

        return ResponseEntity.status(200).body(new BaseResponse(200, "기록장이 열렸습니다.", cafes));
    }

    @Transactional
    public ResponseEntity getFreqorderPage(int memberId) {
        Member member = memberRepository.findById(memberId).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        List<RecordOrderPageResponseDTO> cafes = getPageDTO(member);
        cafes.sort((dto1, dto2) -> dto2.getVisitcount().compareTo(dto1.getVisitcount()));

        return ResponseEntity.status(200).body(new BaseResponse(200, "기록장이 열렸습니다.", cafes));
    }

    private List<RecordOrderPageResponseDTO> getPageDTO(Member member) {
        List<Cafe> cafeList = recordRepository.findAllCafeByMember(member);
        List<RecordOrderPageResponseDTO> cafes = new ArrayList<>();
        for(Cafe cafe : cafeList) {
            int visited = recordRepository.getVisitCount(member, cafe.getCafeId());
            Date latest = recordRepository.getLatestDate(member, cafe);

            RecordOrderPageResponseDTO tempDto = RecordOrderPageResponseDTO.builder()
                    .cafeId(cafe.getCafeId())
                    .cafename(cafe.getCafeName())
                    .visitcount(visited)
                    .latestvisit(latest).build();
            cafes.add(tempDto);
        }
        return cafes;
    }

    @Transactional
    public ResponseEntity getCafePage(int memberId, int cafeId) {
        Member member = memberRepository.findById(memberId).get();
        if (member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        Cafe cafe = cafeRepository.getCafeByCafeid(cafeId);
        if(cafe == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 Cafe가 없습니다."));

        int heart = 0;
        if(groupCafePKRepository.findGroupCafePKByMemberIdandCafe(memberId, cafe) != null) heart = 1;
        CafeRecordPageResponseDTO pageDTO = new CafeRecordPageResponseDTO().builder()
                .visitcount(recordRepository.getVisitCount(member, cafeId))
                .heart(heart).build();

        List<Records> recordsList = recordRepository.findAllByMember(member);
        List<CafeRecordsDTO> records = new ArrayList<>();
        for(int i = 0; i < recordsList.size(); i++) {
            CafeRecordsDTO temp = CafeRecordsDTO.builder()
                    .date(recordsList.get(i).getRecordDate())
                    .text(recordsList.get(i).getRecordText())
                    .build();
            records.add(temp);
        }
        pageDTO.setRecords(records);

        return ResponseEntity.status(200).body(new BaseResponse(200, "카페별 기록 화면입니다.", pageDTO));
    }
}
