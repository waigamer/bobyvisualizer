package com.bot.soportesyresistencias.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "candle_real_binance")
@Getter
@Setter
@NoArgsConstructor
public class CandleReal {

	
	@Column(unique = true, nullable = true)
	private long pk;
	
	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

		@Column(length = 20, nullable = true, unique = false)
		private String instId;
		
		@Column(length = 100, nullable = false, name="open_time", unique = false)
		private String openTime;
		
		@Column(length = 100, nullable = false, unique = false)
		private String open;

		@Column(length = 100, nullable = false, unique = false)
		private String high;
		
		@Column(length = 100, nullable = false, unique = false)
		private String low;
		
		@Column(length = 100, nullable = false, unique = false)
		private String close;
		
		@Column(length = 100, nullable = false, unique = false)
		private String volume;
		
		@Column(length = 100, nullable = true, name = "close_time", unique = false)
		private String closeTime;
		
		@Column(length = 100, nullable = true, name = "quote_volume", unique = false)
		private String quoteVolume;
		
		@Column(length = 100, nullable = true, unique = false)
		private String count;
		
		@Column(length = 100, nullable = true, name = "taker_by_volume", unique = false)
		private String takerByVolume;
		
		@Column(length = 100, nullable = true, name = "taker_by_quote_volume", unique = false)
		private String takerByTakerVolume;
		
		@Column(length = 100, nullable = true, name = "ignore_count", unique = false)
		private String ignore;
		
		@Column(nullable = false, name = "open_timestamp")
		private Long openTimeStamp;
		
		@Column(nullable = false, name = "minutos_vela")
		private int minutosVela;
		
		@Column(nullable = false, name = "fecha_parseada")
		private String fechaParseada;
		
		

}
