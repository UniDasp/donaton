package com.bff.service;

import com.bff.client.DonationClient;
import com.bff.client.LogisticsClient;
import com.bff.client.NeedsClient;
import com.bff.dto.request.DonationRequest;
import com.bff.dto.request.LogisticsRequest;
import com.bff.dto.response.DonationResponse;
import com.bff.dto.response.LogisticsResponse;
import com.bff.dto.response.NeedsResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;


@Service
public class DonationService {

    private static final Logger logger = Logger.getLogger(DonationService.class.getName());

    private final DonationClient donationClient;
    private final NeedsClient needsClient;
    private final LogisticsClient logisticsClient;

  
    public DonationService(
            DonationClient donationClient,
            NeedsClient needsClient,
            LogisticsClient logisticsClient
    ) {
        this.donationClient = donationClient;
        this.needsClient = needsClient;
        this.logisticsClient = logisticsClient;
    }

    
    public List<DonationResponse> list() {
        return donationClient.list();
    }

    
    public DonationResponse get(Long id) {
        return donationClient.get(id);
    }

  
    public DonationResponse create(DonationRequest request) {
        logger.info("Iniciando creación de donación para necesidad: " + request.getNeedId());

        NeedsResponse need = validateNeedExists(request.getNeedId());
        logger.info("Necesidad validada: " + need.getId() + " - Categoría: " + need.getCategory());

        DonationResponse donation = donationClient.create(request);
        logger.info("Donación creada con ID: " + donation.getId());

        try {
            needsClient.receive(request.getNeedId(), request.getCantidad());
            logger.info("Cantidad recibida actualizada en necesidad");

            LogisticsRequest logisticsRequest = new LogisticsRequest();
            logisticsRequest.setDonacionId(donation.getId());
            
            LogisticsResponse logistics = logisticsClient.create(logisticsRequest);
            logger.info("Envío logístico creado con ID: " + logistics.getId());

            donation.setLogisticsId(logistics.getId());

        } catch (RuntimeException logisticsException) {
            logger.warning("Error creando envío logístico: " + logisticsException.getMessage());
            try {
                donationClient.delete(donation.getId());
                logger.warning("Donación eliminada por fallo en logística");
            } catch (Exception rollbackException) {
                logger.warning("Error al hacer rollback: " + rollbackException.getMessage());
            }
            throw new RuntimeException(
                "Error al crear la logística de la donación. Donación cancelada.",
                logisticsException
            );
        }

        logger.info("Donación completada exitosamente: " + donation.getId());
        return donation;
    }

   
    public DonationResponse update(Long id, DonationRequest request) {
        return donationClient.update(id, request);
    }

   
    public void delete(Long id) {
        donationClient.delete(id);
    }

    private NeedsResponse validateNeedExists(String needId) {
        try {
            NeedsResponse need = needsClient.get(needId);
            if (need == null) {
                throw new RuntimeException("Necesidad no encontrada: " + needId);
            }
            return need;
        } catch (RuntimeException ex) {
            logger.warning("Error validando necesidad: " + needId);
            throw new RuntimeException("No se pudo validar la necesidad. Verifique el ID.", ex);
        }
    }
}
