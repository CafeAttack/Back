package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Dto.request.*;
import com.cafeattack.springboot.Domain.Dto.response.*;
import com.cafeattack.springboot.Domain.Entity.Bookmark;
import com.cafeattack.springboot.Domain.Entity.mapping.CafeCategoryPK;
import com.cafeattack.springboot.Domain.Entity.mapping.GroupCafePK;
import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Repository.*;
import com.cafeattack.springboot.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final CafeRepository cafeRepository;
    private final CafeCategoryPKRepository cafeCategoryPKRepository;
    private final GroupCafePKRepository groupCafePKRepository;

    @Transactional
    public ResponseEntity bookmark_Page (Integer member_id) {
        Member member = memberRepository.findById(member_id).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        List<bookmarkPageGroupResponseDto> groups = new ArrayList<bookmarkPageGroupResponseDto>();
        List<Integer> allgroupId = groupCafePKRepository.findAllgroupIdBymemberId(member_id);
        for(int i = 0; i < allgroupId.size(); i++) {
            String groupName = bookmarkRepository.getGroupNameByGroupid(allgroupId.get(i));
            List<Integer> allcafeId = groupCafePKRepository.findAllcafeByGroupid(allgroupId.get(i));
            List<bookmarkPageCafeResponseDto> cafes = new ArrayList<bookmarkPageCafeResponseDto>();
            for(int j = 0; j < allcafeId.size(); j++) {
                String cafeName = cafeRepository.getCafeNameByCafeid(allcafeId.get(j));
                List<Integer> allcategoryId = cafeCategoryPKRepository.findAllCategory(allcafeId.get(j));
                List<bookmarkPageCategoryResposeDto> categories = new ArrayList<bookmarkPageCategoryResposeDto>();
                for(int k = 0; k < allcategoryId.size(); k++) {
                    bookmarkPageCategoryResposeDto Tmp = bookmarkPageCategoryResposeDto.builder()
                            .categoryId(allcategoryId.get(k)).build();
                    categories.add(Tmp);
                }
                bookmarkPageCafeResponseDto tmp = bookmarkPageCafeResponseDto.builder()
                        .cafeId(allcafeId.get(j))
                        .cafeName(cafeName)
                        .categories(categories).build();
                cafes.add(tmp);
            }
            bookmarkPageGroupResponseDto tmp = bookmarkPageGroupResponseDto.builder()
                    .groupId(allgroupId.get(i))
                    .groupName(groupName)
                    .cafes(cafes).build();
            groups.add(tmp);
        }

        bookmarkPageResponseDto BookmarkPageResponseDto = bookmarkPageResponseDto.builder()
                .groups(groups).build();

        return ResponseEntity.status(200).body(new BaseResponse(200, "즐겨찾기가 열렸습니다.", BookmarkPageResponseDto));
    }

    @Transactional
    public ResponseEntity getPageforEdit(Integer member_id, Integer cafeId) {
        Member member = memberRepository.findById(member_id).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        bookmarkPageforEditDto BookmarkPageforEditDto = new bookmarkPageforEditDto();
        BookmarkPageforEditDto.setGroups(new ArrayList<>());
        List<Integer> allgroupId = groupCafePKRepository.findAllgroupIdBymemberId(member_id);
        for(int i = 0; i < allgroupId.size(); i++) {
            bookmarkPageforEditGroupDto group = new bookmarkPageforEditGroupDto();

            group.setGroupId(allgroupId.get(i));
            group.setGroupName(bookmarkRepository.getGroupNameByGroupid(allgroupId.get(i)));
            boolean checked = false;
            List<Integer> allcafeId = groupCafePKRepository.findAllcafeByGroupid(allgroupId.get(i));
            for(int j = 0; j < allcafeId.size(); j++) {
                if(allcafeId.get(j) == cafeId) {
                    checked = true;
                }
            }
            group.setChecked(checked);
            BookmarkPageforEditDto.groups.add(group);
        }

        return ResponseEntity.status(200).body(new BaseResponse(200, "즐겨찾기 추가 화면입니다.", BookmarkPageforEditDto));
    }

    @Transactional
    public ResponseEntity addBookmark(Integer member_id, addbookmarkDto AddBookmarkDto) {
        Member member = memberRepository.findById(member_id).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        for(int j = 0; j < AddBookmarkDto.groups.size(); j++) {
            boolean isChecked = false;
            if(AddBookmarkDto.groups.get(j).isChecked()) {
                for(int k = 0; k < bookmarkRepository.countByGroupid(AddBookmarkDto.groups.get(j).getGroupId()); k++) {
                    if(AddBookmarkDto.getCafeId() == groupCafePKRepository.findAllcafeidByGroupid(AddBookmarkDto.groups.get(j).getGroupId()).get(k)) {
                        isChecked = true;
                    }
                }
                if(!isChecked) {
                    GroupCafePK relation = new GroupCafePK();
                    relation.setId(AddBookmarkDto.groups.get(j).getGroupId());
                    relation.setCafe(cafeRepository.getCafeByCafeid(AddBookmarkDto.getCafeId()).get());
                    Bookmark bookmark = Bookmark.builder()
                            .memberId(member_id)
                            .groupName(bookmarkRepository.getGroupNameByGroupid(AddBookmarkDto.groups.get(j).getGroupId()))
                            .build();
                    bookmarkRepository.save(bookmark);
                }
            }
            else {
                isChecked = true;
                for(int k = 0; k < bookmarkRepository.countByGroupid(AddBookmarkDto.groups.get(j).getGroupId()); k++) {
                    if(AddBookmarkDto.getCafeId() == groupCafePKRepository.findAllcafeidByGroupid(AddBookmarkDto.groups.get(j).getGroupId()).get(k)) {
                        isChecked = false;
                    }
                }
                if(!isChecked) {
                    GroupCafePK groupCafePK = groupCafePKRepository.findByRelation(AddBookmarkDto.groups.get(j).getGroupId(), AddBookmarkDto.getCafeId());

                    groupCafePKRepository.delete(groupCafePK);
                }
            }
        }

        return ResponseEntity.status(200).body(new BaseResponse(200, "추가가 완료되었습니다."));
    }

    @Transactional
    public ResponseEntity addGroup(Integer member_id, addGroupDto AddGroupDto) {
        Member member = memberRepository.findById(member_id).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        Integer newGroupId = bookmarkRepository.getMaxGroupid() + 1;
        Bookmark bookmark = Bookmark.builder()
                .groupId(newGroupId)
                .groupName(AddGroupDto.getGroupName())
                .memberId(member_id).build();
        bookmarkRepository.save(bookmark);

        return ResponseEntity.status(200).body(new BaseResponse(200, "그룹이 추가되었습니다.", newGroupId));
    }

    @Transactional
    public ResponseEntity deleteGroup(Integer member_id, deleteGroupDto DeleteGroupDto) {
        Member member = memberRepository.findById(member_id).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 유저가 없습니다."));

        List<Integer> cafes = groupCafePKRepository.findAllcafeByGroupid(DeleteGroupDto.getGroupId());
        if(cafes.size() != 1)
            return ResponseEntity.status(401).body(new BaseResponse(401, "오류가 발생하였습니다."));
        else {
            Bookmark bookmark = Bookmark.builder()
                    .groupId(DeleteGroupDto.getGroupId())
                    .memberId(member_id)
                    .groupName(bookmarkRepository.getGroupNameByGroupid(DeleteGroupDto.getGroupId())).build();
            bookmarkRepository.delete(bookmark);

            return ResponseEntity.status(200).body(new BaseResponse(200, "제거가 완료되었습니다."));
        }
    }
}
