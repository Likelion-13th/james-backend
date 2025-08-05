package likelion13th.shop.service;

import jakarta.transaction.Transactional;
import likelion13th.shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryResponseDto getCategory(Long id){

    }

    public CategoryResponseDto getCategoryByName(String name){

    }

    public CategoryResponseDto getCategoryByCategoryId(Long categoryId){

    }

    public CategoryResponseDto getCategoryByCategoryName(String categoryName){

    }
}
