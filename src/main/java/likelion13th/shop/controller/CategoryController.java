package likelion13th.shop.controller;

import io.swagger.v3.oas.annotations.Operation;

import likelion13th.shop.repository.CategoryRepository;
import likelion13th.shop.service.CategoryService;
import likelion13th.shop.global.api.ApiResponse;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.api.SuccessCode;
import likelion13th.shop.global.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation (summary = "모든 카테고리 조회", description = "전체 카테고리를 조회합니다.")
    public ApiResponse<?> getAllCategories(){
        List<CategoryResponseDto> categories = categoryService.getAllCategories();

        if (categories.isEmpty()){
            return ApiResponse.onSuccess(SuccessCode.CATEGORY_ITEMS_EMPTY, Collections.emptyList());
        }
        return ApiResponse.onSuccess(SuccessCode.CATEGORY_ITEMS_GET_SUCCESS, categories);
    }

    @GetMapping("/{categoryId}")
    @Operation (summary = "개별 카테고리 조회", description = "개별 카테고리를 조회합니다.")
    public ApiResponse<?> getCategory(@PathVariable Long categoryId){
        try {
            CategoryResponseDto category = categoryService.getCategoryById(categoryId);
            return ApiResponse.onSuccess(SuccessCode.CATEGORY_ITEMS_GET_SUCCESS, category);
        } catch (GeneralException e) {
            log.error("[ERROR] 카테고리 조회 중 예외 발생: {}", e.getReason().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ERROR] 알 수 없는 예외 발생: {}", e.getMessage());
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // OrderController 기반으로 모든 카테고리랑 개별 카테고리를 조회하도록 했습니다.
}
