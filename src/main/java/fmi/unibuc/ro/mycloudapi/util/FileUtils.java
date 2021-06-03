package fmi.unibuc.ro.mycloudapi.util;

import fmi.unibuc.ro.mycloudapi.properties.FileStorageProperties;
import fmi.unibuc.ro.mycloudapi.constant.EncryptionKeyType;
import fmi.unibuc.ro.mycloudapi.payload.request.DirectorySpecification;
import fmi.unibuc.ro.mycloudapi.payload.request.SimpleFileSpecification;
import fmi.unibuc.ro.mycloudapi.payload.request.UploadFileSpecification;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class FileUtils {

    private static final StandardCopyOption COPY_OPTION = StandardCopyOption.REPLACE_EXISTING;

    AuthenticationUtil authenticationUtil;

    final String CLOUD_STORAGE_EMAIL_TEMPLATE;
    final String CLOUD_KEYS_EMAIL_TEMPLATE;


    public FileUtils(FileStorageProperties fileStorageProperties, AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;

        // uploadDir/email
        CLOUD_STORAGE_EMAIL_TEMPLATE = getStorageEmailTemplate(fileStorageProperties);

        // keysDir/email
        CLOUD_KEYS_EMAIL_TEMPLATE = getKeysEmailTemplate(fileStorageProperties);
    }

    private String getKeysEmailTemplate(FileStorageProperties fileStorageProperties) {
        return fileStorageProperties.getKeysDir() + File.separator + "%s";
    }

    private String getStorageEmailTemplate(FileStorageProperties fileStorageProperties) {
        return fileStorageProperties.getUploadDir() + File.separator + "%s";
    }

    @Autowired
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil){
        this.authenticationUtil = authenticationUtil;
    }

    @SneakyThrows
    public String readFileFromClasspathAsString(String filename) {
        File file = ResourceUtils.getFile("classpath:" + filename);
        return new String(Files.readAllBytes(file.toPath()));
    }

    @SneakyThrows
    public byte[] readFileFromClasspathAsByteArray(String filename) {
        File file = ResourceUtils.getFile("classpath:" + filename);
        return Files.readAllBytes(file.toPath());
    }

    public Path getStorageLocation(){
        String email = authenticationUtil.getLoggedInUserEmail();
        String storageLocation = String.format(CLOUD_STORAGE_EMAIL_TEMPLATE, email);
        Path path = Path.of(storageLocation);
        return path;
    }

    public Path getKeysLocation(){
        String email = authenticationUtil.getLoggedInUserEmail();
        String keysLocation = String.format(CLOUD_KEYS_EMAIL_TEMPLATE, email);
        Path path = Path.of(keysLocation);
        return path;
    }

    public Path getStorageLocation(SimpleFileSpecification simpleFileSpecification){
        Path storageLocation = getStorageLocation();
        for(String folder : simpleFileSpecification.getBreadcrumb()){
            storageLocation = storageLocation.resolve(folder);
        }
        String filename = simpleFileSpecification.getFilename();
        filename = sanitizeFilename(filename);
        storageLocation = storageLocation.resolve(filename);

        return storageLocation;
    }

    public Path getKeysLocation(SimpleFileSpecification simpleFileSpecification){
        Path keysLocation = getKeysLocation();
        for(String folder : simpleFileSpecification.getBreadcrumb()){
            keysLocation = keysLocation.resolve(folder);
        }
        String filename = sanitizeFilename(simpleFileSpecification.getFilename());
        String hostDir = FilenameUtils.getBaseName(filename);

        keysLocation = keysLocation.resolve(hostDir);
        return keysLocation;
    }

    public Path getStorageLocation(UploadFileSpecification uploadFileSpecification){
        Path storageLocation = getStorageLocation();

        for(String folder : uploadFileSpecification.getBreadcrumb()){
            storageLocation = storageLocation.resolve(folder);
        }

        String filename = uploadFileSpecification.getFile().getOriginalFilename();
        filename = "disertatie_" + filename;

        filename = sanitizeFilename(filename);
        storageLocation = storageLocation.resolve(filename);

        return storageLocation;
    }

    public Path getKeysLocation(UploadFileSpecification uploadFileSpecification){
        Path keysLocation = getKeysLocation();

        for(String folder : uploadFileSpecification.getBreadcrumb()){
            keysLocation = keysLocation.resolve(folder);
        }

        String filename = uploadFileSpecification.getFile().getOriginalFilename();
        filename = FilenameUtils.getBaseName(filename);
        filename = sanitizeFilename(filename);

        keysLocation = keysLocation.resolve(filename);

        return keysLocation;
    }

    public Path getStorageLocation(DirectorySpecification directorySpecification){
        Path storageLocation = getStorageLocation();

        for(String folder : directorySpecification.getBreadcrumb()){
            storageLocation = storageLocation.resolve(folder);
        }

        String filename = directorySpecification.getFolderName();
        filename = sanitizeFilename(filename);
        storageLocation = storageLocation.resolve(filename);

        return storageLocation;
    }

    public Path getKeysLocation(DirectorySpecification directorySpecification){
        Path keysLocation = getKeysLocation();

        for(String folder : directorySpecification.getBreadcrumb()){
            keysLocation = keysLocation.resolve(folder);
        }

        String filename = directorySpecification.getFolderName();
        filename = FilenameUtils.getBaseName(filename);
        filename = sanitizeFilename(filename);

        keysLocation = keysLocation.resolve(filename);

        return keysLocation;
    }

    public Path getKeyLocation(SimpleFileSpecification simpleFileSpecification, EncryptionKeyType key){
        Path targetLocation = getKeysLocation(simpleFileSpecification);
        targetLocation = targetLocation.resolve(key.value);
        return targetLocation;
    }

    public Path getKeyLocation(UploadFileSpecification uploadFileSpecification, EncryptionKeyType key){
        Path targetLocation = getKeysLocation(uploadFileSpecification);
        targetLocation = targetLocation.resolve(key.value);
        return targetLocation;
    }

    public String sanitizeFilename(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "");
    }

    @SneakyThrows
    public void storeFile(UploadFileSpecification uploadFileSpecification, byte[] content) {
        Path targetLocation = getStorageLocation(uploadFileSpecification);
        copyContentToFile(targetLocation, content);
    }

    @SneakyThrows
    public void storeKey(Path targetLocation, byte[] content) {
        copyContentToFile(targetLocation, content);
    }

    private void copyContentToFile(Path targetLocation, byte[] content) throws IOException {
        File newFile = new File(targetLocation.toUri());
        newFile.mkdirs();
        Files.copy(new ByteArrayInputStream(content), targetLocation, COPY_OPTION);
    }



}
