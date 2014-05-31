// some shared state
var S = {};

///////////////////////////////
/// TASK INSERTION
///////////////////////////////

var insertTask = function () {
	// TODO minimum validation!
	var requestedUrl = vineyard.config.serverUrl + "task/";
	
	S.taskDueTime = $("#task-due-time");
	var origValue = S.taskDueTime.val();
	var origType = S.taskDueTime.attr("type");
	
	S.taskDueTime.attr("type", "hidden");
	S.taskDueTime.val(origValue + " 00:00:00");
	
	$.post(requestedUrl, $("form").serialize(), function (data, e, xhr) {
		
		data = $.parseJSON(data);

		if (xhr.status != 201) {
			console.log(data);
			return;
		}
		
		window.location = "/task/" + data.id;
	});
	
	S.taskDueTime.attr("type", origType)
	S.taskDueTime.val(origValue);
};

var loadTaskInsertion = function () {
	var requestedUrl = vineyard.config.serverUrl + "task/";
	$("#task-id").remove();
	$("#task-status").parents("tr").remove();
	$("#task-revisions").remove();
	$("#changes").remove();
	
	loadPlaces();
	$("#task-place").attr("name", "place");
	
	loadWorkers();
	loadGroups();
	
	$("input, select").on("change", function () { this.name = this.dataset.name; });	
	$("#task-assigned-to").on("change", function () {
		if (this.selectedIndex == 0) {
			this.removeAttribute("name");
			return;
		}
		
		this.name = this.selectedOptions[0].parentNode.dataset.name;
	});
	$("#control-ok").on("click", insertTask);
};

///////////////////////////////
/// TASK MODIFICATION
///////////////////////////////
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
			$("#task-assign-group").append(option);
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
			$("#task-assign-worker").append(option);
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
			$("#task-place").append(option);
        });
	});
};

var loadTask = function(id) {
	
	var workersRequest = loadWorkers();
	var groupsRequest = loadGroups();
	var placesRequest = loadPlaces();
	
	requestedUrl = vineyard.config.serverUrl + "task/" + id + window.location.search;
    return $.getJSON(requestedUrl, function (task) {
		S.task = task;
        // Name
        $("#task-title").val(task.title); 
        // Description
        $("#task-description").val(task.description);     
        // Position Link
        if (task.latitude !== undefined)
            $("#task-location-link a").text("Mostra mappa").on("click", showMap);
		
		// Status
		$("#task-status").addClass(task.status).text(task.status);
		
		// Priority
		if (task.priority)
			$("#task-priority option[value='" + task.priority + "']").attr("selected", "selected")
		
		// Place
		$.when( placesRequest ).done(function(){
			$("#task-place option[value='" + task.place + "']").attr("selected", "selected")
		});
		
		// Assign time
		if (task.assign_time != null)
			$("#task-assign-th").append('<span class="assign-time">' + task.assign_time + '</span>');

		// Assigned worker
		if (task.assigned_worker != null)
			$("#task-assign-worker option[value='" + task.assigned_worker + "']").attr("selected", "selected");
		// Assigned group
		else if (task.assigned_group != null)
			$("#task-assign-group option[value='" + task.assigned_group + "']").attr("selected", "selected");
		
		// Due time
		if (task.due_time != null)
			$("#task-due-time").val(task.due_time.substring(0, 10));
		
		// Revisions & Last changed
		var requestedUrl = vineyard.config.serverUrl + "task/" + id + "/revs";
		$.getJSON(requestedUrl, function(revisions) {
			
			$.when( workersRequest ).done(function() {
				// Last changed
				$("#change-date").text(task.start_time);
				$("#modifier").attr("href", "/worker/" + task.modifier).text(S.workers[task.modifier].name);
				
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
					var li = '<li' + current + '><a href="/task/' + task.id + '/?rev=' + encodeURIComponent(rev.end_time) + '">' +
						rev.end_time + ' (' + S.workers[rev.modifier].name + ')</a></li>';

					$revUl.append(li);
				});
				
				if (currentRevision)
					$("#revisions ul li:first-child").addClass("current");
			});
		});
    });
};

var commitTaskChanges = function () {
	
	S.taskDueTime = $("#task-due-time");
	adjustDueTime = (S.taskDueTime.attr("name") != null);
	if (adjustDueTime)
	{
		var origValue = S.taskDueTime.val();
		var origType = S.taskDueTime.attr("type");
	
		S.taskDueTime.attr("type", "hidden");
		S.taskDueTime.val(origValue + " 00:00:00");
	}
	
	var data = $("form").serialize();
	
	if (adjustDueTime) {
		S.taskDueTime.attr("type", origType)
		S.taskDueTime.val(origValue);
	}
	
	if (data != "")
		$.ajax({
			type: 'PUT',
			url: vineyard.config.serverUrl + "task/" + S.task.id,
			data: data,
			success: function() { window.location.reload(); }
		}).fail(showError);
};


var loadTaskModification = function (id) {
	
	var taskRequest = loadTask(id);
	
	S.controls.css("visibility", "hidden");
	$.when(taskRequest).then(function () {
		
		$("#control-ok").on("click", commitTaskChanges);
		
		$("input, select").on("change", function () {
			this.name = this.dataset.name;
			S.controls.css("visibility", "visible");
		});		
	});
};

var init = function() {
	// get id from url
	var id = $("#task-id").val();	
	$("#task-modifier").val(sessionStorage.getItem("workerId"));
	
	S.controls = $(".controls");
	
	$("#control-cancel").on("click", function () {
		window.location.reload();
	});
	
	if (id == "insert")
		loadTaskInsertion();
	else loadTaskModification(id);
};

init();