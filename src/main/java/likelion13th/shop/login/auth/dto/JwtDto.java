package likelion13th.shop.login.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@Getter
@Setter

public class JwtDto {
    private String AccessToken;
    private String RefreshToken;

    public JwtDto(String accessToken, String refreshToken) {
        this.AccessToken = accessToken;
        this.RefreshToken = refreshToken;
    }
}

/*
* 1) 왜 필요한가?
* - 클라이언트 로그인 시에 서버에서 로그인 정보를 확인해 JWT 가 발행된다.
* 사용자 정보에 접근할 수 있게 도와준다. 매번 요청할때마다 아이디/비밀번호를 입력하지 않고 증명한다.
*
* 2) 없으면 / 틀리면 ?
* - JWT가 없으면 사용자 정보에 접근할 수 없다.
*
* */