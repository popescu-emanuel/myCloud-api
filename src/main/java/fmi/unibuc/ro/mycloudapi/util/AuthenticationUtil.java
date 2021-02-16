package fmi.unibuc.ro.mycloudapi.util;

import fmi.unibuc.ro.mycloudapi.exception.authorization.UserNotAuthenticatedException;
import fmi.unibuc.ro.mycloudapi.security.services.UserDetailsImpl;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationUtil {
    public UserDetailsImpl getPrincipal() {
        if (isAuthenticated()) {
            return (UserDetailsImpl)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } else {
            throw new UserNotAuthenticatedException();
        }
    }

    public String getLoggedInUserEmail(){
        return getPrincipal().getEmail();
    }

    public String getPassword() {
        return getPrincipal().getPassword();
    }

    public Boolean isAuthenticated() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final boolean isAnonymousToken = authentication instanceof AnonymousAuthenticationToken;
        return authentication != null && !isAnonymousToken;
    }
}
