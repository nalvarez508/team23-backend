package com.team23.stim.controller;
import com.team23.stim.classes.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.io.*;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
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

/*class InventoryList{
	String name;
	BigDecimal amount;
	String sku;
	BigDecimal price;
	ItemGroupDetail category;
	InventoryList(String name, BigDecimal amount, String sku, BigDecimal price, ItemGroupDetail category){
		this.name = name;
		this.amount = amount;
		this.sku = sku;
		this.price = price;
		this.category = category;
	}
	void printInventoryList()
	{
		System.out.println(this.name);
		System.out.println(this.amount);
		System.out.println(this.sku);
		System.out.println(this.price);
		System.out.println(this.category);
	}
	String getILName()
	{
		return this.name;
	}
}*/

@Controller
public class InventoryController {

	@Autowired
	OAuth2PlatformClientFactory factory;

	@Autowired
	public QBOServiceHelper helper;

	private static final Logger logger = Logger.getLogger(InventoryController.class);
	
	private static final String ACCOUNT_QUERY = "select * from Account where AccountType='%s' and AccountSubType='%s' maxresults 1";


	@ResponseBody
	@CrossOrigin(origins = "http://localhost:3000")
	@RequestMapping("/testEndpoint")
	public String returnStringToYou()
	{
		/*String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		String accessToken = (String)session.getAttribute("access_token");*/

		//return new JSONObject().put("response", "Endpoint call successful!").toString();
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

			//Creates output for functions.html
			/*String outputMessage = "";
			boolean main = true;
			for (int x=0; x<InventoryListContainer.size(); x++)
			{
				main = true;
				outputMessage += (InventoryListContainer.get(x).getName() + "<br />");
				outputMessage += ((InventoryListContainer.get(x).getSku() != null) ? ("Sku: " + InventoryListContainer.get(x).getSku() + "<br />") : "");
				outputMessage += ((InventoryListContainer.get(x).getQtyOnHand() != null) ? ("Qty: " + InventoryListContainer.get(x).getQtyOnHand() + "<br />") : "");
				outputMessage += "Price: " + (InventoryListContainer.get(x).getUnitPrice() + "<br />");
				if (InventoryListContainer.get(x).getUnitPrice().compareTo(BigDecimal.ZERO) == 0) {main = false;}
				outputMessage += "Category: " + (main ? "Main" : "Sub") + "<br />";
				outputMessage += "<br />";
			}*/

			boolean main = true;
			JSONObject iList = new JSONObject();
			JSONArray itemDetailArray = new JSONArray();
			for (int x=0; x<InventoryListContainer.size(); x++)
			{
				main = true;
				JSONObject itemDetail = new JSONObject();
				itemDetail.put("name", InventoryListContainer.get(x).getName());
				itemDetail.put("sku", InventoryListContainer.get(x).getSku());
				//itemDetail.put("type", InventoryListContainer.get(x).getParentRef().getName());
				itemDetail.put("qty", InventoryListContainer.get(x).getQtyOnHand());
				itemDetail.put("price", InventoryListContainer.get(x).getUnitPrice());
				if (InventoryListContainer.get(x).getUnitPrice().compareTo(BigDecimal.ZERO) == 0) {main = false;}
				itemDetail.put("type", (main ? "Main" : "Sub"));
				iList.put("Item " + x, itemDetail);
			}

			// Return response back
			//return createResponse(outputMessage);
			return iList.toString();

		} catch (InvalidTokenException e) {
			return new JSONObject().put("response", "InvalidToken - Refresh token and try again").toString();
		} catch (FMSException e) {
			List<Error> list = e.getErrorList();
			list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
			return new JSONObject().put("response","Failed").toString();
		}
	}

	/**
	 * Sample QBO API call using OAuth2 tokens
	 *
	 * @param session
	 * @return
	 */
	
	@ResponseBody
	@CrossOrigin("http://localhost:3000")
	@RequestMapping("/createMainItem")
	public String addMainItem(@RequestHeader("access_token") String accessToken, @RequestHeader("realm_id") String realmId, @RequestParam("name") String name, @RequestParam("sku") String sku, @RequestParam("price") float price) {

		//String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		//String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			// Add main item - with initial Quantity on Hand of 450
			Item item = getItemWithAllFields(service, "Main", name, sku, price, 0);
			Item savedItem = service.add(item);

			//Creates output for functions.html
			/*String outputMessage = "";
			outputMessage += (savedItem.getName() + "<br />");
			outputMessage += ((savedItem.getSku() != null) ? ("Sku: " + savedItem.getSku() + "<br />") : "");
			outputMessage += ((savedItem.getQtyOnHand() != null) ? ("Qty: " + savedItem.getQtyOnHand() + "<br />") : "");
			//outputMessage += "Category: " + (savedItem.getParentRef().getName()) + "<br />";
			outputMessage += "Price: " + (savedItem.getUnitPrice() + "<br />");
			outputMessage += "<br />";*/

			// Return response back
			//return createResponse(outputMessage);
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
	@RequestMapping("/createSubItem")
	public String addSubItem(@RequestHeader("access_token") String accessToken, @RequestHeader("realm_id") String realmId, @RequestParam("name") String name, @RequestParam("sku") String sku, @RequestParam("qty") int qty, @RequestParam("muq") int muq) {

		//String realmId = (String)session.getAttribute("realmId");
		if (StringUtils.isEmpty(realmId)) {
			return new JSONObject().put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
		}
		//String accessToken = (String)session.getAttribute("access_token");

		try {

			// Get DataService
			DataService service = helper.getDataService(realmId, accessToken);

			// Add main item - with initial Quantity on Hand of 450
			Item item = getItemWithAllFields(service, "Sub", name, sku, 0, qty);
			Item savedItem = service.add(item);

			//Creates output for functions.html
			/*String outputMessage = "";
			outputMessage += (savedItem.getName() + "<br />");
			outputMessage += ((savedItem.getSku() != null) ? ("Sku: " + savedItem.getSku() + "<br />") : "");
			outputMessage += ((savedItem.getQtyOnHand() != null) ? ("Qty: " + savedItem.getQtyOnHand() + "<br />") : "");
			//outputMessage += "Category: " + (savedItem.getParentRef().getName()) + "<br />";
			outputMessage += "Price: " + (savedItem.getUnitPrice() + "<br />");
			outputMessage += "<br />";

			// Return response back
			return createResponse(outputMessage);*/
			return createResponse("Success");

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
	private Item getItemWithAllFields(DataService service, String category, String name, String sku, float price, int qty) throws FMSException {
		Item item = new Item();
		item.setType(ItemTypeEnum.INVENTORY);
		//item.setName("Test " + category + " Item " + RandomStringUtils.randomAlphanumeric(5));
		item.setName(name);
		item.setSku(sku);
		if (category == "Main")
		{
			item.setUnitPrice(new BigDecimal(price));
			item.setQtyOnHand(new BigDecimal(0));
		}
		if (category == "Sub")
		{
			item.setQtyOnHand(new BigDecimal(qty));
		}
		item.setInvStartDate(new Date());
		//item.setParentRef(ReferenceType);

		// Start with 10 items
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

	/**
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