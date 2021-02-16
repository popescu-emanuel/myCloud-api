package fmi.unibuc.ro.mycloudapi.payload.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SimpleFileSpecification  implements Serializable {
    String filename;
    List<String> breadcrumb = new ArrayList<>();

}
