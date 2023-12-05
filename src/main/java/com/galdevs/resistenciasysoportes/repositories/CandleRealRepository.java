package com.galdevs.resistenciasysoportes.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.galdevs.resistenciasysoportes.entities.CandleReal;





public interface CandleRealRepository extends JpaRepository<CandleReal, Integer> {
	
	List<CandleReal> findByOpenTimeStampBetween(long from, long to);
	List<CandleReal> findByInstIdAndOpenTimeStampBetween(String InstId, long from, long to);
	List<CandleReal> findByInstIdAndOpenTimeStampBetween(String instId, long from, long to, Sort sort);

	CandleReal findByInstId(String InstId);
	
	List<CandleReal> findAll();
	
	List<CandleReal> findAllByOpenTimeStamp(Long openTimeStamp);
	
	List<CandleReal> findByPkBetween(long from, long to);
	
	@Query("SELECT DISTINCT instId FROM candle_real")
	List<String> findDistinctInstId();
	
	@Query("SELECT DISTINCT openTimeStamp FROM candle_real order by openTimeStamp")
	List<Long> findDistinctOpen();
	
	CandleReal save(CandleReal candleReal);
	
	long count();
}

