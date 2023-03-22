package com.example.personaljwtpractice.api.config.oauth2;

import com.example.personaljwtpractice.api.config.oauth2.userinfo.GoogleOAuth2UserInfo;
import com.example.personaljwtpractice.api.config.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.example.personaljwtpractice.api.config.oauth2.userinfo.NaverOAuth2UserInfo;
import com.example.personaljwtpractice.api.config.oauth2.userinfo.OAuth2UserInfo;
import com.example.personaljwtpractice.api.entity.Role;
import com.example.personaljwtpractice.api.entity.SocialType;
import com.example.personaljwtpractice.api.entity.User;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class OAuthAttributes {

    private String nameAttributeKey;
    private OAuth2UserInfo oAuth2UserInfo;

    @Builder
    public OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oAuth2UserInfo) {
        this.nameAttributeKey = nameAttributeKey;
        this.oAuth2UserInfo = oAuth2UserInfo;
    }

    public static OAuthAttributes of(SocialType socialType, String usernameAttributeName, Map<String, Object> attributes) {
        if (socialType == SocialType.NAVER) {
            return ofNaver(usernameAttributeName, attributes);
        }
        if (socialType == SocialType.KAKAO) {
            return ofKakao(usernameAttributeName, attributes);
        }
        return ofGoogle(usernameAttributeName, attributes);
    }

    public static OAuthAttributes ofNaver(String usernameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(usernameAttributeName)
                .oAuth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .build();
    }

    public static OAuthAttributes ofKakao(String usernameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(usernameAttributeName)
                .oAuth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();

    }

    public static OAuthAttributes ofGoogle(String usernameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(usernameAttributeName)
                .oAuth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();

    }

    public User toEntity(SocialType socialType, OAuth2UserInfo oAuth2UserInfo) {
        return User.builder()
                .socialType(socialType)
                .socialId(oAuth2UserInfo.getId())
                .email(UUID.randomUUID() + "@socialUser.com")
                .nickname(oAuth2UserInfo.getNickname())
                .imageUrl(oAuth2UserInfo.getImageUrl())
                .role(Role.GUEST)
                .build();
    }
}
