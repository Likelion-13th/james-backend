package likelion13th.shop.controller;

import io.swagger.v3.oas.annotations.Operation;

import likelion13th.shop.DTO.response.UserMileageResponse;
import likelion13th.shop.domain.User;
import likelion13th.shop.DTO.response.UserInfoResponse;
import likelion13th.shop.global.api.ApiResponse;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.api.SuccessCode;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.login.auth.jwt.CustomUserDetails;
import likelion13th.shop.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/")
public class UserInfoController {
    private final UserService userService;
    private final UserInfoService userInfoService;

    @GetMapping("/profile")
    @Operation(summary = "내 정보 조회", description = "로그인한 회원의 정보를 조회합니다.")
    public ApiResponse<UserInfoResponse> getUserInfo(
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        User user= userService.findByProviderId(customUserDetails.getProviderId())
                .orElseThrow(()-> new GeneralException(ErrorCode.USER_NOT_FOUND));
        log.info("[STEP 1] 내 정보 조회 요청 수신");
        try{
            UserInfoResponse userInfoResponse = userInfoService.getUserInfo(user);
            log.info("[STEP 2] 내 정보 조회 성공");
            return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, userInfoResponse);
        }
        catch (GeneralException e) {
            log.error("[ERROR] 내정보 조회 중 예외 발생: {}", e.getReason().getMessage());
            throw e;
        }
        catch (Exception e){
            log.error("[ERROR] 알 수 없는 예외 발생: {}", e.getMessage());
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        //사용자 정보 조회
    }



    @GetMapping ("/mileage")
    @Operation(summary = "내 마일리지 조회", description = "로그인한 회원의 마일리지를 조회합니다.")
    public ApiResponse<?> getUserMileage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        User user = userService.findByProviderId(CustomUserDetails.getProviderId())
                .orElseThrow(()-> new GeneralException(ErrorCode.USER_NOT_FOUND));
        log.info("[STEP 1] 내 마일리지 조회 요청 수신");

        try{
            UserMileageResponse Mileage = userInfoService.getUserMileage(user);
            log.info("[STEP 2] 내 마일리지 조회 성공");
            return ApiResponse.onSuccess(SuccessCode.USER_MILEAGE_GET_SUCCESS, Mileage);
        }
        catch (GeneralException e) {
            log.error("[ERROR] 내 마일리지 조회 중 에러 발생: {}", e.getReason().getMessage());
            throw e;
        }
        catch (Exception e){
            log.error("[ERROR] 알 수 없는 예외 발생: {}", e.getMessage());
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        // 마일리지 조회
    }

    // 로그인한 사용자의 주소 조회
    @GetMapping("/address")
    @Operation(summary = "내 주소 조회", description = "로그인한 회원의 주소를 조회합니다.")
    public ApiResponse<?> getUserAddress(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        User user= userService.findByProviderId(customUserDetails.getProviderId())
                .orElseThrow(()-> new GeneralException(ErrorCode.USER_NOT_FOUND));
        log.info("[STEP 1] 내 주소 조회 요청 수신");
        try{
            UserInfoResponse userInfoResponse = userInfoService.getUserInfo(user);
            log.info("[STEP 2] 내 주소 조회 성공");
            return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, userInfoResponse);
        }
        catch (GeneralException e) {
            log.error("[ERROR] 내주소 조회 중 예외 발생: {}", e.getReason().getMessage());
            throw e;
        }
        catch (Exception e){
            log.error("[ERROR] 알 수 없는 예외 발생: {}", e.getMessage());
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
