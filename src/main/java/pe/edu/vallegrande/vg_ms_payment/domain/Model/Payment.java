package pe.edu.vallegrande.vg_ms_payment.domain.Model;

import pe.edu.vallegrande.vg_ms_payment.domain.dto.ManagerDTO;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Setter
@Getter
@Document(collection = "payments")
public class Payment {
    // Getters y setters
    @Id
    private String id;
    private ManagerDTO manager; // Almacena detalles completos del Manager
    private String description;
    private LocalDate dueDate;
    private LocalDate date;
    private String amount;
    private String status;
    private String className;
}