package com.bot.soportesyresistencias.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.print.Pageable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bot.soportesyresistencias.dto.NivelConIndice;
import com.bot.soportesyresistencias.entity.CandleReal;
import com.bot.soportesyresistencias.repository.CandleRealRepository;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ChartController {
	
	@Autowired
	CandleRealRepository candleRealRepository;

	static List<CandleReal> candles;

	
	private String cryptoPair="BTC";
	double permisividad = 0; //% de permisividad
	
	@GetMapping("/chart")
	public void getChart(HttpServletResponse response) throws IOException {
	    insertDataCandles();
	    
	    Map<String, Object> zigzagData = calcularSoportesYResistenciasZigZag(candles, 0.25, 150, 0.05, 2);
	    List<NivelConIndice> zigzagNiveles = (List<NivelConIndice>) zigzagData.get("ZigZagNiveles");
	    List<NivelConIndice> soportesAgrupados = (List<NivelConIndice>) zigzagData.get("Soportes");
	    List<NivelConIndice> resistenciasAgrupadas = (List<NivelConIndice>) zigzagData.get("Resistencias");

	    int numCandles = candles.size();
	    Date[] date = new Date[numCandles];
	    double[] high = new double[numCandles];
	    double[] low = new double[numCandles];
	    double[] open = new double[numCandles];
	    double[] close = new double[numCandles];
	    double[] volume = new double[numCandles];

	    double maxHigh = Double.MIN_VALUE; // Inicializa con el valor más pequeño posible para un double
        double minLow = Double.MAX_VALUE;  // Inicializa con el valor más grande posible para un double
        
	    for (int i = 0; i < numCandles; i++) {
	        CandleReal candle = candles.get(i);
	        date[i] = new Date(candle.getOpenTimeStamp());
	        open[i] = Double.parseDouble(candle.getOpen());
	        close[i] = Double.parseDouble(candle.getClose());
	        high[i] = Double.parseDouble(candle.getHigh());
	        low[i] = Double.parseDouble(candle.getLow());
	        volume[i] = Double.parseDouble("1");
	        
	        if (high[i] > maxHigh) { // Comprueba si el high actual es mayor que el maxHigh registrado
                maxHigh = high[i];   // Si es así, actualiza maxHigh
            }
            if (low[i] < minLow) {   // Comprueba si el low actual es menor que el minLow registrado
                minLow = low[i];     // Si es así, actualiza minLow
            }
	    }
	    double range = maxHigh - minLow;  // El rango total de precios
        double pointSize = range * 0.02;  // El tamaño de los puntos como el 1% del rango total
       
	    DefaultHighLowDataset dataset = new DefaultHighLowDataset(
	            cryptoPair,
	            date,
	            high,
	            low,
	            open,
	            close,
	            volume
	    );

	    JFreeChart chart = ChartFactory.createCandlestickChart(
	            "Precios " + cryptoPair,
	            "Tiempo",
	            "Precio",
	            dataset,
	            false
	    );

	    XYPlot plot = (XYPlot) chart.getPlot();
	    plot.setRenderer(new CandlestickRenderer());

	    // Dibujar líneas de zigzag
	    for (int i = 1; i < zigzagNiveles.size(); i++) {
	        double x1 = candles.get(zigzagNiveles.get(i - 1).getIndice()).getOpenTimeStamp();
	        double y1 = zigzagNiveles.get(i - 1).getNivel();
	        double x2 = candles.get(zigzagNiveles.get(i).getIndice()).getOpenTimeStamp();
	        double y2 = zigzagNiveles.get(i).getNivel();

	        XYLineAnnotation line = new XYLineAnnotation(x1, y1, x2, y2, new BasicStroke(1.0f), Color.BLACK);
	        plot.addAnnotation(line);

	        // Dibujar puntos de zigzag
	        XYShapeAnnotation point = new XYShapeAnnotation(new Ellipse2D.Double(x2 - pointSize , y2 - pointSize , pointSize*2, pointSize*2), new BasicStroke(2.0f), Color.CYAN);
	        plot.addAnnotation(point);
	    }

	    // Dibujar soportes y resistencias
	    for (NivelConIndice soporte : soportesAgrupados) {
	        Marker marker = new ValueMarker(soporte.getNivel());
	        marker.setPaint(Color.BLUE);
	        marker.setStroke(new BasicStroke(0.5f));
	        plot.addRangeMarker(marker);
	    }

	    for (NivelConIndice resistencia : resistenciasAgrupadas) {
	        Marker marker = new ValueMarker(resistencia.getNivel());
	        marker.setPaint(Color.magenta);
	        marker.setStroke(new BasicStroke(0.5f));
	        plot.addRangeMarker(marker);
	    }
	    // Set the Y axis range based on maxHigh and minLow
	    NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
	    yAxis.setRange(minLow , maxHigh ); // Small padding added

	    DateAxis axis = (DateAxis) plot.getDomainAxis();
	    axis.setDateFormatOverride(new SimpleDateFormat("dd-MM HH:mm"));

	    response.setContentType("image/png");
	    ChartUtils.writeChartAsPNG(response.getOutputStream(), chart, 1200, 800);
	    response.getOutputStream().close();
	}

    
    public void insertDataCandles() {
    	if (candles != null) return;
        // candles = candleRealRepository.findByInstIdAndOpenTimeStampBetween("ADA-USDT-SWAP", 1695670200000L,1695849300000L );
//         candles = candleRealRepository.findByPkBetween(1, 6) ;
         candles = candleRealRepository.findByInstIdAndOpenTimeStampBetween(
        		 cryptoPair + "-USDT-SWAP",
        		    1695670200000L, 
        		    1695849300000L, 
        		    Sort.by("openTimeStamp").ascending()  // puedes cambiar a .descending() para orden descendente
        		);
    }

    public Map<String, Object> calcularSoportesYResistenciasZigZag(List<CandleReal> velas, double minRetrace, int maxBars, double maxDiff, int minPoints) {
        List<NivelConIndice> zigzagNiveles = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        double curVal = Double.parseDouble(velas.get(0).getClose());
        int curDir = 1;
        int curPos = 0;

        for (int i = 1; i < velas.size(); i++) {
            double close = Double.parseDouble(velas.get(i).getClose());

            if ((close - curVal) * curDir >= 0) {
                curVal = close;
                curPos = i;
            } else {
                double retracePrc = Math.abs((close - curVal) / curVal * 100);
                if (retracePrc >= minRetrace) {
                    zigzagNiveles.add(new NivelConIndice(curVal, curPos));
                    indices.add(curPos);
                    curVal = close;
                    curPos = i;
                    curDir = -curDir;
                }
            }
        }
        zigzagNiveles.add(new NivelConIndice(curVal, curPos));
        indices.add(curPos);

        List<NivelConIndice> resistencias = new ArrayList<>();
        List<NivelConIndice> soportes = new ArrayList<>();

        for (int i = 1; i < zigzagNiveles.size() - 1; i++) {
            NivelConIndice prevPoint = zigzagNiveles.get(i - 1);
            NivelConIndice currentPoint = zigzagNiveles.get(i);
            NivelConIndice nextPoint = zigzagNiveles.get(i + 1);

            if (currentPoint.getNivel() > prevPoint.getNivel() && currentPoint.getNivel() > nextPoint.getNivel()) {
                resistencias.add(currentPoint);
            } else if (currentPoint.getNivel() < prevPoint.getNivel() && currentPoint.getNivel() < nextPoint.getNivel()) {
                soportes.add(currentPoint);
            }
        }

        List<NivelConIndice> resistenciasAgrupadas = agruparNiveles(resistencias, maxDiff, minPoints, maxBars); 
        List<NivelConIndice> soportesAgrupados = agruparNiveles(soportes, maxDiff, minPoints, maxBars); 

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("Resistencias", resistenciasAgrupadas);
        resultado.put("Soportes", soportesAgrupados);
        resultado.put("ZigZagNiveles", zigzagNiveles);  // Aquí están los puntos y tiempos zigzag

        return resultado;
    }



    private List<NivelConIndice> agruparNiveles(List<NivelConIndice> nivelesConIndice, double maxDiff, int minPoints, int maxBars) {
        List<List<Double>> grupos = new ArrayList<>();
        List<List<Integer>> gruposIndices = new ArrayList<>();

        for (NivelConIndice nivelConIndice : nivelesConIndice) {
            double nivel = nivelConIndice.getNivel();
            int index = nivelConIndice.getIndice();
            boolean agregado = false;

            for (int i = 0; i < grupos.size(); i++) {
                List<Double> grupo = grupos.get(i);
                List<Integer> grupoIndices = gruposIndices.get(i);
                double avgNivel = grupo.stream().mapToDouble(val -> val).average().orElse(0.0);
                int maxIndex = Collections.max(grupoIndices);
                int minIndex = Collections.min(grupoIndices);

                if (Math.abs(nivel - avgNivel) / avgNivel < maxDiff / 100 && Math.abs(index - maxIndex) < maxBars && Math.abs(index - minIndex) < maxBars) {
                    grupo.add(nivel);
                    grupoIndices.add(index);
                    agregado = true;
                    break;
                }
            }

            if (!agregado) {
                List<Double> nuevoGrupo = new ArrayList<>();
                nuevoGrupo.add(nivel);
                grupos.add(nuevoGrupo);

                List<Integer> nuevoGrupoIndices = new ArrayList<>();
                nuevoGrupoIndices.add(index);
                gruposIndices.add(nuevoGrupoIndices);
            }
        }

        List<NivelConIndice> agrupados = new ArrayList<>();
        for (int i = 0; i < grupos.size(); i++) {
            if (grupos.get(i).size() >= minPoints) {
                double avg = grupos.get(i).stream().mapToDouble(val -> val).average().orElse(0.0);
                int indicePromedio = (int) gruposIndices.get(i).stream().mapToInt(val -> val).average().orElse(0);
                agrupados.add(new NivelConIndice(avg, indicePromedio));
            }
        }

        return agrupados;
    }
}