// some shared state
var S = {};

///////////////////////////////
/// TASK INSERTION
///////////////////////////////

var insertIssue = function () {
	// TODO minimum validation!
	var requestedUrl = vineyard.config.serverUrl + "issue/";
	
	S.issueDueTime = $("#issue-due-time");
	var origValue = S.issueDueTime.val();
	var origType = S.issueDueTime.attr("type");
	
	S.issueDueTime.attr("type", "hidden");
	S.issueDueTime.val(a + " 00:00:00");
	
	$.post(requestedUrl, $("form").serialize(), function (data, xhr) {
		
		data = $.parseJSON(data);
		
		if (xhr.status != 201) {
			console.log(data);
			return;
		}
		
		window.location = "/issue/" + data.id;
	});
	
	S.issueDueTime.attr("type", origType)
	S.issueDueTime.val(origValue);
};

var loadIssueInsertion = function () {
	var requestedUrl = vineyard.config.serverUrl + "issue/";
	$("#issue-id").remove();
	$("#issue-status").parents("tr").remove();
	$("#issue-revisions").remove();
	$("#changes").remove();
	
	// TODO lasciare la scelta della location anche per i issue?
	$("#issue-location-link").parents("tr").remove();
	
	loadPlaces();
	$("#issue-place").attr("name", "place");
	
	loadWorkers();
	loadGroups();
	
	$("input, select").on("change", function () { this.name = this.dataset.name; });	
	$("#issue-assigned-to").on("change", function () {
		if (this.selectedIndex == 0) {
			this.removeAttribute("name");
			return;
		}
		
		this.name = this.selectedOptions[0].parentNode.dataset.name;
	});
	$("#control-ok").on("click", insertIssue);
};

///////////////////////////////
/// TASK MODIFICATION
///////////////////////////////

var showMap = function () {
    // TODO
    console.log("showMap NOT IMPLEMENTED");
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
        // Description
        $("#issue-description").val(issue.description); 
        // Position Link
        if (issue.latitude !== undefined)
            $("#issue-location-link a").text("Mostra mappa").on("click", showMap);
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