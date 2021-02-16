package fmi.unibuc.ro.mycloudapi.payload.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
public class MemoryAllocationResponse {
    private final String totalSize;
    private final String freeSpace;
    private final String usedSpace;
}
