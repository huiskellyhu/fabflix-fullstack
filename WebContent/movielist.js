/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function limitBy(stringdata, num) {
    let stringarray = stringdata.split(",");
    stringarray = stringarray.slice(0, num);
    return stringarray.join(", ");
}

function getStarsIdandName (starsdata) {
    let stars = starsdata.split(",");
    let stars_limited = [];

    for(let i=0; i< Math.min(3, stars.length); i++) {
        let [id, name] = stars[i].split(":");
        stars_limited.push('<a href="single-star.html?id=' + id + '">' + name + '</a>');
    }
    return stars_limited.join(", ");
}
function handleMovieListResult(resultData) {
    console.log("handleMovieListResult: populating movielist table from resultData");

    // Populate the movielist table
    // Find the empty table body by id "movie_list_body"
    let movie_list_BodyElement = jQuery("#movie_list_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
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

        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        // rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
        let genres_limited = limitBy(resultData[i]["movie_genres"], 3);
        rowHTML += "<th>" + genres_limited + "</th>";

        // -- MOVIE STARS HYPERLINKS -- (format= id:starname, id:starname,...)
        // rowHTML += "<th>" + resultData[i]["movie_stars"] + "</th>";
        //let stars_limited = limitBy(resultData[i]["movie_stars"], 3);
        rowHTML += "<th>" + getStarsIdandName(resultData[i]["movie_stars"]) + "</th>";


        rowHTML += "<th>" + resultData[i]["movie_rating"]+ "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movie_list_BodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movielist", // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
    success: (resultData) => handleMovieListResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});