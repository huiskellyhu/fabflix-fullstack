
let add_star_form = $("#add-star-form");

function handleResult(resultDataString) {
    let resultDataJson = resultDataString;

    console.log("handle add star response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);


    if (resultDataJson["status"] === "success") {
        $("#add-star-message").text(resultDataJson["message"]);
        // $.ajax(
        //     "api/addstar", {
        //         method: "POST",
        //         success: function(){
        //             $("#add-star-message").text(resultDataJson["message"]);
        //         },
        //         error: function(xhr, status, error) {
        //             alert("Failed to place order.");
        //         }
        //     }
        // );

    } else {
        // If payment fails, the web page will display
        // error messages on <div> with id "payment_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#add-star-message").text(resultDataJson["message"]);
    }


    //
}
/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitPaymentForm(formSubmitEvent) {
    console.log("submit add star form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/addstar", {
            method: "POST",
            // Serialize the payment form to the data sent by POST request
            data: add_star_form.serialize(),
            success: handleResult
        }
    );
}


// Bind the submit action of the form to a handler function
add_star_form.submit(submitPaymentForm);