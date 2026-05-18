package com.donaton.donation.repository;

import com.donaton.donation.model.DonationModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository
        extends JpaRepository<DonationModel, Long> {
}