package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalSecretKey;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import org.springframework.data.domain.Page;

public interface AuthenticationPrincipalSecretKeyService {
    AuthenticationPrincipalSecretKey getKeyById(Long keyId) throws NotFoundException;

    AuthenticationPrincipalSecretKey deleteKeyById(Long keyId) throws NotFoundException;

    Page<AuthenticationPrincipalSecretKey> pagingKeysOfOwner(AuthenticationPrincipal principal, int page, int size);

    AuthenticationPrincipalSecretKey generateKey(AuthenticationPrincipal principal, AuthenticationPrincipalSecretKey.GenKeyPairForm form);

    AuthenticationPrincipalSecretKey getKeyByKeyCode(String keyCode) throws NotFoundException;
}
