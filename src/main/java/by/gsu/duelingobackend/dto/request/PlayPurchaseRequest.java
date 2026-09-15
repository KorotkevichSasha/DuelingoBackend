package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayPurchaseRequest(
        @NotBlank @Size(max = 80) String productId,
        @NotBlank @Size(max = 4096) String purchaseToken,
        @NotBlank @Size(max = 16384) String signedData,
        @NotBlank @Size(max = 4096) String signature
) {
}
