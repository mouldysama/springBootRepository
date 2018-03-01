package interview.dataspark.currencyexchange.tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

import interview.dataspark.currencyexchange.CurrencyExchangeModel;
import interview.dataspark.currencyexchange.CurrencyExchangeController;
import interview.dataspark.currencyexchange.CurrencyExchangeService;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyExchangeControllerTest {
	
	private String fileRepository = "./src/test/resources";
	
	private MockMvc mockMvc;

	
	@InjectMocks
	private CurrencyExchangeController exchangeController;
	
	@Mock
	private CurrencyExchangeService exchangeService;
		
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = standaloneSetup(exchangeController).build();
	}
	
	@Test
	public void testLoadIntroPageInfo() throws Exception {

		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		
		List<Object> introPageInfo = new ArrayList<Object>();
		introPageInfo.add(exchangeService.getFileRepository());
		introPageInfo.add("::::/ByDate/yyyy-MM-dd::::");
		introPageInfo.add("::::/ByDate/yyyy-MM-dd/currencyOfInterest/referenceCurrency::::");
		introPageInfo.add("::::/ByCurrency/currencyOfInterest/yyyy-MM-dd/yyyy-MM-dd::::");
		
		Mockito.when(exchangeService.loadIntroPageInfo()).thenReturn(introPageInfo);
		
		mockMvc.perform(get("/"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$", hasSize(introPageInfo.size())))
		.andExpect(jsonPath("$",hasItems(introPageInfo.toArray())));
	}
	
	@Test
	public void testGetAllDates() throws Exception {

		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		
		List<Object> list = new ArrayList<Object>();
		File[] files = new File(exchangeService.getFileRepository()).listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if(file.isFile() && !list.contains(fileName)) {
				list.add(fileName);
			}
		}
		if (list.isEmpty()) list.add("No data available yet.");

		Mockito.when(exchangeService.getAllDates()).thenReturn(list);
				
		mockMvc.perform(get("/ByDate"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$", hasSize(list.size())))
		.andExpect(jsonPath("$",hasItems(list.toArray())));
	}
	
	@Test
	public void testGetRateByDate() throws Exception {

		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		String[] dates = {/* valid date */ "2017-01-01", /* invalid date */ "7777-77-77"};
		
		for(String date : dates) {
			
			List<Object> list = new ArrayList<Object>();
			try {
				FileReader fileReader = new FileReader(exchangeService.getFileRepository() + "/" + date + ".txt");
	            BufferedReader bufferedReader = new BufferedReader(fileReader);
	            String line = "";
	            while ((line = bufferedReader.readLine()) != null) {
	            	String[] items = line.split(" ");
	            	if (items.length == 7) {
	                	list.add(new CurrencyExchangeModel(date, items[1], items[6], Double.parseDouble(items[4])/Double.parseDouble(items[0])));
	            	}
	            }
	            bufferedReader.close();
	            fileReader.close();
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
			} catch (NumberFormatException e) {
				//e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
			}		
			if (list.isEmpty()) list.add("No data available for specified date - " + date);
			
			Mockito.when(exchangeService.getRateByDate(date)).thenReturn(list);
			
			mockMvc.perform(get("/ByDate/" + date))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(list.size())))
			.andExpect(content().string(new ObjectMapper().writeValueAsString(list)));		
		}		
	}
	
	@Test
	public void testGetSpecificRateByDate() throws Exception {
		
		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		String[] dates = {/* valid date */ "2017-01-01", /* invalid date */ "7777-77-77"};
		String[] currenciesOfInterest = {/* valid currency */ "CHF", /* invalid currency */ "XXX"};
		
		for(String date : dates) {
			for(String currencyOfInterest : currenciesOfInterest) {
				
				List<Object> list = new ArrayList<Object>();
				try {
					FileReader fileReader = new FileReader(exchangeService.getFileRepository() + "/" + date + ".txt");
		            BufferedReader bufferedReader = new BufferedReader(fileReader);
		            String line = "";
		            while ((line = bufferedReader.readLine()) != null) {
		            	String[] items = line.split(" ");
		            	if (items.length == 7 && items[1].equals(currencyOfInterest)) {
		                	list.add(new CurrencyExchangeModel(date, items[1], items[6], Double.parseDouble(items[4])/Double.parseDouble(items[0])));
		            	}
		            }
		            bufferedReader.close();
		            fileReader.close();
				} catch (FileNotFoundException e) {
					//e.printStackTrace();
				} catch (NumberFormatException e) {
					//e.printStackTrace();
				} catch (IOException e) {
					//e.printStackTrace();
				}		
				if (list.isEmpty()) list.add("No data available for specified date - " + date + " and specified currency - " + currencyOfInterest);
				
				Mockito.when(exchangeService.getSpecificRateByDate(date, currencyOfInterest)).thenReturn(list);
				
				mockMvc.perform(get("/ByDate/" + date + "/" + currencyOfInterest))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(list.size())))
				.andExpect(content().string(new ObjectMapper().writeValueAsString(list)));	
				
			}	
		}		
	}
	
	@Test
	public void testGetSpecificRateByDateNewRef() throws Exception {
		
		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		String[] dates = {/* valid date */ "2017-01-01", /* invalid date */ "7777-77-77"};
		String[] currenciesOfInterest = {/* valid currency */ "CHF", /* invalid currency */ "XXX", /* indirect currency */ "USD"};
		String[] referenceCurrencies = {/* valid currency */ "SGD", /* invalid currency */ "YYY",/* redundant currency */ "USD"};

		
		for(String date : dates) {
			for(String currencyOfInterest : currenciesOfInterest) {
				for(String referenceCurrency : referenceCurrencies) {

					List<Object> list = new ArrayList<Object>();
					CurrencyExchangeModel modelOfInterest = null;
					CurrencyExchangeModel referenceModel = null;
					
					try {
						FileReader fileReader = new FileReader(exchangeService.getFileRepository() + "/" + date + ".txt");            
						BufferedReader bufferedReader = new BufferedReader(fileReader);
			            String line = "";
			            while ((line = bufferedReader.readLine()) != null) {
			            	String[] items = line.split(" ");
			            	if (items.length==7) {
			            		if(items[1].equals(currencyOfInterest)) {
			            			modelOfInterest = new CurrencyExchangeModel(date, items[1], items[6], Double.parseDouble(items[4])/Double.parseDouble(items[0]));
			            			if(items[6].equals(referenceCurrency)) {
			            				list.add(modelOfInterest);
			            				break;
			            			}
			            		}
			            		if(items[1].equals(referenceCurrency)) {
			            			if(items[6].equals(currencyOfInterest)) {
			            				list.add(new CurrencyExchangeModel(date,items[6], items[1], Math.round((1.0/Double.parseDouble(items[4]))*100.0)/100.0));
			            				break;
			            			}
			            			referenceModel = new CurrencyExchangeModel(date, items[1], items[6], Double.parseDouble(items[4])/Double.parseDouble(items[0]));
			            		}
			            	}
			            }
			            bufferedReader.close();
			            fileReader.close();
			            if(modelOfInterest != null && referenceModel != null) {
			            	list.add(new CurrencyExchangeModel(date, currencyOfInterest, referenceCurrency,Math.round(modelOfInterest.getExchangeRate()/referenceModel.getExchangeRate()*100.0)/100.0));
			            }
					} catch (FileNotFoundException e) {
						// e.printStackTrace();
					} catch (NumberFormatException e) {
						// e.printStackTrace();
					} catch (IOException e) {
						// e.printStackTrace();
					}
					if (list.isEmpty()) list.add("No data available for specified date - " + date + " and specified currency - " + currencyOfInterest + " and reference currency - " + referenceCurrency);
					
					Mockito.when(exchangeService.getSpecificRateByDate(date, currencyOfInterest, referenceCurrency)).thenReturn(list);
					
					mockMvc.perform(get("/ByDate/" + date + "/" + currencyOfInterest + "/" + referenceCurrency))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(list.size())))
					.andExpect(content().string(new ObjectMapper().writeValueAsString(list)));	
					
				}				
			}	
		}		
	}
	
	@Test
	public void testGetAllCurrencies() throws Exception {
		
		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		
		List<Object> list = new ArrayList<Object>();
		list.add("View exchange rates for specific currency via ../ByCurrency/currencyOfInterest/yyyy-MM-dd/yyyy-MM-dd");
				
		Mockito.when(exchangeService.getAllCurrencies()).thenReturn(list);
		
		mockMvc.perform(get("/ByCurrency"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$", hasSize(list.size())))
		.andExpect(jsonPath("$",hasItems(list.toArray())));
		
	}
	
	@Test
	public void testGetRateByCurrency() throws Exception {
		
		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		String[] currenciesOfInterest = {/* valid currency */ "SGD", /* invalid currency */ "YYY"};
		
		for (String currencyOfInterest : currenciesOfInterest) {
			
			List<Object> list = new ArrayList<Object>();
			list.add("View exchange rates for specific currency via ../ByCurrency/currencyOfInterest/yyyy-MM-dd/yyyy-MM-dd");
					
			Mockito.when(exchangeService.getRateByCurrency(currencyOfInterest)).thenReturn(list);
			
			mockMvc.perform(get("/ByCurrency/" + currencyOfInterest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(list.size())))
			.andExpect(jsonPath("$",hasItems(list.toArray())));	
			
		}				
	}
	
	@Test
	public void testGetSpecificRateByCurrency() throws Exception {
		
		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		String[] dates = {/* valid date */ "2017-01-01", /* invalid date */ "7777-77-77"};
		String[] currenciesOfInterest = {/* valid currency */ "CHF", /* invalid currency */ "XXX"};
		
		for(String date : dates) {
			for(String currencyOfInterest : currenciesOfInterest) {
				
				List<Object> list = new ArrayList<Object>();
				try {
					FileReader fileReader = new FileReader(exchangeService.getFileRepository() + "/" + date + ".txt");
		            BufferedReader bufferedReader = new BufferedReader(fileReader);
		            String line = "";
		            while ((line = bufferedReader.readLine()) != null) {
		            	String[] items = line.split(" ");
		            	if (items.length == 7 && items[1].equals(currencyOfInterest)) {
		                	list.add(new CurrencyExchangeModel(date, items[1], items[6], Double.parseDouble(items[4])/Double.parseDouble(items[0])));
		            	}
		            }
		            bufferedReader.close();
		            fileReader.close();
				} catch (FileNotFoundException e) {
					//e.printStackTrace();
				} catch (NumberFormatException e) {
					//e.printStackTrace();
				} catch (IOException e) {
					//e.printStackTrace();
				}		
				if (list.isEmpty()) list.add("No data available for specified date - " + date + " and specified currency - " + currencyOfInterest);
				
				Mockito.when(exchangeService.getSpecificRateByDate(date, currencyOfInterest)).thenReturn(list);
				
				mockMvc.perform(get("/ByCurrency/" + currencyOfInterest + "/" + date))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(list.size())))
				.andExpect(content().string(new ObjectMapper().writeValueAsString(list)));	
				
			}	
		}		
		
	}
	
	@Test
	public void testGetSpecifiedRatesByCurrency() throws Exception {

		Mockito.when(exchangeService.getFileRepository()).thenReturn(fileRepository);
		
		String[] currenciesOfInterest = {/* valid currency */ "SGD", /* invalid currency */ "XXX"};
		String[] starts = {/* valid date */ "2017-01-01", /* valid date */ "2017-01-10", /* invalid date */ "7777-77-77"};
		String[] ends = {/* valid date */ "2017-01-05", /* invalid date */ "7777-77-77"};
		
		for (String currencyOfInterest : currenciesOfInterest) {
			for(String start : starts) {
				for(String end : ends) {
					List<Object> list = new ArrayList<Object>();
					try {
						DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").parseStrict().toFormatter();
						LocalDate startDate = LocalDate.parse(start, formatter);
						LocalDate endDate = LocalDate.parse(end, formatter);
						LocalDate date = endDate;
						if (startDate.isAfter(endDate)) {
							endDate = startDate;
							startDate = date;
						}
						date = startDate;
						while (date.isEqual(endDate) || date.isBefore(endDate)) {
							FileReader fileReader = new FileReader(exchangeService.getFileRepository() + "/" + date + ".txt");
				            BufferedReader bufferedReader = new BufferedReader(fileReader);
				            String line = "";
				            while ((line = bufferedReader.readLine()) != null) {
				            	String[] items = line.split(" ");
				            	if (items.length==7 && items[1].equals(currencyOfInterest)) {
				                	list.add(new CurrencyExchangeModel(date.format(formatter), items[1], items[6], Double.parseDouble(items[4])/Double.parseDouble(items[0])));
				            	}
				            }
				            bufferedReader.close();
				            fileReader.close();
							date = date.plusDays(1);
						} 
					} catch (DateTimeParseException e) {
						// TODO: handle exception
					} catch (FileNotFoundException e) {
						//e.printStackTrace();
					} catch (NumberFormatException e) {
						//e.printStackTrace();
					} catch (IOException e) {
						//e.printStackTrace();
					}
					if (list.isEmpty()) list.add("No data available for specified dates - " + start + ", " + end + " and specified currency - " + currencyOfInterest);
					
					Mockito.when(exchangeService.getSpecifiedRatesByCurrency(currencyOfInterest, start, end)).thenReturn(list);
					
					mockMvc.perform(get("/ByCurrency/" + currencyOfInterest + "/" + start + "/" + end))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(list.size())))
					.andExpect(content().string(new ObjectMapper().writeValueAsString(list)));	
				}
			}
		}		
	}
}
