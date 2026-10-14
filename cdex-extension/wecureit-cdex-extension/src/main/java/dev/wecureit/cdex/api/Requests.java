package dev.wecureit.cdex.api;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public final class Requests {
 private Requests() {}
 public record Match(@NotBlank String firstName,@NotBlank String lastName,@NotNull LocalDate birthDate,String postalCode) {}
 public record CreateExchange(@NotBlank String memberId,@NotBlank String patientId,@NotBlank String reason) {}
}
