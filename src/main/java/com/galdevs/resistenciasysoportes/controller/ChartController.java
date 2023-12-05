package com.galdevs.resistenciasysoportes.controller;

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

import com.galdevs.resistenciasysoportes.dto.NivelConIndice;
import com.galdevs.resistenciasysoportes.entities.CandleReal;
import com.galdevs.resistenciasysoportes.repositories.CandleRealRepository;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ChartController {
	
	@Autowired
	CandleRealRepository candleRealRepository;

	
	static List<CandleReal> candles;
	static List<CandleReal> candlesforAnalisys=new ArrayList<CandleReal>(0);
	static int indice=0;
	
	static List<NivelConIndice> soportesAgrupados = new ArrayList<NivelConIndice>(0);
	static List<NivelConIndice> resistenciasAgrupadas = new ArrayList<NivelConIndice>(0);
	
	private String cryptoPair="CRV";
	double permisividad = 0; //% de permisividad
	
	@GetMapping("/chart")
	public void getChart(HttpServletResponse response) throws IOException {
	    insertDataCandles();
	    
	    // Limitar la lista de velas a las primeras 500
	    if (candles.size() > 200) {
	        candles = candles.subList(0, 200);
	    }
	    
	    Map<String, Object> zigzagData = calcularSoportesYResistenciasZigZag(candles, 1, 150, 0.05, 2);
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

	    double maxHigh = Double.MIN_VALUE; 
	    double minLow = Double.MAX_VALUE;  
	        
	    for (int i = 0; i < numCandles; i++) {
	        CandleReal candle = candles.get(i);
	        date[i] = new Date(candle.getOpenTimeStamp());
	        open[i] = Double.parseDouble(candle.getOpen());
	        close[i] = Double.parseDouble(candle.getClose());
	        high[i] = Double.parseDouble(candle.getHigh());
	        low[i] = Double.parseDouble(candle.getLow());
	        volume[i] = Double.parseDouble("1");
	        
	        if (high[i] > maxHigh) { 
	            maxHigh = high[i];   
	        }
	        if (low[i] < minLow) {   
	            minLow = low[i];     
	        }
	    }
	    double range = maxHigh - minLow;  
	    double pointSize = range * 0.02;  
	       
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

	    // Dibujar líneas de zigzag y numerar los puntos clave
	    for (int i = 1; i < zigzagNiveles.size(); i++) {
	        double x1 = candles.get(zigzagNiveles.get(i - 1).getIndice()).getOpenTimeStamp();
	        double y1 = zigzagNiveles.get(i - 1).getNivel();
	        double x2 = candles.get(zigzagNiveles.get(i).getIndice()).getOpenTimeStamp();
	        double y2 = zigzagNiveles.get(i).getNivel();

	        XYLineAnnotation line = new XYLineAnnotation(x1, y1, x2, y2, new BasicStroke(1.0f), Color.BLACK);
	        plot.addAnnotation(line);

	        XYShapeAnnotation point = new XYShapeAnnotation(new Ellipse2D.Double(x2 - pointSize, y2 - pointSize, pointSize * 2, pointSize * 2), new BasicStroke(2.0f), Color.CYAN);
	        plot.addAnnotation(point);

	        // Añadir anotación numérica en cada punto del zigzag
	        XYTextAnnotation annotation = new XYTextAnnotation(Integer.toString(i), x2, y2);
	        annotation.setFont(new Font("SansSerif", Font.BOLD, 12));
	        annotation.setPaint(Color.DARK_GRAY);
	        annotation.setTextAnchor(TextAnchor.TOP_CENTER);
	        plot.addAnnotation(annotation);
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

	    // Configurar rango del eje Y y formato del eje X
	    NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
	    yAxis.setRange(minLow, maxHigh);

	    DateAxis axis = (DateAxis) plot.getDomainAxis();
	    axis.setDateFormatOverride(new SimpleDateFormat("dd-MM HH:mm"));

	    // Generar y enviar la imagen del gráfico
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
        		 1609425000000L, 
        		 1627424100000L, 
        		    Sort.by("openTimeStamp").ascending()  // puedes cambiar a .descending() para orden descendente
        		);
    }

    /**
     * Calcula los soportes y resistencias de un conjunto de velas usando el método ZigZag.
     * 
     * @param velas Lista de objetos CandleReal representando las velas.
     * @param minRetrace Porcentaje mínimo de retracción para considerar un cambio de dirección.
     * @param maxBars Número máximo de barras entre niveles para agrupar.
     * @param maxDiff Diferencia máxima permitida para agrupar niveles.
     * @param minPoints Número mínimo de puntos para considerar un grupo.
     * @return Mapa con listas de niveles de soportes, resistencias y niveles ZigZag.
     */
 // Este método calcula los soportes y resistencias de un conjunto de velas usando el método ZigZag.
 // Recibe una lista de velas y varios parámetros para configurar el algoritmo de cálculo.
 public Map<String, Object> calcularSoportesYResistenciasZigZag(List<CandleReal> velas, double minRetrace, int maxBars, double maxDiff, int minPoints) {

     // Inicializa una lista para almacenar los niveles ZigZag detectados.
     List<NivelConIndice> zigzagNiveles = new ArrayList<>();
     // Inicializa una lista para almacenar los índices de estos niveles en la lista original de velas.
     List<Integer> indices = new ArrayList<>();

     // Obtiene el valor de cierre de la primera vela para iniciar el análisis.
     double curVal = Double.parseDouble(velas.get(0).getClose());
     // Establece la dirección inicial del análisis. El valor 1 indica que se comienza buscando un aumento en el precio.
     int curDir = 1;
     // Guarda la posición inicial (índice en la lista de velas) para el primer nivel ZigZag.
     int curPos = 0;

     // Itera sobre la lista de velas, comenzando por la segunda, para calcular los niveles ZigZag.
     for (int i = 1; i < velas.size(); i++) {
    	 if(i==160) 
    		 System.out.println("esta en la vela");
    	 
         // Obtiene el valor de cierre de la vela actual.
         double close = Double.parseDouble(velas.get(i).getClose());

         // Comprueba si el precio continúa en la misma dirección (subiendo o bajando).
         if ((close - curVal) * curDir >= 0) {
             // Si es así, actualiza el valor actual y la posición actual.
             curVal = close;
             curPos = i;
         } else {
             // Si el precio cambia de dirección, calcula el porcentaje de retracción.
             double retracePrc = Math.abs((close - curVal) / curVal * 100);
             // Si la retracción alcanza el mínimo configurado, se registra un nuevo nivel ZigZag.
             if (retracePrc >= minRetrace) {
                 // Añade el nivel ZigZag detectado a la lista.
                 zigzagNiveles.add(new NivelConIndice(curVal, curPos));
                 // Añade el índice del nivel a la lista de índices.
                 indices.add(curPos);
                 // Actualiza el valor actual, la posición actual y cambia la dirección del análisis.
                 curVal = close;
                 curPos = i;
                 curDir = -curDir;
             }
         }
     }
     // Añade el último nivel ZigZag detectado al final del análisis.
     zigzagNiveles.add(new NivelConIndice(curVal, curPos));
     // Añade el último índice a la lista de índices.
     indices.add(curPos);

     // Inicializa listas para almacenar los niveles de soportes y resistencias detectados.
     List<NivelConIndice> resistencias = new ArrayList<>();
     List<NivelConIndice> soportes = new ArrayList<>();
     // Llama a un método para determinar los soportes y resistencias basándose en los niveles ZigZag.
     determinarSoportesYResistencias(zigzagNiveles, resistencias, soportes);

     // Agrupa los niveles de resistencia y soporte según la proximidad y la diferencia máxima permitida.
     List<NivelConIndice> resistenciasAgrupadas = agruparNiveles(resistencias, maxDiff, minPoints, maxBars); 
     List<NivelConIndice> soportesAgrupados = agruparNiveles(soportes, maxDiff, minPoints, maxBars); 

     // Prepara un mapa para devolver los resultados del cálculo.
     Map<String, Object> resultado = new HashMap<>();
     resultado.put("Resistencias", resistenciasAgrupadas); // Añade las resistencias agrupadas al mapa.
     resultado.put("Soportes", soportesAgrupados); // Añade los soportes agrupados al mapa.
     resultado.put("ZigZagNiveles", zigzagNiveles); // Añade todos los niveles ZigZag al mapa.

     // Devuelve el mapa con los resultados.
     return resultado;
 }

    /**
     * Determina los niveles de soportes y resistencias a partir de los niveles ZigZag.
     *
     * @param zigzagNiveles Lista de niveles ZigZag.
     * @param resistencias Lista para almacenar niveles de resistencia.
     * @param soportes Lista para almacenar niveles de soporte.
     */
    private void determinarSoportesYResistencias(List<NivelConIndice> zigzagNiveles, List<NivelConIndice> resistencias, List<NivelConIndice> soportes) {
        for (int i = 1; i < zigzagNiveles.size() - 1; i++) {
            NivelConIndice prevPoint = zigzagNiveles.get(i - 1);
            NivelConIndice currentPoint = zigzagNiveles.get(i);
            NivelConIndice nextPoint = zigzagNiveles.get(i + 1);

            // Determinar si el punto actual es una resistencia o un soporte
            if (currentPoint.getNivel() > prevPoint.getNivel() && currentPoint.getNivel() > nextPoint.getNivel()) {
                resistencias.add(currentPoint);
            } else if (currentPoint.getNivel() < prevPoint.getNivel() && currentPoint.getNivel() < nextPoint.getNivel()) {
                soportes.add(currentPoint);
            }
        }
    }

    /**
     * Agrupa los niveles de soportes o resistencias según la proximidad y la diferencia máxima permitida.
     *
     * @param nivelesConIndice Lista de niveles con índices para agrupar.
     * @param maxDiff Diferencia máxima permitida para considerar niveles como parte del mismo grupo.
     * @param minPoints Número mínimo de puntos para formar un grupo.
     * @param maxBars Número máximo de barras entre niveles para agrupar.
     * @return Lista de niveles agrupados.
     */
    private List<NivelConIndice> agruparNiveles(List<NivelConIndice> nivelesConIndice, double maxDiff, int minPoints, int maxBars) {
        // Inicializar listas para grupos y sus índices
        List<List<Double>> grupos = new ArrayList<>();
        List<List<Integer>> gruposIndices = new ArrayList<>();

        // Iterar sobre cada nivel para agrupar
        for (NivelConIndice nivelConIndice : nivelesConIndice) {
            double nivel = nivelConIndice.getNivel();
            int index = nivelConIndice.getIndice();
            // Intentar agregar el nivel a un grupo existente
            if (!agregarAGrupo(nivel, index, grupos, gruposIndices, maxDiff, maxBars)) {
                // Si no se puede agregar a un grupo existente, crear uno nuevo
                List<Double> nuevoGrupo = new ArrayList<>();
                nuevoGrupo.add(nivel);
                grupos.add(nuevoGrupo);

                List<Integer> nuevoGrupoIndices = new ArrayList<>();
                nuevoGrupoIndices.add(index);
                gruposIndices.add(nuevoGrupoIndices);
            }
        }

        // Crear lista para niveles agrupados
        List<NivelConIndice> agrupados = new ArrayList<>();
        for (int i = 0; i < grupos.size(); i++) {
            // Considerar solo grupos con el mínimo de puntos requerido
            if (grupos.get(i).size() >= minPoints) {
                double avg = grupos.get(i).stream().mapToDouble(val -> val).average().orElse(0.0);
                int indicePromedio = (int) gruposIndices.get(i).stream().mapToInt(val -> val).average().orElse(0);
                agrupados.add(new NivelConIndice(avg, indicePromedio));
            }
        }

        return agrupados;
    }

    /**
     * Intenta agregar un nivel a un grupo existente según las condiciones dadas.
     *
     * @param nivel Nivel a agregar.
     * @param index Índice del nivel.
     * @param grupos Lista de grupos existentes.
     * @param gruposIndices Índices de los grupos existentes.
     * @param maxDiff Diferencia máxima permitida para agrupar.
     * @param maxBars Número máximo de barras entre niveles para agrupar.
     * @return Verdadero si se agregó al grupo, falso en caso contrario.
     */
    private boolean agregarAGrupo(double nivel, int index, List<List<Double>> grupos, List<List<Integer>> gruposIndices, double maxDiff, int maxBars) {
        // Iterar sobre cada grupo existente para intentar agregar el nivel
        for (int i = 0; i < grupos.size(); i++) {
            List<Double> grupo = grupos.get(i);
            List<Integer> grupoIndices = gruposIndices.get(i);
            // Calcular el promedio del grupo y obtener índices máximos y mínimos
            double avgNivel = grupo.stream().mapToDouble(val -> val).average().orElse(0.0);
            int maxIndex = Collections.max(grupoIndices);
            int minIndex = Collections.min(grupoIndices);

            // Verificar si el nivel cumple las condiciones para ser agregado al grupo
            if (Math.abs(nivel - avgNivel) / avgNivel < maxDiff / 100 && Math.abs(index - maxIndex) < maxBars && Math.abs(index - minIndex) < maxBars) {
                grupo.add(nivel);
                grupoIndices.add(index);
                return true;
            }
        }
        return false;
    }

}