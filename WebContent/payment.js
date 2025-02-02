document.addEventListener("DOMContentLoaded", function () {
    $("#final-payment").text("Total Cart Price: $" + sessionStorage.getItem("total_cart_price"));
});


let payment_form = $("#payment-form");

function handlePaymentResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If payment succeeds, it will redirect the user to confirmation.html
    if (resultDataJson["status"] === "success") {
        // add to sales and remove from cart_items
        $.ajax(
            "api/placeorder", {
                method: "POST",
                success: function(){
                    window.location.replace("confirmation.html");
                },
                error: function(xhr, status, error) {
                    alert("Failed to place order.");
                }
            }
        );

    } else {
        // If payment fails, the web page will display
        // error messages on <div> with id "payment_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#payment-error-message").text(resultDataJson["message"]);
    }


    //
}
/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the payment form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}


// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);