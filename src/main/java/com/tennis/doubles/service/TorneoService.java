package com.tennis.doubles.service;

import org.springframework.stereotype.Service;
import com.tennis.doubles.repository.TorneoRepository;

@Service
public class TorneoService {
	
	private final TorneoRepository torneoRepository;
	
	public TorneoService(TorneoRepository torneoRepository) {
        this.torneoRepository = torneoRepository;
    }

}
