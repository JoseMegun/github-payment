package pe.edu.vallegrande.vg_ms_payment.application.Service;

import pe.edu.vallegrande.vg_ms_payment.domain.Model.Payment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentService {
    Flux<Payment> listAllActive();
    Flux<Payment> listAllInactive();
    Flux<Payment> findByClassName(String className);
    Mono<Payment> createPayment(Payment payment);
    Mono<Payment> updatePayment(String id, Payment payment);
    Mono<Payment> deletePayment(String id);
    Mono<Payment> findById(String id);
    // Nuevo método para reactivar un pago
    Mono<Payment> reactivatePayment(String id);
}
