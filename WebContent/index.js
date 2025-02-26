// SETTING UP BROWSING FOR GENRES AND TITLES
function getAllTitles(){
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
            '<a style="color: #e60073; font-size: 25px" href="results.html?prefix=' + prefixes[i].toLowerCase() + '">'
            + prefixes[i] +
            '</a>   ';
    }
    rowHTML += "</p>";
    all_titles.append(rowHTML);
}

function handleGenresResult(resultData) {
    // EXAMPLE !!
    // <div class="row">
    //    <div class="col-md-3"><a href="results.html?genre=1">Action</a></div>
    //    <div class="col-md-3"><a href="results.html?genre=2">Adult</a></div>
    //    <div class="col-md-3"><a href="results.html?genre=3">Adventure</a></div>
    //    <div class="col-md-3"><a href="results.html?genre=4">Animation</a></div>
    // </div>

    const all_genres = jQuery("#browse-genres");

    // Iterate through resultData
    let rowHTML = "";
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        if (i%4 == 0){
            rowHTML += "<div class='row'>";
        }
        rowHTML +=
            '<div class="col-md-3"><a style="color: #e60073; font-size: 25px" href="results.html?genre=' + resultData[i]['genre_id'] + '">'
            + resultData[i]['genre_name'] +
            '</a></div>';

        if (i%4 == 3 || i == resultData.length-1){
            rowHTML += "</div>";
        }
    }
    all_genres.append(rowHTML);

}

$(document).ready(function () {
    // GETTING ALL GENRES
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/genres", // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
        success: (resultData) => handleGenresResult(resultData) // Setting callback function to handle data returned successfully by the GenresServlet
    });
    // GETTING ALL STARTS OF TITLES
    getAllTitles();
})

// --- PROJECT4 IMPLEMENTATIONS BELOW ---
/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    // TODO: if you want to check past query results first, you can do it here

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/autocomplete?query=" + encodeURIComponent(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")
    console.log(data);

    // parse the string into JSON
    // var jsonData = JSON.parse(data);
    let jsonData = data;
    //console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"])
    window.location.href = "single-movie.html?id=" + encodeURIComponent(suggestion["data"]["movieId"]);
}


$('#ft-title').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },

    // TODO: add other parameters, such as minimum characters
    minChars: 3,
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements

});

/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here

    window.location.href = "results.html?title=" + encodeURIComponent(query) + "&year=&director=&star=";
    // try with this if doesn't work + "&year=&director=&star=";
}

$('#ft-title').keypress(function(event) {
    if (event.keyCode == 13) {
        handleNormalSearch($('#ft-title').val());
    }
});


$('#ft-search-btn').click(function() {
    handleNormalSearch($('#ft-title').val());
})
