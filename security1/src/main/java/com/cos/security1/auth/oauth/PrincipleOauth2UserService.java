package com.cos.security1.auth.oauth;

import com.cos.security1.auth.PrincipleDetails;
import com.cos.security1.auth.oauth.provider.FacebookUserInfo;
import com.cos.security1.auth.oauth.provider.GoogleUserInfo;
import com.cos.security1.auth.oauth.provider.OAuth2UserInfo;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class PrincipleOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("userRequest : " + userRequest.getClientRegistration());
        System.out.println("userRequest : " + userRequest.getAccessToken().getTokenValue());

        OAuth2User oauth2user = super.loadUser(userRequest);
        System.out.println("userRequest : " + oauth2user.getAttributes());

        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("google 로그인 지원");
            oAuth2UserInfo = new GoogleUserInfo(oauth2user.getAttributes());

        } else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            System.out.println("facebook 로그인 지원");
            oAuth2UserInfo = new FacebookUserInfo(oauth2user.getAttributes());
        } else {
            System.out.println("페이스북만 지원합니다.");
        }

        String provider =  oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerId;
        String password = passwordEncoder.encode("겟인데어");
        String email = oAuth2UserInfo.getEmail();
        String role = "ROLE_USER";

        User userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
        }


        return new PrincipleDetails(userEntity, oauth2user.getAttributes());
    }
}

