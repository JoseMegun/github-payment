package pe.edu.vallegrande.vg_ms_payment.application.Service.impl;

import pe.edu.vallegrande.vg_ms_payment.domain.Model.Payment;
import pe.edu.vallegrande.vg_ms_payment.domain.dto.ManagerDTO;
import pe.edu.vallegrande.vg_ms_payment.domain.Repository.PaymentRepository;
import pe.edu.vallegrande.vg_ms_payment.application.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import static pe.edu.vallegrande.vg_ms_payment.application.Util.StatusConstants.ACTIVE;
import static pe.edu.vallegrande.vg_ms_payment.application.Util.StatusConstants.INACTIVE;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, WebClient.Builder webClientBuilder) {
        this.paymentRepository = paymentRepository;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Flux<Payment> listAllActive() {
        log.info("Listando todos los pagos activos");
        return paymentRepository.findByStatus(ACTIVE)
                .doOnComplete(() -> log.info("Finalizó la lista de pagos activos"))
                .doOnError(error -> log.error("Error al listar pagos activos: {}", error.getMessage(), error));
    }

    @Override
    public Flux<Payment> listAllInactive() {
        log.info("Listando todos los pagos inactivos");
        return paymentRepository.findByStatus(INACTIVE)
                .doOnComplete(() -> log.info("Finalizó la lista de pagos inactivos"))
                .doOnError(error -> log.error("Error al listar pagos inactivos: {}", error.getMessage(), error));
    }

    @Override
    public Flux<Payment> findByClassName(String className) {
        log.info("Buscando pagos por clase: {}", className);
        return paymentRepository.findByClassName(className)
                .doOnComplete(() -> log.info("Finalizó la búsqueda de pagos por clase: {}", className))
                .doOnError(error -> log.error("Error al buscar pagos por clase: {}. Error: {}", className, error.getMessage(), error));
    }

    @Override
    public Mono<Payment> createPayment(Payment payment) {
        log.info("Iniciando creación de un nuevo pago: {}", payment);
        if (payment.getManager() == null || payment.getManager().getId() == null) {
            log.error("Manager ID es nulo, no se puede crear el pago.");
            return Mono.error(new IllegalArgumentException("Manager es requerido para la creación del pago."));
        }

        return getManagerDetails(payment.getManager().getId())
                .doOnNext(manager -> log.info("Detalles del Manager obtenidos: {}", manager))
                .doOnError(error -> log.error("Error al obtener detalles del Manager: {}", error.getMessage(), error))
                .flatMap(managerDetails -> {
                    payment.setManager(managerDetails);
                    payment.setStatus(ACTIVE);
                    log.debug("Configurando y guardando el pago con estado ACTIVO.");
                    return paymentRepository.save(payment)
                            .doOnSuccess(saved -> log.info("Pago guardado exitosamente con ID: {}", saved.getId()))
                            .doOnError(error -> log.error("Error al guardar el pago: {}", error.getMessage(), error));
                })
                .onErrorResume(e -> {
                    log.error("Error en la creación del pago. Detalle: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Error al crear el pago."));
                });
    }

    @Override
    public Mono<Payment> updatePayment(String id, Payment payment) {
        log.info("Iniciando actualización del pago con ID: {}", id);
        return paymentRepository.findById(id)
                .doOnSuccess(existingPayment -> {
                    if (existingPayment != null) {
                        log.info("Pago encontrado para actualización: {}", existingPayment);
                    } else {
                        log.warn("No se encontró un pago con ID: {}", id);
                    }
                })
                .flatMap(existingPayment -> {
                    log.debug("Actualizando detalles del pago con ID: {}", id);
                    existingPayment.setDescription(payment.getDescription());
                    existingPayment.setDueDate(payment.getDueDate());
                    existingPayment.setAmount(payment.getAmount());
                    existingPayment.setClassName(payment.getClassName());
                    return getManagerDetails(payment.getManager().getId())
                            .doOnNext(managerDetails -> log.info("Detalles del Manager actualizados: {}", managerDetails))
                            .flatMap(managerDetails -> {
                                existingPayment.setManager(managerDetails);
                                return paymentRepository.save(existingPayment)
                                        .doOnSuccess(updated -> log.info("Pago actualizado exitosamente con ID: {}", updated.getId()))
                                        .doOnError(error -> log.error("Error al guardar el pago actualizado: {}", error.getMessage(), error));
                            });
                })
                .doOnError(e -> log.error("Error al actualizar el pago con ID: {}. Error: {}", id, e.getMessage(), e));
    }

    @Override
    public Mono<Payment> deletePayment(String id) {
        log.info("Iniciando eliminación lógica del pago con ID: {}", id);
        return paymentRepository.findById(id)
                .flatMap(existingPayment -> {
                    log.debug("Marcando pago como INACTIVO con ID: {}", id);
                    existingPayment.setStatus(INACTIVE);
                    return paymentRepository.save(existingPayment)
                            .doOnSuccess(saved -> log.info("Pago marcado como INACTIVO con ID: {}", saved.getId()))
                            .doOnError(error -> log.error("Error al guardar el pago como INACTIVO: {}", error.getMessage(), error));
                })
                .doOnError(e -> log.error("Error al eliminar el pago con ID: {}. Error: {}", id, e.getMessage(), e));
    }

    @Override
    public Mono<Payment> findById(String id) {
        log.info("Buscando pago por ID: {}", id);
        return paymentRepository.findById(id)
                .doOnSuccess(payment -> {
                    if (payment != null) {
                        log.info("Pago encontrado con ID: {}", id);
                    } else {
                        log.warn("No se encontró pago con ID: {}", id);
                    }
                })
                .doOnError(e -> log.error("Error al buscar pago por ID: {}. Error: {}", id, e.getMessage(), e));
    }

    @Override
    public Mono<Payment> reactivatePayment(String id) {
        log.info("Iniciando reactivación del pago con ID: {}", id);
        return paymentRepository.findById(id)
                .flatMap(existingPayment -> {
                    if (INACTIVE.equals(existingPayment.getStatus())) {
                        log.debug("Pago encontrado como INACTIVO. Reactivando...");
                        existingPayment.setStatus(ACTIVE);
                        return paymentRepository.save(existingPayment)
                                .doOnSuccess(saved -> log.info("Pago reactivado exitosamente con ID: {}", saved.getId()))
                                .doOnError(error -> log.error("Error al reactivar el pago: {}", error.getMessage(), error));
                    }
                    log.warn("Pago con ID: {} ya estaba activo. No se realizaron cambios.", id);
                    return Mono.just(existingPayment);
                })
                .doOnError(e -> log.error("Error al reactivar el pago con ID: {}. Error: {}", id, e.getMessage(), e));
    }

    private Mono<ManagerDTO> getManagerDetails(String managerId) {
        log.debug("Obteniendo detalles del Manager con ID: {}", managerId);
        return webClientBuilder.build()
                .get()
                .uri("https://advisory-jessalin-proyect-angel2025-b0b6aa9e.koyeb.app/public/manager/api/v1/" + managerId)
                .retrieve()
                .bodyToMono(ManagerDTO.class)
                .doOnError(e -> log.error("Error al obtener detalles del Manager con ID: {}. Error: {}", managerId, e.getMessage(), e));
    }
}
