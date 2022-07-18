package com.devit.user.controller;

import com.devit.user.dto.EditResumeRequest;
import com.devit.user.dto.SendResumeMessage;
import com.devit.user.dto.response.ResponseDetails;
import com.devit.user.dto.SendResumeRequest;
import com.devit.user.entity.Resume;
import com.devit.user.exception.NoResourceException;
import com.devit.user.message.RabbitMqSender;
import com.devit.user.service.CategoryService;
import com.devit.user.service.ResumeService;
import com.devit.user.service.UserService;
import com.devit.user.util.HttpStatusChangeInt;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;


/**
 * 1. 이력서 수정.
 *
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final RabbitMqSender rabbitMqSender;

    @PutMapping("/api/users/resumes") //수정되야함
    @ApiOperation(value = "이력서 수정", notes = "이력서를 수정합니다.")
    public ResponseEntity<?> editResume(@RequestHeader("Authorization") String data, @RequestBody EditResumeRequest request) throws NoResourceException {

        String[] chunks = data.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));

        JSONObject jsonObject = new JSONObject(payload);
        String sample = jsonObject.getString("uuid");
        UUID uuid = UUID.fromString(sample); //토큰 복호화 완료

        //        Category findCategory = categoryService.findCategoryByName(request.getCategoryName()); //카테고리 찾아오기


        Resume editResume = resumeService.save(request, uuid);

        int httpStatus = HttpStatusChangeInt.ChangeStatusCode("OK");
        String path = "api/users/resumes";

        ResponseDetails responseDetails = new ResponseDetails(new Date(), editResume, httpStatus, path);
        return new ResponseEntity<>(responseDetails, HttpStatus.CREATED);
    }

    @PostMapping("/api/users/resumes")
    @ApiOperation(value = "이력서 전송", notes = "이력서를 전송합니다.")
    public ResponseEntity<?> sendResume(@RequestHeader("Authorization") String data, @RequestBody @Valid SendResumeRequest request) {
        String[] chunks = data.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));

        JSONObject jsonObject = new JSONObject(payload);
        String sample = jsonObject.getString("uuid");
        UUID uuid = UUID.fromString(sample); //토큰 복호화 완료


        SendResumeMessage sendResumeMessage = new SendResumeMessage(uuid, request.getBoardId());
        rabbitMqSender.send(sendResumeMessage);

        String message = "이력서 전송 완료";
        int httpStatus = HttpStatusChangeInt.ChangeStatusCode("OK");
        String path = "api/users/resumes";


        ResponseDetails responseDetails = new ResponseDetails(new Date(), message, httpStatus, path);
        return new ResponseEntity<>(responseDetails, HttpStatus.CREATED);
    }


}
