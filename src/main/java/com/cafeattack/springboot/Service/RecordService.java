package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Dto.request.NewlyRecordRequestDTO;
import com.cafeattack.springboot.Domain.Dto.response.*;
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

        Cafe cafe = cafeRepository.getCafeByCafeid(cafeId).get();
        if(cafe == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 Cafe가 없습니다."));

        int heart = 0;
        if(groupCafePKRepository.findByMemberCafe(memberId, cafe) != null) heart = 1;
        CafeRecordPageResponseDTO pageDTO = new CafeRecordPageResponseDTO().builder()
                .visitcount(recordRepository.getVisitCount(member, cafeId))
                .heart(heart).build();

        List<Records> recordsList = recordRepository.findAllByMember(member);
        List<CafeRecordsDTO> records = new ArrayList<>();
        for(int i = 0; i < recordsList.size(); i++) {
            CafeRecordsDTO temp = CafeRecordsDTO.builder()
                    .id(recordsList.get(i).getRecordId())
                    .date(recordsList.get(i).getRecordDate())
                    .text(recordsList.get(i).getRecordText())
                    .build();
            records.add(temp);
        }
        pageDTO.setRecords(records);

        return ResponseEntity.status(200).body(new BaseResponse(200, "카페별 기록 화면입니다.", pageDTO));
    }

    @Transactional
    public ResponseEntity getEnrollPage(int memberId, int cafeId) {
        Member member = memberRepository.findById(memberId).get();
        if (member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        Cafe cafe = cafeRepository.getCafeByCafeid(cafeId).get();
        if(cafe == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 Cafe가 없습니다."));

        int CurrentCount = 0;
        CurrentCount = recordRepository.getVisitCount(member, cafe.getCafeId());

        EnrollPageDTO enrollPageDTO = EnrollPageDTO.builder()
                .newCount(CurrentCount + 1)
                .build();

        return ResponseEntity.status(200).body(new BaseResponse(200, "새로 기록할 수 있습니다.", enrollPageDTO));
    }

    @Transactional
    public ResponseEntity newlyRecord(int memberId, int cafeId, NewlyRecordRequestDTO request) {
        Member member = memberRepository.findById(memberId).get();
        if (member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        Cafe cafe = cafeRepository.getCafeByCafeid(cafeId).get();
        if(cafe == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 Cafe가 없습니다."));

        Records newRecord = Records.builder()
                .recordDate(request.getRecorddate())
                .recordText(request.getRecordtext())
                .cafe(cafe)
                .member(member)
                .build();
        newRecord = recordRepository.save(newRecord);

        NewlyRecordResponseDTO data = NewlyRecordResponseDTO.builder()
                .recordId(newRecord.getRecordId()).build();

        return ResponseEntity.status(200).body(new BaseResponse(200, "새로 기록하였습니다.", data));
    }

    @Transactional
    public ResponseEntity getEditPage(int memberId, int cafeId, int recordId) {
        Member member = memberRepository.findById(memberId).get();
        if (member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        Cafe cafe = cafeRepository.getCafeByCafeid(cafeId).get();
        if(cafe == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 Cafe가 없습니다."));

        Records records = recordRepository.findById(recordId).get();
        if(records == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 Record가 없습니다."));

        int recordCount = recordRepository.getVisitCount(member, cafe.getCafeId());

        EditRecordsResponseDTO responseDTO = EditRecordsResponseDTO.builder()
                .recordcount(recordCount)
                .recorddate(records.getRecordDate())
                .recordtext(records.getRecordText()).build();

        return ResponseEntity.status(200).body(new BaseResponse(200, "수정할 수 있습니다.", responseDTO));
    }

    @Transactional
    public ResponseEntity editPage(int memberId, int cafeId, int recordId, EditRecordsResponseDTO request) {
        Member member = memberRepository.findById(memberId).get();
        if (member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        Cafe cafe = cafeRepository.getCafeByCafeid(cafeId).get();
        if(cafe == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 Cafe가 없습니다."));

        Records records = recordRepository.findById(recordId).get();

        records.setRecordDate(request.getRecorddate());
        records.setRecordText(request.getRecordtext());
        recordRepository.save(records);

        return ResponseEntity.status(200).body(new BaseResponse(200, "기록이 수정되었습니다."));
    }
}
