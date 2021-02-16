package fmi.unibuc.ro.mycloudapi.encryption;

import fmi.unibuc.ro.mycloudapi.constant.EncryptionKeyType;
import fmi.unibuc.ro.mycloudapi.exception.storage.FileStorageException;
import fmi.unibuc.ro.mycloudapi.model.User;
import fmi.unibuc.ro.mycloudapi.payload.request.SimpleFileSpecification;
import fmi.unibuc.ro.mycloudapi.payload.request.UploadFileSpecification;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import fmi.unibuc.ro.mycloudapi.util.AuthenticationUtil;
import fmi.unibuc.ro.mycloudapi.util.ByteArrayUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@SuppressWarnings("UnnecessaryLocalVariable")
public class EncryptionUtils {

    public static final String CK_FILENAME = "ck.key";
    public static final String CKP_FILENAME = "ckp.key";
    private final static String HASHING_ALGORITHM = "SHA3-256";

    private final AESUtils aesUtils;
    private final RSAUtils rsaUtils;

    private final UserRepository userRepository;
    private final ByteArrayUtil byteArrayUtil;
    private final fmi.unibuc.ro.mycloudapi.util.FileUtils fileUtils;

    private final AuthenticationUtil authenticationUtil;
    private final PasswordEncoder passwordEncoder;

    public EncryptionUtils(
            UserRepository userRepository,
            AESUtils aesUtils,
            RSAUtils rsaUtils,
            ByteArrayUtil byteArrayUtil,
            fmi.unibuc.ro.mycloudapi.util.FileUtils fileUtils,
            AuthenticationUtil authenticationUtil,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.aesUtils = aesUtils;
        this.rsaUtils = rsaUtils;
        this.byteArrayUtil = byteArrayUtil;

        this.fileUtils = fileUtils;
        this.authenticationUtil = authenticationUtil;
        this.passwordEncoder = passwordEncoder;
    }


    @SneakyThrows
    public void storeKeyFor(String email, File file) {
//        Path path = getStorageLocationFor(email, file.getName());
//        File newFile = new File(path.toUri());
//        newFile.mkdirs();
//        Files.copy(new FileInputStream(file), path, COPY_OPTION);
    }

    @SneakyThrows
    public byte[] encryptFileWithAES(UploadFileSpecification uploadFileSpecification) {

        MultipartFile multipartFile = uploadFileSpecification.getFile();

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        checkIfPathContainsIllegalSequence(fileName);

        byte[] byteContent = multipartFile.getBytes();

        final byte[] randomEncByteArray = generateRandomByteArray(16);
//        SecretKey randomSecretKey = aesUtils.generateSecretKeyForFile(multipartFile, new String(randomEncByteArray));
        SecretKey randomSecretKey = aesUtils.generateSecretKey(new String(randomEncByteArray));

        final String userPassword = authenticationUtil.getPassword();
        final byte[] hashedPassword = hash(userPassword);

        computeIntegrityKeysFor(uploadFileSpecification, randomEncByteArray, hashedPassword);

        return aesUtils.encrypt(randomSecretKey, byteContent);

    }

    @SneakyThrows
    public void recomputeIntegrityKeysForAllFiles(String email, String password) {
        log.warn("Old password {}", authenticationUtil.getPassword());

        final String encodedPassword = passwordEncoder.encode(password);
        byte[] newPasswordHash = hash(encodedPassword);

        Path keysLocation = fileUtils.getKeysLocation();
        File keysDir = new File(keysLocation.toUri());

        log.warn("Recompute keys for {} using {}", email, password);
        final File[] allKeyDirectories = keysDir.listFiles();
        updatePassword(encodedPassword);

        if (allKeyDirectories == null || allKeyDirectories.length == 0) {
            log.warn("There are no stored keys for {}", email);
            return;
        }

        reEncryptFolder(keysDir, newPasswordHash);
    }

    private void reEncryptFolder(File startingDir, byte[] newPasswordHash) throws IOException {
        if (!startingDir.isDirectory()) {
            log.warn("Could not process folder {}", startingDir);
            return;
        }

        if (startingDir.listFiles() == null) {
            log.warn("Could not retrieve folder content for {}", startingDir);
            return;
        }

        byte[] oldPasswordHash = hash(authenticationUtil.getPassword());
        File[] folderContent = startingDir.listFiles();

        for (File folder : folderContent) {
            boolean isKeysDirectory = isKeysDirectory(folder);
            if (isKeysDirectory) {
                log.warn("\tReencrypt file " + folder.getAbsolutePath());
                reEncryptFile(folder, oldPasswordHash, newPasswordHash);
            } else { // isDir
                log.warn("Open folder " + folder.getName());
                reEncryptFolder(folder, newPasswordHash);
            }
        }
    }

    private boolean isKeysDirectory(File folder) {
        if (folder == null || !folder.isDirectory()) {
            log.warn("Could not check if the input file is file");
            throw new InvalidParameterException();
        }
        File[] folderContent = folder.listFiles();
        final List<File> keysInDirectory = Arrays.stream(folderContent)
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().equals("ck.key"))
                .collect(Collectors.toList());

        final boolean containsKeys = keysInDirectory.size() != 0;
        return containsKeys;
    }

    private void reEncryptFile(File folder, byte[] oldPasswordHash, byte[] newPasswordHash) throws IOException {
        File[] keysForFile = folder.listFiles();

        if (keysForFile == null || keysForFile.length != 2) {
            log.warn("Could not recompute keys for an invalid file structure {}", folder.getName());
            return;
        }

        final Optional<File> maybeCkFile = Stream.of(keysForFile)
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().equals("ck.key"))
                .reduce((a, b) -> {
                    throw new IllegalStateException("Duplicate found for " + a.getName());
                });

        if (!maybeCkFile.isPresent()) {
            log.warn("\tIntegrity file not found for {}", folder.getName());
            return;
        }

        reEncrypt(folder, newPasswordHash, oldPasswordHash, maybeCkFile.get());
    }

    private void reEncrypt(File folder, byte[] newPasswordHash, byte[] oldPasswordHash, File ckFile) throws IOException {
        log.warn("\t{}", ckFile.getAbsolutePath());

        byte[] oldCkContent = FileUtils.readFileToByteArray(ckFile);
        final byte[] randomGeneratedKey = aesUtils.decrypt(new String(oldPasswordHash), oldCkContent);
        log.warn("\tSuccessfully decrypted oldCkContent file for {}", folder.getName());

        final SecretKey secretKeyForRandomKey = aesUtils.generateSecretKey(new String(newPasswordHash));
        byte[] ck = aesUtils.encrypt(secretKeyForRandomKey, randomGeneratedKey); // Cheie pentru fisiere criptata
        byte[] ckp = aesUtils.encrypt(secretKeyForRandomKey, byteArrayUtil.additionToByteArray(randomGeneratedKey, 1)); // CKP

        // todo: Recompute
        storeIntegrityKeysFor(folder, ck, ckp);
        log.warn("\tSuccessfully generated new pair of integrity keys for {}", folder.getName());
        log.warn("");
    }

    private void updatePassword(String password) {
        final String loggedInUserEmail = authenticationUtil.getLoggedInUserEmail();
        final Optional<User> maybeUser = userRepository.findByEmail(loggedInUserEmail);
        if (maybeUser.isEmpty()) {
            log.error("Email could not be found");
            throw new UsernameNotFoundException("User Not Found with email: " + loggedInUserEmail);
        } else {
            User user = maybeUser.get();
            user.setPassword(password);
            userRepository.save(user);
            log.warn("New password: {}", password);
        }
    }

    public void computeIntegrityKeysFor(UploadFileSpecification uploadFileSpecification, byte[] randomEncByteArray, byte[] hashedPassword) {
        final SecretKey secretKeyForRandomKey = aesUtils.generateSecretKey(new String(hashedPassword));
        byte[] ck = aesUtils.encrypt(secretKeyForRandomKey, randomEncByteArray); // Cheie pentru fisiere criptata
        byte[] ckp = aesUtils.encrypt(secretKeyForRandomKey, byteArrayUtil.additionToByteArray(randomEncByteArray, 1)); // CKP

        removeIntegrityKeysFor(uploadFileSpecification);          // todo: Check if keys inside folders are removed
        storeIntegrityKeysFor(uploadFileSpecification, ck, ckp);  // todo: Store keys inside corresponding folder
    }

    private void removeIntegrityKeysFor(UploadFileSpecification uploadFileSpecification) {
        try {
            Path keysLocation = fileUtils.getKeysLocation(uploadFileSpecification);
            Files.deleteIfExists(keysLocation);
        } catch (IOException exception) {
            log.warn("Could not remove integrity files for {}", uploadFileSpecification.getFile().getOriginalFilename());
        }
    }

    private void storeIntegrityKeysFor(UploadFileSpecification uploadFileSpecification, byte[] ckContent, byte[] ckpContent) {
        Path ckPath = fileUtils.getKeyLocation(uploadFileSpecification, EncryptionKeyType.CK);
        Path ckpPath = fileUtils.getKeyLocation(uploadFileSpecification, EncryptionKeyType.CKP);
        fileUtils.storeKey(ckPath, ckContent);
        fileUtils.storeKey(ckpPath, ckpContent);
    }

    private void storeIntegrityKeysFor(File folder, byte[] ckContent, byte[] ckpContent) {
        Path ckPath = folder.toPath();
        ckPath = ckPath.resolve(EncryptionKeyType.CK.value);
        log.warn("\t\tCK: {}", ckPath);

        Path ckpPath = folder.toPath();
        ckpPath = ckpPath.resolve(EncryptionKeyType.CKP.value);
        log.warn("\t\tCKP: {}", ckpPath);

        fileUtils.storeKey(ckPath, ckContent);
        fileUtils.storeKey(ckpPath, ckpContent);
    }

    public byte[] decryptFileWithAES(SimpleFileSpecification simpleFileSpecification) {
        byte[] aesDecrypted = null;
        try {
            File encryptedFile = loadEncryptedFile(simpleFileSpecification);
            byte[] fileContent = FileUtils.readFileToByteArray(encryptedFile);

            String password = authenticationUtil.getPassword();
            String hashedUserPasswordAsString = new String(hash(password));

            Path ckPath = fileUtils.getKeyLocation(simpleFileSpecification, EncryptionKeyType.CK);
            Path ckpPath = fileUtils.getKeyLocation(simpleFileSpecification, EncryptionKeyType.CKP);

            File ckFile = new File(ckPath.toUri());
            File ckpFile = new File(ckpPath.toUri());

            byte[] ck = FileUtils.readFileToByteArray(ckFile);
            byte[] ckp = FileUtils.readFileToByteArray(ckpFile);

            byte[] decryptedCk = aesUtils.decrypt(hashedUserPasswordAsString, ck); // randomKeyDecrypted
            byte[] decryptedCkp = aesUtils.decrypt(hashedUserPasswordAsString, ckp);//

            final boolean isPasswordCorrect = checkIntegrityOfKeys(decryptedCk, decryptedCkp);
            if (!isPasswordCorrect) {
                throw new RuntimeException("password is not correct");
            }

            aesDecrypted = aesUtils.decrypt(new String(decryptedCk), fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aesDecrypted;
    }

    private boolean checkIntegrityOfKeys(byte[] decryptedCk, byte[] decryptedCkp) {
        byte[] addition = byteArrayUtil.additionToByteArray(decryptedCk, 1);
        return Arrays.equals(addition, decryptedCkp);
    }


    private void checkIfPathContainsIllegalSequence(String fileName) {
        if (fileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
        }
    }

    /**
     * @param size - 16 for 128 bits, 32 for 256 bits
     * @return random byte array of size {@param size}
     */
    public byte[] generateRandomByteArray(int size) {
        SecureRandom random = new SecureRandom();
        byte[] randomByteKey = new byte[size]; // 128 bits are converted to 16 bytes;
        random.nextBytes(randomByteKey);
        return randomByteKey;
    }

    public byte[] hash(String text) {
        byte[] hashedUserPassword = text.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest sha = MessageDigest.getInstance(HASHING_ALGORITHM);
//            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            hashedUserPassword = sha.digest(hashedUserPassword);
            log.debug("Key is {}", new String(hashedUserPassword));
        } catch (Exception e) {
            log.error("Error when hashing text {}", text);
            e.printStackTrace();
        }
        return hashedUserPassword;
    }

    private File loadEncryptedFile(SimpleFileSpecification simpleFileSpecification) throws FileNotFoundException {
        Path targetLocation = fileUtils.getStorageLocation(simpleFileSpecification);
        final File file = new File(targetLocation.toUri());
        if (!file.exists()) {
            log.warn("Could not find file {} on the server", targetLocation.toUri());
            throw new FileNotFoundException();
        }
        return file;
    }

}
