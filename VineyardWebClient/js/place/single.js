var showMap = function () {
    // TODO
    console.log("showMap NOT IMPLEMENTED");
};

var loadPlace = function () {
    // get id from url
	var id = $("#place-id").attr("value");
    var requestedUrl = vineyard.config.serverUrl + "place/" + id;
	
    $.getJSON(requestedUrl, function (place) {
        // Name
        $("#place-name").attr("value", place.name);
        
        // Description
        $("#place-description").attr("value", place.description);
        
        // Position Link
        var $positionLink = $('<a href="#"></a>');
        if (place.latitude !== undefined)
            $positionLink
            .text("Mostra mappa")
            .on("click", function() {
                showMap();
            });
        else $positionLink
            .text("Inserisci un punto geografico");
        
        $("#place-location-link").append($positionLink);
        
        // Children
        var $table = $("#place-table-body");
        var numChildren = (place.children != null) ? place.children.length : 0;
        
        if (numChildren > 0) {
            $table.append('<tr><th>Composizione</th><td id="place-first-child"></td></tr>');
            $("#place-first-child").append('<a href="/place/' + place.children[0].id + '">' + place.children[0].name + '</a>');
            
            if (numChildren > 1)
                for(var i = 1; i < numChildren; i++)
                    $table.append('<tr><th></th><td><a href="/place/' + place.children[i].id + '">' + place.children[i].name + '</a></td></tr>');
        }
        
        // Photo
        if (place.photo !== undefined) {
            $("#place-photo")
			.css("background-image", "url(" + vineyard.config.serverUrl + "photo/" + place.photo + ")")
			.css("background-size", "cover")
			.find(".add")
				.removeClass("add")
				.addClass("modify")
			.end()
				.find(".delete")
				.show();
			
        }
		
		// Attributes
		
		if (place.attributes !== undefined)
			for (var key in place.attributes) {
				var input = '<input type="text" name="' + key + '" class="attribute" value="' + place.attributes[key] + '" />';
				$table.append('<tr><th>' + key + '</th><td>' + input + '</td></tr>');
			}
		
        $("#loading").css("visibility", "hidden");
        
    });
}

loadPlace();