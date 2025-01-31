package pe.edu.vallegrande.vg_ms_payment.presentation.Controller;

import pe.edu.vallegrande.vg_ms_payment.domain.Model.Payment;
import pe.edu.vallegrande.vg_ms_payment.application.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/actives")
    public Flux<Payment> getAllActivePayments() {
        return paymentService.listAllActive();
    }

    @GetMapping("/inactives")
    public Flux<Payment> getAllInactivePayments() {
        return paymentService.listAllInactive();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Payment>> getPaymentById(@PathVariable String id) {
        return paymentService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public Mono<Payment> createPayment(@RequestBody Payment payment) {
        return paymentService.createPayment(payment);
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<Payment>> updatePayment(@PathVariable String id, @RequestBody Payment payment) {
        return paymentService.updatePayment(id, payment)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<Payment>> deletePayment(@PathVariable String id) {
        return paymentService.deletePayment(id)
                .map(ResponseEntity::ok)  // Usamos una referencia de método en lugar de una lambda
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/reactivate/{id}")
    public Mono<ResponseEntity<Payment>> reactivatePayment(@PathVariable String id) {
        return paymentService.reactivatePayment(id)
                .map(ResponseEntity::ok)  // Devolvemos el pago reactivado
                .defaultIfEmpty(ResponseEntity.notFound().build());  // Si no se encuentra, devuelve 404
    }

}
