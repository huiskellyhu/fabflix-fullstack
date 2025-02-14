function handleMetadataResult(resultData) {
    console.log("handleMetadataResult: populating metadata table from resultData");


    // Populate the metadata table
    let metadata_BodyElement = jQuery("#metadata-body");

    // EXAMPLE TABLE STRUCTURE
    // <table id=movie_list_table className="table table-striped">
    //     <!-- Create a table header -->
    //     <thead className="table-color">
    //     <tr>
    //         <th>Title</th>
    //     </tr>
    //     </thead>
    //     <tbody id=movie_list_body></tbody>
    // </table>
    // Iterate through resultData
    let metadataContainer = jQuery("#metadata-body");
    metadataContainer.empty();

    let tables = {};

    for (let i = 0; i < resultData.length; i++) {
        let tableName = resultData[i]['table'];

        if (tableName !== "movies" && tableName !== "stars" && tableName !== "sales") {
            continue;
        }

        let columnName = resultData[i]['column'];
        let columnType = resultData[i]['type'] + "(" + resultData[i]['size'] + ")";

        if (!tables[tableName]) {
            tables[tableName] = `<h4>  ${tableName}</h4>
                <table id="${tableName}" class="table">
                    <thead class="table-color">
                        <tr><th>Attribute</th><th>Type</th></tr>
                    </thead>
                    <tbody>
            `;
        }

        tables[tableName] += `
            <tr>
                <td>${columnName}</td>
                <td>${columnType}</td>
            </tr>
        `;
    }

    for (let table in tables) {
        tables[table] += "</tbody></table>";
        metadataContainer.append(tables[table]);
    }
}


// $(document).ready(function () {
//     jQuery.ajax({
//         dataType: "json", // Setting return data type
//         method: "GET", // Setting request method
//         url: "api/metadata",
//         success: (resultData) => handleMetadataResult(resultData)
//     });
// })
// $.ajax({
//     dataType: "json", // Setting return data type
//     method: "GET", // Setting request method
//     url: "api/metadata",
//     success: (resultData) => handleMetadataResult(resultData)
// });

$.ajax(
    "api/dashboardmetadata", {
        method: "GET",
        success: (resultData) => handleMetadataResult(resultData)
    }
);
