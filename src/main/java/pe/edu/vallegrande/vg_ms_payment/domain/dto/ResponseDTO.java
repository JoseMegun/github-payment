package pe.edu.vallegrande.vg_ms_payment.domain.dto;

import pe.edu.vallegrande.vg_ms_payment.domain.Model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    private Payment payment;
    private ManagerDTO managerDTO;
}
