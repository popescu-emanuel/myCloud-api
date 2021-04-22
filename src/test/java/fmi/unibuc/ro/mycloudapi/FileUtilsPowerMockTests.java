package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.properties.FileStorageProperties;
import fmi.unibuc.ro.mycloudapi.constant.EncryptionKeyType;
import fmi.unibuc.ro.mycloudapi.payload.request.SimpleFileSpecification;
import fmi.unibuc.ro.mycloudapi.util.AuthenticationUtil;
import fmi.unibuc.ro.mycloudapi.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

@SpringBootTest
@Slf4j
@TestPropertySource(locations= "classpath:application.properties")
public class FileUtilsPowerMockTests {

    private static SimpleFileSpecification simpleFileSpecification;

    String email = "test@gmail.com";

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Mock
    private AuthenticationUtil authenticationUtil;

    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeAll
    public static void initData() {
        simpleFileSpecification = new SimpleFileSpecification();
        simpleFileSpecification.setFilename("fisierTest.txt");
        simpleFileSpecification.setBreadcrumb(Arrays.asList("folder1", "folder2"));
    }

    @Test
    void givenFolder_thenProvideEncKeys(){
        Path p = Path.of(fileStorageProperties.getKeysDir(), email);
        for(String f : simpleFileSpecification.getBreadcrumb()){
            p = p.resolve(f);
        }
        String basename = FilenameUtils.getBaseName(simpleFileSpecification.getFilename());
        p = p.resolve(basename);
        p = p.resolve(EncryptionKeyType.CK.value);

        File f = new File(p.toUri());
        log.warn("{}", f.toPath());
    }

}
