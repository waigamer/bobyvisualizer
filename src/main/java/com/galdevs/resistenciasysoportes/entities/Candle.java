package com.galdevs.resistenciasysoportes.entities;



import java.text.SimpleDateFormat;
import java.util.Date;

import com.galdevs.resistenciasysoportes.dto.CandleDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "candle_order")
@Getter
@Setter
@NoArgsConstructor
public class Candle {

	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long pk;
	
	@Column(length = 20, nullable = true, unique = false)
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
		

		
		
		public Candle(CandleDto candleDto) {
			this.instId=candleDto.getArg().getInstId();
			this.openTime=candleDto.getData().get(0).get(0);
			this.openTimeStamp=new Long(openTime);
			this.open=candleDto.getData().get(0).get(1);
			this.high=candleDto.getData().get(0).get(2);
			this.low=candleDto.getData().get(0).get(3);
			this.close=candleDto.getData().get(0).get(4);
			this.quoteVolume=candleDto.getData().get(0).get(7);
			

			
		}

}
