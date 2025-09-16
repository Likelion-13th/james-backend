package likelion13th.shop.login.auth.repository;

import likelion13th.shop.domain.User;
import likelion13th.shop.login.auth.jwt.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RefreshToken 저장소
 * - 사용자(User)와 1:1로 매핑된 RefreshToken을 조회/삭제한다.
 * - Spring Data JPA의 파생 쿼리와 @Query(JPQL)를 혼용.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 사용자 엔티티로 RefreshToken 한 건을 조회
    // - 존재하지 않을 수 있으므로 Optional로 감싼다.
    Optional<RefreshToken> findByUser(User user);

    // 사용자 기준으로 RefreshToken을 삭제 (JPQL 직접 정의)
    // - @Modifying: DML(DELETE/UPDATE) 쿼리임을 명시
    // - 트랜잭션 경계(@Transactional)는 서비스 레이어에서 감싸는 것을 권장
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);
}

/*
 * 1) 왜 필요한가?
 * - 사용자의 RefreshToken을 조회하거나 삭제한다
 *
 * 2) 없으면 / 틀리면?
 * - 없으면 로그아웃/탈취 대응이 불가능하다
 * - 틀리면 잘못된 사용자 매핑으로 인해 다른 사용자의 토큰을 삭제하거나 조회할 수 있다
 *
 * */