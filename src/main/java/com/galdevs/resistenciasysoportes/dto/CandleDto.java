package com.galdevs.resistenciasysoportes.dto;

import java.util.ArrayList;
import java.util.List;

import com.galdevs.resistenciasysoportes.entities.Candle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandleDto {

	
	private Arg arg;

	private List<List<String>> data = new ArrayList<>(0);
	
	
	public CandleDto( Candle candle) {

		this.arg= new Arg(null, candle.getInstId());
		List<String> datos= new ArrayList<String>(0);
		datos.add(candle.getOpenTime());
		datos.add(candle.getOpen());
		datos.add(candle.getHigh());
		datos.add(candle.getLow());
		datos.add(candle.getClose());
		datos.add(candle.getVolume());
		datos.add("close_time");
		datos.add(candle.getQuoteVolume());
		datos.add("0");
		this.data.add(datos);
		
		
	}
	
}
