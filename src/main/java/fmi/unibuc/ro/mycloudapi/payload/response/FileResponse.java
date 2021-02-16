package fmi.unibuc.ro.mycloudapi.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {

    private String filename;
    private String type;
    private String size;
    private String uploadDate;

}
