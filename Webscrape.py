import csv
from bs4 import BeautifulSoup 
from selenium import webdriver
import json
import numpy as np
from flask import Flask
from flask_restful import Api, Resource
from flask_cors import CORS

MAX = 10000000

def getUrl(search):
    template = 'https://www.amazon.com/s?k={}&ref=nb_sb_noss_1'
    search = search.replace(' ', '+')
    return template.format(search)

def cheapest(item):
    firstmin = MAX
    secmin = MAX
    thirdmin = MAX

    driver = webdriver.Chrome()
    url = getUrl(item)
    driver.get(url)
    soup = BeautifulSoup(driver.page_source, 'html.parser')
    result = soup.find_all('div', {'data-component-type': 's-search-result'})

    records = []
    secRecords = []
    thirdRecords = []
    newPrice = []
    for item in result:
        record = extract_record(item)
        if record:
            if "Ounce" in record[2] or "Oz" in record[2]:
                newAmount = filter(str.isdigit, record[2])
                finalAmount = "".join(newAmount)
                newPrice.append(finalAmount)
                records.append(record)
            elif record[0] and record[2] == '':
                secRecords.append(record)
            elif record[3] and record[4] and record[5] and record[0] == '' and record[1] == '':
                thirdRecords.append(record)
                          
    driver.close()
    records = np.array(records)
    newPrice = np.array(newPrice)
    secRecords = np.array(secRecords)
    thirdRecords = np.array(thirdRecords)


    if len(newPrice) > 0:
        for i in range(len(newPrice)):
        
        # Check if current element
        # is less than firstmin, 
        # then update first,second
        # and third

            if float(newPrice[i]) < float(firstmin):
                thirdmin = secmin
                secmin = firstmin
                firstmin = newPrice[i]
    
            # Check if current element is
            # less than secmin then update
            # second and third
            elif float(newPrice[i]) < float(secmin):
                thirdmin = secmin
                secmin = newPrice[i]
    
            # Check if current element is
            # less than,then upadte third
            elif float(newPrice[i]) < float(thirdmin):
                thirdmin = newPrice[i]
        #Iteration match
        for i in range(len(newPrice)):
            if(newPrice[i] == firstmin):
                iteration1 = i
            if(newPrice[i] == secmin):
                iteration2 = i
            if(newPrice[i] == thirdmin):
                iteration3 = i
        
        for i in range(records.shape[0]):
            newAmount2 = filter(str.isdigit, record[2])
            if(newAmount2 == newPrice[iteration1]):
                it1 = i
            if(newAmount2 == newPrice[iteration2]):
                it2 = i
            if(newAmount2 == newPrice[iteration3]):
                it3 = i


        final1 = records[iteration1]
        final2 = records[iteration2]
        final3 = records[iteration3]

        if "ounce" in records[iteration1][4].lower():
            res1 = records[iteration1][4].lower().split("ounce")[0]
            res1 = res1.split()[-1]

        elif "pounds" in records[iteration1][4].lower():
            res1 = records[iteration1][4].lower().split("pounds")[0]
            res1 = res1.split()[-1]
            res1 = res1.replace('(', '')
            res1 = float(res1) * 16
        elif "pound" in records[iteration1][4].lower():
            res1 = records[iteration1][4].lower().split("pound")[0]
            res1 = res1.split()[-1]
            res1 = res1.replace('(', '')
            res1 = float(res1) * 16
        elif "lbs" in records[iteration1][4].lower():
            res1 = records[iteration1][4].lower().split("lbs")[0]
            res1 = res1.split()[-1]
            res1 = res1.replace('(', '')
            res1 = float(res1) * 16
        elif "lb" in records[iteration1][4].lower():
            res1 = records[iteration1][4].lower().split("lb")[0]
            res1 = res1.split()[-1]
            res1 = res1.replace('(', '')
            res1 = float(res1) * 16
        elif "fl" in records[iteration1][4].lower():
            res1 = records[iteration1][4].lower().split("fl")[0]
            res1 = res1.split()[-1]
            res1 = res1.replace('(', '')
        elif "oz" in records[iteration1][4].lower():
            res1 = records[iteration1][4].lower().split("oz")[0]
            res1 = res1.split()[-1]
            res1 = res1.replace('(', '')
        elif "liter" in records[iteration1][4].lower():
            res1 = records[iteration1][4].lower().split("liter")[0]
            res1 = res1.split()[-1]
            res1 = res1.replace('(', '')
            res1 = float(res1) * 33.814
        else:
            res1 = records[iteration1][4]

        
        if "ounce" in records[iteration2][4].lower():
            res2 = records[iteration2][4].lower().split("ounce")[0]
            res2 = res2.split()[-1]
            res2 = res2.replace('(', '')

        elif "pounds" in records[iteration2][4].lower():
            res2 = records[iteration2][4].lower().split("pounds")[0]
            res2 = res2.split()[-1]
            res2 = res2.replace('(', '')
            res2 = float(res2) * 16
        elif "pound" in records[iteration2][4].lower():
            res2 = records[iteration2][4].lower().split("pound")[0]
            res2 = res2.split()[-1]
            res2 = res2.replace('(', '')
            res2 = float(res2) * 16
        elif "lbs" in records[iteration2][4].lower():
            res2 = records[iteration2][4].lower().split("lbs")[0]
            res2 = res2.split()[-1]
            res2 = res2.replace('(', '')
            res2 = float(res2) * 16
        elif "lb" in records[iteration2][4].lower():
            res2 = records[iteration2][4].lower().split("lb")[0]
            res2 = res2.split()[-1]
            res2 = res2.replace('(', '')
            res2 = float(res2) * 16
        elif "fl" in records[iteration2][4].lower():
            res2 = records[iteration2][4].lower().split("fl")[0]
            res2 = res2.split()[-1]
            res2 = res2.replace('(', '')
        elif "oz" in records[iteration2][4].lower():
            res2 = records[iteration2][4].lower().split("oz")[0]
            res2 = res2.split()[-1]
            res2 = res2.replace('(', '')
        elif "liter" in records[iteration2][4].lower():
            res2 = records[iteration2][4].lower().split("liter")[0]
            res2 = res2.split()[-1]
            res2 = res2.replace('(', '')
            res2 = float(res2) * 33.814
        else:
            res2 = records[iteration2][4]


        if "ounce" in records[iteration3][4].lower():
            res3 = records[iteration3][4].lower().split("ounce")[0]
            res3 = res3.split()[-1]
            res3 = res3.replace('(', '')

        elif "pounds" in records[iteration3][4].lower():
            res3 = records[iteration3][4].lower().split("pounds")[0]
            res3 = res3.split()[-1]
            res3 = res3.replace('(', '')
            res3 = float(res3) * 16
        elif "pound" in records[iteration3][4].lower():
            res3 = records[iteration3][4].lower().split("pound")[0]
            res3 = res3.split()[-1]
            res3 = res3.replace('(', '')
            res3 = float(res1) * 16
        elif "lbs" in records[iteration3][4].lower():
            res3 = records[iteration3][4].lower().split("lbs")[0]
            res3 = res3.split()[-1]
            res3 = res3.replace('(', '')
            res3 = float(res3) * 16
        elif "lb" in records[iteration3][4].lower():
            res3 = records[iteration3][4].lower().split("lb")[0]
            res3 = res3.split()[-1]
            res3 = res3.replace('(', '')
            res3 = float(res3) * 16
        elif "fl" in records[iteration3][4].lower():
            res3 = records[iteration3][4].lower().split("fl")[0]
            res3 = res3.split()[-1]
            res3 = res3.replace('(', '')
        elif "oz" in records[iteration3][4].lower():
            res3 = records[iteration3][4].lower().split("oz")[0]
            res3 = res3.split()[-1]
            res3 = res3.replace('(', '')
        elif "liter" in records[iteration3][4].lower():
            res3 = records[iteration3][4].lower().split("liter")[0]
            res3 = res3.split()[-1]
            res3 = res3.replace('(', '')
            res3 = float(res3) * 33.814
        else:
            res3 = records[iteration3][4]

        
        subItem1 = {
            "Rating": final1[0],
            "Count": final1[1],
            "Amount": final1[2],
            "Total": str(res1) + " oz",
            "price": final1[3],
            "description": final1[4],
            "url": final1[5]
        }
        subItem2 = {
            "Rating": final2[0],
            "Count": final2[1],
            "Amount": final2[2],
            "Total": str(res2) + " oz",
            "price": final2[3],
            "description": final2[4],
            "url": final2[5]
        }
        subItem3 = {
            "Rating": final3[0],
            "Count": final3[1],
            "Amount": final3[2],
            "Total": str(res3) + " oz",
            "price": final3[3],
            "description": final3[4],
            "url": final3[5]
        }

    elif len(secRecords) > 0:
        for i in range(secRecords.shape[0]):
            secRecords[i][3] = secRecords[i][3].replace("$", "")
          
        # Check if current element
        # is less than firstmin, 
        # then update first,second
        # and third
            if float(secRecords[i][3]) < float(firstmin):
                thirdmin = secmin
                secmin = firstmin
                firstmin = secRecords[i][3]
    
            # Check if current element is
            # less than secmin then update
            # second and third
            elif float(secRecords[i][3]) < float(secmin):
                thirdmin = secmin
                secmin = secRecords[i][3]
    
            # Check if current element is
            # less than,then upadte third
            elif float(secRecords[i][3]) < float(thirdmin):
                thirdmin = secRecords[i][3]
        #Iteration match
        for i in range(len(secRecords)):
            if(secRecords[i][3] == firstmin):
                iteration1 = i
            if(secRecords[i][3] == secmin):
                iteration2 = i
            if(secRecords[i][3] == thirdmin):
                iteration3 = i
        
        final1 = secRecords[iteration1]
        final2 = secRecords[iteration2]
        final3 = secRecords[iteration3]

        subItem1 = {
            "Rating": final1[0],
            "Count": final1[1],
            "Amount": "N/A",
            "Total": "N/A",
            "price": "$" + final1[3],
            "description": final1[4],
            "url": final1[5]
        }
        subItem2 = {
            "Rating": final2[0],
            "Count": final2[1],
            "Amount": "N/A",
            "Total": "N/A",
            "price": "$" + final2[3],
            "description": final2[4],
            "url": final2[5]
        }
        subItem3 = {
            "Rating": final3[0],
            "Count": final3[1],
            "Amount": "N/A",
            "Total": "N/A",
            "price": "$" + final3[3],
            "description": final3[4],
            "url": final3[5]
        }
    elif len(thirdRecords) > 0:
        for i in range(thirdRecords.shape[0]):
            thirdRecords[i][3] = thirdRecords[i][3].replace("$", "").replace(",", "")
          
        # Check if current element
        # is less than firstmin, 
        # then update first,second
        # and third
            if float(thirdRecords[i][3]) < float(firstmin):
                thirdmin = secmin
                secmin = firstmin
                firstmin = thirdRecords[i][3]
    
            # Check if current element is
            # less than secmin then update
            # second and third
            elif float(thirdRecords[i][3]) < float(secmin):
                thirdmin = secmin
                secmin = thirdRecords[i][3]
    
            # Check if current element is
            # less than,then upadte third
            elif float(thirdRecords[i][3]) < float(thirdmin):
                thirdmin = thirdRecords[i][3]
        #Iteration match
        for i in range(len(thirdRecords)):
            if(thirdRecords[i][3] == firstmin):
                iteration1 = i
            if(thirdRecords[i][3] == secmin):
                iteration2 = i
            if(thirdRecords[i][3] == thirdmin):
                iteration3 = i
        
        final1 = thirdRecords[iteration1]
        final2 = thirdRecords[iteration2]
        final3 = thirdRecords[iteration3]

        subItem1 = {
            "Rating": "Not currenty available. Please try again",
            "Count": "Not currenty available. Please try again",
            "Amount": "N/A",
            "Total": "N/A",
            "price": final1[3],
            "description": final1[4],
            "url": final1[5]
        }
        subItem2 = {
            "Rating": "Not currenty available. Please try again",
            "Count": "Not currenty available. Please try again",
            "Amount": "N/A",
            "Total": "N/A",
            "price": final2[3],
            "description": final2[4],
            "url": final2[5]
        }
        subItem3 = {
            "Rating": "Not currenty available. Please try again",
            "Count": "Not currenty available. Please try again",
            "Amount": "N/A",
            "Total": "N/A",
            "price": final3[3],
            "description": final3[4],
            "url": final3[5]
        }

    else:
        errorItem = []
        subItem1 = {
            "Rating": "Please try again",
            "Count": "Please try again",
            "Amount": "Please try again",
            "Total": "Please try again",
            "price": "Please try again",
            "description": "Please try again",
            "url": "Please try again"
        }
        errorItem.append(subItem1)
        return errorItem
    


    items = []
    items.append(subItem1)
    items.append(subItem2)
    items.append(subItem3)

    
    return items


def extract_record(item):
    atag = item.h2.a
    description = atag.text.strip()
    url = 'https://www.amazon.com' + atag.get('href')

    try:
        price_parent = item.find('span', 'a-price')
        price = price_parent.find('span', 'a-offscreen').text
    
    except AttributeError:
        return

    try:
        rating = item.i.text
        review_count = item.find('span', 'a-size-base').text

    except AttributeError:
        rating = ''
        review_count = ''

    try: 
        amount = item.find('span','a-size-base a-color-secondary').text
    except AttributeError:
        amount = ''
    
    result = (rating, review_count, amount, price, description, url)

    return result

app = Flask(__name__)
api = Api(app)
CORS(app)

class Web(Resource):
    def get(self, name):
        return cheapest(name)
api.add_resource(Web, "/subItem/<string:name>")
        
if __name__ == "__main__":
    app.run(debug=True)