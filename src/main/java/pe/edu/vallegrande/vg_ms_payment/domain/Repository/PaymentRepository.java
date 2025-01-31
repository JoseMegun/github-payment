package pe.edu.vallegrande.vg_ms_payment.domain.Repository;

import pe.edu.vallegrande.vg_ms_payment.domain.Model.Payment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> {
    Flux<Payment> findByStatus(String status);
    Flux<Payment> findByClassName(String className);
}
