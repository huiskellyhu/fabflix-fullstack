document.addEventListener("DOMContentLoaded", function () {
    // FOR RETURNING TO RESULTS
    console.log("setting resultspage href");
    let target_params = sessionStorage.getItem("resultsParams");
    if (target_params.includes("&returning=1")) {
        document.getElementById("resultspage").href = "results.html?" + target_params;
    } else if (target_params.includes("&returning=0")){
        let modified_params = target_params.replace("&returning=0", "&returning=1");
        document.getElementById("resultspage").href = "results.html?" + modified_params;
    } else {
        document.getElementById("resultspage").href = "results.html?" + target_params + "&returning=1";
    }
})


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function getGenresIdandName(genresdata) {
    if(genresdata == null){
        return "";
    }
    let genres = genresdata.split(",");
    let genres_limited = [];

    // NO LIMIT ON GENRES
    for(let i=0; i< genres.length; i++) {
        let [id, name] = genres[i].split(":");
        genres_limited.push('<a style="color: #e60073" href="results.html?genre=' + encodeURIComponent(id) + '">' + name + '</a>');
    }
    return genres_limited.join(", ");
    // let stringarray = stringdata.split(",");
    // stringarray = stringarray.slice(0, num);
    // return stringarray.join(", ");
}

function getStarsIdandName (starsdata) {
    if(starsdata == null){
        return "";
    }
    let stars = starsdata.split(",");
    let stars_limited = [];

    // NO LIMIT ON STARS
    for(let i=0; i< stars.length; i++) {
        let [id, name] = stars[i].split(":");
        stars_limited.push('<a style="color: #e60073" href="single-star.html?id=' + encodeURIComponent(id) + '">' + name + '</a>');
    }
    return stars_limited.join(", ");
}
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie info table from resultData");

    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<h3 class='year-line'>" + resultData[0]["movie_title"] + "</h3>" +
        "<p class='year'>(" + (resultData[0]["movie_year"] || "N/A") + ")</p>");

    document.title = resultData[0]["movie_title"];

    // Populate the movie table
    // Find the empty table body by id "movie_info_table"
    let movie_list_BodyElement = jQuery("#movie_info_table");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        rowHTML += "<td>" + getGenresIdandName(resultData[i]["movie_genres"]) + "</td>";

        // -- MOVIE STARS HYPERLINKS -- (format= id:starname, id:starname,...)
        // rowHTML += "<th>" + resultData[i]["movie_stars"] + "</th>";
        //let stars_limited = limitBy(resultData[i]["movie_stars"], 3);
        rowHTML += "<td>" + getStarsIdandName(resultData[i]["movie_stars"]) + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_rating"]+ "</td>";
        rowHTML += "<td> <button class='btn btn-primary add-to-cart custom-button' data-movie='" + resultData[i]['movie_id'] + "'>Add</button> </td>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movie_list_BodyElement.append(rowHTML);
    }
}
$(document).on("click", ".add-to-cart", function() {
    const movieId = $(this).data("movie");

    console.log("adding movie:", movieId);
    $.ajax(
        "api/addtocart", {
            method: "POST",
            data: {movie_id: movieId},
            success: function(response) {
                alert("Successfully added movie to cart!");
            },
            error: function(xhr, status, error){
                alert("Failed to add movie to cart.");
            }
        }
    );
});

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
// Get id from URL
let movieId = getParameterByName('id');
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
