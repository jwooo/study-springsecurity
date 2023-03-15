package com.jwtoauth.jwtoauth.global.oauth2;

import com.jwtoauth.jwtoauth.global.oauth2.userinfo.GoogleOAuth2UserInfo;
import com.jwtoauth.jwtoauth.global.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.jwtoauth.jwtoauth.global.oauth2.userinfo.NaverOAuth2UserInfo;
import com.jwtoauth.jwtoauth.global.oauth2.userinfo.OAuth2UserInfo;
import com.jwtoauth.jwtoauth.user.entity.Role;
import com.jwtoauth.jwtoauth.user.entity.SocialType;
import com.jwtoauth.jwtoauth.user.entity.User;
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

    public static OAuthAttributes of(SocialType socialType,
                                     String usernameAttributeName,
                                     Map<String, Object> attributes) {

        if (socialType == SocialType.NAVER) {
            return ofNaver(usernameAttributeName, attributes);
        }
        if (socialType == SocialType.KAKAO) {
            return ofKakao(usernameAttributeName, attributes);
        }
        return ofGoogle(usernameAttributeName, attributes);
    }

    private static OAuthAttributes ofKakao(String usernameAttributeName,
                                           Map<String, Object> attributes) {

        return OAuthAttributes.builder()
                .nameAttributeKey(usernameAttributeName)
                .oAuth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }

    private static OAuthAttributes ofGoogle(String usernameAttributeName,
                                            Map<String, Object> attributes) {

        return OAuthAttributes.builder()
                .nameAttributeKey(usernameAttributeName)
                .oAuth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();
    }

    private static OAuthAttributes ofNaver(String usernameAttributeName,
                                           Map<String, Object> attributes) {

        return OAuthAttributes.builder()
                .nameAttributeKey(usernameAttributeName)
                .oAuth2UserInfo(new NaverOAuth2UserInfo(attributes))
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
