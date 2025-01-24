// document.addEventListener("DOMContentLoaded", function () {
//     const all_titles = jQuery("#browse-titles");
//     let prefixes = [];
//
//     for(let i=65;i <=90; i++) {
//         prefixes.push(String.fromCharCode(i));
//     }
//     for(let i=0;i<=9;i++){
//         prefixes.push(i.toString());
//     }
//     prefixes.push("*");
//
//     let rowHTML = "<p>";
//     for (let i = 0; i < prefixes.length; i++) {
//         if (prefixes[i] === "0") {
//             rowHTML += "</p> <p>";
//         }
//         rowHTML +=
//             '<a href="results.html?prefix=' + prefixes[i].toLowerCase() + '">'
//             + prefixes[i] +
//             '</a>   ';
//     }
//     rowHTML += "</p>";
//     all_titles.append(rowHTML);
// })

$(document).ready(function() {
    const all_titles = jQuery("#browse-titles");
    let prefixes = [];

    for(let i=65;i <=90; i++) {
        prefixes.push(String.fromCharCode(i));
    }
    for(let i=0;i<=9;i++){
        prefixes.push(i.toString());
    }
    prefixes.push("*");

    let rowHTML = "<p>";
    for (let i = 0; i < prefixes.length; i++) {
        if (prefixes[i] === "0") {
            rowHTML += "</p> <p>";
        }
        rowHTML +=
            '<a href="results.html?prefix=' + prefixes[i].toLowerCase() + '">'
            + prefixes[i] +
            '</a>   ';
    }
    rowHTML += "</p>";
    all_titles.append(rowHTML);
})














let cart = $("#cart");

/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

    // show the session information 
    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataJson["previousItems"]);
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
    console.log(resultArray);
    let item_list = $("#item_list");
    // change it to html list
    let res = "<ul>";
    for (let i = 0; i < resultArray.length; i++) {
        // each item will be in a bullet point
        res += "<li>" + resultArray[i] + "</li>";
    }
    res += "</ul>";

    // clear the old array and show the new array in the frontend
    item_list.html("");
    item_list.append(res);
}

/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.ajax("api/index", {
        method: "POST",
        data: cart.serialize(),
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousItems"]);
        }
    });

    // clear input form
    cart[0].reset();
}

$.ajax("api/index", {
    method: "GET",
    success: handleSessionData
});

// Bind the submit action of the form to a event handler function
cart.submit(handleCartInfo);
