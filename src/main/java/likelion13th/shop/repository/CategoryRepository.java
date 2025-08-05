package likelion13th.shop.repository;

import likelion13th.shop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    //카테고리 id 기반 카테고리 탐색
    Optional<Category> findById(Long categoryId);
    //카테고리 이름 기반 카테고리 탐색
    Optional<Category> findByCategoryName(String name);
}

