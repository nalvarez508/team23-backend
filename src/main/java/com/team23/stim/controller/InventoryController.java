package com.team23.stim.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.io.*;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team23.stim.client.OAuth2PlatformClientFactory;
import com.team23.stim.helper.QBOServiceHelper;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.Account;
import com.intuit.ipp.data.AccountSubTypeEnum;
import com.intuit.ipp.data.AccountTypeEnum;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.data.IntuitEntity;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemGroupDetail;
import com.intuit.ipp.data.ItemTypeEnum;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.SalesItemLineDetail;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;

@Controller
public class InventoryController {

	@Autowired
	OAuth2PlatformClientFactory factory;

	@Autowired
	public QBOServiceHelper helper;

	private static final Logger logger = Logger.getLogger(InventoryController.class);
	
	private static final String ACCOUNT_QUERY = "select * from Account where AccountType='%s' and AccountSubType='%s' maxresults 1";

	private int threshold = 50;


	@ResponseBody
	@CrossOrigin(origins = "http://localhost:3000")
	@RequestMapping("/testEndpoint")
	public String returnStringToYou()
	{
		return createResponse("Endpoint call successful!");
	}

/**
	 * API call with OAuth2 to return inventory list
	 *
	 * @param session
	 * @return
	 */
	@ResponseBody
	@CrossOrigin("http://localhost:3000")
	@RequestMapping("/inventory_list")
	public String getInventoryList(@RequestHeader("access_token") String accessToken, @RequestHeader("realm_id") String realmId) {

		//String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		//String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			String ITEM_QUERY = "select * from Item maxresults 99";
			QueryResult ItemList = service.executeQuery(ITEM_QUERY); //Creates QueryResult object with inventory
			List<? extends IEntity> entities = ItemList.getEntities(); //Creates list of entities

			//Stores entities in vector
			Vector<Item> InventoryListContainer = new Vector<Item>(entities.size());

			//Populates vector with items
			for (int i=0; i<entities.size(); i++)
			{
				Item tempItem = ((Item)entities.get(i));
				if (tempItem.getType() != ItemTypeEnum.CATEGORY){ //Items of type CATEGORY are skipped
					InventoryListContainer.add(tempItem);
				}
			}

			JSONObject iList = new JSONObject();
			JSONArray itemDetailArray = new JSONArray();
			for (int x=0; x<InventoryListContainer.size(); x++)
			{
				JSONObject itemDetail = new JSONObject();
				itemDetail.put("name", InventoryListContainer.get(x).getName());
				itemDetail.put("sku", InventoryListContainer.get(x).getSku());
				//itemDetail.put("type", InventoryListContainer.get(x).getParentRef().getName());
				itemDetail.put("qty", InventoryListContainer.get(x).getQtyOnHand());
				itemDetail.put("price", InventoryListContainer.get(x).getUnitPrice());
				itemDetailArray.put(itemDetail);
			}

			// Return response back
			//return createResponse(outputMessage);
			return itemDetailArray.toString();

		} catch (InvalidTokenException e) {
			return new JSONObject().put("response", "InvalidToken - Refresh token and try again").toString();
		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
			return new JSONObject().put("response","Failed").toString();
		}
	}

	/**
	 *
	 * @param session
	 * @return
	 */
	
	@ResponseBody
	@CrossOrigin("http://localhost:3000")
	@RequestMapping("/createMainItem")
	public String addMainItem(@RequestHeader("access_token") String accessToken, @RequestHeader("realm_id") String realmId, @RequestParam("name") String name, @RequestParam("sku") String sku, @RequestParam("price") float price, @RequestParam("qty") int qty) {

		//String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		//String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			// Add item
			Item item = getItemWithAllFields(service, name, sku, price, qty);
			Item savedItem = service.add(item);

			return createResponse("Success");

		} catch (InvalidTokenException e) {
			return new JSONObject().put("response", "InvalidToken - Refresh token and try again").toString();
		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
			return new JSONObject().put("response","Failed").toString();
		}
	}

	@ResponseBody
	@CrossOrigin("http://localhost:3000")
	@RequestMapping("/getAllInvoices")
	public String getAllInvoices(@RequestHeader("access_token") String accessToken, @RequestHeader("realm_id") String realmId)
	{
		//String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		//String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);
			
			String ITEM_QUERY = "select * from Invoice maxresults 10";
			QueryResult InvoiceList = service.executeQuery(ITEM_QUERY); //Creates QueryResult object with inventory
			List<? extends IEntity> entities = InvoiceList.getEntities(); //Creates list of entities

			//Stores entities in vector
			Vector<Invoice> InvoiceListContainer = new Vector<Invoice>(entities.size());
			float dataPoints[] = new float[12];

			//Populates vector with invoices
			for (int i=0; i<entities.size(); i++)
			{
				Invoice tempInvoice = ((Invoice)entities.get(i));
				InvoiceListContainer.add(tempInvoice);
			}
			//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

			JSONObject iList = new JSONObject();
			JSONArray invoiceDetailArray = new JSONArray();
			for (int x=0; x<InvoiceListContainer.size(); x++)
			{
				//Iterating through each invoice's Line items and saving values
				JSONObject invoiceDetail = new JSONObject();
				JSONArray lineDetailArray = new JSONArray();
				List<Line> tempLineList = InvoiceListContainer.get(x).getLine();

				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Los Angeles"));
				cal.setTime(InvoiceListContainer.get(x).getTxnDate());
				dataPoints[cal.get(Calendar.MONTH)] = 0;

				for (int y=0; y<(tempLineList.size()-1); y++)
				{
					JSONObject lineDetail = new JSONObject();
					lineDetail.put("itemName", tempLineList.get(y).getSalesItemLineDetail().getItemRef().getName());
					lineDetail.put("qty", tempLineList.get(y).getSalesItemLineDetail().getQty());
					lineDetail.put("unitPrice", tempLineList.get(y).getSalesItemLineDetail().getUnitPrice());


					lineDetail.put("totalPurchasePrice", tempLineList.get(y).getAmount());
					dataPoints[cal.get(Calendar.MONTH)] += tempLineList.get(y).getAmount().floatValue();
					lineDetailArray.put(lineDetail);
					//System.out.println(lineDetailArray.toString());
				}
				//Adding Line array to JSONObject and getting transaction date
				String invoiceInfo = "Invoice ID " + InvoiceListContainer.get(x).getId();
				invoiceDetail.put(invoiceInfo, lineDetailArray);
				invoiceDetail.put("txnDate", InvoiceListContainer.get(x).getTxnDate());
				invoiceDetailArray.put(invoiceDetail);
				//System.out.println(invoiceDetailArray.toString());
			}
			//iList.put(itemDetailArray);

			// Return response back
			//return createResponse(outputMessage);

			JSONArray coordinates = new JSONArray();
			for (int z=0; z<12; z++)
			{
				JSONObject point = new JSONObject();
				System.out.println("Month " + z);
				System.out.println(dataPoints[z]);
				point.put("month", z);
				point.put("value", dataPoints[z]);
				coordinates.put(point);
			}


			return coordinates.toString();

		} catch (InvalidTokenException e) {
			return new JSONObject().put("response", "InvalidToken - Refresh token and try again").toString();
		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
			return new JSONObject().put("response","Failed").toString();
		}
	}

	@ResponseBody
	@CrossOrigin("http://localhost:3000")
	@RequestMapping("/alertCheck")
	public String findItemsWithLowInventory(@RequestHeader("access_token") String accessToken, @RequestHeader("realm_id") String realmId)
	{
	
		//String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		//String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			String ITEM_QUERY = "select * from Item maxresults 99";
			QueryResult ItemList = service.executeQuery(ITEM_QUERY); //Creates QueryResult object with inventory
			List<? extends IEntity> entities = ItemList.getEntities(); //Creates list of entities

			//Stores entities in vector
			Vector<Item> InventoryListContainer = new Vector<Item>(entities.size());
			//Populates vector with items
			for (int i=0; i<entities.size(); i++)
			{
				Item tempItem = ((Item)entities.get(i));
				if (tempItem.getType() != ItemTypeEnum.CATEGORY){ //Items of type CATEGORY are skipped
					if (tempItem.getQtyOnHand() != null){ //The item has a QtyOnHand value
						if (tempItem.getQtyOnHand().compareTo(new BigDecimal(threshold)) <= 0){
							InventoryListContainer.add(tempItem);
						}
					}
				}
			}

			JSONObject iList = new JSONObject();
			JSONArray itemDetailArray = new JSONArray();
			for (int x=0; x<InventoryListContainer.size(); x++)
			{
				JSONObject itemDetail = new JSONObject();
				itemDetail.put("name", InventoryListContainer.get(x).getName());
				itemDetail.put("sku", InventoryListContainer.get(x).getSku());
				itemDetail.put("qty", InventoryListContainer.get(x).getQtyOnHand());
				itemDetailArray.put(itemDetail);
			}

			// Return response back
			//return createResponse(outputMessage);
			return itemDetailArray.toString();

		} catch (InvalidTokenException e) {
			return new JSONObject().put("response", "InvalidToken - Refresh token and try again").toString();
		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
			return new JSONObject().put("response","Failed").toString();
		}
	}

	@ResponseBody
	@CrossOrigin("http://localhost:3000")
	@RequestMapping("/updateItem")
	public String modifyMainItem(@RequestHeader("access_token") String accessToken, @RequestHeader("realm_id") String realmId, @RequestParam("name") String name, @RequestParam("sku") String sku, @RequestParam("price") float price, @RequestParam("qty") int qty)
	{
		//String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		//String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			String ITEM_QUERY = "select * from Item where sku = " + sku + " maxresults 1";
			QueryResult ItemList = service.executeQuery(ITEM_QUERY); //Creates QueryResult object with inventory
			List<? extends IEntity> entities = ItemList.getEntities(); //Creates list of entities

			//Stores entities in vector
			Vector<Item> InventoryListContainer = new Vector<Item>(entities.size());
			//Populates vector with items
			for (int i=0; i<entities.size(); i++)
			{
				Item tempItem = ((Item)entities.get(i));
				if (tempItem.getSku() == sku){
					InventoryListContainer.add(tempItem);
				}
			}
			//Vector<Item> InventoryListContainer = new Vector<Item>(entities.size());
			Item itemToModify = InventoryListContainer.get(0);
			itemToModify.setUnitPrice(new BigDecimal(price).setScale(2, RoundingMode.HALF_UP));
			itemToModify.setQtyOnHand(new BigDecimal(qty));
			Item savedItem = service.update(itemToModify);

			// Return response back
			//return createResponse(outputMessage);
			return createResponse("Success!");

		} catch (InvalidTokenException e) {
			return new JSONObject().put("response", "InvalidToken - Refresh token and try again").toString();
		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
			return new JSONObject().put("response","Failed").toString();
		}
	}

	@ResponseBody
	@CrossOrigin("http://localhost:3000")
	@RequestMapping("/changeThreshold")
	public String changeThreshold(@RequestParam("threshold") int t)
	{
		threshold = t;
		return createResponse("Threshold updated.");
	}


	@RequestMapping("/importInventory")
	public String importInventory(HttpSession session)
	{
		String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			
			List<List<String>> records = new ArrayList<>();
			try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Nick Alvarez\\OneDrive - nevada.unr.edu\\2020 Fall\\CS425\\Team 23\\OnlineRetailInvoices\\Testing\\Inventory\\OO3_Inventory.csv")))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					String[] values = line.split(",");
					records.add(Arrays.asList(values));
				}
			}
			catch (FileNotFoundException e) {System.out.println("Where is your file?");}
			catch (IOException e) {System.out.println("Something went wrong!");}

			//System.out.println(Arrays.toString(records.toArray()));
			

			for (List<String> csv : records)
			{
				System.out.println(csv.get(2));
				if (!csv.get(2).contains("SKU"))
				{
					Item myNewItem = getItemWithAllFields(service, csv.get(0), csv.get(2), Float.parseFloat(csv.get(4)), Integer.parseInt(csv.get(10)));
					service.add(myNewItem);
				}
			}

			
			return "Great!";

		} catch (InvalidTokenException e) {
			return new JSONObject().put("response", "InvalidToken - Refresh token and try again").toString();
		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
			return new JSONObject().put("response","Failed").toString();
		}
	}

	@RequestMapping("/importInvoices")
	public String importInvoices(HttpSession session)
	{
		String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			
			List<List<String>> records = new ArrayList<>();
			try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Nick Alvarez\\OneDrive - nevada.unr.edu\\2020 Fall\\CS425\\Team 23\\OnlineRetailInvoices\\Testing\\Inventory\\OO3_Invoices.csv")))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					String[] values = line.split(",");
					records.add(Arrays.asList(values));
				}
			}
			catch (FileNotFoundException e) {System.out.println("Where is your file?");}
			catch (IOException e) {System.out.println("Something went wrong!");}

			//System.out.println(Arrays.toString(records.toArray()));
			

			for (List<String> csv : records)
			{
				System.out.println(csv.get(0));
				if (!csv.get(0).contains("InvoiceNo"))
				{
					Item myNewItem = getItemWithAllFields(service, csv.get(0), csv.get(2), Float.parseFloat(csv.get(4)), Integer.parseInt(csv.get(10)));
					service.add(myNewItem);
				}
			}

			
			return "Great!";

		} catch (InvalidTokenException e) {
			return new JSONObject().put("response", "InvalidToken - Refresh token and try again").toString();
		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
			return new JSONObject().put("response","Failed").toString();
		}
	}

	/**
	 * Prepare Item request
	 * @param service
	 * @return
	 * @throws FMSException
	 */
	private Item getItemWithAllFields(DataService service, String name, String sku, float price, int qty) throws FMSException {
		Item item = new Item();
		item.setType(ItemTypeEnum.INVENTORY);
		//item.setName("Test " + category + " Item " + RandomStringUtils.randomAlphanumeric(5));
		item.setName(name);
		item.setSku(sku);
		item.setUnitPrice(new BigDecimal(price).setScale(2, RoundingMode.HALF_UP));
		item.setQtyOnHand(new BigDecimal(qty));
		item.setInvStartDate(new Date());
		//item.setParentRef(ReferenceType);

		item.setTrackQtyOnHand(true);

		Account incomeBankAccount = getIncomeBankAccount(service);
		item.setIncomeAccountRef(createRef(incomeBankAccount));

		Account expenseBankAccount = getExpenseBankAccount(service);
		item.setExpenseAccountRef(createRef(expenseBankAccount));

		Account assetAccount = getAssetAccount(service);
		item.setAssetAccountRef(createRef(assetAccount));

		return item;
	}

	/**
	 * Prepare Customer request
	 * @return
	 */
	private Customer getCustomerWithAllFields() {
		Customer customer = new Customer();
		customer.setDisplayName(org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(6));
		customer.setCompanyName("ABC Corporations");

		EmailAddress emailAddr = new EmailAddress();
		emailAddr.setAddress("testconceptsample@mailinator.com");
		customer.setPrimaryEmailAddr(emailAddr);

		return customer;
	}

	/**OUT
	 * Prepare Invoice Request
	 * @param customer
	 * @param item
	 * @return
	 */
	private Invoice getInvoiceFields(Customer customer, Item item) {
		Invoice invoice = new Invoice();
		invoice.setCustomerRef(createRef(customer));

		List<Line> invLine = new ArrayList<Line>();
		Line line = new Line();
		line.setAmount(new BigDecimal("100"));
		line.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);

		SalesItemLineDetail silDetails = new SalesItemLineDetail();
		silDetails.setQty(BigDecimal.valueOf(1));
		silDetails.setItemRef(createRef(item));

		line.setSalesItemLineDetail(silDetails);
		invLine.add(line);
		invoice.setLine(invLine);

		return invoice;
	}



	/**
	 * Get Income Account
	 * @param service
	 * @return
	 * @throws FMSException
	 */
	private Account getIncomeBankAccount(DataService service) throws FMSException {
		QueryResult queryResult = service.executeQuery(String.format(ACCOUNT_QUERY, AccountTypeEnum.INCOME.value(), AccountSubTypeEnum.SALES_OF_PRODUCT_INCOME.value()));
		List<? extends IEntity> entities = queryResult.getEntities();
		if(!entities.isEmpty()) {
			return (Account)entities.get(0);
		}
		return createIncomeBankAccount(service);
	}

	/**
	 * Create Income Account
	 * @param service
	 * @return
	 * @throws FMSException
	 */
	private Account createIncomeBankAccount(DataService service) throws FMSException {
		Account account = new Account();
		account.setName("Income " + RandomStringUtils.randomAlphabetic(5));
		account.setAccountType(AccountTypeEnum.INCOME);
		account.setAccountSubType(AccountSubTypeEnum.SALES_OF_PRODUCT_INCOME.value());
		
		return service.add(account);
	}

	/**
	 * Get Expense Account
	 * @param service
	 * @return
	 * @throws FMSException
	 */
	private Account getExpenseBankAccount(DataService service) throws FMSException {
		QueryResult queryResult = service.executeQuery(String.format(ACCOUNT_QUERY, AccountTypeEnum.COST_OF_GOODS_SOLD.value(), AccountSubTypeEnum.SUPPLIES_MATERIALS_COGS.value()));
		List<? extends IEntity> entities = queryResult.getEntities();
		if(!entities.isEmpty()) {
			return (Account)entities.get(0);
		}
		return createExpenseBankAccount(service);
	}

	/**
	 * Create Expense Account
	 * @param service
	 * @return
	 * @throws FMSException
	 */
	private Account createExpenseBankAccount(DataService service) throws FMSException {
		Account account = new Account();
		account.setName("Expense " + RandomStringUtils.randomAlphabetic(5));
		account.setAccountType(AccountTypeEnum.COST_OF_GOODS_SOLD);
		account.setAccountSubType(AccountSubTypeEnum.SUPPLIES_MATERIALS_COGS.value());
		
		return service.add(account);
	}


	/**
	 * Get Asset Account
	 * @param service
	 * @return
	 * @throws FMSException
	 */
	private Account getAssetAccount(DataService service)  throws FMSException{
		QueryResult queryResult = service.executeQuery(String.format(ACCOUNT_QUERY, AccountTypeEnum.OTHER_CURRENT_ASSET.value(), AccountSubTypeEnum.INVENTORY.value()));
		List<? extends IEntity> entities = queryResult.getEntities();
		if(!entities.isEmpty()) {
			return (Account)entities.get(0);
		}
		return createOtherCurrentAssetAccount(service);
	}

	/**
	 * Create Asset Account
	 * @param service
	 * @return
	 * @throws FMSException
	 */
	private Account createOtherCurrentAssetAccount(DataService service) throws FMSException {
		Account account = new Account();
		account.setName("Other Current Asset " + RandomStringUtils.randomAlphanumeric(5));
		account.setAccountType(AccountTypeEnum.OTHER_CURRENT_ASSET);
		account.setAccountSubType(AccountSubTypeEnum.INVENTORY.value());
		
		return service.add(account);
	}

	/**
	 * Creates reference type for an entity
	 * 
	 * @param entity - IntuitEntity object inherited by each entity
	 * @return
	 */
	private ReferenceType createRef(IntuitEntity entity) {
		ReferenceType referenceType = new ReferenceType();
		referenceType.setValue(entity.getId());
		return referenceType;
	}

	/**
	 * Map object to json string
	 * @param entity
	 * @return
	 */
	private String createResponse(Object entity) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString;
		try {
			jsonInString = mapper.writeValueAsString(entity);
		} catch (JsonProcessingException e) {
			return createErrorResponse(e);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
		return jsonInString;
	}

	private String createErrorResponse(Exception e) {
		logger.error("Exception while calling QBO ", e);
		return new JSONObject().put("response","Failed").toString();
	}


}