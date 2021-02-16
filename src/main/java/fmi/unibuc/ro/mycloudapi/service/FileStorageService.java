package fmi.unibuc.ro.mycloudapi.service;

import fmi.unibuc.ro.mycloudapi.properties.FileStorageProperties;
import fmi.unibuc.ro.mycloudapi.encryption.EncryptionUtils;
import fmi.unibuc.ro.mycloudapi.exception.storage.FileStorageException;
import fmi.unibuc.ro.mycloudapi.payload.request.DirectorySpecification;
import fmi.unibuc.ro.mycloudapi.payload.request.SimpleFileSpecification;
import fmi.unibuc.ro.mycloudapi.payload.request.UploadFileSpecification;
import fmi.unibuc.ro.mycloudapi.util.AuthenticationUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FileStorageService {

    private Path fileStorageLocation;
    private final FileStorageProperties fileStorageProperties;
    private final EncryptionUtils multipleEncryptionUtils;
    private final fmi.unibuc.ro.mycloudapi.util.FileUtils fileUtils;
    private final AuthenticationUtil authenticationUtil;

    @Autowired
    public FileStorageService(
            FileStorageProperties fileStorageProperties,
            EncryptionUtils multipleEncryptionUtils,
            fmi.unibuc.ro.mycloudapi.util.FileUtils fileUtils,
            AuthenticationUtil authenticationUtil
    ) {
        this.fileStorageProperties = fileStorageProperties;
        this.authenticationUtil = authenticationUtil;

        this.multipleEncryptionUtils = multipleEncryptionUtils;
        this.fileUtils = fileUtils;
    }

    public void initContext() {
        final String userStorageFolder = fileStorageProperties.getUploadDir() + File.separator + authenticationUtil.getLoggedInUserEmail();
        this.fileStorageLocation = Paths.get(userStorageFolder)
                .toAbsolutePath()
                .normalize();

        File f = new File(userStorageFolder);
        if (f.exists()) {
            log.debug("Directory structure already initialized for {}", authenticationUtil.getLoggedInUserEmail());
            return;
        }

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFileSecurely(UploadFileSpecification uploadFileSpecification) {
        MultipartFile file = uploadFileSpecification.getFile();
        log.warn("Breadcrumb for store file ");

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            final byte[] multipartFileAESEncryptedBytes = multipleEncryptionUtils.encryptFileWithAES(uploadFileSpecification);
            fileUtils.storeFile(uploadFileSpecification, multipartFileAESEncryptedBytes);
            return fileName;
        } catch (Exception ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(SimpleFileSpecification simpleFileSpecification) throws FileNotFoundException {
        String fileName = simpleFileSpecification.getFilename();
        try {
            final byte[] decryptFileWithAES = multipleEncryptionUtils.decryptFileWithAES(simpleFileSpecification);

            File temp = new File(fileName);
            FileUtils.writeByteArrayToFile(temp, decryptFileWithAES);

            Resource resource = new UrlResource(temp.toPath().toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (IOException ex) { // todo: Repair this shit
            throw new FileNotFoundException("File not found " + fileName);
        }
    }


    @SneakyThrows
    public boolean removeFile(SimpleFileSpecification simpleFileSpecification) {
        Path targetLocation = fileUtils.getStorageLocation(simpleFileSpecification);
        String fileName = FilenameUtils.getBaseName(simpleFileSpecification.getFilename());

        Path keysPath = fileUtils.getKeysLocation(simpleFileSpecification);

        try {
            File f = new File(targetLocation.toUri());
            if (f.isDirectory()) {
                FileUtils.deleteDirectory(f);
                log.warn("Folder {} removed", fileName);
            } else {
                Files.deleteIfExists(targetLocation);
                log.warn("File {} removed", fileName);
            }

            FileUtils.deleteDirectory(new File(keysPath.toUri()));
            log.warn("Integrity files for {} removed", fileName);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<File> listAllAvailableFiles() {
        try {
            File f = new File(fileUtils.getStorageLocation().toUri());
            if (f.isDirectory() && f.listFiles() != null) {
                File[] directories = f.listFiles();
                return Arrays.asList(directories);
            }
            throw new IOException();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean createDir(DirectorySpecification directorySpecification) {
        final Path userStorageLocation = fileUtils.getStorageLocation(directorySpecification);
        final Path userKeysLocation = fileUtils.getKeysLocation(directorySpecification);
        try {
            Files.createDirectories(userStorageLocation);
            Files.createDirectories(userKeysLocation);
            return true;
        } catch (IOException e) {
            log.error("Could not create folder: " + directorySpecification.getFolderName());
            return false;
        }
    }

    public File[] getFolderContent(DirectorySpecification directorySpecification) {
        final Path userStorageLocation = fileUtils.getStorageLocation(directorySpecification);
        File file = new File(userStorageLocation.toUri());
        if (file.isDirectory()) {
            return file.listFiles();
        } else {
            throw new InvalidPathException("{} is not a directory", directorySpecification.getFolderName());
        }
    }
}
