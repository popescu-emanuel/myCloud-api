package fmi.unibuc.ro.mycloudapi.payload.request;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class DirectorySpecification {
    @Size(min = 1, message
            = "Folder name must contain at least 1 character")
    String folderName;

    List<String> breadcrumb;
}
