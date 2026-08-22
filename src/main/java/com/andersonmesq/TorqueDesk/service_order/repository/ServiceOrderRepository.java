package com.andersonmesq.TorqueDesk.service_order.repository;

import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {

}