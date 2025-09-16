package likelion13th.shop.login.auth.utils;

import likelion13th.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("// 카카오 OAuth2 로그인 시도");

        String providerId = oAuth2User.getAttributes().get("id").toString();

        @SuppressWarnings("unchecked")
        Map<String, Object> properties =
                (Map<String, Object>) oAuth2User.getAttributes().getOrDefault("properties", Collections.emptyMap());
        String nickname = properties.getOrDefault("nickname", "카카오사용자").toString();

        Map<String, Object> extendedAttributes = new HashMap<>(oAuth2User.getAttributes());
        extendedAttributes.put("provider_id", providerId);
        extendedAttributes.put("nickname", nickname);

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                extendedAttributes,
                "provider_id"
        );
    }
}
/*
 * 1) 왜 필요한가?
 * - 카카오 로그인 시 서비스 표준 키로 정규화하기 위해 필요하다.
 * - principal의 name으로 사용할 키를 명확히 지정하여 핸들러/서비스가 동일한 식별자로 동작하게 한다.
 * - OAuth2의 내부 서비스 정보로 변환해준다
 *
 * 2) 없으면 / 틀리면?
 * - 해당클래스가 없으면 카카오 로그인 시 provider 별로 제각각의 속성을 가진 상태로 변환되므로 서비스 안에서 사용자 식별이 어렵다.
 *
 * */