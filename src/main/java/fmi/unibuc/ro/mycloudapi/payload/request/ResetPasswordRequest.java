package fmi.unibuc.ro.mycloudapi.payload.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String oldPassword;
    private String newPassword;
}
