// some shared state
var S = {};

///////////////////////////////
/// ISSUE MODIFICATION
///////////////////////////////

var showLocationPicker = function () {

	if (S.locationPickerLoaded == null) {
		S.latitude = $("#issue-latitude");
		S.longitude = $("#issue-longitude");
		
		$("#location-picker").locationpicker({
			location: { latitude: S.latitude.val(), longitude: S.longitude.val() },
			radius: 0,
			inputBinding: {
				latitudeInput: S.latitude,
				longitudeInput: S.longitude
			},
		 	onchanged: function () {
			  	S.controls.css("visibility", "visible");
				$("#issue-latitude, #issue-longitude").each(function(){
					this.name = this.dataset.name;
				});
			}
		});

		S.locationPickerLoaded = true;
		S.showLocationPicker = $("#show-location-picker");
		S.hideLocationPicker = $("#hide-location-picker");
		S.hideLocationPicker.on("click", hideLocationPicker);
		
	}

	$("#revisions").addClass("show-location");
	$("#issue-latitude, #issue-longitude").attr("type", "text");
	S.hideLocationPicker.css("visibility", "visible");
  	S.showLocationPicker.hide();
  
};

var hideLocationPicker = function () {
	$("#revisions").removeClass("show-location");
	$("#issue-latitude, #issue-longitude").attr("type", "hidden");
	S.hideLocationPicker.css("visibility", "hidden");
 	S.showLocationPicker.show();
};

var showError = function () {
    // TODO
    console.log("Something gone wrong..");
	console.log(arguments);
};

var resetForm = function () {
	S.controls.css("visibility", "hidden");
};

var loadGroups = function () {
	var requestedUrl = vineyard.config.serverUrl + "group/";
	return $.getJSON(requestedUrl, function(groups) {
		S.groups = {};
		 $.each(groups, function (index, group) {
        	S.groups[group.id] = group;
			var option = '<option value="' + group.id + '">' + group.name + '</option>';
			$("#issue-assign-group").append(option);
        });
	});
};

var loadWorkers = function () {
	var requestedUrl = vineyard.config.serverUrl + "worker/";
	return $.getJSON(requestedUrl, function(workers) {
		S.workers = {};
		 $.each(workers, function (index, worker) {
        	S.workers[worker.id] = worker;
			var option = '<option value="' + worker.id + '">' + worker.name + '</option>';
			$("#issue-assign-worker").append(option);
        });
	});
};

var loadPlaces = function () {
	var requestedUrl = vineyard.config.serverUrl + "place/";
	return $.getJSON(requestedUrl, function (places) {
		S.places = {};
        $.each(places, function (index, place) {
        	S.places[place.id] = place;
			var option = '<option value="' + place.id + '">' + place.name + '</option>';
			$("#issue-place").append(option);
        });
	});
};

var loadIssue = function(id) {
	
	var workersRequest = loadWorkers();
	var groupsRequest = loadGroups();
	var placesRequest = loadPlaces();
	
	requestedUrl = vineyard.config.serverUrl + "issue/" + id + window.location.search;
    return $.getJSON(requestedUrl, function (issue) {
		S.issue = issue;
        // Name
        $("#issue-title").val(issue.title); 
		
		// Photos
		if (issue.photos != null)
			for (var i = 0, l = issue.photos.length; i < l; i++)
				$("#issue-photos")
				.append('<a target="_blank" href="http://vineyard-server.no-ip.org/api/photo/' + issue.photos[i] + '"><img src="http://vineyard-server.no-ip.org/api/photo/' + issue.photos[i] + '?h=100"></a>');
		
        // Description
        $("#issue-description").val(issue.description); 
        // Position Link
        if (issue.latitude !== undefined)
            $("#issue-location-link a").text("Mostra mappa").on("click", showLocationPicker); 
		else $("#issue-location-link").parents("tr").remove();
		
		// Status
		$("#issue-status").addClass(issue.status).text(issue.status);
		
		// Priority
		if (issue.priority)
			$("#issue-priority option[value='" + issue.priority + "']").attr("selected", "selected")
		
		// Place
		$.when( placesRequest ).done(function(){
			$("#issue-place option[value='" + issue.place + "']").attr("selected", "selected")
		});
		
		// Assign time
		if (issue.assign_time != null)
			$("#issue-assign-th").append('<span class="assign-time">' + issue.assign_time + '</span>');

		// Assigned worker
		if (issue.assigned_worker != null)
			$("#issue-assign-worker option[value='" + issue.assigned_worker + "']").attr("selected", "selected");
		// Assigned group
		else if (issue.assigned_group != null)
			$("#issue-assign-group option[value='" + issue.assigned_group + "']").attr("selected", "selected");
		
		// Due time
		if (issue.due_time != null)
			$("#issue-due-time").val(issue.due_time.substring(0, 10));
		
		// Revisions, Issuer, Last changed
		var requestedUrl = vineyard.config.serverUrl + "issue/" + id + "/revs";
		$.getJSON(requestedUrl, function(revisions) {
			
			$.when( workersRequest ).done(function() {
				// Last changed
				$("#change-date").text(issue.start_time);
				$("#modifier").attr("href", "/worker/" + issue.modifier).text(S.workers[issue.modifier].name);
				
				// Issuer
				$("#issuer").attr("href", "/worker/" + issue.issuer).text(S.workers[issue.issuer].name);
				
				var currentRevision = true;
				var $revUl = $("#revisions ul");
				$.each(revisions, function (index, rev) {
					if (rev.end_time == null)
						return;
					
					var current = "";
					if (rev.end_time == decodeURIComponent(window.location.search).replace("?rev=", "")) {
						currentRevision = false;
						current = ' class="current"';
					}
					var li = '<li' + current + '><a href="/issue/' + issue.id + '/?rev=' + encodeURIComponent(rev.end_time) + '">' +
						rev.end_time + ' (' + S.workers[rev.modifier].name + ')</a></li>';

					$revUl.append(li);
				});
				
				if (currentRevision)
					$("#revisions ul li:first-child").addClass("current");
			});
		});
    });
};

var commitIssueChanges = function () {
	
	S.issueDueTime = $("#issue-due-time");
	adjustDueTime = (S.issueDueTime.attr("name") != null);
	if (adjustDueTime)
	{
		var origValue = S.issueDueTime.val();
		var origType = S.issueDueTime.attr("type");
	
		S.issueDueTime.attr("type", "hidden");
		S.issueDueTime.val(origValue + " 00:00:00");
	}
	
	S.issueAssignTo = $("#issue-assigned-to");
	
	if ( S.issueAssignTo[0].selectedIndex == 0 ) {
		S.issueAssignTo.removeAttr("name");
		S.issueAssignTo.after('<input type="hidden" name="assigned_worker" value="" />');
		S.issueAssignTo.after('<input type="hidden" name="assigned_group" value="" />');
	} else if (S.issueAssignTo.attr("name") == "assigned_worker")
		S.issueAssignTo.after('<input type="hidden" name="assigned_group" value="" />');
	else if (S.issueAssignTo.attr("name") == "assigned_group")
		S.issueAssignTo.after('<input type="hidden" name="assigned_worker" value="" />');
	
	var data = $("form").serialize();
	
	if (adjustDueTime) {
		S.issueDueTime.attr("type", origType)
		S.issueDueTime.val(origValue);
	}
	
	if (data != "")
		$.ajax({
			type: 'PUT',
			url: vineyard.config.serverUrl + "issue/" + S.issue.id,
			data: data,
			success: function() { window.location.reload(); }
		}).fail(showError);
	
	S.issueAssignTo.siblings("input[type='hidden']").remove();

};


var loadIssueModification = function (id) {
	
	var issueRequest = loadIssue(id);
	
	S.controls.css("visibility", "hidden");
	$.when(issueRequest).then(function () {
		
		$("#control-ok").on("click", commitIssueChanges);
		
		$("input, select").on("change", function () {
			this.name = this.dataset.name;
			S.controls.css("visibility", "visible");
		});
		
		$("#issue-assigned-to").on("change", function () {
			if (this.selectedIndex == 0) {
				this.removeAttribute("name");
				return;
			}

			this.name = this.selectedOptions[0].parentNode.dataset.name;
		});
	});
};

var init = function() {
	// get id from url
	var id = $("#issue-id").val();	
	$("#issue-modifier").val(sessionStorage.getItem("workerId"));
	
	S.controls = $(".controls");
	
	$("#control-cancel").on("click", function () {
		window.location.reload();
	});
	
	if (id == "insert")
		loadIssueInsertion();
	else loadIssueModification(id);
};

init();
