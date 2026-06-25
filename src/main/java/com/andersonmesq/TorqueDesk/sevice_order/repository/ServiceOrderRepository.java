package com.andersonmesq.TorqueDesk.sevice_order.repository;

import com.andersonmesq.TorqueDesk.sevice_order.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {

}