package com.daangn.clone.item.service;

import com.daangn.clone.category.Category;
import com.daangn.clone.category.repository.CategoryRepository;
import com.daangn.clone.common.enums.DelYn;
import com.daangn.clone.common.enums.SaleSituation;
import com.daangn.clone.common.response.ApiException;
import com.daangn.clone.common.response.ApiResponseStatus;
import com.daangn.clone.encryption.AES128;
import com.daangn.clone.file.FileServiceUtil;
import com.daangn.clone.item.Item;
import com.daangn.clone.item.dto.*;
import com.daangn.clone.item.dto.paging.ItemSummaryDto;
import com.daangn.clone.item.dto.paging.SortCriteria;
import com.daangn.clone.item.repository.ItemRepository;
import com.daangn.clone.itemimage.ItemImage;
import com.daangn.clone.itemimage.repository.ItemImageRepository;
import com.daangn.clone.member.Member;
import com.daangn.clone.member.repository.MemberRepository;
import com.daangn.clone.town.repository.TownRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.daangn.clone.common.response.ApiResponseStatus.FAIL_GET_ITEM_LIST;
import static com.daangn.clone.item.QItem.item;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService{

    @Value("${file.dir}")
    private String fileDir;

    @Value("${sample.dir}")
    private String sampleDir;



    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final TownRepository townRepository;

    private final FileServiceUtil fileServiceUtil;
    private final AES128 aes128;




    /** 암호화/복호화를 위한 private 서비스 */


    /**
     * [조회]
     * */

    /** <특정 아이템 조회> */
    @Transactional
    public ItemDto getItem(Long id) {


        //0. itemId 유효성 검사와 동시에 , Item 엔티티 조회
        Item item = itemRepository.findItemById(id).orElseThrow(
                () -> {
                    throw new ApiException(ApiResponseStatus.FAIL_GET_ITEM, "itemId : " + id + " 의 유효하지 않은 itemId 값으로 인해 특정 Item 조회에 실패했습니다.");
                }
        );

        //1. 해당 Item 엔티티의 조회수 필드값 업데이트 후 (단 동시성 문제 남아있음) -> 해당 Item 정보를 가공한 ItemDto 반환
        item.increaseVisitCount();

        //2. 해당 상품과 관련된 상품 이비지의 리스트를 "AES128 암호화 후 URL 인코딩" 시킴
        // 단 이때 상품 이미지가 저장된 로컬 경로는 , AES128로 암호화 한후 -> URL에서 인삭하지 못하는 특수문자들을 한번 더 인코딩한 후 -> 보낸다.
        // -> 예를들어 +라는 문자를 %2B로 인코딩 해서 보내야 - 그 %2B 가 url로써 네트워크를 타고 넘어왔을 때 + 로 바뀐다 (아마 이런게 URL인코딩의미) (그런데도 만약 여기서 이미 +로 변한 결과를 또 URL 디코딩 하면 문제!)

        List<String> encrpytedPathList = fileServiceUtil.getEncryptedPathList(item, sampleDir, aes128);

        return ItemDto.builder()
                .itemeId(item.getId())
                .title(item.getTitle())
                .content(item.getContent())
                .price(item.getPrice())
                .visitCount(item.getVisitCount())
                .categoryName(item.getCategory().getName())
                .townName(item.getTown().getName())
                .sellerMemberName(item.getSellerMember().getNickname())
                .createdAt(item.getCreatedAt())
                .delYn(item.getDelYn())
                .saleSituation(item.getSalesituation())
                .numOfWish(item.getWishList().size())
                .numOfChattingRoom(item.getChattingRoomList().size())
                .itemImagePathList(encrpytedPathList)
                .build();

    }



    /** <아이템 목록 조회> */
    public List<ItemSummaryDto> getItemList(String username,
                                            int page, int limit,
                                            Long townId,
                                            SortCriteria sortCriteria,
                                            Long categoryId, SaleSituation situation){

        //0. 유효성 검사 : 설정한 townId가 , 그 사용자가 속한 town과 관련된 town의 Id 인지 검사
        /** 실제 당근마켓에서는 여러 town들이 설정될 수 있지만, 여기서는 사용자와 Item모두 하나의 town에 속한다는 가정 */
        if(memberRepository.findByUsername(username).getTown().getId() != townId){
            throw new ApiException(FAIL_GET_ITEM_LIST, "상품 목록을 가져올 town이 , 사용자가 속한 town과 다른 town으로 요청이 들어왔습니다.");
        }


        //1. 이후 조건에 따른 페이징을 수행하여 - 조회된 ItemSummaryDto 들을 반환
        /** 그냥 순수 Item으로 조회하면 , Item의 필드 중 , content 필드도 함꼐 조회해오는데 , 이게 양이 어마어마할 수 있음.
         * 따라서 DB에 무리가 가는 작업을 막기 위해 , ItemSummaryDto에 필요한 컬럼만 추출하는 [DTO로 조회하기] 를 사용 */
        List<Item> itemList = itemRepository.searchItems(townId,
                categoryId, situation, sortCriteria.getSpecifier(), page, limit);

//        List<ItemSummaryDto> itemSummaryDtoList = itemRepository.searchItemSummaryDtos(townId,
//                categoryId, situation, sortCriteria.getSpecifier(), page, limit);


        //2_1. 만약 조회한 상품이 하나도 없다면 -> 그에 따른 예외를 터뜨림 (클라이언트는 이 응답 결과를 이용)
        if(CollectionUtils.isEmpty(itemList)){
            throw new ApiException(ApiResponseStatus.NO_ITEMLIST, "더이상 등록된 상품이 없습니다.");
        }

        //2_2. 조회한 상품이 하나라도 있다면 - 조회한 DTO 그 자체를 반환
        return itemList.stream().map(i -> ItemSummaryDto.builder()
                .itemId(i.getId()).title(i.getTitle()).townName(i.getTown().getName()).createdAt(i.getCreatedAt())
                .price(i.getPrice()).itemImageUrl(i.getItemImageList().get(0).getPath())
                .numOfWish(i.getWishList().size()).numOfChattingRoomList(i.getChattingRoomList().size()).build())
                .collect(Collectors.toList());
        //return itemSummaryDtoList;
    }

    /** <특정 Path의 이미지 조회> */
    public InputStreamResource getItemImage(String encryptedPath){

       /** 단 , 복호화 이후 실제 상품과 관련되었는지의 추가검사는 할 필요가 없다 - 매우 희박한 가능성이면서 ,
        *  뚫린다고 해도 어차피 open된 이미지 이니 큰 문제가 되지 x */
        return fileServiceUtil.getImage(encryptedPath, aes128);
    }


    /** *
     * [등록서비스에서 사용되는 유효성 체크 로직]
     */
    private void checkRegister(List<MultipartFile> files, Long categoryId, Long townId, String username){

       //1. 사진 확장자에 대한 유효성 검사
        if(fileServiceUtil.checkExtension(files)==false){
            throw new IllegalArgumentException(
                    files.stream()
                            .map(f -> FilenameUtils.getExtension(f.getOriginalFilename())).collect(Collectors.toList()).toString()
            );
        }

        //2. categortyId에 대한 유효성 검사
        if(categoryRepository.existsById(categoryId)==false){
            throw new ApiException(ApiResponseStatus.FAIL_REGISTER_ITEM, "categoryId = " + categoryId + " , 유효하지 않은 categoryId 값이 들어왔기 때문에, 상품 등록에 실패했습니다.");
        }

        //3. townId에 대한 유효성 검사 (만약 살제 당근마켓처럼 사용자가 여러 town에 속하게 된다면, 이게 같지 않냐로 비교하는게 아니라 -> 속하지 않는걸로 비교)
        /** 참고로 굳이 townRepository에서 townId가 유효한 아이디 인지 확인할 필요가 없는게 , 어차피 SellerMember의 Town의 ID라면 유효한 값일 것이기 떄문에
         * 먼저 townId가 유효한 아이디 인지를 검사하고 나중에 그 Member의 townId임을 검사한다면 -> 이는 사실상 중복검사다 */
        if(memberRepository.findByUsername(username).getTown().getId() != townId){
            throw new ApiException(ApiResponseStatus.FAIL_REGISTER_ITEM, "townId = " + townId + " , 유효하지 않은 townId 값이 들어왔기 때문에, 상품 등록에 실패했습니다.");
        }

    }

    /**
     * [등록 서비스]
     */
    @Transactional
    public Long register(String username, RegisterItemDto registerItemDto){

        //0. 인자로 넘어온 값들의 유효성 검증
        checkRegister(registerItemDto.getImageList(), registerItemDto.getCategoryId(), registerItemDto.getTownId(), username);

        //1. 인자로 넘어온 유효한 값들을 기반으로 Item 엔티티 , ItemImage 엔티티를 생성하여 DB에 먼저 저장 (DB 작업을 먼저 수행해야 함이 핵심)
        Member sellerMember = memberRepository.findByUsername(username);

        Item item = createItem(registerItemDto, sellerMember);
        itemRepository.save(item);


        /**2_1. 이때 함께 등록할 이미지가 하나도 없으면 -> 곧바로 Item 엔티티만 생성한 후 , itemId값을 리턴  */
        if(CollectionUtils.isEmpty(registerItemDto.getImageList())){
            return item.getId();
        }

        /** 2_2.그렇지 않고 함께 등록할 이미지가 하나 이상 존재하면   <항상 DB먼저 수행 후 - File 작업을 수행해야 함>
         * -> 각 이미지들을 저장할 경로를 가진 ItemImage 엔티티들을 DB에 save한 후 -> 실제 그 경로에 각 사진을 저장한다. */

        // (1) 일단 각 이미지들이 저장될 경로를 가진 ItemImage 엔티티를 DB에 save (이때 동시성 문제를 고려하여 AtomicInteger 사용)

        //각 이미지 별로 내부 로컬에 저장할 path를 결정하고
        List<String> pathList = fileServiceUtil.getPathList(fileDir, registerItemDto, sellerMember.getId(), item.getId());

        //그 pathList에 담긴 각 이미지가 저장될 절대경로를 가지고 ItemImage 엔티티를 생성해서 save
        /** 이떄 각각 save하는 것 보다 , saveAll 하는게 트랜젝션상 성능이 더 좋다.*/
        List<ItemImage> itemImageList = createItemImageList(pathList, item);
        itemImageRepository.saveAll(itemImageList);


        // (2) 이후 해당 이미지를 실제로 로컬에 저장
        fileServiceUtil.saveImages(fileDir, registerItemDto, pathList, sellerMember.getId(), item.getId());

        return item.getId();
    }

    private Item createItem(RegisterItemDto registerItemDto, Member sellerMember){
        return Item.builder().title(registerItemDto.getTitle())
                .content(registerItemDto.getContent())
                .price(registerItemDto.getPrice())
                .visitCount(0)
                .delYn(DelYn.N)
                .salesituation(SaleSituation.FOR_SALE)
                .sellerMemberId(sellerMember.getId())
                .categoryId(registerItemDto.getCategoryId())
                .townId(registerItemDto.getTownId())
                .build();
    }

    private List<ItemImage> createItemImageList(List<String> pathList, Item item){
        return pathList.stream()
                .map(p -> ItemImage.builder().path(p).itemId(item.getId()).build())
                .collect(Collectors.toList());
    }




}
