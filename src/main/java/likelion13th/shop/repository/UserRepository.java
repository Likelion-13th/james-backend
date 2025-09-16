package likelion13th.shop.repository;

import likelion13th.shop.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long userId);
    boolean existsById(Long userId);

    Optional<User> findByProviderId(String providerId);
    boolean existsByProviderId(String providerId);

    //List<User> findByUsernickname(String usernickname);

    // Optional<User> findByKakaoId(String kakaoId);
}
