/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
// function handleMovieListResult(resultData) {
//     console.log("handleMovieListResult: populating movielist table from resultData");
//
//     // Populate the movielist table
//     // Find the empty table body by id "movie_list_body"
//     let movie_list_BodyElement = jQuery("#movie_list_body");
//
//     // Iterate through resultData, no more than 20 entries
//     for (let i = 0; i < Math.min(20, resultData.length); i++) {
//
//         // Concatenate the html tags with resultData jsonObject
//         let rowHTML = "";
//         rowHTML += "<tr>";
//         rowHTML +=
//             "<th>" +
//             // Add a link to single-movie.html with id passed with GET url parameter
//             '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
//             + resultData[i]["movie_name"] +     // display star_name for the link text
//             '</a>' +
//             "</th>";
//         rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
//         rowHTML += "</tr>";
//
//         // Append the row created to the table body, which will refresh the page
//         movie_list_BodyElement.append(rowHTML);
//     }
// }
//
//
// /**
//  * Once this .js is loaded, following scripts will be executed by the browser
//  */
//
// // Makes the HTTP GET request and registers on success callback function handleStarResult
// jQuery.ajax({
//     dataType: "json", // Setting return data type
//     method: "GET", // Setting request method
//     url: "api/movielist", // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
//     success: (resultData) => handleMovieListResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
// });