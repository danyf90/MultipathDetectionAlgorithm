var S = {};

var loadParentPlaces = function (avoidId) {
	var requestedUrl = vineyard.config.serverUrl + "place/hierarchy/";
	return $.getJSON(requestedUrl, {avoidOffsprings: avoidId}, function (hierarchy) {
		var appendPlace = function (place, $container) {
			$container.append('<option value="' + place.id + '">' + place.name + '</option>');
			if (place.children)
				for (var i = 0; i < place.children.length; i++)
					appendPlace(place.children[i], $container);
		};
		
		if (hierarchy.id != null) // if list of parents is not empty
			appendPlace(hierarchy, $("#place-parent select"));
	});
}

var loadPlaceInsertion = function () {
	var requestedUrl = vineyard.config.serverUrl + "place/";
	$("#place-id").remove();
	$("#place-add-attribute").remove();
	$("#loading").css("visibility", "hidden");
	
	/*$.post(requestedUrl, {name: "Luogo nuovo"}, function (data, xhr) {
		if (xhr.status != 201) {
			console.log("Something went wrong..");
			return;
		}
		
		data = $.parseJSON(data);
		$("#place-id").attr('value', data.id);
		$("#place-name").attr('value', "Luogo nuovo");
		
	});*/
};

var showMap = function () {
    // TODO
    console.log("showMap NOT IMPLEMENTED");
};

var loadPlace = function(id) {
    var requestedUrl = vineyard.config.serverUrl + "place/" + id;
    return $.getJSON(requestedUrl, function (place) {
		
		S.place = place;
		
        // Name
        $("#place-name").attr("value", place.name); 
        // Description
        $("#place-description").attr("value", place.description);     
		
        // Position Link
        var $positionLink = $('<a href="#"></a>');
        if (place.latitude !== undefined)
            $positionLink.text("Mostra mappa").on("click", showMap);
       
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
		
		var $attributeTable = $("#place-attributes");
		
		// Attributes
		if (place.attributes !== undefined)
			for (var key in place.attributes) {
				var input = '<input type="text" data-key="' + key + '" class="attribute" value="' + place.attributes[key] + '" />';
				$attributeTable.append('<tr><th>' + key + '</th><td>' + input + '</td></tr>');
			}   
		
		S.placeAddAttribute.on("click", addNewAttribute);
    });
};

var addNewAttribute = function () {
	var input = '<input type="text" class="new-attribute" placeholder="Valore attributo..." />';
	$attributeTable.append('<tr><th><input class="new-attribute-key" placeholder="Nome attributo.." /></th><td>' + input + '</td></tr>')
};

var commitPlaceChanges = function () {
	
	S.loading.find("span").text("Saving...").end().css("visibility", "visible");
	
	var data = $("form").serialize();
	
	var saveAttributes = function () {
		$(".attribute.changed").each(function () {
			console.log("saving" + this.dataset.key + ": " + this.value);
		});
	};
	
	if (data != "")
		$.ajax({
			type: 'PUT',
			url: vineyard.config.serverUrl + "place/" + S.place.id,
			data: data
		}).done(saveAttributes).always(function(){
			S.loading.css("visibility", "hidden");
			window.location.reload();
		}).fail(function() {});
	else saveAttributes();
};

var loadPlaceModification = function (id) {
	
	var placeRequest = loadPlace(id);
	var loadParentPlaceRequest = loadParentPlaces(id);
	
	$.when(placeRequest, loadParentPlaceRequest).then(function () {
		$("#place-parent select option[value='" + S.place.parent + "']")[0].selected = "selected";
		S.loading.css("visibility", "hidden");
		$("#control-ok").on("click", commitPlaceChanges);
		$("input, select").on("change", function () {
			if (!$(this).hasClass("attribute"))
				this.name = this.dataset.name;
			S.controls.css("visibility", "visible");
			S.placeAddAttribute.css("visibility", "hidden");
		});
		
		$("input.attribute").on("change", function() {
			$(this).addClass("changed");
		});
		
	});
};

var init = function() {
	// get id from url
	var id = $("#place-id").attr("value");	
	
	S.controls = $(".controls");
	S.controls.css("visibility", "hidden");
	S.placeAddAttribute = $("#place-add-attribute");
	S.loading = $("#loading");
	
	$("#control-cancel").on("click", function () {
		window.location = "/place/" + id;
	});
	
	if (id == "insert")
		loadPlaceInsertion();
	else loadPlaceModification(id);
};

init();