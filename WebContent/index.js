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
            '<a href="results.html?prefix=' + prefixes[i].toLowerCase() + '">'
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
            '<div class="col-md-3"><a href="results.html?genre=' + resultData[i]['genre_id'] + '">'
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
