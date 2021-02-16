package fmi.unibuc.ro.mycloudapi.payload.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UploadFileSpecification implements Serializable {
    MultipartFile file;
    List<String> breadcrumb = new ArrayList<>();
}
