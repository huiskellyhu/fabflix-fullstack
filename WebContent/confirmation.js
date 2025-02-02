let total_sales_price = 0;

function handleSalesResult(resultData) {
    console.log("handleSalesResult: populating sales table from resultData");


    // Populate the sales_list  table
    // Find the empty table body by id "sales_list_body"
    let sales_list_BodyElement = jQuery("#sales_list_body");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let total_price = parseInt(resultData[i]['sales_quantity']) * parseInt(resultData[i]['movie_price']);
        total_sales_price += total_price;

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["sales_id"] + "</th>";
        // -- MOVIE TITLE HYPERLINKS --
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a style="color: #e60073" href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display movie_title for the link text
            '</a>' +
            "</th>";

        rowHTML += "<th>" + resultData[i]["sales_quantity"] + "</th>";
        rowHTML += "<th> $" + resultData[i]["movie_price"] + "</th>";
        // CHANGE THIS TO TOTAL WITH A FUNCTION quantity * price
        rowHTML += "<th> $" + total_price + "</th>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        sales_list_BodyElement.append(rowHTML);
    }
    // ADDING TOTAL SALE PRICE FOR SYMMETRY
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th></th><th></th><th></th><th></th>"; // empty columns
    rowHTML += "<th id='total-sales-price'> $" + total_sales_price.toString() +  "</th>"; // total_cart_price
    rowHTML += "</tr>";
    sales_list_BodyElement.append(rowHTML);
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/confirmation",
    success: (resultData) => handleSalesResult(resultData)
});