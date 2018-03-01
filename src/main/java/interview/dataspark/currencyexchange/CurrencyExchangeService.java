package interview.dataspark.currencyexchange;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyExchangeService {
	
	@Value("${fileRepository}")
	private String fileRepository;
	
	/***
	 * Lists out all files available in the data repository.
	 * @return List of strings of format "yyyy-MM-dd.txt" to be parsed in the page as json message
	 */
	public List<Object> getAllDates() {
		List<Object> list = new ArrayList<Object>();
		File[] files = new File(getFileRepository()).listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if(file.isFile() && !list.contains(fileName)) {
				list.add(fileName);
			}
		}
		if (list.isEmpty()) list.add("No data available yet.");
		return list;
	}

	/***
	 * Returns exchange rates for all currencies for a given date
	 * @param date of "yyyy-MM-dd" format
	 * @return If date is valid and data is available, the method returns a list of objects(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message list. Otherwise return error message.
	 */
	public List<Object> getRateByDate(String date) {		
		List<Object> list = new ArrayList<Object>();
		try {
			FileReader fileReader = new FileReader(getFileRepository() + "/" + date + ".txt");
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
		return list;
	}

	/***
	 * Returns exchange rates for a specified currency for a given date
	 * @param date of "yyyy-MM-dd" format
	 * @param currencyOfInterest of "XXX" format - case sensitive
	 * @return If date, currencyOfInterest is valid and data is available, the method returns an object(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message. Otherwise return error message.
	 */
	public List<Object> getSpecificRateByDate(String date, String currencyOfInterest) {
		List<Object> list = new ArrayList<Object>();
		try {
			FileReader fileReader = new FileReader(getFileRepository() + "/" + date + ".txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
            	String[] items = line.split(" ");
            	if (items.length==7 && items[1].equals(currencyOfInterest)) {
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
		return list;
	}

	/***
	 * Returns exchange rates for a specified currency for a given date using a specified reference currency (non-USD included).
	 * @param date of "yyyy-MM-dd" format
	 * @param currencyOfInterest of "XXX" format - case sensitive
	 * @param referenceCurrency of "XXX" format - case sensitive
	 * @return If date, currencyOfInterest,referenceCurrency is valid and data is available, the method returns an object(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message. Otherwise return error message. Error message to be returned if currencyOfInterest==referenceCurrency.
	 */
	public List<Object> getSpecificRateByDate(String date, String currencyOfInterest, String referenceCurrency) {
		List<Object> list = new ArrayList<Object>();
		CurrencyExchangeModel modelOfInterest = null;
		CurrencyExchangeModel referenceModel = null;
		
		try {
			FileReader fileReader = new FileReader(getFileRepository() + "/" + date + ".txt");            
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
			//e.printStackTrace();
		} catch (NumberFormatException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		finally {
			if (list.isEmpty()) list.add("No data available for specified date - " + date + " and specified currency - " + currencyOfInterest + " and reference currency - " + referenceCurrency);
		}
		return list;
	}

	/***
	 * Provide static instructions on accessing exchange rates for specified currencies and date ranges.
	 * @return Static instructions on accessing exchange rates for specified currencies and date ranges.
	 */
	public List<Object> getAllCurrencies() {
		List<Object> list = new ArrayList<Object>();
		list.add("View exchange rates for specific currency via ../ByCurrency/currencyOfInterest/yyyy-MM-dd/yyyy-MM-dd");
		return list;
	}

	/*** 
	 * Provide static instructions on accessing exchange rates for specified currencies and date ranges.
	 * @param currencyOfInterest of "XXX" format - case sensitive. 
	 * @return Static instructions on accessing exchange rates for specified currencies and date ranges. 
	 */
	public List<Object> getRateByCurrency(String currencyOfInterest) {
		List<Object> list = new ArrayList<Object>();
		list.add("View exchange rates for specific currency via ../ByCurrency/currencyOfInterest/yyyy-MM-dd/yyyy-MM-dd");
		return list;
	}

	/***
	 * Returns exchange rates for a specified currency for a given date
	 * @param start of "yyyy-MM-dd" format
	 * @param end of "yyyy-MM-dd" format
	 * @param currencyOfInterest of "XXX" format - case sensitive
	 * @return If date, currencyOfInterest is valid and data is available, the method returns an object(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message. Otherwise return error message.
	 */
	public List<Object> getSpecifiedRatesByCurrency(String currencyOfInterest, String start, String end) {
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
				list.addAll(getSpecificRateByDate(date.format(formatter), currencyOfInterest));
				date = date.plusDays(1);
			} 
		} catch (DateTimeParseException e) {
			// TODO: handle exception
		}
		if (list.isEmpty()) list.add("No data available for specified dates - " + start + ", " + end + " and specified currency - " + currencyOfInterest);
		return list;
	}

	public List<Object> loadIntroPageInfo() {
		List<Object> introPageInfo = new ArrayList<Object>();
		introPageInfo.add(getFileRepository());
		introPageInfo.add("::::/ByDate/yyyy-MM-dd::::");
		introPageInfo.add("::::/ByDate/yyyy-MM-dd/currencyOfInterest/referenceCurrency::::");
		introPageInfo.add("::::/ByCurrency/currencyOfInterest/yyyy-MM-dd/yyyy-MM-dd::::");
		return introPageInfo;
	}

	public String getFileRepository() {
		return fileRepository;
	}

	public void setFileRepository(String fileRepository) {
		this.fileRepository = fileRepository;
	}

}
