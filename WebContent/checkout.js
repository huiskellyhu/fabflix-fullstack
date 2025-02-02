// handle total price of shopping cart
let total_cart_price = 0;

function handleShoppingCartResult(resultData) {
    console.log("handleShoppingCartResult: populating shopping cart table from resultData");

    // Populate the shoppingcart_table
    // Find the empty table body by id "shoppingcart_table"
    let shoppingcart_BodyElement = jQuery("#shoppingcart_table");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let total_price = parseInt(resultData[i]['movie_quantity']) * parseInt(resultData[i]['movie_price']);
        total_cart_price += total_price;

        let rowHTML = "";
        rowHTML += "<tr id=resultData[i]['movie_id']>";

        // -- MOVIE TITLE HYPERLINKS --
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a style="color: #e60073" href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display movie_title for the link text
            '</a>' +
            "</th>";

        // QUANTITY NEEDS TO BE ABLE TO GO UP AND DOWN, AND SEND BACK TO SERVLET TO UPDATE AS A POST?
        rowHTML += "<th> <button class='btn btn-secondary me-2 decrease-quantity' data-movie='" + resultData[i]['movie_id'] + "'>-</button>";
        rowHTML += "<span id=quantity-" + resultData[i]['movie_id']+ ">" + resultData[i]["movie_quantity"] +"</span>";
        rowHTML += "<button class='btn btn-secondary ms-2 increase-quantity' data-movie='" + resultData[i]['movie_id'] + "'>+</button></th>";


        // CHANGE THIS INTO A DELETE BUTTON
        // TO DELETE: GET SESSION USER CUSTOMER ID AND THEN MOVIE ID
        rowHTML += "<th> <button class='btn btn-primary delete-from-cart custom-button' data-movie='" + resultData[i]['movie_id'] + "'>Delete</button> </th>";

        rowHTML += "<th id=price-" + resultData[i]['movie_id']+ "> $" + resultData[i]["movie_price"] + "</th>";

        // CHANGE THIS TO TOTAL WITH A FUNCTION quantity * price
        rowHTML += "<th id=total-price-" + resultData[i]['movie_id']+ "> $" + total_price + "</th>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        shoppingcart_BodyElement.append(rowHTML);
    }

    // ADDING PROCEED TO PAYMENT BUTTON AND TOTAL CART PRICE FOR SYMMETRY
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th> <button id='proceedpay' class='btn btn-secondary me-2 custom-button'>Proceed to Payment</button> </th>";
    rowHTML += "<th></th><th></th><th></th>"; // empty columns
    rowHTML += "<th id='total-cart-price'> $" + total_cart_price.toString() +  "</th>"; // total_cart_price
    rowHTML += "</tr>";

    // update total_cart_price "total_cart_price"
    // $("#total_cart_price").text(total_cart_price.toString());
    shoppingcart_BodyElement.append(rowHTML);
}
$(document).on('click', '#proceedpay', function() {
    if(total_cart_price == 0){
        alert("Your cart is empty.");
        return;
    }
    sessionStorage.setItem("total_cart_price", total_cart_price.toString());
    console.log("total cart price saved to session:", total_cart_price.toString());
    window.location.href = 'payment.html';
});

$(document).on("click", ".delete-from-cart", function() {
    const movieId = $(this).data("movie");

    function updateOnDelete(){
        // $("#" + movieid_to_delete).remove();
        // console.log("removed movie:" + movieid_to_delete);
        location.reload();
    }

    console.log("removing movie:", movieId);
    $.ajax({
        url: "api/addtocart?movie_id=" + encodeURIComponent(movieId),
        method: "DELETE",
        success: function() {
            updateOnDelete();
        },
        error: function(xhr, status, error){
            alert("Failed to remove movie from cart.");
        }
        }
    );
});
$(document).on("click", ".increase-quantity, .decrease-quantity", function() {
    const movieId = $(this).data("movie");
    const is_incr = $(this).hasClass("increase-quantity");
    const quantityElement = $("#quantity-" + movieId);
    let curr_quantity = parseInt(quantityElement.text());
    let curr_price = parseInt($("#price-" + movieId).text().replace("$", ""));

    console.log("curr quantity and curr price:", curr_quantity, curr_price);
    if (!is_incr && curr_quantity === 1) return; // lowerbound for quantity

    const newQuantity = is_incr ? curr_quantity + 1 : curr_quantity - 1;
    console.log("new quant:", newQuantity);
    function updateOnQuant(){
        let newTotalPrice = curr_price * newQuantity;
        console.log("new total price:", newTotalPrice);
        $("#total-price-" + movieId).text("$" + newTotalPrice.toString());
        if(!is_incr) {
            // decrease quantity = recalculate total_price and lower total_cart_price
            total_cart_price -= curr_price;
        } else {
            // increase quantity = recalculate total_price and increase total_cart_price
            total_cart_price += curr_price;
        }
        console.log("new cart price:", total_cart_price);
        $("#total-cart-price").text("$" + total_cart_price.toString());
    }


    $.ajax({
        url: "api/addtocart",
        method: "POST",
        data: {movie_id: movieId, movie_quantity: newQuantity.toString()},
        success: function() {
            quantityElement.text(newQuantity);
            updateOnQuant();
            //location.reload();
            console.log("updated quantity");
        },
        error: function(xhr, status, error) {
            alert("Failed to update quantity.");
        }
    });
});

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/shoppingcart",
    success: (resultData) => handleShoppingCartResult(resultData)
});