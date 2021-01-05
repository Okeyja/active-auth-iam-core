package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Entity
public class AuthenticationPrincipal implements IamResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String encryptedPassword;

    private Date createdAt;

    private Date updatedAt;

    private boolean sessionCreatable;

    private boolean signatureCreatable;

    private PrincipalType principalType;

    @ManyToOne
    private AuthenticationPrincipal owner;

    @Override
    public String resourceLocator() {
        return String.format("%s://users/%s/principal", "iam", id);
    }

    public AuthenticationPrincipal(
            String name, String originalPassword,
            boolean sessionCreatable, boolean signatureCreatable,
            PrincipalType principalType,
            PasswordHashingStrategy passwordHashingStrategy
    ) {
        this.name = name;
        this.encryptedPassword = passwordHashingStrategy.getHashing().hashing(originalPassword);
        this.sessionCreatable = sessionCreatable;
        this.signatureCreatable = signatureCreatable;
        this.principalType = principalType;
        this.createdAt = new Date();
    }

    public static enum PrincipalType {
        PRINCIPAL, PRINCIPAL_GROUP, APP_DOMAIN;
    }

    public static Long idFromLocator(String locator) throws PatternException {
        String pattern = "^iam://users/(\\d+)/principal/?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(locator);
        if (m.find()) {
            String principalIdStr = m.group(1);
            return Long.valueOf(principalIdStr);
        } else {
            throw new PatternException("Principal locator regex not matching for ^iam://users/\\d+$/principal/?");
        }
    }

    public boolean passwordVerify(String toCheckPassword, PasswordHashingStrategy passwordHashingStrategy) {
        return passwordHashingStrategy.getHashing().check(toCheckPassword, encryptedPassword);
    }

    public Vo vo() {
        Vo vo = new Vo();
        vo.id = id;
        vo.resourceLocator = this.resourceLocator();
        vo.name = name;
        vo.createAt = createdAt;
        vo.sessionCreatable = sessionCreatable;
        vo.signatureCreatable = signatureCreatable;
        vo.principalType = principalType;
        return vo;
    }

    @Data
    @Schema(name = "AuthenticationPrincipal.Vo")
    public static class Vo {
        private Long id;
        @Schema(defaultValue = "iam://users/116/principal")
        private String resourceLocator;
        @Schema(defaultValue = "pony")
        private String name;
        private Date createAt;
        @Schema(defaultValue = "false", type = "boolean")
        private boolean sessionCreatable;
        @Schema(defaultValue = "false", type = "boolean")
        private boolean signatureCreatable;
        private PrincipalType principalType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthenticationPrincipal.PrincipalForm")
    public static class PrincipalForm {

        @NotBlank
        @Schema(defaultValue = "pony")
        private String name;

        @NotBlank
        @Schema(defaultValue = "P0ny_1980")
        private String password;

        private boolean canUseToken;

        private boolean canUseSignature;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthenticationPrincipal.UserRegisterForm")
    public static class UserRegisterForm {

        @NotBlank
        @Schema(defaultValue = "pony")
        private String name;

        @NotBlank
        @Schema(defaultValue = "P0ny_1980")
        private String password;
    }

    public static class PasswordNotMatchException extends Exception {
        public PasswordNotMatchException(String msg) {
            super(msg);
        }
    }

    public static class PrincipalTypeDoesNotAllowedToLoginException extends Exception {
        public PrincipalTypeDoesNotAllowedToLoginException(String msg) {
            super(msg);
        }
    }
}
