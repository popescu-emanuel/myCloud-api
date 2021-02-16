package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.properties.FileStorageProperties;
import fmi.unibuc.ro.mycloudapi.payload.request.SimpleFileSpecification;
import io.jsonwebtoken.lang.Assert;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * TODO:
 * 1. Create files before tests
 * 2. Run tests
 * 3. Remove files
 */
@SpringBootTest
@Slf4j
public class FileUtilsTests {

    static SimpleFileSpecification simpleFileSpecification;
    private String CLOUD_STORAGE_EMAIL_TEMPLATE;
    private String CLOUD_KEYS_EMAIL_TEMPLATE;

    @Autowired
    FileStorageProperties fileStorageProperties;

    String email = "test@gmail.com";

    @BeforeAll
    public static void initData() {
        simpleFileSpecification = new SimpleFileSpecification();
        simpleFileSpecification.setFilename("fisierTest.txt");
        simpleFileSpecification.setBreadcrumb(Arrays.asList("folder1", "folder2"));
    }

    @BeforeEach
    public void initPaths() {
        // uploadDir/email
        CLOUD_STORAGE_EMAIL_TEMPLATE = fileStorageProperties.getUploadDir() + File.separator + "%s";

        // keysDir/email
        CLOUD_KEYS_EMAIL_TEMPLATE = fileStorageProperties.getKeysDir() + File.separator + "%s";
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @SneakyThrows
    void givenStorageLocation_thenProvideMockData() {
        String storageLocation = String.format(CLOUD_STORAGE_EMAIL_TEMPLATE, email);
        Path path = Path.of(storageLocation);

        Path oneDocument = path.resolve("folderTest");
        Files.createDirectories(oneDocument);

        oneDocument = oneDocument.resolve("file1.txt");
        File doc = new File(oneDocument.toUri());

        log.info("{}", oneDocument.toUri());
        boolean wasCreated = doc.createNewFile();

        Assert.isTrue(wasCreated);

        final boolean wasDeleted = Files.deleteIfExists(oneDocument);
        Assert.isTrue(wasDeleted);
    }

    @Test
    void givenEmail_thenProvideStorageLocation() {
        String storageLocation = String.format(CLOUD_STORAGE_EMAIL_TEMPLATE, email);
        Path path = Path.of(storageLocation);
        log.info("{}", Path.of(storageLocation));
        Assert.isTrue(Files.exists(path));
    }

    @Test
    void givenEmail_thenProvideKeysLocation() {
        String storageLocation = String.format(CLOUD_KEYS_EMAIL_TEMPLATE, email);
        Path path = Path.of(storageLocation);
        log.info("{}", Path.of(storageLocation));
        Assert.isTrue(Files.exists(path));
    }

    @Test
    void givenEmailAndFileSpecification_thenProvideStorageLocation() {
        String storageLocation = String.format(CLOUD_STORAGE_EMAIL_TEMPLATE, email);
        Path path = Path.of(storageLocation);

        for (String folder : simpleFileSpecification.getBreadcrumb()) {
            path = path.resolve(folder);
        }

        log.info("{}", path);
        Assert.isTrue(Files.exists(path));
    }

    @Test
    void givenEmailAndFileSpecification_thenProvideKeysLocation() {
        String storageLocation = String.format(CLOUD_KEYS_EMAIL_TEMPLATE, email);
        Path path = Path.of(storageLocation);

        for (String folder : simpleFileSpecification.getBreadcrumb()) {
            path = path.resolve(folder);
        }

        String filename = FilenameUtils.getBaseName(simpleFileSpecification.getFilename());
        path = path.resolve(filename);

        log.info("{}", path);
        Assert.isTrue(Files.exists(path));
    }


}
