function handleShoppingCartResult(resultData) {
    console.log("handleShoppingCartResult: populating shopping cart table from resultData");

    // Populate the shoppingcart_table
    // Find the empty table body by id "shoppingcart_table"
    let shoppingcart_BodyElement = jQuery("#shoppingcart_table");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        // -- MOVIE TITLE HYPERLINKS --
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display movie_title for the link text
            '</a>' +
            "</th>";

        // QUANTITY NEEDS TO BE ABLE TO GO UP AND DOWN, AND SEND BACK TO SERVLET TO UPDATE AS A POST?
        rowHTML += "<th>" + resultData[i]["quantity"] + "</th>";


        // CHANGE THIS INTO A DELETE BUTTON
        // TO DELETE: GET SESSION USER CUSTOMER ID AND THEN MOVIE ID
        //rowHTML += "<th> <button class='btn btn-primary add-to-cart' data-movie='" + resultData[i]['movie_id'] + "'>Delete</button> </th>";

        rowHTML += "<th>" + resultData[i]["price"] + "</th>";

        // CHANGE THIS TO TOTAL WITH A FUNCTION quantity * price
        //rowHTML += "<th>" + getStarsIdandName(resultData[i]["movie_stars"]) + "</th>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        shoppingcart_BodyElement.append(rowHTML);
    }
}



// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/shoppingcart",
    success: (resultData) => handleShoppingCartResult(resultData)
});