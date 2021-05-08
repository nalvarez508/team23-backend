# Backend (QBO)
####	~/…/STIM/Controller/: Inventory Controller
  -	(Test Endpoint) Used to test frontend calls to backend without requiring headers.
  - (Inventory List) Returns up to 1000 inventory items (QBO API limitation), removes unnecessary items (of type Category, treated as an item), takes relevant data from each item and returns it all in a JSON Array of JSON Objects.
  - (Create Main Item) Takes in parameters from frontend (name, SKU, price, quantity) and calls a helper function to create the Item object. This object is then added to QBO inventory.
  - (Get All Invoices) Returns up to 1000 invoices (QBO API limitation), pulling total transaction amount and the date from each. These values are placed into data points based on month and total sale amount. The data points are put into a JSON Array and returned to the frontend to be graphed.
  - (Get Item Amounts) Takes in a specific item SKU. Returns up to 1000 invoices (QBO API limitation) and checks if the specified item was sold in that invoice. Quantity of items sold and the transaction date are pulled from each relevant invoice, and turned into data points based on month and amount of the item sold. The data points are put into a JSON Array and returned to the frontend to be graphed.
  - (Alert Check) Returns up to 1000 inventory items (QBO API limitation) and checks if some items’ stock levels are at or below a user-specified threshold (default: 50). These low inventory items’ name, SKU, and remaining quantity are pulled and put into a JSON Array of JSON Objects to be returned to the frontend.
  - (Update Item) Takes in SKU, quantity, and price. The SKU is used to identify the item (SKUs are unique, names may not be) and the latter two attributes are what will be updated. The QBO inventory is queried for items matching that SKU, and this specific item is updated with the passed in values.
  - (Change Threshold) Updates variable threshold within backend with new frontend value passed in. This is used for the alert check function.
  - (Import Inventory) Helper function for development purposes. Reads in a CSV file, parses each row, and creates a new Item in the QBO inventory with the values from the file. QBO has a row limit of 1000 per file on their website, so the API is the only way to do it in bulk without the desktop application.
  - (Import Invoices) Ultimately unused. Helper function for development purposes. Would read CSV file of invoices, parse the rows, and create an Invoice object for each entry. Invoices spread out over multiple lines could not be implemented in the project timeframe, so this was abandoned.
  - (Get Item With All Fields) Helper function to create an Item object based on passed in values. Creates an Item object, assigns required information, and returns it.
  - (Get Customer With All Fields) Helper function to create a Customer object with a given name. Would have been used in conjunction with the invoice helper function, but was ulitmately unnecessary.
  - (Get Invoice Fields) Helper function to create an Invoice object based on a Customer and Item sold. Invoices were tested with an amount of 1 Item sold at a cost of $100. Would have been used to import invoices in bulk and/or create sales before STIM’s focus shifted.
  - (Get Income Bank Account) Finds user’s default income bank account as specified in QBO via a query of the user’s company.
  - (Create Income Bank Account) If the user does not have a default income bank account, create one for them.
  - (Get Expense Bank Account) Finds user’s default expense bank account as specified in QBO via a query of the user’s company.
  - (Create Expense Bank Account) If the user does not have a default expense bank account, create one for them.
  - (Get Asset Account) Find’s user’s default asset account as specified in QBO via a query of the user’s company.
  - (Create Other Current Asset Account) If the user does not have a default asset account, create one for them.
  - (Create Reference) Item type methods for accounts ask for a ReferenceType object. This helper function converts Account objects into ReferenceType objects.
  - (Create Response) Method used to create simple JSON responses. Most functions preferred using JSON Object’s / JSON Array’s “To String” method instead.
  - (Create Error Response) Default error handler, describing the exception and that the response failed.
####	~/…/STIM/Controller/: Callback Controller
  - (OAuth2 Redirect) Authorization tokens are transformed into bearer tokens. These values are set within the Session object and saved on the backend.
  - (Get Headers) The saved access token, refresh token, and realm ID on the backend are put into a JSON Object and returned to the frontend when called.
####	~/…/STIM/Controller/: Home Controller
  - Pages Home, Connected, Functions, and Test Page (development only) are mapped.
  - (Connect to Quickbooks) Upon pressing the Connect to Quickbooks button, the program retrieves OAuth2 information from the application properties file. This results in a popup asking the user to sign in to Quickbooks. The OAuth2 Redirect endpoint is called in the callback controller once the user signs in.
####	~/…/STIM/Controller/: QBO Controller
  - Some of file provided from QBO code samples.
  - (Get Company Info) Comes with sample. Retrieves company information, like name, address, etc. Not used within application anymore, just a testing endpoint initially.
  - (Return Session) Did not come with sample. Early implementation of what eventually became Get Headers. Meant to take Session object and use it for all backend calls requiring authorization.
####	~/…/STIM/Client/: OAuth2 Platform Client Factory
  - File provided from QBO code samples.
  - Retrieves OAuth2 information from application properties file.
####	~/…/STIM/Helper/: QBO Service Helper
  - File provided from QBO code samples.
  - Since QBO Controller is not used, this helper file is not either.
####	~/…/Resources/: Home Webpage
  - Information about the project.
  - Connect to Quickbooks button.
####	~/…/Resources/: Connected Webpage
  - Launch STIM button.
  - Functions page button.
  - Old endpoint call for company info within QBO Controller.
####	~/…/Resources/: Functions Webpage
  - Buttons placed to test functionality of backend calls, without requiring frontend. Since the backend is configured to accept one or the other (Headers from frontend or Session object from backend) any call that went on this page needed the appropriate function arguments updated.
  - Import inventory button is currently the only functioning button, on purpose. The other buttons served their purpose but were converted for frontend compatibility. This button takes a CSV file placed in the project directory and imports all the items into the user’s QBO inventory. Used to import sample data required for project.
