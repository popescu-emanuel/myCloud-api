package fmi.unibuc.ro.mycloudapi.constant;

public enum EncryptionKeyType {
    CK("ck.key"),
    CKP("ckp.key");

    public final String value;

    private EncryptionKeyType(String value) {
        this.value = value;
    }
}
