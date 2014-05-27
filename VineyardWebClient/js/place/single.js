// some shared state
var S = {};

///////////////////////////////
/// PLACE INSERTION
///////////////////////////////

var insertPlace = function () {
	// TODO minimum validation!
	var requestedUrl = vineyard.config.serverUrl + "place/";
	$.post(requestedUrl, $("form").serialize(), function (data, xhr) {

		data = $.parseJSON(data);

		if (xhr.status != 201) {
			console.log(data);
			return;
		}

		window.location = "/place/" + data.id;
	});
};

var loadPlaceInsertion = function () {
	loadParentPlaces();
	$("#place-id").remove();
	$("#place-add-attribute").remove();
	$(".controls").css("visibility", "visible");
	showLocationPicker();

	$("#control-ok").on("click", insertPlace);
	$("#control-cancel").on("click", function(){ window.location = "/place"; });
	$("#place-attributes").append('<tr><th></th><td style="font-size: 0.7em;">NOTE: A photo and attributes can be added once a place has been created.</td></tr>');

	$("table input, table select").on("change", function () {
		this.name = this.dataset.name;
	});
};

///////////////////////////////
/// PLACE MODIFICATION
///////////////////////////////

var showLocationPicker = function () {

	if (S.locationPickerLoaded == null) {
		S.latitude = $("#place-latitude");
		S.longitude = $("#place-longitude");

		$("#location-picker").locationpicker({
			location: { latitude: S.latitude.val(), longitude: S.longitude.val() },
			radius: 0,
			inputBinding: {
				latitudeInput: S.latitude,
				longitudeInput: S.longitude
			},
		 	onchanged: function () {
			  	S.controls.css("visibility", "visible");
				$("#place-latitude, #place-longitude").each(function(){
					this.name = this.dataset.name;
				});
			}
		});

		S.locationPickerLoaded = true;
		S.showLocationPicker = $("#show-location-picker");
		S.hideLocationPicker = $("#hide-location-picker");
		S.hideLocationPicker.on("click", hideLocationPicker);
		
	}

	$("#place-photo").addClass("show-location");
	$("#place-latitude, #place-longitude").attr("type", "text");
	S.hideLocationPicker.css("visibility", "visible");
  	S.showLocationPicker.hide();
  
};

var hideLocationPicker = function () {
	$("#place-photo").removeClass("show-location");
	$("#place-latitude, #place-longitude").attr("type", "hidden");
	S.hideLocationPicker.css("visibility", "hidden");
 	S.showLocationPicker.show();
};

var showError = function () {
    // TODO
    console.log("Something gone wrong..");
	console.log(arguments);
};

var addPhoto = function () {

	S.progress = $('progress');

	S.newPhoto.attr("name", "photo");
	var imageForm = $("<form></form>").append(S.newPhoto)[0];
	var formData = new FormData(imageForm);

	var progressHandlingFunction = function (e) {
		if(e.lengthComputable){
			S.progress.attr({value:e.loaded,max:e.total});
		}
	};

	$.ajax({
        url: vineyard.config.serverUrl + "place/" + S.place.id + "/photo/",
        type: 'POST',
        xhr: function() {  // Custom XMLHttpRequest
            var myXhr = $.ajaxSettings.xhr();
            if(myXhr.upload){ // Check if upload property exists
                myXhr.upload.addEventListener('progress',progressHandlingFunction, false); // For handling the progress of the upload
            }
            return myXhr;
        },
		// Form data
        data: formData,
		beforeSend: function () { S.progress.css("visibility", "visible"); },
		success: function (response) {
			S.progress.css("visibility", "hidden");
			response = JSON.parse(response);
			setPlacePhoto(response.url);
		},
        //Options to tell jQuery not to process data or worry about content-type.
        cache: false,
        contentType: false,
        processData: false
    });
};

var setPlacePhoto = function (photoUrl) {
	$("#place-photo")
	.css("background-image", "url(" + vineyard.config.serverUrl + "photo/" + photoUrl + ")")
	.css("background-size", "cover")
	.find(".add")
		.removeClass("add")
		.addClass("modify")
	.end()
		.find(".delete")
		.show();
};

var deletePhoto = function () {
	$.ajax({
		url: vineyard.config.serverUrl + "place/" + S.place.id + "/photo/",
		type: "DELETE"
	}).done(function() {
		$("#place-photo")
		// remove inline CSS
		// .removeCss() ???
		.find(".add")
			.removeClass("modify")
			.addClass("add")
		.end()
			.find(".delete")
			.hide();
	});
};

var resetForm = function () {
	S.controls.css("visibility", "hidden");
	S.placeAddAttribute.css("visibility", "visible");
	$("input, select").not(".attribute").removeAttr("name");
	$("input.attribute").removeClass("changed");
	$(".new-attribute-key").each(function(){
		var $this = $(this);

		var key = this.value;
		var value = $this.parents("tr").find(".new-attribute").val();

		addAttributeRow(key, value);
		$this.parents("tr").remove();
	});
};

// loads parent <select></select> with possible values only
var loadParentPlaces = function (avoidId) {
	var requestedUrl = vineyard.config.serverUrl + "place/hierarchy/";

	var data = {};
	if (avoidId != null) data = {avoidOffsprings: avoidId};

	return $.getJSON(requestedUrl, data, function (hierarchy) {
		var appendPlace = function (place, $container) {
			$container.append('<option value="' + place.id + '">' + place.name + '</option>');
			if (place.children)
				for (var i = 0; i < place.children.length; i++)
					appendPlace(place.children[i], $container);
		};

		if (hierarchy.id != null) // if list of parents is not empty
			appendPlace(hierarchy, $("#place-parent select"));
	});
};

var addAttributeRow = function (key, value) {
	var input = '<input type="text" data-key="' + key + '" class="attribute" value="' + value + '" />';
	var $row = $('<tr><th>' + key + '</th><td style="display: flex;">' + input + '</td></tr>');

	var $deleteSpan = $('<span class="delete-row"></span>');
	$deleteSpan.on("click", removeAttribute);
	$row.find("td").append($deleteSpan);

	S.placeAttributes.append($row);
};

var addNewAttribute = function () {
	var input = '<input type="text" class="new-attribute" placeholder="Valore attributo..." />';
	var $row = $('<tr><th><input class="new-attribute-key" placeholder="Nome attributo.." /></th><td style="display: flex;">' + input + '</td></tr>');
	var $deleteSpan = $('<span class="delete-row"></span>');
	$deleteSpan.on("click", removeAttribute);
	$row.find("td").append($deleteSpan);

	S.placeAttributes.append($row);
	S.controls.css("visibility","visible");
};

var removeAttribute = function () {
	// this = clicked span
	$span = $(this);
	$inputSibling = $span.siblings("input");

	if ($inputSibling.hasClass("new-attribute")) // remove uncommitted attribute
		$span.parents("tr").remove();
	else if ($inputSibling.hasClass("attribute")) { // remove alredy present attribute
		var requestOptions = {
			url: vineyard.config.serverUrl + "place/" + S.place.id + "/attribute/",
			type: 'DELETE',
			data: { key: $inputSibling.data("key") }
		};

		$.ajax( requestOptions ).done(function () {
			$span.parents("tr").remove();
		});
	}
};

var loadPlace = function(id) {
    var requestedUrl = vineyard.config.serverUrl + "place/" + id;

    return $.getJSON(requestedUrl, function (place) {

		S.place = place;

        // Name
        $("#place-name").val(place.name);
        // Description
        $("#place-description").val(place.description);

        // Position Link
        if (place.latitude !== undefined) {
            $("#place-location-link a").text("Show map").on("click", showLocationPicker);
			$("#place-latitude").val(place.latitude);
			$("#place-longitude").val(place.longitude);
		}

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
        if (place.photo !== undefined)
          setPlacePhoto(place.photo);

        // Attributes
        if (place.attributes !== undefined)
          for (var key in place.attributes)
            addAttributeRow(key, place.attributes[key]);

        S.placeAddAttribute.on("click", addNewAttribute);
    });
};

var commitPlaceChanges = function () {

	var data = $("form").serialize();

	var saveAttributes = function () {
		var defObjs = [];

		$(".attribute.changed").each(function () {
			var requestOptions = {
				url: vineyard.config.serverUrl + "place/" + S.place.id + "/attribute/",
				type: 'PUT',
				data: {
					key: this.dataset.key,
					value: this.value
				}
			};

			defObjs.push( $.ajax(requestOptions) );
		});

		$(".new-attribute-key").each(function(){
			if (this.value == "") {
				showError();
				return;
			}

			var requestOptions = {
				url: vineyard.config.serverUrl + "place/" + S.place.id + "/attribute/",
				type: 'POST',
				data: {
					key: this.value,
					value: $(this).parents("tr").find(".new-attribute").val()
				}
			};

			console.log(requestOptions);

			defObjs.push( $.ajax(requestOptions) );

		});

		$.when(defObjs).always(resetForm);
	};

	if (data != "")
		$.ajax({
			type: 'PUT',
			url: vineyard.config.serverUrl + "place/" + S.place.id,
			data: data
		})
		.done(saveAttributes)
		.fail(showError);
	else saveAttributes();
};

var loadPlaceModification = function (id) {

	var placeRequest = loadPlace(id);
	var loadParentPlaceRequest = loadParentPlaces(id);

	$.when(placeRequest, loadParentPlaceRequest).then(function () {

		var selectedOpt = $("#place-parent select option[value='" + S.place.parent + "']")[0]
		if (selectedOpt) selectedOpt.selected = "selected";

		$("#control-ok").on("click", commitPlaceChanges);
		S.newPhoto = $("#new-photo");
		S.newPhoto.on("change", addPhoto);

		$("#place-photo .add, #place-photo .modify").on("click", function() { S.newPhoto.click(); });
		$("#place-photo .delete").on("click", deletePhoto);

		$("input, select").not("#new-photo").on("change", function () {
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
	var id = $("#place-id").val();

	S.controls = $(".controls");
	S.controls.css("visibility", "hidden");
	S.placeAddAttribute = $("#place-add-attribute");
	S.placeAttributes = $("#place-attributes");

	$("#control-cancel").on("click", function () {
		window.location = "/place/" + id;
	});

	if (id == "insert")
		loadPlaceInsertion();
	else loadPlaceModification(id);
};

init();