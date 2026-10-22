package com.andersonmesq.TorqueDesk.unit.vehicle.service;

import com.andersonmesq.TorqueDesk.customer.exception.CustomerNotFoundException;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.vehicle.dto.CreateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.dto.UpdateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.dto.VehicleResponse;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleAlreadyExistException;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException;
import com.andersonmesq.TorqueDesk.vehicle.mapper.VehicleMapper;
import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import com.andersonmesq.TorqueDesk.vehicle.service.VehicleService;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {
    @Mock
    CustomerRepository customerRepository;

    @Mock
    VehicleRepository vehicleRepository;

    @Mock
    VehicleMapper vehicleMapper;

    @InjectMocks
    VehicleService vehicleService;

    @Test
    void shouldCreateVehicleWhenRequestIsValid() {
        UUID customerId = UUID.randomUUID();
        CreateVehicleRequest request = new CreateVehicleRequest(
                customerId,
                "legacy",
                "Subaru",
                "Subaru Legacy BD 2.0",
                1994,
                "1234674987"
        );
        Customer customer = Customer.builder()
                .id(customerId)
                .build();
        VehicleResponse expectedResponse = new VehicleResponse(
                UUID.randomUUID(),
                request.brand(),
                request.model(),
                request.modelYear(),
                request.plate()
        );
        when(vehicleRepository.existsByPlateAndModel(request.plate(), request.model())).thenReturn(false);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(vehicleMapper.toResponse(any(Vehicle.class))).thenReturn(expectedResponse);

        VehicleResponse response = vehicleService.createVehicle(request);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        Vehicle vehicleSaved = captor.getValue();
        assertThat(vehicleSaved.getBrand()).isEqualTo(request.brand());
        assertThat(vehicleSaved.getModel()).isEqualTo(request.model());
        assertThat(vehicleSaved.getModelYear()).isEqualTo(request.modelYear());
        assertThat(vehicleSaved.getPlate()).isEqualTo(request.plate());
        assertThat(vehicleSaved.getCustomer()).isEqualTo(customer);
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void shouldThrowVehicleAlreadyExistExceptionWhenVehicleAlreadyExists() {
        UUID customerId = UUID.randomUUID();
        CreateVehicleRequest request = new CreateVehicleRequest(
                customerId,
                "legacy",
                "Subaru",
                "Subaru Legacy BD 2.0",
                1994,
                "1234674987"
        );
        when(vehicleRepository.existsByPlateAndModel(request.plate(), request.model())).thenReturn(true);

        ThrowableAssert.ThrowingCallable action = () -> vehicleService.createVehicle(request);

        assertThatThrownBy(action).isInstanceOf(VehicleAlreadyExistException.class).hasMessage("Vehicle already exists");
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        CreateVehicleRequest request = new CreateVehicleRequest(
                customerId,
                "legacy",
                "Subaru",
                "Subaru Legacy BD 2.0",
                1994,
                "1234674987"
        );
        when(vehicleRepository.existsByPlateAndModel(request.plate(), request.model())).thenReturn(false);
        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> vehicleService.createVehicle(request);

        assertThatThrownBy(action).isInstanceOf(CustomerNotFoundException.class).hasMessage("Customer not found");
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldReturnVehicleWhenVehicleIdExists() {
        UUID vehicleId = UUID.randomUUID();
        Vehicle vehicle = Vehicle.builder()
                .id(vehicleId)
                .build();
        VehicleResponse expectedResponse = new VehicleResponse(
                UUID.randomUUID(),
                "Subaru",
                "Subaru Legacy BD 2.0",
                1994,
                "1234674987"
        );
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponse(any(Vehicle.class))).thenReturn(expectedResponse);

        VehicleResponse response = vehicleService.findById(vehicleId);

        assertThat(response).isEqualTo(expectedResponse);
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleMapper).toResponse(vehicle);
    }

    @Test
    void shouldThrowVehicleNotFoundExceptionWhenVehicleIdDoesNotExist() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> vehicleService.findById(vehicleId);

        assertThatThrownBy(action).isInstanceOf(VehicleNotFoundException.class).hasMessage("Vehicle not found");
        verify(vehicleMapper, never()).toResponse(any(Vehicle.class));
    }

    @Test
    void shouldReturnVehicleWhenVehiclePlateExists() {
        UUID vehicleId = UUID.randomUUID();
        Vehicle vehicle = Vehicle.builder()
                .id(vehicleId)
                .plate("1234674987")
                .build();
        VehicleResponse expectedResponse = new VehicleResponse(
                UUID.randomUUID(),
                "Subaru",
                "Subaru Legacy BD 2.0",
                1994,
                "1234674987"
        );
        when(vehicleRepository.findByPlate("1234674987")).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponse(any(Vehicle.class))).thenReturn(expectedResponse);

        VehicleResponse response = vehicleService.findByPlate(vehicle.getPlate());

        assertThat(response).isEqualTo(expectedResponse);
        verify(vehicleRepository).findByPlate(vehicle.getPlate());
        verify(vehicleMapper).toResponse(vehicle);
    }

    @Test
    void shouldThrowVehicleNotFoundExceptionWhenVehiclePlateDoesNotExist() {
        String vehiclePlate = "1234674987";
        when(vehicleRepository.findByPlate(vehiclePlate)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> vehicleService.findByPlate(vehiclePlate);

        assertThatThrownBy(action).isInstanceOf(VehicleNotFoundException.class).hasMessage("Vehicle not found");
        verify(vehicleMapper, never()).toResponse(any(Vehicle.class));
    }

    @Test
    void shouldReturnVehicleWhenVehicleModelExists() {
        UUID vehicleId = UUID.randomUUID();
        Vehicle vehicle = Vehicle.builder()
                .id(vehicleId)
                .model("Subaru Legacy BD 2.0")
                .build();
        VehicleResponse expectedResponse = new VehicleResponse(
                UUID.randomUUID(),
                "Subaru",
                "Subaru Legacy BD 2.0",
                1994,
                "1234674987"
        );
        when(vehicleRepository.findByModel(vehicle.getModel())).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponse(any(Vehicle.class))).thenReturn(expectedResponse);

        VehicleResponse response = vehicleService.findByModel(vehicle.getModel());

        assertThat(response).isEqualTo(expectedResponse);
        verify(vehicleRepository).findByModel(vehicle.getModel());
        verify(vehicleMapper).toResponse(vehicle);
    }

    @Test
    void shouldThrowVehicleNotFoundExceptionWhenVehicleModelDoesNotExist() {
        String vehicleModel = "Subaru Legacy BD 2.0";
        when(vehicleRepository.findByModel(vehicleModel)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> vehicleService.findByModel(vehicleModel);

        assertThatThrownBy(action).isInstanceOf(VehicleNotFoundException.class).hasMessage("Vehicle not found");
        verify(vehicleMapper, never()).toResponse(any(Vehicle.class));
    }

    @Test
    void shouldUpdateVehiclePlateWhenRequestIsValid() {
        UUID vehicleId = UUID.randomUUID();
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "1234674987"
        );
        Vehicle vehicle = Vehicle.builder()
                .id(vehicleId)
                .build();
        VehicleResponse expectedResponse = new VehicleResponse(
                UUID.randomUUID(),
                "Subaru",
                "Subaru Legacy BD 2.0",
                1994,
                "new-plate"
        );
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponse(any(Vehicle.class))).thenReturn(expectedResponse);

        VehicleResponse response = vehicleService.updateVehicle(vehicleId, request);

        assertThat(vehicle.getPlate()).isEqualTo(request.plate());
        assertThat(response).isEqualTo(expectedResponse);
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleMapper).toResponse(vehicle);
    }
}