document.addEventListener("DOMContentLoaded", function () {
    // PREVIOUS AND NEXT PAGE FUNCTIONALITY
    const prevBtn = jQuery("#prevpage");
    const currpage = jQuery("#currpage");
    const nextBtn = jQuery("#nextpage");

    // getting current page number from url
    const urlParams = new URLSearchParams(window.location.search);
    let currpage_num = parseInt(urlParams.get("page")) || 1;
    currpage.text(`${currpage_num}`);

    prevBtn.prop("disabled", currpage_num == 1); // disable prev if first page

    // update page for prevBtn
    prevBtn.on("click", function() {
        if (currpage_num > 1) {
            urlParams.set("page", (currpage_num - 1).toString());
            urlParams.set("returning", "0");
            window.location.search = urlParams.toString();
        }
    });
    // update page for nextBtn
    nextBtn.on("click", function() {
        // SET CONDITION FOR DISABLING nextBtn IN handleMovieListResult
        urlParams.set("page", (currpage_num + 1).toString());
        urlParams.set("returning", "0");
        window.location.search = urlParams.toString();

    });


    // DROPDOWN SORTING MENUS FUNCTIONALITY
    const updateBtn = jQuery("#updateBtn");
    const item_limit = jQuery("#item_limit");
    const sorting_menu = jQuery("#sorting_menu");

    // TO SET CURRENT VALUES OF THE DROPDOWN MENUS
    //const urlParams = new URLSearchParams(window.location.search);

    const sortValue = urlParams.get("sort") || "title_asc_rating_asc";
    const limit = urlParams.get("limit") || "25";

    sorting_menu.val(sortValue);
    item_limit.val(limit);


    // TO UPDATE VALUES OF THE DROPDOWN MENUS AND REFRESH
    updateBtn.on("click", function() {
        const sortValue = sorting_menu.val();
        const limit = item_limit.val();

        urlParams.set("sort", sortValue);
        urlParams.set("limit", limit);
        urlParams.set("page", "1"); // reset page if changing sorting
        urlParams.set("returning", "0");
        window.location.search = urlParams.toString();
        console.log("URL params:", window.location.search);
    })


    sessionStorage.setItem("resultsParams", urlParams.toString());
    console.log("saved resultsparams", sessionStorage.getItem("resultsParams"));
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

function getGenresIdandName(genresdata) {
    if(genresdata == null){
        return "";
    }
    let genres = genresdata.split(",");
    let genres_limited = [];

    for(let i=0; i< Math.min(3, genres.length); i++) {
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

    for(let i=0; i< Math.min(3, stars.length); i++) {
        let [id, name] = stars[i].split(":");
        stars_limited.push('<a style="color: #e60073" href="single-star.html?id=' + encodeURIComponent(id) + '">' + name + '</a>');
    }
    return stars_limited.join(", ");
}
function handleMovieListResult(resultData) {
    console.log("handleMovieListResult: populating movielist table from resultData");
    console.log("URL params:", window.location.search);
    console.log(resultData[0]);
    if(resultData[0] === undefined){
        $("#nextpage").prop("disabled", true);
        return;
    }
    // disabling nextBtn depending on total_results
    const total_results = resultData[0]['total_results'] || 0;
    $("#nextpage").prop("disabled", page >= Math.ceil(total_results / limit));
    console.log("total_results: ", total_results);


    // Populate the movielist table
    // Find the empty table body by id "movie_list_body"
    let movie_list_BodyElement = jQuery("#movie_list_body");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        // -- MOVIE TITLE HYPERLINKS --
        rowHTML +=
            "<td>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a style="color: #e60073" href="single-movie.html?id=' + encodeURIComponent(resultData[i]['movie_id']) + '">'
            + resultData[i]["movie_title"] +     // display movie_title for the link text
            '</a>' +
            "</td>";

        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        // rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
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


// Get id from URL
let genreId = getParameterByName('genre');
let prefixId = getParameterByName('prefix');
let title = getParameterByName('title');
let year = getParameterByName('year');
let director = getParameterByName('director');
let star = getParameterByName('star');
let sort = getParameterByName('sort');
let limit = getParameterByName('limit') || 25;
let page = getParameterByName('page') || 1;
let returning = getParameterByName('returning') || 0;
let fts = getParameterByName('fts');

if (genreId) {
    apiURL = "api/results?genre=" + genreId.trim();
}
else if (prefixId) {
    apiURL = "api/results?prefix=" + prefixId.trim();
}
else {
    apiURL = "api/results?";
    apiURL += "title=" + title;
    apiURL += "&year=" + year;
    apiURL += "&director=" + director;
    apiURL += "&star=" + star;
}
if (sort) {
    apiURL += "&sort=" + sort;
}
if (limit) {
    apiURL += "&limit=" + limit;
}
if (page) {
    apiURL += "&page=" + page;
}
if (returning) {
    apiURL += "&returning=" + returning;
}
if(fts){
    apiURL += "&fts=" + fts;
}
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: apiURL,
    success: (resultData) => handleMovieListResult(resultData)
});