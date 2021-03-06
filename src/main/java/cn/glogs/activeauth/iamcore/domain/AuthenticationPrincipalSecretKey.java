package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Base64;
import java.util.Date;

@Data
@Entity
public class AuthenticationPrincipalSecretKey implements IamResource {

    private static final Base64.Encoder base64Encoder = Base64.getEncoder();

    private static final String INFO_SECURED = "INFORMATION IS SECURED AND HIDDEN!";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AuthenticationPrincipal principal;

    @Column(unique = true)
    private String keyCode;


    private String description;

    @Lob
    private String pubKey;

    @Transient
    private String priKey;

    private boolean enabled;

    private SecretKeyType keyType;

    private Date createTime;

    @Override
    public String resourceLocator(LocatorConfiguration locatorConfiguration) {
        return locatorConfiguration.fullLocator(String.valueOf(principal.getId()), "secret-key", String.valueOf(id));
    }

    public enum SecretKeyType {
        RSA_2048,
        PRIVATE_KEY
    }

    public Vo vo(LocatorConfiguration locatorConfiguration) {
        Vo vo = new Vo();
        vo.id = id;
        vo.locator = resourceLocator(locatorConfiguration);
        vo.keyCode = keyCode;
        if (StringUtils.isNotBlank(priKey))
            vo.privateKey = new String(base64Encoder.encode(priKey.getBytes()));
        vo.description = description;
        vo.enabled = enabled;
        vo.createTime = createTime;
        return vo;
    }

    @Data
    @Schema(name = "AuthenticationPrincipalKeyPair.Vo")
    public static class Vo {
        private Long id;
        @Schema(example = "arn:cloudapp:iam::72:key-pair/45")
        private String locator;
        @Schema(defaultValue = "39125471-2164-4ae6-b41c-7a0f2f28f1ae")
        private String keyCode;
        @Schema(defaultValue = "base64Encode('-----BEGIN PRIVATE KEY----- \n ****** \n -----END PRIVATE KEY-----')")
        private String privateKey;
        @Schema(defaultValue = "My Private Key.")
        private String description;
        private boolean enabled;
        private Date createTime;

        public Vo securePrivateKey() {
            this.privateKey = INFO_SECURED;
            return this;
        }
    }

    @Data
    @Schema(name = "AuthenticationPrincipalKeyPair.GenKeyPairForm")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenKeyPairForm {
        @Schema(defaultValue = "My Private Key.")
        private String description;
    }
}
