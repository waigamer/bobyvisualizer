package com.galdevs.resistenciasysoportes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.galdevs.resistenciasysoportes.entities.Candle;





public interface CandleRepository extends JpaRepository<Candle, Integer> {
	
	List<Candle> findByOpenTimeStampBetween(long from, long to);

	Candle findByInstId(String InstId);
	
	List<Candle> findAll();
	
	List<Candle> findAllByOpenTimeStamp(Long openTimeStamp);
	
	List<Candle> findByPkBetween(long from, long to);
	
	@Query("SELECT DISTINCT instId FROM candle_order ")
	List<String> findDistinctInstId();
	
	@Query("SELECT DISTINCT openTimeStamp FROM candle_order order by openTimeStamp")
	List<Long> findDistinctOpen();
	
	long count();
	
	long countByOpenTimeStampBetween(long from, long to);
	long countByOpenTimeStampGreaterThan(long from);
	long countByOpenTimeStampLessThan(long to);
	
	public Candle findFirstByOpenTimeStamp (long timeStamp);
	
}

