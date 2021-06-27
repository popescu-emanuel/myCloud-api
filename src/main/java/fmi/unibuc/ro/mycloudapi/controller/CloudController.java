package fmi.unibuc.ro.mycloudapi.controller;

import fmi.unibuc.ro.mycloudapi.encryption.EncryptionUtils;
import fmi.unibuc.ro.mycloudapi.payload.request.DirectorySpecification;
import fmi.unibuc.ro.mycloudapi.payload.request.ResetPasswordRequest;
import fmi.unibuc.ro.mycloudapi.payload.request.SimpleFileSpecification;
import fmi.unibuc.ro.mycloudapi.payload.request.UploadFileSpecification;
import fmi.unibuc.ro.mycloudapi.payload.response.FileResponse;
import fmi.unibuc.ro.mycloudapi.payload.response.MemoryAllocationResponse;
import fmi.unibuc.ro.mycloudapi.payload.response.UploadFileResponse;
import fmi.unibuc.ro.mycloudapi.service.FileStorageService;
import fmi.unibuc.ro.mycloudapi.service.UserService;
import fmi.unibuc.ro.mycloudapi.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RestController
@SuppressWarnings("rawtypes")
@RequestMapping("/api/cloud")
public class CloudController {

    private final FileStorageService fileStorageService;
    private final EncryptionUtils encryptionUtils;
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;

    @PostMapping("/uploadFiles")
    public ResponseEntity<String> uploadMultipleFiles(@RequestBody UploadFileSpecification[] files) {
        Arrays.stream(files)
                .map(this::uploadFile)
                .collect(Collectors.toList());
        logUploadedFiles(files);
        return ResponseEntity
                .accepted()
                .body("Files successfully uploaded");
    }

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity uploadFile(@ModelAttribute UploadFileSpecification uploadFileSpecification) {
        MultipartFile file = uploadFileSpecification.getFile();
        String fileName = fileStorageService.storeFileSecurely(uploadFileSpecification);

        // todo: remove files after move to server upload path
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        log.info("File now available at: {}", fileDownloadUri);
        final UploadFileResponse response = new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
        return ResponseEntity
                .accepted()
                .body(response);
    }

    @PostMapping(value = "/reset", consumes = {"application/json"})
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            final boolean checkPassword = userService.checkPassword(authenticationUtil.getLoggedInUserEmail(), resetPasswordRequest.getOldPassword());
            if (!checkPassword) {
                log.warn("Password does not match");
                return ResponseEntity.badRequest().build();
            }
            encryptionUtils.recomputeIntegrityKeysForAllFiles(authenticationUtil.getLoggedInUserEmail(), resetPasswordRequest.getNewPassword());
            return ResponseEntity.ok("Password reset");
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }

    }

    @PostMapping(value = "/download", consumes = {"application/json"})
    public ResponseEntity downloadFile(@RequestBody SimpleFileSpecification simpleFileSpecification, HttpServletRequest request)
            throws FileNotFoundException {
        // Load file as Resource
        String fileName = simpleFileSpecification.getFilename();
        Resource resource = fileStorageService.loadFileAsResource(simpleFileSpecification);

        // Try to determine file's content type
        String contentType = determineFileContent(request, resource);

        // Fallback to the default content type if type could not be determined
        contentType = checkIfDefaultContentTypeIsNeeded(contentType);

        log.warn("File {} downloaded successfully", fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping(value = "/delete", consumes = {"application/json"})
    public ResponseEntity<String> removeFile(@RequestBody SimpleFileSpecification simpleFileSpecification) {
        final boolean wasFileRemoved = fileStorageService.removeFile(simpleFileSpecification);
        String fileName = FilenameUtils.getBaseName(simpleFileSpecification.getFilename());

        String message;
        if (wasFileRemoved) {
            message = String.format("File %s was successfully removed", fileName);
            return ResponseEntity.ok(message);
        } else {
            message = String.format("File %s could not be removed", fileName);
            return ResponseEntity.badRequest().body(message);
        }
    }

    @GetMapping("/list")
    public List<FileResponse> listAllStoredFiles() {
        return fileStorageService
                .listAllAvailableFiles()
                .stream()
                .map(this::mapFileToFileResponse)
                .collect(Collectors.toList());
    }

    @PostMapping(value = "/list")
    public ResponseEntity listFolderContent(@RequestBody DirectorySpecification directorySpecification) {
        log.info("List folder content " + directorySpecification.getBreadcrumb());
        try {
            File[] f = fileStorageService.getFolderContent(directorySpecification);
            List<FileResponse> fr = Arrays.stream(f)
                    .map(this::mapFileToFileResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(fr);
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/createdir", consumes = {"application/json"})
    public ResponseEntity createDirectory(@RequestBody DirectorySpecification directorySpecification) {
        final boolean wasDirCreated = fileStorageService.createDir(directorySpecification);
        breadcrumbToFolderPath(directorySpecification.getBreadcrumb());
        String message;
        if (wasDirCreated) {
            message = String.format("Directory %s/%s created",
                    directorySpecification.getBreadcrumb(),
                    directorySpecification.getFolderName());
            return ResponseEntity.ok(message);
        } else {
            message = String.format("Directory %s/%s could not be created",
                    directorySpecification.getBreadcrumb(),
                    directorySpecification.getFolderName());
            return ResponseEntity.badRequest().body(message);
        }
    }

    @PostMapping(value = "/size")
    public ResponseEntity getMemoryAllocationData() {
        try {
            final MemoryAllocationResponse dataUsage = userService.getDataUsage(
                    authenticationUtil.getLoggedInUserEmail()
            );
            return ResponseEntity.ok(dataUsage);
        } catch (IOException exception) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not process the request. Please try again later");
        }
    }

    @GetMapping(value = "/size")
    public ResponseEntity getSizePlans() {
        return ResponseEntity
                .ok()
                .body(userService.getSubscriptionTypes());
    }

    private String breadcrumbToFolderPath(List<String> breadcrumb) {
        StringBuilder sb = new StringBuilder();
        for (String folder : breadcrumb) {
            sb
                    .append(File.separator)
                    .append(folder);
        }
        return sb.toString();
    }

    private FileResponse mapFileToFileResponse(File file) {
        FileResponse fr = new FileResponse();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try {
            String uploadDate = getFormattedFileDate(file.toPath());
            fr.setUploadDate(uploadDate);
        } catch (IOException e) {
//            final String now = LocalDateTime.now().toString();
//            LocalDateTime dateTime = LocalDateTime.parse(now, formatter);
            fr.setUploadDate("N/A");
        }

        fr.setFilename(FilenameUtils.getBaseName(file.getName()));

        if (file.isDirectory()) {
            fr.setType("folder");
            fr.setSize(FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(file)));
        } else {
            fr.setType(FilenameUtils.getExtension(file.getName()));
            fr.setSize(FileUtils.byteCountToDisplaySize(file.length()));
        }
        return fr;
    }

    private String getFormattedFileDate(Path path) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        long cTime = attr.creationTime().toMillis();
        ZonedDateTime t = Instant.ofEpochMilli(cTime).atZone(ZoneId.of("UTC"));
        String dateCreated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(t);
        return dateCreated;
    }

    private BasicFileAttributes getFileAttributes(File file) throws IOException {
        try {
            return Files.readAttributes(
                    file.toPath(),
                    BasicFileAttributes.class);
        } catch (IOException e) {
            log.error("Could not retrieve file attributes for {}", file.getName());
            throw new IOException();
        }
    }

    private String checkIfDefaultContentTypeIsNeeded(String contentType) {
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }

    private String determineFileContent(HttpServletRequest request, Resource resource) {
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }
        return contentType;
    }


    private void logUploadedFiles(UploadFileSpecification[] files) {
        log.warn("{} file uploaded", files.length);
        for (UploadFileSpecification file : files) {
            log.warn("\t {}", file.getFile().getOriginalFilename());
        }
    }
}
