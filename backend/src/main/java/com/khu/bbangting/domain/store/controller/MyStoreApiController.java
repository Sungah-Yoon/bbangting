package com.khu.bbangting.domain.store.controller;

import com.khu.bbangting.domain.store.dto.StoreFormDto;
import com.khu.bbangting.domain.store.service.MyStoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/myStore")
public class MyStoreApiController {

    @Autowired
    private MyStoreService myStoreService;

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/new")
    public ResponseEntity<String> createMyStore(@Valid @RequestPart StoreFormDto requestDto, BindingResult bindingResult, @RequestPart("imageFile")
    List<MultipartFile> imageFileList) throws Exception {

        // 유효성 검사
        if (bindingResult.hasErrors()) {
            log.info("requestDto 검증 오류 발생 errors={}", bindingResult.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("스토어 등록 실패 : " + bindingResult.getFieldError().getDefaultMessage());
        }

        // 예외 처리) 스토어 이미지 등록 2개 이상 필수 (로고 이미지, 스토어 대표 이미지)
        if (imageFileList.size() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("스토어 등록 실패 : 로고 이미지와 스토어 대표 이미지는 필수 입력값입니다.");
        }

        myStoreService.saveStore(requestDto, imageFileList);
        return ResponseEntity.status(HttpStatus.CREATED).body("마이스토어 등록 성공");

    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteMyStore(@PathVariable Long userId) {

        myStoreService.deleteStore(userId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("마이스토어 삭제 성공");
    }

    @GetMapping("/edit/{userId}")
    public ResponseEntity<?> updateMyStorePage(@PathVariable Long userId) {

        StoreFormDto storeFormDto = myStoreService.getStoreForm(userId);
        return ResponseEntity.ok(storeFormDto);

    }

    @PostMapping("/edit/{userId}")
    public ResponseEntity<?> updateMyStore(@PathVariable Long userId, @Valid @RequestPart StoreFormDto requestDto, BindingResult bindingResult, @RequestPart("imageFile")
    List<MultipartFile> imageFileList) throws Exception {

        if (bindingResult.hasErrors()) {
            log.info("requestDto 검증 오류 발생 errors={}", bindingResult.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("스토어 수정 실패 : " + bindingResult.getFieldError().getDefaultMessage());
        }

        myStoreService.updateStore(userId, requestDto, imageFileList);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("마이스토어 수정 성공");

    }
}
